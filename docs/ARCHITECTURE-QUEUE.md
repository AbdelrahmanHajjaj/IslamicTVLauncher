# Architecture migration queue

Updated: 2026-11-12 (Africa/Cairo). Owner: project owner. Execution: autonomous,
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
  inputs and results; no rendering, filesystem, network, device APIs or background timers.
- Owner's 2026-11-11 expansion: enforce a shared semantic UI/settings/action and
  remote-input contract across native shells. Use Android as the parity reference;
  inventory actual gaps, distinguish missing common features from intrinsic platform
  limitations, and test bindings/behavior. Keep native renderers; no generic UI engine.
- Owner clarified feature policy: the universal contract includes the full product
  design/settings language; a shell may leave features unimplemented and hide/lock
  them. Prefer hiding unimplemented LG controls initially. Explicitly distinguish
  implemented, unimplemented and OS-unsupported; do not remove definitions or
  silently pretend an unimplemented control works. Port supported/common bindings
  through the contract and retain an honest gap report.
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
| Q09.5 | Active priority | Owner-requested shared UI/settings/input contract. Home input and initial Settings control coverage are verified; continue bounded value/bounds and linked-editor coverage, then the new LG build's host/UI acceptance. Power/startup passed on 0.2.7; Android hardware remains open. |

## Current cursor

Execution resumed on 2026-11-12: owner reports the LG is available now. Scope and
acceptance criteria are unchanged; continue Q09.5d with the refreshed baseline.

Next: Q09.5e owner-requested All apps header navigation refinement, shared by
Android/LG: Up from any column of the first app row reaches Back; logical horizontal
movement stays in the Back → Prayer → Settings header row. Keep lower-row paging,
activation/Back behavior and all stored preferences intact. Add actual adapter
checks and verify the resulting LG patch while the owner is available; physical
Android acceptance remains separate. LG 0.2.8 is installed and
its payload, live legacy hosts, bilingual bindings and preserved state are verified.
The bounded
UI contract work is software-verified: 88 semantic controls, actual native/LG
bindings, canonical labels, choice values/bounds, five per-prayer switches, room
direction and favourite action flows. Native has 24 whole-control gaps and LG 22;
partial-option gaps remain separately declared. LG editing/cosmetic features may
stay hidden with honest unimplemented reasons, as the owner approved. Do not add a
generic UI engine, speculative features or forced cosmetic ports.

Authorized 0.2.7→0.2.8 close/install/relaunch succeeded. Fresh before/after/final
snapshots q095-ready-lg-*-service prove all 30 original preferences, Arabic, input
and complete startup state unchanged. All 100 installed payload files match the
candidate, and all 66 core assertions pass on actual Node 0.10.25 and WebKit 538.2.
Actual WebKit checks in Arabic/English account for all 88 controls (66 bound/22
gaps), 20 choice models/conditional variants, five prayer bindings and Home vectors.
English was temporary and Arabic is restored; no test is pending. Owner confirms
unchanged HDMI picture/sound, joined/readable Arabic values and normal physical
Home Up/Down/All apps/Back routes. Q09.5d is accepted; the new All apps header request
is Q09.5e. The owned inspector and its endpoint are verified closed. No duplicate audible/
power test is justified by these metadata/focus changes. Physical Android remains
unavailable until around 2026-11-25 and is a separate open gate.

HomeNavigation is shared and physically accepted on installed LG 0.2.7. Its Up
route uses Android's column-dependent top controls; Down beyond apps selects All
apps. Watched cold boot/startup passed: automatic launcher and HDMI picture/sound,
exactly one launch (3 to 4), all 30 preferences/input preserved, no pending test.
Browser then stayed visible for 90 seconds without a repeat launch; the same-boot
counter remained 4. Preserve the current Arabic preference, not an older English
snapshot. See docs/UI-CONTRACT.md for the implemented semantic/input specification;
the remaining gate is actual final-package verification, followed by unavailable
physical Android acceptance around 2026-11-25.

Q09.4.4 background service test passed its live checks and cleanup. Owner heard
adhan and reports Browser now, but did not observe whether Browser was visible
before the alert. Preserve that observation limit. The separate watched new-boot/
startup check passed on 0.2.7; leave the TV in the owner's Browser afterward.
The later fresh backup's current Arabic choice and all 30 values are preserved.
No test activity is pending. Do not reset Arabic from the older English snapshot.

Real LG evidence so far: 0.2.4 to 0.2.5 to 0.2.6 in-place upgrades preserve all
30 current preferences, HDMI 1 and startup state. Owner confirms picture/sound,
physical OK/Home/arrows/Settings/Back focus, joined Arabic/RTL, short adhan and
normal automatic audio/HDMI/settings restoration. Actual Node 0.10.25/core and
WebKit 538.2 checks pass; all 100 installed 0.2.6 files match the reviewed package.
English was restored/verified earlier; fresh pre-0.2.7 state was Arabic and the
update preserved it. Current backups are ignored q094-lg-home-*-service/.
The third owned inspector is closed; leave the current Browser state alone.
Background controller state in ignored q094-background/operations.json confirms
complete-awaiting-human and cleanupConfirmed=true; do not arm a duplicate test.

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

