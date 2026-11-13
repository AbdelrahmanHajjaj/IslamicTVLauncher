# Architecture migration queue

Updated: 2026-11-13 (Africa/Cairo). Branch: `feature/platform-core`.

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

- Old LG **0.2.17**, source `f49a67d`, is installed and verified. HDMI adhan keeps
  video moving behind the central mihrab while suppressing programme sound;
  existing native-volume ownership and input restoration remain intact.
- Owner accepted the normal 12-second installed test. The artificial 2-second
  diagnostic was too short to establish audibility; it was not a playback defect.
- All 31 preferences, selected/current input, complete startup, boot and master
  volume/mute are preserved. No active test/audio owner; diagnostics/emulator closed.
- Online HTTPS, shared navigation/sizing, Arabic clipping fixes, Home layout,
  patterned previews and startup/power checks are accepted on the old LG.
- Other HDMI ports and newer LG/Samsung/Sony hardware are unverified. This is a
  support limit, not approval to add speculative platform scaffolding.

## Next architecture action

When Android TV hardware is available, follow `CLAUDE.md`/`CONTRIBUTING.md`:

1. Capture current settings/device state; perform a preserving in-place upgrade.
2. Check both languages, joined Arabic, global sizing, remote focus/return routes.
3. Check online comparison, prayer/reminder/media, startup and lifecycle on the TV.
4. Record the concise result and essential evidence. Close the goal only when
   this required acceptance passes, or the owner explicitly changes its scope.

Until then, do not repeat unavailable-device checks or invent more refactor work.

## Separate authorized follow-up

**Recording cleanup:** all 14 adhan recordings and 12 spoken clips decoded and
screened. Seven confirmed site-credit endings removed losslessly; originals stay
outside packaged assets. The owner deferred the one-minute review of four
inconclusive endings to tomorrow; preserve those tracks. Findings/checks live in
[AUDIO-AUDIT.md](AUDIO-AUDIT.md).
Shared preparation/build guards and LG 0.2.18 are ready locally. **Leave the TV
unchanged until 2026-11-14** for fresh state capture and watched in-place upgrade.
This follow-up does not extend the architecture goal.

## Deferred and guarded

Companion Android/iOS apps, LAN pairing/control, accounts/cloud, remote updates
and Samsung implementation are deferred. Sony Android/Google TV uses Android;
other Sony targets need identification. No new feeds/analytics/ads/frameworks.
No publishing, push/PR/merge/release, remote detachment, firmware or root changes.
Preserve private device state/addresses/keys/media under ignored local state.
