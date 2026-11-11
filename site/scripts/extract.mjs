// Reads remaining native data/assets for the preview; shared catalogs are imported directly.
//   Text.java       -> src/data/strings.json   (every `ar ? … : …` string, as {ar, en} templates)
//   Cities/regions come directly from shared/data/; they are not extracted.
//   Method definitions also come directly from shared/data/.
//   Radio stations also come directly from shared/data/.
//   res/font/*.ttf  -> src/assets/fonts/
//   docs/screenshots -> public/screenshots/   (social preview images)
// Templates use {param} for the Java method's parameters; "d": true means the app passes it through d()
// (Eastern Arabic digits when that setting is on). Methods with other logic are listed as skipped and are
// written by hand in src/lib/text.ts.
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateCatalog, validateStations, validateVoices } from '../../tools/catalogs.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const src = (f) => readFileSync(resolve(root, 'app/src/main/java/tv/afnon/home', f), 'utf8');
const out = resolve(root, 'site/src/data');
mkdirSync(out, { recursive: true });
const write = (name, data) => {
  writeFileSync(resolve(out, name), JSON.stringify(data, null, 2) + '\n');
  console.log(`wrote site/src/data/${name}`);
};

const STR = String.raw`"((?:[^"\\]|\\.)*)"`;
const unescape = (s) => JSON.parse(`"${s}"`);

// ---------------------------------------------------------------- Text.java

/** `"a" + x + "b"` (params only) -> "a{x}b"; null if the expression does anything else. */
function template(expr, params) {
  let e = expr.trim();
  let d = false;
  const m = /^d\((.*)\)$/s.exec(e);
  if (m) { d = true; e = m[1].trim(); }
  const parts = [];
  const re = new RegExp(String.raw`\s*(?:${STR}|(\w+))\s*(\+|$)`, 'y');
  let pos = 0;
  while (pos < e.length) {
    re.lastIndex = pos;
    const p = re.exec(e);
    if (!p) return null;
    if (p[1] !== undefined) parts.push(unescape(p[1]));
    else if (params.includes(p[2])) parts.push(`{${p[2]}}`);
    else return null;
    pos = re.lastIndex;
    if (p[3] === '') break;
  }
  if (pos < e.length) return null;
  return { s: parts.join(''), d };
}

function splitTernary(body) {
  // body: `ar ? X : Y` with X, Y free of top-level ?: (string literals may contain ':').
  if (!body.startsWith('ar ?')) return null;
  let depth = 0, inStr = false;
  for (let i = 4; i < body.length; i++) {
    const c = body[i];
    if (inStr) { if (c === '\\') i++; else if (c === '"') inStr = false; continue; }
    if (c === '"') inStr = true;
    else if (c === '(' || c === '{') depth++;
    else if (c === ')' || c === '}') depth--;
    else if (c === '?' && depth === 0) return null;
    else if (c === ':' && depth === 0) return [body.slice(4, i).trim(), body.slice(i + 1).trim()];
  }
  return null;
}

const text = src('Text.java');
const strings = {};
const skipped = [];
const methodRe = /^ {4}(String(?:\[\])?) (\w+)\(([^)]*)\) \{\s*(?:return ([^;]*);\s*\})?/gm;
for (const m of text.matchAll(methodRe)) {
  const [, type, name, rawParams, body] = m;
  const params = rawParams.split(',').map((p) => p.trim().split(/\s+/).pop()).filter(Boolean);
  const tern = body && splitTernary(body.replace(/\s+/g, ' ').trim());
  if (!tern) { skipped.push(name); continue; }
  if (type === 'String[]') {
    const arr = (e) => [...e.matchAll(new RegExp(STR, 'g'))].map((x) => unescape(x[1]));
    if (!/^new String\[\] \{/.test(tern[0])) { skipped.push(name); continue; }
    strings[name] = { ar: arr(tern[0]), en: arr(tern[1]) };
    continue;
  }
  const ar = template(tern[0], params), en = template(tern[1], params);
  if (!ar || !en) { skipped.push(name); continue; }
  strings[name] = { ar: ar.s, en: en.s, ...(ar.d || en.d ? { d: true } : {}) };
}
write('strings.json', strings);
console.log(`  ${Object.keys(strings).length} strings; by hand in text.ts: ${skipped.join(', ')}`);

// ---------------------------------------------------------------- Shared catalog validation

validateCatalog(
  JSON.parse(readFileSync(resolve(root, 'shared/data/cities.json'), 'utf8')),
  JSON.parse(readFileSync(resolve(root, 'shared/data/regions.json'), 'utf8')),
);

validateStations(JSON.parse(readFileSync(resolve(root, 'shared/data/stations.json'), 'utf8')));
validateVoices(JSON.parse(readFileSync(resolve(root, 'shared/data/voices.json'), 'utf8')));

// ---------------------------------------------------------------- fonts (the app's own files)

import { readdirSync, copyFileSync } from 'node:fs';
const fontsIn = resolve(root, 'app/src/main/res/font');
const fontsOut = resolve(root, 'site/src/assets/fonts');
mkdirSync(fontsOut, { recursive: true });
for (const f of readdirSync(fontsIn)) copyFileSync(resolve(fontsIn, f), resolve(fontsOut, f));
console.log('copied app fonts to site/src/assets/fonts');

// ---------------------------------------------------------------- README screenshots, for social previews

const shotsIn = resolve(root, 'docs/screenshots');
const shotsOut = resolve(root, 'site/public/screenshots');
mkdirSync(shotsOut, { recursive: true });
for (const f of readdirSync(shotsIn)) copyFileSync(resolve(shotsIn, f), resolve(shotsOut, f));
console.log('copied docs/screenshots to site/public/screenshots');

const prayerIcons = ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'].map(name => {
  const xml = readFileSync(resolve(root, `app/src/main/res/drawable/ic_prayer_${name}.xml`), 'utf8');
  const path = /android:pathData="([^"]+)"/.exec(xml)?.[1];
  if (!path) throw new Error(`Missing prayer icon: ${name}`);
  return path;
});
write('prayer-icons.json', prayerIcons);

const backgroundPatterns = Object.fromEntries(['arches', 'eight_point_stars', 'mashrabiya', 'arabesque', 'star_tiles', 'rosettes', 'octagon_lattice', 'corner_medallions'].map(name => {
  const xml = readFileSync(resolve(root, `afnon-mihrab/patterns/${name}.svg`), 'utf8');
  const path = / d="([^"]+)"/.exec(xml)?.[1];
  const stroke = /stroke-width="([^"]+)"/.exec(xml)?.[1];
  if (!path || !stroke) throw new Error(`Missing background pattern: ${name}`);
  return [name, { path, stroke: Number(stroke) }];
}));
write('background-patterns.json', backgroundPatterns);
