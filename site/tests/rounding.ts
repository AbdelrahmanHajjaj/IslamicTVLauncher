import assert from 'node:assert/strict';
import { Coordinates, PrayerTimes, Rounding } from 'adhan';
import { DEFAULT_PREFS, METHODS } from '../src/lib/data';
import { methodParams, prayerDay } from '../src/lib/prayer';

const now = new Date('2026-11-12T10:00:00Z');
const names = ['fajr','sunrise','dhuhr','asr','maghrib','isha'] as const;
for (const method of METHODS) {
  const rawParams = methodParams(method);
  rawParams.rounding = Rounding.None;
  const raw = new PrayerTimes(new Coordinates(30.0444,31.2357), now, rawParams);
  const actual = prayerDay(now, {...DEFAULT_PREFS, city: 'cairo', method});
  assert.deepEqual(actual.times.map(Number), names.map(key => Math.floor(+raw[key] / 60000) * 60000), method + ' must floor the unrounded calculation');
}
const cairo = prayerDay(now, {...DEFAULT_PREFS, city: 'cairo', method: 'EGYPTIAN'});
assert.equal(+cairo.times[4], Date.parse('2026-11-12T15:00:00Z'), 'Cairo 17:00:30 displays 17:00, preserving UTC+2');
console.log('All 19 preview methods use unrounded astronomy and downward minute normalization');
