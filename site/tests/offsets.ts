import assert from 'node:assert/strict';
import { DEFAULT_PREFS, CITIES, type Prefs } from '../src/lib/data';
import { makeCtx } from '../src/lib/ctx';
import { clock, prayerDay, prayerOffsets } from '../src/lib/prayer';
import { rowsFor, rowsHtml, savedDraft, moveDraft, commit, PRAYER } from '../src/screens/settings';

const prefs: Prefs = { ...DEFAULT_PREFS, city: 'leamingtonspa', method: 'MUSLIM_WORLD_LEAGUE', clock: '24' };
assert.ok(CITIES.some(c => c.id === prefs.city), 'Use the intended city, never the fallback');
const offsets = [3, -1, 2, 4, -2, 5];
for (const date of ['2026-03-29', '2026-06-21', '2026-10-25', '2026-12-21']) {
  const now = new Date(`${date}T00:00:00`);
  const plain = prayerDay(now, prefs), shifted = prayerDay(now, { ...prefs, prayerOffsets: offsets });
  for (let i = 0; i < 6; i++) assert.equal(+shifted.times[i] - +plain.times[i], offsets[i] * 60000, `${date}: prayer ${i}`);
  const afterIsha = new Date(+shifted.times[5] + 60000);
  const next = prayerDay(afterIsha, { ...prefs, prayerOffsets: offsets });
  const nextPlain = prayerDay(afterIsha, prefs);
  assert.equal(next.nextIndex, nextPlain.nextIndex);
  assert.equal(+next.nextTime - +nextPlain.nextTime, offsets[next.nextIndex] * 60000);
  assert.equal(+next.suhoorEnd - +nextPlain.suhoorEnd, offsets[0] * 60000);
}
assert.deepEqual(prayerOffsets(prefs), [0, 0, 0, 0, 0, 0]);
assert.deepEqual(prayerOffsets({ ...prefs, prayerOffsets: [Infinity, NaN, 1.5, 99, -99] }), [0, 0, 0, 30, -30, 0]);

for (const lang of ['en', 'ar'] as const) for (const format of ['12', '24'] as const) {
  const ctx = makeCtx(lang, { ...prefs, clock: format }, new Date('2026-10-26T09:00:00'));
  const rows = rowsFor(ctx, PRAYER, false, true);
  assert.equal(rows.length, 6);
  for (const [i, row] of rows.entries()) {
    assert.equal(row.prayer, i);
    assert.equal(savedDraft(ctx, row), 0);
    assert.equal(moveDraft(row, 30, 1), 30);
    assert.equal(moveDraft(row, -30, -1), -30);
    const preview = rowsHtml(ctx, PRAYER, false, { row: i, draft: 2 }, true);
    const time = clock(ctx.t, new Date(+ctx.day.times[i] + 120000), ctx.h24);
    assert.ok(preview.includes(time), `adjusted time in ${lang}/${format}`);
    assert.ok(preview.includes('offset-time num'));
    assert.deepEqual(prayerOffsets(ctx.prefs), [0, 0, 0, 0, 0, 0], 'Preview cannot save');
    const saved = commit(ctx, row, 2)!;
    assert.equal(saved.prayerOffsets![i], 2);
    assert.equal(saved.prayerOffsets!.filter(n => n !== 0).length, 1);
    assert.equal(commit(ctx, row, 0), null);
  }
}
console.log('Preview offsets: six prayers, draft/save/cancel, limits, Arabic/English clocks and UK clock changes passed');
