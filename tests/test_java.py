"""Run pure Java guards without Android or a TV."""
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

class JavaChecks(unittest.TestCase):
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
        javac, java = shutil.which("javac"), shutil.which("java")
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([javac, "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/ThemeSpec.java"),
                str(ROOT / "tests/ThemeCheck.java")], check=True, capture_output=True)
            subprocess.run([java, "-cp", output, "tv.afnon.home.ThemeCheck"], check=True, capture_output=True)
            for path in (ROOT / "app/src/main/assets/themes").glob("*.json"):
                with self.subTest(theme=path.stem):
                    values = list(json.loads(path.read_text(encoding="utf-8"))["colors"].values())
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
        with tempfile.TemporaryDirectory() as output:
            out = Path(output)
            # Android-only display/context pieces are stubbed; all prayer calculations use the real library.
            sources = {
                "Context.java": "package android.content; public class Context {}",
                "IslamicCalendar.java": "package android.icu.util; public class IslamicCalendar extends java.util.GregorianCalendar { public static final int RAMADAN = 8; public enum CalculationType { ISLAMIC_UMALQURA } public void setCalculationType(CalculationType t) {} }",
                "Prefs.java": "package tv.afnon.home; import android.content.Context; import com.batoulapps.adhan.*; final class Prefs { static final int MAX_PRAYER_OFFSET = 30; static Coordinates where(Context c) { return null; } static PrayerMethod method(Context c) { return null; } static Madhab madhab(Context c) { return null; } static int hijriOffset(Context c) { return 0; } static int[] prayerOffsets(Context c) { return new int[6]; } }",
            }
            for name, source in sources.items():
                (out / name).write_text(source, encoding="utf-8")
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-cp", str(jars[0]), "-d", output,
                *(str(out / name) for name in sources),
                str(ROOT / "app/src/main/java/tv/afnon/home/PrayerMethod.java"),
                str(ROOT / "app/src/main/java/tv/afnon/home/PrayerDay.java"),
                str(ROOT / "tests/PrayerDayCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output + os.pathsep + str(jars[0]),
                "tv.afnon.home.PrayerDayCheck"], check=True, capture_output=True)

    def test_home_favourite_replacement_and_navigation(self):
        with tempfile.TemporaryDirectory() as output:
            subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                str(ROOT / "app/src/main/java/tv/afnon/home/FavouriteChoice.java"),
                str(ROOT / "tests/FavouriteChoiceCheck.java")], check=True, capture_output=True)
            subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.FavouriteChoiceCheck"], check=True, capture_output=True)
