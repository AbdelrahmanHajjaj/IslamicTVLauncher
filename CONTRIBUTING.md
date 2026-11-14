# Contributing to Islamic TV Launcher · المساهمة في المشروع

Islamic TV Launcher serves households using a TV remote. We maintain its product
rules, platform shells and contribution policy in this repository. Open issues
and pull requests in Arabic or English.

<p dir="rtl">نرحب بالمشكلات وطلبات الدمج بالعربية أو الإنجليزية. حافظ على دقة مواقيت الصلاة والنصوص الدينية، واختبر التنقل بالريموت والعربية والإنجليزية، مع الاحتفاظ بإعدادات المستخدم عند التحديث.</p>

Start with the [architecture](docs/ARCHITECTURE.md), [UI/input contract](docs/UI-CONTRACT.md)
and [test requirements](TESTING.md). The [durable queue](docs/ARCHITECTURE-QUEUE.md)
owns the migration's approved scope and open hardware acceptance. Historical
upstream plans do not govern current work.

## Where changes belong

| Project/source | Responsibility |
| --- | --- |
| `shared/core/` | Portable Kotlin rules, semantic navigation and UI/numeric contracts; JVM and ES5, without platform APIs. |
| `shared/data/` | Stable catalog/control IDs, bilingual strings, methods, voices, stations and exact sacred text. |
| `shared/design/`, `afnon-mihrab/` | Canonical assets, themes, tokens, patterns and notices. |
| `platforms/android-tv/` | Java/Android Views shell, astronomy/calendar and Android storage, media, remote and Home integration. |
| `platforms/webos/` | Separate ES5 renderer, adapters and Activity Manager service for LG. |
| `site/` | Browser demonstration and generation tools; simulated device integration. |
| `installer/`, `tools/device.py` | Existing Android-phone/PC installation and recovery, separate from a future companion app. |

Put common product behavior in core and define controls in the canonical catalog
before binding them in shells. Core returns decisions from explicit inputs;
shells own rendering, persistence, clocks, transport and OS operations. Keep
platform gaps explicit in the generated [coverage report](docs/UI-GAPS.md).
Use Android's existing screens for parity where the contract leaves a detail open.
Sharing a token or control ID does not update another renderer automatically.

Read [Android integration notes](CLAUDE.md) for Android work and
[LG agent rules](platforms/webos/AGENTS.md) for LG work. Android remains Java Views;
LG's installed app/service must work on WebKit 538.2 and Node 0.10.25. Build tools
use JDK 17 and Node 24, plus Android SDK 34 for Android. See
[independent builds](docs/ARCHITECTURE.md#independent-builds) and [TESTING.md](TESTING.md)
for setup and exact checks. Run shared JS builds sequentially.

## Product and compatibility rules

- Keep the visible product name, installed package/service IDs, signing continuity
  and saved preferences. Upgrade in place; do not uninstall or clear data to make
  an update work. Preserve later user changes when restoring temporary test state.
- Edit canonical sources and regenerate Java/XML/CSS/catalog outputs through the
  owning tools. Do not hand-edit generated projections or duplicate shared rules.
- Ship Arabic and English together. Check RTL/LTR, joined Arabic, clipping and
  focus at supported sizes. Do not add letter spacing to Arabic; Quranic text
  uses Amiri. Reuse design tokens and existing controls.
- Follow semantic focus and numeric editor/step/bounds rules. Ordinary numeric
  settings edit inline with OK/save and Back/cancel; appearance keeps its Apply
  scope. Translate horizontal remote input for RTL once at the shell boundary.
- Keep prayer calculation and scheduling offline. Manual AlAdhan comparison is
  advisory and must not replace the schedule or saved adjustments. Runtime
  network use stays limited to requested radio/comparison; the installer's
  documented ADB/download flow is separate. No accounts, ads, analytics or feeds.
- Retain low-memory rendering: cached local artwork, no blur, decorative video
  backgrounds or idle animation loops. Android prayer checks share the existing
  service timer; LG uses its existing OS activities. Avoid new background services,
  polling, runtime dependencies or frameworks for incidental features.

Companion apps/pairing and additional TV shells are deferred. Sony Android/Google
TV uses the Android shell; Samsung/Tizen remains research. Do not describe a TV
brand as supported from a browser demo or another model's test.

## Catalogs, prayer rules and religious content

Cities live in `shared/data/cities.json`, with region membership in `regions.json`.
Keep IDs/order stable, use cited coordinates and identify the authority behind a
method default. Run `npm --prefix site run gen`, then the relevant data/consumer
checks. Cairo remains the fallback for old or unknown saved city IDs.

For a city/method or calculation change, include a dated six-time comparison with
a relevant ministry, awqaf or local timetable and link the source. Record timezone,
Asr school, method and offsets so differences can be reproduced. Report differences;
do not promise minute-exact agreement with an official timetable from astronomical
calculation alone. Test changed parameters, flooring and date/DST boundaries.

Preserve exact Quranic text, prayer names and complete adhan wording. Cite surah
and ayah for changed verses and use a verified text source. Discuss ambiguous
religious choices before changing defaults. Frozen sacred-text/calculation fixtures
must change only with an intentional, explained source or behavior change.

## Recordings and attribution

Only propose recordings with a clear source and redistribution permission. Local
recordings stay ignored under `state/media/`; metadata lives in
`shared/data/voices.json`. The current downloaded collection does not establish
redistribution rights. Do not commit or publish restricted recordings.

Reviewed content cuts live in `shared/data/adhan-edits.json`, guarded by exact
source SHA-256. `node tools/prepare-adhans.mjs` prepares them once using FFmpeg;
originals/stamps stay under `state/media-originals/`, outside packaged inputs.
Follow the media checks in [TESTING.md](TESTING.md) and preserve complete final
words. Keep curated findings in [AUDIO-AUDIT.md](docs/AUDIO-AUDIT.md) and raw review
clips/transcripts private.

Keep the original Afnon MIT copyright, CleanYourTV credit and third-party texts.
[Repository independence](docs/REPOSITORY-INDEPENDENCE.md) changes project
maintenance and GitHub affiliation, without erasing provenance or retained code.

## Pull requests and review

1. Work on a feature branch against `AbdelrahmanHajjaj/IslamicTVLauncher:main`.
   Keep one coherent change with its tests and docs; an approved architecture
   change may span modules. Unrelated cleanup belongs separately.
2. Follow [TESTING.md](TESTING.md), add focused regressions for changed rules/bugs
   and check affected consumers. State pending hardware checks explicitly; a
   passing mock or emulator does not close a physical-TV gate.
3. Fill the [PR template](.github/pull_request_template.md) with the problem,
   resulting behavior, affected modules, commands/results and remaining limits.
   Label screenshots browser/emulator/TV; record human LG observations where TV
   capture APIs cannot show the result. Update owning docs and the Changelog for
   user-visible behavior.
4. Review the diff for credentials, device/account identifiers, private content,
   keys, `state/`, local configuration, build outputs and restricted audio. Keep
   raw logs/backups/captures private; share sanitized evidence only.
5. Resolve relevant failures and new touched-code warnings. Draft review can
   proceed while acceptance is pending; do not claim hardware/release readiness.
   Publishing, merging, releasing and permanent repository administration require
   the corresponding owner authorization. A local change or PR is not a release.

Report a TV issue with platform, model, OS/firmware, app version and reproducible
steps. Distinguish Android remote/Home integration from LG's window/input behavior.
Do not include device addresses, pairing keys or private programme content.

Code contributions use the repository's [MIT licence](LICENSE). Retain third-party
notices and identify any dependency or asset rights change in the PR.
