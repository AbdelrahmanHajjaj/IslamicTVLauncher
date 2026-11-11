# Shared design resources

`resources/font/` owns the seven unchanged Alexandria, Amiri and Marcellus TTFs.
`assets/licenses/` owns their existing full OFL notices. Keep the filenames, bytes
and notices together; see the root `THIRD_PARTY.md` for attribution.

The same asset directory distributes generated `IslamicTVLauncher-MIT.txt` and
`CleanYourTV-MIT.txt`. Their canonical sources remain root `LICENSE` and
`docs/CLEANYOURTV-LICENSE.txt`; `tools/catalogs.mjs` generates/checks the exact copies
here and in installer assets. Do not edit generated license copies by hand.

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

`appearance.json` owns the eight procedural backgrounds, eight SVG pattern IDs,
seven surface IDs and eight theme-v1 color slots. Generated `ThemeCatalog` arrays
feed native `ThemeSpec`; preview uses the same background order/color keys. LG
retains its existing alphabetic SVG-pattern list and separate plain-background
choice; it does not gain Android's procedural backgrounds or surface treatments.
Contrast, import bounds and actual drawing remain in the platform adapters.

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
