# Architecture migration queue

Updated: 2026-11-14 (Africa/Cairo). Branch: `feature/platform-core`.

## Authority and maintenance

This file owns approved scope, status and the next action. Read it first; work
sequentially and preserve unrelated changes. Keep it under 120 lines: replace
completed progress with its outcome, never append tool-by-tool notes.
[ARCHITECTURE-EVIDENCE.md](ARCHITECTURE-EVIDENCE.md) holds essential verification
facts, not a second queue. Raw logs/backups/media stay in ignored `state/`.
Superseded investigations remain in Git history.

## Goal status

**Implementation complete; goal blocked only on Android TV hardware acceptance.**
Core extraction, independent projects, canonical resources, shared UI/input rules,
automated checks and available old-LG acceptance are complete. Q00–Q08 are done;
Q09/Q09.5 await the Android TV, expected around **2026-11-25**. Emulator evidence
is recorded but does not close this physical-TV gate. Full adhan was previously
tested, as the owner confirms; do not reopen that gate without a new failure.

## Approved scope

- One household product: portable Kotlin JVM/ES5 core, canonical data/design,
  native Android Views, separate LG renderer/service and browser demonstration.
- Core owns product rules and semantic UI/input contracts; shells own rendering,
  persistence, astronomy/calendar adapters, networking, media and OS integration.
- Keep package IDs, preferences, Arabic/English, RTL/LTR, exact religious text,
  original MIT/third-party notices and this fork's ownership/download targets.
- Universal controls remain defined. Missing implementations and OS limitations
  have explicit gaps; initially hide unsupported/unimplemented LG controls.
- One global inline size control: 80–150%, steps of 10, default 100. Shared Home
  dimensions/typography and focus rules remain canonical; video is unscaled.
- Prayer times round down to the calculated minute. Manual online comparison is
  advisory; offline calculations remain the schedule authority.

## Current checkpoint

- Old LG **0.2.20**, source `469885c`, is installed; all 103 files match the candidate.
  Node/WebKit each pass 93 core checks; Node also passes 30 adapter checks. HDMI adhan keeps
  video moving behind the central mihrab while suppressing programme sound;
  existing native-volume ownership and input restoration remain intact.
- Owner accepted the normal 12-second test and 0.2.19's absolute 30/100 level,
  restoring 50 with normal HDMI picture/sound. The same volume service is in 0.2.20.
- Owner accepted 0.2.20 picture/sound, physical inline adjustment/cancellation,
  and complete cleaned endings without advertising, with normal restoration.
- All 31 preferences, selected/current input, complete startup, boot and master
  volume/mute are preserved. No active test/audio owner; inspector/emulator closed.
- Online HTTPS, shared navigation/sizing, Arabic clipping fixes, Home layout,
  patterned previews and startup/power checks are accepted on the old LG.
- Other HDMI ports and newer LG/Samsung/Sony hardware are unverified. This is a
  support limit, not approval to add speculative platform scaffolding.

## Next architecture action

When Android TV hardware is available, follow `CLAUDE.md`/`CONTRIBUTING.md`:

1. Capture current settings/device state; perform a preserving in-place upgrade.
2. Check both languages, joined Arabic, inline numeric controls, sizing and focus.
3. Check online comparison, prayer/reminder/media, startup and lifecycle on the TV.
4. Record the concise result and essential evidence. Close the goal only when
   this required acceptance passes, or the owner explicitly changes its scope.

Until then, do not repeat unavailable-device checks or invent more refactor work.

## Completed follow-up

**Recording cleanup:** all 14 adhan recordings and 12 spoken clips decoded and
screened. Seven confirmed site-credit endings removed losslessly; originals stay
outside packaged assets. The owner accepted the other four endings unchanged.
Shared numeric editor/step/adjustment rules now drive Android, LG and preview.
All supported LG numbers edit inline; the bearing card and platform gaps remain
explicit. Core, LG browser/service, preview, Android build/lint and 40 regressions
pass. 0.2.20 preserves all 31 current preferences (25%, both voices Al-Menshawy),
HDMI, startup, boot and volume/mute. Actual inline save 25→30→25 changes only
volume. Cleaned Al-Qatami/Chebae previews reach natural EOF at native 25 and restore
HDMI/50. The owner confirms normal picture/sound, inline edit/cancel, complete
endings without advertising and normal restoration. **This follow-up is complete.** Final state is
clean and the inspector is closed; no further LG tests are queued. Findings live in
[AUDIO-AUDIT.md](AUDIO-AUDIT.md). This does not extend the architecture goal.

## Repository preparation

Project documentation, agent rules, test requirements and PR/issue templates now
follow the implemented core/shell architecture. Historical upstream plans are
superseded. [REPOSITORY-INDEPENDENCE.md](REPOSITORY-INDEPENDENCE.md) prepares
detachment with retained history, attribution and installed update identities;
the permanent GitHub action remains unexecuted and needs its own approved route.
Documentation links, issue forms and documented commands validate. No runtime,
device, CI workflow or remote configuration changed in this follow-up.

## Deferred and guarded

Companion Android/iOS apps, LAN pairing/control, accounts/cloud, remote updates
and Samsung implementation are deferred. Sony Android/Google TV uses Android;
other Sony targets need identification. No new feeds/analytics/ads/frameworks.
No publishing, push/PR/merge/release, remote detachment, firmware or root changes.
Preserve private device state/addresses/keys/media under ignored local state.
