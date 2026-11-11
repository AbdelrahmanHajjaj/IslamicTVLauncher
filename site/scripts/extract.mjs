// Projects shared resources for the preview; catalogs are imported directly.
//   Shared catalogs and text come directly from shared/data/.
//   shared/design/resources/font/*.ttf -> src/assets/fonts/
//   shared/{design,core}/assets/licenses/ -> public/licenses/
//   docs/screenshots -> public/screenshots/ (social preview images)
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateShared } from '../../tools/validate-shared.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
validateShared();
const out = resolve(root, 'site/src/data');
mkdirSync(out, { recursive: true });
const write = (name, data) => {
  writeFileSync(resolve(out, name), JSON.stringify(data, null, 2) + '\n');
  console.log(`wrote site/src/data/${name}`);
};

const appearance = JSON.parse(readFileSync(resolve(root, 'shared/design/appearance.json'), 'utf8'));

// ---------------------------------------------------------------- shared fonts and notices

import { readdirSync, copyFileSync, cpSync } from 'node:fs';
const fontsIn = resolve(root, 'shared/design/resources/font');
const fontsOut = resolve(root, 'site/src/assets/fonts');
mkdirSync(fontsOut, { recursive: true });
for (const f of readdirSync(fontsIn)) copyFileSync(resolve(fontsIn, f), resolve(fontsOut, f));
console.log('copied shared fonts to site/src/assets/fonts');
const noticesIn = resolve(root, 'shared/design/assets/licenses');
const noticesOut = resolve(root, 'site/public/licenses');
mkdirSync(noticesOut, { recursive: true });
for (const f of readdirSync(noticesIn)) copyFileSync(resolve(noticesIn, f), resolve(noticesOut, f));
cpSync(resolve(root, 'shared/core/assets/licenses'), noticesOut, {recursive: true});

// ---------------------------------------------------------------- README screenshots, for social previews

const shotsIn = resolve(root, 'docs/screenshots');
const shotsOut = resolve(root, 'site/public/screenshots');
mkdirSync(shotsOut, { recursive: true });
for (const f of readdirSync(shotsIn)) copyFileSync(resolve(shotsIn, f), resolve(shotsOut, f));
console.log('copied docs/screenshots to site/public/screenshots');

const backgroundPatterns = Object.fromEntries(appearance.patterns.map(name => {
  const xml = readFileSync(resolve(root, `afnon-mihrab/patterns/${name}.svg`), 'utf8');
  const path = / d="([^"]+)"/.exec(xml)?.[1];
  const stroke = /stroke-width="([^"]+)"/.exec(xml)?.[1];
  if (!path || !stroke) throw new Error(`Missing background pattern: ${name}`);
  return [name, { path, stroke: Number(stroke) }];
}));
write('background-patterns.json', backgroundPatterns);
