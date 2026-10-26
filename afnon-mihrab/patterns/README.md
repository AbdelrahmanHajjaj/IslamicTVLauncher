# Background patterns

These editable SVG paths are shared by the browser preview. Android ships small, transparent
480×270 PNG masks generated from them, then tints each mask with the active theme and reuses its
cached background bitmap. This avoids parsing large ornament vectors on the TV.

After editing a path, run `npm ci` and `npm run patterns` in `site/`, then `npm run build`.
The renderer uses Sharp, already installed by Astro; there is no added launcher dependency.
Commit the SVG and its generated `app/src/main/res/drawable-nodpi/bg_*.png` together.
The eight masks currently total about 60 KB. The local suite verifies their dimensions and size.
