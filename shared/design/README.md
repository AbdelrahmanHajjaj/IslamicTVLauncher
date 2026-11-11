# Shared design resources

`resources/font/` owns the seven unchanged Alexandria, Amiri and Marcellus TTFs.
`assets/licenses/` owns their existing full OFL notices. Keep the filenames, bytes
and notices together; see the root `THIRD_PARTY.md` for attribution.

Android adds these resource/asset directories to its main source set, retaining
the existing `R.font` names with no generated font copies or Node prerequisite.
The preview copies fonts and notices during generation; LG reads them directly.
Only the three existing regular fonts are bundled by LG, as before. Font choice,
weights, Arabic shaping, CSS and native rendering remain in their adapters.

Tokens and SVG patterns still come from `afnon-mihrab/`; do not duplicate or
hand-edit their generated Java/XML/CSS outputs. Remaining asset extraction and
the migration queue are recorded only in `docs/ARCHITECTURE-QUEUE.md`.
