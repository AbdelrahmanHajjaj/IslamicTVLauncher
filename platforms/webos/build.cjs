const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const babel = require('@babel/core');
const acorn = require('acorn');
const adhanRoot = path.resolve(path.dirname(require.resolve('adhan')), '../..');
const root = path.resolve(__dirname, '../..');
const out = path.join(__dirname, 'build');
const read = p => fs.readFileSync(path.join(root, p), 'utf8');
const json = p => JSON.parse(read(p));
const methods = json('shared/data/methods.json');
// Keep the old web extractor's compact literal spacing.
const compact = value => Array.isArray(value) ? value.map(text => text.replace(/\s+/g, ' ')) : value.replace(/\s+/g, ' ');
const strings = Object.fromEntries(Object.entries(json('shared/data/strings.json')).map(([key, entry]) => [key, {...entry, ar: compact(entry.ar), en: compact(entry.en)}]));
const write = (p, text) => { fs.mkdirSync(path.dirname(p), { recursive: true }); fs.writeFileSync(p, text); };
execFileSync(process.execPath, [path.join(root, 'tools/validate-shared.mjs')], { stdio: 'inherit' });
execFileSync(process.execPath, [path.join(root, 'shared/core/build-js.cjs')], { stdio: 'inherit' });
const outputs = ['app', 'service'].map(part => path.join(out, part));
for (const directory of [out, ...outputs]) {
  if (fs.existsSync(directory) && fs.realpathSync(directory) !== directory) throw new Error('Build output must stay inside the project');
}
for (const directory of outputs) fs.rmSync(directory, {recursive: true, force: true});
for (const part of ['app', 'service']) fs.cpSync(path.join(__dirname, part), path.join(out, part), { recursive: true });
const data = {
  cities: json('shared/data/cities.json'), methods: methods.map(method => method.id),
  presets: Object.fromEntries(methods.filter(method => method.preset).map(method => [method.id, {...method.preset, ...(method.methodAdjustments ? {methodAdjustments: method.methodAdjustments} : {})}])),
  strings,
  formatText: json('shared/data/format-text.json'),
  sacred: json('shared/data/sacred-text.json'),
  stations: json('shared/data/stations.json'), apps: JSON.parse(fs.readFileSync(path.join(__dirname, 'tv-apps.json'), 'utf8')),
  voices: json('shared/data/voices.json').map(({id, ar, en}) => ({id, ar, en})), themes: [], hijri: []
};
for (const voice of data.voices) {
  write(path.join(out, 'app/audio/adhan', voice.id + '.mp3'), fs.readFileSync(path.join(root, 'state/media/adhan', voice.id + '.mp3')));
}
for (const prayer of ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha', 'jumuah']) {
  for (const kind of ['before', 'now']) {
    const name = kind + '_' + prayer + '.mp3';
    write(path.join(out, 'app/audio/announce', name), fs.readFileSync(path.join(root, 'state/media/announce', name)));
  }
}
data.themes = json('shared/design/themes.json').filter(id => id !== 'custom');
// ICU is available at build time; ship month boundaries for the TV's pre-Intl runtime.
const calendar = new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn', {timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric'});
for (let at = Date.UTC(1999, 0, 1); at < Date.UTC(2102, 0, 1); at += 86400000) {
  const parts = Object.fromEntries(calendar.formatToParts(new Date(at)).map(p => [p.type, p.value]));
  if (+parts.day === 1) data.hijri.push([at / 86400000, +parts.year, +parts.month]);
}
data.methodNames = Object.fromEntries(methods.map(method => [method.id, {ar: method.ar, en: method.en}]));
data.icons = json('shared/design/icons.json');
const appIds = new Set();
for (const app of data.apps) {
  if (!/^[a-z0-9][a-z0-9._-]{0,127}$/.test(app.id) || appIds.has(app.id) || !app.ar || !app.en || !data.icons[app.icon]) throw new Error('Invalid TV app shortcut');
  appIds.add(app.id);
}
const tokens = json('afnon-mihrab/tokens.json');
data.palettes = {};
for (const theme of data.themes) {
  const colors = theme === 'mihrab' ? {} : json('shared/design/assets/themes/' + theme + '.json').colors;
  data.palettes[theme] = {};
  for (const key of ['lapis-900', 'lapis-800', 'lapis-700', 'gold-400', 'plaster-100', 'mist-400']) {
    data.palettes[theme][key] = colors[key] || tokens.color[key].$value;
  }
}
data.patterns = json('shared/design/appearance.json').patterns.slice().sort();
write(path.join(out, 'app/data.js'), 'var AfnonData = ' + JSON.stringify(data) + ';\n');
write(path.join(out, 'service/data.json'), JSON.stringify(data));
for (const part of ['app', 'service']) fs.copyFileSync(path.join(__dirname, 'core.js'), path.join(out, part, 'core.js'));
for (const part of ['app', 'service']) {
  for (const name of ['kotlin-kotlin-stdlib.js', 'IslamicTVLauncher-core.js']) {
    fs.copyFileSync(path.join(root, 'shared/core/build/compileSync/js/main/productionExecutable/kotlin', name), path.join(out, part, name));
  }
  fs.cpSync(path.join(root, 'shared/core/assets/licenses'), path.join(out, part, 'licenses'), {recursive: true});
}
const source = fs.readFileSync(path.join(adhanRoot, 'lib/bundles/adhan.umd.js'), 'utf8');
const compiled = babel.transformSync(source, {
  configFile: false, babelrc: false, comments: false,
  presets: [[require.resolve('@babel/preset-env'), { targets: { safari: '7' }, modules: false }]]
}).code;
for (const part of ['app', 'service']) {
  write(path.join(out, part, 'adhan.js'), '/* adhan-js 4.4.6, MIT; see licenses/adhan.txt */\n' + compiled);
  write(path.join(out, part, 'compat.js'), fs.readFileSync(path.join(__dirname, 'app/compat.js'), 'utf8'));
  write(path.join(out, part, 'licenses/adhan.txt'), fs.readFileSync(path.join(adhanRoot, 'LICENSE'), 'utf8'));
}
for (const theme of data.themes) {
  const colors = theme === 'mihrab' ? {} : json('shared/design/assets/themes/' + theme + '.json').colors;
  const css = fs.readFileSync(path.join(__dirname, 'app/style.css'), 'utf8').replace(/\{\{([\w.-]+)\}\}/g, (_, key) => {
    if (key.startsWith('color.') && colors[key.slice(6)]) return colors[key.slice(6)];
    let value = tokens;
    for (const part of key.split('.')) value = value[part];
    return value.$value;
  });
  const patterns = data.patterns.map(name => '[data-pattern=' + name + '] .home { background-image: url("patterns/' + name + '.svg"); background-size: 960px 540px; }').join('\n');
  write(path.join(out, 'app', theme === 'mihrab' ? 'style.css' : 'theme-' + theme + '.css'), css + '\n' + patterns);
}
for (const name of data.patterns) {
  write(path.join(out, 'app/patterns', name + '.svg'), read('afnon-mihrab/patterns/' + name + '.svg'));
}
for (const font of ['alexandria_regular', 'amiri_regular', 'marcellus_regular']) {
  write(path.join(out, 'app/fonts', font + '.ttf'), fs.readFileSync(path.join(root, 'shared/design/resources/font', font + '.ttf')));
}
write(path.join(out, 'app/licenses/adhan.txt'), fs.readFileSync(path.join(adhanRoot, 'LICENSE'), 'utf8'));
write(path.join(out, 'app/licenses/media.txt'), read('THIRD_PARTY.md'));
fs.cpSync(path.join(root, 'shared/design/assets/licenses'), path.join(out, 'app/licenses'), { recursive: true });
for (const part of ['app', 'service']) {
  for (const name of fs.readdirSync(path.join(out, part)).filter(n => n.endsWith('.js'))) {
    acorn.parse(fs.readFileSync(path.join(out, part, name), 'utf8'), { ecmaVersion: 5 });
  }
}
console.log('Built platforms/webos/build: ES5 app + service, shared Afnon data, tokens and fonts.');
