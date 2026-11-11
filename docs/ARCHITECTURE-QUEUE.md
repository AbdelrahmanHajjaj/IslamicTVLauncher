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
| Q06 | Done | Separate platform projects/builds: migrate paths only after boundaries work; update Gradle, npm, installer, tools, generated outputs, tests and CI; add no empty future-platform projects. |
| Q07 | Done | Ownership/documentation: update actual upstream download/support references safely, architecture/contribution/agent guidance and relevant changelog; retain attribution/notices; do not publish or detach. |
| Q08 | Done | Full automated review and measured optimization: affected Python/Java, Android build/lint/size, preview test/build/tokens and LG ES5/service/browser checks; resolve introduced regressions and review final diff. |
| Q09 | Hardware gate | Upgrade and device acceptance on old LG and Android: preserve settings/input/startup, verify both languages/focus, prayer/media/remote/lifecycle; record exact evidence and unverified paths. Android awaits hardware. |

## Current cursor

Next: Q09.4.4, watched LG closed-app background delivery/return and actual new-boot
startup acceptance. Q09.4.3's cropped Arabic status text is fixed in installed
0.2.6; owner confirms the installed values are readable. First restore original
English through the settings control and verify all 30 original preference values.
Then run one bounded 30-second Activity Manager test over native Browser, checking
cold launch, real sound and return; cancel the one-shot on completion/abandonment.
Power checks follow sequentially with the owner watching. Preserve selected input,
startup choice, IDs and user data; never uninstall/clear data to make a test pass.

Real LG evidence so far: 0.2.4 to 0.2.5 to 0.2.6 in-place upgrades preserve all
30 current preferences, HDMI 1 and startup state. Owner confirms picture/sound,
physical OK/Home/arrows/Settings/Back focus, joined Arabic/RTL, short adhan and
normal automatic audio/HDMI/settings restoration. Actual Node 0.10.25/core and
WebKit 538.2 checks pass; all 100 installed 0.2.6 files match the reviewed package.
Original English is temporarily Arabic from this test only; current backups are
ignored q094-lg-*-service/. The second owned inspector is open. No background
one-shot has been armed yet; record its pending/cleanup state before a handoff.

Owner explicitly authorized watched in-place/audio/remote/power tests, lifting
the earlier read-only limit. Keep tests sequential and distinguish human picture/
sound evidence from DOM/service results. Private device details/artifacts stay
ignored. The previous blocked audit is historical; the user has set the goal active.

Android physical upgrade/older ICU/remote/lifecycle acceptance still awaits hardware
around 2026-11-25. Q09.2 ART/ICU and Q09.3 data-preserving emulator checks passed;
the owned emulator is closed and the user's AVD configuration remained unchanged.
The API 37 phone image does not close Android TV gates. Newer LG, Samsung and Sony
hardware remain unavailable. Keep the full objective incomplete until required
physical checks pass; add no speculative work or repeated passing suites while
waiting. No release, merge, remote detach, firmware or root action.

Q00–Q08 software work is complete. Preview retains three low build-only findings
from one upstream-unpatched deepmerge chain, limited to repository-owned tokens and
documented beside use. Core and LG audits are zero; do not claim zero preview audit.
Keep immutable upstream notices byte-identical, including BoringSSL whitespace.

Q06 is complete: `shared/core`, `platforms/android-tv` and `platforms/webos` build
independently with canonical shared inputs. Root `:app` and installer tasks remain
supported. LG currently emits 100 files: the unused font in the former 97-file
cache was removed, and Q07 added four retained MIT notice copies. All retained
runtime bytes match. Android retains all 95 former APK entry bytes plus four new
notices (99 entries). Do not add new UIs, transports or future shells.

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

### Q06.3a LG project directory (2026-11-11)

- Moved the complete LG project to `platforms/webos` after verifying absolute
  source/destination inside the workspace and no existing destination. Git rename
  preserves tracked history and ignored dependencies/build output. Package/service
  IDs, aligned 0.2.5 metadata, renderer/service/core and all UI/media code are unchanged.
- Builder resolves shared roots at the new depth and reads its own TV-app snapshot,
  compatibility source and CSS from its directory. Updated test/dependency/fixture
  paths, private browser-capture root, npm/package commands, active agent/contributor/
  release/PR-template links and current links in historical research. Historical
  experiment commands/content remain historical; no new support claim.
- PASS: moved LG build/ES5/service/calendar/reminder regressions and bilingual
  browser suite. All 97 emitted app/service payload files are byte-identical to
  pre-move hashes, including IDs, data, fonts, CSS, scripts, notices and recordings.
  PASS: actual media/runtime/notices checks plus shared catalog/tokens checks.
  Evidence: ignored `lg-tree-before-move.json`, `check-lg-tree.cjs`,
  `lg-move-{tests,browser}.log`. Updated active private proof paths only; recordings,
  captures, local dependencies/builds and diagnostic helpers remain ignored.
