import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { validateCatalog, validateMethods, validateStations, validateVoices, validateThemes } from './catalogs.mjs';
import { renderText, validateStrings, renderLiterals, validateFormatText, validateSacredText } from './strings.mjs';
import { validateIcons, validatePrayerIcons, validateAppearance } from './design.mjs';

const cities = JSON.parse(readFileSync(new URL('../shared/data/cities.json', import.meta.url)));
const regions = JSON.parse(readFileSync(new URL('../shared/data/regions.json', import.meta.url)));
const methods = JSON.parse(readFileSync(new URL('../shared/data/methods.json', import.meta.url)));
validateMethods(methods);
for (const change of [
  method => { method.id = '../METHOD'; },
  method => { method.ar = ''; },
  method => { method.preset = null; },
  method => { method.preset = {fajr: NaN, isha: 12, interval: 0}; },
  method => { method.preset = {fajr: 12, isha: 0, interval: 0}; },
  method => { method.preset = {fajr: 12, isha: 12, interval: -1}; },
  method => { method.methodAdjustments = [0, 1]; },
  method => { method.preset = {fajr: 12, isha: 12, interval: 0}; method.methodAdjustments = [0, 0, 0, 0, 0, 0.5]; },
]) {
  const modified = structuredClone(methods);
  change(modified[1]);
  assert.throws(() => validateMethods(modified), /Invalid method/);
}
assert.throws(() => validateMethods([...methods, methods[1]]), /Invalid method/);
assert.throws(() => validateMethods(methods.slice(1)), /fallback/);
validateCatalog(cities, regions);
const java = readFileSync(new URL('../app/src/main/java/tv/afnon/home/CityCatalog.java', import.meta.url), 'utf8');
const quoted = String.raw`("(?:[^"\\]|\\.)*")`;
const cityRows = new RegExp(String.raw`new Prefs\.City\(${Array(5).fill(quoted).join(', ')}, ([-\d.eE+]+), ([-\d.eE+]+), PrayerMethod\.([A-Z_]+), Madhab\.(HANAFI|SHAFI)\)`, 'g');
const roundTrip = [...java.matchAll(cityRows)].map(m => ({
  id: JSON.parse(m[1]), ar: JSON.parse(m[2]), en: JSON.parse(m[3]),
  countryAr: JSON.parse(m[4]), countryEn: JSON.parse(m[5]),
  lat: +m[6], lng: +m[7], method: m[8], hanafi: m[9] === 'HANAFI',
}));
assert.deepEqual(roundTrip, cities, 'Java catalog must preserve every field and city order');
const regionRows = java.split('static final String[][] REGIONS = {')[1];
assert.deepEqual([...regionRows.matchAll(/^\s*\{(.*)\},$/gm)].map(m => JSON.parse('[' + m[1] + ']')), regions);
for (const change of [
  city => { city.id = '../cairo'; },
  city => { city.lat = 91; },
  city => { city.lng = -181; },
  city => { city.lat = NaN; },
  city => { city.method = 'UNKNOWN'; },
  city => { city.hanafi = 'false'; },
  city => { city.ar = ''; },
  city => { city.countryEn = 'Missing'; },
]) {
  const modified = structuredClone(cities);
  change(modified[1]);
  assert.throws(() => validateCatalog(modified, regions), /Invalid city/);
}
assert.throws(() => validateCatalog([...cities, cities[1]], regions), /Invalid city/);
assert.throws(() => validateCatalog(cities.slice(1), regions), /fallback/);
assert.throws(() => validateCatalog(cities, [...regions, [regions[0][0]]]), /duplicate/);
assert.throws(() => validateCatalog(cities, [...regions, []]), /Empty region/);

