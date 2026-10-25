# Local validation — 25 October 2026

Tested on TCL 50C641K, Android 11/API 30 confirmed over ADB, firmware V8-R51MT02-LF1V903.021315. All work stayed local; no push, fetch, pull or other remote Git operation was performed. Existing CleanYourTV working changes were left alone.

## Passed locally

- `python -S -B -m unittest discover -s tests -v`: 26 tests. Covers package/Home guards, failed and partial handovers, durable backups, interrupted settings writes, preserving later user choices, exported setting order, Java safety rules, theme bounds/contrast, complete remote-button press capture, prayer offsets across UK clock changes, favourite navigation/replacement and Android 11 theme-import failure reporting.
- `./gradlew --offline --no-daemon :app:assembleDebug :installer:assembleDebug :app:lintDebug :installer:lintDebug`: both APKs built; lint reports 0 errors (65 launcher warnings and 14 installer warnings remain). The final launcher build was rebuilt and linted separately: 0 errors, 64 warnings.
- In `site/`, `npm run build` and `npm run tokens:check`: passed. Local browser review covered English/Arabic theme and device controls; the latest offsets, app-lighting and favourite controls were tested in the native TV app instead. The preview still has its earlier four-slot layout.
- Shell syntax checked for install/disable/enable/restore/env scripts; Windows install script parsed successfully.
- `git diff --check`: passed. No new launcher runtime dependencies.

## Passed on the TV

