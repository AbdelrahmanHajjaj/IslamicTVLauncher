"""Device changes against a fake TV; never connects to ADB."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("device", ROOT / "tools" / "device.py")
device = importlib.util.module_from_spec(spec)
spec.loader.exec_module(device)


class GuardTest(unittest.TestCase):
    def test_core_packages_cannot_be_disabled(self):
        for package in ("android", "com.android.systemui", "com.android.ext.services",
                        "com.google.android.modulemetadata", "com.android.providers.media",
                        "com.android.vending", "tv.afnon.home"):
            with self.subTest(package=package), self.assertRaises(ValueError):
                device.validate_disable(package)

    def test_shell_syntax_is_not_a_package(self):
        for package in ("com.example;reboot", "com.example\nreboot", "-x", "com.example app"):
            with self.subTest(package=package), self.assertRaises(ValueError):
                device.validate_package(package)

    def test_normal_optional_app_is_allowed(self):
        self.assertEqual(device.validate_disable("com.example.optional"), "com.example.optional")

    def test_factory_activity_cannot_be_home(self):
        for component in ("com.cvte.tv.hkcfac/.FactoryActivity", "mediatek.factorymenu.ui/.Main",
                          "com.android.tv.settings/.FallbackHome", "android/com.android.internal.app.ResolverActivity"):
            self.assertFalse(device.usable_home(component))


class FakeAdb:
    target = "test-tv"
    def __init__(self):
        self.enabled = {"com.google.android.apps.tv.launcherx", device.PACKAGE, "com.example.optional"}
        self.disabled = {"com.example.alreadyoff"}
        self.current_home = "com.google.android.apps.tv.launcherx/.Home"
        self.settings = {("secure", "enabled_accessibility_services"): "other.app/.Reader",
                         ("secure", "screensaver_components"): "com.google.android.apps.tv.dreamx/.Backdrop"}
        self.mutations = []
        self.reject_start = False
        self.reject_home = False
        self.wifi = "cname=com.android.settings"
        self.cvte = False

    def shell(self, command):
        import shlex
        args = shlex.split(command)
        if args[:2] == ["content", "call"]: return "Bundle[{restored=true}]"
        if args[:2] == ["content", "insert"]: return ""
        if args[:2] == ["pm", "grant"]: return ""
        if args[:2] == ["appops", "set"]: return ""
        if args[:2] == ["getprop", "ro.serialno"]:
            return "fake-device"
        if args[:2] == ["getprop", "ro.build.version.sdk"]:
            return "30"
        if args[0] == "getprop":
            return "fake"
        if args[:3] == ["pm", "list", "packages"]:
            names = self.disabled if "-d" in args else self.enabled if "-e" in args else self.enabled | self.disabled
            if self.cvte:
                names = names | {"com.cvte.tv.api.impl"}
            return "\n".join("package:" + p for p in sorted(names))
        if args[:3] == ["cmd", "package", "resolve-activity"]:
            return self.current_home
        if args[:3] == ["cmd", "package", "query-activities"]:
            return self.current_home + "\n" + device.HOME
        if args[:2] == ["settings", "list"]:
            return "\n".join(k + "=" + v for (ns, k), v in self.settings.items() if ns == args[2])
        if args[:2] == ["settings", "get"]:
            return self.settings.get((args[2], args[3]), "null")
        if args[:2] == ["dumpsys", "wifi"]:
            return self.wifi
        if args[:2] == ["dumpsys", "package"]:
            return "requested permissions:\n install permissions:"
        if args[:2] == ["dumpsys", "activity"]:
            return "mResumedActivity: ActivityRecord{0 " + self.current_home + "}"
        if args[:3] == ["am", "start", "-W"]:
            if self.reject_start:
                return "Error: Activity not started"
            return "Status: ok\nActivity: " + args[-1]
        self.mutations.append(command)
        if args[:3] == ["cmd", "package", "set-home-activity"]:
            if not self.reject_home or args[-1] != device.HOME:
                self.current_home = args[-1]
            return "Success"
        if args[:2] == ["pm", "disable-user"]:
            self.enabled.discard(args[-1])
            self.disabled.add(args[-1])
            return args[-1] + " new state: disabled-user"
        if args[:2] == ["pm", "enable"]:
            self.disabled.discard(args[-1])
            self.enabled.add(args[-1])
            return args[-1] + " new state: enabled"
        if args[:2] == ["settings", "put"]:
            self.settings[(args[2], args[3])] = args[4]
            return ""
        if args[:2] == ["settings", "delete"]:
            self.settings.pop((args[2], args[3]), None)
            return ""
        raise AssertionError("Unexpected fake-TV command: " + command)


class RecoveryTest(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.addCleanup(self.folder.cleanup)
        self.adb = FakeAdb()
        self.tv = device.Device(self.adb, Path(self.folder.name))

    def test_backup_retry_preserves_original_state(self):
        first = self.tv.backup()
        self.adb.current_home = device.HOME
        self.assertEqual(self.tv.backup()["home"], first["home"])
        self.assertIn("com.example.alreadyoff", first["disabled"])

    def test_failed_start_keeps_previous_launcher(self):
        self.adb.reject_start = True
        with self.assertRaises(RuntimeError):
            self.tv.activate(exclusive=True)
        self.assertIn("com.google.android.apps.tv.launcherx", self.adb.enabled)
        self.assertFalse(any("disable-user" in cmd for cmd in self.adb.mutations))

    def test_failed_home_switch_does_not_disable_stock(self):
        self.adb.reject_home = True
        with self.assertRaises(RuntimeError):
            self.tv.activate(exclusive=True)
        self.assertIn("com.google.android.apps.tv.launcherx", self.adb.enabled)

    def test_existing_home_update_keeps_previous_cleanup_and_service(self):
        self.tv.activate(exclusive=True)
        self.tv.configure()
        self.tv.disable("com.example.optional")
        enabled, disabled, settings = set(self.adb.enabled), set(self.adb.disabled), dict(self.adb.settings)
        shell = self.adb.shell
        starts = []
        def sleeping_tv(command):
            if command.startswith("am start -W"):
                starts.append(command)
                if len(starts) == 2: return "Error: Activity did not become visible"
            return shell(command)
        self.adb.shell = sleeping_tv
        self.tv.activate()
        self.assertEqual(len(starts), 1)
        self.assertEqual(self.adb.current_home, device.HOME)
        self.assertEqual(self.adb.enabled, enabled)
        self.assertEqual(self.adb.disabled, disabled)
        self.assertEqual(self.adb.settings, settings)

    def test_exclusive_home_then_restore(self):
        original = self.adb.current_home
        self.tv.activate(exclusive=True)
        self.assertEqual(self.adb.current_home, device.HOME)
        self.assertIn("com.google.android.apps.tv.launcherx", self.adb.disabled)
        self.tv.restore()
        self.assertEqual(self.adb.current_home, original)
        self.assertIn("com.example.alreadyoff", self.adb.disabled)
        self.assertEqual(self.adb.settings[("secure", "enabled_accessibility_services")], "other.app/.Reader")

    def test_wifi_owner_is_never_disabled(self):
        package = "com.google.android.tungsten.setupwraith"
        self.adb.enabled.add(package)
        self.adb.wifi = "cname=" + package
        with self.assertRaises(ValueError):
            self.tv.disable(package)
        self.assertIn(package, self.adb.enabled)

    def test_cvte_home_stays_enabled(self):
        self.adb.cvte = True
        with self.assertRaises(ValueError):
            self.tv.activate(exclusive=True)
        self.assertFalse(any("disable-user" in cmd for cmd in self.adb.mutations))

    def test_failed_backup_changes_nothing(self):
        import unittest.mock
        with unittest.mock.patch.object(device, "atomic_json", side_effect=OSError("disk full")):
            with self.assertRaises(OSError):
                self.tv.activate(exclusive=True)
        self.assertEqual(self.adb.mutations, [])

    def test_setting_restore_preserves_later_user_choice(self):
        self.tv.change_setting("secure", "screensaver_components", "tv.afnon.home/.AfnonDream")
        self.adb.settings[("secure", "screensaver_components")] = "other.dream/.Dream"
        self.tv.restore()
        self.assertEqual(self.adb.settings[("secure", "screensaver_components")], "other.dream/.Dream")

    def test_selected_screensaver_stays_enabled(self):
        self.adb.enabled.add("com.google.android.apps.tv.dreamx")
        with self.assertRaises(ValueError): self.tv.disable("com.google.android.apps.tv.dreamx")
        self.assertFalse(self.adb.mutations)

    def test_corrupt_recovery_file_changes_nothing(self):
        record = self.tv.backup()
        record["packages_changed"] = ["com.example;reboot"]
        device.atomic_json(self.tv.path, record)
        with self.assertRaises(ValueError): self.tv.restore()
        self.assertFalse(self.adb.mutations)

    def test_restore_keeps_accessibility_services_added_later(self):
        self.tv.configure()
        self.adb.settings[("secure", "enabled_accessibility_services")] += ":new.app/.Reader"
        self.tv.restore()
        self.assertEqual(self.adb.settings[("secure", "enabled_accessibility_services")], "other.app/.Reader:new.app/.Reader")
        self.assertEqual(self.adb.settings[("secure", "accessibility_enabled")], "1")

    def test_setting_disconnect_after_write_remains_recoverable(self):
        original = self.adb.settings[("secure", "screensaver_components")]
        shell = self.adb.shell
        def disconnected(command):
            result = shell(command)
            if command.startswith("settings put secure screensaver_components "):
                raise RuntimeError("Connection lost after TV applied the setting")
            return result
        self.adb.shell = disconnected
        with self.assertRaises(RuntimeError):
            self.tv.change_setting("secure", "screensaver_components", "tv.afnon.home/.AfnonDream")
        self.adb.shell = shell
        self.tv.restore()
        self.assertEqual(self.adb.settings[("secure", "screensaver_components")], original)

    def test_repeated_configure_keeps_later_screensaver_choice(self):
        self.tv.configure()
        self.adb.settings[("secure", "screensaver_components")] = "other.dream/.Dream"
        self.tv.configure()
        self.tv.restore()
        self.assertEqual(self.adb.settings[("secure", "screensaver_components")], "other.dream/.Dream")

    def test_optional_vendor_grant_failure_keeps_prayer_service(self):
        shell = self.adb.shell
        def unsupported(command):
            if command.startswith("appops set "): raise RuntimeError("Unknown operation")
            return shell(command)
        self.adb.shell = unsupported
        import unittest.mock
        with unittest.mock.patch("sys.stderr"):
            self.tv.configure()
        self.assertIn(device.PACKAGE + "/.RemoteKeys", self.adb.settings[("secure", "enabled_accessibility_services")])

    def test_imported_setting_order_does_not_leave_accessibility_on(self):
        self.adb.settings[("secure", "enabled_accessibility_services")] = ""
        self.adb.settings[("secure", "accessibility_enabled")] = "0"
        self.tv.configure()
        record = self.tv.backup()
        record["settings_changed"] = dict(reversed(list(record["settings_changed"].items())))
        device.atomic_json(self.tv.path, record)
        self.tv.restore()
        self.assertEqual(self.adb.settings[("secure", "enabled_accessibility_services")], "")
        self.assertEqual(self.adb.settings[("secure", "accessibility_enabled")], "0")

    def test_partial_handover_rolls_back(self):
        self.adb.enabled.add("com.gathin.tvhome")
        original = self.adb.current_home
        shell = self.adb.shell
        def fail_one(command):
            if command == "pm disable-user --user 0 com.google.android.apps.tv.launcherx":
                return "Error: not allowed"
            return shell(command)
        self.adb.shell = fail_one
        with self.assertRaises(RuntimeError): self.tv.activate(exclusive=True)
        self.assertEqual(self.adb.current_home, original)
        self.assertIn("com.gathin.tvhome", self.adb.enabled)
        self.assertIn("com.google.android.apps.tv.launcherx", self.adb.enabled)

class ThemeCommandTest(unittest.TestCase):
    def test_json_theme_passes_intact_and_requires_save_confirmation(self):
        import json, shlex
        from unittest.mock import patch
        document = {"version": 1, "background": "panorama", "lighting": 35}
        calls = []
        class ThemeAdb:
            target = "test-tv"
            reply = "Bundle[{saved=true}]"
            def __init__(self, *args): pass
            def run(self, *args): return "device"
            def shell(self, command):
                calls.append(command)
                parts = shlex.split(command)
                if parts[:2] == ["content", "call"]:
                    self_outer.assertEqual(json.loads(parts[parts.index("--arg") + 1]), document)
                    return self.reply
                return "Status: ok"
        self_outer = self
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "theme.json"
            path.write_text(json.dumps(document))
            with patch.object(device, "Adb", ThemeAdb), patch("sys.stdout"), patch("sys.stderr"):
                self.assertEqual(device.main(["--tv", "test-tv", "theme", str(path)]), 0)
                self.assertTrue(any(c.startswith("am start") for c in calls))
                calls.clear()
                ThemeAdb.reply = "usage: content\n[ERROR] Binding not well formed"
                self.assertEqual(device.main(["--tv", "test-tv", "theme", str(path)]), 1)
                self.assertFalse(any(c.startswith("am start") for c in calls))


if __name__ == "__main__":
    unittest.main()