Q06 is complete: core/Android/LG build independently with canonical inputs; root
`:app` and installer tasks remain supported. Q06/Q07's path/notice acceptance
recorded 100 LG files and 99 Android APK entries with retained bytes preserved.
Later Home-contract runtime changes are intentional and measured in Q09.5b; do not
reuse the old byte-equality claim as proof of current navigation artifacts. Add no
unrelated renderer redesign, transport or future shell.

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

### Q09.4.4 cold-app background test and cleanup (2026-11-11)

- Original English and all 30 baseline preferences/input/startup verified after
  asynchronous UI save completion. An initial immediate status snapshot preceded
  the completed save; retained it and verified a fresh snapshot after live DOM
  showed English/Saved. This was a checker timing issue, not a settings reset.
- Closed the launcher, successfully launched native LG Browser and verified our
  app was absent from the actual inspector target inventory. Armed one 30-second
  test through the production service, with cleanup in finally. Live service
  evidence confirms Activity Manager callback, alert consumption, real audio
  progress, bounded stop and launcher hidden afterward. Cancelled the test and
  verified test=null, all original preferences/input/startup unchanged.
- Owner heard adhan and reports Browser afterward; did not observe Browser before
  playback. Do not claim a fully observed before/after foreground sequence from
  that reply. CLI/closed-app/service evidence remains distinct. Private controller
  operations/logs are under q094-background/. No one-shot remains pending.

### Q09.5 owner expansion and first findings (2026-11-11)

- Owner requests an enforceable cross-platform UI/settings contract and consistent
  remote behavior; LG may omit settings and Home navigation currently takes too
  many unintuitive steps. This is now approved iteration-one scope and priority
  before remaining power/final hardware signoff; companion/future shells stay deferred.
- Source confirms Android uses explicit FavouriteChoice.homeMove with logical
  directions; LG uses nearest-button geometry for Home. Owner chose top-controls
  behavior matching Android (Radio/Network/Settings according to first-row column),
  not unconditional Settings-gear focus. Down past the final app row must select
  All apps. Preserve RTL horizontal swapping, initial/return focus and modal input.
- Initial concrete LG gaps: appearance lighting, focus lighting, reduced motion and
  appearance cycling are absent from its preferences/settings; Home uses fixed app
  shortcuts rather than Android favourite selection/layout. Finish full inventory,
  distinguishing feasible common gaps from OS-owned Home/remote/screensaver limits.
  Do not label missing implementations unsupported just to pass coverage.
- Next deliverable is the source-grounded setting/action coverage and small contract
  shape, then shared Home behavior and shell conformance. Keep UI framework/layout
  and native operations in each shell; tests must cover actual consumers, not only
  a duplicate manifest. User has set the persistent goal active.

### Q09.5a contract and ordered implementation (2026-11-11)

- Wrote docs/UI-CONTRACT.md as behavior/boundary specification, not another queue.
  It covers semantic IDs, setting/editor coverage, capabilities, Home focus,
  RTL/modal input and actual-consumer conformance. No runtime enforcement is
  claimed yet. Keep shared definitions platform-free and native rendering intact.
- Ordered slices: (b) share existing Android Home map and wire both shells with
  JVM/ES5 plus actual adapter tests; (c) define the settings/action catalog and
  actual binding coverage, completing the source inventory; (d) port confirmed
  missing common behavior in thin tested slices rather than mislabeling it
  unsupported; (e) rerun affected real-LG input/UI/upgrade and then power acceptance.
  Android physical acceptance remains open throughout; it does not block these
  independent software slices.
- Additional inventory facts: Android has manual screen-facing/Qibla alignment,
  configurable favourite rows and favourite editing. LG has a true-north Qibla
  display and three fixed Home app shortcuts, with no corresponding settings.
  Language/clock system-follow options and native/global controls differ and need
  explicit capability/default mapping. LG currently has no CSS animation/transition,
  so assess reduced-motion behavior separately from its absent toggle. Do not
  inflate the gap list from labels alone or invent unsupported platform claims.

### Q09.5b shared Home contract: software verified (2026-11-11)

- Added pure JVM/ES5 HomeNavigation: stable semantic slots, primary/recent rows,
  initial app focus, Android-derived top-controls boundaries, downward All apps,
  last-primary return focus and optional Watch input. Shells map physical keys once
  for RTL and apply actual focus; no pixel/DOM/device API enters the core. Android
  delegates its prior map; LG binds real Home buttons and uses the core instead
  of nearest geometry. Other screens/modal ownership retain existing behavior.
- Froze original Android outputs before extraction: all 11,760 transitions over
  actual app/recent/classic/wide configurations match the new native/core adapter
  exactly. Common JVM/JS tests cover empty/partial rows, available-target/reachability
  invariants, boundaries, invalid configurations and optional input. Java calls and
  50 ES5/browser/CommonJS assertions pass without modern runtime APIs: 15 JVM and
  14 JS tests total. Combined production JS runtime is 45,168 bytes (+6,666).
