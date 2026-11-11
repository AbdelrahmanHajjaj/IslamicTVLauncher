const assert = require('node:assert/strict');

module.exports = function checkCalendar(A, data) {
  const midnight = day => new Date(day * 86400000);
  const prefs = A.prefs({utcOffset: 0});
  for (let i = 0; i < data.hijri.length - 1; i++) {
    const [day, year, month] = data.hijri[i], end = data.hijri[i + 1][0];
    assert.ok(end - day === 29 || end - day === 30, 'consecutive lunar month boundaries');
    assert.deepEqual({...A.hijri(midnight(day), prefs)}, {day: 1, month: month - 1, year});
    assert.deepEqual({...A.hijri(midnight(end - 1), prefs)}, {day: end - day, month: month - 1, year});
  }
  const first = data.hijri[0][0], last = data.hijri[data.hijri.length - 1][0];
  for (const offset of [-2, 0, 2]) {
    const p = A.prefs({utcOffset: 0, hijriOffset: offset});
    assert.equal(A.hijri(midnight(first - offset - 1), p), null);
    assert.equal(A.hijri(midnight(first - offset), p).day, 1);
    assert.ok(A.hijri(midnight(last - offset - 1), p));
    assert.equal(A.hijri(midnight(last - offset), p), null, 'last boundary is a sentinel, not a supported month');
  }
  for (const utcOffset of [-720, 345, 840]) {
    const now = new Date('2026-12-31T23:30:00Z'), p = A.prefs({utcOffset});
    assert.deepEqual({...A.hijri(now, p)}, {...A.hijri(A.wall(now, p), prefs)}, 'fixed offsets use the wall date');
    const date = A.wall(now, p);
    const tomorrow = A.day(new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + 1, 12) - utcOffset * 60000), p);
    assert.equal(A.day(now, p).tomorrowFajr, tomorrow.times[0], 'tomorrow crosses year/date-line boundaries');
  }
  assert.throws(() => A.day(new Date(NaN), prefs), /unavailable/);
  assert.throws(() => A.next({times: [1, 2, NaN, 4, 5, 6], tomorrowFajr: 10}, 0), /Invalid prayer time/);
  const city = A.city('cairo'), latitude = city.lat;
  try {
    city.lat = 90;
    assert.throws(() => A.day(new Date('2026-06-21T12:00:00Z'), prefs), /unavailable/);
  } finally {city.lat = latitude;}
};
