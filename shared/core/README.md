# Islamic TV Launcher core

Pure product rules compiled from one Kotlin source to a JVM library and ES5
JavaScript. The migration queue and adoption decisions live only in
[`docs/ARCHITECTURE-QUEUE.md`](../../docs/ARCHITECTURE-QUEUE.md).

`PrayerRules` normalizes/validates six integer prayer offsets in [-30, 30] and selects the
current/next prayer from six already-calculated epoch-millisecond times plus
tomorrow's Fajr. Sunrise is a marker, never an alert. Selection does not mutate
today's times. Platform adapters retain astronomical calculation, calendar/timezone
resolution, persisted defaults, formatting, display conventions and OS integration.
The core has no Android, DOM, filesystem, network or timer dependency.

Android, LG and the preview consume those rules. `ReminderRules` owns native
eligibility/rounding, LG future-event selection and inclusive delivery classification.
The shells retain their different windows, stored keys, claims and lifecycle.

`HomeNavigation` defines semantic Home focus slots, initial content focus and
logical cardinal transitions. Android binds its existing views; LG binds actual
Home buttons, including its optional Watch input action. Each shell translates
physical horizontal keys for RTL once and applies the returned focus target.
Counts/columns describe visible primary/recent app rows; no pixel positions or
platform key codes enter the core. See the [UI/input contract](../../docs/UI-CONTRACT.md).

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