- Reproduced LG's old initial focus on Today rather than app content. Actual
  renderer key tests now pass for initial focus, all three app columns, top targets,
  downward All apps, last-app restoration, OK activation and Back return across
  Arabic/English and Home layouts. Instrumented test calls prove each Home arrow
  invokes the shared core exactly once; no second hand-maintained focus table.
- PASS: full LG ES5/build/service/async suite, full bilingual browser suite,
  preview/shared generator tests, 39 Python/Java/simulated-device regressions and
  independent Android size/build/lint/test-APK compilation. An older app-cache
  harness also compiled FavouriteChoice; added the real core classpath there after
  its failure, then the full suite passed. No stub navigation substitute.
- Native APK 20,938,596 bytes, under 25 MB. Lint 0 errors / 55 warnings: unchanged
  54 source diagnostics plus one newly reported AGP-version-availability notice
  at unchanged settings.gradle, identified against retained prior XML. No source
  warning suppression or dependency upgrade. No speed/memory claim.
- LG patch metadata is 0.2.7, npm-generated lock version fields only. Source/native
  IDs, existing saved keys/defaults and media remain unchanged. Hardware validation
  of this navigation build is next; installed TV is still 0.2.6. Settings/action
  catalog enforcement and common missing-feature work remain Q09.5c/d.
- Evidence: ignored home-focus-{before,after}.csv, old native source/harness,
  ui-home-{core-check,lg-test,lg-binding-final,android-build,preview-test,
  native-regressions-fixed}.log and common/actual-renderer tests in source.

### Q09.5b installed LG Home acceptance (2026-11-11)

- Committed source 37adb68. Packaged 0.2.7 without minification: 18,632,668 bytes,
  SHA 93aab9580ef24ff85defaf41b164be4a19e40da3bd2ce25bd984ea335123a729.
  All 100 payload/source files match. Fresh 0.2.6 backup and in-place upgrade prove
  all 30 current preferences, input and startup preserved, scheduling successful,
  no UI error or pending test. Fresh/current lang is Arabic; preserve it rather
  than forcing the earlier English backup. No language change made for this check.
- Actual ready WebKit 538.2 has stored Mosaic theme, Arabic/RTL, HDMI 1 readyState=4
  and initial Home focus on the first app. Injected real key-handler vectors pass
  all three top-column targets, downward All apps and last-app return. All 50 core
  assertions pass against installed files on Node 0.10.25; all 100 installed hashes
  match. These diagnostics remain distinct from the physical remote evidence.
- Owner physically performed Up/top controls, Down/app, Down/All apps, OK and Back:
  "Yes, navigation now works as expected." Home slice accepted on old LG. Android's
  11,760 frozen transitions match; its physical hardware remains unavailable.
- Evidence: ignored ui-home-candidate/, ui-home-upgrade-operations.json,
  q094-lg-home-{before,after}-service/, q094-dom-ui-home-* and versioned installed
  payload/legacy logs. Third owned inspector is closed; TV UI left untouched.

### Q09.5 owner policy: universal contract, gated implementations (2026-11-11)

- Owner says to port the shared features/contract, permits LG features to remain
  unimplemented and hidden/locked, and requires universal design language. Adopt
  one full contract; shell profiles explicitly declare implementation state. Hide
  unimplemented LG features initially. OS limitations require their own reason;
  an unimplemented portable feature must not be mislabeled OS-unsupported.
- Scope refinement: enforce actual working bindings for implemented features and
  report the rest. Unimplemented LG controls may remain hidden without blocking
  the contract foundation solely to force every native Android feature into old
  LG. Preserve every currently working control and all stored values. Remaining
  feature implementation is visible, not silently claimed as complete parity.

### Next catalog implementation shape

- Prefer canonical shared/data/ui-contract.json plus generation into portable core
  IDs/definitions through the existing tools, keeping core standalone after
  generation. Definitions cover the union of current Android/LG controls/actions,
  semantic kinds, sections, canonical label references, appearance draft grouping
  and relevant shared bounds; no pixel/widget engine or preference-key migration.
- Add core lookup/profile/coverage validation and shell-owned implementation
  profiles. Every definition must be accounted for as implemented, explicitly
  unimplemented/hidden, or genuinely OS-unsupported with a reason. Derive supported
  coverage from actual registered/rendered bindings, not another manifest claiming
  controls exist. Existing working controls must remain working.
- Tag/enumerate real native rows and LG rows/carousels; check kinds, labels and
  actual UI coverage. Native labels already use canonical Text; LG should consume
  those references. Preserve contextual labels where needed. New LG-only label
  keys belong in shared/data and the Text template/generator, never generated files.
- Keep option/default/OS differences explicit. In particular Android home_layout
  means favourite-row layout; LG homeLayout means countdown mode. Use distinct
  semantic IDs instead of equating similarly named storage keys. Already static LG
  motion and native/global OS controls need honest profile declarations.

### Q09.4.5 watched startup acceptance (2026-11-11)

- Saved a fresh 0.2.7 baseline, startup enabled and test=null. Owner turned the TV
  off, disconnected/reconnected power and observed automatic launcher startup with
  HDMI picture/sound, without manually launching it. The kernel boot changed;
  lastBoot matches it, startup launchResult is successful, counter increased from
  3 to exactly 4, all 30 preferences/input survived and no test activity is pending.
