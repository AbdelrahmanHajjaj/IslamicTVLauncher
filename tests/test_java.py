"""Run pure Java guards without an Android runtime or a TV."""
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

class JavaChecks(unittest.TestCase):
    def test_appearance_rotation_policy(self):
        with tempfile.TemporaryDirectory() as output:
            source = ROOT / "app/src/main/java/tv/afnon/home"
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(source / "Tokens.java"), str(source / "AppearanceCycle.java"), str(ROOT / "tests/AppearanceCycleCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.AppearanceCycleCheck"], check=True, capture_output=True)

    def test_background_resolution_and_memory_budget(self):
        with tempfile.TemporaryDirectory() as output:
            source = ROOT / "app/src/main/java/tv/afnon/home"
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                *(str(source / name) for name in ("Tokens.java", "BackgroundSize.java")),
                str(ROOT / "tests/BackgroundSizeCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.BackgroundSizeCheck"], check=True, capture_output=True)

    def test_home_layout_modes_and_prayer_selection(self):
        with tempfile.TemporaryDirectory() as output:
            source = ROOT / "app/src/main/java/tv/afnon/home"
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                *(str(source / name) for name in ("Tokens.java", "FavouriteChoice.java", "HomeLayout.java")),
                str(ROOT / "tests/HomeLayoutCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.HomeLayoutCheck"], check=True, capture_output=True)

    def test_prayer_vectors_cover_each_prayer_in_order(self):
        import json
        import re
        import xml.etree.ElementTree as ET
        ns = "{http://schemas.android.com/apk/res/android}"
        names = ["fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"]
        canonical = json.loads((ROOT / "shared/design/prayer-icons.json").read_text())
        self.assertEqual([icon["id"] for icon in canonical["prayers"]], names)
        source = (ROOT / "app/src/main/java/tv/afnon/home/PrayerIcon.java").read_text()
        self.assertEqual(re.findall(r"R.drawable.ic_prayer_(\w+)", source), names)
        paths = []
        for i, name in enumerate(names):
            icon = ET.parse(ROOT / f"app/src/main/res/drawable/ic_prayer_{name}.xml").getroot()
            self.assertEqual((icon.get(ns + "viewportWidth"), icon.get(ns + "viewportHeight")), (str(canonical["size"]),) * 2)
            path = icon.find("path")
            self.assertEqual(path.get(ns + "fillColor"), "#00000000")
            self.assertGreater(float(path.get(ns + "strokeWidth")), 0)
            self.assertEqual(float(path.get(ns + "strokeWidth")), canonical["strokeWidth"])
            self.assertEqual(path.get(ns + "strokeColor"), "#FFFFFFFF")
            self.assertEqual(path.get(ns + "strokeLineCap"), "round")
            self.assertEqual(path.get(ns + "strokeLineJoin"), "round")
            self.assertEqual(path.get(ns + "pathData"), canonical["prayers"][i]["path"])
            paths.append(path.get(ns + "pathData"))
        self.assertTrue(all(paths))
        self.assertEqual(len(set(paths)), 6, "Each prayer needs a distinct glyph")

    def test_background_patterns_and_translations(self):
        import json
        import re
        import xml.etree.ElementTree as ET
        ns = "{http://schemas.android.com/apk/res/android}"
        java = ROOT / "app/src/main/java/tv/afnon/home"
        modes = re.findall(r'"([^"\n]+)"', re.search(r'BACKGROUNDS = \{([^}]+)', (java / "ThemeCatalog.java").read_text()).group(1))
        design = json.loads((ROOT / "shared/design/appearance.json").read_text())
        self.assertEqual(modes, design["proceduralBackgrounds"] + design["patterns"])
        labels = re.search(r'String\[\] backgroundNames\(\).*', (java / "Text.java").read_text(encoding="utf-8")).group(0)
        for language in re.findall(r'new String\[\] \{([^}]+)', labels):
            self.assertEqual(len(re.findall(r'"[^"]+"', language)), len(modes) + 1)
        mappings = re.findall(r'case "([^"]+)": pattern = R.drawable.(bg_\w+)', (java / "Appearance.java").read_text())
        self.assertEqual(len(mappings), 8)
        self.assertEqual(design["patterns"], [mode for mode, _ in mappings])
        for mode, resource in mappings:
            self.assertIn(mode, modes)
            import struct
            svg = ET.parse(ROOT / f"afnon-mihrab/patterns/{mode}.svg").getroot()
            self.assertEqual(svg.get("viewBox"), "0 0 480 270")
            path = svg.find("{http://www.w3.org/2000/svg}path")
            self.assertTrue(path.get("d"))
            self.assertGreater(float(path.get("stroke-width")), 0)
            png = (ROOT / f"app/src/main/res/drawable-nodpi/{resource}.png").read_bytes()
            self.assertEqual(png[:8], b"\x89PNG\r\n\x1a\n")
            self.assertEqual(struct.unpack(">II", png[16:24]), (1920, 1080), "Home must not enlarge a preview-resolution mask")
            self.assertLess(len(png), 200000, "Static pattern masks must stay small")
            self.assertFalse((ROOT / f"app/src/main/res/drawable/{resource}.xml").exists())

    def test_phone_package_and_home_guards(self):
        javac, java = shutil.which("javac"), shutil.which("java")
        self.assertIsNotNone(javac, "JDK 17 is required for local checks")
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([javac, "-encoding", "UTF-8", "-d", output,
                str(ROOT / "installer/src/main/java/tv/afnon/installer/Safety.java"),
                str(ROOT / "tests/SafetyCheck.java")], check=True, capture_output=True)
            subprocess.run([java, "-cp", output, "tv.afnon.installer.SafetyCheck"], check=True, capture_output=True)

    def test_theme_bounds_and_preset_contrast(self):
        import json
        import re
        source = ROOT / "app/src/main/java/tv/afnon/home"
        native = re.findall(r'"([^"]+)"', re.search(r'PRESETS = \{([^}]+)', (source / "ThemeCatalog.java").read_text()).group(1))
        self.assertEqual(native, json.loads((ROOT / "shared/design/themes.json").read_text()))
        browser = re.search(r'const presets:.*? = \{([^}]+)', (ROOT / "site/src/scripts/appearance.ts").read_text()).group(1)
        self.assertEqual(native, ["mihrab", *[name.strip() for name in browser.split(",")], "custom"])
        self.assertEqual(len(native) - 1, 20, "Twenty built-in themes; imported Custom is separate")
        assets = ROOT / "shared/design/assets/themes"
        self.assertEqual(set(native[1:-1]), {path.stem for path in assets.glob("*.json")})
        labels = re.search(r'String\[\] themeNames\(\).*', (source / "Text.java").read_text(encoding="utf-8")).group(0)
        for language in re.findall(r'new String\[\] \{([^}]+)', labels):
            self.assertEqual(len(re.findall(r'"[^"]+"', language)), len(native))
        javac, java = shutil.which("javac"), shutil.which("java")
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([javac, "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/ThemeSpec.java"),
                str(ROOT / "app/src/main/java/tv/afnon/home/ThemeCatalog.java"),
                str(ROOT / "app/src/main/java/tv/afnon/home/ThemeSurface.java"),
                str(ROOT / "app/src/main/java/tv/afnon/home/Tokens.java"),
                str(ROOT / "tests/ThemeCheck.java")], check=True, capture_output=True)
            subprocess.run([java, "-cp", output, "tv.afnon.home.ThemeCheck"], check=True, capture_output=True)
            for path in assets.glob("*.json"):
                with self.subTest(theme=path.stem):
                    preset = json.loads(path.read_text(encoding="utf-8"))
                    values = [*preset["colors"].values(), preset["background"], preset.get("surface", "classic")]
                    subprocess.run([java, "-cp", output, "tv.afnon.home.ThemeCheck", *values], check=True, capture_output=True)

    def test_remote_capture_waits_for_a_complete_press(self):
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/ButtonPress.java"),
                str(ROOT / "tests/ButtonPressCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.ButtonPressCheck"], check=True, capture_output=True)

    def test_prayer_offsets_and_next_day_across_clock_changes(self):
        import os
        cache = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle"))
        jars = list((cache / "caches/modules-2/files-2.1/com.batoulapps.adhan/adhan/1.2.1").glob("*/*.jar"))
        self.assertTrue(jars, "Build the launcher first to cache its prayer library")
        core = ROOT / "shared/core/build/libs/core-jvm.jar"
        self.assertTrue(core.is_file(), "Build the launcher first to compile the shared JVM core")
        stdlib = list((cache / "caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.2.10").glob("*/*.jar"))
        self.assertEqual(len(stdlib), 1, "Use the shared core's pinned Kotlin runtime")
        runtime = os.pathsep.join(map(str, [jars[0], core, stdlib[0]]))
        with tempfile.TemporaryDirectory() as output:
            out = Path(output)
            # Platform storage/clock/ICU and appearance are stubbed; Prefs, core and astronomy are real.
            sources = {
                "Context.java": "package android.content; public class Context { public static final int MODE_PRIVATE = 0; public SharedPreferences getSharedPreferences(String name, int mode) { throw new UnsupportedOperationException(); } }",
                "SharedPreferences.java": "package android.content; public interface SharedPreferences { int getInt(String k, int d); String getString(String k, String d); boolean getBoolean(String k, boolean d); Editor edit(); interface Editor { Editor putInt(String k, int v); Editor putString(String k, String v); Editor putBoolean(String k, boolean v); void apply(); } }",
                "DateFormat.java": "package android.text.format; public class DateFormat { public static boolean is24HourFormat(android.content.Context c) { return true; } }",
                "Screen.java": "package tv.afnon.home; final class Screen { static long secondsUntil(java.util.Date when, java.util.Date now) { return Math.max(0, (when.getTime() - now.getTime() + 999) / 1000); } }",
                "IslamicCalendar.java": "package android.icu.util; public class IslamicCalendar extends java.util.GregorianCalendar { public static final int RAMADAN = 8; public enum CalculationType { ISLAMIC_UMALQURA } public void setCalculationType(CalculationType t) {} }",
                "Appearance.java": "package tv.afnon.home; final class Appearance { static String key(android.content.Context c) { return \"\"; } static boolean reduced(android.content.Context c) { return false; } static int cycleMinutes(android.content.Context c) { return 0; } }",
            }
            for name, source in sources.items():
                (out / name).write_text(source, encoding="utf-8")
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-cp", runtime, "-d", output,
                *(str(out / name) for name in sources),
                str(ROOT / "app/src/main/java/tv/afnon/home/PrayerMethod.java"),
                str(ROOT / "app/src/main/java/tv/afnon/home/PrayerDay.java"),
                *(str(ROOT / "app/src/main/java/tv/afnon/home" / name) for name in
                  ("Prefs.java", "CityCatalog.java", "Tokens.java", "HomeLayout.java", "FavouriteChoice.java", "Reminders.java")),
                *(str(ROOT / "tests" / name) for name in ("PrayerDayCheck.java", "PrefsCheck.java", "RemindersCheck.java"))], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output + os.pathsep + runtime,
                "tv.afnon.home.PrayerDayCheck"], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output + os.pathsep + runtime,
                "tv.afnon.home.PrefsCheck"], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output + os.pathsep + runtime,
                "tv.afnon.home.RemindersCheck"], check=True, capture_output=True)

    def test_system_filter_keeps_media_games_and_store(self):
        import os
        sdk = Path(os.environ.get("ANDROID_HOME", os.environ.get("ANDROID_SDK_ROOT", Path.home() / "AppData/Local/Android/Sdk")))
        android = sdk / "platforms/android-34/android.jar"
        self.assertTrue(android.is_file(), "Set ANDROID_HOME to an SDK with Android platform 34")
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-cp", str(android), "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/AppFilter.java"),
                str(ROOT / "tests/AppFilterCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.AppFilterCheck"], check=True, capture_output=True)

    def test_live_tv_prefers_hardware_and_digital_tuners(self):
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/InputOrder.java"),
                str(ROOT / "tests/InputOrderCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.InputOrderCheck"], check=True, capture_output=True)

    def test_prayer_bar_stays_out_of_home_settings_sleep_and_saver(self):
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/DockPolicy.java"),
                str(ROOT / "tests/DockPolicyCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.DockPolicyCheck"], check=True, capture_output=True)

    def test_home_favourite_replacement_and_navigation(self):
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/FavouriteChoice.java"),
                str(ROOT / "tests/FavouriteChoiceCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.FavouriteChoiceCheck"], check=True, capture_output=True)
