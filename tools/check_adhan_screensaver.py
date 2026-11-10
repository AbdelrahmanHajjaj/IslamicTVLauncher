"""Real-TV adhan regression check. Plays audio, then returns Home; does not change settings.

Requires an enabled prayer service, adhan assets, and SystemUI's Somnambulator activity.
Run explicitly; the offline unit suite never connects to a TV. Raw evidence stays in state/.
"""
import argparse
import os
import re
import sys
import time
import xml.etree.ElementTree as ET

from device import Adb, ROOT


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def playing(audio, uid):
    # Only current player configurations, never the historical playback event log.
    return any("AudioPlaybackConfiguration" in line and f"u/pid:{uid}/" in line
               and "state:started" in line for line in audio.splitlines())


def preferences(xml):
    return {item.get("name"): (item.tag, item.get("value"), item.text) for item in ET.fromstring(xml)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tv", default=os.environ.get("TV"))
    parser.add_argument("--adb", default=os.environ.get("ADB_BIN"))
    parser.add_argument("--home-only", action="store_true", help="Check normal Home playback without a dream")
    parser.add_argument("--dismiss", choices=("back", "home"), default="back")
    args = parser.parse_args()
    adb = Adb(args.tv, args.adb)
    output = ROOT / "state" / ("adhan-screensaver-" + time.strftime("%Y%m%d-%H%M%S"))
    output.mkdir(parents=True, exist_ok=True)

    def capture(name, command):
        value = adb.shell(command)
        (output / (name + ".txt")).write_text(value, encoding="utf-8")
        return value

    prefs_command = "run-as tv.afnon.home cat shared_prefs/afnon.xml"
    before = preferences(capture("preferences-before", prefs_command))
    require(before.get("adhan", (None, "true", None))[1] != "false", "Enable adhan before this check")
    uid_match = re.search(r"package:tv\.afnon\.home uid:(\d+)", adb.shell("pm list packages -U tv.afnon.home"))
    require(uid_match is not None, "Launcher package not found")
    uid = uid_match.group(1)
    health = capture("service-before", "dumpsys accessibility")
    require(any("Bound services:" in line and "Afnon" in line for line in health.splitlines()),
            "Prayer service must be bound before the check")
    require(not playing(capture("audio-before", "dumpsys audio"), uid), "Stop launcher radio/audio before the check")
    saver = adb.shell("settings get secure screensaver_components")
    try:
        adb.shell("am start -W -n tv.afnon.home/.MainActivity --es test call")
        started = time.monotonic()
        if not args.home_only:
            adb.shell("am start -W -n com.android.systemui/.Somnambulator")
            time.sleep(2)
            dream = capture("dream-start", "dumpsys dreams")
            require(re.search(r"mCurrentDreamName=ComponentInfo\{", dream), "Selected screensaver did not start")
            power = capture("power-start", "dumpsys power")
            require("mWakefulness=Dreaming" in power, "TV must be dreaming before the call")
            require(time.monotonic() - started < 8, "Dream started too late for the delayed call; retry")
        time.sleep(max(0, 14 - (time.monotonic() - started)))
        # Both samples are beyond the five-second wake lock; playback must survive independently.
        for sample in range(2):
            dream = capture(f"dream-{sample}", "dumpsys dreams")
            require("mCurrentDreamName=null" in dream, "Screensaver still covers the adhan")
            activities = capture(f"activities-{sample}", "dumpsys activity activities")
            require(any("mResumedActivity:" in line and "tv.afnon.home/.NowActivity" in line
                        for line in activities.splitlines()), "Adhan screen is not resumed")
            require(playing(capture(f"audio-{sample}", "dumpsys audio"), uid), "Adhan playback stopped")
            if sample == 0:
                time.sleep(10)
        adb.shell("input keyevent " + ("4" if args.dismiss == "back" else "3"))
        time.sleep(2)
        require(not playing(capture("audio-dismissed", "dumpsys audio"), uid), "Dismissal left audio playing")
        require(saver == adb.shell("settings get secure screensaver_components"), "Screensaver selection changed")
    finally:
        try:
            capture("logcat", "logcat -b all -d -v threadtime")
        finally:
            adb.shell("input keyevent 224 3")
        time.sleep(1)
        after = preferences(capture("preferences-after", prefs_command))
        capture("service-after", "dumpsys accessibility")
        print("Private evidence:", output)
        require(before == after, "Preferences changed during the check; inspect the private snapshots")
    print("PASS: adhan remains visible and playing after wake; dismissal stops it; preferences unchanged.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (RuntimeError, ValueError, OSError) as error:
        print("FAIL:", error, file=sys.stderr)
        sys.exit(1)
