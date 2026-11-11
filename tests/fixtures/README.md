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
