import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { validateCatalog, validateMethods, validateStations } from './catalogs.mjs';

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
console.log('PASS: canonical catalog validation and complete Java city/region/radio fidelity');
