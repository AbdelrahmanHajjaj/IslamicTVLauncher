# Test requirements

These are Islamic TV Launcher's requirements for changes into this project's
`main`. Choose checks from the changed behavior and its consumers, rather than
running every platform for a documentation edit. The PR must record commands,
results and untested paths. [CONTRIBUTING.md](CONTRIBUTING.md) owns the contribution
workflow; the [architecture queue](docs/ARCHITECTURE-QUEUE.md) owns current acceptance gates.

## Required evidence

- Add a focused regression for a bug or new product rule. For a bug, demonstrate
  the failure before the fix when practical. Test observable behavior and boundary
  cases; avoid tests that repeat implementation details. Prose-only edits need no
  runtime test.
- Put portable rule tests in `shared/core/src/commonTest/` so JVM and JS exercise
  the same cases. Keep OS, storage, network and media tests in the relevant shell.
- A shared change requires the union of affected consumer checks. Inventory
  Android, LG and preview bindings; validate canonical IDs, choices and explicit
  capability gaps. A passing core test alone does not establish UI parity.
- Keep existing checks enabled. Fix new failures and touched-code warnings;
  identify any existing failure against the base. Do not relax assertions, edit
  frozen outputs or declare a capability gap just to make a test pass.
- Identify the tested commit/app version and distinguish unit tests, mocked
  browsers, emulator runs, live TV diagnostics and human observations. Record
  unavailable hardware as pending. Required device acceptance remains open until
  it passes or the owner explicitly changes the scope.

## Setup and commands

Run from the repository root with JDK 17 and Node 24 on `PATH`. Android needs SDK
34 and `ANDROID_HOME` or ignored local SDK configuration. Install the relevant npm
lockfiles with `npm --prefix shared/core ci --ignore-scripts`,
`npm --prefix site ci` or `npm --prefix platforms/webos ci`.
On Windows use `gradlew.bat`, `npm.cmd` and `python`; elsewhere use `./gradlew`,
`npm` and `python3`. Use `--offline` only after caching Gradle dependencies.

Run core, preview and LG builds **sequentially**: their generators write the same
shared JS outputs. Run generated-file checks before a generator can repair stale
files, and inspect `git diff` afterwards.

| Changed area | Required automated checks |
| --- | --- |
| Documentation/templates only | `git diff --check`; verify local links, paths and documented commands against the owning scripts. Review claims against current evidence. |
| Portable core | `./gradlew -p shared/core check jvmJar --no-daemon`; this includes JVM, JS, Java interoperability and ES5/legacy checks. Then run affected shell/preview checks. |
| Canonical data, text, UI catalog or generators | `node tools/catalogs.mjs --check`, `node tools/catalogs.test.mjs`, `node tools/validate-shared.test.mjs`; regenerate intentional source changes and check each affected consumer. |
| Design/tokens, shared layout or scale | `npm --prefix site run tokens:check`, preview suite/build, and affected Android/LG rendering checks. Regenerate through the owning token/pattern tools. |
| Android TV | `./gradlew -p platforms/android-tv verifyApkSize lintDebug assembleDebugAndroidTest --no-daemon`, then `python3 -S -B -m unittest discover -s tests -v`. Build first: JVM regressions need the core jar and extracted Adhan sources. |
| Preview | `npm --prefix site run data:check`, `npm --prefix site test`, `npm --prefix site run build`; check changed flows in Arabic and English in a browser. |
| LG webOS | `npm --prefix platforms/webos test`, then `npm --prefix platforms/webos run test:browser`; these build and check shipped ES5, adapters, service, volume, HTTPS and mocked UI behavior. |
| Android phone installer / PC recovery | `./gradlew :app:assembleDebug :installer:assembleDebug :installer:lintDebug --no-daemon`, then root Python regressions. Exercise changed setup/recovery paths on an explicitly selected test device. |
| Media preparation | `node tools/prepare-adhans.test.mjs` (FFmpeg/ffprobe required); `node tools/prepare-adhans.mjs --check`; check the affected platform packages and audition any changed religious wording/cut. |