- Owner opened LG Browser and confirmed it remained visible for about 90 seconds
  without the launcher reopening. A subsequent read-only snapshot is on the same
  boot with launch counter still 4 and preferences/input unchanged. Leave Browser
  untouched. No forwarding process or one-shot test was started for these checks.
- Private evidence: q094-lg-{power-before,reboot,power-stable}-service/snapshot.json
  and bounded read-only helper logs. Power/startup slice accepted on this old LG;
  this does not close unavailable Android hardware or the new UI catalog checks.

### Q09.5c catalog implementation in progress

- Added 76 semantic settings/action definitions, canonical label references,
  choice/toggle/number/action kinds and immediate/appearance-draft/action semantics.
  Generated portable core lookup/binding/coverage checks and explicit shell gaps.
  Native actual Row objects and LG rows/carousels now carry IDs; no stored keys or
  default values changed. LG rows consume canonical bilingual labels.
- Generated gap report distinguishes absent controls/layout alternatives from
  unimplemented features and OS restrictions. Existing working controls remain.
  This slice covers Settings, its six-offset editor and LG's test pane; Home input
  is separate. Further linked editors/favourite operations and choice/value/bounds
  enforcement still require review before claiming the full UI contract complete.
- PASS so far: portable core check (17 JVM/16 JS; 55 stripped ES5 assertions),
  source/label/gap validation and isolated shared-input checks, full LG build/ES5/
  service suite and actual browser binding/label coverage in Arabic and English.
  Avoided a set implementation that pulled extra runtime into ES5; measured combined
  production runtime 56,618 bytes (+11,450 over the accepted Home build).
- Native standalone build initially lacked SDK env configuration; rerun uses the
  already installed SDK 34. New actual-view instrumentation must be run on an owned
  emulator, then closed; no physical Android acceptance is claimed. No new TV
  package has been installed; the owner's LG remains on accepted 0.2.7 in Browser.

### Q09.5c1 shared settings coverage: verified foundation (2026-11-11)

- Canonical shared/data/ui-contract.json defines 76 settings/offset/test controls,
  stable semantic IDs, groups, value kinds, canonical label references and save
  semantics. Generated UiCatalog is checked in so core stays independently buildable.
  UiContract rejects unknown IDs/kinds, duplicate declarations and omitted controls.
  No preference keys/defaults or platform operations were migrated or renamed.
- Native Settings Row constructors require a contract ID, validate the actual
  widget's semantic kind and tag the real view. LG rows/carousels bind through the
  core and consume canonical bilingual labels. Conformance enumerates actual
  controls in every section and six-offset editor, checks duplicate IDs, sections,
  types and labels, and combines their IDs with explicit shell gaps. Neither shell
  has an independent manifest pretending supported controls exist.
- Generated docs/UI-GAPS.md reports 24 absent Android controls and 15 LG controls,
  with reasons that distinguish layout alternatives, unimplemented features and
  tested OS restrictions. These counts describe controls, not equivalent counts of
  missing product features. LG lighting/cycling/favourite rows/focus lighting remain
  explicitly unimplemented and hidden; reduced-motion policy documents static UI.
- PASS: 17 JVM /16 JS common tests; 55 legacy ES5 assertions in stripped browser and
  CommonJS hosts, 56,618 combined production JS bytes; canonical source/generation
  and isolation checks; full preview suite; full LG ES5/build/service/async suite;
  actual bilingual LG browser binding/label/section coverage and existing workflows;
  39 Python/Java/simulated-device regressions; native build/lint/test APK compilation.
- Actual API 37 emulator ran check=ui successfully for both languages, all native
  settings sections and six offset steppers. It restores locale and verifies every
  preference unchanged. Early failures identified test event timing and PrayerIcon's
  object-replacement character; dispatch through the real Activity handler is now
  deterministic and label comparison excludes that icon placeholder. This is real
  renderer evidence, not physical TV remote delivery. Final section check also passed.
- Preserved all original compiled-text output hashes. Froze their original 174-key
  set in tests/fixtures/text-output-keys.json so 19 new canonical LG label getters do
  not invalidate the original-output proof. Generator/label tests cover the new keys.
  A normalized-source comparison proves every native Row implementation and existing
  preference/action handler is byte-equivalent after removing contract metadata.
- Native APK 21,122,011 bytes, below 25 MB; lint 0 errors/55 existing warnings. No
  speed/memory claim. LG patch versions aligned to 0.2.8 with npm-generated lock
  metadata, full build/service checks passed after alignment. No TV install occurred.
- Native App CI now checks generated projections and runs actual-view conformance
  with the already-used emulator runner. Workflow YAML and its script syntax pass;
  remote CI has not run because there is no push. Context7 lacked that action;
  configuration checked against its official README and the existing installer job.
- Owned emulator was closed, resumed only for the added section assertion, then
  closed again. Both closures and unchanged original user AVD hashes are verified.
  No inspector, emulator, one-shot test or background controller is left running.
