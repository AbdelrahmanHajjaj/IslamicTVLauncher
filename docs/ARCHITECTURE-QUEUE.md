# Architecture migration queue

Updated: 2026-11-11 (Africa/Cairo). Owner: project owner. Execution: autonomous,
sequential, on `feature/platform-core`. Starting commit: `74a4287`.

## Resume contract

This ledger is the authoritative migration scope, decisions, queue and evidence.
Read it first; inspect only the source, applicable instructions and documentation
needed for the first actionable item. Update it before changing focus or yielding.
Work on one queue item at a time. Commit verified, reviewable increments. Do not
reconstruct scope from old chat, historical contribution plans or speculative work.
Repository safety, licensing, platform instructions and actual source still apply.
Record a material new user decision here before implementing it.

## Approved outcome

One household product, with independently buildable platform projects consuming a
portable domain core and canonical resources. Preserve current Android and old-LG
functionality and intentional platform differences, stored preferences, installed
identities, Arabic/English, RTL/LTR, accessibility and exact religious text.

- Shared core: models, validation and prayer-day/reminder product rules. Explicit
  inputs and results; no UI, filesystem, network, device APIs or background timers.
- Shared data/design: cities, methods, bilingual text, station/voice catalogs,
  existing tokens/patterns and distributable assets. Platform outputs are generated.
- Android: existing Java Views and native integrations; existing installer/recovery
  remains functional. Do not replace native UI as part of core extraction.
- webOS: existing web UI and LG service/integrations, with webOS 2.2 / WebKit 538.2 /
  Node 0.10.25 compatibility. Support newer LG through this project when validated.
- Eventual layout: `shared/{core,data,design}`, `platforms/{android-tv,webos}`,
  existing installer, site and tools. Move paths after dependency boundaries work.
- Use Kotlin Multiplatform JVM + ES5, selected by Q02's actual-host proof. The approved rewrite permits a
  narrowly scoped Kotlin core despite the inherited Java-only guidance. Adoption
  depends on measured legacy-host compatibility, size and integration evidence.
- Reuse existing adhan-java/adhan-js adapters initially. Do not rewrite astronomy.
- Keep this repository/history initially. Independent project ownership and
  distribution are intended; preserve original and third-party license notices.

## Explicitly deferred

Companion Android/iOS apps, pairing, control protocols/transports, cloud/accounts,
remote management and update delivery are outside iteration one. Pairing may remain
unsupported on some TVs. A later companion configures an already installed TV app
on the same LAN without accounts/cloud. No Apple hardware/build access exists yet.
Samsung implementation and additional platform scaffolding wait until this
foundation works. Sony Android/Google TV uses the Android project; other Sony
models need identification. No unrelated UI redesign, telemetry, ads, new feeds,
speculative plugin framework, firmware changes or root experiments.

Publishing, merging, deleting/recreating a remote repository and GitHub detachment
are separate operations; this execution does not perform them. Keep private device
state, credentials, logs, captures, binaries and restricted audio under ignored
local state. No permanent always-on development PC is a product requirement.

## Validation and completion

Old LG is available now; use configured local device tooling and distinguish
service/DOM results from human picture/sound/remote confirmation. Audible and power
tests require the platform's watcher workflow. Android TV hardware returns around
2026-11-25; automated/JVM/emulator checks may proceed now. Modern LG, Samsung and
Sony hardware are unavailable. Never label these targets hardware-validated.

Iteration one is complete only after Android/webOS consume the shared foundation,
affected regression/build/lint/generated-output checks pass, upgrades preserve
settings, and the required existing-TV hardware checks are recorded. Missing
hardware remains an open acceptance gate, not a fabricated pass. Continue
independent software work while awaiting hardware. Retain the Android APK budget
below 25,000,000 bytes; record performance measurements before claiming improvement.

## Ordered queue

| ID | State | Work and acceptance |
| --- | --- | --- |
| Q00 | Done | Create branch, persistent goal and this ledger; route future migration work here. |
| Q01 | Done | Baseline: dependency/caller map, current behavior differences, executable checks/tool/media availability and performance evidence; record results without private device details. |
| Q02 | Done | Portable-core proof: shared offset/next-prayer behavior on JVM and ES5 app/service hosts; meaningful common fixtures, native-Java integration, legacy API checks, size/startup/memory evidence and actual LG probe where available; select technology with recorded rationale. |
| Q03 | Done | Canonical resource extraction: move source ownership out of Android; migrate all generators/consumers and fixtures together; verify catalog/text/token equivalence and deterministic regeneration. |
| Q04 | Done | Core domain migration: extract settings and prayer-day rules in complete tested slices; cover date rollover, tomorrow Fajr, DST, offsets, Hijri range, unavailable calculations and deliberate platform differences. |
| Q05 | Done | Reminder policy migration: preserve enabled/default states, expiry, deduplication, cancellation, overlap and platform lifecycle; keep OS scheduling and playback in shells. |
| Q06 | Active | Separate platform projects/builds: migrate paths only after boundaries work; update Gradle, npm, installer, tools, generated outputs, tests and CI; add no empty future-platform projects. |
| Q07 | Queued | Ownership/documentation: update actual upstream download/support references safely, architecture/contribution/agent guidance and relevant changelog; retain attribution/notices; do not publish or detach. |
| Q08 | Queued | Full automated review and measured optimization: affected Python/Java, Android build/lint/size, preview test/build/tokens and LG ES5/service/browser checks; resolve introduced regressions and review final diff. |
| Q09 | Hardware gate | Upgrade and device acceptance on old LG and Android: preserve settings/input/startup, verify both languages/focus, prayer/media/remote/lifecycle; record exact evidence and unverified paths. Android awaits hardware. |

## Current cursor