LG builds require all 14 adhan and 12 announcement clips under ignored
`state/media/{adhan,announce}/`. Follow the [audio setup](README.md#audio-assets)
and [audit](docs/AUDIO-AUDIT.md); keep originals outside packaged inputs. Android
allows an absent media directory, so a media-free CI APK cannot prove audible
playback or the size of a fully populated package. The complete TV APK must remain
strictly below **25,000,000 bytes**. Do not commit recordings or download them in
public CI to avoid a prerequisite.

## UI and input acceptance

Use [UI-CONTRACT.md](docs/UI-CONTRACT.md) and the generated
[gap report](docs/UI-GAPS.md). Tests must inspect actual bound views/DOM controls,
including choice values, rather than asserting only a support manifest.

For changed controls, exercise Arabic/RTL and English/LTR, draft versus saved
state, OK/Apply, Back cancellation, re-entry and return focus. Numeric controls use
core editor kinds, steps and bounds; ordinary numbers stay inline in their row.
Translate physical horizontal keys for RTL once at the shell boundary.

Navigation changes check grid/header edges, Home's top controls and All apps,
and focus after closing a child page. Sizing/layout changes check 80–150% in 10%
steps, focused-row visibility, joined Arabic, clipping, Home/mihrab separation and
unscaled video. Keep pointer behavior working where the shell supports it.

Android's [instrumentation guide](platforms/android-tv/README.md#native-runtime-check)
documents the existing `prayer`, `ui`, `size`, `size-windows`, `online` and
`online-network` modes. Choose affected modes on an explicitly selected device or
emulator. `size-windows` requires an isolated owned API 29+ emulator and temporarily
changes accessibility/screensaver system settings; do not use it as a routine
physical-TV check. The real-network mode is separate from deterministic fixtures.

## Platform and device acceptance

For an upgrade or a change to storage, startup, remote keys, scheduling, playback
or OS integration, capture a fresh private baseline and install over the existing
app with its existing identity/key. Compare preferences, selected/current input,
startup and relevant system state afterwards. Never uninstall or reset data to
make an upgrade pass. Restore only temporary test-owned changes; preserve later
user changes.

Test the affected lifecycle: duplicate/expired/cancelled callbacks, stop/end/error,
suspend/reopen and cleanup. Prayer changes cover minute flooring, offsets, civil
day/DST boundaries, tomorrow's Fajr and sunrise remaining informational. Online
comparison must preserve the offline schedule/settings and handle invalid replies,
cancellation and retry. Storage/volume recovery tests must include interrupted
and failed writes, overlap and user changes during temporary ownership.

Physical remote, speaker, moving HDMI and power behavior require human observation
on the target TV. Check relevant normal playback, temporary volume/mute handling,
input restoration and return focus. Run audible/power tests sequentially with a
watcher under the [LG rules](platforms/webos/AGENTS.md) or
[Android rules](CLAUDE.md). Read-only diagnostics may proceed without a watcher.
Do not schedule a reboot, change firmware or run historical probes as routine QA.

Label screenshots by source: browser, emulator or physical TV. LG screenshot APIs
do not reliably capture the video plane; live DOM/service state cannot establish
what the owner saw or heard. A result for one TV/firmware does not certify a brand.

## CI coverage and review

The checked-in workflows currently provide:

| Workflow | Evidence |
| --- | --- |
| `core.yml` | Standalone core JVM/JS/Java and ES5/legacy checks. |
| `app.yml` | Canonical projections, standalone Android build/lint/size, aggregate build, Python/Java regressions and native UI contract on API 34 emulator. |
| `site.yml` | Data/token freshness, preview regressions and site build; deployment is a separate job. |
| `installer.yml` | Installer build and bilingual emulator captures, without a full real-phone setup/recovery test. |

LG/media suites currently require local inputs and are **required local evidence**
for affected changes, not CI coverage. CI does not provide physical-TV acceptance
or automatically run every Android instrumentation mode. Run required checks even
when a workflow's path filter does not select them. If changing build/test wiring,
update affected workflow paths and commands too.

Use the [PR template](.github/pull_request_template.md). Explain a skipped check
with its reason and remaining risk; an unavailable device is not a pass. Draft
review can proceed with pending checks, but do not claim release/hardware readiness.
Keep concise durable outcomes in the owning docs and raw private evidence under
ignored `state/`. Existing historical results are evidence for their recorded build.
