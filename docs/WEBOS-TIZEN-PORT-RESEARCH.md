# LG webOS and Samsung Tizen port research

This document preserves the initial research and probe history. The working LG
port now lives in [`platforms/webos/`](../platforms/webos/README.md); use that README for current
features and versioned validation, and [`platforms/webos/AGENTS.md`](../platforms/webos/AGENTS.md) for
maintenance. Later tests verified Magic Remote controls, local adhan, Cairo radio,
app shortcuts and Browser interruption/return. Earlier "not yet tested" statements
below describe their experiment date, not the current port. Tizen remains research.

Investigated 26 October 2026; updated 27 October for the photographed target TV
and 10 November after Developer Mode pairing, a firmware check and successful
background scheduling, short-audio, embedded-HDMI, input restoration and automatic-start probes.
LG webOS is the next target.

**Recommendation:** build on the successful app/service probe on the LG
43LF6300-TA running webOS 2.2.3-178. Reuse Afnon's design, generated data and
prayer logic, but build for this old web engine rather than simply packaging the
current preview. A foreground prayer dashboard with an HDMI viewing mode is now
supported by the device tests; app shortcuts, Quran radio and full adhan still
need checks. The inspected
Razdva Home helper does not support this TV's documented stock service runtime.
Full Android launcher parity remains unproven. Keep Samsung for a later port.

This combines source research with physical-TV checks on 10 November 2026.
Developer Mode pairing, a temporary app/service installation, scheduled callbacks,
short MP3 playback, embedded HDMI, recorded-input return and once-per-boot startup
succeeded. Probe source is in `tools/webos-background-probe`; the
production Android app and web preview were not changed. No firmware or root
modifications were performed. The remote type remains unconfirmed.

## Confirmed LG target and changed scope