Next: Q06.3a, move the LG shell to `platforms/webos`. Finish Q06.2's commit if
resuming between operations. Inspect every production/build/test/doc/CI path
consumer before moving; verify absolute source/destination remain in the workspace,
preserve unrelated/ignored work and keep package/service IDs and aligned versions.
Update root-relative build reads, dependency/test/fixture paths, npm commands and
agent/contributor links together. LG now consumes shared core/data/design, site
tooling and ignored `state/media/`; it must not regain an Android source/SDK tie.
Run the affected build/ES5/service/browser and consumer checks before committing.
Do not move Android simultaneously: Q06.3b will place it in `platforms/android-tv`
with its own build entry plus working aggregate `:app`/installer/tools/generators/
tests/CI. Inspect the smallest standalone Android settings before making that
slice. No empty future projects, UI changes, installed-ID changes or new media.
Q04/Q05 software extraction is complete; clocks, calendar/astronomical engines,
persistence, OS scheduling and playback remain adapters with existing keys/IDs.
LG was unreachable during Q04.2; actual-host integration and native ICU accuracy,
TV upgrade/picture/sound/remote/lifecycle acceptance remain Q09 gates. A temporary
browser host probe must use the current `tv.afnon:core` namespace (older ignored
helpers used the former parent-name namespace) and restore a polyfilled Math.imul.
Private logs/helpers remain under ignored `state/platform-core/`; do not publish
them. Q02 commit: `83b0c72`. No release, remote detach, merge or TV firmware action.

## Evidence and decisions

- 2026-11-11: owner approved the architecture plan, durable queue, sequential full
  autonomy, feature branch and persistent goal. Companion/pairing explicitly
  deferred; unavailable pairing is acceptable. No unanswered start decision.
- Branch created from clean `main` at `74a4287`; no unrelated working-tree changes.
- Persistent goal created for this ledger's iteration-one migration. No token or
  time budget was requested. Human/hardware acceptance is part of completion.

### Q01 baseline (2026-11-11)

- Node 24.13.1, JDK 17.0.10 and Python 3.14.6 available. SDK 34 is installed; this
  session needs `ANDROID_HOME` pointing at the existing SDK. Existing Gradle cache
  and Python temporary directories require sandbox-approved execution. Initial
  permission/SDK failures were environmental; corrected runs below passed.
- Launcher APK: 20,246,215 bytes. Lint baseline: launcher 0 errors / 54 warnings;
  installer 0 errors / 14 warnings. This is the comparison baseline for this branch.
- PASS: 36 Python/Java regression tests; Android launcher size/build/lint and
  installer build/lint; preview tests, 18-page build and tokens check; LG ES5/core/
  service regressions and Arabic/English browser suite. Raw logs are ignored under
  `state/platform-core/baseline-*.log`. Browser Luna/decoder behavior is mocked.
- All 14 adhan and 12 announcement clips and site/LG npm dependencies are locally
  available; media remains ignored. No source changes resulted from generation.
- Dependencies: Android `PrayerDay` combines astronomical adapters, Android ICU
  calendar and domain selection. Its consumers include Home, All apps, Today,
  screensaver, call screen, prayer dock/watch, reminders, settings and announcements.
  LG `core.js` is consumed by app/service and tests; it also owns LG input/app rules.
  Preview has separate prayer/layout rules. Android `Prefs`, `Text`, `PrayerMethod`,
  `Radio`, `Adhans`, themes/fonts/icons feed site extraction and LG packaging.
- Preserve the intentional day-model difference: Android/preview replace the
  displayed Fajr slot after Isha, while LG keeps today's row and a separate
  tomorrow-Fajr value. New domain selection must not mutate its input times.
- No current physical-TV performance measurement was made. The configured LG CLI
  is not on this session's PATH; discover existing private device configuration
  before any hardware probe. No device was changed.
- Initial candidate was cached Kotlin 2.2.10 on Gradle 8.9 / AGP 8.5.2. Q02's
  integration evidence determined the final stable toolchain below. Source:
  https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html.

### Q02 accepted foundation (2026-11-11)

- Additive `shared/core` candidate: offset validation and nonmutating current/next
  prayer selection. Production Android/LG callers have not switched to it.
- PASS: 4 common tests on JVM and JavaScript, plus a native-Java caller test.
  PASS: ES5 parsing and 31 assertions in both browser and CommonJS VM hosts with
  modern APIs removed. Runtime JS plus its pruned Kotlin stdlib: 16,794 bytes;
  JVM core jar: 3,309 bytes. Kotlin JS test tooling uses npm and the installed Node;
  retain strict repository policy rather than adding Node/Yarn download repositories.
- Existing paired LG profile recovered through local CLI config. Isolated files
  were uploaded, checked and removed; installed source/preferences/schedules were
  not changed. Inspector forwarding was closed after each bounded evaluation.
- PASS on actual LG Node 0.10.25: 31 assertions; one-run load 32 ms, checks 20 ms;
  RSS 9,867,264 -> 10,240,000 bytes. PASS on actual WebKit 538.2: 31 assertions in
  CommonJS and real browser-UMD modes; storage/focus unchanged and temporary globals
  restored. Browser-UMD load/check: 7/18 ms (warm proof). These are tiny proof
  measurements, not app performance claims. Private JSON evidence:
  `state/platform-core/lg-core-proof.json`, `lg-webkit-proof.json` and
  `lg-webkit-browser-proof.json`.
- Selected Kotlin 2.2.10. Required stable tool correction: AGP 8.10.1 + Gradle
  8.11.1 (official distribution SHA256 pinned; wrapper regenerated with the wrapper
  task). The previous AGP lint could not read metadata; no suppression or
  experimental lint override remains. Java 17 and app SDK levels are unchanged.
  Sources: Android Kotlin-support table and
  https://developer.android.com/build/releases/agp-8-10-0-release-notes.
- PASS: clean linked-core APK/lint, installer build/lint, then clean normal APK/
  lint and complete `:core:check`. Reject hidden metadata diagnostics even if a
  Gradle task exits zero. Clean normal APK: 20,066,700 bytes, no core/Kotlin runtime
  descriptors, runtime dependency graph remains adhan-java only. Clean trial APK:
  20,903,595 bytes with core/runtime descriptors; incremental cost 836,895 bytes,
  under 25,000,000. Trial injection is ignored tooling, not a source dependency.
  Use clean paired measurements; mixed incremental trial builds retained stale
  dex content and produced misleading sizes. Final logs: `foundation-final.log`
  and `android-core-final.log`; artifact inspections: `*-apk-inspection.json`.
