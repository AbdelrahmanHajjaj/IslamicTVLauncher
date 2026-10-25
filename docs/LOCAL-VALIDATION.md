# Local validation — 25 October 2026

Tested on TCL 50C641K, Android 11/API 30 confirmed over ADB, firmware V8-R51MT02-LF1V903.021315. This is a chronological record: counts and selections below describe each increment, not necessarily the latest state. Work was initially local-only; a later explicitly authorised fetch/merge is recorded below. No push or PR was made. Existing CleanYourTV working changes were left alone.

## Passed locally

- `python -S -B -m unittest discover -s tests -v`: 27 tests. Covers package/Home guards, failed and partial handovers, durable backups, interrupted settings writes, preserving later user choices, exported setting order, Java safety rules, theme bounds/contrast, complete remote-button press capture, prayer offsets across UK clock changes, favourite navigation/replacement and Android 11 theme-import failure reporting.
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

## Update check recovery

During the final icon APK update, Google's Backdrop was active. The APK installed, but the repeated Home check failed and the PC tool attempted the original setup rollback. This temporarily selected CleanYourTV, enabled BrowseHere and stopped Afnon's helper. No reboot was issued by the assistant.

After waking the TV, the existing setup tools restored Afnon as exclusive Home, re-enabled its helper and disabled BrowseHere again. Read-back checks confirmed the same 13 disabled packages, Afnon as Home, the bound helper and unchanged user prayer/favourite/theme settings. The recovery path added an explicit `repair_service=false`, matching its previous default. The private preference backup was refreshed.

An ordinary update now detects that Afnon is already Home and verifies it without repeating the original handover/rollback. A regression test failed before this fix and passed afterwards; the complete suite passes 27 tests. The corrected existing-Home path also passed on the TV. The initial exclusive-handover safety checks remain in place. A new full reboot and physical-remote check after this recovery have not been performed.

## Fork name and local checkout

