// Pre-render theme-tinted masks; Android never parses the full ornament paths.
import { readdir, readFile, mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';
const tokens = JSON.parse(await readFile(new URL('../../afnon-mihrab/tokens.json', import.meta.url), 'utf8'));
const width = parseFloat(tokens.background['max-width'].$value);
const height = parseFloat(tokens.background['max-height'].$value);

const source = new URL('../../afnon-mihrab/patterns/', import.meta.url);
const output = new URL('../../app/src/main/res/drawable-nodpi/', import.meta.url);
await mkdir(output, { recursive: true });
for (const name of (await readdir(source)).filter(n => n.endsWith('.svg')).sort()) {
  const svg = await readFile(new URL(name, source));
  // Render SVG paths at full resolution, rather than enlarging the old thumbnail PNG.
  await sharp(svg, { density: 72 * width / parseFloat(tokens.background['preview-width'].$value) })
    .resize(width, height).png({ compressionLevel: 9, palette: true })
    .toFile(fileURLToPath(new URL(`bg_${name.slice(0, -4)}.png`, output)));
}