- New lint reports 0 errors and 55/15 warnings, versus the old-engine 54/14.
  The extra recommendations concern Gradle 8.14.6 availability and installer
  compileSdk 35 availability. Q08 must resolve/assess these explicit tool findings;
  do not claim warning-count equivalence or lower the final quality gate silently.
- Core CI runs common JVM/JS, Java interop and ES5 checks. App CI watches shared
  sources and Gradle properties. Keep generated artifacts/media/private helpers
  ignored and retain the generated Kotlin JS npm test lockfile.
- On later production adoption, package the Kotlin runtime's Apache-2.0 notices
  in the actual Android/LG artifacts and update third-party documentation. Android
  hardware integration/performance and complete LG UI/service acceptance remain
  Q09 gates; this proof is not a user-facing platform migration.

### Q03.1 canonical cities and regions (2026-11-11)

- `shared/data/{cities,regions}.json` exactly preserves the previous 137 cities,
  43 countries, six regions, ordering, identifiers, bilingual labels, coordinates,
  methods and Hanafi flags. No new city or timetable claim. Old/new comparison
  snapshots are ignored under `state/platform-core/{cities,regions}-before.json`.
- `tools/catalogs.mjs` validates catalogs and generates Android `CityCatalog.java`;
  `Prefs.City`, `Prefs.CITIES`, `Prefs.REGIONS`, fallback and persisted keys stay
  compatible. Preview/LG read canonical JSON directly; obsolete ignored copies
  were removed so stale imports cannot silently work. Generator check is in site CI.
- PASS: generator input rejection and complete Java field/order round-trip;
  exact pre-migration JSON comparison; actual compiled `Prefs`/`CityCatalog` JVM
  initialization and every field/region versus canonical JSON (private helper
  `state/platform-core/check-city-runtime.py`).
- PASS: 36 Python/Java regressions; Android/installer build/lint/size; preview
  tests and 18-page build, data and token checks; LG ES5/service and Arabic/English
  browser regressions. Raw `cities-*.log` evidence is ignored. No TV deployment:
  LG data values and renderer behavior are unchanged; browser media/Luna are mocked.
- Developer guidance now points to canonical cities/regions and regeneration.
  Remaining catalog extraction is still Q03, not a completed platform migration.
- Verified city slice committed as `761dace`; working tree is clean at this
  checkpoint except subsequent ledger notes. Continue Q03.2 without redoing Q01/Q02.

### Q03.2 canonical methods (2026-11-11)

- `shared/data/methods.json` owns all 19 ordered IDs, bilingual names, nine custom
  presets and Turkey's `[0,-7,5,4,7,0]` corrections. Android enum is generated from
  its native adapter template; native/preview labels use metadata. Preview/LG read
  canonical JSON; old generated method copies and Java scraping were removed.
- Original built-in factories/tags stay in adapters. JS Turkey keeps its factory
  and canonical overrides; Java custom parameters remain equivalent. No astronomy
  rewrite or saved-method change. Corrections remain independent per calculation.
- PASS: input validation, 19 frozen JS parameter sets (including factory identity),
  ID order, bilingual labels/fallback, 76 native minute-rounded prayer-day outputs
  before/after, and 76 exact LG daily outputs versus the pre-migration snapshot.
  Public regression baseline: `tests/fixtures/method-parameters.json` (adhan-js
  4.4.6). Private snapshot helpers/results are under `state/platform-core/`.
- PASS: 36 native/Python regressions, Android/installer build/lint/size, preview
  tests/build/data/token checks, LG ES5/service and bilingual browser suite. Raw
  `methods-*.log` evidence is ignored. No TV deployment or audible test was needed.
- Context7 lacked adhan-js coverage; installed 4.4.6 source and primary Batoul Apps
  CalculationMethod/PrayerTimes sources were checked before changing adapters.

### Q03.3 canonical radio (2026-11-11)

- `shared/data/stations.json` owns all 16 entries, retaining exact IDs/order, URLs,
  bilingual names/locations and broadcast/reciter flags. Android's generated
  `RadioCatalog` feeds the unchanged `Radio.Station`/selection/playback interface.
  Preview/LG read JSON directly; both old Java-scraping passes were deleted and
  the obsolete ignored preview JSON was removed.
- PASS: catalog input rejection (identities/labels/flags/URLs), full generated Java
  round-trip and exact canonical/packaged-LG equality against the old snapshot.
  All preview tests and 18-page build, data/token checks, Android size/build/lint,
  LG ES5/service and bilingual browser regressions pass. Evidence: ignored
  `state/platform-core/radio-*.log` and `stations-before.json`.
- No playback, TV deployment or codec/TLS/hardware claim. Existing saved station
  keys and Cairo fallback are unchanged. Installer/core logic was unaffected;
  their suites were not repeated for this catalog-only increment.

### Q03.4 canonical adhan voices (2026-11-11)

- `shared/data/voices.json` owns the 14 ordered IDs/bilingual names and original
  download filenames. Generated `VoiceCatalog` preserves the `Adhans.VOICES` API;
  preview/LG consume canonical metadata. The two Java-scraping paths and obsolete
  ignored preview JSON were removed. Playback/default preference keys are untouched.
- The Bash/curl fetcher reads generated `tools/adhan-sources.tsv`, retaining its
  existing prerequisites and exact URLs without needing Node at download time.
  Rights status and ignored audio policy are unchanged.
- PASS: malformed metadata rejection, generated Java/TSV fidelity, exact labels/
  ordering/source filenames against old snapshots, packaged LG metadata and all
  14 MP3 hashes. Bash syntax and all 14 mocked downloads passed; a second run skipped
  existing files. Initial Bash sandbox signal-pipe denial was environmental; the
  approved isolated run passed without network requests.
- PASS: preview tests/18-page build/data/tokens, Android size/build/lint and LG
  ES5/service/bilingual browser suites. Private evidence: ignored `voices-*.log`,
  `voices-before.json`, `fetch-adhans-before.sh` and `voice-fetch-check/`. No TV
  deployment, audible check or recording download. Unaffected installer/core suites
  were not repeated in this increment.

### Q03.5a canonical plain bilingual text (2026-11-11)