The checkout folder and Gradle project are now `IslamicTVLauncher`, on `feature/improvements_and_cleanup`. After explicit approval, local `origin` was changed to [the user's fork](https://github.com/AbdelrahmanHajjaj/IslamicTVLauncher), with the original repository retained as `upstream`; future pushes default to `origin`. No fetch, pull or push was run.

The TV label is **IslamicTVLauncher (Afnon)**. Package `tv.afnon.home` and signing identity were preserved. The renamed checkout built and linted successfully (0 errors, 64 warnings); the APK installed successfully through the corrected ordinary-update path. Native Android App info displayed the new label. Afnon still resolved as Home, its helper was bound, CleanYourTV/Google TV/BrowseHere stayed disabled, and saved user preferences matched the post-recovery backup. The TV was returned to Home.

## Theme previews, offsets and everyday apps

- All 28 local checks passed, including the new system-app classification regression and every theme's contrast/background bounds. The final native build and lint passed with 0 errors and 64 warnings. The browser build produced all 18 routes and generated-token validation passed.
- **Prayer time offsets** now uses six minus/value/plus rows. On the TCL, Fajr +1 saved, cancelling a second draft preserved +1, and the original zero offset was restored. Back returned to the parent Prayer settings row.
- **Themes & backgrounds** is a separate section. Theme and background rows are horizontal selectors. Drafts update only the miniature preview; **Apply changes** saves the page. Leaving the section or returning from Home discards unapplied choices. Native preference assertions verified previewing did not save, applying Sand did save, and applying Custom restored the original imported theme. Prayer settings, favourites, room bearing, app filtering and motion choices were preserved.
- Added Midnight, Rose, Sand and Slate palettes with generated night sky, halo, dunes and mountains backgrounds. All four rendered on the TV. They use the existing static 480×270 drawing path and introduce no wallpaper assets, downloads, runtime dependencies or idle animation loop. This is not an overall performance benchmark.
- Browser checks verified draft and active colours differ while previewing, navigation away restores the saved choice, Apply updates the active colours, and Arabic Left/Right selection follows the reading direction. The browser's older app layout and prayer controls remain documented limitations.
- **Hide system apps** now preserves media/image apps, games and Play Store, with tested package fallbacks for common preloads lacking categories. A native filtered screenshot showed 21 entries, including YouTube, Netflix, Play Store, Play Games and Play Movies. The setting was returned to off; it never disables or removes packages.
- The Home reminder card and the banner over Settings both used the saved custom palette in real-TV screenshots. Test alerts left saved preferences unchanged.
- The Qibla arrow and room bearing matched before, during and after opening the room-direction editor; the reported intermittent restart issue was not reproduced. No Qibla logic was changed and no new TV reboot was performed in this pass.
- Final checks confirmed Afnon as Home, its helper bound, the same 13 disabled packages and preserved prayer/favourite/theme settings. The private preference backup was refreshed.
- Re-authored the seven local feature commits using the user's requested main identity and updated local/global Git identity. The corresponding trees, messages and author dates were verified unchanged; upstream history was preserved. The old local history remains on a recovery branch. No remote Git operations were performed.

## Outputs and recovery

- Launcher: `app/build/outputs/apk/debug/app-debug.apk` (19,927,781 bytes with the local audio).
- Phone installer: `installer/build/outputs/apk/debug/installer-debug.apk` (13,177,857 bytes). Choose **Install a local Afnon APK** to install this branch's launcher; upstream downloads contain upstream code.
- Audio and APKs remain ignored by Git. See [setup and recovery](../tools/device.md) and [themes and buttons](THEMES.md).
- `tools/device.py restore` with the same private backup folder restores the previous CleanYourTV setup, including only packages/settings changed by this setup. It does not undo the earlier cleanup or uninstall Afnon.

## Remaining checks

The physical remote check after the exclusive-Home reboot passed. The user confirmed the final All apps opening feels better and navigation between both favourite rows, recent apps and All apps works. Debugging remains enabled at the user's explicit request for further changes; turning it off, revoking authorisations and verifying network ADB closure are deferred until that work is finished.

Not tested: physical branded-button reassignment on this TCL firmware, Bluetooth pairing/voice, saver idle/wake for each choice, service recovery after force-stop, a real restore operation or the phone installer on a phone. All D-pad edges were checked by the pure Java favourite-selection test; a complete native Arabic/English focus sweep remains outstanding. Google's Backdrop selection was preserved, not a claim that all Google photo options were tested.

A three-launch local All apps check measured a baseline of 549/372/512 ms (median 512 ms). The final full-list build measured 407/366/397 ms (median 397 ms), after an unmeasured warm-up; an intermediate build measured a 357 ms median. Metadata reuse, deferred offscreen tiles and fewer nested views reduced work, but the final run still missed the diagnostic target of 300 ms. These are small, uncontrolled same-TV samples, not a general performance guarantee or proof that every pause is gone. No overall launcher performance comparison was performed. Fixed prayer offsets do not import a seasonal mosque timetable and remain set if the location or method changes. Location discovery is not implemented.


## Quick controls and prayer bar (25 October 2026)

- Local Python/Java suite: 30 checks passed. Launcher debug build and lint passed with 0 errors and 71 warnings. Browser build generated all 18 routes; token validation passed. No new runtime dependencies or permissions.
- On TCL 50C641K / Android 11, Inputs showed Home, HDMI 1–3, Live TV and the TV input menu. HDMI 1 and Live TV opened TCL's player. Input discovery uses the public registered list; the hardware tuner is preferred over software preview inputs.
- The service overlay appeared while TCL's input player remained the resumed activity. Window inspection confirmed the compact strip uses NOT_FOCUSABLE and NOT_TOUCHABLE. Hidden, strip and Off modes, top/bottom placement, Inputs submenu/Back, Home routing and minimize were exercised. The final expanded bar spans 1920 pixels and is approximately 157 pixels high on this TV. Screenshots were inspected privately.
- Blue was assigned to Prayer bar without removing existing assignments. The user verified the physical Blue button, Back/minimize and held Volume Up/Down all work. The mapping uses the existing accessibility path on this firmware; native TCL remapper behavior on other firmware was not verified.
- The Wi-Fi icon opened the system network panel, whose UI exposed both Connected and Available networks. Device location remained off. The custom connection-only popup was removed because Android restricted its network details. No network or credentials were changed.
- Quick control labels were enabled and disabled through the native Settings UI and left off. All apps' header cog opened launcher settings. The final eight Display rows fit on screen.
- Home focus was tested after a simulated touch/mouse action: Home selected the first favourite; Down traversed both favourite rows and recents, then highlighted All apps; OK opened All apps. Default focus now handles Android's touch mode as well as the remote. The existing user favourites were preserved.
- A delayed reminder banner appeared above TCL's input player without changing its resumed activity. A test adhan opened NowActivity; Back returned to TCL's input player. No claim is made about HDMI sound ducking/mixing: no live HDMI device or aerial was available.
- English and Arabic browser quick-input menus, All apps' Settings link, label setting and first-favourite focus were checked. Android overlays and native Wi-Fi controls cannot be exercised in the browser preview.
- Left at Home with the bar hidden until opened, Bottom selected, icon labels off, and Blue assigned. Debugging remains enabled at the user's request. No reboot, package disabling, remote Git operation or push was performed in this increment.

Remaining: live HDMI/tuner picture and sound, HDMI-CEC device labels with an attached device, and overlay behavior in each streaming app. The previously listed broader recovery, saver and phone-installer checks remain outstanding.

## Shared controls, prayer icons and Arabic bar (25 October 2026)

- All 31 Python/Java checks passed, including vector coverage and order. The launcher build and lint passed with 0 errors and 69 warnings. Browser build generated 18 routes; generated-token validation passed. Six small vector resources add no runtime dependency or permission.
- The expanded prayer bar is now a single full-width row, approximately 100 pixels high on the TCL. Bordered prayer tiles fill the available space; Now uses a light fill and Next uses the theme border. Controls sit at the right in English and the left in Arabic. Both icon-only and labelled layouts were inspected on the real TV and fit without overlap.
- Quick control labels was toggled through Settings in both languages and verified on Home and the bar. The original icon-only choice was restored. The user's Top placement and hidden-until-opened mode were preserved.
- The Inputs popup starts on Home. Up selects the new Advanced inputs button beside the title, and OK opens TCL's source chooser. Arabic mirrors the popup, aligns HDMI labels to the right and keeps the firmware-provided source names. The bar's Inputs/Back and minimize navigation were exercised in Arabic.
- Prayer icons were inspected on native Home, Today & Qibla, All apps, offsets, settings preview, the bar, reminder banner and adhan screen. Text and icons follow the theme and focus colours. Arabic icon spacing and icon-only Home tags were corrected after visual checks. A test adhan opened NowActivity and Back returned to Settings. The alert tests did not change prayer schedules.
- English/Arabic browser checks found all expected icons on prayer day, All apps, call, settings and screensaver routes, with no console warnings/errors. Both Inputs menus and Advanced inputs focus were checked. The browser reads the same vector paths as Android.
- Final checks confirmed Afnon as Home, its helper bound, and the same 13 disabled packages. Saved settings, favourites and button assignments matched the pre-update snapshot; recent apps reflect test navigation, and the default system language is now explicitly saved. The private recovery backup was refreshed. The TV was left at Home. No reboot, remote Git operation or push was performed.

Not rechecked: screensaver idle/wake on the TV, a fresh physical Blue/volume test, live HDMI/tuner picture and sound, or each streaming app's overlay restrictions. The earlier physical remote confirmation still applies to the unchanged key handling.


## Larger icons and geometric backgrounds (25 October 2026)

- Prayer glyphs are approximately 18% larger in the shared native span and browser component, preserving text metrics, theme tint and RTL spacing. Native Home and the compact bar were inspected in English and Arabic.
- Added eight static line patterns with matching Arabic labels and shared native/browser paths. All 32 Python/Java checks passed, including pattern/translation coverage and native/browser catalogue order. The launcher built; lint reported 0 errors and 85 warnings. Sixteen new warnings concern vector size/path complexity: these backgrounds are rasterized on appearance changes and reuse the existing bitmap between redraws. No new runtime dependency or animation loop was added.
- The browser built all 18 routes and generated-token validation passed. All eight patterns were previewed across all eight palettes (64 combinations), with theme-coloured strokes and unchanged active settings until Apply. Their native preview, Apply and Home rendering were tested; the original background was restored. Arabic preview navigation and larger icon spacing were also checked.
- Star tiles, Rosettes, Octagon lattice and Corner medallions also passed native preview, Apply and Home checks. Rosettes initially exceeded Android's resource-string limit; the path was simplified and a regression check now bounds every pattern path below 30,000 UTF-8 bytes. The repaired resources were rebuilt and tested on the TV. Private screenshots and preference snapshots remain outside Git.

## Main sync, layout cards and adjusted times (25 October 2026)

- Fetched main from both configured repositories at the user's request. The fork's main (`8fdff74`) was already included; upstream main (`ca457a7`) had six additional commits and was merged locally as `f377db0`. Nothing was pushed.
- Kept the fork's backup-first installation and Home safeguards, dynamic favourite handling, cached app metadata, deferred All apps tiles and appearance caching. The merge did not change `Apps`, `MainActivity`, `AllAppsActivity`, `AppLighting` or `Appearance`. The phone installer's failed required steps remain visible with Retry; continuing past a required failure is blocked. Launcher version is 0.6.1; installer is 1.0.1.
- Added a default two-row Home layout and a classic four-favourite row, chosen from horizontally scrolling cards with miniature previews. English and Arabic card browsing, Back cancellation, selection and Home-to-All-apps navigation passed on the TCL. All six saved favourites survived switching; classic shows only the first four and switching back restores the full visible set. Pure Java navigation checks cover one through nine favourites and the classic row edges.
- Browser card styling, English cancellation/application, Arabic reversed navigation and four/six visible favourite tiles were checked. Both browser and native previews mirror for RTL. The browser's existing prayer-offset editing limitation remains.
- Native offset checks verified all six adjusted times, unsaved +2-minute preview, Back cancellation, saving +1 minute, restoring the original value, 12-hour AM/PM, 24-hour formatting and Arabic digits/period labels. The final 30px times were checked against card bounds in English and Arabic: all six are centred, and live draft updates still work. A spacing-helper crash caught during this adjustment was fixed and the corrected build passed the same TV checks.
- Launcher and installer debug builds/lint passed; launcher lint has 0 errors and 85 warnings, installer 0 errors and 14 warnings. The 32 local Python/Java checks, all 18 browser routes and generated-token validation passed. No dependency or permission was added. Screenshots and preference snapshots are private and untracked.

- Final checks kept Afnon as Home, its prayer/button helper present, all 13 previously disabled packages unchanged and Google Backdrop selected. The original language, two-row layout, favourites, offsets, filter, bar placement and theme were preserved; the default system clock choice is now explicitly stored. The private preference backup was refreshed and the TV left at Home.

Not tested in this increment: the phone installer on a physical phone, a full performance benchmark, screensaver idle/wake, or live HDMI/tuner picture and sound. No TV reboot or force-stop was performed.

## Screensaver activation and documentation refresh

- The existing dream-service label already uses the same app-name resource as the launcher: **IslamicTVLauncher (Afnon)**. Selected it through the guarded setup tool, preserving the previous saver in the private recovery record.
- Started the installed Android dream service and verified `tv.afnon.home/tv.afnon.home.AfnonDream` was the active dream. Captured its dim clock/next-prayer display. Automatic idle start and physical-remote wake were not verified in this pass.
- README and Unreleased changelog now describe the implemented fork features; the city reference lists the catalog and explicitly marks the new city's official timetable comparison as pending. Browser intro totals follow the catalog.
- Refreshed real-TV screenshots in English/Arabic from the feature checks. Home city text is redacted; private originals, backups and device identifiers remain outside the commit. See `screenshots/README.md` for capture scope.
- `CONTRIBUTION-PLAN.md` records focused upstream scopes and remaining checks, including shared-token adoption, browser parity and new vector warnings. The whole feature branch is not declared upstream-ready.
- Documentation checks passed: 32 existing automated tests, all 18 browser routes and token consistency. Catalog assertions verified 137 unique bilingual city IDs, 43 countries, six regions, valid coordinates/methods and parity with native source and the city reference. English/Arabic rendered intro totals, 59 local documentation links and all 13 screenshot dimensions also passed.
- No native code changed during the documentation refresh, so the Android APK was not rebuilt or reinstalled and no TV settings were changed. Native build/lint counts above remain the last recorded results, not a new run.
