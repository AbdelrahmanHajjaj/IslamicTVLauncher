"""Compare compiled bilingual text with the pre-extraction output baseline."""
import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


def text_hashes():
    with tempfile.TemporaryDirectory() as output:
        out = Path(output)
        stubs = {
            "CalculationParameters.java": "package com.batoulapps.adhan; public class CalculationParameters { public double fajrAngle, ishaAngle; public int ishaInterval; }",
            "Madhab.java": "package com.batoulapps.adhan; public enum Madhab { HANAFI, SHAFI }",
            "PrayerMethod.java": "package tv.afnon.home; class PrayerMethod { String ar, en; com.batoulapps.adhan.CalculationParameters getParameters() { return null; } }",
            "Radio.java": "package tv.afnon.home; class Radio { static final int PLAYING=2, CONNECTING=1, RETRYING=3, PAUSED=4; static class Station { String ar, en, whereAr, whereEn; boolean reciter; } }",
        }
        for name, source in stubs.items():
            (out / name).write_text(source, encoding="utf-8")
        subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
            *(str(out / name) for name in stubs),
            str(ROOT / "app/src/main/java/tv/afnon/home/Text.java"),
            str(ROOT / "tests/TextCatalogCheck.java")], check=True, capture_output=True)
        keys = sorted(json.loads((ROOT / "shared/data/strings.json").read_text(encoding="utf-8")))
        result = subprocess.run([shutil.which("java"), "-cp", output, "tv.afnon.home.TextCatalogCheck", *keys],
            check=True, capture_output=True, text=True)
        return dict(line.split(":") for line in result.stdout.splitlines())


class TextChecks(unittest.TestCase):
    def test_catalog_preserves_both_languages_digits_and_array_independence(self):
        expected = json.loads((ROOT / "tests/fixtures/text-output-hashes.json").read_text())
        self.assertEqual(text_hashes(), expected)