- `shared/data/strings.json` owns 174 plain string/list templates. Android's
  `Text.java` is generated from `Text.java.template`; remaining formatting logic
  stays in the template. Preview/LG read canonical JSON directly; Java scraping
  and the obsolete ignored preview JSON were removed. Validation covers language
  types, list lengths, parameters, digit flags and adapter markers.
- Existing difference preserved explicitly: the web extractor compacted repeated
  whitespace in `saved` and `roomKeys`; Android kept it. Canonical text preserves
  native spacing and each web adapter compacts literals before filling parameters.
  No visible labels, religious content, preference keys or UI layouts were changed.
- PASS: frozen pre-extraction JVM hashes, covering 1,392 outputs across 174 methods,
  Arabic/English, both digit modes, sample/null parameters and independent arrays.
  PASS: 1,392 preview outputs and all packaged LG entries against the old snapshot.
  Frozen public hashes live in `tests/fixtures/text-output-hashes.json`; private
  `Text-before.java`, `strings-before.json` and extraction proof remain ignored.
- PASS: all 37 Python/Java regressions, catalog input rejection/generated checks,
  preview tests/18-page build/tokens, Android size/build/lint (APK 20,248,190 bytes),
  LG ES5/service and bilingual browser suite. Logs: ignored `strings-*.log`.
  Unaffected installer/shared-core suites were not repeated. No TV deployment,
  sound/picture check or performance-improvement claim.

### Q03.5b canonical formatter copy (2026-11-11)

- `shared/data/format-text.json` owns 102 locale-specific literals: significant
  prefix/suffix spaces, labels, directions and grammatical forms. Language-only
  forms (such as Arabic ordinals) retain their actual scope. Compile-time markers
  generate native constants directly; formatting/selection logic stays native.
  Preview helpers and matching LG copy read the same catalog. Different LG copy
  remains in its shell; no UI wording or supported controls were changed.
- Captured unchanged-source native/preview edge-case hashes before extraction.
  PASS: both native text baselines, preview formatter hashes, and 2,128 preview
  settings row sets versus the original adapter. Coverage includes plural minute
  thresholds, null/empty values, extreme signed numbers, radio states, method
  angles, both languages/digit styles and compass-sector boundaries.
- PASS: 38 Python/Java tests, catalog validation/complete native marker coverage,
  preview tests/build/data/tokens, Android size/build/lint and LG ES5/service/
  bilingual browser checks. Evidence: ignored `format-*.log`, `Text-format-before.java`
  and `format-*-before.*` / `format-*-baseline.ts`. Frozen public hashes live in
  `tests/fixtures/{text-format,site-text-format}-hashes.json`.
- No TV deployment or media test; unaffected installer/core suites were not repeated.

### Q03.5c canonical sacred UI text (2026-11-11)

- `shared/data/sacred-text.json` owns the exact existing An-Nisa 4:103 three-line
  verse and adhan call. Generated `SacredText` constants feed all three native
  callers. Preview/LG insert escaped text; the footer joins the same verse with
  spaces. Text, diacritics, line layout, Arabic direction and font styles remain
  unchanged in either UI language. No recording or religious-content addition.
- PASS: every pre-migration renderer matched before extraction; frozen UTF-8 hashes,
  generated native constant round-trip and malformed-text rejection. PASS: all 18
  built preview pages, including 10 arches/two call screens and every footer,
  preserve exact text/direction. The private HTML check handles Astro's scoped
  attributes; its initial bare-`br` matcher was corrected, not the renderer.
- PASS: preview tests/build/data/tokens, Android size/build/lint, LG ES5/service.
  The extraction commit `6f84c1c` prematurely recorded a browser pass: its new
  `innerText` assertion failed when an existing layout hid the verse. Corrective
  exact-DOM checks now pass independently of visibility, and the full bilingual
  browser suite passed on rerun before the correction commit. APK: 20,248,232 bytes.
  Logs/helpers: ignored `sacred-*.log`, `sacred-before.json` and
  `check-sacred-built.cjs`. Public baseline: `tests/fixtures/sacred-text-hashes.json`.
- Astro's official Context7 documentation confirmed escaped expressions and
  `Fragment` mapping (docs.astro.build/en/reference/astro-syntax/). No new runtime
  dependency. No TV deployment or human picture/sound check; unaffected native
  Python/installer/core suites were not repeated for this constant-only slice.

### Q03.6a shared fonts and notices (2026-11-11)

- Seven unchanged TTFs now live in `shared/design/resources/font/`; the three
  existing full OFL notices moved from LG into `shared/design/assets/licenses/`.
  Android adds shared resource/asset directories to its main source set, keeping
  every `R.font` name with no extra task or Node prerequisite. Preview/LG source
  fonts directly from the shared tree. LG still packages only its three regular
  fonts. Font/CSS selections, weights, shaping and UI geometry are unchanged.
- All actual artifacts now include the three notices: Android `assets/licenses`,
  LG `app/licenses`, preview `licenses`. Notices remain byte-identical; the new
  generated preview copies are ignored. Attribution/source paths were updated.
- PASS: clean Android size/build/lint (20,073,354-byte APK), exact pre/post APK
  font names/hashes, all shared/preview/LG font hashes, and every artifact's three
  notice hashes. Evidence: ignored `fonts-before.json`, `fonts-artifacts.json`
  and `fonts-*.log`. The lower clean APK size is not an optimization claim.
- PASS: preview tests/18-page build/data/tokens and LG ES5/service/bilingual browser
  regressions. No new behavior tests were added for byte-preserving file moves.
  Unaffected Python/installer/core suites and physical-TV checks were not repeated.
- Context7 official Gradle/Android source-set documentation checked before build
  changes. Direct resource directories avoided an unnecessary generated-copy task.

### Q03.6b shared theme presets (2026-11-11)

- `shared/design/themes.json` owns the exact 21 ordered IDs: 20 built-ins and
  Android's final custom slot. Generated `ThemeCatalog` feeds the existing
  `Appearance.PRESETS` API. Preview/LG filter `custom` as before; no new platform
  feature or selection behavior. The 19 preset JSON files moved unchanged into
  `shared/design/assets/themes/`; Android's shared asset source set retains the
  exact `assets/themes/<id>.json` paths. Mihrab still uses design-token defaults.