- No native/core/preview behavior changed; their unrelated full suites were not
  repeated. No physical deployment/picture/sound/remote test. LG still uses preview
  extraction/Adhan dependency at this checkpoint; Q06.3a2 explicitly removes that
  remaining build coupling before claiming independently buildable shells.

### Q06.3a2 independent LG build and shared validation (2026-11-11)

- LG now declares Adhan 4.4.6 directly, with the exact version/integrity already
  consumed from the preview. No calculation/library upgrade or new product network
  behavior. Shared validation moved out of preview extraction into
  `tools/validate-shared.mjs`, reusing all existing catalog/text/design guards plus
  method validation. LG invokes it directly; preview extraction calls the same
  function. LG no longer reads/generates preview files or needs its dependencies.
- PASS: new shared-entry regression failed for missing entry, then validates a
  source-only fixture without platform/preview trees and rejects active SVG markup.
  Actual isolated LG build/test uses only LG, shared core/data/design, common tools,
  wrapper/bootstrap and local media. No Android, installer, site, docs/screenshots
  or SDK configuration is present; SDK/NDK variables were unset. Source assertion
  and successful build prove the dependency boundary. Private fixture/evidence:
  `isolated-lg-q063a2`, `lg-isolated-{npm,tests}.log`.
- Isolation exposed one stale `app/fonts/amiri_bold.ttf` (368,616 bytes) in the
  previous 97-file output. No runtime/stylesheet references it; the builder has
  always declared only three regular fonts. The initial exact-tree assertion
  correctly failed for that missing stale file. Kept the original snapshot and
  explicitly excluded only that verified obsolete file from current comparison.
  All 96 retained payload files are byte-identical, including all religious text,
  three used fonts, media, styles, scripts, data, identities and notices.
- Builder now replaces only generated app/service outputs after input validation
  and core compilation, with resolved-path guards before recursive cleanup. A
  stale-output regression failed first, then passed on normal/isolated builds.
  Actual invalid shared input leaves the complete last-good 96-file payload intact;
  dropping its SVG validation fails the real regression. No renderer/service/prefs
  behavior changed. Reduction is an uncompressed unused-file payload measurement,
  not a speed, memory, IPK-size or physical-picture improvement claim.
- PASS: LG ES5/service/calendar/5,190-event-per-host fixtures, normal/isolated clean
  output and input-preservation checks, bilingual browser suite; preview tests,
  shared malformed-input check, 18-page build and built-client browser checks.
  Runtime and 21 notice copies match source. Logs: `lg-independent-*`,
  `lg-clean-output-red.log`, `lg-shared-validation-red.log`; active private gate:
  `check-lg-isolated-gate.cjs`. Native/core/installer logic was unchanged and its
  unrelated suites were not repeated. Actual TV acceptance remains Q09.
- First offline npm attempts found uncached metadata/build-tool tarballs. Standard
  installs fetched only the existing pinned/locked artifacts; Adhan integrity
  matched the preview and no other LG lock versions changed. Existing glob tooling
  deprecation remains for Q08's dependency review. No publishing or device action.

### Q06.3b independent Android project (2026-11-11)

- Moved the native shell from `app` to `platforms/android-tv` after snapshotting
  source/APK entries and verifying absolute source/destination boundaries. Preserved
  ignored build state; no old compatibility source tree remains. Its own settings
  select AGP 8.10.1, local core and only Google/Maven Central. Standalone project name
  `app` preserves `app-debug.apk`; source-set paths now resolve from the project.
  Root settings map the existing `:app` task path to the moved directory, keeping
  installer and install/recovery commands functional. No installed identity,
  version, signing configuration, settings key or native integration changed.
- Updated all active generators/templates, test consumers, APK defaults, source
  links, build/contribution/release guidance and app CI paths/commands. Generated
  PrayerMethod's source-path comment through the actual generator. Historical
  changelog/validation paths and protective legacy ignore rules remain historical.
  CI now checks standalone size/lint plus aggregate launcher/installer build tasks;
  workflow was reviewed locally, not run on GitHub.
- PASS: clean standalone build/size/lint and root launcher/installer build/lint.
  APK remains 20,934,629 bytes; every one of 95 APK entry names and bytes matches
  the pre-move snapshot, including manifest/resources, DEX, 26 uncompressed clips,
  notices and signing metadata. All 82 native input files retain their bytes except
  the expected path-only comment in PrayerMethod template/generated Java. Actual
  core/runtime classes and all seven Kotlin notice files remain in the APK.
- PASS: fresh isolated build with only the native/shared inputs, wrapper and local
  media; no aggregate settings/build/properties, installer, preview, LG or local SDK
  config. Init assertions verify a single native project and only local core as an
  included build, with no JitPack repo. All 95 isolated APK entries match the normal
  build exactly. Initial private proof assertions assumed traversal order/only one
  Gradle build; corrected the proof for unordered source traversal/included core,
  with no product/source workaround.
