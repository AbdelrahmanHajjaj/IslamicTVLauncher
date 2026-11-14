# Architecture migration evidence

Essential verification reference, consolidated on 2026-11-13. Current scope,
status and next action belong only to [the queue](ARCHITECTURE-QUEUE.md).
Update relevant facts here; do not append execution transcripts. Full older
investigations are recoverable from the queue's Git history through `0708275`.
Raw logs, captures, backups, binaries and restricted recordings remain ignored.

## Implemented milestones

| Work | Retained result |
| --- | --- |
| Q00–Q02 foundation | Feature branch from `74a4287`; Kotlin 2.2.10 JVM/ES5 selected after native Java interop and actual old-LG Node/WebKit proof. |
| Q03 canonical resources | Cities, methods, stations, voices, bilingual/formatter/sacred text, fonts/notices, themes/icons/tokens have shared ownership and checked generation. |
| Q04–Q05 product rules | Both shells consume portable settings, prayer selection/comparison and reminder policy; astronomy, calendar, clocks, storage and scheduling remain adapters. |
| Q06 project boundaries | `shared/core`, `platforms/android-tv`, `platforms/webos`, installer and site build through their documented independent entries. |
| Q07 ownership | Downloads/support target this fork; original project/CleanYourTV and dependency notices retained. History retained; no remote detachment/publication. |
| Q08 review | Core/adapters/generators/CI reviewed; affected automated checks, standalone/aggregate builds, lint, size and notice/media continuity passed. |
| Q09/Q09.5 acceptance | Emulated Android upgrades/UI and real old-LG upgrades/UI/media/startup verified; physical Android TV remains the only required open gate. |

Implemented boundaries and deliberate calendar/reminder differences are documented
in [ARCHITECTURE.md](ARCHITECTURE.md). Semantic controls, capabilities and focus
rules are in [UI-CONTRACT.md](UI-CONTRACT.md), with generated [gaps](UI-GAPS.md).
Do not duplicate those specifications here.

## Current artifacts and compatibility

| Item | Verified result |
| --- | --- |
| Old LG | LF6300, webOS 2.2 / WebKit 538.2 / Node 0.10.25; actual installed package 0.2.17. |
| LG source | `f49a67d` (`fix(webos): keep HDMI picture behind adhan`). Later documentation commits do not change its payload. |
| Local IPK | 18,661,548 bytes; SHA-256 `7444077698b45ff8ec7b6a74b6708f7bcc14e31e8fe7f3637d8aa310b7add1a9`. |
| Payload | All 103 packed/installed files and app/service/control metadata match. IDs remain `tv.afnon.webos` / `tv.afnon.webos.service`. |
| Media/notices | All 14 adhan and 12 announcement recordings retained; 52 earlier media/font/notice files unchanged. Attribution intentionally adds the public CA; its certificate/source notice are included. |
| Shared JS | Core 58,760 + pruned Kotlin stdlib 27,730 = 86,490 bytes. Actual Node/WebKit each pass 88 common assertions; Node adapter passes 30. |
| Android | Latest verified APK 21,159,759 bytes, below 25,000,000; package remains `tv.afnon.home`. No Android source change in the later LG-only fixes. |

Initial Kotlin adoption was measured with paired clean builds and actual LG host
probes. It introduced a measured runtime/package cost; no general app startup,
memory or speed improvement is claimed. One unused 368,616-byte LG font was removed
from packaging. Early tiny-core timings are not current app-performance results.

## Automated verification retained

- Standalone core JVM/JS checks, Java interoperability and shipped ES5 parsing pass.
- Q08 full review passed 39 Python/Java/simulated-TV regressions, standalone and
  aggregate Android/installer builds/lint, signatures, packaged notices/media and
  preview/generated/client checks. Later changes used the affected regression sets.
- Native final Home build/lint and all 16 English/Arabic × 80–150% page cases pass,
  including five enlarged Home variants and real text-width/focus assertions.
  Separate-window/global UI checks passed in the preceding sizing slice.
- Android instrumentation raw result **-1** is the acceptance signal; shell success
  alone is insufficient. Earlier failed prototypes are not counted as passes.
- Preview domain/editor/geometry checks, 18-page build, full size/language matrix
  and final affected 80/150% bilingual smoke pass. Token/catalog freshness passes.
- LG final ES5/service, scheduling/overlap/expiry, volume ownership, online validation
  and full browser media/UI matrix pass. New HDMI regression is red before the fix
  and green afterward. Non-HDMI/spoken/radio retain decoder release; replay/end/
  error, preparation races/cancel, manual volume override, suspension/external
  return and normal-source restoration are covered.
- Browser media/Luna are mocked. Desktop captures do not prove TV picture/sound.

The final browser external-return assertion measures the completion boundary:
video-only startup during adhan is intentional; restarting HDMI after returning
to an external app remains forbidden and checked.

## Android runtime evidence and remaining gate

An owned API 37 x86_64 emulator verified actual ART/ICU 78.3, frozen calendar/core/
reminder fixtures and preference-preserving baseline-to-current replacements in
both languages. The upgrade check preserved 53 typed preferences, exact XML, UID,
provider interpretation and external Home/accessibility/screensaver selections.