- LG no longer scrapes `Appearance.java`; preview imports canonical files/order.
  Custom import validation/storage, colors, surfaces, lighting, backgrounds,
  rotation logic and bilingual labels stay unchanged in the adapters.
- PASS: catalog input rejection/generated-registry fidelity; exact 21 IDs/order,
  every preset byte, all 19 actual clean-APK theme assets and 20 LG CSS outputs
  versus pre-migration snapshots. Evidence: ignored `themes-before.json` and
  `themes-*.log`. Clean APK: 20,073,462 bytes.
- PASS: 38 native/Python tests (including every preset's existing contrast/bounds
  checks), preview tests/18-page build/data/tokens, clean Android size/build/lint,
  LG ES5/service regressions. LG browser checks were not repeated: client code,
  ID list and every packaged stylesheet are identical; Q03.6a's browser run passed.
  Unaffected installer/core suites and physical-TV checks were not repeated.

### Q03.6c shared icon geometry (2026-11-11)

- `shared/design/icons.json` owns the 26 existing preview/LG SVG fragments;
  `prayer-icons.json` owns six ordered prayer paths, 24-unit dimensions and the
  1.7-unit stroke. Android's six existing vector resources are generated from it;
  preview labels read the same geometry. Java/`.astro` geometry scraping and the
  obsolete ignored preview JSON were removed. Native resource changes are only
  generated-file comments; every path/presentation attribute is unchanged.
- Native `Icon` canvas code remains in its adapter: its arcs, fill, RTL handling
  and some geometry differ intentionally from the web SVGs. No device-side SVG
  parser or universal drawing framework was introduced. Native `PrayerIcon` sizing,
  tint/baseline/focus and accessibility code is unchanged.
- PASS: curated SVG shape/attribute allowlist, rejection of active/external markup,
  prayer order/dimension/path validation, complete native XML attribute parity,
  and exact original six-vector/26-fragment/packaged-LG data comparison. Evidence:
  ignored `icons-before.json` and `icons-*.log`.
- PASS: 38 Python/Java regressions, generated-output checks, preview tests/18-page
  build/data/tokens, Android size/build/lint (20,073,928 bytes), LG ES5/service checks.
  LG browser tests were not repeated: its client code and complete icon payload
  are unchanged. No physical-TV check or performance-improvement claim; unaffected
  installer/core suites were not repeated.

### Q03.6d final design metadata and Q03 audit (2026-11-11)

- `shared/design/appearance.json` owns the ordered procedural backgrounds,
  SVG-pattern IDs, surface IDs and eight theme-v1 color slots. `ThemeSpec` uses
  generated `ThemeCatalog` arrays; preview uses the same background/color IDs.
  LG retains its existing alphabetic pattern order and separate plain choice.
  Native contrast/import validation and platform drawing remain in their adapters.
- PASS: malformed/duplicate/default/slot metadata rejection; complete generated
  arrays; exact old/new metadata, preview pattern data, LG order/CSS and all eight
  regenerated Android PNG mask hashes. Pattern dimensions/size were checked again
  after generation finished. No SVG, token, palette or rendered mask bytes changed.
- PASS: 38 Python/Java regressions, preview tests/18-page build/data/tokens, Android
  size/build/lint (incremental APK 20,255,657 bytes), LG ES5/service checks. Evidence:
  ignored `appearance-ids-before.json` and `appearance-ids-*.log`. No new client
  behavior or physical-TV test; unaffected browser/installer/core suites were not
  repeated. No performance-improvement claim from differing build modes.
- Q03 audit: common catalogs, text and distributable design sources are outside
  Android. The preview extractor no longer reads Java, native XML or `.astro`
  source; it validates shared inputs and projects/copies shared resources. LG no
  longer scrapes native/preview UI source. Its remaining Android file reads are
  the ignored, restricted local adhan/announcement clips; Q06 will separate those
  build paths without changing rights or publishing media. Astronomical factories,
  native formatting/contrast/canvas code and platform-only copy stay in adapters.
- Existing token/SVG sources remain in `afnon-mihrab/`; generated Java/XML/CSS,
  vector metadata and regenerated mask fidelity were verified. Q03 is complete
  for the approved shared resources; production core adoption is still required.

### Q04.1 Android production core adoption (2026-11-11)

- Android now depends on `:core`. `PrayerDay` calls the real shared offset
  validator and current/next selector; astronomy, date/time-zone/ICU handling,
  native Fajr-row replacement, Suhoor, formatting and persisted settings remain
  in the adapter. No UI, key/ID, timer, scheduler or media change.
- Native regression compilation/runtime now includes the actual core JVM jar
  and pinned Kotlin stdlib, with only Android context/settings/ICU stubbed. Added
  exact prayer/sunrise boundaries, before/after-Isha display and Suhoor assertions
  and extreme-offset rejection. PASS: all 38 Python/Java regressions and complete
  core JVM/JS/Java-interop/ES5 checks. ICU accuracy itself is not proven by its JVM stub.
- PASS: 9,576 exact original/core-backed adapter outputs across 19 methods,
  three time zones, seasonal/DST dates, offsets and every prayer boundary.
  Private evidence: `PrayerDay-before-core.java`, `NativeCoreParity.java`,
  `check-native-core-parity.py` and `android-core-parity.json` under ignored state.
- Six unchanged runtime license texts were copied from Kotlin's pinned v2.2.10
  upstream license directory, including its identified stdlib third-party texts.
  Source attribution also records transitive JetBrains annotations 13.0 (Apache 2
  verified from the resolved artifact POM). Android packages the seven source/text
  files from `shared/core/assets/licenses/kotlin/`. No compiler/test dependencies
  are shipped. Root MIT/upstream notices are retained; Q07's broader audit remains.
- PASS: clean linked APK/build/lint and final notice-only rebuild; actual DEX class
  definitions include core, Kotlin runtime and annotations; all notice payloads
  match source bytes. Final APK: 20,931,664 bytes, below 25,000,000. Current lint:
  0 errors / 54 warnings; Q08 still must assess warning differences/tool findings.
  Logs/artifact proof: ignored `android-core-{adoption,final,dependencies,python}.log`,
  `android-core-artifact.json`, `kotlin-notice-sources.json`.
