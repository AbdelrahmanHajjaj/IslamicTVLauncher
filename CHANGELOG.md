# Changelog

All notable changes to Afnon. Versions match `versionName` in `app/build.gradle` and the `vX.Y` git tags.

## [Unreleased]

### Added in IslamicTVLauncher (Afnon)

- Optional random appearance in Home & appearance: Off, every 15 minutes or every 30 minutes. Each rotation chooses a different built-in theme and background while preserving layout and focused app. It uses Home's existing tick, defers during menus/reminders or other screens, and never wakes the TV. Apply restarts the interval; Off keeps the current appearance.
- Home customization with a live layout preview, Apply, Back-to-cancel and reset. Choose a mihrab on either side, a compact countdown at the top/bottom, or hide it; independently show all prayer times, current, next, both, or none. Favourite and recent-app regions reflow while preserving saved apps and prayer-alert settings. Arabic/English browser previews mirror these controls.
- Permanent Today & Qibla quick control at the outer end of the quick-action row, beyond Inputs, replacing the mihrab button. Remote navigation reaches every remaining control even with both prayer sections hidden or no favourites.
- Restored the full-size mihrab with the clock beside it. Next prayer name, prayer time, large countdown and location fill the panel. Wider app rows use four columns for six favourites without the mihrab; focus spacing protects labels and adjacent panels.
- One Home & appearance page combines layout, theme and background in a shared preview, with scrolling controls, one Apply and Back cancellation. The Asr school row now shows its own live draft time instead of a separate sidebar card.

- Fork app and screensaver name, while retaining package `tv.afnon.home` and compatible signed updates.
- Leamington Spa in the UK catalog: 137 cities across 43 countries. Its default still needs an official local timetable comparison before an upstream city PR. Browser introductory totals derive from the catalog.
- Nineteen offline calculation methods, six per-prayer offsets (including sunrise), and a larger centred adjusted-time preview with − / value / + controls, 12/24-hour formatting and Arabic digits. Saved offsets apply to displays and alerts.
- Settings previews for reminder banners and adhan, using current audio settings without consuming scheduled alerts. Theme-aware reminder styling and six shared prayer icons throughout native and browser prayer displays.
- Two favourite rows for up to nine apps and a Classic four-tile layout, selected in the Home customization preview. Both preserve the saved favourites and include a row of recent apps plus All apps.
- Favourite replacement by selecting a destination on Home; Android uninstall confirmation for removable apps; optional system-utility filtering that retains media apps, games and Play Store.
- Eight theme presets and validated custom JSON themes. The shared Home & appearance page previews theme/background choices before Apply; ambient light, reduced motion and off-by-default app focus lighting.
- Sixteen named local backgrounds plus theme default, including eight theme-coloured line patterns: Arches, Eight-point stars, Mashrabiya, Arabesque, Star tiles, Rosettes, Octagon lattice and Corner medallions. Small cached drawings; no wallpaper downloads or idle animation loop.
- Home/input switching with registered HDMI/CEC and tuner entries, a Home entry and an Advanced inputs header button. Wi-Fi opens the system network panel; All apps has a Settings cog.
- An optional full-width prayer bar over apps/inputs, with current/next prayer styling, top/bottom placement, hidden/tab/off modes, mirrored RTL controls and a minimize button. Quick-control labels share one setting with Home.
- Remote settings: assign installed apps or launcher actions, including the prayer bar, using named shortcuts or physical button capture. Preserve essential keys, complete press/release handling and gamepad passthrough; retain other applications' TCL mappings.
- Installed screensaver selection and opt-in prayer-service recovery while preserving unrelated accessibility helpers. The launcher saver keeps its dim clock/next-prayer/radio display and periodic movement.
- Guarded Windows/Python setup, private recovery backups, verified Home takeover and reversible stock-launcher disabling. Phone installer local APK selection, backup export, bundled setup policy and a stop/retry path when required setup fails. Signature mismatches keep the installed app.

### Changed and fixed