const stations = JSON.parse(readFileSync(new URL('../shared/data/stations.json', import.meta.url)));
validateStations(stations);
for (const change of [
  station => { station.id = '../radio'; },
  station => { station.ar = ''; },
  station => { station.whereEn = ''; },
  station => { station.en = 'bad\nlabel'; },
  station => { station.reciter = 'false'; },
  station => { station.url = 'not a URL'; },
  station => { station.url = 'file:///radio.mp3'; },
  station => { station.url = 'https://user:secret@example.com/radio'; },
  station => { station.url = 'https://example.com/bad path'; },
]) {
  const modified = structuredClone(stations);
  change(modified[1]);
  assert.throws(() => validateStations(modified), /Invalid station/);
}
assert.throws(() => validateStations([...stations, stations[1]]), /Invalid station/);
assert.throws(() => validateStations(stations.slice(1)), /fallback/);
const radio = readFileSync(new URL('../app/src/main/java/tv/afnon/home/RadioCatalog.java', import.meta.url), 'utf8');
const stationRows = new RegExp(String.raw`new Radio\.Station\(${Array(6).fill(quoted).join(', ')}, (true|false)\)`, 'g');
assert.deepEqual([...radio.matchAll(stationRows)].map(m => ({
  id: JSON.parse(m[1]), ar: JSON.parse(m[2]), en: JSON.parse(m[3]),
  whereAr: JSON.parse(m[4]), whereEn: JSON.parse(m[5]), url: JSON.parse(m[6]), reciter: m[7] === 'true',
})), stations, 'Java radio catalog must preserve all station/reciter fields and order');
const voices = JSON.parse(readFileSync(new URL('../shared/data/voices.json', import.meta.url)));
validateVoices(voices);
for (const change of [
  voice => { voice.id = '../voice'; },
  voice => { voice.ar = ''; },
  voice => { voice.en = 'bad\tlabel'; },
  voice => { voice.sourceFile = '../clip.mp3'; },
  voice => { voice.sourceFile = 'clip\t.mp3'; },
  voice => { voice.sourceFile = 'clip.wav'; },
]) {
  const modified = structuredClone(voices);
  change(modified[1]);
  assert.throws(() => validateVoices(modified), /Invalid voice/);
}
assert.throws(() => validateVoices([...voices, voices[1]]), /Invalid voice/);
assert.throws(() => validateVoices(voices.slice(1)), /defaults/);
assert.throws(() => validateVoices(voices.filter(voice => voice.id !== 'chebae_fajr')), /defaults/);
const voiceJava = readFileSync(new URL('../app/src/main/java/tv/afnon/home/VoiceCatalog.java', import.meta.url), 'utf8');
const voiceRows = new RegExp(String.raw`\{${Array(3).fill(quoted).join(', ')}\},`, 'g');
assert.deepEqual([...voiceJava.matchAll(voiceRows)].map(m => ({id: JSON.parse(m[1]), ar: JSON.parse(m[2]), en: JSON.parse(m[3])})),
  voices.map(({id, ar, en}) => ({id, ar, en})), 'Java voice catalog must preserve all labels and order');
const downloads = readFileSync(new URL('./adhan-sources.tsv', import.meta.url), 'utf8').trimEnd().split('\n').slice(1);
assert.deepEqual(downloads.map(row => row.split('\t')), voices.map(voice => [voice.id, voice.sourceFile]));
console.log('PASS: canonical catalog validation and complete Java city/region/radio/voice fidelity');

