# Islamic TV Launcher for LG webOS

This is a separate, installable webOS app (`tv.afnon.webos`) and JavaScript service.
It opens the current TV input full screen and keeps that video element alive while
the viewer opens Islamic TV Launcher Home, prayer times, Inputs and Settings. The first target is
the LG LF6300 running webOS 2.2 / WebKit 538.2, with a Node 0.10 service runtime.

The visible name is **Islamic TV Launcher**. The existing package/service ID is kept
so upgrades preserve settings and the installed startup activity.

The Android launcher and Astro preview have separate renderers. Version 0.2.5 shares their
cities, prayer methods, translations, fonts, color palettes, audio recordings, radio
stations and adhan-js calculation library. Native streaming apps cannot be embedded
as HDMI sources. Home now follows Android's compact top icon row: Today & Qibla,
Inputs, Radio, LG TV/network settings and launcher Settings. Large tiles open Netflix,
Prime Video and YouTube; All apps opens a remote/pointer-navigable grid of 16 links.

`tv-apps.json` records the visible apps read from this LF6300 on 10 November 2026,
excluding this launcher and hidden LG utilities. It includes Browser, SmartShare,
Screen Share, TV Guide and Developer Mode. The app itself cannot refresh this inventory:
public `listApps`, `listLaunchPoints` and `getAppLoadStatus` each returned “Unknown
method” on webOS 2.2. Refresh the catalog from a paired external SSAP inventory when
apps change. The service permits only catalog IDs and LG Settings, and reports a
launch failure without leaving the current page. Icons reuse our local line artwork;
these are not the apps' official logos. No SSAP pairing key is bundled.

The LG app and service also consume `shared/core` for offset validation and
current/next prayer selection. Astronomy and date/time-zone handling remain in
the LG adapter; today's Fajr row and tomorrow's separate value are preserved.
Scheduled event selection and the inclusive 90-second delivery rule also use
the core; the LG service retains persisted claims, cancellation and OS activities.

## Ported settings and tests

Settings now has an Arabic/English sidebar and scrolling rows navigable by arrows,
OK, Back and the Magic Remote pointer. Back leaves an editor without saving, returns
from a row to its section, then returns Home (or All apps when opened there).
The visible top Back arrow leaves Settings directly; in Time adjustments it returns
to the Prayer section. Unsaved appearance changes are discarded when leaving.

- Prayer: country/city, method, Asr school, TV/fixed time zone, six minute adjustments
  (-30 to +30), Hijri adjustment (-2 to +2), Today & Qibla and five prayer switches.
- Adhan: separate regular/Fajr voices (all 14 existing local recordings), app audio
  level, spoken reminders/announcements, reminder lead time and call-screen duration.
- Display: Arabic/English, independent digit style, 12/24-hour clock, quick-control
  labels, top/bottom prayer bar, automatic/manual bar dismissal and a manual clock screen.
- Home: mihrab/compact/hidden countdown, mihrab side, compact countdown top/bottom,
  all/current/next/current-and-next/hidden prayer times, the 20 existing color palettes
  and all eight bundled SVG patterns. Android's animated
  backgrounds, surface treatments, appearance rotation and custom-theme import are
  not reproduced by these color/pattern controls.
- Quran radio: shared station list, play/stop and optional resume after adhan. A
  station's TLS, codec and availability still require checking on this old TV.
- TV: preserved input selection and startup, native LG Settings shortcut, selected
  TV-app shortcuts, and an alert/startup status action.

Version 0.2.8 binds those five prayer switches to the same semantic controls
as Android; sunrise stays informational. Bilingual browser checks exercise every
switch's saved value and Back to its originating setting. These checks mock the LG
boundary; actual TV evidence is recorded under Hardware validation below.
Shared favourite editing and native Uninstall action definitions are retained in
the catalog, with explicit unimplemented gaps: this shell keeps its fixed shortcuts
and hides those actions. This is an implementation gap, without a claim that every
LG model forbids editing.

Home & appearance has a schematic layout preview and palette swatches, matching
Android's draft/apply workflow. Theme, background, countdown, placement and prayer
information use inline carousels: Left/Right previews, Up/Down changes rows, and
pointer arrows select adjacent choices. The 704×396 preview remains above the scrolling
rows. Patterns affect only Home and this preview, not Settings or other pages.
The UI omits the port's navigation tutorials, HDMI click hint and explanatory footers;
control labels, state and actionable errors remain.
Changing a choice updates the draft; Apply saves it, while Cancel,
Back from the appearance rows, changing section or leaving Settings discards it.
Reset layout changes only the draft layout fields. Prayer/audio settings are preserved.
All apps has Android's top Back arrow, next-prayer chip, Settings and five-column grid.
Back restores focus to Home's All apps button. Settings and Prayer day opened from
All apps return there on Back.

