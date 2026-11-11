// Reads remaining native data/assets for the preview; shared catalogs are imported directly.
//   Shared catalogs and text come directly from shared/data/.
//   shared/design/resources/font/*.ttf -> src/assets/fonts/
//   shared/design/assets/licenses/ -> public/licenses/
//   docs/screenshots -> public/screenshots/ (social preview images)
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateCatalog, validateStations, validateVoices, validateThemes } from '../../tools/catalogs.mjs';
import { validateStrings, validateFormatText, validateSacredText } from '../../tools/strings.mjs';
import { validateIcons, validatePrayerIcons } from '../../tools/design.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const out = resolve(root, 'site/src/data');
mkdirSync(out, { recursive: true });
const write = (name, data) => {
  writeFileSync(resolve(out, name), JSON.stringify(data, null, 2) + '\n');
  console.log(`wrote site/src/data/${name}`);
};

// ---------------------------------------------------------------- Shared catalog validation

validateCatalog(
  JSON.parse(readFileSync(resolve(root, 'shared/data/cities.json'), 'utf8')),
  JSON.parse(readFileSync(resolve(root, 'shared/data/regions.json'), 'utf8')),
);

validateStations(JSON.parse(readFileSync(resolve(root, 'shared/data/stations.json'), 'utf8')));
validateVoices(JSON.parse(readFileSync(resolve(root, 'shared/data/voices.json'), 'utf8')));
validateStrings(JSON.parse(readFileSync(resolve(root, 'shared/data/strings.json'), 'utf8')));
validateFormatText(JSON.parse(readFileSync(resolve(root, 'shared/data/format-text.json'), 'utf8')));
validateSacredText(JSON.parse(readFileSync(resolve(root, 'shared/data/sacred-text.json'), 'utf8')));
validateThemes(JSON.parse(readFileSync(resolve(root, 'shared/design/themes.json'), 'utf8')));
validateIcons(JSON.parse(readFileSync(resolve(root, 'shared/design/icons.json'), 'utf8')));
validatePrayerIcons(JSON.parse(readFileSync(resolve(root, 'shared/design/prayer-icons.json'), 'utf8')));

// ---------------------------------------------------------------- shared fonts and notices

import { readdirSync, copyFileSync } from 'node:fs';
const fontsIn = resolve(root, 'shared/design/resources/font');
const fontsOut = resolve(root, 'site/src/assets/fonts');
mkdirSync(fontsOut, { recursive: true });
for (const f of readdirSync(fontsIn)) copyFileSync(resolve(fontsIn, f), resolve(fontsOut, f));
console.log('copied shared fonts to site/src/assets/fonts');
const noticesIn = resolve(root, 'shared/design/assets/licenses');
const noticesOut = resolve(root, 'site/public/licenses');
mkdirSync(noticesOut, { recursive: true });
for (const f of readdirSync(noticesIn)) copyFileSync(resolve(noticesIn, f), resolve(noticesOut, f));

// ---------------------------------------------------------------- README screenshots, for social previews

const shotsIn = resolve(root, 'docs/screenshots');
const shotsOut = resolve(root, 'site/public/screenshots');
mkdirSync(shotsOut, { recursive: true });
for (const f of readdirSync(shotsIn)) copyFileSync(resolve(shotsIn, f), resolve(shotsOut, f));
console.log('copied docs/screenshots to site/public/screenshots');

const backgroundPatterns = Object.fromEntries(['arches', 'eight_point_stars', 'mashrabiya', 'arabesque', 'star_tiles', 'rosettes', 'octagon_lattice', 'corner_medallions'].map(name => {
  const xml = readFileSync(resolve(root, `afnon-mihrab/patterns/${name}.svg`), 'utf8');
  const path = / d="([^"]+)"/.exec(xml)?.[1];
  const stroke = /stroke-width="([^"]+)"/.exec(xml)?.[1];
  if (!path || !stroke) throw new Error(`Missing background pattern: ${name}`);
  return [name, { path, stroke: Number(stroke) }];
}));
write('background-patterns.json', backgroundPatterns);