- Evidence: ignored ui-catalog-{core-final,lg-0.2.8-final,lg-browser-final,
  preview-test,native-regressions-final,final-native-build,native-section-final,
  emulator-closure}.log, ui-contract-emulator/ with original closed-session evidence,
  and committed source/tests. Android TV hardware and actual old-LG 0.2.8 host/UI
  acceptance remain open. Choice/bounds and linked-editor coverage is Q09.5c2 next.

### Q09.5c2 numeric bounds: verified (2026-11-11)

- Numeric controls reference prayer-offset, hijri-offset or percentage ranges in
  the canonical catalog. Generation rejects missing/unknown ranges and ranges on
  other control kinds. Core minimum/maximum reuse PrayerRules constants rather
  than duplicate prayer/Hijri limits; percentages remain 0–100.
- Android validates the actual STEP row's lowest/highest against its shared
  control. LG captures the numeric ID from the real row that opened the editor,
  validates its kind, and reads minimum/maximum from the core. No duplicate
  action-to-setting map or stored-key migration. Existing volume step size stays 5;
  integer prayer/Hijri stepping and saved/default values are unchanged.
- LG browser exercised both limits, saving, Back cancellation, reopen and original
  restoration for all six offsets, Hijri adjustment and app volume through actual
  editor key handlers. Final preference object equals the original. Full bilingual
  browser workflows and LG build/ES5/service/async suite pass.
- PASS: core 18 JVM/17 JS tests and 58 stripped ES5 assertions; combined production
  JS 57,674 bytes. Canonical generation and full preview/shared checks pass. Native build/lint/test APK
  pass, APK 21,122,164 bytes and lint 0 errors/55 existing warnings. Actual API 37
  bilingual native conformance passed with all STEP range guards exercised.
- Reopened only the same private owned emulator with a second closed-session
  snapshot preserved. Final shutdown, offline serial, exited process and original
  user AVD hashes verified. No emulator/TV inspector/test activity remains active.
  Evidence: ignored ui-number-{core-check,lg-test,lg-browser,native-build,
  native-views,emulator-closure}.log and ui-contract-emulator/number-runtime-*.
- Choice value mapping and per-prayer/favourite linked controls are next. This is
  still the uninstalled 0.2.8 source; accepted TV remains 0.2.7 in Browser. No
  reason to repeat audio/power checks or install an intermediate package now.

### Q09.5c2 choice mapping implementation in progress

- Use semantic string values, resolving city/method/voice/station/theme/background
  references from the existing canonical catalogs. Keep stored values, choice order
  and platform labels/editors in their adapters. Validate the actual offered values;
  aggregate conditional variants in conformance rather than narrowing the universal
  definition to whatever happens to be visible in one current state.
- Declare partial option gaps separately from whole missing controls, with explicit
  reasons. Android currently lacks explicit auto digits and city-default method;
  LG lacks system-follow language/clock, custom themes and procedural backgrounds.
  Map LG plain to shared solid semantics, not a preference rename. Native imported
  custom-theme and countdown-position conditions need real renderer checks.
- Do not claim this enforcement complete until both actual adapters and conformance
  consume it. No companion, future-shell scaffolding or intermediate TV install.

### Q09.5c2 semantic choice values: verified (2026-11-11)

- All 24 choice definitions now include semantic values or references to existing
  city/method/voice/station/theme/background catalogs. Time-zone choices come from
  the core; remaining shell values/order are preserved. Canonical IDs distinguish
  countdown from favourite layout, city-default method and system-follow settings.
  Native enums/indices and LG stored plain/nullable defaults are explicit adapters;
  no preference rename, reset or new feature is introduced.
- UiContract returns copied choice arrays and rejects unknown/duplicate offered
  values. Full option coverage accounts for real conditional variants and explicit
  partial gaps. Generated report includes 2 native and 4 LG partial-option gaps,
  separately from whole missing controls. Reasons remain implementation gaps,
  without inventing OS restrictions for cosmetic/product options.
- Native Row.keys derives from actual arrays/catalogs; SEG/carousel counts validate
  against the real labels, and popup builders validate option IDs/counts as they
  open, including automatic single-city selection. Actual rows carry their native
  binding model. LG rows/carousels derive semantic metadata from the same choice
  builder used by the editor; opened pickers and chosen offered values validate.
- Native conformance passed on actual API 37 for Arabic/English, including absent/
  present valid custom-theme prerequisites and all countdown-position modes. It
  restores all changed locale/theme prerequisites and verifies the complete saved
  preference object unchanged. LG actual renderer coverage passes in both languages,
  aggregating mihrab/compact/hidden variants plus all settings; prior grouped-city,
  voice, numeric save/cancel, appearance apply/cancel and media workflows pass.
- Portable UI catalog generation/check now runs independently of shell/preview
  trees through tools/ui-contract.mjs [--write]. JS consumer builds fail early on
  stale generated definitions. An isolated fixture proves checking, intentional
  staleness rejection and regeneration with no platform/preview sources present.
  Core's checked-in generated Kotlin remains independently compilable.
