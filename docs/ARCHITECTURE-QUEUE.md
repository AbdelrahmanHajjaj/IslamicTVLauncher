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
| Q03 | Active | Canonical resource extraction: move source ownership out of Android; migrate all generators/consumers and fixtures together; verify catalog/text/token equivalence and deterministic regeneration. |
| Q04 | Queued | Core domain migration: extract settings and prayer-day rules in complete tested slices; cover date rollover, tomorrow Fajr, DST, offsets, Hijri range, unavailable calculations and deliberate platform differences. |
| Q05 | Queued | Reminder policy migration: preserve enabled/default states, expiry, deduplication, cancellation, overlap and platform lifecycle; keep OS scheduling and playback in shells. |
| Q06 | Queued | Separate platform projects/builds: migrate paths only after boundaries work; update Gradle, npm, installer, tools, generated outputs, tests and CI; add no empty future-platform projects. |
| Q07 | Queued | Ownership/documentation: update actual upstream download/support references safely, architecture/contribution/agent guidance and relevant changelog; retain attribution/notices; do not publish or detach. |
| Q08 | Queued | Full automated review and measured optimization: affected Python/Java, Android build/lint/size, preview test/build/tokens and LG ES5/service/browser checks; resolve introduced regressions and review final diff. |
| Q09 | Hardware gate | Upgrade and device acceptance on old LG and Android: preserve settings/input/startup, verify both languages/focus, prayer/media/remote/lifecycle; record exact evidence and unverified paths. Android awaits hardware. |

## Current cursor

Next: Q03.2, canonical method IDs/presets. Finish the Q03.1 commit if resuming
between these operations, then inspect `PrayerMethod.java`, site prayer adapters,
LG method handling and their tests. Preserve all 19 IDs/order, library presets,
numeric parameters and Turkey corrections; keep library-specific API bindings in
adapters. Remove the city validator's temporary native-enum scraping when shared
method data exists. Continue one catalog at a time with exact old/new comparisons.
Do not move platform directories yet (Q06). Production apps retain their existing
domain implementations until Q04. Private logs/helpers are under ignored
`state/platform-core/`; do not publish them. Q02 commit: `83b0c72`.

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
- Proof candidate: Kotlin 2.2.10 is already in the local Gradle cache and its
  documented compatibility includes Gradle 8.9 / AGP 8.5.2. Keep those existing build
  tools unchanged. Source: https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html.

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

### Q03.2 inspected context / next action

- `PrayerMethod.java` has ten original adhan-java factories and nine custom
  presets. Turkey is custom in Java with method corrections `[0,-7,5,4,7,0]`;
  site/LG currently use adhan-js's Turkey factory. Preserve factory method tags and
  built-in algorithm behavior while moving numerical source ownership; do not
  flatten MoonsightingCommittee or other library behavior into angles alone.
- Existing generated `site/src/data/{methods,method-presets}.json` and
  `webos/build/service/data.json.methodNames` can seed exact canonical inputs.
  They are not authoritative sources after migration. Inspect `Text.methodName`
  and `methodAngles` before changing labels/adapters. Keep all 19 IDs and order.
- Affected consumers: `Prefs.METHODS` and method persistence, native prayer/text
  helpers, preview `data.ts`/`prayer.ts`, LG `core.js`/`build.cjs`, generator and
  tests. `tests/PrayerDayCheck.java` already covers original-method equivalence,
  France/Gulf/Turkey behavior, offsets and date transitions. Add exact old/new
  parameter/output comparisons and small validation tests for the new registry.