**Settings → Test alerts / اختبار التنبيهات** works without enabling real scheduled
alerts. Choose a prayer, then test a silent banner, a silent card, a 12-second adhan,
a full adhan, a spoken reminder, or a prayer-time announcement. Back/Dismiss closes
the test and returns to its original page, focused row and scroll position. The adhan
uses Android's central mihrab with separate Stop/Play and Dismiss controls. All 14
voices appear in one scrolling list with explicit inline previews; finishing a
preview retains that list and its position. A separate 30-second test exercises the real
Activity Manager background service and has a cancellation action.

Silent banners/cards do not pause, unload or reload the input. Audible playback must
release the input decoder first on the LF6300; after Stop, end or a media error, the
audio source is removed and the same recorded input is reloaded inside the app.
Master TV volume is never changed. The app-level audio setting scales only its audio.
Spoken Arabic clips are the same clips as Android, including Friday's Jumuah line.

Scheduled alerts are **off by default**; upgrades preserve your saved choice. Turn them on in Adhan & reminders
after checking the timetable and the audible test. They use a separate persistent
Activity Manager task for the next exact alert time, independent of automatic startup.
Settings changes reschedule it. Callbacks are claimed before launch, duplicates are
ignored, and callbacks over 90 seconds late expire silently and schedule the next one.
They cannot wake the TV from standby. The app consumes each delivered alert once.
In-app alerts restore the original screen and input. Since 0.2.4, an external
interruption closes our window afterward so LG can return to its prior app history;
Browser return is verified, streaming playback resumption is not. Overlays appear
inside this app, not globally over another native app.

## Controls

| Remote action | Result |
| --- | --- |
| OK on full-screen input | Show prayer times and quick actions |
| Click the picture with the Magic Remote pointer | Open the same compact prayer bar at the top |
| Arrows | Move focus between buttons; visual direction follows the layout |
| Enter Home | Focus the first app, matching Android |
| Up from Home's first app row | Enter the top controls by column: Radio, Network/native settings or launcher Settings |
| Down beyond Home's last app row | Focus All apps; Up restores the last app focus |
| OK on a button | Activate it |
| Back from prayer bar or Islamic TV Launcher Home | Return to full-screen input |
| Back from Settings rows | Return to section sidebar; Back again returns Home |
| Back from Inputs, Today, Radio or TV apps | Return Home |
| Back from a chooser | Return to Settings |
| Back on full-screen input | Open the controls, without exiting |
| Home / Power / Volume | Keep the LG system behavior |
| Home → Exit Islamic TV Launcher | Launch the recorded native input, then close Islamic TV Launcher |

Home, Settings, Inputs and the prayer bar are layers in one document, not separate
app launches. HDMI picture/sound continue during silent navigation and overlays.
Adhan and radio take exclusive playback. The app releases media while hidden by
another LG app and resumes the input when visible; it does not keep playing radio
in the background of another native app.

The top prayer strip follows Android's `PrayerDock` layout. Home uses the shared
pointed mihrab shape, top icon controls, upper prayer row and TV app tiles.
Arabic prayer names use Alexandria on this old TV; Amiri Regular remains used for
the Quranic text. Browser checks cannot establish the TV's font-shaping behavior.

## Startup and persistence

### Remote shortcuts while the app is closed

The Activity Manager schedule survives the app closing; this does not mean the
JavaScript service is permanently resident or receives global remote keys. LG's
standard service library exits idle services and Activity Manager invokes them when
needed. Color keys are available to the foreground web app. No supported global
colored-key registration is available to this Developer Mode service.