- PASS: all 39 Python/Java/simulated-TV regressions, including default and selected
  APKs reaching the guarded CLI flow; reverting its default path fails the actual
  new regression. Native lint stays 0 errors / 54 warnings; installer stays 0 / 15.
  PASS: actual pattern/catalog/token regeneration, generated-source checks and
  preview regression suite. Pattern bytes remain identical. LG product sources
  were unchanged; its unrelated full suite/browser checks were not repeated.
- Evidence: ignored `native-before-move.json`, `native-move-*`, `native-isolated.log`,
  `isolated-native-q063b`, `check-native-{move,relocation,core-artifact}.py` and
  `check-media-inputs.py`. No physical/emulator deployment, TV picture/sound/remote
  or upgrade test, performance claim, publishing or remote operation.

### Q07.1 ownership and implemented architecture (2026-11-11)

- Actual installer release/tag/APK and cleanup-catalogue URLs now target this
  project's repository; bundled setup commands, local selection, signing mismatch
  protection and recovery behavior are unchanged. GitHub connector read verified
  the public project/default branch and only `installer-preview` prerelease as of
  this date; no stable TV APK exists here. No artifact downloaded or remote mutated.
- Preview source/build links target this project and its own Pages base/canonical
  URL; removed inherited search-verification metadata and advertised binary URL.
  Both languages link to build/install instructions while no stable TV artifact
  exists. This is deployment configuration, not a claim that Pages was deployed.
  Issue/question/private-security routes target this owner; private reporting's
  remote enablement was not changed or verified.
- Added `docs/ARCHITECTURE.md` for implemented module/dependency boundaries,
  independent/aggregate builds, preserved identities, policy differences and
  explicit companion/future-platform deferrals. The queue remains the only active
  scope/status source. Updated contribution/agent/release guidance and root README;
  marked older upstream proposal/authorization text historical. Original MIT and
  CleanYourTV license source bytes remain unchanged. No UI/ID/signing migration.
- PASS: actual Setup lookup/download/catalogue methods with JVM URL handlers,
  covering absent release, project tag lookup, same-owner catalogue, no upstream
  fallback and preservation of the last-good APK on 404. Helper is ignored
  `SetupSourceCheck.java`. PASS: installer build/lint (0 errors / 15 warnings),
  18-page preview build, bilingual Home/settings browser tests on the new base,
  canonical/CTA/repository metadata, removal of old-owner/verification/binary links
  and no failed JS/CSS/client runtime. Logs: ignored `ownership-*`.
- Audit follow-up Q07.2: full project/CleanYourTV texts were absent from the TV APK
  even though retained in source; fix distribution completeness through existing
  generated asset tooling. Q08 must also review inherited installer CI's automatic
  prerelease/branch publication against the explicit-release workflow. Neither
  inherited behavior was triggered. Physical phone/TV and published site routes
  were not tested. No release, PR, merge, remote setting or detachment operation.

### Q07.2 retained distribution notices (2026-11-11)

- Existing catalog generation now projects unchanged root `LICENSE` and
  `docs/CLEANYOURTV-LICENSE.txt` bytes into shared and installer assets; existing
  asset consumers carry them to native/LG/preview packages. LG service explicitly
  receives both texts too. Generated fidelity checks cover all four source copies;
  canonical original copyright/license files remain byte-identical to the base.
- Artifact inspection confirmed adhan-java and installer AARs lacked bundled full
  license texts. Added pinned upstream notices/source references to the appropriate
  APK asset sources: adhan-java 1.2.1; libadb 3.1.1 COPYING/all LICENSES; SPAKE2 2.2.1
  LGPL/native component notices; Conscrypt 2.5.3 LICENSE/NOTICE/Harmony/Netty; its
  BoringSSL revision read from actual AAR `org/conscrypt/conscrypt.properties`.
  Upstream tag and submodule refs were verified through GitHub reads; all 13 full
  texts match their upstream Git blob hashes exactly. Native source headers retain
  ISC/MIT/LGPL/public-domain attribution; installer SOURCES records exact source/
  rebuild refs and libadb's Apache option. No library/version or product licence
  changed; no additional runtime code, UI, protocol or network dependency.
- PASS: both Android builds/lint/TV size budget, actual complete packaged notice
  equality, native core/runtime DEX and seven Kotlin notices, all 26 unchanged
  source/APK/LG clips and ZIP_STORED Android audio. Native APK 20,937,377 bytes:
  all 95 former entry bytes are unchanged, with exactly four additional notice
  files (99 total). Installer 13,223,311 bytes and 16 asset notice files plus its
  existing AndroidX notice. Both APK signatures verify; native certificate equals
  the frozen Q06 isolated pre-notice APK. The initial private proof expected v1
  signature ZIP entries; corrected it after observing unchanged ZIP entries and
  verified the actual signatures/certificate using apksigner separately.
