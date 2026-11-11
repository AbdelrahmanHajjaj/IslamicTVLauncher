# Android TV shell

The native Java Views launcher keeps package `tv.afnon.home`, its existing settings
and debug signing configuration. The portable domain rules come from `shared/core`;
astronomy, Android ICU dates, persistence, scheduling, playback and UI stay here.

From the repository root, with JDK 17 and Android SDK 34 installed:

```sh
./gradlew -p platforms/android-tv verifyApkSize lintDebug --no-daemon
```

Set `ANDROID_HOME` to the installed SDK for this standalone build, or use an ignored
`platforms/android-tv/local.properties`. It consumes only this project, `shared/`,
local optional recordings under `state/media/`, and the root Gradle wrapper. It does
not configure the phone installer, preview or LG project. The output remains
`platforms/android-tv/build/outputs/apk/debug/app-debug.apk`, with a strict 25 MB limit.
Use the same signing key as the installed app when upgrading to preserve its data.

The repository's aggregate build keeps the existing task names:

```sh
./gradlew :app:assembleDebug :installer:assembleDebug --no-daemon
python3 -S -B -m unittest discover -s tests -v
```

See [contribution rules](../../CONTRIBUTING.md), [native integration notes](../../CLAUDE.md)
and [guarded installation and recovery](../../tools/device.md). Edit canonical data
and design sources in `shared/` and `afnon-mihrab/`; regenerate their Java/XML/PNG
outputs through the existing tools. Keep restricted audio and build/device state
ignored. Automated checks do not replace physical TV, sound and remote acceptance.

## Native runtime check

Build `./gradlew :app:assembleDebugAndroidTest`. From the repository root, on an
explicitly selected test device:

```sh
adb -s "$TV" install -r platforms/android-tv/build/outputs/apk/debug/app-debug.apk
adb -s "$TV" install -r -t platforms/android-tv/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "$TV" shell am instrument -w -e check prayer tv.afnon.home.test/tv.afnon.home.AppearanceCycleDeviceCheck
```

This explicit mode reuses `PrayerDayCheck.java` (also compiled by the root JVM
regressions), checks real ART/core/reminder linkage and validates Android ICU against
14 frozen calendar cases. They were cross-checked with the existing preview Intl
and LG Umm al-Qura adapters, covering DST/civil offsets, leap/year boundaries,
Ramadan and Friday. Update expected data only for an intentional calendar change.
The runner restores process timezone defaults and does not write preferences, open
UI, schedule alerts or play sound. Omitting `-e check prayer` retains the existing
appearance-cycle UI test. Emulator results describe that image/API, not a physical TV or its older ICU.
