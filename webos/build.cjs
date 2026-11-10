const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const babel = require('@babel/core');
const acorn = require('acorn');
const root = path.resolve(__dirname, '..');
const out = path.join(__dirname, 'build');
const read = p => fs.readFileSync(path.join(root, p), 'utf8');
const json = p => JSON.parse(read(p));
const write = (p, text) => { fs.mkdirSync(path.dirname(p), { recursive: true }); fs.writeFileSync(p, text); };
execFileSync(process.execPath, [path.join(root, 'site/scripts/extract.mjs')], { stdio: 'ignore' });
for (const part of ['app', 'service']) fs.cpSync(path.join(__dirname, part), path.join(out, part), { recursive: true });
const data = {
  cities: json('site/src/data/cities.json'), methods: json('site/src/data/methods.json'),
  presets: json('site/src/data/method-presets.json'), strings: json('site/src/data/strings.json'),
  stations: json('site/src/data/stations.json'), apps: json('webos/tv-apps.json'), voices: [], themes: [], hijri: []
};
for (const match of read('app/src/main/java/tv/afnon/home/Adhans.java').matchAll(/\{"(\w+)", "([^"]+)", "([^"]+)"\}/g)) {
  data.voices.push({id: match[1], ar: match[2], en: match[3]});
  write(path.join(out, 'app/audio/adhan', match[1] + '.mp3'), fs.readFileSync(path.join(root, 'app/src/main/assets/adhan', match[1] + '.mp3')));
}
if (data.voices.length !== 14) throw new Error('Adhan voice extraction changed');
for (const prayer of ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha', 'jumuah']) {
  for (const kind of ['before', 'now']) {
    const name = kind + '_' + prayer + '.mp3';
    write(path.join(out, 'app/audio/announce', name), fs.readFileSync(path.join(root, 'app/src/main/assets/announce', name)));
  }
}
data.themes = JSON.parse('[' + /PRESETS = \{([^}]+)\}/.exec(read('app/src/main/java/tv/afnon/home/Appearance.java'))[1] + ']').filter(id => id !== 'custom');
// ICU is available at build time; ship month boundaries for the TV's pre-Intl runtime.
const calendar = new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn', {timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric'});
for (let at = Date.UTC(1999, 0, 1); at < Date.UTC(2102, 0, 1); at += 86400000) {
  const parts = Object.fromEntries(calendar.formatToParts(new Date(at)).map(p => [p.type, p.value]));
  if (+parts.day === 1) data.hijri.push([at / 86400000, +parts.year, +parts.month]);
}
const methodSource = read('app/src/main/java/tv/afnon/home/Text.java').split('String methodName(')[1].split('/**')[0];
data.methodNames = {};
for (const match of methodSource.matchAll(/(?:case (\w+)|default): return ar \? ("[^"]*") : ("[^"]*");/g)) {
  data.methodNames[match[1] || 'EGYPTIAN'] = { ar: JSON.parse(match[2]), en: JSON.parse(match[3]) };
}
if (data.methods.some(key => !data.methodNames[key])) throw new Error('Missing prayer method translation');
data.icons = {};
for (const match of read('site/src/components/Icon.astro').matchAll(/^  (\w+): '([^']+)'/gm)) {
  data.icons[match[1]] = match[2];
}
const appIds = new Set();
for (const app of data.apps) {
  if (!/^[a-z0-9][a-z0-9._-]{0,127}$/.test(app.id) || appIds.has(app.id) || !app.ar || !app.en || !data.icons[app.icon]) throw new Error('Invalid TV app shortcut');
  appIds.add(app.id);
}
const tokens = json('afnon-mihrab/tokens.json');
data.palettes = {};
for (const theme of data.themes) {
  const colors = theme === 'mihrab' ? {} : json('app/src/main/assets/themes/' + theme + '.json').colors;
  data.palettes[theme] = {};
  for (const key of ['lapis-900', 'lapis-800', 'lapis-700', 'gold-400', 'plaster-100', 'mist-400']) {
    data.palettes[theme][key] = colors[key] || tokens.color[key].$value;
  }
}
data.patterns = fs.readdirSync(path.join(root, 'afnon-mihrab/patterns')).filter(n => n.endsWith('.svg')).map(n => n.slice(0, -4));
write(path.join(out, 'app/data.js'), 'var AfnonData = ' + JSON.stringify(data) + ';\n');
write(path.join(out, 'service/data.json'), JSON.stringify(data));
for (const part of ['app', 'service']) fs.copyFileSync(path.join(__dirname, 'core.js'), path.join(out, part, 'core.js'));
const source = read('site/node_modules/adhan/lib/bundles/adhan.umd.js');
const compiled = babel.transformSync(source, {
  configFile: false, babelrc: false, comments: false,
  presets: [[require.resolve('@babel/preset-env'), { targets: { safari: '7' }, modules: false }]]
}).code;
for (const part of ['app', 'service']) {
  write(path.join(out, part, 'adhan.js'), '/* adhan-js 4.4.6, MIT; see licenses/adhan.txt */\n' + compiled);
  write(path.join(out, part, 'compat.js'), read('webos/app/compat.js'));
  write(path.join(out, part, 'licenses/adhan.txt'), read('site/node_modules/adhan/LICENSE'));
}
for (const theme of data.themes) {
  const colors = theme === 'mihrab' ? {} : json('app/src/main/assets/themes/' + theme + '.json').colors;
  const css = read('webos/app/style.css').replace(/\{\{([\w.-]+)\}\}/g, (_, key) => {
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
  write(path.join(out, 'app/fonts', font + '.ttf'), fs.readFileSync(path.join(root, 'app/src/main/res/font', font + '.ttf')));
}
write(path.join(out, 'app/licenses/adhan.txt'), read('site/node_modules/adhan/LICENSE'));
write(path.join(out, 'app/licenses/media.txt'), read('THIRD_PARTY.md'));
fs.cpSync(path.join(__dirname, 'licenses'), path.join(out, 'app/licenses'), { recursive: true });
for (const part of ['app', 'service']) {
  for (const name of fs.readdirSync(path.join(out, part)).filter(n => n.endsWith('.js'))) {
    acorn.parse(fs.readFileSync(path.join(out, part, name), 'utf8'), { ecmaVersion: 5 });
  }
}
console.log('Built webos/build: ES5 app + service, shared Afnon data, tokens and fonts.');
