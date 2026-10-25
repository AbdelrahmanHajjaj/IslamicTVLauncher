# Local validation — 25 October 2026

Target: TCL 50C641K, expected Android 11 (API 30), firmware V8-R51MT02-LF1V903.021315. The Android version remains unverified until ADB reports it. No TV was connected or changed during this work.

## Passed locally

- `python -S -B -m unittest discover -s tests -v`: 23 tests. Covers package/Home guards, failed and partial handovers, durable backups, interrupted settings writes, preserving later user choices, exported setting order, Java safety rules, theme bounds/contrast, and complete remote-button press capture (held/repeated keys, mismatched releases and cancellation).
- `./gradlew --offline --no-daemon :app:assembleDebug :installer:assembleDebug :app:lintDebug :installer:lintDebug`: both APKs built; lint reports 0 errors (65 launcher warnings and 14 installer warnings remain). The final phone recovery change and subsequent launcher button-picker changes were rebuilt and linted separately.
- In `site/`, `npm run build` and `npm run tokens:check`: passed.
- Local browser preview: English and Arabic display/device sections, theme selection, reduced motion, Arabic Home and assigning an example shortcut to a different app reviewed. No browser warnings/errors observed. This is preview validation, not an Android UI test.
- Shell syntax checked for install/disable/enable/restore/env scripts; Windows install script parsed successfully.
- `git diff --check`: passed. No new launcher runtime dependencies.

## Local outputs

- Launcher: `app/build/outputs/apk/debug/app-debug.apk`.
- Phone installer: `installer/build/outputs/apk/debug/installer-debug.apk`. Choose **Install a local Afnon APK** to install this branch's launcher; upstream downloads contain upstream code.
- The launcher APK contains the 14 locally downloaded adhan recordings. Optional spoken reminder/announcement clips have not been generated. Audio downloads and APKs remain ignored by Git.

See [setup and recovery](../tools/device.md) and [themes and buttons](THEMES.md).

## Still required on the TV

Keep stock Home enabled for the first run. Confirm Home from another app and after reboot, all D-pad edges in Arabic/English, HDMI/source selection, Bluetooth/voice, held volume and physical shortcut buttons. Verify radio, prayer alerts, service recovery after a force-stop, and each chosen screensaver's idle/wake behavior. TCL's private remapping API may differ on this firmware; specifically check that the TV UI does not restart while changing/restoring mappings.

Only then test reversible stock-Home disabling and restore. Google Ambient/Backdrop depends on the installed TV services. Performance has not been measured, so no speed improvement is claimed yet. Native Android UI/phone flows and real ADB interruption recovery have not been tested on hardware or an emulator.

All work was local; no push, fetch, pull or other remote Git operation was performed. Existing CleanYourTV working changes were left alone.
