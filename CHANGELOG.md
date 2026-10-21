# Changelog

All notable changes to Afnon. Versions match `versionName` in `app/build.gradle` and the `vX.Y` git tags.

## [Unreleased]

### Added
- 57 cities, for 74 in 13 countries. Coordinates and Arabic names from Wikipedia/Wikidata.
  - Palestine, the seat of every governorate: Jerusalem, Ramallah, Nablus, Hebron, Bethlehem, Jenin, Tulkarm,
    Qalqilya, Salfit, Tubas, Jericho, Gaza, Jabalia, Deir al-Balah, Khan Yunis and Rafah.
  - Lebanon (Beirut, Tripoli, Sidon, Tyre, Baalbek), Syria (Damascus, Aleppo, Homs, Hama, Latakia, Deir ez-Zor),
    Iraq (Baghdad, Basra, Mosul, Erbil, Najaf, Karbala, Kirkuk, Sulaymaniyah), Bahrain (Manama, Muharraq),
    Oman (Muscat, Salalah, Sohar, Nizwa) and Yemen (Sanaa, Aden, Taiz, Hodeidah, Mukalla).
  - More cities where there was one: Saudi Arabia (Jeddah, Dammam, Taif, Tabuk, Abha) with Umm al-Qura,
    UAE (Abu Dhabi, Sharjah, Al Ain) with the Dubai method, Jordan (Irbid, Zarqa, Aqaba).
  - The new countries use the Muslim World League method, like Amman; the method row can still change it.

### Changed
- Location picks a country first, then one of its cities, so the remote never scrolls through all 74. A country
  with a single city is picked at once.

### Fixed
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

[Unreleased]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/egyjs/afnon-android-tv-launcher/compare/v0.1...v0.1.1
[0.1]: https://github.com/egyjs/afnon-android-tv-launcher/releases/tag/v0.1
