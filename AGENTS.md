# Islamic TV Launcher: agent instructions

For the active platform architecture migration, read
[`docs/ARCHITECTURE-QUEUE.md`](docs/ARCHITECTURE-QUEUE.md) first. It is the
authoritative approved scope, status and next action. Keep it concise; essential
verification facts belong in [`docs/ARCHITECTURE-EVIDENCE.md`](docs/ARCHITECTURE-EVIDENCE.md),
not per-tool progress notes. Continue sequentially; companion work is deferred.

Read `README.md`, `CONTRIBUTING.md` and `TESTING.md` before larger changes.
Islamic TV Launcher is independently maintained in
`AbdelrahmanHajjaj/IslamicTVLauncher`; this project's requirements govern work here.
Android and LG are separate applications over shared rules and sources.

## Repository map

- `platforms/android-tv/`: native Android TV launcher (`tv.afnon.home`), Java and Android Views.
- `installer/`: Android phone installer and recovery workflow; Android targets only.
- `site/`: Astro browser preview and shared-data/token extraction tools.
- `shared/core/`: standalone Kotlin JVM/ES5 product rules and semantic UI/input contracts; no platform APIs or rendering.
- `shared/data/`, `shared/design/`: canonical product data, design and distributable assets.
- `afnon-mihrab/`: shared design tokens, patterns and design references.
- `platforms/webos/`: LG application (`tv.afnon.webos`) and JS service
  (`tv.afnon.webos.service`). Read `platforms/webos/AGENTS.md` before changing this port.
- `tools/webos-background-probe/`: historical, bounded hardware experiments;
  not the installed launcher or its production service.

The LG port is a normal folder in this repo, not a Git submodule. Its build consumes
shared core/data/design, common validation tools and local ignored media. Android UI code is not shared with the LG UI;
changes to one renderer do not automatically update the other. Tizen is research
only; do not claim a working Samsung port.

[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) owns module boundaries and build
entries. [`docs/UI-CONTRACT.md`](docs/UI-CONTRACT.md) owns semantic UI/input rules;
the generated [`docs/UI-GAPS.md`](docs/UI-GAPS.md) records shell coverage.
[`TESTING.md`](TESTING.md) owns test requirements. None is a second queue.
[`docs/REPOSITORY-INDEPENDENCE.md`](docs/REPOSITORY-INDEPENDENCE.md) prepares the
administrative fork detachment. Preserve original MIT and third-party notices,
Git history, installed identities and this project's download/support targets.

## Change boundaries

- Put deterministic product policy in `shared/core`, with explicit inputs/results
  and common JVM/JS tests. Keep astronomy/calendar, persistence, clocks/timers,
  transport, rendering, media and OS APIs in the shells.
- Change canonical data/design before regenerating consumers. Do not add a second
  catalog, duplicate platform policy or a speculative platform/companion framework.
- Define UI controls and choices in `shared/data/ui-contract.json`; behavior,
  navigation and numeric adjustment belong in core. Bind actual views/DOM to those
  IDs and declare missing capabilities/options. A catalog entry does not implement
  a renderer. Mirror physical horizontal input for RTL once in the shell.
- Check all affected consumers of a shared change. Android's existing screens are
  the parity reference where the shared contract leaves behavior unspecified.

## Shared rules

- Keep the visible name **Islamic TV Launcher** and existing package/service IDs.
  Renaming IDs can orphan preferences, scheduled activities and upgrade paths.
- Preserve Arabic/English, RTL/LTR, correct joined Arabic and exact Quranic text.
  Do not add tutorial copy where the controls already communicate the action.
- Reuse `afnon-mihrab/tokens.json`, canonical city/method/radio/voice data and assets.
  Do not hand-edit generated files. Shared-source changes require checking consumers.
- Reuse core numeric editor/step/adjustment rules; ordinary numbers edit inline
  in the same row, with shared save/cancel semantics. Keep specialized editors and
  capability gaps explicit.
- Android-specific constraints and device workflows are in `CLAUDE.md`; its Java,
  ADB, accessibility and TCL rules do not apply to LG's JS/Activity Manager service.
- Keep credentials, local device addresses, pairing keys, logs, backups and captures
  under ignored `state/` or local tool configuration. Never publish `local.properties`,
  private keys, APK/IPK outputs, downloaded adhan audio or generated speech recordings.
- Runtime network access stays limited to selected radio and manual advisory
  prayer comparison; preserve the installer's separate documented setup/download
  flow. No analytics, ads, feeds or new network dependencies. Preserve local settings on
  upgrades; do not reset a user's prayer times, input, theme or startup preferences.
- Distinguish browser mocks, live DOM/service diagnostics and human picture/sound
  confirmation. A desktop screenshot is not a TV screenshot.
- Use a feature branch and PR into this project's `main`; preserve unrelated work.
  A PR request does not authorize merging it, publishing a release or changing firmware.

## Verification and delivery

- Follow the scope matrix in `TESTING.md`; shared changes require affected consumer
  checks as well as core checks. Add focused behavioral regressions for changed
  rules/bugs. Documentation edits need link/path/claim checks, not device tests.
- Core, preview and LG generators share JS outputs: run builds sequentially.
- Hardware-sensitive changes retain their physical-TV gate. Never describe a
  mock, emulator, injected key or DOM read as human picture/sound/remote acceptance.
- Use `.github/pull_request_template.md` to record commands, outcomes, evidence
  type and remaining limits. Do not weaken checks or hide gaps to obtain a pass.
- Document user-visible changes in `CHANGELOG.md`. Record confirmed LG behavior and
  limitations in `platforms/webos/README.md`; historical research is not a current support matrix.
- Record only concise durable outcomes; keep raw evidence private. Fork detachment
  preparation does not authorize deleting/recreating the repository, rewriting
  history, removing attribution, merging or publishing.

## Current documentation through Context7

Use Context7 MCP for library, framework, SDK, API, CLI or cloud-service questions,
including syntax, configuration, migration, setup and platform-specific debugging.
Start with `resolve-library-id` unless the user provides an exact `/org/project` ID;
choose the best relevant source and use `query-docs` with the full question. Prefer
official/version-appropriate documentation and use official sources as a fallback
when Context7 lacks coverage. Do not use it for ordinary refactoring, business logic,
code review or general programming concepts that do not depend on platform behavior.