- Saved the original package list, disabled list, Home, settings and properties before installing. Recovery data and screenshots remain private and ignored by Git.
- Afnon became default Home. The first reboot returned to Afnon with its prayer/button service bound. The user confirmed physical Home, D-pad, OK, Back and held volume, plus Settings/input after enabling the service.
- The user heard both Quran radio and a test adhan; Back dismissed the adhan to Home. The APK contains 14 adhan recordings and 12 locally generated spoken Arabic reminder/announcement clips. Individual spoken clips were not all auditioned.
- Imported Aurora's panorama through the permission-checked provider call and read the saved theme back. Android 11 rejects JSON colons in `content --bind` while returning process status zero; the tool now uses `content call --arg` and requires `saved=true`.
- Restored the old favourites from the private CleanYourTV screenshot, then preserved the user's subsequent edits. Home now has two balanced rows of up to nine favourites, followed by up to five recent apps and All apps. The user's six favourites remain in their saved order.
- The native app menu opened Android's uninstall confirmation for a removable app. The check cancelled it and verified the app remained installed. System apps do not get this action. The assistant did not uninstall any apps during setup.
- From the long-press menu, **Replace a favourite** opened Home; selecting a lower-row tile saved the exact replacement. The same flow restored the original tile and order. Back cancellation preserved the saved favourites.
- Native **Prayer time offsets** saved Fajr +1 minute and then reset it to zero. All offsets were left at zero. Leamington Spa was selected, initially with Muslim World League calculation and Standard Asr (the user later selected Hanafi); the user can change all three choices. Coordinates came from [Mapcarta/OpenStreetMap](https://mapcarta.com/Leamington_Spa).
- **App focus lighting** is a separate off-by-default toggle and was left off at the user's request. Aurora panorama remains selected.
- After Home and remote checks, exclusive handover disabled only CleanYourTV. The previously disabled packages remained disabled; Play Store, Play Services, existing apps and Google Backdrop remained available. Android's system FallbackHome remains enabled as a system component.
- A second reboot, with CleanYourTV disabled, returned automatically to Afnon and rebound its prayer/button service. The user confirmed physical Home, Settings/input and held Volume Up/Down still worked. The disabled list and Google Backdrop selection were unchanged. No Afnon crash appeared in the post-reboot crash buffer.
- Installed the official TV Bro 2.1.6 WebView-only APK (6,790,605 bytes); release digest and APK signature verified. BrowseHere is an updated system app, so it was disabled reversibly and recorded in the recovery journal. TV Bro loaded a webpage, then its home page was set to Google at the user's request. Google loaded and remained the current tab after restarting the browser. Optional Google cookies were declined.
- All apps shows four rows without the bottom information bar; scrolling loads subsequent rows. Home/settings/prayer footer instructions and adhan dismissal text were removed. Native Home and All apps screenshots were checked.
- Recent apps recorded TV Bro and VLC in launch order, moved the most recently opened app first without duplicates, and stayed within five entries. This records Afnon launches, not arbitrary firmware or external app activity.
- The native system-app switch reduced All apps from 33 to 13 entries while Home retained Netflix and YouTube favourites; the All apps count matched. The switch was returned to off, with reduce motion and app focus lighting also off.
- The updated two-row replacement picker reached a lower-row tile; Back preserved the favourite order. The earlier replacement/save/restore check and the pure Java navigation checks also passed.
- Final package checks preserved all 11 previously disabled packages and disabled only CleanYourTV and BrowseHere additionally. Play Store, Play Services, Backdrop, TV Bro and Afnon remained enabled. Afnon still resolved as Home; the preference backup was refreshed.

## Calculation methods, Home icons and alert previews

- Expanded to 19 offline methods without new dependencies. The Java checks cover all presets at Leamington Spa over both UK clock changes and the summer/winter solstices, preserve per-prayer offsets, and verify the original ten presets produce unchanged times. Focused assertions check France's twilight angles, the Gulf Isha interval and Turkey's corrections. All 26 local tests passed.
- Rebuilt and linted the launcher: 0 errors, 64 warnings. Rebuilt all 18 preview routes and checked generated tokens; both passed. Generated preview data contains 19 methods and 137 cities, and every city references an available method.
- Installed the new APK on the TCL without clearing preferences. The native calculation-method chooser saved France, then restored Muslim World League. The user's Hanafi Asr choice was preserved.
- **Reminders & adhan → Test reminders & adhan** opened both chooser actions. The banner appeared over Settings; the adhan popup displayed its playback controls, and Back returned to Settings. Tests use the existing alert path and do not write the scheduled-alert markers. Actual YouTube/Netflix pause and resume behaviour was not tested in this pass.
- Replaced the Today & Qibla symbol with a compass, and the Settings sun-like symbol with a toothed cog, in the native app and preview. Checked a real-TV Home screenshot. Private screenshots of Home, both previews and the method chooser remain outside Git.
- Current method, Asr, offsets and favourites were checked against the pre-test snapshot, and the private preference backup was refreshed. TV was returned to Home. Debugging remains enabled at the user's request.

## Outputs and recovery

- Launcher: `app/build/outputs/apk/debug/app-debug.apk` (19,806,249 bytes with the local audio).
- Phone installer: `installer/build/outputs/apk/debug/installer-debug.apk` (13,177,857 bytes). Choose **Install a local Afnon APK** to install this branch's launcher; upstream downloads contain upstream code.
- Audio and APKs remain ignored by Git. See [setup and recovery](../tools/device.md) and [themes and buttons](THEMES.md).
- `tools/device.py restore` with the same private backup folder restores the previous CleanYourTV setup, including only packages/settings changed by this setup. It does not undo the earlier cleanup or uninstall Afnon.

## Remaining checks

The physical remote check after the exclusive-Home reboot passed. The user confirmed the final All apps opening feels better and navigation between both favourite rows, recent apps and All apps works. Debugging remains enabled at the user's explicit request for further changes; turning it off, revoking authorisations and verifying network ADB closure are deferred until that work is finished.

Not tested: physical branded-button reassignment on this TCL firmware, Bluetooth pairing/voice, saver idle/wake for each choice, service recovery after force-stop, a real restore operation or the phone installer on a phone. All D-pad edges were checked by the pure Java favourite-selection test; a complete native Arabic/English focus sweep remains outstanding. Google's Backdrop selection was preserved, not a claim that all Google photo options were tested.

A three-launch local All apps check measured a baseline of 549/372/512 ms (median 512 ms). The final full-list build measured 407/366/397 ms (median 397 ms), after an unmeasured warm-up; an intermediate build measured a 357 ms median. Metadata reuse, deferred offscreen tiles and fewer nested views reduced work, but the final run still missed the diagnostic target of 300 ms. These are small, uncontrolled same-TV samples, not a general performance guarantee or proof that every pause is gone. No overall launcher performance comparison was performed. Fixed prayer offsets do not import a seasonal mosque timetable and remain set if the location or method changes. Location discovery is not implemented.