- Enlarged Home favourite icons to 72 design pixels (88 in Classic), with automatic shrinkage in shorter cards. Card dimensions and focus spacing stay unchanged; labels remain visible in both languages.
- Fixed soft Home backgrounds caused by stretching a 480×270 preview bitmap. Home now caches the scene at the app's resolution, capped at 1920×1080, with full-HD pattern masks. Settings retains its small preview and downsamples masks before decoding; no animation loop or new dependency is added.
- Expanded from eight to twenty themes with Glass, Frost, Cinema, Carbon, Neon, Blueprint, Signal, Arcade, Dusk, Oasis, Brass and Mosaic. Six new surface treatments change card shapes, borders, fills and Home typography. Glass uses translucency without blur; no artwork, fonts or dependencies were added. Old selections and Custom imports remain compatible. Native/browser previews match, and the build checks the 25 MB APK limit.
- Aligned Home prayer-card names and times left in English and right in Arabic, with the icon above the name and Now/Next at the opposite end of the top row.

- All apps no longer expires its catalogue after two seconds on Home. List/label preparation and icon decoding run off the UI thread, with visible icons delivered in batches of five. Package changes refresh cached data, and rebuilding or closing the screen discards outdated loading results.
- Home focus begins at the first favourite; Down traverses favourites/recent apps to All apps, and Up reaches the quick controls including Today & Qibla. Rebuilding Home preserves the focused app when it remains visible. App metadata is cached and All apps creates tiles as rows enter view; package changes refresh the list even when the count is unchanged.
- Removed the unused All apps dock, footer instructions and adhan dismissal hints. Replaced ambiguous Home settings/Qibla symbols with a cog and compass, and enlarged prayer icons.
- Unified Arabic/English additions and RTL layout; the preview shares native city/method/icon/pattern data and includes both Home layouts. Browser offset editing now mirrors all six native rows, including draft/save/cancel, centred adjusted times, clock formats and Arabic digits.
- Ordinary updates to an existing Home no longer repeat the initial handover/rollback; setup preserves the selected screensaver. Three text-layout calls now respect the declared Android 8 minimum.
- Merged upstream main through `ca457a7`, preserving fork setup guards, favourite capacity and layouts. Updated README, city reference and sanitised English/Arabic TV screenshots; documented the remaining upstream contribution checks.
- Shared settings, preview, quick-menu and prayer-bar dimensions now come from design tokens. Browser keyboard focus no longer jumps to a stationary pointer when rows redraw.
- Replaced eight complex background vectors with small tinted PNG masks generated from shared SVG paths; removed their sixteen lint warnings. Remote-key setup now reports failed preference writes, and the All apps key is offered only on Android versions supporting it.
- Existing fork CI now runs Android lint, the launcher/recovery regression suite and browser prayer-offset tests.
- Safety/recovery and colour-sampling patterns adapted from CleanYourTV, Copyright (c) 2026 Gathin, MIT; see `docs/CLEANYOURTV-LICENSE.txt`.

## [0.6.1] (upstream) - 2026-10-25

### Fixed
- Home no longer crashes on start on a TV with fewer than four apps besides Afnon (found on a stock Android TV
  9 emulator); the missing favourites show as empty tiles.
- The installer (and `tools/install.sh`) can make Afnon Home on stock Android TV and Google TV. There
  `set-home-activity` says "Success" but Home keeps opening the stock launcher, whose filter outranks any app,
  so the step showed "Afnon is not set". The home step now turns the stock launcher off (plus Google's setup app
  on Google TV) behind a safety switch on the TV that turns it back on after 5 minutes unless Afnon came up and
  stayed up, puts it back at once if Afnon didn't, and notes it for the cleanup's undo. Installers already out
  get this from the release's `tools/setup-steps.txt`.
- When a required setup step fails, the installer stays on the step list with Try again and Continue, instead of
  moving on to the city after a second and showing the red step only at the very end.

This fork retains its guarded, backup-first setup and screensaver preservation; it does not execute the upstream automatic launcher-disable script. Its dynamic Home layout already handles short app lists. Failed required phone setup stays on the failed step with Retry, without allowing cleanup to proceed.

## [0.6] - 2026-10-25

