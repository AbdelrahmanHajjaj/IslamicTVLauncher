// Pre-render theme-tinted masks; Android never parses the full ornament paths.
import { readdir, readFile, mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';

const source = new URL('../../afnon-mihrab/patterns/', import.meta.url);
const output = new URL('../../app/src/main/res/drawable-nodpi/', import.meta.url);
await mkdir(output, { recursive: true });
for (const name of (await readdir(source)).filter(n => n.endsWith('.svg')).sort()) {
  const svg = await readFile(new URL(name, source));
  await sharp(svg).png({ compressionLevel: 9 }).toFile(fileURLToPath(new URL(`bg_${name.slice(0, -4)}.png`, output)));
}