System-wide remapping tools such as LG Input Hook require root. No compatible root
path has been established for this LF6300 / 04.06.75; nothing was rooted or remapped
for this port. LG's numeric Quick Access feature on later models is not confirmed on
this 2015 TV. See [LG remote keys](https://webostv.developer.lge.com/develop/guides/magic-remote),
[service lifecycle](https://webostv.developer.lge.com/develop/guides/js-service-faq),
and [Input Hook requirements](https://github.com/Simon34545/lginputhook).

### Scheduled startup

Settings → **Open Islamic TV Launcher after TV starts** controls a persistent Activity Manager
activity. It checks the kernel boot ID every 30 seconds, launching Islamic TV Launcher once when
that ID changes. It does not wake a powered-off TV or repeatedly take focus after
the viewer exits Islamic TV Launcher. In the preceding probe, startup happened around 44 seconds
after kernel boot. This depends on LG keeping the developer app/service installed.

State is written atomically inside `/media/internal/tv.afnon.webos`, owned by the
jailed service user. Do not store writable state in the installed service directory:
its permissions change after reboot on this TV. The selected input is persisted;
on launch, a valid native current input takes priority, then the saved input.

Startup claims each boot before launching, including when a launch reply is
ambiguous. This avoids repeated focus stealing. A failed launch can therefore
require opening Islamic TV Launcher manually that boot; the service's `status` reports the result.

Developer Mode and its session need to remain active. This is a sideloaded development
build, not an LG Content Store release or a replacement for system firmware. No root,
firmware changes or system init edits are used.

## Prayer clock

Arabic is the default; English can be selected in Settings. Cairo / Egyptian method /
standard Asr are the initial defaults. All existing Islamic TV Launcher cities and methods are
read from the canonical shared catalogs at build time. adhan-js is bundled locally and
compiled to ES5; no CDN, network requests or modern `Intl` features are needed.
Hijri dates use Umm al-Qura month boundaries generated by Node's ICU at build time,
covering 2000–2100. The UI explicitly reports dates outside the bundled range as
unavailable. Qibla is a bearing clockwise from true north, not a TV orientation sensor.

The default time zone follows the TV. Settings also permits a fixed UTC offset in
15-minute steps for old or incorrectly configured TV time-zone databases. A fixed
offset does **not** change for daylight saving: update it when local rules require.
Selecting another city does not change the time zone. Keep the TV clock correct and
compare the displayed times with the timetable you follow locally.

## Build and check

Use Node 24 and JDK 17 for the build tools. The build compiles `shared/core` with
the root Gradle wrapper; the packaged app/service remain ES5 / Node 0.10 compatible.
The LG project's own dependencies include the pinned Adhan build input; no preview
or Android project is needed.

```powershell
npm --prefix platforms/webos ci
npm --prefix platforms/webos test
npm --prefix platforms/webos run test:browser
ares-package platforms/webos/build/app platforms/webos/build/service --outdir platforms/webos/build --no-minify
ares-install --device YOUR_LG_ALIAS platforms/webos/build/tv.afnon.webos_0.2.8_all.ipk
ares-launch --device YOUR_LG_ALIAS tv.afnon.webos
```

The `ares-*` commands come from LG's webOS CLI. Pair the TV using its Developer Mode
app first. This checkout used CLI 3.2.6. Enable startup in Islamic TV Launcher Settings after install.

`--no-minify` preserves the prebuilt ES5 files checked by the regression suite.
Use `ares-device --system-info --device YOUR_LG_ALIAS` for read-only device info
with this CLI; its older `ares-device-info` command is a deprecated stub.

The build requires the local `state/media/{adhan,announce}/` recordings (see
`tools/fetch-adhans.sh` and `tools/fetch-announce.sh` if absent). It reuses
the common `tools/validate-shared.mjs` checks, emits literal token values into CSS,
and parses every shipped JavaScript file as ES5. Build outputs and packages are
ignored; each build replaces its generated app/service directories. The browser check uses a temporary, headless Microsoft Edge profile and
writes desktop simulation screenshots to ignored `state/webos-port/`. These do not
capture the real TV's video plane or establish audible output.

`test.cjs` covers the legacy runtime without Symbol/Intl/new Math methods, every city
and method, date rollover, tomorrow's Fajr, input validation, atomic-write failure,
once-per-boot launch, ambiguous launch replies and startup disable. New checks cover
offsets, ICU calendar agreement, scheduled-event deduplication, expiry/cancellation,
and invalid media/preferences. Browser checks mock Luna and decoder events while
testing both directions, sidebar/scroll navigation, saving/cancelling editors, HDMI
retention under silent overlays, Stop/end/error recovery, radio and appearance.
They cannot establish real hardware decoder behavior or audible output. Voice-list
coverage also checks all 14 entries, inline previews and retained scroll position;
alert tests check returning to the original test row and the adhan Stop/Play controls.

## Diagnostics and removal

The service exposes `status`, `inputs`, `savePrefs`, `selectInput`, `exitToInput`,
`setStartup`, `startupTick` (Activity Manager callback only), `syncAlerts`,
`firePrayer`/`fireTest` (Activity Manager callbacks only), `takeAlert`, `testAlert`,
`cancelTest`, allowlisted `launchApp`, and bounded `report`/`media` diagnostics.
`status` returns the latest UI view/instance, selected input and startup result.
These records stay on the TV; there is no remote telemetry or screen capture.

For developer-shell diagnostics on this firmware, keep stdin open while Luna replies:

```sh
sleep 4 | luna-send-pub -w 3000 -n 1 -f luna://tv.afnon.webos.service/status '{}'
sleep 4 | luna-send-pub -w 3000 -n 1 -f luna://tv.afnon.webos.service/setStartup '{"enabled":false}'
```

Turn off scheduled alerts in Settings, cancel a pending test, and disable startup
before uninstalling with `ares-install --device YOUR_LG_ALIAS --remove tv.afnon.webos`.
These actions cancel only this package's own activities; they do not touch other apps,
native input settings or Developer Mode.

## Hardware validation

Version 0.2.8 was installed over 0.2.7 on the LF6300 on 12 November 2026. All 100
installed payload files match the reviewed package. All 30 preferences, Arabic,
HDMI 1 and the complete startup state survived unchanged; the owner confirmed
unchanged picture/sound with no visible error. The installed core passes all 66
assertions on Node 0.10.25 and WebKit 538.2, with a ready decoder and no video error.
Actual Arabic/English DOM checks cover 66 bound controls plus 22 declared gaps,
canonical labels, choice values/conditional variants, five prayer bindings and
Home/return-focus vectors. English was temporary and Arabic was restored exactly.
Installed Arabic status values have clear clipping bounds without diagnostic CSS.
The owner also confirms joined/readable Arabic values and physical Home Up/Down,
All apps, OK and Back/return-focus routes. These checks are specific to this device;
they do not establish support on other models or implement the declared gaps.

Version 0.2.7 uses shared Home navigation. Its in-place update preserved all 30
current preferences, HDMI 1 and startup; all 100 installed files match the build.
Actual WebKit 538.2 passed the column/top-controls, All apps and last-app vectors
in Arabic/RTL, and all 50 core assertions passed on Node 0.10.25. The owner
confirmed the physical Up/Down/OK/Back routes and return focus work as expected.
The owner then performed a watched cold power cycle: the launcher opened
automatically with HDMI picture/sound. Service reads verified a new boot and
exactly one successful startup launch, preserving all 30 preferences and the input.
After the owner opened Browser for 90 seconds, it stayed visible; a same-boot read
confirmed the launch count did not increase. This does not establish settings/action
catalog parity or support on other models.

Version 0.2.6 fixes old-WebKit clipping of Arabic status values such as enabled,
disabled and silent; long settings values wrap. Its in-place update from 0.2.5
preserved all 30 current preferences, HDMI 1 and startup. All 100 installed payload
files matched the reviewed build; live WebKit verified the corrected values with
no diagnostic CSS override and unchanged normal row height. The owner confirmed
the installed text is fully readable. Runtime/data/audio/notices are unchanged.

Version 0.2.5 adds serialized scheduling and the portable shared core. On the
LF6300 / webOS 2.2, its in-place upgrade from 0.2.4 preserved all 30 preference
values, HDMI 1 and the entire enabled-startup state. Prayer rescheduling succeeded;
the owner confirmed unchanged picture/sound with no visible error. All 100 installed
app/service payload files matched the reviewed build. The installed service core
passed 30 adapter assertions on Node 0.10.25; live WebKit 538.2 confirmed the shared
core, offset/sunrise rules and a ready HDMI decoder without error. These observations
also include owner confirmation that the physical OK/prayer-bar/Home/arrows/
Settings/Back sequence and focus work normally. The owner also confirmed joined
Arabic/RTL and the 12-second adhan's sound, automatic stop and normal HDMI/settings
restoration. These checks do not validate other models/firmware. The separately versioned observations below
still belong to 0.2.4 and earlier; do not relabel them as 0.2.5 tests.

Version 0.2.3 adds the All apps header and appearance draft workflow. Live TV DOM
checks confirmed five app columns, Back restoring Home's All apps focus, all 20 theme
entries, focus changing the preview to Mosaic, and cancellation retaining Aurora.
The actual WebKit 538 renderer exposed an SVG `height:auto` incompatibility; explicit
250/644-pixel preview heights fixed it on-device. Browser regression checks include
all theme entries reached by D-pad, draft cancellation, Apply, both layout directions
and labeled controls across Home layouts. The corrected package was reinstalled;
reading its installed CSS confirmed both explicit heights. All existing preference
values, HDMI 1 and enabled startup were unchanged afterward, and the service reported
the input view without an error. The owner confirmed All apps Back, then reported
Settings' missing Back arrow, undersized preview, full-page pickers instead of Android
carousels and patterns leaking onto Settings; these are addressed in 0.2.4.

On 0.2.3, both closed and suspended launcher tests interrupted the native Browser.
SSAP confirmed Browser beforehand; the closed case created a fresh launcher instance,
and the suspended case reused its existing instance. Both played the bounded adhan,
then returned to embedded HDMI 1. The owner confirmed the first test's sound/picture
and identified the unwanted HDMI return. A separate launch/close experiment restored
Browser through LG's app history. Public `getForegroundAppInfo` returned "Unknown
method"; no pairing key or external PC is added to the runtime.

Version 0.2.4 uses our visibility report captured before delivery, plus cold-start
alert detection, to distinguish external interruptions from alerts in the launcher.
External alerts release media and close the app after completion/dismissal; LG chooses
the underlying app from its history. In-app alerts retain their original screen.
This is not an explicit foreground-app snapshot: a missed visibility report or an
underlying app evicted by LG can limit restoration. A third-party app may resume
paused or reload; preserving its playback position is outside this launcher's control.
The service is restarted by Activity Manager when due, not immediately after every
user Close action. Disabling scheduled alerts or losing the Developer Mode installation
prevents that delivery. This does not wake the TV from standby.

The first 0.2.4 Browser-return test closed the launcher before arming the 30-second
adhan. The event recorded `returnToPrevious:true`, started a new instance and advanced
the decoder by 9.7 seconds; the final UI report was hidden and SSAP identified Browser
in the foreground again. The owner heard the adhan and found Browser afterward; the
recorded foreground read establishes that Browser was also in front before delivery.

After removing tutorial text, the final 0.2.4 UI was inspected on WebKit 538: the
preview measured 704×396, five inline carousels were present, pointer theme preview
changed Aurora to Ember, and leaving/reopening retained Aurora. The visible Settings
Back arrow returned to All apps. Settings' computed background stayed `none` with a
pattern selected on the stage, and the removed input hint/settings footer were absent.
Core/service and browser regression checks passed, including foreground alert return,
external adhan/banner exit without restarting HDMI, cold-start return, draft Apply/
Cancel, pointer/D-pad navigation, RTL and Home-only patterns. Browser media is mocked.
An expired pending cold-start alert also cannot cause a subsequent in-app test to
close the launcher. A separate warm-instance silent banner test began over native
HDMI 1, consumed the event once and returned to `com.webos.app.hdmi1` according to
SSAP; the launcher instance was reused. This checks native-input restoration without
another audible interruption.
The final 0.2.4 package was installed with identical saved preferences, HDMI 1 and
enabled startup; prayer rescheduling succeeded. A final live DOM read confirmed the
appearance page, Back label, Aurora selection, 396-pixel preview height, five carousels
and absent helper copy, with no reported UI error. On 11 November 2026, the owner
confirmed that Left/Right theme browsing, the larger preview, scrolling to the other
appearance rows, the top Back arrow and remote/pointer navigation looked and worked
better without the extra instructional text. These physical UI checks passed.

Version 0.2.2 was installed on 10 November 2026. A before/after comparison confirmed
all preferences, HDMI 1 and enabled startup were preserved. Live Web Inspector reads
confirmed five 64×64 top controls, the three Home app tiles and all 16 app links on
the TV. Clicking Browser through the actual app UI changed SSAP's foreground app to
`com.webos.app.browser`; reopening the launcher restored its `ext://hdmi:1` source
and Home. The owner confirmed the small top icons, arrow/OK and pointer navigation,
access to the app tiles, and that an app-grid shortcut opens the intended LG app.

The 0.2.2 closed-app background test also passed. The launcher was explicitly closed,
the developer running-app list was empty, and SSAP identified native HDMI 1 in the
foreground before arming the separate 30-second test. Activity Manager delivered it
once, started a new launcher instance, and consumed the alert. Decoder time advanced
to 9.7 seconds within the bounded test window; the final UI report was HDMI 1 with
no error. The owner confirmed automatic opening, audible adhan and normal HDMI
picture/sound afterward. This verifies delivery with the app closed and TV on;
it does not establish wake from standby or an unattended overnight prayer run.

Android parity remains incomplete: Home favorites cannot yet be customized/reordered,
there is no recent-app history, and app symbols are generic local artwork. Input/radio
controls still open their existing full pages rather than Android's quick popovers.
The catalog is a snapshot, and other native apps cannot run inside the HDMI layer.

The remaining settings gaps are room-facing Qibla calibration, radio screen-off timing,
system-language/clock following, app filtering, dock-tab/off modes, lighting strength,
focus lighting, animation/reduced-motion behavior, appearance cycling and theme import.
Android screensaver, default-Home, accessibility repair and global remote remapping
depend on system APIs absent from this unrooted port. See the implemented settings
above rather than treating a similarly named LG option as complete Android parity.

Version 0.2.0 was installed on 10 November 2026, preserving Cairo/Arabic settings,
HDMI 1 and enabled startup. The owner confirmed that an in-app banner test appears
over HDMI with picture/sound uninterrupted and that Back dismisses it. Web Inspector
DOM reads also worked, showing the updated Home, input source and new clock setting.
This is not a TV screenshot or audible confirmation. The owner then confirmed on
0.2.1 that all 14 voices are reachable, a preview near the bottom plays within the
list, and the short adhan plays with the central mihrab and returns to the same test
menu. Cairo Holy Quran Radio also played, with HDMI picture/sound restored afterward.
This does not establish playback of every voice/station or full-length adhan completion.

The 0.2.1 installation retained the owner's subsequently selected English, Aurora,
12-hour clock, voice and enabled scheduled-alert settings. The service accepted the
next reminder for 11 November 2026 at 04:33 Cairo time, before Fajr at 04:48. A prior
in-app 30-second test has a recorded Activity Manager callback, successful app launch
and consumed alert; media telemetry recorded advancing adhan audio before Stop.
These service/decoder observations do not replace the owner's audible confirmation.

On 10 November 2026, version 0.1.0 was installed on the LF6300 / firmware 04.06.75.
The viewer confirmed HDMI picture/sound and OK/arrows/Back navigation through the
prayer bar, Home and Settings, returning to HDMI without exiting Islamic TV Launcher. The service
reported the same app instance through this navigation, HDMI 1 selected, no error.
The power-cycle check subsequently confirmed an automatic launch on a new kernel
boot, with one launch recorded and HDMI picture/sound intact. The initial OK failure
was isolated by the viewer to Magic Remote pointer mode: visible-pointer OK dispatches
a click, and the original app ignored clicks on the full-screen input background.

Version 0.1.1 was installed over that build, preserving settings and enabled startup.
It handles the background click, uses the requested product name, moves the compact
prayer strip to the top, brings Home closer to the Android layout, and changes the
Arabic prayer-name font. The pointer regression failed before the fix and passed
after it, together with both-language browser checks. The viewer then confirmed on
the LG that clicking with the visible pointer opens the top bar, its Home action
works, and both the Arabic prayer name and Quranic text render correctly.

A post-boot resource snapshot showed 414,220 kB available RAM out of 936,428 kB and
no swap use. This does not establish general performance, but there was no evidence
of memory exhaustion. No LG apps were disabled or removed. The pointer bug did not
require debloating. App-only Web Inspector evaluation worked; its screenshot request
did not return a usable image, so physical confirmation is still used for rendering.

HDMI embedding and overlays are based on behavior verified on this television,
not a promise of compatibility across LG models. Tuner, AV and component mappings
are included but are not yet hardware-verified in this port. Samsung Tizen remains
a separate target. See [research and earlier probes](../../docs/WEBOS-TIZEN-PORT-RESEARCH.md).

Sources: [LG Back button](https://webostv.developer.lge.com/develop/guides/back-button),
[app lifecycle](https://webostv.developer.lge.com/develop/guides/app-lifecycle-management),
[service storage](https://webostv.developer.lge.com/develop/guides/js-service-usage),
[community input embedding](https://www.webosbrew.org/develop/guides/web-app/),
[adhan-js](https://github.com/batoulapps/adhan-js).