- PASS: 19 JVM/18 JS common tests; 62 stripped ES5/browser/CommonJS assertions,
  combined production JS 74,893 bytes; native build/lint/test APK and actual-view
  conformance; full LG build/ES5/service/async and bilingual browser suites; full
  preview/shared/generator checks; all 39 native Python/Java regressions. Tested
  native APK 21,124,730 bytes; lint 0 errors/55 existing warnings. No speed claim.
- Third closed-session snapshot preserved before reopening only the same owned
  emulator. After success, final closure/offline serial/exited process and original
  user AVD hashes are verified. No emulator/inspector/test activity is pending.
  No TV install occurred; 0.2.8 remains source-only and installed old LG is 0.2.7.
- Evidence: ignored ui-choice-{core-check,legacy-final,native-final-build,
  native-views,emulator-closure,isolation-final,preview-test,lg-complete,
  lg-browser-complete,native-regressions}.log and private choice-runtime records.
  Linked per-prayer/favourite controls and editor flows are next; physical Android
  and the final 0.2.8 old-LG host/UI checks remain open.

### Q09.5c2 linked prayer screen: diagnosis in progress

- Actual native source exposes six adhan toggles, including sunrise, despite core
  policy excluding sunrise from every alert. LG already exposes only five. Added
  an actual-view regression before changing production code: sunrise interaction
  must not write a setting or remain an actionable/focusable adhan row. Preserve
  any existing bell_1 value; no preference deletion or new sunrise alert.
- Next bind the five real prayer switches through shared IDs, test round trips,
  and cover the existing room-direction editor's save/cancel/return behavior.
  Favourite operations follow sequentially. No TV update or audible test here.

### Q09.5c2 prayer controls and room editor: verified (2026-11-11)

- Added five semantic per-prayer toggle IDs. Core isPrayer accepts only the five
  exact indexes, shared by selection/reminder policy and actual native/LG controls;
  prayerAdhanId rejects sunrise and malformed numbers. Catalog now has 81 controls.
  Room direction is a numeric value with a shared 0–359° range, using its existing
  native editor and storage. LG's missing room editor remains an explicit gap.
- Reproduced the native sunrise defect before production edits: activating its
  apparent adhan switch wrote a preference although sunrise can never alert.
  Removed that misleading focus/click/bell affordance, kept the visible time/status
  and preserved any existing bell_1 value. Native arrows skip the information row;
  the preview matches, with a disabled row, no bell/action and no adhan label.
- Actual API 37 native conformance passes in Arabic/English: all five bound switches
  mutate their corresponding stored flag and visible state; sunrise cannot mutate
  preferences. Room checks cover physical compass directions in either language,
  359/0 wrap, Back cancellation, OK saving, launch from its actual Settings row,
  return focus and rendered saved bearing. Every changed flag/bearing and the full
  original preference object are restored. No alert/media/OS action is triggered.
- Native fixture initially stayed in touch mode and lost focus across a rebuild;
  real D-pad input establishes the precondition before direct handler checks. LG's
  mock stores before its asynchronous reply; wait for the actual redrawn button
  after each toggle, not merely the mock storage write. The unsynchronized test
  once dropped Back while save was busy; synchronized full bilingual workflows pass.
- PASS: 21 JVM/20 JS common tests; 66 stripped ES5/browser/CommonJS assertions,
  combined production JS 76,930 bytes; canonical generation/isolation/freshness,
  full preview tests/build and tokens check; full LG ES5/build/service/async suite
  and bilingual browser flows, including five toggle round trips and originating
  Settings return; all 39 native Python/Java regressions. Native build/lint/test APK
  and actual UI checks pass; APK 21,124,952 bytes, lint 0 errors/55 existing warnings.
- Fourth prior closed-session record preserved before resuming only the owned
  emulator. Final shutdown/offline serial/exited process and unchanged original
  AVD hashes verified. No emulator/TV inspector/test activity remains pending.
  No TV update, sound or power test occurred; accepted LG remains 0.2.7 in Browser.
- Evidence: ignored ui-prayer-{core-check,legacy-final,final-native-build,
  final-native-check,native-regressions,preview-test,preview-build,lg-complete,
  lg-browser-synchronized,catalog-final,tokens-final,emulator-closure}.log and
  private ui-contract-emulator/prayer-{red,green}-result.log. The red regression
  failed on the original production screen before the fix. Next: favourite actions,
  then the final 0.2.8 old-LG host/UI check. Physical Android remains open.

### Q09.5c2 favourite actions: implementation in progress

- Bind the existing native arrange menu's Add/Remove/Move earlier/Move later/
  Replace/Cancel and system Uninstall entry to canonical action IDs/labels. Keep
  native minimum/maximum, ordering, package validation and storage unchanged.
  LG editing/uninstall controls remain hidden with explicit unimplemented gaps.
- Exercise real native menus and replacement mode in both languages, including
  long-press release suppression, boundaries, mutations, Back/Cancel and focus.
  A test-APK-only launchable fixture supplies a removable non-favourite without
  touching user apps; never activate Uninstall or launch another app. Restore all
  test preferences and close only the owned emulator after verification.
- Real native regression passed menu mutation/cancellation and exposed a return-
  focus defect: replacing slot two saved correctly but Home focused slot one.
  Before the fix, ui-favourite-red.log fails specifically on the chosen-slot
  return assertion. Keep the existing slot fallback when the previous package is
  replaced; still follow the package if it remains elsewhere, and use initial
  focus when the old slot disappears. No preference or app-launch change.