### Added
- Afnon Installer (`installer/`): an Android phone app that installs Afnon on a TV with no computer. It guides
  the user through Developer options, finds the TV on the Wi-Fi (mDNS for classic network debugging, Wireless
  debugging's random port and the pairing port, plus a sweep for port 5555, or an address typed by hand with a
  port scan), connects with "Allow debugging?" or a pairing code, downloads the latest release and runs the
  same setup as `tools/install.sh`. Arabic (for Arabic phones) and English. Built by `.github/workflows/installer.yml`.
  After the install it sets Afnon up: the city, language, adhan and spoken reminder; the four favourites; and a
  cleanup that turns off ads, tracking and preloaded apps, with an undo. TV menu names on the screens that send
  the user into the TV are shown in Arabic and English, since the TV's language isn't known yet.
- `SetupProvider`: what the phone installer reads and sets over ADB (cities, regions, apps and their icons, the
  button table, the settings above), guarded by the DUMP permission so only the shell reaches it.
- Remote buttons are a setting now (pref `keys`, `KEY=package` pairs), defaulting to the old table; a new table
  applies at once, through TCL's remapping as before.
- `tools/setup-steps.txt`: the setup after `adb install`, in one file that `tools/install.sh` runs and the phone
  installer reads from the release's tag. Each step has a check (`?|` line) that both run before and after it.
- The installer tracks what can be undone after setup in three states: not set, set, and checked on the TV.
  Setup steps already in place are left alone; cleaned apps are confirmed off, and ones a firmware update turned
  back on are offered again.
- `tools/clean-catalog.tsv`, from cleanyourtv's per-TV debloat lists (MIT) by `tools/gen-clean-catalog.py`, and
  refreshed weekly by `.github/workflows/clean-catalog.yml`.

### Changed
- `tools/install.sh` and the release steps build `:app` only, now that the repo has a second module.

### Fixed
- Installer: no longer hangs on "Installing Afnon". libadb 3.1.1 loses the TV's close of a stream that still
  has data queued, so a quick command (the first one, `getprop`) waited for ever; the installer now notices
  the close itself.
- Installer: Continue on prayer times took about 10 s with nothing on screen. Its settings now go to the TV in
  one ADB shell instead of one each, and the button says it is saving and takes no second tap.
- Home rebuilds when the language, city or another setting changed while it was away and the Home key brings it
  back. It used to reload the words but keep the old layout: half English, half Arabic, after the phone
  installer or Afnon Settings changed the language.

## [0.5] - 2026-10-22

### Added
- Settings → Reminders → Close the call screen: when the adhan ends, or 1, 5 or 10 minutes after it appears (10,
  as before, by default). Then it's back to what was playing. With no adhan playing (turned off, or stopped with
  the button), "when the adhan ends" closes it after a minute. The call screen's hint says which.
- Spoken reminder: «اقترب موعد صلاة …» with the reminder card or banner (Settings → Reminders, on by default).
- Announce the time: «حان الآن موعد صلاة …» when a prayer's time begins with its adhan off, either for all prayers
  or by that prayer's bell (on by default). Friday says الجمعة. Both lower whatever is playing for a moment
  instead of pausing it, and each is said once per prayer, also across a restart of the remote buttons service.
  The 12 clips are not in the repo: `tools/fetch-announce.sh` (run by `install.sh` the first time) records
  them with a Microsoft neural Arabic voice through edge-tts into `app/src/main/assets/announce/`; without them
  both stay silent. Release APKs include them. `--es test announce` tries it; `--es test banner` and
  `--es test reminder` now say the reminder too.

### Changed
- The reminder can come 5, 10, 15, 20 or 30 minutes before each prayer (or not at all). Settings → Reminders →
  Remind me before prayer is now a list, like the adhan voices; 15 is still the default.
- The Hijri date comes first: on Home it is the line under the clock (with the city) and the Gregorian date is
  the smaller line below it; on Prayer day the date line starts with it.
- The verse reference under the arch («النساء 103») follows the digits setting like every other number; it was
  always Eastern Arabic (١٠٣), even with Western digits chosen. It is the only surah:ayah reference on screen:
  Friday, Ramadan, the call screen and the screensaver show none.

## [0.4] - 2026-10-22