- Build/contributor documentation and Unreleased notes now describe the actual
  Java-UI/Kotlin-core boundary. No physical Android TV check (hardware pending);
  unaffected preview/LG/installer suites were not repeated for this native slice.

### Q04.2 LG and preview production core adoption (2026-11-11)

- LG app/service now package the compiler's unchanged ES5 UMD files and call the
  shared offset validator/current-next selector. The preview consumes the same
  output as a private local npm package, with compiler-generated TypeScript
  declarations. No custom runtime wrapper, platform ID/key/default change, media
  change or new product network dependency. OS scheduling remains in the shells.
- JS input checks reject wrong array lengths, holes, nonintegers and values outside
  Int32 before conversion; core enforces the six [-30,30] offsets. Added app/service
  trust-boundary and exact-prayer/sunrise assertions, nonmutation and preview Fajr
  display tests. PASS: relaxing the overflow guard in memory fails the real suite.
  A new UK summer-Isha test initially assumed all six times were on one calendar
  day; corrected fixed-boundary assertions use Cairo, while parity covers rollover.
- PASS: 26,024 original/new LG selections and 29,280 exact preview day outputs,
  covering 19 methods, three time zones, eight seasonal/DST/year-end dates, offset
  extremes, prayer boundaries, fixed-offset zones and all 137 cities. The 3,288
  catalog-day cases contained no unavailable times; invalid/unavailable and Hijri
  range handling still need Q04.3 coverage. Private snapshots/helper/results:
  `*-before-portable.*`, `check-js-core-parity.cjs`, `js-core-parity.{json,log}`.
- Build tools now require JDK 17 as well as Node 24 for LG/preview; site CI installs
  Java. Generating declarations adds only compiler-dev TypeScript 5.8.3 to Kotlin's
  npm lock, upgraded through the official lock task; the local package adds no npm
  registry runtime dependency. Pinned compiler/stdlib versions remain 2.2.10.
  Official Kotlin declaration docs and Context7 Vite linked-CommonJS/SSR docs checked.
- PASS: LG ES5/service regressions and bilingual browser suite; preview tests,
  18-page build, targeted TypeScript check, generated data/tokens; full core JVM/JS,
  Java interop and restricted-API/ES5 checks. Built preview Home/settings in both
  languages load without client runtime/asset errors. Logs: `js-core-*.log`.
- PASS: all four LG runtime JS copies and 21 packaged notice files across LG app,
  service and preview match source bytes. Runtime notices and build prerequisites
  documented. No performance-improvement claim; Android/installer suites were not
  repeated for JS packaging/callers, and Linux CI itself was not executed locally.
- Actual old-LG isolated adapter probe could not connect (timeout); no installed
  code, preferences, activities, picture or sound changed. Private failure/probe
  helpers are `lg-adapter-core-*` / `run-lg-adapter-proof.cjs`. Q02's earlier host
  proof does not replace final production/upgrade acceptance. Android hardware,
  actual LG picture/sound/remote/lifecycle and upgrade checks remain Q09 gates.

### Q04.3a shared settings rules (2026-11-11)

- Existing `PrayerRules` now owns tolerant saved-offset normalization and both
  adjustment limits. Double input preserves JS overflow/fraction information until
  validation/clamping. Android/preview normalize; LG retains strict validation.
  Native, LG and preview settings controls use the shared limits. Original saved
  keys, defaults, error text, writes and native legacy Hijri read policy remain.
- Tests characterized the old native behavior before changes, then ran with real
  `Prefs`, city registry, astronomy and shared core. Only storage/platform APIs,
  ICU and unrelated appearance calls are stubbed. Coverage: default offsets, all
  six stored keys, extreme integer reads/writes, rejection without mutation, Hijri
  clamping/legacy reads and preservation of unrelated settings. No disk migration.
- PASS: new core test failed for missing API, then full JVM/JS/Java interop and
  ES5/restricted-API suites passed; legacy proof now has 34 assertions. PASS: real
  preview regression fails a fractional-truncation mutant. Browser/CommonJS LG
  strict boundary tests and preview malformed/sparse/overflow/Hijri-bound tests pass.
- PASS: 38 Python/Java regressions; Android build/size/lint (21,113,433-byte
  incremental APK; 0 errors / 54 warnings); LG ES5/service/bilingual browser suite;
  preview tests/18-page build/targeted TypeScript and actual built-client check.
  Original/new 26,024 LG and 29,280 preview outputs still match; eight malformed
  normalization sets also match. All runtime/notices copies still byte-identical.
- Core JS grows from 16,794 to 18,290 bytes with numeric normalization helpers.
  This is measured payload size, not a performance improvement. Logs/helpers are
  ignored under `state/platform-core/settings-*`; native baseline there is
  `Prefs-before-normalize.java`. No physical TV or installer tests repeated;
  native ICU accuracy and TV upgrade/lifecycle acceptance remain hardware gates.

### Q04.3b calendar and unavailable-calculation coverage (2026-11-11)

- Calendar/astronomical engines remain platform adapters, as approved: native
  Calendar/ICU/adhan-java, preview Date/Intl/adhan-js and LG Date/adhan-js with its
  generated Umm al-Qura month table. No new calculation engine or fallback dates.
- PASS: app/browser-VM and CommonJS LG adapters check all 1,273 supported month
  starts/ends, 29/30-day lengths, +/-2-day range boundaries and last-row sentinel.
  Actual generated range is [1999-01-19, 2101-12-22); outside dates remain null.
  Fixed UTC -12/+5:45/+14 cases check wall-date and year/tomorrow-Fajr rollover.
- PASS: preview civil-day Hijri offsets versus independent UTC-noon Intl dates
  across DST/year boundaries; native/preview tomorrow Fajr and Suhoor across UK
  clock changes/year-end, plus native Cairo clock-change dates. Native ICU remains
  stubbed in JVM tests, so these do not assert native Hijri conversion accuracy.
- PASS: invalid dates and forced unavailable polar astronomy are rejected in JS;
  native unavailable astronomy returns no usable PrayerDay (its existing null-date
  failure is retained). Test-only latitude changes are restored in finally blocks;
  no unsupported location is added. Core rejects every nonfinite value in each
  prayer slot and tomorrow Fajr on JVM/JS rather than selecting a fabricated event.
