# Background patterns

These editable SVG paths are shared by the browser preview. Android ships small, transparent
1920×1080 PNG masks generated from them, then tints the selected mask with the active theme and
reuses its cached background bitmap. Home renders at the app's resolution, capped at Full HD;
the settings preview downsamples the mask before decoding. This avoids parsing large ornament
vectors on the TV or stretching a thumbnail across Home. The temporary decoded mask is released
after compositing, without retaining every visited pattern in Android's drawable cache.

After editing a path, run `npm ci` and `npm run patterns` in `site/`, then `npm run build`.
The renderer uses Sharp, already installed by Astro; there is no added launcher dependency.
Commit the SVG and its generated `app/src/main/res/drawable-nodpi/bg_*.png` together.
The eight masks currently total about 245 KB. The local suite verifies their dimensions and size.