The user's photo shows **43LF6300-TA**, **webOS TV 2.2.3-178
(beehive-biscayne)**. This is the webOS version, not the separate software/firmware
version. Serial numbers and network identifiers are not needed for this research.
LG describes the [LF6300 family](https://www.lg.com/ph/tv-soundbars/full-hd-tvs/43lf6300/)
as Full HD LED LCD with webOS 2.0; this target is not an OLED set.

Live results from LG CLI 3.2.6 on 10 November 2026:

| Check | Observed result |
| --- | --- |
| Developer connection and Key Server | Reachable; key retrieval and authenticated SSH succeeded |
| Model reported by `ares-device --system-info` | `43LF6300-TA` |
| Firmware | `04.06.75` |
| SDK version | `2.2.0`; keep this API field distinct from the photo's webOS display version `2.2.3-178` |
| Board | `M14_DVB_EU` |
| `node --version` on the TV | `v0.10.25` |

The CLI's additional session-info query returned that the session-manager service
does not exist on this device. That query concerns multiple sessions; its failure
does not negate successful Developer Mode access or reveal the expiry timer.

**Firmware:** the installed `04.06.75` matches the newest download listed on
[LG's support page](https://www.lg.com/ae/support/product/lg-43LF6300.AFT), whose
applicability list explicitly includes `43LF6300-TA` (download dated 14 November
2022). No newer official download was found on 10 November 2026, so no firmware
was downloaded or flashed. The TV's own online update check was not run. LG's
[update instructions](https://www.lg.com/us/support/help-library/lg-tv-how-to-update-an-lg-smart-tv-CT10000018-1430510575535)
place that check under Settings > All Settings > General > About This TV for
webOS 3.5 and older. Matching the latest listed firmware has not changed this
TV's measured SDK or service-runtime limits.

| Constraint | Consequence for this TV |
| --- | --- |
| webOS 2.x packaged apps use WebKit 538.2; the browser app uses Chromium 34 | A page working in the TV browser would not prove the installed app works. Target the packaged-app engine. |
| LG documents Node.js 0.10.25 for webOS 2.x services, confirmed on this TV | Razdva's inspected No-root edition requires TV Node.js 0.12.2 or newer and rejects older runtimes. It cannot be adopted as-is. |
| The inspected lasharor root port requires webOS 5 or newer | It is also not a ready-made Home replacement for this target. |

Sources: [LG engine table](https://webostv.developer.lge.com/develop/specifications/web-api-and-web-engine),
[LG service runtimes](https://webostv.developer.lge.com/develop/guides/js-service-basics),
[Razdva compatibility](https://github.com/Razdva122/clean-your-tv-lg-webos/blob/7cce1da4244d67375a0ac890a02710b32864f6a9/docs/COMPATIBILITY.md),
[lasharor source](https://github.com/lasharor/cleanyourtv-WebOS/tree/2de173690d763c2b189b32a67a202eeabc30564a).
The engine and helper constraints are documented; the service runtime is now
measured on this TV. The mismatch does not rule out writing a much smaller compatible
prayer service; its permissions and scheduling behavior still need proof.

**Basic app installation and background callbacks are now proven.** LG's [CLI guide](https://webostv.developer.lge.com/develop/tools/cli-dev-guide)
describes Developer Mode testing from webOS 3.0, while the community project's
[Developer Mode guide](https://github.com/webosbrew/webosbrew.github.io/blob/main/src/views/guides/devmode/quick-guide.md)
lists 2015-or-newer TVs. The earlier uncertainty is resolved for this set: the
user installed Developer Mode and pairing succeeded on 10 November. A minimal
app/service package also installed, launched and completed the background probe
described below. Afnon's full UI, audio and real prayer scheduling remain to be
implemented and tested.

**The first build should be deliberately small.** Use an ES5-compatible classic
script bundle, local fonts and simple CSS with explicit RTL layouts. Replace or
provide compatible alternatives for the preview's CSS variables, modern selectors,
`color-mix()`, `ResizeObserver` and `replaceChildren()`. Validate the compiled
prayer dependency and date/Hijri formatting rather than assuming modern `Intl`
support. Astro can remain a build tool on the computer; its generated output
must meet the TV's limits. Start with one screen showing Arabic text and prayer
times, a few focusable controls, saved preferences, local audio and one known app
launch. Confirm the remote's Back behavior as well as its arrow keys.

Radio is a separate compatibility check: webOS 2 supports TLS 1.2, but certificate
trust and station formats need testing. It lacks MSE, so a modern MSE-based
streaming fallback is not a solution. Use supported native playback and report
unavailable streams; do not disable certificate checks. See [LG TLS support](https://webostv.developer.lge.com/develop/specifications/tls)
and [streaming support](https://webostv.developer.lge.com/develop/specifications/streaming-protocol-drm).

A packaged prayer dashboard can be designed
to calculate locally once installed, given a correct clock and configured location;
radio still needs connectivity. [Developer Mode provisioning and maintenance](https://webostv.developer.lge.com/develop/getting-started/developer-mode-app)
also need connectivity, and session expiry can remove development-installed apps.
Do not describe a Developer Mode installation as permanent offline deployment.

For this target, Home takeover and adhan while another streaming app is open are
**unproven follow-up experiments**. A bounded once-per-boot startup test has passed;
ongoing reliability and deployment maintenance remain. An external Home watcher is
only a possible fallback if this TV exposes the necessary paired APIs and an
always-on companion is acceptable. If full existing Android behavior is essential,
running the current Android app on a compatible Android TV device over HDMI is
the alternative to developing around these webOS 2 limits.

## Evidence inspected

| Source | Snapshot |
| --- | --- |
| Local Afnon / IslamicTVLauncher | `fbf02ad`, 26 October 2026; clean at initial inspection |
| Local CleanYourTV | `f9f0c53`, 23 October 2026; existing theme/config edits left untouched |
| Current CleanYourTV upstream | `76a9312cd9ac378fc8698f31427805af32c6f965` |
| OmkarK97/webos-vibehack | `31a518aef23768067975f3ac2e464462a8046bab` |
| Razdva122/clean-your-tv-lg-webos | `7cce1da4244d67375a0ac890a02710b32864f6a9`, 26 October 2026 |
| lasharor/cleanyourtv-WebOS | `2de173690d763c2b189b32a67a202eeabc30564a`, default branch `dev` |

Current upstream was checked because the local CleanYourTV checkout is older.
For example, its showcase describes an older long-Home escape gesture; current
Razdva documentation describes triple Home. Release notes and hardware evidence
must be matched to a particular component version.

On 26 October, Context7 resolution for both platforms returned a monthly-quota
error, so official LG/Samsung documentation and upstream source were used instead.
On 27 October, Context7 supplied webOS CLI and community Developer Mode documents;
LG's own engine and service tables were checked for the webOS 2 target.
These are consumer-TV findings; webOS OSE, signage and hospitality permissions
must not be assumed to apply.

## What CleanYourTV actually does

The main repo remains an Android project. Its `tools/tv.py` uses ADB; its launcher
is Java/Android. Neither can be installed directly on webOS or Tizen. The project
routes platform ports to independent repositories in its
[showcase](https://github.com/Gathin23/cleanyourtv/blob/76a9312cd9ac378fc8698f31427805af32c6f965/SHOWCASE.md#other-platforms-fire-tv-and-lg-webos).

### LG: three separate approaches

| Project | Mechanism found | What it means for Afnon |
| --- | --- | --- |
| [OmkarK97/webos-vibehack](https://github.com/OmkarK97/webos-vibehack) | HTML/CSS/JS app installed through Developer Mode. Setup reads installed app launch points and inputs over paired SSAP, downloads icons and generates a local catalogue. A Python watcher on another device sees LG Home become foreground and launches the custom app. | Small, understandable reference for inventory, launching and input shortcuts. Home redirection needs an always-on device and can visibly pass through LG Home. |
| [Razdva122/clean-your-tv-lg-webos](https://github.com/Razdva122/clean-your-tv-lg-webos) | Extends the preceding approach with an on-TV native Home helper, JS service, foreground monitoring, startup/recovery logic and installers. Current repo also has a separate Root edition. | Most relevant reference for a computer-free daily experience. The No-root helper is a separate compatibility project, beyond ordinary web-app packaging. |
| [lasharor/cleanyourtv-WebOS](https://github.com/lasharor/cleanyourtv-WebOS) | Requires existing root/Homebrew access. Replaces Home through a RAM mount with boot scripts and a root service; adds inputs, notifications and optional debloat. | Reference for a possible advanced edition. Root availability and recovery become product requirements; do not make this the initial dependency. |

Omkar's source separates app launch from discovery:
[app/app.js](https://github.com/OmkarK97/webos-vibehack/blob/31a518aef23768067975f3ac2e464462a8046bab/app/app.js)
calls `luna://com.webos.applicationManager/launch`, while
[sync-apps.py](https://github.com/OmkarK97/webos-vibehack/blob/31a518aef23768067975f3ac2e464462a8046bab/sync-apps.py)
uses `listLaunchPoints` and `getExternalInputList` over SSAP.
[home-watch.py](https://github.com/OmkarK97/webos-vibehack/blob/31a518aef23768067975f3ac2e464462a8046bab/home-watch.py)
subscribes to foreground-app changes and redirects LG Home. Its timing claims
are not a latency guarantee for another TV.

Razdva's No-root package declares `type: native`, `noWindow: true` in
[appinfo.json](https://github.com/Razdva122/clean-your-tv-lg-webos/blob/7cce1da4244d67375a0ac890a02710b32864f6a9/native/app/appinfo.json).
Its [relay.c](https://github.com/Razdva122/clean-your-tv-lg-webos/blob/7cce1da4244d67375a0ac890a02710b32864f6a9/native/prototype/relay.c)
opens specific `LGE M-RCU - Builtin [0]` and `[1]` input devices and validates
compatibility. The [controller](https://github.com/Razdva122/clean-your-tv-lg-webos/blob/7cce1da4244d67375a0ac890a02710b32864f6a9/native/app/native.js)
launches the Home app, monitors the relay, and registers a persistent scheduled
activity for guarded startup. This is why “no root” does not mean “portable public
web API.” The repo itself says it is not an LG-supported system Home replacement.

Its [compatibility record](https://github.com/Razdva122/clean-your-tv-lg-webos/blob/7cce1da4244d67375a0ac890a02710b32864f6a9/docs/COMPATIBILITY.md)
contains historical C3 and C4 evidence, pending verification of newer No-root
helper behavior, and unresolved C5/webOS 26 startup. Root and No-root evidence
are separate. Do not infer that a current release tag validates every component
on every listed TV. Preserve input checks, watchdogs and escape behavior if code
is reused; use Afnon-specific app identities rather than overwriting either project.

### Samsung: a capability report, not a launcher port

The current [Samsung report](https://github.com/Gathin23/cleanyourtv/blob/76a9312cd9ac378fc8698f31427805af32c6f965/docs/devices/samsung-tizen.md)
covers AU8000, DU7000, NU7405 and Q67BA observations. The useful application test
is the DU7000 session of 23 October:

- A signed full-screen web app and physical D-pad navigation worked.
- `tizen.application.getAppsInfo()` returned 486 entries, including system/helper entries.
- Three selected streaming apps launched successfully.
- Source discovery and displaying connected HDMI 1 through `tizen.tvwindow` worked.
- Physical Home still opened Samsung Home.
- One standby sequence reopened the diagnostic app; another returned to HDMI.
  A long-press restart returned to HDMI. This did not prove reliable boot startup.

The other sets add reconnaissance, not successful custom-app tests. There is no
ADB debloat workflow, verified system-app removal or full restorable TV backup.
SDB developer access is for installing/debugging signed apps, not unrestricted shell access.

Samsung documents both [application discovery/launch](https://developer.samsung.com/smarttv/develop/api-references/tizen-web-device-api-references/application-api.html)
and [TV source windows](https://developer.samsung.com/smarttv/develop/guides/pip-picture-in-picture/managing-pip-windows.html).
HDMI inside our own app could eventually keep prayer UI active while showing an
external source. It does not place our UI over another streaming application;
composition, audio and return behavior need a real-TV test.

## What we can port

These general assessments apply to a regular unrooted application unless a helper is named.
The webOS 2 constraints above take precedence for the first TV; reuse means adapting
source and data, not that the current generated web preview runs unchanged.
“Candidate” means there is a plausible mechanism, not demonstrated Afnon behavior.

| Afnon feature | LG webOS | Samsung Tizen |
| --- | --- | --- |
| Mihrab, prayer times/countdown, Qibla, Friday/Ramadan, offsets | Reuse logic/data; legacy build and date-format validation for webOS 2 | Same reusable web code |
| Arabic/English, RTL, fonts, colours, backgrounds, layouts | Reuse with engine compatibility fixes | Reuse with model-year compatibility fixes |
| Settings and favourites | Local storage; replace example tiles with real app IDs | Local storage plus public app discovery |
| Launch installed apps | Public launch-by-ID API; catalogue needs setup sync or a separately validated discovery service | Public Application API; filter system/helper entries |
| HDMI/settings shortcuts | Embedded HDMI 1 picture, sound and a concurrent UI counter confirmed on this LF6300; undocumented external-video path. Input/settings shortcuts remain separate | TVWindow/VIDEOSOURCE documented; app-contained picture is distinct from system handoff |
| Quran radio | Existing HTML audio is useful; test each stream and lifecycle | Same starting point; test formats and TV media behavior |
| Adhan while Afnon is visible | Four-second generated MP3 completed in the probe; full recordings and prayer scheduling remain | Implement the same foreground behavior |
| Adhan while another app is visible | Scheduled service restart, app launch and MP3 completion passed during the HDMI test; other apps and reliable return behavior need verification | Ordinary app JavaScript pauses in background; no equivalent reliable mechanism established here |
| Prayer dock/banner over other apps | No supported equivalent established in the reviewed consumer APIs | No supported equivalent established in the reviewed consumer APIs |
| Physical Home redirects to Afnon | Community helpers on supported hardware; neither inspected on-TV port supports this webOS 2 target as-is. External watcher untested | No supported Home replacement established |
| Launch automatically at power-on | Bounded persistent activity plus a boot-ID guard passed on this LF6300, about 44 seconds into a new boot; ongoing reliability remains to test | Last-app autorun is conditional, not a launcher registration |
| Afnon ambient/screensaver UI | Inside Afnon; retain LG's OLED protection policy | Inside Afnon; follow Samsung display/lifecycle policy |
| Android system screensaver replacement, global key remapping, TCL Audio Only | Android-specific; separate research, outside first release | Android-specific; outside first release |
| Android installer, ADB cleanup/recovery | Replace with webOS packaging/setup | Replace with signed Tizen packaging/setup |

LG's [Application Manager](https://webostv.developer.lge.com/develop/references/application-manager)
documents launch and installed-state lookup for a known ID, not a public inventory
method. This distinction explains the companion discovery in the community ports.
Treat undocumented settings/input targets as capabilities to probe and hide when unavailable.

## Existing Afnon code is the best starting point

The Java app is not a portable runtime. Its Android
[manifest](../app/src/main/AndroidManifest.xml) registers HOME, an accessibility
service and a DreamService.
[PrayerWatch.java](../app/src/main/java/tv/afnon/home/PrayerWatch.java) runs from
`RemoteKeys`, checks every 15 seconds, shows an accessibility overlay and opens
`NowActivity`. [PrayerDock.java](../app/src/main/java/tv/afnon/home/PrayerDock.java)
also uses Android accessibility windows. Those behaviors require new implementations.

The browser preview already provides much of the product:

| Existing source | Reuse and necessary work |
| --- | --- |
| `site/src/lib/prayer.ts`, `ctx.ts`, `text.ts` | Real adhan-js prayer calculations, Qibla, Hijri/date formatting and translated screen models. Verify device timezone/DST and Intl calendar behavior. |
| `site/scripts/extract.mjs`, `tokens.mjs`, `afnon-mihrab/tokens.json` | Existing shared data/design pipeline: cities, methods, strings, stations and fonts come from the Android sources. Preserve this instead of maintaining separate catalogues. |
| `site/src/screens/*.astro` and models | Reuse screens and appearance controls in a TV-specific full-screen entry. Remove surrounding website navigation and simulated test modes. |
| `site/src/scripts/tv.ts` | D-pad and pointer focus foundation. Add actual TV Back handling and reliable focus restoration. |
| `site/src/screens/Home.astro` | Already plays radio through an audio element. App/input/settings actions currently show toasts; wire them to platform operations. |
| `site/src/screens/apps.ts`, `Apps.astro` | Example apps include Android-specific entries. Replace with discovered/selected apps and persist genuine favourites. |
| `site/src/screens/Call.astro` and `call.ts` | Call-screen presentation exists, but the preview does not play adhan. Add audio, scheduling and once-only event handling. |
| `site/src/scripts/live.ts` | Saves preferences and renders every second. Add suspend/resume handling and validate stored values; the current preference merge is not a full schema validator. |
| `site/scripts/test.mjs`, `site/tests/` | Existing assert-based checks for offsets, home layout and appearance cycling; extend only for new behavior. |

Three concrete packaging issues must be solved:

1. **URLs and navigation.** The Astro configuration currently uses a GitHub Pages
   base and directory routes. A packaged app needs working local entry, asset and
   navigation paths. The current per-page audio element is lost on full navigation;
   keep a loaded TV shell if radio must continue across settings/prayer screens.
2. **Engine support.** The code uses `color-mix()`, `:has()`, `:where()`, logical
   properties, `replaceChildren()` and `ResizeObserver`. The target's WebKit 538.2
   needs a separate compatibility pass as described above. Even newer sets need
   checks: LG documents Chromium 108 for webOS 24, while `color-mix()` arrived in
   Chromium 111.
   Check each feature against the chosen minimum engine; a JS build target does
   not fix unsupported CSS or DOM APIs. See [LG engine table](https://webostv.developer.lge.com/develop/specifications/web-api-and-web-engine)
   and [Chrome 111 release notes](https://developer.chrome.com/blog/chrome-111-beta).
3. **Audio and assets.** Reuse the station list, but test MP3/AAC/HLS, TLS,
   redirects and failure recovery per station. Radio and adhan should share one
   controlled audio path. LG recommends one audio element and pausing on suspend
   in its [audio guidance](https://webostv.developer.lge.com/develop/guides/multi-sound-playback).
   Downloaded adhan/announcement recordings are excluded from Git and marked as
   not licensed for redistribution; packaging cannot silently bundle them.

## Background prayer: initial scheduler test passed

On 10 November, the unrooted LF6300 successfully ran a single Activity Manager
callback after both the probe app and its original service process had exited.
The callback started a new service process and reopened the probe app. All times
below are UTC, recorded by the TV:

| Event | Observation |
| --- | --- |
| App/service installed | `tv.afnon.bgprobe` version `0.0.1`; ordinary Developer Mode installation |
| Initial screen JavaScript acknowledged | 16:38:20.207 |
| Scheduled | 16:40:03.673; activity 111; service PID 5938; one callback about 90 seconds later |
| UI and original process closed | At 16:40:19 the running-app list was empty and `/proc/5938` was absent |
| Scheduled UTC second | 16:41:33; no repeating interval and `persist: false` |
| Callback | 16:41:34.723; fresh service PID 6028, started at 16:41:34.686 |
| App launch | Application Manager returned success |
| Reminder screen JavaScript acknowledged | 16:41:35.240; probe was again in the running-app list |
| Scheduled work cleanup | Cancel returned error 2, activity name/creator not found, consistent with automatic completion |
| Device cleanup | Probe app/service uninstalled, generated state file and empty service directory removed; developer-app and running-app lists empty |

The post-deadline check read the saved state file and running-app list; it did
not invoke the service or trigger the app. This distinguishes an actual scheduled
service restart from a computer-triggered reminder or a continuously running
service. The screen acknowledgement proves its script loaded, not a visual review
of the display. Device evidence is saved locally under ignored
`state/webos-background-probe/2026-11-10.json`.

This supports proceeding with an unrooted background prayer prototype. It is one
successful awake-TV trial, not proof of reliable daily timing, playback, overlays,
behavior over a particular streaming app/HDMI, reboot persistence or standby wake.
That first prototype contained no audio, recurring timer or boot hook. The later
media tests below added a bounded test tone and embedded HDMI.

### HDMI and audio follow-up, 10 November 2026

**HDMI inside Afnon works on this unrooted webOS 2.2 TV.** The owner confirmed
HDMI 1 picture and sound inside the app, with a changing seconds counter alongside
it. This directly tests the user's proposal to keep Afnon foreground while viewing
an external source. It does not make Afnon a background overlay over other apps.

The probe uses the [webOS Homebrew external-input recipe](https://www.webosbrew.org/develop/guides/web-app/):
an HTML video source with `type="service/webos-external"` and `src="ext://hdmi:1"`.
This is documented by Homebrew as an undocumented platform feature, not an LG
public consumer API. The same guide describes tuner input, but that was not tested.
The HDMI element reported `playing` while `videoWidth`, `videoHeight` and
`currentTime` stayed zero; those properties cannot establish picture failure on
this external-input path. The owner's visual confirmation is the stronger evidence.

The second scheduled test used a locally generated four-second MP3 tone:

| Event, UTC | Observation |
| --- | --- |
| 17:22:08.532 | Audio reminder scheduled; activity 130, original service PID 7904 |
| 17:22:38 | Original service process absent; probe app not running |
| 17:23:39.800 | Scheduler invoked a fresh service, PID 7981; scheduled second was 17:23:38 |
| 17:23:40.409 | Relaunched screen acknowledged loading |
| 17:23:41.088 to 17:23:45.643 | Audio `playing`, advancing playback position, then `ended` at four seconds |
| 17:23:53.635 | Probe recorded closure; subsequent running-app list was empty |

The tone uses element volume 0.25 and leaves system volume unchanged. Completion
events prove the media pipeline ran, not audible output or successful HDMI return;
the later watched retry below supplies those observations. The original service was not polled during
the wait. Evidence is in ignored `state/webos-background-probe/2026-11-10-hdmi-audio.json`.

The first HDMI trial also exposed a media conflict: an unused audio element with
a file URL reported decoder error 3 despite `preload="none"`. The probe now assigns
the MP3 URL only in audio mode. A subsequent HDMI trial recorded no audio errors.
LG's [audio guidance](https://webostv.developer.lge.com/develop/guides/multi-sound-playback)
warns about hardware decoder conflicts. Simultaneous HDMI audio and adhan mixing
is therefore unproven; design the first port around explicit audio handoff.

A third trial showed/hid an in-app reminder banner while the embedded HDMI element
kept reporting progress and the UI counter advanced. In the watched retry, the
owner confirmed that the banner was above the HDMI video while picture and sound
continued. This establishes an overlay inside Afnon's own HDMI viewing screen,
not a global overlay over arbitrary TV applications.
The HDMI counter continued through 119 seconds before the probe's timed closure.
The activity had already completed when cancelled. The temporary app/service,
generated state file and empty service directory were removed; developer-app and
running-app lists were empty. System audio remained unmuted at volume 50, matching
the pre-test reading.

#### Watched retry and HDMI return fix

The owner missed the initial sound/banner trials, so version 0.0.4 extended the
banner's visible interval to seconds 4–100. The owner then confirmed both the
banner's composition over HDMI and uninterrupted HDMI picture/sound. Audio mode
was launched directly for this retry, without repeating the already logged
90-second scheduler test.

The owner heard the four-second tone, but closing the app returned to Live TV
programmes instead of HDMI. Embedding HDMI does not establish a reliable native
input return target. Version 0.0.5 explicitly launches `com.webos.app.hdmi1` from
the probe service when the audio screen closes. The owner confirmed that the next
tone was followed by HDMI 1 picture and sound. This is a fixed HDMI 1 test path;
restoring arbitrary previous sources or streaming sessions remains unimplemented.
Retry evidence is saved in ignored `state/webos-background-probe/2026-11-10-watched-retry.json`.
After verification, the temporary package and generated state directory were
removed again, leaving the TV on HDMI 1.

Remote app/input control works through Developer Mode, but Android-style screen
capture is not established. A request to `com.webos.surfacemanager/captureCompositorOutput`
returned `Unknown method`, and `com.webos.service.capture/executeOneShot` returned
`Service does not exist` through the current Developer Mode access. No screenshot
was generated. These results describe the tested access path, not every possible
privileged route. LG also states that there is
[no public consumer-TV screen-capture API](https://forum.webostv.developer.lge.com/t/how-to-capture-screen-on-webos-tv/7880).
No rooting, privilege changes or system modifications were attempted for capture.

#### Capturing the previous input

Version 0.0.6 replaces the fixed HDMI 1 return with a source snapshot immediately
before the reminder opens. On this LF6300, `com.webos.service.eim/getCurrentInput`
returns `mainInputSourceId`, `getLastInput` returns `lastSourceId`, and
`getAllInputStatus` provides input IDs and their launch app IDs. DTV is mapped to
Live TV; other sources must match a discovered and validated input application.
The snapshot is written atomically. An unknown source prevents the test reminder
from opening instead of falling back to an unrelated or stale input.

The hardware checks recorded and restored DTV, then HDMI 2. The latter finished
with `getCurrentInput` reporting `HDMI_2`, demonstrating that restoration is no
longer fixed to HDMI 1. HDMI 1 was restored afterward for the power tests. Input
switching is asynchronous: the first HDMI 2 trial began before that switch had
settled, captured DTV, and was interrupted by the delayed HDMI launch. Waiting
until the TV reported HDMI 2 before repeating the test produced the correct
snapshot and return. A product must also handle a source change already in progress.

This records the underlying TV input, not the foreground streaming app or its
playback position. The service captures it when the reminder fires, rather than
when it is scheduled, so an intervening input change is reflected.

#### Bounded startup experiment

The next probe uses a persistent Activity Manager interval of 30 seconds, with
a 20-minute expiry. It records `/proc/sys/kernel/random/boot_id` and claims a new
boot on disk before asking Application Manager to launch the silent startup
screen. Repeated callbacks in the same boot do not relaunch it. The activity can
be cancelled with `disableStartupTest`; no system startup file is changed.
The first attempt exposed a storage mistake. Turning the TV off and on with the
remote produced a new kernel boot ID, but the app did not appear. After reboot,
the installed service directory had become `root:root` mode 0775. Atomic writes
there failed with `EACCES`. The initially writable install directory was not a
valid persistent-data location. The non-persistent one-shot reminder did not run
while the TV was off, and the owner reported no automatic wake.

Version 0.0.7 moved state to a service-owned mode-0700 directory under
`/media/internal/tv.afnon.bgprobe`, following LG's
[service storage guidance](https://webostv.developer.lge.com/develop/guides/js-service-usage).
Writes remain atomic; the code rejects a pre-existing path that is not a directory
owned by the service UID. This avoids changing permissions on the installed code.

**The second remote off/on cycle passed automatic startup without root.** The owner
confirmed Afnon appeared automatically. Evidence, UTC:

| Observation | Result |
| --- | --- |
| Startup test enabled | 17:57:56.336, with the preceding boot ID recorded |
| New boot | Boot ID changed again; uptime was 72.75 seconds at 18:00:15 |
| Once-per-boot claim | 17:59:46.577, about 44 seconds into the new boot |
| Launch | Application Manager success; process ID `1000` |
| Screen acknowledgement | 17:59:50.394, before any diagnostic service call from the computer |
| Owner observation | Afnon appeared automatically after power-on |
| After closing Afnon | At least two further 30-second callbacks ran; launch count stayed at one and the app stayed closed |
| Cancellation | `disableStartupTest` cancelled the persistent activity successfully |

The later `status` request started a diagnostic service process at 18:00:16.466;
it does not run the startup callback and came after the recorded launch and the
owner's observation. The startup activity was removed after verification. Both
power cycles were controlled by the physical remote and produced new kernel boot
IDs. Mains disconnection, every future boot, and long-term Developer Mode retention
are not established by this test. This is delayed startup, not standby wake or
Home-button replacement.

Source/input and power-test evidence is in ignored
`state/webos-background-probe/2026-11-10-input-startup.json`.

#### Five-minute HDMI run and cleanup

Version 0.0.8 kept HDMI embedded with the UI counter running from 18:02:11 to
18:07:11 UTC. Progress records continued through 290 seconds, followed by the
planned closure at tick 299 and HDMI pause. No media error was recorded. Near the
end, the owner confirmed normal picture/sound and an advancing counter. This is
a short stability check, not a long-term endurance or frame-rate benchmark.

One diagnostic RPC returned `Message status unknown` while progress recording
continued; retrying succeeded. The same transient response occurred during data
cleanup, which required another attempt. Its cause was not established. A product
needs bounded recovery for service calls, with event IDs to avoid replaying a
reminder; do not present these trials as failure-free service reliability.

The persistent startup activity was successfully cancelled. The package, private
data directory and older generated files in the install directory were removed,
and HDMI 1 was restored. No permanent startup hook is left installed by this test.

The resulting port direction is an Afnon viewing screen that owns its HDMI window
and prayer UI. The scheduled service handles reminders after the user leaves it.
This does not establish always-on operation, Home-button replacement, internal
Netflix/YouTube embedding or standby wake. Delayed power-on startup has passed the
bounded test above.

LG [suspends the web app when hidden](https://webostv.developer.lge.com/develop/guides/app-lifecycle-management).
However, [JS services](https://webostv.developer.lge.com/develop/guides/js-service-basics)
can work independently of the visible app. They are not permanent daemons:
the [service FAQ](https://webostv.developer.lge.com/develop/guides/js-service-faq)
documents an idle exit and recommends Activity Manager for condition-based work.

The [Activity Manager](https://webostv.developer.lge.com/develop/references/activity-manager)
documents scheduled callbacks and persistent activities. The next experiments
should cover a streaming app, full adhan playback, mixed-media handoff, more power
cycles and longer operation. Recorded-input return and startup after a new boot
have passed the tests above.

The initial callback/relaunch path above passed; it is not proof of exact
alarm timing, overlays, waking from standby, audio mixing, or restoring the
previous streaming session. Persist event IDs, recalculate after date/timezone or
settings changes, and prevent stale/duplicate calls after resume. Do not carry
over the Android background loop as a browser `setInterval` and claim parity.

Samsung explicitly says [JavaScript pauses when an app is hidden](https://developer.samsung.com/smarttv/develop/guides/fundamentals/multitasking.html).
Therefore the first Tizen version should promise reminders while Afnon is open.
Other delivery mechanisms require separate TV-specific evidence; Tizen mobile or
wearable alarm/service documentation does not establish consumer TV support.

## Recommended LG sequence

### Background hooks and root findings, 10 November 2026

A suspended web page is not the limit of the platform. An Afnon JS service can be
written for the measured Node.js 0.10.25 runtime. LG's documented Activity Manager
provides scheduled callbacks, making a small service that opens Afnon at prayer
time a useful non-root experiment. The initial test of a callback firing after
the visible app and its service exited passed, followed by a short-audio test
during HDMI viewing. Tests over a streaming app remain. Standby wake, audio mixing, global overlays and
restoring another app's playback are separate claims and remain unproven.

For root, the generic webOS Homebrew guide is not sufficient: its older method
summary omits a more relevant legacy tool. The maintainer's
[mvpd-autoroot README](https://github.com/throwaway96/mvpd-autoroot/blob/5c739bf0c80bb820904c677394a88ba03556f24c/README.md)
targets webOS 1 through 3.4.2 and starts from Developer Mode SSH. However, the
[live LF6300/04.06.75 compatibility lookup](https://cani.rootmy.tv/?q=43LF6300+04.06.75)
returned the LF6300 family, machine `m14tv`, OTA ID `HE_DTV_W15M_AFAAATAA`, with:

- MVPD: **possibly patched**; latest known rootable firmware **04.06.40**, below
  this TV's **04.06.75**. This is not a confirmed vulnerable result.
- GetMeNow: may work on some webOS 1–3 models; Developer Mode updates may block it.
- NVM: a hardware modification requiring specialist knowledge.

The checker identifies a model-family entry; the TV's own OTA ID was not returned
by the earlier system-info query. Do not treat family-level compatibility as a
completed exploit test or downgrade to the older listed version on that basis.

Read-only checks on the actual TV returned `uid=6503(prisoner)`, kernel
`3.16.0-p.3.badlands.m14tv.1`, and a `/dev/mem` node whose permissions allow writes
from this account. The permission test did not open or write device memory.
This is a clue for the legacy GetMeIn/GetMeNow approach, not proof that memory
mapping or privilege escalation succeeds. The linked GetMeNow guide currently
says it is under construction. A limited search of service paths visible to the
developer account did not confirm MVPD's presence; the restricted filesystem view
does not establish that the service is absent.

If root can be established safely, [Homebrew Channel](https://github.com/webosbrew/webos-homebrew-channel/blob/6e928d01b21f0282a945ae2c8d3c65d25c9c1578/README.md)
documents boot hooks in `/var/lib/webosbrew/init.d` and a service for starting
long-running processes. That would support investigating an Afnon prayer daemon,
foreground-app monitoring and Home redirection. It does not automatically solve
audio focus, compositor overlays, standby operation or the old rendering engine.
Its source explicitly targets legacy Node.js 0.10, so the age of the runtime alone
does not rule out a custom helper.

The MVPD instructions require removing LG Developer Mode **after successful
rooting and before rebooting**. This changes the access/recovery arrangement;
do not remove the working app as a preliminary test. No exploit, memory write,
Homebrew installation, downgrade or firmware modification was performed during
this investigation. The temporary scheduled startup test and remote-controlled
power cycles are documented above. Root remains a candidate, not a verified capability.

### Implementation order

1. **Extend the successful LF6300 probe.** Developer access, installation, a
   scheduled service restart, app relaunch, short MP3 completion and embedded
   HDMI picture/sound are confirmed. Record the remote type. Check Arabic fonts,
   D-pad, Back, pointer if present, saved preferences, full adhan, one radio stream,
   media handoff and suspend/resume behavior.
2. **Deliver the useful partial port.** Add a dedicated TV entry/build using the
   current web screens and generated data, plus `webos/` metadata/package inputs
   and a small LG integration module. Include real app favourites, offline prayer
   calculations, foreground adhan, radio, settings, HDMI viewing and an in-app ambient view.
   Start with setup-time app inventory if that is sufficient; no always-on
   discovery service is required just to display a dock.
3. **Validate Home integration separately.** Do not install the inspected Razdva
   helper on the LF6300 or bypass its minimum-runtime check. Any adaptation for
   webOS 2 is additional work requiring evidence about available input devices,
   permissions and startup behavior. On newer supported TVs, a pinned helper
   could be evaluated, retaining recovery checks and attribution. Test physical Home, Back/Cancel/confirmed exit,
   held keys, volume, pointer, network loss, helper failure, standby and true cold
   boot. Emergency exit must remain reliable. Keep manual launch available when
   the helper is unsupported. An external watcher is an alternative only if the
   household already accepts an always-on device.
4. **Promote background adhan only after evidence.** The one-shot service experiment
   passed; implement the minimal scheduled prayer service with deduplication,
   rescheduling and cancellation. State exactly which power states and foreground
   apps were tested. If it fails, the partial port remains useful and its UI must
   say reminders require Afnon to be open.
5. **Package for Samsung later.** Reuse the web screens and prayer logic; add
   `config.xml`, TV key/lifecycle handling, app inventory/launch and signing.
   Avoid a broad cross-platform framework before these two integrations need one.

Keep this work in Afnon alongside the existing data pipeline. There is no need
to fork the entire CleanYourTV platform stack or rewrite in React/Flutter.
The new scope reaches the TV entry, URL helpers, screen actions, audio lifecycle,
preferences, packaging, generated assets, relevant tests and install documentation.
The Android accessibility/DreamService implementations remain platform-specific.

## Installation, distribution and acceptance

For development, use an offline-capable [packaged web app](https://webostv.developer.lge.com/develop/getting-started/web-app-types)
(`.ipk`), with fonts, design assets and prayer code local. LG's
[Developer Mode](https://webostv.developer.lge.com/develop/getting-started/developer-mode-app)
requires an account and a maintained session. When Developer Mode is disabled,
development-installed apps are removed. Razdva labels on-TV renewal beta and
unverified on unrooted TVs; do not assume maintenance-free installation.

A store version requires [LG Seller Lounge submission and QA](https://webostv.developer.lge.com/distribute/app-approval-process).
The ordinary prayer app and community Home interception have different approval
and compatibility assumptions. No store eligibility for the helper was established.
LG staff reiterated in [March 2026](https://forum.webostv.developer.lge.com/t/how-to-make-the-app-launch-automatically-after-power-on/28121)
that a public power-on app launch API is unavailable.

Samsung development uses a signed `.wgt`, SDB and Developer Mode. Its
[certificate process](https://developer.samsung.com/smarttv/develop/getting-started/setting-up-sdk/creating-certificates.html)
requires author/distributor certificates and the test TV's DUID. Preserve signing
materials privately; confirm install persistence on the chosen TV rather than
equating a successful developer install with consumer distribution.

Acceptance for the LG port should cover:

- Prayer fixtures around midnight, Friday, Ramadan, DST and +/-30-minute offsets;
  persisted once-only adhan behavior across restart and settings changes.
- All screens in Arabic and English, D-pad edges, pointer/OK, Back, long press,
  focus after returning from another app and restoration after interrupted startup.
- Network loss, unavailable app/input, invalid saved preferences and stream failure.
- Radio across internal navigation; adhan interruption and restoration of our own
  radio state; hidden/resumed audio without conflicts or unexpected replay.
- Standby versus cold boot, Developer Mode maintenance, removal and helper escape.
- Display protection, including LG's [OLED screensaver policy](https://webostv.developer.lge.com/develop/guides/screensaver)
  if support later expands to OLED models; the LF6300 target is LED LCD.
  A dark page is not panel power-off, and an in-app ambient screen is not a system
  screensaver replacement. Preserve panel protection.

Use the existing web tests plus small checks for new scheduler/platform behavior.
LG's [simulator](https://webostv.developer.lge.com/develop/tools/simulator-introduction)
cannot establish native-helper or real media compatibility; physical-TV results
are the release gate for those claims.

Skipped: full Android feature parity, full adhan/stream tests, long-term endurance,
root/firmware changes, the on-TV online update check and a security audit of community
helpers. Background relaunch, audible short MP3 playback, embedded HDMI with a
banner, recorded-input restoration and delayed once-per-boot startup have passed
their initial tests. Main risks remain the legacy engine, media handoff, Developer
Mode maintenance and Home integration. Android feature parity is not established.

## First installable LG port — 10 November 2026

The research has now produced [Afnon webOS 0.1.0](../platforms/webos/README.md), a separate
`tv.afnon.webos` app and service. It reuses the existing cities, methods, prayer
library, translations, fonts and design tokens. The build transpiles the installed
adhan-js bundle and checks all shipped JavaScript against ES5 syntax.

- Opens the current native input full screen, falling back to the saved input.
- Keeps the input video element loaded through prayer-bar, Home, Inputs and Settings
  navigation; OK/arrows/Back operate the foreground Afnon UI.
- Offers Arabic/English, city, calculation method, Asr school and time-zone settings.
- Provides a persistent, switchable once-per-boot startup activity without the probe's
  temporary expiry. It records state under the proven private `/media/internal` path.
- Records only the latest local UI status and startup result; no remote telemetry.

The first physical check passed: the viewer confirmed remote navigation and HDMI
picture/sound after returning from Home and Settings. The same app instance remained
running across navigation, with HDMI 1 recorded and no error in the service report.
The production startup activity was enabled for the next power-cycle check.

Local checks passed for all existing cities/methods in a simulated legacy runtime,
midnight/tomorrow-Fajr behavior, input validation, failed writes, startup deduplication
and disable. Browser checks passed for Arabic/English, settings persistence and keeping
the video element alive. Desktop screenshots do not prove HDMI-plane composition;
the viewer's hardware observation supplies that evidence.

The first port does not yet provide adhan audio, radio, Hijri dates, per-prayer offsets
or native streaming-app embedding. Tuner/AV/component mappings remain unverified on
this build. Developer Mode session expiry and long-term reliability remain open risks.

### Power-cycle result and version 0.1.1

The viewer confirmed automatic startup and normal HDMI picture/sound after reboot.
Service records showed a new kernel boot, exactly one launch, a new UI instance and
no app error. OK initially appeared unresponsive, then worked when the Magic Remote
pointer disappeared. The viewer isolated this to pointer mode, and code inspection
found that the input view handled keyboard OK but ignored a background mouse click.
A browser regression reproduced the missing menu before the click handler fix.

The updated package retains `tv.afnon.webos` for state/activity continuity, while its
visible name is now **Islamic TV Launcher**. It adds the pointer click path, an Android-style
compact top prayer strip, the pointed mihrab/upper prayer row/Home tile layout and a
compatibility font for Arabic prayer names. Version 0.1.1 was installed; browser and
legacy/service tests pass. The viewer confirmed that pointer click opens the top bar,
Home works, and both the Arabic prayer name and Quranic text are rendered correctly.

The resource snapshot showed about 404 MiB available RAM and zero swap use. No LG
apps were removed; memory exhaustion was not established, and debloating does not
address the identified missing click handler. Web Inspector allowed an app DOM query,
but its screenshot method did not produce a usable image on this TV.
