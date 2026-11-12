# Migration regression fixtures

`method-parameters.json` freezes the 19 JavaScript adapter parameter sets before
the shared-method migration, using the installed adhan-js 4.4.6 factories. It
includes factory identity and effective method corrections, not just angles.
Change this baseline only alongside an intentional, reviewed calculation change.

`text-output-hashes.json` freezes the compiled Java output of the 174 plain text
methods before extraction. It covers Arabic/English × Western/Eastern digits,
parameter text containing digits/punctuation and null parameters. The JVM check
also verifies fresh label arrays. Preserve the exact baseline during the migration;
update it only for an intentional, reviewed text change, alongside its catalog.

`text-format-hashes.json` and `site-text-format-hashes.json` freeze the remaining
native/preview formatter behavior separately. They cover both languages/digit
modes, null/empty values, minute thresholds/plural forms, signed/extreme offsets,
radio states, method angles and compass-sector boundaries. Platform differences
are retained. The fixtures were captured from unchanged source before literal
extraction; update them only for an intentional, reviewed formatting change.

`sacred-text-hashes.json` freezes the exact UTF-8 code points of the existing
An-Nisa 4:103 verse (including its three-line layout) and adhan call. All three
renderers were compared before extraction. Any change requires an intentional
religious-text review; a visually similar character is not sufficient.

`reminder-events.json` freezes 5,190 LG next-event outputs from the original
adapter at `6fb81fa` (adhan-js 4.4.6, host UTC, fixed UTC+2 preference). It covers
automatic off/on, lead times, adhan/announcement combinations, bell masks, exact
reminder/prayer boundaries and exclusion of the first handled event. The app and
service must match it during core extraction. The owner's global downward-rounding
change intentionally updates its absolute timestamps: first the original policy
reproduced the old fixture, then the same original source with only unrounded
astronomy selected produced the new fixture and matched the current core exactly.
The reminder function itself was unchanged. Update only for an intentional,
reviewed reminder-policy or calculation-time change. Native reminder policies differ and are checked
independently by `RemindersCheck.java`.
