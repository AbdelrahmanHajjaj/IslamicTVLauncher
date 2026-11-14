# Islamic TV Launcher: Android integration notes

Read `AGENTS.md` first for repository-wide instructions and the platform map.
LG work belongs in `platforms/webos/`; read `platforms/webos/AGENTS.md` and `platforms/webos/README.md` for its
ES5/Node 0.10 constraints, Activity Manager service and hardware validation workflow.
The Android rules below do not prohibit the LG service or embedded HDMI video.
Use `TESTING.md` for this project's test requirements and `docs/ARCHITECTURE.md`
for the core/shell boundary. These notes do not override the shared UI/input contract.

The Android shell is a home screen (HOME launcher) built around the prayer day: next-prayer countdown in a mihrab
arch, offline prayer times, Qibla, the adhan at prayer time, reminders, Friday/Ramadan modes and Quran Radio Cairo.
Package `tv.afnon.home`. The maintained project is github.com/AbdelrahmanHajjaj/IslamicTVLauncher;
original Afnon attribution and MIT notices remain. Contribution/release policy belongs to this project.

Read `README.md` (features, install, TCL notes) and `CONTRIBUTING.md` (the rules below in full) before larger work.
Releasing: `gh.md`. History: `CHANGELOG.md`. Design spec: `afnon-mihrab/HANDOFF.md` and `afnon-mihrab/design/`.
Machine- and TV-specific details (TV address, adb path) live in `CLAUDE.local.md`, which is not committed.

## Android target device

Historical local hardware: TCL 50C641K, Android 11. The original project also
reported TCL 55P7K/Android 12 and Super General/CVTE Android 9; that is provenance,
not current branch acceptance. Confirm the actual connected model/firmware before
testing. Target low-memory TVs with D-pad remotes. The queue owns the current
physical Android TV gate; emulator/build results cannot close it.

## Android rules

- Java + Android Views for UI; adhan-java for astronomy; Kotlin shared core for product rules. No AndroidX or Compose.
- No blur, video or shadows; animate transform/opacity only. No new timers or background services: prayer checks
  share one 15 s timer in the accessibility service.
- Network access is limited to the chosen radio station's stream and the owner-approved
  manual AlAdhan prayer-time comparison (see the queue and README Privacy section).
  No automatic comparison polling, accounts, analytics, ads or feeds.
- Colours/sizes/timings from `afnon-mihrab/tokens.json` (generated into `Tokens.java`, `colors.xml` and the site's
  `tokens.css` by `cd site && npm run tokens`; never edit those by hand); every string in Arabic and English in
  `shared/data/{strings,format-text}.json` and `platforms/android-tv/src/main/templates/Text.java.template`; regenerate `Text.java`. Arabic (RTL) is the
  default; check RTL and LTR. No letter-spacing on Arabic. Quranic text uses Amiri and must be exact (surah:ayah).
- D-pad focus is explicit per screen, with Left/Right swapped in RTL.
- Never commit `state/`, `local.properties`, APKs or the adhan MP3s (no licence; `tools/fetch-adhans.sh` downloads
  them). No personal data in screenshots or logs (Wi-Fi names, IPs, serials).

## Build, install, test

```bash
export TV=<ip>:5555                 # the tools refuse to run without it
bash tools/install.sh               # assembleDebug, backup, install, verify HOME; device.py configure enables prayer/remote service
bash tools/shot.sh home             # screenshot -> state/shots/home.png (look at it)
adb -s $TV shell am start -n tv.afnon.home/.MainActivity --es test friday   # ramadan|now|reminder|call|banner|announce|radio
```

`tools/env.sh` finds adb itself (PATH, then `local.properties` sdk.dir, ANDROID_HOME, default SDK folders);
`ADB_BIN` overrides. Other tools: `disable.sh`/`enable.sh` (selected apps, recorded in `state/backups/`),
`restore.sh` (restore recorded TV state, keep Afnon installed, then verify and reboot).
See `tools/device.md` and `docs/THEMES.md` for this branch’s setup and theme workflows.

Keys injected with `adb shell input keyevent` do not reach the accessibility service: test remote buttons with the
real remote, and ask the user to press them. `adb shell getevent -lt` and `logcat | grep handleKeyBefore` show what
a button sends and who handled it.

## Architecture

- `MainActivity` Home · `PrayerDayActivity` today's prayers + Qibla · `AllAppsActivity` · `SettingsActivity` ·
  `NowActivity` the call · `AfnonDream` screensaver · `ScreenOffActivity` radio with the panel off (TCL Audio Only).
