import assert from 'node:assert/strict';
import { CITIES, DEFAULT_PREFS } from '../src/lib/data';
import { hijriOf, prayerDay } from '../src/lib/prayer';

const calendar = new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn', {
  timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric',
});
for (const moment of ['2026-03-28T23:30:00', '2026-03-29T00:30:00', '2026-10-24T23:30:00',
  '2026-10-25T00:30:00', '2026-12-31T23:30:00']) {
  const now = new Date(moment);
  for (const offset of [-2, 0, 2]) {
    const civilDay = new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate() + offset, 12));
    const parts = Object.fromEntries(calendar.formatToParts(civilDay).map(p => [p.type, p.value]));
    assert.deepEqual(hijriOf(now, offset), {day: +parts.day, month: +parts.month - 1, year: +parts.year});
  }
}
const prefs = {...DEFAULT_PREFS, city: 'leamingtonspa', method: 'MUSLIM_WORLD_LEAGUE', prayerOffsets: [3, -1, 2, 4, -2, 5]};
for (const date of ['2026-03-28', '2026-03-29', '2026-10-24', '2026-10-25', '2026-12-31']) {
  const now = new Date(date + 'T12:00:00'), today = prayerDay(now, prefs);
  const afterIsha = prayerDay(new Date(+today.times[5] + 1), prefs);
  const tomorrow = prayerDay(new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 12), prefs);
  assert.equal(afterIsha.nextIndex, 0);
  assert.equal(+afterIsha.nextTime, +tomorrow.times[0], date + ': tomorrow is a calendar day');
  assert.equal(+afterIsha.suhoorEnd, +tomorrow.times[0]);
}
assert.throws(() => prayerDay(new Date(NaN), DEFAULT_PREFS), /Invalid prayer time/);
const city = CITIES.find(c => c.id === DEFAULT_PREFS.city)!;
const latitude = city.lat;
try {
  city.lat = 90; // Force unavailable astronomy without adding an unsupported catalog location.
  assert.throws(() => prayerDay(new Date('2026-06-21T12:00:00Z'), DEFAULT_PREFS), /Invalid prayer time/);
} finally {city.lat = latitude;}
console.log('Calendar: civil-day Hijri offsets, DST/year rollover, tomorrow Fajr/Suhoor and unavailable calculations passed');
