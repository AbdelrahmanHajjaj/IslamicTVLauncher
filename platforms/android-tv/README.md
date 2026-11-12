# Android TV shell

The native Java Views launcher keeps package `tv.afnon.home`, its existing settings
and debug signing configuration. The portable domain rules come from `shared/core`;
astronomy, Android ICU dates, persistence, scheduling, playback and UI stay here.

**Settings → Prayer times → Check online** makes one advisory AlAdhan comparison
for the selected city/method/school and today's civil day. It shows six calculated
times, six reference times and signed minute differences. Calculated times retain
saved offsets; provider values omit them. The schedule and settings remain local.
The HTTPS adapter validates response identity, ISO dates/offsets and six usable
times, with a 32 KiB body cap, no redirects/cache and a 12-second one-shot deadline.
Pause/Back cancel the request; returning offers manual retry. No account, polling,
new network dependency or background service is involved.

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

The build resolves Adhan Java 1.2.1's sources classifier and extracts its 19 unchanged
astronomy/data source files under `build/generated/adhan`. The owned `CalendarUtil`
adapter changes only minute rounding, delegating to the shared downward rule.
This retains the existing library parameters and formulas without upgrading the
runtime dependency. The full upstream MIT notice and source reference stay packaged.

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

Build `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest`. From the repository root, on an
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

Use `-e check ui` for UI contract conformance. It opens real settings rows in
Arabic/English, visits every section and the six-offset editor, and checks semantic
IDs, sections, kinds, canonical labels and complete binding/choice/gap coverage.
It visits countdown variants and temporarily seeds a valid custom-theme prerequisite.
It exercises five prayer switches, the room-direction editor and actual favourite
menus/replacement, including save/cancel, visible state and return focus. Favourite
tests need four launchable apps plus the test APK's fixture entry; that entry is
checked as removable but never launched or uninstalled. The runner temporarily
sets bounded UI prerequisites, then restores the complete original preference
object. It does not play test audio or change TV integrations. D-pad injection
establishes remote focus mode, with further key codes dispatched through the real
Activity handler; physical remote/firmware delivery remains a separate hardware check.
App CI runs this mode on an API 34 emulator. Shell omissions are declared in
`src/androidTest/assets/ui-{gaps,choice-gaps}.json`; see the generated [gap report](../../docs/UI-GAPS.md).

Use `-e check online` for the comparison's strict JSON/ISO, 19-method mapping,
offset/civil-Fajr, bounded transport/deadline and bilingual rendered UI checks.
Negative HTTPS cases use fake connections; UI checks restore all preferences and
the process timezone. They exercise the actual Settings action, D-pad return focus,
12/24-hour formats, cancellation and an external fixture's pause/resume interruption.
Use `-e check online-network` separately for one real verified HTTPS request to
AlAdhan. This sends the fixed public Cairo test coordinates/date/method/school.
Emulator proof does not validate a physical Android TV's firmware or network stack.