- `PrayerDay`, `Prefs` (cities and methods), `Reminders`, `Adhans`, `Radio`/`RadioBars`, `Apps` (launching).
- `RemoteKeys` is the accessibility service. It is always on, so it also hosts `PrayerWatch` (reminders, banner,
  the call). Turning it off stops the call to prayer.

### Remote buttons and the volume-hold bug (fixed 2026-10-21)

With accessibility key filtering on (`FLAG_REQUEST_FILTER_KEY_EVENTS`), Android re-injects every key and injected
keys never auto-repeat, so holding Volume moved it one step. On this firmware the volume keys never even reach the
service. So filtering is off on TCL:

- `TclKeys` hands the buttons to TCL's own remapping in system_server (`TclCustomShortCutKeyManager` in
  `/system/framework/tcl-service.jar`): Settings.Global `CustomKeyList` (JSON array of keycodes) and
  `CustomKey_<code>` (`{"keycode", "behavior":[{"keyaction":1 press|2 long|3 release, "type":1 activity|2 service|
  3 broadcast, "intent":"<intent: URI>", "appnotinstall":{...}}]}`). It runs before apps see the key and swallows it.
- `RemoteKeys.onServiceConnected` rewrites the mappings and turns key filtering on only where TCL remapping is
  unavailable (no feature `com.tcl.software.tinput_shortcut_config`, or no WRITE_SECURE_SETTINGS grant). The
  button table is pref `keys` (else `APP_KEYS`) plus `targets()` in `RemoteKeys.java`.
- **TCL's code has no error handling; mistakes restart the TV's UI.** Changing `CustomKey_<n>` while n is not in
  `CustomKeyList` crashes it (this happened once). So: write the list first, keys a second later, never drop codes
  from the list (empty them). Every intent must name its package (TCL checks it is installed and otherwise runs
  `appnotinstall`, which must be a broadcast). Restoring: write only original values owned by Afnon, retain list entries for observer safety, then verify on the TV.
- After any change here: `logcat -b crash -d | grep "SYSTEM PROCESS"` must stay empty, and hold-volume must work.

## Phone installer (`installer/`)

A separate phone app (plain Java, libadb-android, Conscrypt) installs a locally selected APK or this project's release over ADB. Until a stable TV APK is published here, select a local build. It writes a private/exportable recovery record before changing the TV. Setup commands are bundled in `Setup.java` and mirrored in `tools/setup-steps.txt`; the PC uses `tools/device.py`. Required setup failures stop cleanup. Cities, apps, icons and buttons come from `SetupProvider` (guarded by DUMP).
That provider is a contract: add to it, bump `SetupProvider.API`, never break what's there. Remote buttons are pref
`keys` (`KEY=package`, KEY a keycode name or number), defaulting to `RemoteKeys.APP_KEYS`.
`--es screen <name>` opens any installer screen with sample data (debug builds); CI screenshots them.

## TCL specifics

Settings button sends `KEYCODE_NOTIFICATION` (83); Web is 4159; Netflix is 4062 (mapped to Shahid); YouTube 4065
and 190. Audio Only via `com.tcl.settings.SHOW_WINDOW`. `APP_AUTO_START` appop keeps the service across reboots.
`screen_brightness` does nothing (TCL drives the backlight). Each of these fails quietly on other brands.

## Other brands (CVTE)

`FirmwareKeys` detects CVTE boards (`android.intent.action.HOTKEYSERVICE`): no key filtering there, and Home opens
CVTE's source menu for Source. `Redirects` (pref `redirects`, `from=to` packages) repoints buttons the firmware
hard-wires to an app. Tested on one Super General (Android 9) only; any change here must leave TCL untouched.

## Web preview (`site/`)

Astro site on GitHub Pages (`.github/workflows/site.yml`), one page per screen and mode in Arabic (root) and English
(`/en/`). Catalogs and plain strings come directly from `shared/data/`; `scripts/extract.mjs` reads
fonts/artwork and `docs/screenshots` into gitignored files. Formatting helpers still mirror
`Text.java.template` in `src/lib/text.ts`. Each screen is `src/screens/<X>.astro`
plus a model (`(ctx) => {html, attr}`) that renders at build time and every second in the browser. When a screen's
layout changes in the app, update its component. `npm run dev` / `npm run build` in `site/`.

## Releases

LG has its own package version and IPK build; see `platforms/webos/AGENTS.md`. Do not apply
these Android signing, ADB or APK release steps to the LG port.

Existing builds use `~/.android/debug.keystore` on the maintainer's machine:
releases must keep that key or installed TVs can't upgrade. Steps in `gh.md`; record changes in `CHANGELOG.md`
under Unreleased as you go.
