import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { renderText } from './strings.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const read = path => readFileSync(resolve(root, path), 'utf8');
const methodDefinitions = JSON.parse(read('shared/data/methods.json'));

export function validateMethods(definitions) {
  if (!Array.isArray(definitions) || definitions[0]?.id !== 'EGYPTIAN') throw new Error('Method catalog must retain the Egyptian fallback');
  const ids = new Set();
  for (const method of definitions) {
    if (!method || typeof method.id !== 'string' || !/^[A-Z][A-Z_]*$/.test(method.id) || ids.has(method.id) ||
        !['ar', 'en'].every(key => typeof method[key] === 'string' && method[key].trim() && !/[\u0000-\u001f]/.test(method[key]))) {
      throw new Error('Invalid method identity or labels');
    }
    const p = method.preset;
    if (p !== undefined && (!p || typeof p !== 'object' || Array.isArray(p) || !Number.isFinite(p.fajr) || p.fajr < 0 || p.fajr > 90 ||
        !Number.isFinite(p.isha) || p.isha < 0 || p.isha > 90 ||
        !Number.isInteger(p.interval) || p.interval < 0 || p.interval > 1440 || !p.isha && !p.interval)) {
      throw new Error('Invalid method preset: ' + method.id);
    }
    const adjustments = method.methodAdjustments;
    if (adjustments !== undefined && (!p || !Array.isArray(adjustments) || adjustments.length !== 6 ||
        adjustments.some(value => !Number.isInteger(value) || Math.abs(value) > 1440))) {
      throw new Error('Invalid method adjustments: ' + method.id);
    }
    ids.add(method.id);
  }
}

validateMethods(methodDefinitions);
const methods = new Set(methodDefinitions.map(method => method.id));

export function validateStations(stations) {
  if (!Array.isArray(stations) || stations[0]?.id !== 'cairo') throw new Error('Station catalog must retain Cairo as its fallback');
  const ids = new Set();
  for (const station of stations) {
    if (!station || !['id', 'ar', 'en', 'whereAr', 'whereEn', 'url'].every(key => typeof station[key] === 'string' && station[key].trim() && !/[\u0000-\u001f]/.test(station[key])) ||
        !/^[a-z][a-z0-9_]*$/.test(station.id) || ids.has(station.id) || typeof station.reciter !== 'boolean') {
      throw new Error('Invalid station identity or labels');
    }
    let url;
    try { url = new URL(station.url); } catch { throw new Error('Invalid station URL: ' + station.id); }
    if (!['http:', 'https:'].includes(url.protocol) || !url.hostname || url.username || url.password || /\s/.test(station.url)) {
      throw new Error('Invalid station URL: ' + station.id);
    }
    ids.add(station.id);
  }
}

export function validateVoices(voices) {
  if (!Array.isArray(voices) || voices[0]?.id !== 'alafasy' || !voices.some(voice => voice?.id === 'chebae_fajr')) {
    throw new Error('Voice catalog must retain regular and Fajr defaults');
  }
  const ids = new Set();
  for (const voice of voices) {
    if (!voice || !['id', 'ar', 'en', 'sourceFile'].every(key => typeof voice[key] === 'string' && voice[key].trim() && !/[\u0000-\u001f]/.test(voice[key])) ||
        !/^[a-z][a-z0-9_]*$/.test(voice.id) || ids.has(voice.id) || !/^[A-Za-z0-9 ()-]+\.mp3$/.test(voice.sourceFile)) {
      throw new Error('Invalid voice identity, labels or source filename');
    }
    ids.add(voice.id);
  }
}

export function validateCatalog(cities, regions) {
  if (!Array.isArray(cities) || !cities.length || cities[0]?.id !== 'cairo') throw new Error('City catalog must retain Cairo as its fallback');
  if (!Array.isArray(regions) || !regions.length) throw new Error('Missing regions');
  const countries = new Set();
  for (const region of regions) {
    if (!Array.isArray(region) || !region.length) throw new Error('Empty region');
    for (const country of region) {
      if (typeof country !== 'string' || !country.trim() || countries.has(country)) throw new Error('Invalid or duplicate region country');
      countries.add(country);
    }
  }
  const ids = new Set();
  for (const city of cities) {
    if (!city || !['id', 'ar', 'en', 'countryAr', 'countryEn'].every(key => typeof city[key] === 'string' && city[key].trim() && !/[\u0000-\u001f]/.test(city[key])) ||
        !/^[a-z][a-z0-9_-]*$/.test(city.id) || ids.has(city.id) ||
        !Number.isFinite(city.lat) || Math.abs(city.lat) > 90 || !Number.isFinite(city.lng) || Math.abs(city.lng) > 180 ||
        !methods.has(city.method) || typeof city.hanafi !== 'boolean' || !countries.has(city.countryEn)) {
      throw new Error('Invalid city: ' + (city && city.id));
    }
    ids.add(city.id);
  }
}