const strings = JSON.parse(readFileSync(new URL('../shared/data/strings.json', import.meta.url)));
validateStrings(strings);
for (const invalid of [null, [], {}, {bad: null}, {bad: {ar: '', en: 'English'}},
  {bad: {ar: 'Arabic', en: 2}}, {bad: {ar: 'Arabic', en: 'English', d: 1}},
  {bad: {ar: 'Arabic', en: 'English', unknown: true}}, {bad: {ar: ['Arabic'], en: []}},
  {bad: {ar: ['Arabic'], en: 'English'}}, {bad: {ar: ['Arabic'], en: ['English'], d: true}},
  {bad: {ar: 'Bad\0text', en: 'English'}}, {bad: {ar: '{name}', en: 'English'}},
  {bad: {ar: '{bad-name}', en: '{bad-name}'}}, {bad: {ar: ['{name}'], en: ['{name}']}},
]) assert.throws(() => validateStrings(invalid));
const example = {greet: {ar: 'أهلًا {name}', en: 'Hello {name}', d: true}};
const marker = '    String greet(String name) { {{STRINGS:greet}} }';
assert.match(renderText(example, marker), /d\("Hello " \+ name\)/);
assert.throws(() => renderText(example, marker.replace('String name', 'String other')), /Undeclared/);
assert.throws(() => renderText(example, ''), /differ/);
assert.throws(() => renderText(example, marker + '\n' + marker), /duplicate/);
assert.throws(() => renderText(example, marker.replace('greet}}', 'missing}}')), /Missing/);
assert.throws(() => renderText(example, marker + '{{STRINGS:bad-key}}'), /differ/);
console.log('PASS: bilingual text values, list sizes, parameters, digit flags and adapter markers');

const format = JSON.parse(readFileSync(new URL('../shared/data/format-text.json', import.meta.url)));
validateFormatText(format);
for (const invalid of [null, {}, {ar: {}, en: {}}, {ar: {key: ''}, en: {key: 'Text'}},
  {ar: {key: []}, en: {key: 'Text'}}, {ar: {key: [null]}, en: {key: 'Text'}},
  {ar: {key: 4}, en: {key: 'Text'}}, {ar: {'bad-key': 'Text'}, en: {key: 'Text'}},
  {ar: {key: 'bad\0text'}, en: {key: 'Text'}},
]) assert.throws(() => validateFormatText(invalid));
const literal = {ar: {key: 'نص'}, en: {key: ['A', 'B']}};
assert.equal(renderLiterals(literal, '{{TEXT:ar:key}} + {{TEXT:en:key}}'), '"نص" + new String[] {"A", "B"}');
assert.throws(() => renderLiterals(literal, '{{TEXT:ar:missing}}'), /Missing/);
assert.throws(() => renderLiterals(literal, '{{TEXT:ar:key}}'), /differ/);
assert.throws(() => renderLiterals(literal, '{{TEXT:ar:key}} {{TEXT:en:key}} {{TEXT:bad:key}}'), /differ/);
console.log('PASS: locale-specific formatter literals and complete native marker coverage');

const sacred = JSON.parse(readFileSync(new URL('../shared/data/sacred-text.json', import.meta.url)));
validateSacredText(sacred);
for (const change of [
  text => { text.prayerVerse.pop(); },
  text => { text.prayerVerse[0] = ''; },
  text => { text.prayerVerse[0] += '\n'; },
  text => { text.adhanCall = '<b>Text</b>'; },
  text => { text.adhanCall = null; },
  text => { text.extra = 'Text'; },
]) {
  const modified = structuredClone(sacred); change(modified);
  assert.throws(() => validateSacredText(modified), /Invalid sacred/);
}
const hashes = JSON.parse(readFileSync(new URL('../tests/fixtures/sacred-text-hashes.json', import.meta.url)));
const sacredJava = readFileSync(new URL('../app/src/main/java/tv/afnon/home/SacredText.java', import.meta.url), 'utf8');
for (const [key, field, text] of [
  ['prayerVerse', 'PRAYER_VERSE', sacred.prayerVerse.join('\n')], ['adhanCall', 'ADHAN_CALL', sacred.adhanCall],
]) {
  assert.equal(createHash('sha256').update(text).digest('hex'), hashes[key], 'Sacred text must match exact pre-extraction code points');
  const declaration = new RegExp('static final String ' + field + ' = ' + quoted + ';').exec(sacredJava);
  assert.equal(JSON.parse(declaration[1]), text, 'Native sacred constant must preserve exact text and breaks');
}
console.log('PASS: exact sacred text baseline, three verse lines and generated native constants');