### Q09.5c2 favourite actions: verified (2026-11-11)

- Catalog now defines 88 controls, including six favourite editing actions and a
  separate native Uninstall action. Real native menu items bind kinds/IDs and
  canonical labels; their small action model keeps labels and callbacks together.
  Existing package checks, menu order, four-to-nine policy, saved keys and action
  handlers remain intact. Home replacement targets carry the same semantic action.
- LG's fixed catalog keeps all seven actions hidden with explicit unimplemented
  gaps, without claiming an OS restriction. Generated report now has 24 native/
  22 LG whole-control gaps; partial-choice gaps are unchanged. Both actual shell
  conformance runs account for every catalog ID; no independent support manifest.
- Actual API 37 Arabic/English tests exercise real long-press menus, opening-release
  suppression, canonical action labels, minimum/first/last boundaries, Add/Remove,
  both reorder directions, saved order, visible stars and originating app focus.
  Replacement checks slot-only focus/Up containment, RTL/LTR end movement, Back
  cancellation, OK saving and the chosen-slot return. A test-only launcher entry
  supplies a removable non-favourite; Uninstall is bound/checked but never invoked.
  All original preferences are restored; no app is launched or removed.
- Regression failed before the focus fix on replacing slot two: the saved order
  was correct but focus jumped to slot one. Deleting the line which discarded the
  existing slot fallback fixes it. The same real regression, plus cancellation/
  modal/visible-marker assertions, passes in both languages. No UI redesign.
- PASS: 21 JVM/20 JS common tests; 66 stripped ES5/browser/CommonJS assertions,
  combined production runtime 78,337 bytes; catalog/generation/isolation/freshness
  checks, full preview/shared suite; full LG ES5/build/service/async and bilingual
  browser workflows; all 39 native Python/Java regressions. Native build/lint/test
  APK and actual-view conformance pass: APK 21,125,157 bytes, 0 errors/55 existing
  lint warnings. Physical long-press delivery/TV timing is not emulator proof.
- Preserved fifth prior closed-session snapshot, resumed only the owned emulator,
  then verified shutdown/offline serial/exited process and unchanged original AVD
  hashes. All tool sessions completed; no emulator/inspector/TV test is pending.
  No TV update occurred. Next is final 0.2.8 package/old-LG acceptance; physical
  Android remains open. No companion or future-platform work is added.
- Evidence: ignored ui-favourite-{red-build,red-target-build,red,green,core-check,
  final-native-build,final-native-ui,native-regressions,lg-test,lg-browser,
  preview-test,catalog-final,legacy-final,emulator-closure}.log and private
  ui-contract-emulator/favourite-{red,green}-result.log.

### Q09.5d final LG candidate: prepared (2026-11-11)

- Packaged the verified 0.2.8 build from source commit 09f7d31 using the existing
  local CLI with --no-minify. Candidate is 18,648,304 bytes; archive review proves
  all 100 app/service files match the checked build byte-for-byte, with only the
  expected packageinfo metadata. Package/app/service IDs and version agree; media,
  notices and ES5 runtime bytes are preserved. Hash/source association is saved in
  the ignored candidate manifest. CLI syntax checked via Context7's CLI sources.
- Read-only device preflight succeeds. Fresh q095-lg-before-service backup confirms
  installed 0.2.7, all 30 current preferences, Arabic, HDMI 1 and enabled startup;
  UI is hidden after Browser use. No test is pending. The retained pendingAlert
  record is already displayed, so it is not a new undelivered alert. Automatic
  scheduling remains enabled and unchanged. SSH query sessions ended normally.
- Asked whether the owner is beside LG for final picture/sound and remote checks;
  answer pending. Do not infer readiness or repeat the old question as approval.
  No close/install/launch, inspector, audible test or TV-state mutation occurred.
  Prepare to run the existing in-place workflow only when the owner is ready,
  with a current baseline and exact candidate hash guard. Do not rerun a mutation
  following an observation timeout until actual installed/operation state is known.
- Evidence: ignored ui-contract-{package,package-check,lg-before}.log,
  ui-contract-candidate/manifest.json and IPK, q095-preflight.json and the private
  q095-lg-before-service snapshot. Actual installed payload, Node/WebKit 66-check
  runtime proof, native TV UI and human acceptance remain open; Android unchanged.

### Q09.5d owner deferral (2026-11-11)

- Owner answered the readiness question: "Later; keep the TV unchanged". This
  defers the TV update and watched acceptance, not the overall migration scope.
  Leave installed 0.2.7, existing settings/input/startup and the user's current
  foreground app untouched. No further TV operation occurred after this answer.
- Verified 0.2.8 candidate and private baseline are retained; no source change or
  repeated build is needed. On readiness, recheck the current baseline and exact
  package before the already-authorized in-place workflow. Keep final LG and
  physical Android acceptance open. Do not replace either gate with mock evidence
  or add unrelated software work merely to avoid waiting.
