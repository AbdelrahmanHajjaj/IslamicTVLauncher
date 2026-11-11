# Shared design resources

`resources/font/` owns the seven unchanged Alexandria, Amiri and Marcellus TTFs.
`assets/licenses/` owns their existing full OFL notices. Keep the filenames, bytes
and notices together; see the root `THIRD_PARTY.md` for attribution.

Android adds these resource/asset directories to its main source set, retaining
the existing `R.font` names with no generated font copies or Node prerequisite.
The preview copies fonts and notices during generation; LG reads them directly.
Only the three existing regular fonts are bundled by LG, as before. Font choice,
weights, Arabic shaping, CSS and native rendering remain in their adapters.

`themes.json` owns the exact ordered IDs: 20 built-ins followed by Android's reserved
`custom` import slot. `assets/themes/` owns the 19 unchanged preset files; Mihrab
uses the existing design tokens. Android packages them as `assets/themes/<id>.json`
and uses generated `ThemeCatalog.PRESETS` through the existing Appearance API.
Preview/LG filter out `custom`, keeping their existing feature sets. Palette,
surface, background and lighting behavior stays in each adapter.

`icons.json` owns the 26 existing SVG fragments used by the preview and LG.
`prayer-icons.json` owns the six ordered prayer paths, their 24-unit grid and
1.7-unit stroke. The catalog generator emits Android's existing vector resource
names; preview labels read the same geometry. `npm --prefix site run data:check`
checks the committed vectors without writing. SVG fragments allow only local
geometric shapes/attributes, with no active markup or external references.

Android's procedural `Icon` canvas renderer stays native, including its existing
RTL, arc/fill and geometry differences. `PrayerIcon` keeps native sizing, tint,
baseline and accessibility behavior. Web stroke/focus rendering also stays in
its adapters; this resource extraction does not replace native drawing APIs.

Tokens and SVG patterns still come from `afnon-mihrab/`; do not duplicate or
hand-edit their generated Java/XML/CSS outputs. Remaining asset extraction and
the migration queue are recorded only in `docs/ARCHITECTURE-QUEUE.md`.