const themes = JSON.parse(readFileSync(new URL('../shared/design/themes.json', import.meta.url)));
validateThemes(themes);
for (const invalid of [null, [], ['custom', 'mihrab'], ['mihrab', 'custom', 'other'], ['mihrab', '../theme', 'custom'],
  ['mihrab', 3, 'custom'], ['mihrab', 'mihrab', 'custom'], ['mihrab', 'custom', 'custom'],
]) assert.throws(() => validateThemes(invalid));
const themeJava = readFileSync(new URL('../app/src/main/java/tv/afnon/home/ThemeCatalog.java', import.meta.url), 'utf8');
assert.deepEqual(JSON.parse('[' + /PRESETS = \{([^}]+)\}/.exec(themeJava)[1] + ']'), themes);
console.log('PASS: ordered theme IDs, default/custom slots and generated native registry');

const icons = JSON.parse(readFileSync(new URL('../shared/design/icons.json', import.meta.url)));
validateIcons(icons);
for (const geometry of [
  '<script/>', '<image href="https://example.com/icon.svg"/>', '<path d="M0 0" onclick="alert(1)"/>',
  '<path d="M0 0" fill="url(https://example.com)"/>', '<path d="M0 0" style="fill:red"/>',
  '<circle r="NaN"/>', '<path d="M0 0" d="M1 1"/>', '<path/>', '<path d="M0 0"/><text/>',
]) assert.throws(() => validateIcons({bad: geometry}));
for (const invalid of [null, {}, [], {'bad-id': '<path d="M0 0"/>'}, {bad: 1}]) assert.throws(() => validateIcons(invalid));
const prayerIcons = JSON.parse(readFileSync(new URL('../shared/design/prayer-icons.json', import.meta.url)));
validatePrayerIcons(prayerIcons);
for (const change of [
  data => { data.size = 0; }, data => { data.size = NaN; }, data => { data.strokeWidth = '1.7'; },
  data => { data.strokeWidth = 0; }, data => { data.strokeWidth = data.size + 1; },
  data => { data.prayers.reverse(); }, data => { data.prayers[0].id = '../fajr'; },
  data => { data.prayers[0].path = '"/><script/>'; }, data => { data.prayers[0].path = ''; },
  data => { data.prayers.pop(); }, data => { data.extra = true; },
]) {
  const modified = structuredClone(prayerIcons); change(modified);
  assert.throws(() => validatePrayerIcons(modified));
}
console.log('PASS: local SVG geometry allowlist and ordered prayer vector dimensions/paths');

const appearance = JSON.parse(readFileSync(new URL('../shared/design/appearance.json', import.meta.url)));
validateAppearance(appearance);
for (const change of [
  data => { data.extra = []; }, data => { data.colors.pop(); }, data => { data.patterns = []; },
  data => { data.patterns[0] = '../pattern'; }, data => { data.patterns[0] = 'bad-pattern'; }, data => { data.patterns.push(data.patterns[0]); },
  data => { data.patterns.push(data.proceduralBackgrounds[0]); }, data => { data.surfaces[0] = 'unknown'; },
  data => { data.proceduralBackgrounds.reverse(); }, data => { data.colors[0] = 1; },
]) {
  const modified = structuredClone(appearance); change(modified);
  assert.throws(() => validateAppearance(modified));
}
for (const [name, expected] of Object.entries({BACKGROUNDS: [...appearance.proceduralBackgrounds, ...appearance.patterns], SURFACES: appearance.surfaces, COLORS: appearance.colors})) {
  assert.deepEqual(JSON.parse('[' + new RegExp(name + ' = \\{([^}]+)\\}').exec(themeJava)[1] + ']'), expected);
}
console.log('PASS: appearance defaults, unique background groups, color slots and generated native ID lists');