- PASS: full core checks, all 38 Python/Java regressions, LG service/calendar and
  preview suites. Logs: ignored `calendar-{core,python,webos,site}.log`. Only tests
  changed after Q04.3a, so unchanged app/LG/preview builds, lint, size and browser
  checks were not repeated. No TV deployment or hardware accuracy claim. Q04's
  software extraction/coverage is complete; hardware acceptance remains Q09.

### Q05.1 reminder-policy characterization (2026-11-11)

- Native policy: global adhan + per-prayer bell gates calls in [0,120000) ms;
  announcements cover adhan-off/bell-off prayers when enabled. Sunrise is excluded
  even if its stored bell is true. Before-prayer reminders ignore those audio/bell
  switches and use positive rounded-up seconds <= lead*60; nonpositive lead is off.
  Native defaults remain audio/announcement/spoken reminder on, lead 15 minutes.
- Native watcher retains persisted per-kind `prayer:epoch` keys, claims before
  playback, defers for screensaver state and skips asleep unless radio plays.
  These device/lifecycle behaviors stay in the shell. Home uses its own card via
  the existing watcher callback; browser forced-demo screens are not a scheduler.
- LG policy: automatic defaults off; three calculated day sets, strictly future
  events, bells gate both reminder and call, call exists if adhan or announcement
  is on; handled IDs are excluded, ties keep first iteration order. Activity and
  pending-display windows are [0,90000] ms inclusive. Early callbacks do not claim;
  due/expired production callbacks claim before delivery, retaining last 24 IDs.
  Existing generation guards, serialized refresh/cancellation, persistence, audio
  mode and app-return behavior remain LG integration responsibilities.
- PASS before production edits: native real-Prefs/Reminders characterization of
  all 64 bell masks x adhan/announcement flags x six prayer/window boundaries;
  reminder rounding/off/ceiling-minute boundaries. PASS: 5,190 frozen next-event
  outputs each on LG app-VM and service adapters (source `6fb81fa`), automatic/
  lead/audio/bell/handled combinations. Public fixture and provenance live under
  `tests/fixtures/reminder-events.json` / README; runtime/native sources unchanged.
- PASS: exact -1/0/90000/90001 ms LG delivery/claim boundaries, stale generation,
  repeated display, clock rollback, pending-display expiry and manual-test edges,
  alongside existing asynchronous overlap/disable/failure-recovery/cancellation
  regressions. PASS: all 38 Python/Java regressions. Ignored evidence:
  `reminders-{native,webos,python}-before.log`. No new product behavior; unchanged
  build/browser suites and unavailable physical TVs were not retested.

### Q05.2 native reminder policy in the core (2026-11-11)

- `ReminderRules` owns due-call/announcement eligibility, before-prayer window and
  ceiling-minute rules. Numeric epochs, bell bits, flags and the existing exclusive
  call window are explicit inputs; no clock, timer, persistence, UI or playback.
  `Reminders.java` converts native Dates/preferences and delegates. `PrayerWatch`,
  deduplication keys, claim order, Home callbacks and sleep/screensaver/media paths
  are unchanged. Finite six-time validation is shared internally with PrayerRules.
- PASS: focused missing-API red test, then full common JVM/JS/Java-interop tests;
  all 38 Python/Java regressions using real Prefs/Reminders/core. New common coverage
  includes all bell/adhan/announcement combinations, exclusive endpoint, overlap
  order, Sunrise exclusion, disabled reminders, fractional-second rounding, extreme
  lead and rejection of unavailable slots/clocks/windows. No Screen stub remains
  in the native reminder test: rounding is now tested on the real core.
- PASS: ES5/restricted-API browser/CommonJS host proof now has 38 assertions;
  changing the native endpoint to inclusive in memory fails that actual proof.
  Runtime JS: 20,826 bytes. The public TypeScript output exposes only the supported
  primitive/typed-array APIs, not the internal validation helper.
- PASS: Android build/size/lint (21,113,583-byte incremental APK, 0 errors / 54
  warnings); rebuilt LG ES5/service/policy fixtures and preview tests/18-page build;
  actual built preview client in both languages. Runtime JS and 21 notice copies
  match sources. Logs: ignored `reminders-native-*.log`; original native adapter:
  `Reminders-before-core.java`. No measured performance-improvement claim.
- LG's reminder implementation is not migrated by this slice. Its renderer/service
  logic and preview UI are unchanged, so the long LG browser suite was not repeated;
  shared bundle load is covered by ES5 hosts and the preview's real browser check.
  Installer and unavailable TV checks were not repeated. Q05.3 and Q09 remain open.

### Q05.3 LG scheduled-event and delivery rules in the core (2026-11-11)

- Core selects the first future unhandled candidate from three adapter-calculated
  raw prayer days. Bell/audio/reminder gates, Sunrise exclusion, tie order and exact
  `kind:prayer:epoch` IDs match the original. LG converts the exported event to the
  same plain JSON; automatic-off and Date/astronomy remain in its adapter. Integer
  safe-epoch validation prevents fractional/rounded identity changes. Native
  reminder policy remains unchanged; no new scheduler or transport.
- Inclusive early/due/expired classification is pure core logic with explicit
  maximum age. The service still reads the clock before and after its persisted
  production claim. A newly characterized slow-write boundary stays silent if
  claiming crosses expiry; caching the earlier result fails that real regression.
  Generation checks, 24 handled IDs, cancellation/overlap, manual/display claims,
  audio modes and OS activity/playback integration retain their existing order.
- PASS: missing-API red test, complete JVM/JS/Java-interoperability checks, 5,190
  frozen event outputs on each LG host adapter, exact delivery/expiry/claim edges,
  clock rollback, duplicates, async overlap/disable/failure recovery and cancel.
  PASS: real ES5 tests fail incorrect delivery/event endpoints and a cached
  pre-claim clock. New malformed handled-list and raw clock type checks reject
  coercion/holes before entering typed core code; no saved-state reset or migration.