- PASS: LG ES5/service/cleanup regression suite; 100 current app/service files,
  with only four new MIT copies and the updated THIRD_PARTY media notice differing
  from the prior retained 96-file snapshot. All runtime/UI/media bytes match. PASS:
  18-page preview build, generated catalog/source-notice equality and copies in
  public/dist. Existing broader native/browser suites were not repeated for this
  asset-only slice; Q08 runs the final integrated checks.
- Evidence: ignored `notice-sources-q072.json`, `check-distributed-notices.py`,
  `notice-artifacts-q072.json`, `notices-*`, and updated active LG artifact checker.
  Full third-party license text adds source lines intentionally; implementation
  changes are the existing generator's four lines, fidelity checks and three LG
  copy lines. No dependency upgrade, release, device deployment or remote mutation.

### Q08.1a installer CI publication boundary (2026-11-11)

- Inherited installer CI published/overwrote a prerelease on same-repo PRs and
  force-pushed emulator captures to a screenshots branch. Removed those remote
  mutation steps and per-job contents-write grants. Existing build/emulator jobs,
  their dependency and APK artifact remain; screenshots now use the existing
  upload-artifact action pattern. CI retains read-only repository permission.
  Release guide/changelog describe the resulting reviewable artifact workflow.
- PASS: YAML-parsed assertions verify read-only access, PR/manual triggers, both
  artifact contracts, emulator command/dependency and absence of release/push/token
  mutation steps. Evidence: ignored `check-installer-ci.cjs`. GitHub/Linux/KVM
  workflow was not run; no remote release or branch was modified/deleted.
- Live npm audits were collected read-only without lock changes. LG and core's own
  package have zero findings; preview build tools have six low/moderate findings;
  Kotlin-generated JS tests have five including high serialize-javascript. Narrow
  supported remediation and full verification continue in Q08.2; no audit gate was
  suppressed. `npm explain glob` correctly reports none in LG (its warning was from
  the already-used preview tool installation); preview has multiple glob branches.

### Q08.1b source notice and controlled lint review (2026-11-11)

- Restored the truncated opening SUPERCOP attribution line in the new SPAKE2 native
  notice and removed its extra EOF blank. Rebuilt installer; packaged notice/source
  byte equality passes. All 13 complete upstream license blobs remain untouched.
  Their pinned BoringSSL LICENSE intentionally retains seven upstream trailing-
  whitespace lines; exclude only that immutable vendored text when checking the
  final diff's whitespace, and keep verifying its source Git hash instead.
- PASS: original installer at branch base with original AGP 8.5.2/Gradle 8.9 yields
  14 warnings; the same untouched original source with current approved AGP 8.10.1/
  Gradle 8.11.1 yields the exact same 15 diagnostics as this branch. Only the newer
  compileSdk 34-to-35 suggestion differs. No source warning regression or lint
  suppression; SDK targeting is unchanged. Evidence: ignored `installer-lint-*`,
  `installer-lint-base-q08`, `check-installer-lint.py`, `notices-header-fix.log`.
- Q08.2 compatibility finding: Mocha 12 changes Base reporter to a class, while
  both pinned and latest Kotlin helper reporters call Base as a function. Avoid
  an unsupported test-runner upgrade/reporter rewrite. Kotlin 2.2.10's official
  BaseNpmExtension exposes supported npm overrides; inspect current advisory ranges
  and use narrow test-tool leaf patches with real runner/negative-case checks.
  Core compiler/runtime and product dependencies remain pinned. Deepmerge has no
  upstream patched version; assess the existing trusted-token build-only use and
  document the bounded finding rather than fabricating an audit pass.

### Q08.2 supported build/test dependency patches (2026-11-11)

- Kotlin compiler/runtime 2.2.10, Mocha 11.7.1 and Kotlin reporter 2.1.0 stay pinned.
  Used the pinned plugin's official BaseNpmExtension overrides for Glob 14.0.2,
  minimatch 11.0.7, diff 8.0.3 and serialize-javascript 7.1.3; preview overrides only
  Glob to 14.0.2. No product dependency change. Official Context7/Kotlin source and
  Mocha migration docs informed compatibility; no unsupported reporter rewrite.
- Actual installed-version guards caught an initially cached unpatched tree, even
  after a task rerun. Declared the override map as rootPackageJson task input.
  Regenerated only bounded, verified `shared/core/build/js` npm outputs with npm;
  copied the generated lock byte-for-byte into the canonical Kotlin lock store.
  No lock entries were hand-edited. The old lock had to be regenerated, rather
  than trusting an install over its cached tree. Normal incremental build then
  regenerated configuration correctly. An initial wrong NpmPlugin class import
  was corrected to the actual NodeJsRootPlugin/NpmExtension API before acceptance.