Later native page/window tests covered both languages and all eight UI sizes.
Owned preferences, secure/display settings and known captures were independently
restored; emulator/AVD ownership was checked and the session closed. Existing user
AVD files remained unchanged. This is emulator evidence, not Android TV hardware
or vendor remote/input/power evidence. The required TV checklist is in the queue.

## Old-LG physical acceptance

| Area | Confirmed result |
| --- | --- |
| In-place upgrades | Close/install-over/launch, no uninstall/reset. Current 31 preferences, selected/current HDMI_1, full startup, boot, master volume/mute preserved. |
| Remote/input | Physical Home top-controls/app/All apps routes; Up from every first app column to Back, horizontal header routing and return focus accepted. |
| Arabic | Joined text, RTL, formerly clipped status values/sidebar and enlarged settings reachable/readable. |
| Sizing/Home | Inline 80–150%/10% draft/save/cancel; tested enlarged settings/Home, separated header/mihrab and larger prayer text. Owner accepted 32px Home art clearance. |
| Appearance preview | Compact/Hidden patterns stay complete after redraw; fixed old-WebKit SVG image paint clipping with a CSS background. |
| Startup | Owner power-cycled; launcher opened with HDMI automatically, preferences retained, exactly one boot launch; Browser did not trigger another startup launch. |
| Volume | 0.2.19 changes native gain to the absolute 0–100 scale. Owner confirms 30 during adhan, restoration to 50 and normal picture/sound; mute/manual ownership remains intact. |
| Online | Real verified HTTPS returns six Cairo/Egyptian advisory rows, readable table and correct Back focus. Calculations/reminders remain offline. |
| HDMI adhan | Watched video-only then quiet concurrent-audio probes kept picture moving, suppressed programme sound and restored normally. Installed 0.2.17 standard 12s test is owner-confirmed working. |
| Full duration | Owner confirms full-length adhan was previously tested. Accept that evidence; do not keep it as an open gate without a new regression. |

The initial 2s installed diagnostic ended before audibility was confirmed. The
owner's subsequent normal 12s test passed; no recording/startup workaround was
needed. Installed DOM showed video ready=4/no error, video-only source, transparent
outer overlay, hidden originating page and central mihrab. Final actual normal
source, visible page, zero audio/overlay and independent state comparison proved
restoration. The most recent instrumented check was bounded, not a newly observed
full-length run of every recording.

Current diagnostics/emulator are closed. No pending test or volume owner remains.
Other HDMI ports and newer LG/Samsung/Sony hardware are unverified; no global Home
replacement, arbitrary-app embedding or standby wake is claimed for old LG.

## Source fixes worth retaining

- `6545506`: universal inline sizing and responsive Home layout.
- `bb727a7`: LTR clipping viewport with RTL page fixes old-WebKit horizontal crop.
- `b6b49ba`: shared Home header/typography; native text-width regression caught
  English clipping and drove the canonical 10px cell inset.
- `7759803`: scaled LG Home mihrab safe margin, without shrinking text.
- `6cdca6f`: CSS pattern background prevents miniature SVG clipping on old WebKit.
- `3552619`: fixed-host public USERTrust RSA root resolves CERT_UNTRUSTED while
  keeping certificate/hostname/body validation; provenance is packaged.
- `f49a67d`: HDMI adhan uses the documented native video-only source; volume and
  lifecycle handlers remain unchanged.

`469885c` unifies numeric editor kinds/steps/draft bounds in core, consumed by
Android, LG and preview. All supported LG numeric rows edit inline; bearing keeps
its specialized card. JVM/JS, 93 legacy ES5 checks, all 16 LG size/language cases,
preview, Android build/lint and 40 regressions pass. Installed 0.2.20 matches 103
files; Node/WebKit pass 93 checks and Node passes 30 adapter checks. Real inline
save 25→30→25 restores all 31 preferences. Cleaned Al-Qatami/Chebae previews reach
natural EOF, native level 25 and normal HDMI/50 restoration; current Al-Menshawy
selections are preserved. Final state has no test/audio/volume owner and the
inspector is closed. Post-0.2.20 human UI/sound feedback is still pending.

The full adhan/third-party texts remain exact. Recording cleanup and inline
follow-up findings are in [AUDIO-AUDIT.md](AUDIO-AUDIT.md); Android TV hardware
remains the architecture acceptance gate.

## Evidence locations

- `state/platform-core/ui-size/`: native build/page/window checks, preview/browser
  matrices and LG 0.2.17 test/package/host logs. `lg-0217-browser-final.log` is the
  accepted complete matrix; earlier failure logs are not passes.
- `state/platform-core/lg-0217-candidate/`: exact local IPK and payload manifest.
- `state/platform-core/q095-0217-lg-*-service/`: before/after/final/after12 state
  comparisons. Always capture a fresh baseline for a later device action.
- `state/media-review/lg-0220-candidate/` and `state/platform-core/q095-0220-lg-*-service/`:
  latest exact package/hash manifest, preserving upgrade and final-state evidence.
- Earlier core/runtime/upgrade proofs remain under `state/platform-core/`; source
  and investigation chronology are recoverable from Git. Do not publish raw state.
