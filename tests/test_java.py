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