- Blocked audit: first goal turn with the explicit owner-unavailable condition.
  No new runtime test, emulator or inspector was started; the previous owned
  emulator's verified closure remains recorded. Goal is not complete or paused.

### Hardware availability audit (2026-11-11)

- The same readiness gate arose in the final-candidate preparation turn (8254382,
  question pending), repeated in the owner's explicit deferral turn (00b9a4a),
  and remains in this continuation. Three consecutive goal turns now require the
  same external change: owner availability for LG checks; physical Android is
  also unavailable. No further independent software item precedes those gates.
- Local revalidation confirms a clean feature branch, retained candidate with its
  reviewed hash/source association, and no final-candidate upgrade operation begun.
  No TV connection, repeated test/build or device mutation was made in this audit.
- Mark the persistent goal blocked, not complete or paused. Keep 0.2.8 uninstalled.
  On owner readiness, resume Q09.5d with a fresh baseline and the retained candidate;
  keep physical Android acceptance open until the device is available. Do not ask
  the same availability question or automatically retry a TV operation meanwhile.

### Q09.5d LG readiness resumed (2026-11-12)

- Owner set the goal active and reported "LG tv available now". This resumes the
  already authorized in-place upgrade and watched UI checks; no further permission
  question is needed. Keep physical Android acceptance separate and open.
- Retained 0.2.8 candidate hash/source association verified against 09f7d31; later
  commits are documentation only. Fresh read-only q095-ready-lg-before-service
  backup succeeds, with 30 current preferences, installed 0.2.7 and no test or
  undelivered alert pending. Preserve this fresh state rather than older snapshots.
- Prepared bounded close/install/launch helper with exact candidate hash and
  operation-record guard. No mutation has started at this checkpoint. Once started,
  inspect authoritative operation/device state before retrying any timeout/failure.

### Q09.5d installed 0.2.8 live verification (2026-11-12)

- Close, in-place install and launch completed with CLI success and a terminal
  operation record. Installed version is 0.2.8; all 100 payload files match the
  reviewed candidate. No uninstall, identity change, firmware or release operation.
  All 30 preferences, input and entire enabled-startup state match the fresh
  before snapshot. Owner confirms picture/sound match, with no visible error.
- Actual installed service core passes all 66 assertions on Node 0.10.25. Actual
  WebKit 538.2 passes the same 66 checks, exposes 88 definitions, has a ready input
  decoder (readyState 4), no video error and no active test overlay.
- Actual Arabic and English rendering each pass complete binding/gap coverage:
  66 bound controls +22 declared gaps, canonical labels and section/kind checks,
  20 offered choice models including all countdown variants, five correctly mapped
  prayer controls excluding sunrise, nine Home focus vectors and Prayer-day Back
  return to its originating setting. Appearance edits stayed drafts and were
  cancelled; no prayer switch, time offset, audio control or startup was changed.
- Real language control was exercised and Arabic restored. Initial live-test
  setup incorrectly expected Home after the language save and its Enter toggled
  English back to Arabic; observed actual state before retrying. Corrected the
  private fixture to leave Settings through its Back control, then English proof
  passed and Arabic was restored through the real save callback. No product fix.
- Actual installed Arabic values (enabled, disabled, Fajr and both silent test
  values) have visible overflow/normal wrapping and clear clipping ancestors,
  normal 88px rows and no diagnostic styling. Left Test alerts visible and asked
  the owner to confirm joined/readable Arabic plus physical Home focus routes.
  Those two human answers remain pending; decoder/DOM checks do not replace them.
- Final service snapshot proves exact restoration of all 30 original preferences,
  Arabic, input and startup after bilingual checks, with no pending test. The one
  owned inspector was closed after process/path/arguments/creation-time identity
  checks; process exit and unreachable local endpoint are verified. UI untouched
  by cleanup. No emulator, forwarding process or one-shot test is left active.
- Evidence: ignored ui-contract-{upgrade,lg-ready-after,lg-installed-payload,
  lg-installed-legacy,lg-webkit-core,lg-webkit-coverage-ar,
  lg-webkit-coverage-en-fixed,lg-language-restored,lg-arabic-values,
  lg-ready-final}.log, upgrade/final-parity records, q095-ready-lg-*-service
  snapshots and q095-inspector-closure.json. Physical Android gate remains open.

### Q09.5d human acceptance and Q09.5e steering (2026-11-12)

- Owner confirms installed Arabic is joined and all status values are fully
  readable, including بلا صوت. Physical Home Up/Down, All apps, OK and Back/return
  focus work. Together with the recorded picture/sound, payload, host/runtime,
  bilingual DOM and preference checks, Q09.5d 0.2.8 is accepted on this old LG.
- Owner requests a small All apps refinement: from any first-row column, Up should
  select Back; while on Back, Left/Right should navigate the header's Prayer and
  Settings controls instead of dropping into the app grid. Interpret the header
  directions logically with one RTL translation, as elsewhere. Keep native lower
  grid movement/paging and existing action handlers unchanged.
- Continue as Q09.5e before the Android hardware gate: put the deterministic All
  apps focus rule in core and bind both actual shells, with core/adapter checks.
  No visual redesign, new settings/features, media/power test or companion work.
