const assert = require('node:assert/strict');
const {createHash} = require('node:crypto');

function eventBaseline(A) {
  const originalZone = process.env.TZ;
  process.env.TZ = 'UTC';
  try {
    const digest = createHash('sha256');
    const today = A.day(new Date('2026-11-10T00:00:00Z'), A.prefs({utcOffset: 120}));
    let cases = 0;
    for (const automatic of [false, true]) for (const remind of [0, 5, 15, 30]) {
      for (const adhan of [false, true]) for (const announce of [false, true]) for (const mask of [0, 1, 21, 61]) {
        const p = A.prefs({utcOffset: 120, automatic, remind, adhan, announce,
          bells: Array.from({length: 6}, (_, i) => !!(mask & 1 << i))});
        for (const time of today.times) for (const delta of [-remind * 60000 - 1, -remind * 60000, -1, 0, 1]) {
          const now = new Date(time + delta), first = A.nextEvent(now, p);
          digest.update(JSON.stringify(first) + '\n'); cases++;
          if (first) {digest.update(JSON.stringify(A.nextEvent(now, p, [first.id])) + '\n'); cases++;}
        }
      }
    }
    return {cases, sha256: digest.digest('hex')};
  } finally {
    if (originalZone === undefined) delete process.env.TZ; else process.env.TZ = originalZone;
  }
}
module.exports = function checkReminders(A) {
  assert.deepEqual(eventBaseline(A), require('../tests/fixtures/reminder-events.json'));
};
module.exports.eventBaseline = eventBaseline;