function generate() {
  const args = process.argv.slice(2);
  if (args.length > 1 || args.length && args[0] !== '--check') throw new Error('Usage: node tools/catalogs.mjs [--check]');
  const cities = JSON.parse(read('shared/data/cities.json'));
  const regions = JSON.parse(read('shared/data/regions.json'));
  const stations = JSON.parse(read('shared/data/stations.json'));
  const voices = JSON.parse(read('shared/data/voices.json'));
  validateCatalog(cities, regions);
  validateStations(stations);
  validateVoices(voices);
  const rows = cities.map(c => '        new Prefs.City(' + [c.id, c.ar, c.en, c.countryAr, c.countryEn].map(value => JSON.stringify(value)).concat(c.lat, c.lng, 'PrayerMethod.' + c.method, 'Madhab.' + (c.hanafi ? 'HANAFI' : 'SHAFI')).join(', ') + '),');
  const groups = regions.map(region => '        {' + region.map(value => JSON.stringify(value)).join(', ') + '},');
  const source = '// Generated from shared/data/{cities,regions}.json by tools/catalogs.mjs. Do not edit.\n' +
    'package tv.afnon.home;\n\nimport com.batoulapps.adhan.Madhab;\n\nfinal class CityCatalog {\n    private CityCatalog() {}\n\n' +
    '    static final Prefs.City[] CITIES = {\n' + rows.join('\n') + '\n    };\n\n' +
    '    static final String[][] REGIONS = {\n' + groups.join('\n') + '\n    };\n}\n';
  const entries = methodDefinitions.map((method, i) => {
    const p = method.preset;
    const parameters = p
      ? [p.fajr, p.isha, p.interval, method.methodAdjustments ? 'new int[] {' + method.methodAdjustments.join(', ') + '}' : 'null'].join(', ')
      : 'CalculationMethod.' + method.id;
    return '    ' + method.id + '(' + JSON.stringify(method.ar) + ', ' + JSON.stringify(method.en) + ', ' + parameters + ')' + (i === methodDefinitions.length - 1 ? ';' : ',');
  });
  const nativeMethods = read('app/src/main/templates/PrayerMethod.java.template').replace('{{METHODS}}', entries.join('\n'));
  const stationRows = stations.map(station => '        new Radio.Station(' +
    ['id', 'ar', 'en', 'whereAr', 'whereEn', 'url'].map(key => JSON.stringify(station[key])).concat(station.reciter).join(', ') + '),');
  const nativeStations = '// Generated from shared/data/stations.json by tools/catalogs.mjs. Do not edit.\n' +
    'package tv.afnon.home;\n\nfinal class RadioCatalog {\n    private RadioCatalog() {}\n\n' +
    '    static final Radio.Station[] STATIONS = {\n' + stationRows.join('\n') + '\n    };\n}\n';
  const voiceRows = voices.map(voice => '        {' + ['id', 'ar', 'en'].map(key => JSON.stringify(voice[key])).join(', ') + '},');
  const nativeVoices = '// Generated from shared/data/voices.json by tools/catalogs.mjs. Do not edit.\n' +
    'package tv.afnon.home;\n\nfinal class VoiceCatalog {\n    private VoiceCatalog() {}\n\n' +
    '    static final String[][] VOICES = {\n' + voiceRows.join('\n') + '\n    };\n}\n';
  const voiceSources = '# Generated from shared/data/voices.json by tools/catalogs.mjs. Do not edit.\n' +
    voices.map(voice => voice.id + '\t' + voice.sourceFile).join('\n') + '\n';
  for (const [destination, output] of [
    ['app/src/main/java/tv/afnon/home/CityCatalog.java', source],
    ['app/src/main/java/tv/afnon/home/PrayerMethod.java', nativeMethods],
    ['app/src/main/java/tv/afnon/home/RadioCatalog.java', nativeStations],
    ['app/src/main/java/tv/afnon/home/VoiceCatalog.java', nativeVoices],
    ['tools/adhan-sources.tsv', voiceSources],
    ['app/src/main/java/tv/afnon/home/Text.java', renderText(JSON.parse(read('shared/data/strings.json')), read('app/src/main/templates/Text.java.template'), JSON.parse(read('shared/data/format-text.json')))],
  ]) {
    if (args[0] === '--check') {
      if (read(destination) !== output) throw new Error(destination + ' is stale; run node tools/catalogs.mjs');
    } else {
      writeFileSync(resolve(root, destination), output);
    }
  }
  console.log(args[0] === '--check' ? 'Catalogs match canonical data' : 'Generated Android catalogs and adhan download list');
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) generate();
