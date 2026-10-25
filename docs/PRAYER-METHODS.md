# Prayer methods and alert previews

Choose **Settings → Prayer times → Calculation method**. All 19 presets calculate on the TV, without fetching prayer times. Home, Today's prayers, the screensaver, reminders and adhan share the selected method, Asr school and per-prayer offsets. The browser preview exposes the same method choices.

## Existing methods

The original ten presets still use the same adhan-java parameters: Egyptian General Authority of Survey, Umm al-Qura, Muslim World League, Karachi, Dubai, Kuwait, Qatar, ISNA North America, Moonsighting Committee and Singapore. Saved method names and city defaults are unchanged; installing this update does not select a different method or reset offsets.

## Added presets

Angles are degrees below the horizon. A fixed Isha interval is measured from sunset.

| Preset | Fajr | Isha |
|---|---:|---:|
| France (UOIF) | 12° | 12° |
| Turkey / Diyanet (approximate) | 18° | 17° |
| Spiritual Administration of Muslims of Russia | 16° | 15° |
| Malaysia (JAKIM) | 20° | 18° |
| Indonesia (KEMENAG) | 20° | 18° |
| Tunisia | 18° | 18° |
| Algeria | 18° | 17° |
| Morocco | 19° | 17° |
| Gulf region | 19.5° | 90 minutes |

These angles and intervals follow [AlAdhan's published method definitions](https://api.aladhan.com/v1/methods). Turkey also applies Batoul Apps' corrections: sunrise −7, Dhuhr +5, Asr +4 and Maghrib +7 minutes, as defined in [adhan-js](https://github.com/batoulapps/adhan-js/blob/master/src/CalculationMethod.ts). User offsets are applied separately. Turkey is labelled approximate because this is a calculated preset, not a downloaded Diyanet timetable.

Regional presets can differ from a mosque's published timetable, especially at high latitudes or where seasonal adjustments are used. The existing library's high-latitude rules still apply. Use **Prayer time offsets** for fixed differences of −30 to +30 minutes per prayer; these remain set when changing city or method.

London Unified is not presented as a generic calculation method. It is a published timetable for London and the M25 area, with observation-based conventions described by [East London Mosque](https://www.eastlondonmosque.org.uk/prayer-times-and-calendar-explained). This update adds general presets for the selected city; it does not import exact mosque timetables.

## Test the banner and popup

Open **Settings → Reminders & adhan → Test reminders & adhan**, then choose **Reminder banner** or **Adhan popup**. The preview uses the next prayer and the same rendering and audio path as scheduled alerts. Sound follows the current reminder/adhan settings. Back dismisses the popup.

Tests do not change preferences or mark a scheduled prayer as delivered. The prayer and button service must be running; when it is stopped, this control opens Android's accessibility settings so it can be enabled. These test controls are in the native TV app; the browser preview does not control TV alerts.
