# Islamic TV Launcher: agent instructions

For the active platform architecture migration, read
[`docs/ARCHITECTURE-QUEUE.md`](docs/ARCHITECTURE-QUEUE.md) first. It is the
authoritative approved scope, ordered queue, current cursor and evidence ledger.
Continue its first actionable item sequentially; companion work is deferred.

Read `README.md` and `CONTRIBUTING.md` before larger changes. This fork contains
separate Android and LG webOS applications, not interchangeable build targets.

## Repository map

- `app/`: native Android TV launcher (`tv.afnon.home`), Java and Android Views.
- `installer/`: Android phone installer and recovery workflow; Android targets only.
- `site/`: Astro browser preview and shared-data/token extraction tools.
- `afnon-mihrab/`: shared design tokens, patterns and design references.
- `webos/`: LG application (`tv.afnon.webos`) and JS service
  (`tv.afnon.webos.service`). Read `webos/AGENTS.md` before changing this port.
- `tools/webos-background-probe/`: historical, bounded hardware experiments;
  not the installed launcher or its production service.

The LG port is a normal folder in this repo, not a Git submodule. Its build imports
Android data/assets and site tooling. Android UI code is not shared with the LG UI;
changes to one renderer do not automatically update the other. Tizen is research
only; do not claim a working Samsung port.

## Shared rules

- Keep the visible name **Islamic TV Launcher** and existing package/service IDs.
  Renaming IDs can orphan preferences, scheduled activities and upgrade paths.
- Use Android's existing screens as the parity reference. Preserve Arabic/English,
  RTL/LTR, correct joined Arabic and exact Quranic text. Do not add tutorial copy
  where the controls already communicate the action.
- Reuse `afnon-mihrab/tokens.json`, canonical city/method/radio/voice data and assets.
  Do not hand-edit generated files. Shared-source changes require checking consumers.
- Android-specific constraints and device workflows are in `CLAUDE.md`; its Java,
  ADB, accessibility and TCL rules do not apply to LG's JS/Activity Manager service.
- Keep credentials, local device addresses, pairing keys, logs, backups and captures
  under ignored `state/` or local tool configuration. Never publish `local.properties`,
  private keys, APK/IPK outputs, downloaded adhan audio or generated speech recordings.
- No analytics, ads, feeds or new network dependencies. Preserve local settings on
  upgrades; do not reset a user's prayer times, input, theme or startup preferences.
- Distinguish browser mocks, live DOM/service diagnostics and human picture/sound
  confirmation. A desktop screenshot is not a TV screenshot.
- Use a feature branch and PR into this fork's `main`; preserve unrelated work.
  A PR request does not authorize merging it, publishing a release or changing firmware.

## Checks by scope

- Android: follow `CONTRIBUTING.md`, Android workflows and the affected regression
  tests under `tests/`; hardware-sensitive changes need the appropriate TV checks.
- Preview/shared data: `npm --prefix site test`; `npm --prefix site run tokens:check`
  when tokens or generated Android resources are involved.
- LG: follow `webos/AGENTS.md` for media prerequisites, ES5 checks and hardware limits.
- Document user-visible changes in `CHANGELOG.md`. Record confirmed LG behavior and
  limitations in `webos/README.md`; historical research is not a current support matrix.

## Current documentation through Context7

Use Context7 MCP for library, framework, SDK, API, CLI or cloud-service questions,
including syntax, configuration, migration, setup and platform-specific debugging.
Start with `resolve-library-id` unless the user provides an exact `/org/project` ID;
choose the best relevant source and use `query-docs` with the full question. Prefer
official/version-appropriate documentation and use official sources as a fallback
when Context7 lacks coverage. Do not use it for ordinary refactoring, business logic,
code review or general programming concepts that do not depend on platform behavior.
