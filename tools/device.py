#!/usr/bin/env python3
"""Local ADB setup, recovery and themes for Afnon. Standard library only.

Package and Home guards adapted from CleanYourTV (Copyright 2026 Gathin, MIT).
No operation connects until this tool is explicitly run with a TV target.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import shutil
import subprocess
import sys
import time

PACKAGE = "tv.afnon.home"
HOME = PACKAGE + "/.MainActivity"
ROOT = Path(__file__).resolve().parents[1]
PACKAGE_RE = re.compile(r"[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z0-9_]+)+")
COMPONENT_RE = re.compile(r"[A-Za-z_][\w.]*/[\w.$]+")
CORE_RE = re.compile(
    r"^(?:android|tv\.afnon\.home|com\.android\.(?:systemui|shell|phone|settings|tv\.settings|vending|"
    r"externalstorage|providers\..*|inputmethod\..*|networkstack.*)|"
    r"com\.(?:google\.)?android\.(?:permissioncontroller|packageinstaller|ext\.services|ext\.shared|"
    r"modulemetadata|overlay\.modules.*|gms|gsf|inputmethod\..*|tv\.remote\.service))$")
FORBIDDEN_HOME = ("factory", "hkcfac", "fallbackhome", "resolveractivity", "setupwraith", "provision")
SETTINGS = (("secure", "screensaver_components"), ("secure", "screensaver_enabled"),
            ("secure", "sleep_timeout"), ("secure", "enabled_accessibility_services"),
            ("secure", "accessibility_enabled"), ("global", "CustomKeyList"))


def validate_package(name):
    if not isinstance(name, str) or not PACKAGE_RE.fullmatch(name):
        raise ValueError("Not a package name")
    return name


def validate_disable(name):
    if not isinstance(name, str): raise ValueError("Not a package name")
    if name == "android" or CORE_RE.fullmatch(name):
        raise ValueError("Protected package: " + name)
    validate_package(name)
    if any(word in name.lower() for word in ("bluetooth", "inputmethod", "networkstack")):
        raise ValueError("Protected system service: " + name)
    return name


def usable_home(component):
    return bool(COMPONENT_RE.fullmatch(component or "")) and not any(
        word in component.lower() for word in FORBIDDEN_HOME)


def packages(output):
    return {line.removeprefix("package:").strip() for line in output.splitlines()
            if line.startswith("package:")}


def atomic_json(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    temporary.replace(path)

STOCK_LAUNCHERS = {"com.google.android.apps.tv.launcherx", "com.google.android.tvlauncher",
                   "com.google.android.leanbacklauncher", "com.mitv.tvhome", "com.gathin.tvhome"}


class Device:
    def __init__(self, adb, backup_dir):
        self.adb = adb
        self.directory = Path(backup_dir)
        self.path = self.directory / "recovery.json"

    def home(self):
        output = self.adb.shell("cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME")
        return next((line.strip() for line in reversed(output.splitlines())
                     if COMPONENT_RE.fullmatch(line.strip())), "")

    def enabled(self):
        return packages(self.adb.shell("pm list packages -e --user 0"))

    def setting(self, namespace, key):
        value = self.adb.shell("settings get " + namespace + " " + key).strip()
        return None if value == "null" else value

    def identity(self):
        serial = self.adb.shell("getprop ro.serialno").strip()
        return hashlib.sha256((serial if serial and serial != "unknown" else self.adb.target).encode()).hexdigest()

    def backup(self):
        identity = self.identity()
        if self.path.exists():
            record = json.loads(self.path.read_text(encoding="utf-8"))
            if record.get("version") != 1 or record.get("device") != identity:
                raise ValueError("Backup belongs to a different TV or format; choose its original backup folder.")
            if not isinstance(record.get("home"), str):
                raise ValueError("Invalid Home in recovery record")
            for key in ("enabled", "disabled", "packages_changed"):
                if not isinstance(record.get(key), list):
                    raise ValueError("Invalid package list in recovery record")
                for item in record[key]:
                    if item != "android": validate_package(item)
            for key in ("settings", "settings_changed"):
                if not isinstance(record.get(key), dict):
                    raise ValueError("Invalid settings in recovery record")
                for name, value in record[key].items():
                    if not re.fullmatch(r"(?:secure|global|system)/[A-Za-z_0-9]+", name) or (value is not None and not isinstance(value, str)):
                        raise ValueError("Invalid setting in recovery record")
            if not record["settings_changed"].keys() <= record["settings"].keys():
                raise ValueError("Missing original settings in recovery record")
            return record
        installed = packages(self.adb.shell("pm list packages --user 0"))
        if not installed:
            raise RuntimeError("TV returned no packages. Nothing changed.")
        record = {"version": 1, "device": identity, "home": self.home(),
                  "enabled": sorted(self.enabled()),
                  "disabled": sorted(packages(self.adb.shell("pm list packages -d --user 0"))),
                  "settings": {ns + "/" + key: self.setting(ns, key) for ns, key in SETTINGS},
                  "packages_changed": [], "settings_changed": {}}
        # TCL mappings need their values as well as the list, before the helper can touch them.
        for key in self.adb.shell("settings list global").splitlines():
            name = key.partition("=")[0]
            if re.fullmatch(r"CustomKey_\d+", name):
                record["settings"]["global/" + name] = self.setting("global", name)
        self.directory.mkdir(parents=True, exist_ok=True)
        for name, command in (("packages.txt", "pm list packages --user 0"),
                              ("disabled.txt", "pm list packages -d --user 0"),
                              ("getprop.txt", "getprop"),
                              ("settings-secure.txt", "settings list secure"),
                              ("settings-global.txt", "settings list global"),
                              ("settings-system.txt", "settings list system")):
            (self.directory / name).write_text(self.adb.shell(command), encoding="utf-8")
        atomic_json(self.path, record)
        return record

    def change_setting(self, namespace, key, value):
        record = self.backup()
        if self.setting(namespace, key) == value: return
        name = namespace + "/" + key
        if name not in record["settings"]:
            record["settings"][name] = self.setting(namespace, key)
        record["settings_changed"][name] = value
        atomic_json(self.path, record)
        command = ("settings delete " + namespace + " " + key if value is None else
                   "settings put " + namespace + " " + key + " " + shlex.quote(str(value)))
        self.adb.shell(command)
        if self.setting(namespace, key) != value:
            raise RuntimeError("TV did not apply " + key)

    def disable(self, package):
        validate_disable(package)
        if package not in self.enabled():
            return
        if package == self.home().partition("/")[0]:
            raise ValueError("Cannot disable the launcher that currently owns Home.")
        if package in STOCK_LAUNCHERS:
            if self.home().partition("/")[0] != PACKAGE:
                raise ValueError("Select and verify Afnon Home before disabling stock Home.")
            self.start(HOME)
            focused = self.adb.shell("dumpsys activity activities")
            if not any(PACKAGE + "/" in line for line in focused.splitlines()
                       if "mResumedActivity" in line or "mFocusedApp" in line or "topResumedActivity" in line):
                raise ValueError("Afnon is not visibly in front; stock Home stays enabled.")
        if package.startswith("com.cvte.tv.launcher"):
            raise ValueError("CVTE stock Home stays enabled until this exact board has been verified.")
        if package == "com.google.android.tungsten.setupwraith":
            owners = re.findall(r"cname=([^\s]+)", self.adb.shell("dumpsys wifi"))
            if not owners or any("setupwraith" in owner for owner in owners):
                raise ValueError("Setup wizard owns Wi-Fi, or its ownership could not be checked; it stays enabled.")
        dream = self.setting("secure", "screensaver_components") or ""
        if any(entry.partition("/")[0] == package for entry in dream.split(",")):
            raise ValueError("This package provides the selected screensaver; it stays enabled.")
        details = self.adb.shell("dumpsys package " + package)
        requested = details.partition("requested permissions:")[2].split("install permissions:")[0]
        if "android.permission.TV_INPUT_HARDWARE" in requested:
            raise ValueError("Package may drive HDMI/AV/Live TV inputs; it stays enabled.")
        record = self.backup()
        if package not in record["packages_changed"]:
            record["packages_changed"].append(package)
            atomic_json(self.path, record)
        output = self.adb.shell("pm disable-user --user 0 " + package)
        if "disabled-user" not in output or package not in packages(self.adb.shell("pm list packages -d --user 0")):
            raise RuntimeError("TV refused to disable " + package)

    def start(self, component):
        if not usable_home(component):
            raise ValueError("No safe Home component was identified.")
        out = self.adb.shell("am start -W -a android.intent.action.MAIN -c android.intent.category.HOME -n " + shlex.quote(component))
        if "Status: ok" not in out or re.search(r"^Error", out, re.M | re.I):
            raise RuntimeError("Launcher did not start. Previous Home remains available.")

    def set_home(self, component):
        if not usable_home(component):
            raise ValueError("Refusing an unknown, setup or factory Home.")
        self.adb.shell("cmd package set-home-activity --user 0 " + shlex.quote(component))
        if self.home() != component:
            # Android may return either a full class name or its .Short form.
            actual = self.home()
            def full(value):
                pkg, _, activity = value.partition("/")
                return pkg + "/" + (pkg + activity if activity.startswith(".") else activity)
            if full(actual) != full(component):
                raise RuntimeError("TV did not select the requested Home.")

    def activate(self, exclusive=False):
        sdk = self.adb.shell("getprop ro.build.version.sdk").strip()
        if not sdk.isdigit() or int(sdk) < 26:
            raise ValueError("Afnon needs Android 8/API 26 or newer; the TV version could not be accepted.")
        self.backup()
        before = self.home()
        installed = packages(self.adb.shell("pm list packages --user 0"))
        if exclusive and any(p.startswith("com.cvte.") for p in installed):
            raise ValueError("CVTE: leave stock enabled, verify Afnon Home and reboot before any remote helper changes.")
        if exclusive and not usable_home(before):
            raise ValueError("No previous working Home was identified; use the TV's Home picker first.")
        if exclusive and before.partition("/")[0] not in STOCK_LAUNCHERS | {PACKAGE}:
            raise ValueError("Unknown previous launcher. Select Afnon without disabling it first.")
        self.start(HOME)
        try:
            self.set_home(HOME)
            self.start(HOME)
            if exclusive:
                for package in sorted(self.enabled() & STOCK_LAUNCHERS):
                    self.disable(package)
                    self.set_home(HOME)
                self.start(HOME)
        except (RuntimeError, ValueError, OSError, subprocess.TimeoutExpired):
            try:
                self.restore()
            except Exception as recovery_error:
                raise RuntimeError("Home handover failed and recovery is incomplete. Keep ADB open; backup: "
                                   + str(self.path)) from recovery_error
            raise

    def restore(self):
        if not self.path.exists():
            raise ValueError("No recovery record. Refusing to guess the original Home or settings.")
        record = self.backup()
        # Only packages changed by this tool, only ones originally enabled.
        for package in record["packages_changed"]:
            validate_package(package)
            if package in record["enabled"]:
                self.adb.shell("pm enable --user 0 " + package)
                if package not in self.enabled():
                    raise RuntimeError("Could not re-enable " + package + "; recovery is incomplete.")
        original = record.get("home", "")
        if not usable_home(original):
            raise ValueError("Backup has no usable original Home. Afnon stays installed.")
        old_package = original.partition("/")[0]
        if old_package not in self.enabled():
            self.adb.shell("pm enable --user 0 " + validate_package(old_package))
        self.start(original)
        self.set_home(original)
        if "secure/enabled_accessibility_services" in record["settings_changed"]:
            result = self.adb.shell("content call --uri content://tv.afnon.home.setup --method restore_remote")
            if "restored=true" not in result:
                raise RuntimeError("Could not restore remote mappings. Keep the backup and Afnon installed.")
        changes = sorted(record["settings_changed"].items(), key=lambda item: item[0] != "secure/enabled_accessibility_services")
        for name, applied in changes:
            ns, key = name.split("/", 1)
            if ns not in ("secure", "global", "system") or not re.fullmatch(r"[A-Za-z_0-9]+", key):
                raise ValueError("Invalid setting in recovery record")
            current = self.setting(ns, key)
            old = record["settings"][name]
            if key == "enabled_accessibility_services" and current != applied:
                # Remove only our added service; preserve services enabled later by the household.
                before = (old or "").split(":")
                ours = {PACKAGE + "/.RemoteKeys", PACKAGE + "/" + PACKAGE + ".RemoteKeys"}
                if not ours.intersection(before):
                    updated = ":".join(s for s in (current or "").split(":") if s and s not in ours)
                    self.adb.shell("settings put secure enabled_accessibility_services " + shlex.quote(updated))
                continue
            if current != applied: continue
            if key == "accessibility_enabled" and self.setting("secure", "enabled_accessibility_services"):
                continue
            self.adb.shell("settings delete " + ns + " " + key if old is None else
                           "settings put " + ns + " " + key + " " + shlex.quote(old))
            if self.setting(ns, key) != old:
                raise RuntimeError("Could not restore " + key)
        self.start(original)


    def configure(self):
        """Keep the existing screensaver; add Afnon without removing other accessibility services."""
        self.backup()
        installed = packages(self.adb.shell("pm list packages --user 0"))
        if any(p.startswith("com.cvte.") for p in installed):
            raise ValueError("CVTE: verify Home and ADB after a reboot before enabling remote buttons in TV Settings.")
        self.adb.shell("pm grant " + PACKAGE + " android.permission.WRITE_SECURE_SETTINGS")
        for command in ("pm grant " + PACKAGE + " android.permission.RECORD_AUDIO",
                        "pm grant " + PACKAGE + " android.permission.ACCESS_FINE_LOCATION",
                        "appops set " + PACKAGE + " APP_AUTO_START allow",
                        "appops set " + PACKAGE + " APP_ASSOC_START allow"):
            try:
                self.adb.shell(command)
            except RuntimeError:
                print("Optional setup unavailable: " + command, file=sys.stderr)
        result = self.adb.shell("content insert --uri content://tv.afnon.home.setup/settings --bind name:s:remote_enabled --bind value:s:true")
        if "Exception" in result or "Error" in result: raise RuntimeError("Could not enable remote mappings")
        service = PACKAGE + "/.RemoteKeys"
        current = self.setting("secure", "enabled_accessibility_services") or ""
        entries = [s for s in current.split(":") if s]
        if not any(s in (service, PACKAGE + "/" + PACKAGE + ".RemoteKeys") for s in entries):
            entries.append(service)
        self.change_setting("secure", "enabled_accessibility_services", ":".join(entries))
        self.change_setting("secure", "accessibility_enabled", "1")

    def screensaver(self, component):
        self.backup()
        if component == "afnon": component = PACKAGE + "/.AfnonDream"
        if not COMPONENT_RE.fullmatch(component) or not usable_home(component):
            raise ValueError("Expected an installed screensaver component (package/service).")
        available = self.adb.shell("cmd package query-services --brief -a android.service.dreams.DreamService")
        def canonical(value):
            pkg, _, name = value.partition("/")
            return pkg + "/" + (pkg + name if name.startswith(".") else name)
        if canonical(component) not in {canonical(line.strip()) for line in available.splitlines()
                                       if COMPONENT_RE.fullmatch(line.strip())}:
            raise ValueError("TV did not report that screensaver as installed. Use TV screensaver settings.")
        self.change_setting("secure", "screensaver_components", component)
        self.change_setting("secure", "screensaver_enabled", "1")


class Adb:
    def __init__(self, target, binary=None):
        if not target or target.startswith("-") or re.search(r"\s", target):
            raise ValueError("Set --tv to the TV's ADB address or serial.")
        self.target = target
        self.binary = binary or os.environ.get("ADB_BIN") or shutil.which("adb")
        if not self.binary:
            roots = [os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT"),
                     str(Path.home() / "AppData/Local/Android/Sdk"), str(Path.home() / "Android/Sdk"),
                     str(Path.home() / "Library/Android/sdk")]
            self.binary = next((str(Path(root) / "platform-tools" / exe) for root in roots if root
                                for exe in ("adb", "adb.exe") if (Path(root) / "platform-tools" / exe).is_file()), None)
        if not self.binary: raise ValueError("Install Android platform-tools or set ADB_BIN.")

    def run(self, *args, timeout=60):
        completed = subprocess.run([self.binary, "-s", self.target, *args], capture_output=True,
                                   text=True, encoding="utf-8", errors="replace", timeout=timeout)
        if completed.returncode:
            raise RuntimeError((completed.stderr or completed.stdout).strip() or "ADB command failed")
        return completed.stdout.strip()

    def shell(self, command):
        return self.run("shell", command)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tv", default=os.environ.get("TV"), help="Connected ADB address/serial (or TV environment variable)")
    parser.add_argument("--adb", help="Path to adb")
    parser.add_argument("--backup-dir", type=Path, help="Use the same private folder for setup and restore")
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("backup", help="Save the original TV state locally, without changing it")
    install = commands.add_parser("install", help="Install a locally built APK and select Home")
    install.add_argument("--apk", type=Path, default=ROOT / "app/build/outputs/apk/debug/app-debug.apk")
    install.add_argument("--exclusive", action="store_true", help="Disable known stock launchers after verified handover")
    activate = commands.add_parser("home", help="Select the already-installed Afnon Home")
    activate.add_argument("--exclusive", action="store_true")
    commands.add_parser("configure", help="Enable the prayer/remote helper; preserve the selected screensaver")
    for verb in ("disable", "enable"):
        commands.add_parser(verb).add_argument("packages", nargs="+")
    commands.add_parser("restore", help="Restore recorded packages and settings; keep Afnon installed")
    dream = commands.add_parser("screensaver", help="List installed screensavers, or select one")
    dream.add_argument("component", nargs="?")
    theme = commands.add_parser("theme", help="Import a local JSON theme")
    theme.add_argument("file", type=Path)
    args = parser.parse_args(argv)
    try:
        adb = Adb(args.tv, args.adb)
        if adb.run("get-state") != "device": raise RuntimeError("TV is not connected and authorised in ADB.")
        folder = args.backup_dir or ROOT / "state/backups" / hashlib.sha256(adb.target.encode()).hexdigest()[:16]
        tv = Device(adb, folder)
        if args.command == "backup": tv.backup()
        elif args.command == "install":
            if not args.apk.is_file(): raise ValueError("Build the APK locally first: " + str(args.apk))
            sdk = adb.shell("getprop ro.build.version.sdk")
            if not sdk.isdigit() or int(sdk) < 26: raise ValueError("Afnon requires Android API 26 or newer.")
            tv.backup()
            result = adb.run("install", "-r", str(args.apk.resolve()), timeout=180)
            if not result.endswith("Success"): raise RuntimeError("Install failed; no Home or package changes made: " + result)
            tv.activate(args.exclusive)
        elif args.command == "home": tv.activate(args.exclusive)
        elif args.command == "configure": tv.configure()
        elif args.command == "restore": tv.restore()
        elif args.command == "disable":
            for package in args.packages: validate_disable(package)
            for package in args.packages: tv.disable(package)
        elif args.command == "enable":
            for package in args.packages: validate_package(package)
            for package in args.packages:
                adb.shell("pm enable --user 0 " + package)
                if package not in tv.enabled(): raise RuntimeError("TV refused to enable " + package)
        elif args.command == "theme":
            if args.file.stat().st_size > 16384: raise ValueError("Theme is too large")
            theme = json.loads(args.file.read_text(encoding="utf-8"))
            # Android 11's content --bind parser rejects JSON colons; --arg passes the document intact.
            result = adb.shell("content call --uri content://tv.afnon.home.setup --method set_theme --arg "
                               + shlex.quote(json.dumps(theme, separators=(",", ":"))))
            if "saved=true" not in result: raise ValueError("TV did not save the theme: " + result)
            tv.start(HOME)
        elif args.command == "screensaver":
            if args.component: tv.screensaver(args.component)
            else: print(adb.shell("cmd package query-services --brief -a android.service.dreams.DreamService"))
        print("Complete. Recovery folder: " + str(folder))
        return 0
    except (ValueError, RuntimeError, OSError, subprocess.TimeoutExpired) as error:
        print("Stopped: " + str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
