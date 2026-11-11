import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { validateCatalog } from './catalogs.mjs';

const cities = JSON.parse(readFileSync(new URL('../shared/data/cities.json', import.meta.url)));
const regions = JSON.parse(readFileSync(new URL('../shared/data/regions.json', import.meta.url)));
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
console.log('PASS: city defaults, unique IDs/countries, coordinates, methods, schools and bilingual labels');