### Added
- 59 more cities, now 136 in 43 countries: India, Pakistan, Bangladesh, Sri Lanka, Maldives, Indonesia, Malaysia,
  Singapore, Brunei, Thailand, Japan, South Korea, Turkey, the United Kingdom, France, Germany, the Netherlands,
  Belgium, Austria, Switzerland, Italy, Spain, Bosnia and Herzegovina, Canada, Kenya, Nigeria, South Africa,
  Australia and New Zealand, plus Houston and Los Angeles. Each starts on its mosques' calculation method. Thanks,
  @mohammed-althaf-h (#4).

### Changed
- Location is picked by region first (Arab world, Asia, Europe, Americas, Africa, Oceania), then country (A to Z),
  then city, so no list is longer than 16.
- Picking a city also sets its usual Asr school: Hanafi for Pakistan, Bangladesh and most Indian cities (Kerala and
  Chennai stay Shafi'i). The Asr row can still change it.

### Fixed
- In Settings, list chooser items no longer draw over the chooser's title when scrolled (#8, thanks
  @mohammed-althaf-h in #9).

## [0.3.1] - 2026-10-22

### Changed
- Settings keys are the same on every row: the arrows only move (toward the start side goes back to the
  sections), and OK is the only key that changes anything. OK on a choice or the Hijri offset starts editing it
  (a ring shows round the control): ◀▶ move the choice, OK saves, Back cancels. OK flips a switch and opens a
  list as before. Every save shows "✓ Saved" at the end of the hint line for 2 s. Reported in #5 (thanks,
  @mohammed-althaf-h).

## [0.3] - 2026-10-22

### Added
- United States in the location list, with New York, Chicago and Dallas (ISNA method).
  The Hijri date and Ramadan mode still use Umm al-Qura, so US users who follow local moon sighting set the Hijri
  offset.
- A web preview of the launcher in `site/` (Astro, deployed to GitHub Pages by `.github/workflows/site.yml`): every
  screen in Arabic and English, driven by the arrow keys like the remote, with live prayer times for the visitor's
  chosen city. It reads the app's own strings (`Text.java`), cities, methods, stations and fonts at build time, so
  it never keeps a second copy. One page per screen and mode, with titles, descriptions, `hreflang`, a sitemap and
  structured data for search engines.
- `CODE_OF_CONDUCT.md`, `SECURITY.md`, issue templates (bug, prayer times or city, TV report, idea) and a pull
  request template. `THIRD_PARTY.md` now lists every radio station and the reciter channels.

### Changed
- `afnon-mihrab/tokens.json` is the single source of the design tokens. `Tokens.java` and `colors.xml` are now
  generated from it (`npm run tokens` in `site/`, Style Dictionary), as is the site's CSS; CI fails if they drift.
  The 15 colours that were hard-coded in the app (screensaver, menus, the reminder card, Qibla rings, the banner
  track, the Settings sidebar) are tokens now. Every value is unchanged.

## [0.2.1] - 2026-10-22

### Fixed
- A prayer's reminder and the call to prayer no longer come back after you close them. Each check computed the
  prayer's time with a few different milliseconds, so it never matched the one already shown; the reminder
  returned within 15 s and the call screen during its first two minutes. What was shown is also saved, so a
  restart of the remote buttons service (low memory, TCL's background manager) doesn't show it again.

## [0.2] - 2026-10-21

### Added
- Radio stations: Settings → Radio → Station picks broadcast Quran radios (Cairo, Nablus, Shabab FM Quran, Qatar 1
  and 2, Jordan) or one of 10 reciters' 24/7 channels from mp3quran.net. Each URL is the station's own stream;
  the same single player plays them. Home's card, the screensaver and the screen-off view show the station.
  `--es station <id>` picks one from adb.
- 57 cities, for 74 in 13 countries. Coordinates and Arabic names from Wikipedia/Wikidata.
  - Palestine, the seat of every governorate: Jerusalem, Ramallah, Nablus, Hebron, Bethlehem, Jenin, Tulkarm,
    Qalqilya, Salfit, Tubas, Jericho, Gaza, Jabalia, Deir al-Balah, Khan Yunis and Rafah.
  - Lebanon (Beirut, Tripoli, Sidon, Tyre, Baalbek), Syria (Damascus, Aleppo, Homs, Hama, Latakia, Deir ez-Zor),
    Iraq (Baghdad, Basra, Mosul, Erbil, Najaf, Karbala, Kirkuk, Sulaymaniyah), Bahrain (Manama, Muharraq),
    Oman (Muscat, Salalah, Sohar, Nizwa) and Yemen (Sanaa, Aden, Taiz, Hodeidah, Mukalla).
  - More cities where there was one: Saudi Arabia (Jeddah, Dammam, Taif, Tabuk, Abha) with Umm al-Qura,
    UAE (Abu Dhabi, Sharjah, Al Ain) with the Dubai method, Jordan (Irbid, Zarqa, Aqaba).
  - The new countries use the Muslim World League method, like Amman; the method row can still change it.
- A `redirects` preference (`from=to` package pairs) that opens another app whenever one app's screen comes up,
  for remote buttons the firmware hard-wires to an app that can't be replaced (`Redirects.java`). Empty by default.
- A `key_filter` preference (`auto`, `on`, `off`) to force key filtering on or off on a TV the guess gets wrong.

### Changed
- Location picks a country first, then one of its cities, so the remote never scrolls through all 74. A country
  with a single city is picked at once.

### Fixed
- On TVs whose firmware answers the remote's buttons itself (CVTE boards, e.g. Super General), holding Volume
  works continuously and the Source button opens the TV's own source menu again. The remote buttons service no
  longer filters keys there (`FirmwareKeys.java`, detected by CVTE's `android.intent.action.HOTKEYSERVICE`).
  CVTE's firmware leaves Source to the app in front, so Home now opens the TV's source menu itself.
- `tools/install.sh` grants location, so Home shows the Wi-Fi name on a fresh install (Android 9+ hides it otherwise).
- Where the service still filters keys, a button whose target is missing (an app that isn't installed, or no
  Android TV inputs screen) now passes through to the TV instead of doing nothing.
- Settings no longer stops answering OK after you leave it with a list open (city, method, adhan) and change
  something elsewhere, such as the room direction: coming back rebuilt the screen without the list but kept it
  marked as open.
- In the settings lists (cities, methods, adhan voices), items scrolled up no longer draw over the list's title.
- Settings → Room direction stays open. Settings acts on OK's press and the picker on its release, so the same
  press closed the picker at once and saved 0°.
- Screen off while listening uses TCL's Audio Only on Android TV 11 TCL sets too (tested on a C06_2K_GB set),
  where TCL Settings hosts it in `com.tcl.settings` instead of `com.android.tv.settings`.

## [0.1.1] - 2026-10-21

### Fixed
- Holding Volume Up or Down on the remote changes the volume continuously again, instead of one step per hold.
  The remote buttons service no longer filters keys on TCL TVs, which had stopped held keys from repeating.

### Changed
- On TCL TVs, the remote's app, Settings, Input and All apps buttons are now handed to TCL's own button
  remapping (`TclKeys.java`). Other TVs keep the previous behaviour.
- `tools/install.sh` and the other tools find adb by themselves (PATH, the Android SDK in `local.properties`,
  `ANDROID_HOME`, or the default SDK folder). `ADB_BIN` still overrides.
- `tools/restore.sh` also removes the TCL button mappings.

## [0.1] - 2026-10-21

First public release.

### Added
- Home with the next prayer and a live countdown in a mihrab arch, the day's six times, favourites and more apps.
- Today's prayers with an adhan on/off bell per prayer, the Qibla dial and room direction.
- Reminders before each prayer, and the call screen with the adhan at prayer time.
- 14 adhan voices plus a separate Fajr adhan.
- Friday (Jumu'ah, Surat al-Kahf) and Ramadan (iftar and suhoor) modes.
- Quran Radio Cairo 98.2 FM with live level bars; pauses for the adhan; screen off while listening (TCL Audio Only).
- Screensaver, on-TV settings and remote button mapping.
- Arabic (RTL) first, with English.

[Unreleased]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.6.1...HEAD
[0.6.1]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.6...v0.6.1
[0.6]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.5...v0.6
[0.5]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.4...v0.5
[0.4]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.3.1...v0.4
[0.3.1]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.3...v0.3.1
[0.3]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.2.1...v0.3
[0.2.1]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.2...v0.2.1
[0.2]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.1.1...v0.2
[0.1.1]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.1...v0.1.1
[0.1]: https://github.com/egyjs/afnon-android-tv-launcher/releases/tag/v0.1
