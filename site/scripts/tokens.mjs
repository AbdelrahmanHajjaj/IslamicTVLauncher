// Builds every design-token output from afnon-mihrab/tokens.json, the one source of truth:
//   app/src/main/java/tv/afnon/home/Tokens.java   (Java constants, names as the app uses them)
//   app/src/main/res/values/colors.xml            (window background etc.)
//   site/src/styles/tokens.css                    (CSS custom properties for the preview site)
// `--check` writes nothing and fails if the app's committed outputs differ (for CI).
import StyleDictionary from 'style-dictionary';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const check = process.argv.includes('--check');
const HEADER = 'Generated from afnon-mihrab/tokens.json by site/scripts/tokens.mjs. Do not edit: run `npm run tokens` in site/.';

const ext = (t) => t.$extensions?.['tv.afnon'] ?? {};
// Colours drop their group name (color.lapis-900 -> LAPIS_900); everything else keeps its full path.
const path = (t) => (t.path[0] === 'color' ? t.path.slice(1) : t.path);
const constName = (t) => ext(t).java ?? path(t).join('_').replace(/-/g, '_').toUpperCase() + (t.$type === 'duration' ? '_MS' : '');

/** "#AARRGGBB" from any CSS colour Style Dictionary resolved, as a Java int literal. */
const javaColor = (hex8android) => '0x' + hex8android.slice(1).toUpperCase();
const durationMs = (v) => {
  const m = /^([\d.]+)(ms|s|min)$/.exec(v);
  if (!m) throw new Error(`bad duration ${v}`);
  return Number(m[1]) * { ms: 1, s: 1000, min: 60000 }[m[2]];
};

function javaLine(t) {
  const v = t.$value;
  switch (t.$type) {
    case 'color':
      return `static final int ${constName(t)} = ${javaColor(v)};`;
    case 'dimension':
      return `static final float ${constName(t)} = ${parseFloat(v)}f;`;
    case 'number':
      return `static final float ${constName(t)} = ${v}f;`;
    case 'duration': {
      const ms = durationMs(v);
      if (ext(t).unit === 'min') return `static final int ${constName(t)} = ${ms / 60000};`;
      return `static final long ${constName(t)} = ${ms};`;
    }
    default:
      return null; // fontFamily, cubicBezier: not used as Java constants
  }
}

StyleDictionary.registerFormat({
  name: 'afnon/java',
  format: ({ dictionary }) => {
    const seen = new Set();
    const body = dictionary.allTokens
      .map((t) => {
        const line = javaLine(t);
        if (!line) return null;
        if (seen.has(constName(t))) throw new Error(`duplicate Java name ${constName(t)}`);
        seen.add(constName(t));
        const doc = `${t.path.join('.')}${t.$description ? ': ' + t.$description : ''}`;
        return `    /** ${doc.replace(/\*\//g, '* /')} */\n    ${line}`;
      })
      .filter(Boolean)
      .join('\n');
    return `package tv.afnon.home;

// ${HEADER}

/** Design tokens in px at 1920×1080. Multiply lengths by {@link Ui#px}. */
final class Tokens {
    private Tokens() {}

${body}
}
`;
  },
});

StyleDictionary.registerFormat({
  name: 'afnon/palette',
  format: ({ dictionary }) => {
    const colors = dictionary.allTokens.filter(t => t.path[0] === 'color');
    const fields = colors.map(t => `    static int ${constName(t)} = Tokens.${constName(t)};`).join('\n');
    const assign = colors.map(t => `        ${constName(t)} = Appearance.tint(Tokens.${constName(t)}, colors);`).join('\n');
    return `package tv.afnon.home;\n\n// ${HEADER}\nfinal class Palette {\n${fields}\n    static void apply(java.util.Map<Integer, Integer> colors) {\n${assign}\n    }\n}\n`;
  },
});

StyleDictionary.registerFormat({
  name: 'afnon/android-colors',
  format: ({ dictionary }) => {
    const rows = dictionary.allTokens
      .filter((t) => t.path[0] === 'color')
      .map((t) => `    <color name="${path(t).join('_').replace(/-/g, '_')}">${t.$value}</color>`)
      .join('\n');
    return `<?xml version="1.0" encoding="utf-8"?>\n<!-- ${HEADER} -->\n<resources>\n${rows}\n</resources>\n`;
  },
});

const sd = new StyleDictionary({
  source: [resolve(root, 'afnon-mihrab/tokens.json')],
  log: { verbosity: 'silent', warnings: 'error' },
  platforms: {
    css: {
      transforms: ['attribute/cti', 'name/kebab', 'color/css', 'fontFamily/css', 'cubicBezier/css'],
      buildPath: resolve(root, 'site/src/styles') + '/',
      files: [{ destination: 'tokens.css', format: 'css/variables', options: { outputReferences: true, fileHeader: () => [HEADER] } }],
    },
    java: {
      transforms: ['attribute/cti', 'name/constant', 'color/hex8android'],
      buildPath: resolve(root, 'app/src/main/java/tv/afnon/home') + '/',
      files: [{ destination: 'Tokens.java', format: 'afnon/java' }, { destination: 'Palette.java', format: 'afnon/palette' }],
    },
    android: {
      transforms: ['attribute/cti', 'name/snake', 'color/hex8android'],
      buildPath: resolve(root, 'app/src/main/res/values') + '/',
      files: [{ destination: 'colors.xml', format: 'afnon/android-colors' }],
    },
  },
});

let stale = [];
// The CSS is gitignored and rebuilt with the site, so --check covers only the app's committed files.
for (const platform of check ? ['java', 'android'] : ['css', 'java', 'android']) {
  for (const { output, destination } of await sd.formatPlatform(platform)) {
    let old = null;
    try { old = readFileSync(destination, 'utf8'); } catch {}
    if (old === output) continue;
    if (check) { stale.push(destination); continue; }
    mkdirSync(dirname(destination), { recursive: true });
    writeFileSync(destination, output);
    console.log('wrote', destination.slice(root.length + 1));
  }
}
if (stale.length) {
  console.error('Out of date with tokens.json (run `npm run tokens` in site/):\n  ' + stale.join('\n  '));
  process.exit(1);
}
