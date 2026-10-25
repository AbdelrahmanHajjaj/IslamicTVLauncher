# Local validation — 25 October 2026

Tested on TCL 50C641K, Android 11/API 30 confirmed over ADB, firmware V8-R51MT02-LF1V903.021315. All work stayed local; no push, fetch, pull or other remote Git operation was performed. Existing CleanYourTV working changes were left alone.

## Passed locally

- `python -S -B -m unittest discover -s tests -v`: 26 tests. Covers package/Home guards, failed and partial handovers, durable backups, interrupted settings writes, preserving later user choices, exported setting order, Java safety rules, theme bounds/contrast, complete remote-button press capture, prayer offsets across UK clock changes, favourite navigation/replacement and Android 11 theme-import failure reporting.
- `./gradlew --offline --no-daemon :app:assembleDebug :installer:assembleDebug :app:lintDebug :installer:lintDebug`: both APKs built; lint reports 0 errors (65 launcher warnings and 14 installer warnings remain). Later launcher changes were rebuilt and linted separately.
- In `site/`, `npm run build` and `npm run tokens:check`: passed. Local browser review covered English/Arabic theme and device controls; the latest offsets, app-lighting and favourite controls were tested in the native TV app instead. The preview still has its earlier four-slot layout.
- Shell syntax checked for install/disable/enable/restore/env scripts; Windows install script parsed successfully.
- `git diff --check`: passed. No new launcher runtime dependencies.

## Passed on the TV

- Saved the original package list, disabled list, Home, settings and properties before installing. Recovery data and screenshots remain private and ignored by Git.
- Afnon became default Home. The first reboot returned to Afnon with its prayer/button service bound. The user confirmed physical Home, D-pad, OK, Back and held volume, plus Settings/input after enabling the service.
- The user heard both Quran radio and a test adhan; Back dismissed the adhan to Home. The APK contains 14 adhan recordings and 12 locally generated spoken Arabic reminder/announcement clips. Individual spoken clips were not all auditioned.
- Imported Aurora's panorama through the permission-checked provider call and read the saved theme back. Android 11 rejects JSON colons in `content --bind` while returning process status zero; the tool now uses `content call --arg` and requires `saved=true`.
- Restored the old favourites from the private CleanYourTV screenshot, then preserved the user's subsequent edits. Home supports four large favourites and up to five additional favourites on the lower row.
- The native app menu opened Android's uninstall confirmation for a removable app. The check cancelled it and verified the app remained installed. System apps do not get this action. No apps were uninstalled during setup.
- From the long-press menu, **Replace a favourite** opened Home; selecting a lower-row tile saved the exact replacement. The same flow restored the original tile and order. Back cancellation preserved the saved favourites.
- Native **Prayer time offsets** saved Fajr +1 minute and then reset it to zero. All offsets were left at zero. Leamington Spa was selected, with Muslim World League calculation and Standard Asr; the user can change all three choices. Coordinates came from [Mapcarta/OpenStreetMap](https://mapcarta.com/Leamington_Spa).
- **App focus lighting** is a separate off-by-default toggle and was left off at the user's request. Aurora panorama remains selected.
- After Home and remote checks, exclusive handover disabled only CleanYourTV. The previously disabled packages remained disabled; Play Store, Play Services, existing apps and Google Backdrop remained available. Android's system FallbackHome remains enabled as a system component.
- A second reboot, with CleanYourTV disabled, returned automatically to Afnon and rebound its prayer/button service. The disabled list and Google Backdrop selection were unchanged. No Afnon crash appeared in the post-reboot crash buffer.

## Outputs and recovery

- Launcher: `app/build/outputs/apk/debug/app-debug.apk` (19,807,267 bytes with the local audio).
- Phone installer: `installer/build/outputs/apk/debug/installer-debug.apk` (13,177,857 bytes). Choose **Install a local Afnon APK** to install this branch's launcher; upstream downloads contain upstream code.
- Audio and APKs remain ignored by Git. See [setup and recovery](../tools/device.md) and [themes and buttons](THEMES.md).
- `tools/device.py restore` with the same private backup folder restores the previous CleanYourTV setup, including only packages/settings changed by this setup. It does not undo the earlier cleanup or uninstall Afnon.

## Remaining checks

The physical remote check after the final exclusive-Home reboot and turning off/revoking debugging are awaiting the user. Network ADB closure must be verified afterward.

Not tested: physical branded-button reassignment on this TCL firmware, Bluetooth pairing/voice, saver idle/wake for each choice, service recovery after force-stop, a real restore operation or the phone installer on a phone. All D-pad edges were checked by the pure Java favourite-selection test; a complete native Arabic/English focus sweep remains outstanding. Google's Backdrop selection was preserved, not a claim that all Google photo options were tested.

Performance has not been benchmarked under controlled conditions; no speed improvement is claimed. Fixed prayer offsets do not import a seasonal mosque timetable and remain set if the location or method changes. Location discovery is not implemented.
