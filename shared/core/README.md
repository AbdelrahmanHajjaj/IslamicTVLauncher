# Islamic TV Launcher core

Pure product rules compiled from one Kotlin source to a JVM library and ES5
JavaScript. The migration queue and adoption decisions live only in
[`docs/ARCHITECTURE-QUEUE.md`](../../docs/ARCHITECTURE-QUEUE.md).

`PrayerRules` normalizes/validates six integer prayer offsets in [-30, 30] and selects the
current/next prayer from six already-calculated epoch-millisecond times plus
tomorrow's Fajr. `isPrayer` identifies the five exact prayer indexes; sunrise is
a marker, never an alert or adhan switch. Selection does not mutate
today's times. Platform adapters retain astronomical calculation, calendar/timezone
resolution, persisted defaults, formatting, display conventions and OS integration.
The core has no Android, DOM, filesystem, network or timer dependency.

`floorToMinute` validates a finite epoch-millisecond value and rounds downward,
including before the epoch. JS adapters request unrounded astronomy first; the
native Adhan minute adapter delegates to this same rule before seconds are lost.
All methods preserve their angle/interval corrections and the user's saved offsets.

`PrayerComparison` reports six signed whole-minute differences (online minus
calculated), without changing either schedule. Both inputs use epoch milliseconds;
shells must validate the reference's date/location/method/school before comparing.
Its named AlAdhan method lookup is generated from `shared/data/methods.json`.
Network requests, ISO parsing, source labels and advisory UI remain in the shells.

Android, LG and the preview consume those rules. `ReminderRules` owns native
eligibility/rounding, LG future-event selection and inclusive delivery classification.
The shells retain their different windows, stored keys, claims and lifecycle.

`HomeNavigation` defines semantic Home focus slots, initial content focus and
logical cardinal transitions. Android binds its existing views; LG binds actual
Home buttons, including its optional Watch input action. Each shell translates
physical horizontal keys for RTL once and applies the returned focus target.
Counts/columns describe visible primary/recent app rows; no pixel positions or
platform key codes enter the core. See the [UI/input contract](../../docs/UI-CONTRACT.md).

`AllAppsNavigation` shares the five-column app-grid transitions and circular
Back/Prayer/Settings header. Any first-row column moves Up to Back; native shells
retain paging and pass the first visible row for the header's Down targets.

`UiContract` exposes the generated semantic settings/action catalog, validates
binding kinds and checks actual binding IDs plus explicitly declared gaps for
complete, nonduplicated coverage. Source definitions and label references live in
`shared/data/ui-contract.json`. `node tools/ui-contract.mjs --write` regenerates
only the portable catalog using shared sources; `node tools/catalogs.mjs` also
updates native projections and the gap report. The checked-in generated Kotlin
keeps core builds standalone. JS consumer builds check freshness before compiling.
Actual native rows, favourite-menu actions/replacement targets and LG rows/carousels carry these IDs. Their conformance checks
enumerate real views/DOM controls in both languages, rather than a support manifest.
Shell gap declarations generate the [gap report](../../docs/UI-GAPS.md).
Numeric controls reference named ranges; `minimum`/`maximum` reuse `PrayerRules`
for prayer/Hijri limits and define 0–100 percentages and 0–359° room bearings. Native steppers validate
their bounds; LG's actual numeric editor reads them from its bound control ID.
Choice values are semantic strings, with catalog references resolved during
generation. `choices` returns a copy; `validateChoices` rejects unknown/duplicate
values, while `validateChoiceCoverage` accounts for offered conditional variants
and explicit platform option gaps. `prayerAdhanId` maps each of the five prayer
indexes to its toggle definition and rejects sunrise. Stored-value conversions remain in the shells.

`UiScale` defines the shared 80/90/100/110 percent preference policy, matching
`display.size` choices. Unknown or invalid values resolve to the original 100%.
Canvas fitting, text/control scaling, video exclusion and persistence belong to
the renderers; defining the control does not implement it in a shell.

Java callers use `tv.afnon.core.PrayerRules.INSTANCE`; the returned
`PrayerSelection` exposes standard getters. JavaScript exposes the same namespace
under the UMD module `tv.afnon:core`; load `kotlin-kotlin-stdlib.js` first
in a browser or use the emitted CommonJS entry. JavaScript numeric-array parameters
are `Float64Array` for epoch milliseconds and `Int32Array` for offsets. Adapters
must validate untrusted values before converting them to integer arrays.

## Build and check

Use JDK 17 and Node 24. This is a standalone Gradle build; it does not configure
Android, require an Android SDK or need LG sources/media/device keys. Its local
Acorn development dependency parses the output as ES5.

Node 24 also runs the compiler's test tooling. Supported npm overrides patch its
Glob, minimatch, diff and serialize-javascript leaves while keeping Kotlin, Mocha
and the Kotlin reporter pinned. Root-package task inputs include those selections
so incremental builds regenerate the npm configuration. The canonical generated
lock is `kotlin-js-store/package-lock.json`; regenerate it with matching overrides
when changing tool pins, rather than editing its package entries by hand. Build/test
tools are not bundled into the JVM library or production ES5 files.

```powershell
npm.cmd --prefix shared/core ci --ignore-scripts
./gradlew.bat -p shared/core check jvmJar --no-daemon
```

On Linux/macOS use `npm` and `./gradlew`. Common tests run on JVM and Node; a Java
test exercises the JVM API. `legacyCheck` parses production JavaScript as ES5 and
runs the legacy proof in browser and CommonJS contexts with modern APIs removed.
It is part of `check` and can be run separately with `node
shared/core/test-legacy.cjs` after compilation.

The JVM jar is under `build/libs/`. Production JavaScript is under
`build/compileSync/js/main/productionExecutable/kotlin/`. These are generated
outputs, never source files. Compiler and test tooling are development dependencies;
include the applicable Kotlin runtime notices when packaging runtime artifacts.
Canonical runtime notices are in `assets/licenses/kotlin/`; Android packages them
through its asset source set. The native JVM regression uses the real core jar and
its pinned Kotlin runtime, stubbing only Android context/calendar access.

Android includes this build as the local `tv.afnon:core` composite dependency.
LG/preview builds invoke it directly through `build-js.cjs`; the private npm
package exposes the generated CommonJS module/declarations. The `.js` filenames
stay fixed; the UMD namespace derives from the core's stable Gradle group/name.