- PASS: forced actual core check and fresh isolated core-only check, JDK/Node only
  with SDK variables unset and no shell/preview sources. Both run 12 JVM and 11 JS
  tests plus 44 ES5/browser/CommonJS assertions without modern runtime APIs. Fresh
  install consumes the exact patched canonical lock. Deliberately failing assertion
  exits 1 under both actual Mocha and Kotlin reporters; matching/serialization APIs
  work. Initial non-elevated child-process canary was sandbox-denied; the normal
  process-permission run passed without product changes.
- PASS: normal and fresh test trees audit at zero findings. Earlier audit caught
  Mocha's separately nested minimatch, leading to its explicit fourth override;
  no audit gate or package was hidden. Reference advisories:
  [Glob](https://github.com/advisories/GHSA-3g3m-2fjr-6pw4),
  [minimatch](https://github.com/advisories/GHSA-g3hv-75wg-m249),
  [diff](https://github.com/advisories/GHSA-73rr-hh4g-fpgx),
  [serialize-javascript](https://github.com/advisories/GHSA-m5mm-p8h7-m29v).
- PASS: runtime JS/types and every JVM jar entry exactly match frozen pre-patch
  bytes, including JS size 38,502. Native Java/XML and preview CSS token outputs
  match exactly. Preview tests, generated checks, 18-page build and bilingual
  Home/settings real-browser/client/link checks pass. No TV speed/memory claim.
- Preview audit drops from six low/moderate findings to three low findings from
  the same [unpatched deepmerge issue](https://github.com/advisories/GHSA-gcqx-w48m-w2h4)
  and its two parent packages. Existing Style Dictionary only handles repository-
  owned local tokens, not remote/user-provided token input; documented the bounded
  exposure and upgrade condition beside its use. No unsafe downgrade/custom parser
  or suppressed advisory. LG and core's own package audits were already zero.
- Evidence: ignored `toolpatch-*`, `runtime-before-toolpatch-q082.json`,
  `check-{patched-test-tools,toolpatch-runtime,toolpatch-isolated}.*`, and fresh
  `toolpatch-core-isolated-q082`. Final platform integration/code review remains
  Q08.3. No native UI, settings, runtime API or hardware/remote operation changed.

### Q08.3 final automated and source review (2026-11-11)

- Reviewed pure core/exported interfaces and native/LG/preview adapters, including
  finite/integer checks before typed conversion, six-prayer/sunrise rules, tomorrow
  Fajr display difference, stable IDs/tie order, separate due/delivery windows,
  persisted-claim clocks and shell-owned scheduling/storage/media. Reviewed shared
  validation/generation, source-path/build entry contracts, notices, ownership links
  and CI mutation boundaries. No additional introduced runtime defect found in this
  self-review. No independent/hardware review is implied.
- PASS: final clean standalone Android build/size/lint and root aggregate launcher/
  installer build/lint; 39 Python/Java/simulated-TV regressions; LG ES5/service/
  calendar/reminder/async/clean-output tests and bilingual real-browser UI checks.
  Current core normal/fresh tests and preview tests/generated/18-page/client checks
  passed in Q08.2 and were not needlessly repeated. Additional preview TypeScript
  5.8.3 no-emit check passes using the existing compiler-provided tool. Initial
  assumed site-local tsc path was absent; no dependency was added to run the check.
- PASS: final native APK signatures/certificate continuity, actual DEX core/runtime
  definitions, 13 upstream license Git hashes, every packaged source notice, all
  26 identical/uncompressed native/LG media clips, four runtime JS copies and 21
  Kotlin notice copies. LG's 100-file output matches the retained runtime snapshot
  with exactly the documented notice/font changes. Final native ZIP still retains
  all 95 old entry bytes plus four notice entries; final clean APK is 20,937,376
  bytes (one byte of archive-size variation from the prior build), below 25 MB.
  Installer is 13,223,311 bytes; lint is 0 errors / native 54, installer 15 warnings.
  Controlled original-source lint proves the extra installer warning is tool-only.
- Measured costs/changes: native APK is 691,161 bytes larger than Q01, within the
  4,062,624-byte budget headroom; no startup/memory gain is asserted. Production
  shared JS remains 38,502 bytes. Removed one 368,616-byte unused LG font from
  generated packaging. Patched core test-tool lock reduces versioned non-link
  node_modules entries from 97 to 62; this is development footprint only. Core and
  LG audits are zero; three existing low preview findings remain bounded as above.
- PASS: final diff whitespace apart from the exact pinned BoringSSL license;
  original project/CleanYourTV source licences unchanged; no new tracked state,
  addresses/keys, APK/IPK or restricted media. The sole tracked MP3 is the unchanged
  historical probe test tone. No empty future shell, new transport, account or UI
  framework. Clarified preview generation's existing native/installer output inputs.
- Evidence: ignored `final-{android-standalone,android-aggregate,lg-tests,lg-browser,
  python-java,typescript,native-cert}.log` and active artifact/lint/runtime/CI
  checkers. GitHub/Linux/KVM, phone/emulator and physical TVs were not run here.
  Old-LG actual integration, preference-preserving upgrade, picture/sound/remote/
  power and Android/ICU acceptance remain Q09. No deployment or publication occurred.

### Q09.1 reviewed LG candidate and bounded preflight (2026-11-11)

- Prepared ignored `q09-candidate/tv.afnon.webos_0.2.5_all.ipk` with CLI 3.2.6 and
  `--no-minify`: 18,629,980 bytes. Parsed its ar/gzip/tar payload without extraction;
  all 100 generated app/service files match source bytes exactly. Only additional
  entry is generated package metadata. IDs, version, ES5, original media and full
  notices are preserved. SHA/manifest/log/helper remain in ignored `q09-*` and
  `check-q09-ipk.py`. Updated the public package command to preserve checked bytes.
- CLI's old ares-device-info entry is a deprecated local stub. Confirmed installed
  help/current official docs and used ares-device --system-info instead. Its one
  bounded read-only query to the existing private alias reported connection timeout
  (2.5 seconds), not a validation pass. No TV data read/changed and no install/test
  activity occurred. Raw output remains private; do not repeat without changed state.
- Package tool warned about newer-TV requiredACG eligibility and target Node age.
  No unsupported manifest fields or modern-LG/store claim added to the old 2.2
  target. Compatibility requires target-specific validation, already an explicit
  boundary. Human watcher/TV availability was asked through the asynchronous input
  tool; audible/power/remote tests have not started.
- Independent opportunity: SDK inventory shows one API 37.0 x86_64 Play Store /
  ai_glasses_compatible image, one existing medium_phone AVD and usable WHPX.
  No avdmanager is installed. Preserve that user's AVD and use private new data/
  configuration for any emulator checks; no new image/tool download. Emulator
  evidence can address native core/ICU integration but cannot close physical TV
  upgrade, old-Android, remote, HDMI, sound or power acceptance.

### Q09.2 actual Android runtime and ICU evidence (2026-11-11)

- Started an isolated fresh API 37.0 x86_64 Play Store / ai_glasses_compatible
  emulator using the installed image, WHPX and private AVD/config/data homes.
  Headless, no audio/cameras/snapshots; boot took 78 seconds. Existing user AVD
  descriptor/config hashes stayed unchanged. No userdata copied or SDK downloaded.
  Private session metadata guards exact AVD name/serial before ADB operations.
- Reused the existing instrumentation runner with explicit `-e check prayer`;
  no-argument appearance testing remains unchanged. Moved the byte-identical
  PrayerDayCheck into the platform's test sources and kept root JVM tests calling
  that canonical source. Added test-only ART/core/reminder assertions and 14 frozen
  calendar fixtures independently agreed by existing preview Intl and LG adapters.
  Cases cover supported table endpoints, DST, civil offsets, leap/year boundaries,
  Ramadan and Friday. CI now compiles the standalone instrumentation APK too.
- PASS: existing native prayer regressions and all core/reminder/ICU assertions on
  API 37 / ICU 78.3.0.0; raw instrumentation result code -1. System timezone stayed
  unchanged; runner restores process timezone and does not modify preferences, open
  UI, schedule alerts or play media. Full 39 Python/Java regressions, standalone
  test APK build and native lint pass (0 errors / unchanged 54 warnings).
- Harness corrections before acceptance: ICU's default setter is not a public
  compile-SDK API, so use the process-local standard Java setter. AGP emits the
  configured runner, so delegate from it rather than adding a second manifest
  runner. Normal instrumentation output hides its result code when stream text is
  present; the private checker now requests raw output and verifies the code too.
  No frozen expectation or production behavior changed to fit a test result.
- Evidence: ignored q09-native-test-build.log, q09-java-tests.log and q09-emulator/
  session, native-runtime-result.log and native-runtime-info.json; private helpers
  retain identity guards. This is actual ART/ICU evidence on the stated emulator,
  not a TV, older ICU or remote/lifecycle acceptance. Physical Q09 gates stay open.

### Q09.3 preference-preserving emulated upgrades (2026-11-11)

- Prepared a new ignored baseline fixture at starting commit 74a4287: all 110
  original tracked Android/build inputs retain exact bytes; copied the same 26
  hash-checked personal media clips. Built its original Gradle 8.9 / AGP 8.5.2 /
  compile SDK 34 sources offline using cached tools. Earlier baseline/evidence
  fixtures remain untouched. Both baseline/current debug APK signatures verify
  with the same certificate; this does not prove a physical installation's key.
- PASS: two baseline-to-current `install -r` replacements on the identity-checked
  isolated API 37 emulator, for ar and en configurations. Each seeds 53 valid typed
  preferences through debug run-as with the process stopped, then verifies exact
  stored XML bytes, all string/int/boolean/long values and unchanged package UID.
  Covers location/method/madhab, all prayer offsets/bells, Hijri correction, voice/
  reminders, theme/layout/appearance deadline, radio, favourites and remote table.
  The existing info/theme/keys provider queries return identical interpreted
  results before/after replacement; subsequent reads do not modify preferences.
  Other getters remain covered by existing tests, not asserted as provider fields.
- PASS: external selected Home, enabled accessibility services and screensaver
  selection stay unchanged. No data clearing/uninstall, UI, service enablement,
  vendor setup, media playback or physical-device command. This validates package
  replacement/storage and the stated provider contract, not real-TV startup,
  input routing, UI focus, sound, remote interception or power behavior.
- Closed only the owned emulator after the checks. Verified ADB offline, owned
  launcher process exited and original user's AVD descriptor/config hashes still
  exact. Session status is closed. Helpers/build/results remain ignored under
  q09-upgrade-baseline*, q09-upgrade-check.log and q09-emulator/upgrade-result.json;
  private source/APK manifests retain hashes for reproduction.
- Current acceptance gate: old LG's last supported query timed out; watcher reply
  is still pending and Android hardware returns around 2026-11-25. Do not repeat
  unchanged unavailable-device queries. No remaining independent approved software
  item; first availability-wait checkpoint after completing emulator work. Goal
  remains incomplete. No release, merge, publication or repository detachment.

### Q09.4 availability audit, checkpoint 2 (2026-11-11 07:22 UTC)

- Previous goal turn made progress: committed real ART/ICU checks, verified two
  data-preserving emulator upgrades and closed the owned emulator. This turn
  confirms the committed cursor and clean feature branch; no watcher reply or
  changed device availability was supplied, and Android hardware's expected return
  remains in the future. No live process/job is awaiting observation. This is a
  hardware availability blocker, not a verified process wait.
- Removed one stale current-cursor paragraph that incorrectly said Q08 review was
  outstanding; Q08.3 already records its completed evidence. No runtime change or
  reason to rerun passing checks. Physical acceptance remains the only actionable
  scope once devices/watcher are available. Unchanged unreachable LG was not probed
  again. Goal stays active at this second availability-blocker checkpoint.

### Q09.4 blocked audit, checkpoint 3 (2026-11-11 07:23 UTC)

- Previous turn: documentation progress, correcting contradictory Q08 status;
  physical acceptance stayed blocked. Revalidated current committed queue and
  clean feature branch. No new watcher/device availability was supplied; Android's
  expected return remains in the future. No live job/process is awaiting a poll.
- The same genuine availability blocker has now recurred across three consecutive
  goal turns, including the turn that completed the remaining emulator evidence.
  All independent approved software work is finished; further meaningful progress
  requires a reachable TV/watcher or physical Android access. No passing-suite
  repetition, unavailable-device retry or speculative scope was added.
- Blocked audit satisfied: mark the persistent goal blocked, preserving the full
  objective and Q09 physical gates. Resume only after changed availability; first
  capture existing preferences/input/startup, then perform the in-place upgrade
  and actual picture/sound/remote/lifecycle checks. No completion claim.

### Owner steering: watcher deferred, read-only checks (2026-11-11)

- Owner answered the watcher request: "Later; keep checks read-only for now."
  Recorded the restriction in the current cursor. No installation, relaunch,
  settings/activity write, audible or power test is authorized during this limit.
  Read-only preparation/diagnostics remain permitted; no changed reachability was
  reported, so the existing timeout is not repeatedly probed.
- The physical acceptance gate and persistent goal remain blocked/incomplete.
  This reply records a constraint, not readiness to resume watched device tests.
  No runtime/source/artifact or TV state changed.

### Owner beside LG: resume read-only preflight (2026-11-11)

- Owner reports being next to LG now. Resume a bounded read-only connection/state
  check, preserving the earlier failure evidence. Clarify whether the earlier
  explicit read-only restriction is lifted for watched upgrade/media/remote/power
  acceptance; no dependent mutation before that answer. Do not repeat an unchanged
  failed connection without further device/network evidence.

### Watched LG acceptance authorized (2026-11-11)

- Bounded read-only system-info query succeeded using the existing private alias
  in 1.4 seconds. Prior timeout evidence retained; no TV mutation during preflight.
  Owner explicitly answered "Ready for the watched LG checks" to the in-place
  update / picture-sound / remote / power question. Earlier read-only limit lifted
  for these queued checks. Proceed sequentially with backups and no uninstall.
- Raw preflight remains ignored in q094-system-info-private.log and
  q094-preflight.json. Continue Q09.4; Android physical acceptance stays open.

### Q09.4.1 real LG upgrade and installed-runtime evidence (2026-11-11)

- Captured the installed 0.2.4 manifest, full service state/preferences, selected
  and current input, startup state and boot ID in ignored q094-lg-before-service/.
  Direct developer-shell access to the service's private settings file was denied;
  used its documented read-only status API instead. No root/permission workaround.
- Rechecked reviewed IPK against all 100 source payload files and frozen SHA, then
  closed only our app, installed 0.2.5 in place and relaunched. CLI confirmed each
  step. After-snapshot proves all 30 saved preference values and entire startup
  state unchanged, HDMI 1 retained, same boot, no UI error and successful prayer
  rescheduling. No uninstall/data clearing or firmware/vendor setting change.
- PASS: all 100 installed app/service files match current build SHA-256 bytes.
  Existing 30-assertion adapter proof passes directly against the installed core
  on actual Node 0.10.25, without writing TV files. Live WebKit 538.2 DOM confirms
  shared core loaded, offset clamp 100 to 30, sunrise current=-1/next=2; input is
  ext://hdmi:1, readyState=4, no video error or visible notice, English/LTR.
- Inspector uses this TV's legacy pagelist.json and /devtools/page socket, as
  confirmed by the installed CLI and TV's own inspector source. Initial modern
  /json inventory was unsupported; corrected the private checker. Closed one
  completed checker whose old socket close handshake stalled. At the subsequent
  human-response checkpoint, closed only the owned inspector forwarder after
  checking its listening port and exact process identity; TV app stays running.
- Human evidence: owner explicitly confirms picture/sound match before upgrade,
  with no visible error. Asked for physical OK/Home/arrows/Settings/Back sequence;
  answer pending. Alert/media, Arabic/RTL and actual power acceptance remain open.
  No one-shot test armed yet. Private q094-* contains backups, exact parity, host,
  installed payload, DOM and operation evidence; do not publish raw device state.
- Read-only DOM observation while waiting for the remote answer still showed the
  input view, English/LTR, no overlay and no notice. This is not physical remote
  confirmation. No temporary preference changes or audio/power test occurred.

### Q09.4.2 physical remote pass; bilingual/media checks (2026-11-11)

- Owner answers the physical OK/prayer bar/Home/arrows/Settings/Back check:
  "Remote and focus work normally." This is actual human remote evidence, separate
  from injected DOM events. Continue watched Arabic/RTL and bounded audio tests;
  restore the before-snapshot preferences afterward. Power acceptance remains open.

- Actual short adhan: WebKit audio progressed, readyState=4 without error, HDMI
  decoder released; after the bound it stopped, removed audio/overlay and restored
  the exact settings row/scroll and HDMI readyState=4. Service records stopped adhan
  and successful rescheduling; only temporary lang=ar differs from all 30 baseline
  preferences, startup/input unchanged. Owner confirms TV-speaker sound, automatic
  stop and normal HDMI/settings restoration.
- Owner confirms joined Arabic and readable RTL, but reports some settings text
  cropped. This is an open acceptance defect, not a complete bilingual pass.
  Asked for affected rows; inspect live geometry while waiting. No background
  one-shot or power test started. The second owned inspector remains open.

### Q09.4.3 cropped Arabic values fixed and installed (2026-11-11)

- Owner identified enabled/disabled and silent values. Actual WebKit spans had
  32-pixel boxes with 33-pixel text ranges extending beyond their top/start edge,
  while overflow:hidden clipped their paint. More line-height/padding improved
  measured boxes but the owner still saw cropping. Removing clipping made the
  values complete; owner confirmed this isolation, then the installed correction.
- Minimal fix removes nowrap/hidden/ellipsis from the shared value rule. Long
  values wrap within the existing max-width; no new text, font, token, renderer or
  settings format. Normal status rows retain 88-pixel height. All 20 generated
  palette CSS files carry exactly this same source change. Two manifests change
  only version; the other 78 runtime/data/media/notice files stay byte-identical.
- Added Arabic/English checks for status text clearance within every clipping
  ancestor and long-value wrapping inside its row. Original modern-browser range
  containment alone passed the old bug; strengthened the actual paint-clearance
  condition, verified failure on original CSS and full suite pass after the fix.
  Full LG ES5/build/service/async scheduling suite also passes. Browser Luna/decoder
  behavior is mocked; actual LG evidence is recorded separately below.
- Generated npm version metadata for 0.2.6 (no dependency selections changed),
  aligned app/service IDs/manifests, and packaged without minification. Candidate
  is 18,629,944 bytes; all 100 IPK payload files match source. Initial local diff
  expectation omitted generated theme CSS; checked the generator and proved each
  of its 20 palette files differs by exactly the same rule removal.
- Fresh 0.2.5 backup, close/install-over/relaunch of 0.2.6, and after-snapshot prove
  all 30 current preferences, startup and HDMI 1 preserved; scheduling succeeds,
  no UI error. All 100 installed files match. Live WebKit has no diagnostic style
  override; enabled/disabled/Fajr/silent values have clear clipping ancestors and
  normal wrapping. Owner: "Yes, the installed fix is readable."
- Evidence: ignored q094-crop-*, q094-dom-crop-*, q094-lg-patch-{before,after}-service/
  and versioned installed-payload logs. Arabic remains temporary until next step;
  no background activity armed yet. No Android/shared runtime source changed.
