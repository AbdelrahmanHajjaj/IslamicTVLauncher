import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const read = path => readFileSync(resolve(root, path), 'utf8');
// shortcut: validate against the native enum until Q03 extracts the shared method catalog.
const methods = new Set([...read('app/src/main/java/tv/afnon/home/PrayerMethod.java').matchAll(/^ {4}([A-Z_]+)\(/gm)].map(m => m[1]));

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
  validateCatalog(cities, regions);
  const rows = cities.map(c => '        new Prefs.City(' + [c.id, c.ar, c.en, c.countryAr, c.countryEn].map(value => JSON.stringify(value)).concat(c.lat, c.lng, 'PrayerMethod.' + c.method, 'Madhab.' + (c.hanafi ? 'HANAFI' : 'SHAFI')).join(', ') + '),');
  const groups = regions.map(region => '        {' + region.map(value => JSON.stringify(value)).join(', ') + '},');
  const source = '// Generated from shared/data/{cities,regions}.json by tools/catalogs.mjs. Do not edit.\n' +
    'package tv.afnon.home;\n\nimport com.batoulapps.adhan.Madhab;\n\nfinal class CityCatalog {\n    private CityCatalog() {}\n\n' +
    '    static final Prefs.City[] CITIES = {\n' + rows.join('\n') + '\n    };\n\n' +
    '    static final String[][] REGIONS = {\n' + groups.join('\n') + '\n    };\n}\n';
  const destination = 'app/src/main/java/tv/afnon/home/CityCatalog.java';
  if (args[0] === '--check') {
    if (read(destination) !== source) throw new Error('CityCatalog.java is stale; run node tools/catalogs.mjs');
    console.log('City catalog matches canonical data');
  } else {
    writeFileSync(resolve(root, destination), source);
    console.log('Generated CityCatalog.java from canonical cities and regions');
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) generate();