- PASS: 44 assertions in ES5 browser/CommonJS hosts, now also without Math.imul,
  Math.clz32 or modern array helpers. Exercise real 64-bit ID formatting, handled
  arrays, nullable events and inclusive windows, including the largest safe integer.
  JVM/JS tests verify negative epochs, ties, unavailable/unsafe/fractional input.
  Compiler 2.2.10 generates/validates the actual Array<Float64Array>/string-array/
  nullable-event TypeScript API. Context7 and official JsExport documentation checked
  (https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.js/-js-export/).
- Internal Long formatting retains decimal IDs on both hosts; Long never crosses
  the JS API and no BigInt feature/upgrade is enabled. Its standard runtime helpers
  increase JS to 38,547 bytes (from 20,826). The existing Apache/stdlib notices cover
  those helpers; do not call this a speed improvement or hardware performance pass.
- PASS: Android build/size/lint (21,113,583-byte incremental APK; 0 errors / 54
  warnings), all 38 Python/Java regressions, actual APK core/reminder/event DEX class
  definitions and seven runtime notice files; LG ES5/service/bilingual browser suite;
  preview tests/18-page build and built-client browser checks in both languages.
  All four runtime copies and 21 packaged LG/preview notice files match source.
  Logs/private helpers: ignored `reminders-lg-*`, `reminders-slow-claim-before.log`,
  `webos-{core,service}-before-reminders.js`, `check-lg-reminder-mutants.cjs`.
- Q05's software migration is complete. Actual old-LG deployment/sound/remote/power
  and Android device acceptance remain Q09. The old LG was not re-probed without
  evidence its earlier connection timeout had cleared; installer was unaffected.

### Q06.1 standalone core build boundary (2026-11-11)

- `shared/core` now owns its Gradle settings, strict Maven/plugin repositories,
  JVM/npm-mode properties and compiler npm lock. Android includes it as a local
  composite `tv.afnon:core` source dependency; existing JVM jar path and Java API
  stay stable. LG/preview build-JS invokes the standalone project using the same
  pinned repository wrapper. No Android plugin/configuration is needed by that path.
- The ES5 parser is now a core-owned dev dependency, pinned to the exact already
  installed Acorn 8.19.0/version/integrity used by LG. Core checks no longer require
  LG node_modules/source. CI installs that small local dev graph and runs the
  standalone build directly; no new product dependency or compiler/tool upgrade.
- Compiler lock moved with the project. Official upgrade task changed only its
  root name metadata (IslamicTVLauncher -> core); dependency versions/integrities
  are unchanged. Site lock only adds the local package's dev metadata. JS output
  filenames, private npm import and JVM jar remain stable. UMD namespace now uses
  stable `tv.afnon:core` group/name instead of the former parent-path name; LG loader,
  host test and core documentation migrated together. Installed TV IDs are unchanged.
- PASS: clean standalone core/Java interop/JVM/JS/ES5 checks; clean isolated copy
  containing only core sources/configuration/dev inputs, with Android SDK/NDK
  variables unset. Init assertion proves one project, no Android/LG source trees,
  no Android plugin or AGP implementation on its buildscript classpath. Uses the
  shared wrapper only as Gradle bootstrap. All JS/types and JVM jar entry bytes
  exactly match the normal standalone build. Proof is ignored under
  `isolated-core-q061`, `core-isolated-*` and `core-independent-artifacts.json`.
- PASS: Android + installer composite build/lint, all 38 Python/Java regressions,
  actual APK classes/notices, size 21,113,583 bytes (incremental); lint 0 errors,
  app 54 / installer 15 warnings. PASS: LG ES5/service/5,190-event-per-host fixtures
  and bilingual browser suite; preview tests/18-page build, TypeScript, data/tokens
  and built-client browser checks. LG/preview commands also ran with SDK variables
  unset. Four runtime and 21 notice copies match source. Logs: `core-standalone-*`,
  `core-composite-android.log`, `core-independent-*.log` under ignored state.
- Runtime JS is 38,502 bytes; the 45-byte difference is namespace text, not an
  algorithm/performance change. Gradle composite/module-name docs checked through
  Context7 and validated with the pinned 8.11.1 / Kotlin 2.2.10 tools. Linux CI and
  physical TVs were not run locally. Platform source/media paths have not moved
  yet; Q06.2/3 remain required. No source publishing, remote/release or device action.

### Q06.2 local media input ownership (2026-11-11)

- The existing 14 adhan and 12 announcement MP3s moved from ignored Android assets
  to ignored `state/media/{adhan,announce}`. Preflight verified all absolute paths
  inside the workspace, source hashes and absent destinations; native PowerShell
  moved each file and rechecked its hash. No download, recording or overwrite.
- Android adds only `state/media` as an asset source; LG copies its same allowlisted
  filenames from that input. Playback code, packed `adhan/` / `announce/` paths,
  catalog IDs, audio focus/decoder handling and religious/audio bytes are unchanged.
  Fetchers, install warnings, active build/contribution/agent/release instructions
  use the new input root. Empty Android `.gitkeep` was removed; old media locations
  remain ignored for safety, and the new inputs remain covered by `state/`.
- PASS: all 26 original source/APK/LG hashes before the move and new source/fresh
  APK/LG hashes afterward, with exact filename sets and ZIP_STORED Android entries
  for AssetFileDescriptor. Evidence: ignored `media-{before,after}.json` and
  `check-media-inputs.py`. No restricted recording is tracked/staged; the unrelated
  historical probe's already-tracked test tone was not changed.
- PASS: both actual fetchers skip existing clips with download/recording commands
  blocked; no network or speech call occurred. PASS: Bash and PowerShell syntax.
  Initial non-elevated Git Bash syntax check hit Windows signal-pipe access denial;
  the approved process-permission rerun passed. Source/runtime behavior was not at
  fault. Logs/helper: ignored `media-fetcher-check.log`, `check-media-fetchers.sh`.
- PASS: clean Android build/size/lint, actual core/event/runtime DEX and seven
  notices; APK 20,934,629 bytes, 0 errors / 54 warnings. PASS: LG rebuilt ES5/service/
  calendar/reminder regressions. Logs: ignored `media-{android,webos}.log`.
  Existing core/native logic, preview/installer and LG renderer were unchanged;
  their broader suites were not repeated after Q06.1. No hardware sound/picture
  check, performance-improvement claim, release or source/media publication.
