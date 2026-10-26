import assert from 'node:assert/strict';
import { armCycle, cycleAppearance, CYCLE_MINUTES } from '../src/lib/appearance-cycle';
import { DEFAULT_PREFS } from '../src/lib/data';
import { makeCtx } from '../src/lib/ctx';
import { rowsFor, pick, APPEARANCE } from '../src/screens/settings';
import { backgroundOf, THEMES, BACKGROUNDS } from '../src/scripts/appearance';

const now = 1_800_000_000_000;
assert.deepEqual(CYCLE_MINUTES, [0, 15, 30]);
for (const minutes of [15, 30]) {
  const p = armCycle({ ...DEFAULT_PREFS, theme:'aurora', background:'theme', homeCountdown:2, appearanceCycle:minutes }, now);
  const at = p.appearanceCycleAt!;
  assert.equal(at - now, minutes * 60_000);
  assert.equal(cycleAppearance(p, at-1), p);
  const rotated = cycleAppearance(p, at, () => 0);
  assert.notEqual(rotated.theme, p.theme);
  assert.notEqual(rotated.background, backgroundOf(p));
  assert.ok(THEMES.includes(rotated.theme!));
  assert.ok(BACKGROUNDS.slice(1).includes(rotated.background!));
  assert.equal(rotated.homeCountdown, 2);
  assert.equal(rotated.appearanceCycleAt, at + minutes * 60_000);
  assert.equal(cycleAppearance(rotated, at), rotated);
  const resumed = cycleAppearance(p, at + 86_400_000);
  assert.equal(resumed.appearanceCycleAt, at + 86_400_000 + minutes * 60_000);
  const rollback = cycleAppearance(p, now - 60_000);
  assert.equal(rollback.theme, p.theme);
  assert.equal(rollback.appearanceCycleAt, now - 60_000 + minutes * 60_000);
  const off = armCycle({ ...rotated, appearanceCycle:0 }, at);
  assert.equal(off.theme, rotated.theme);
  assert.equal(off.appearanceCycleAt, undefined);
  assert.equal(cycleAppearance(off, at + 86_400_000), off);
}
for (const minutes of [undefined, -1, 60, NaN]) {
  const p = { ...DEFAULT_PREFS, appearanceCycle:minutes };
  assert.equal(cycleAppearance(p, now), p);
}
for (const language of ['ar','en'] as const) {
  const c = makeCtx(language, DEFAULT_PREFS, new Date(now));
  const draft = pick(c, 'appearanceCycle', 2)!;
  assert.equal(c.prefs.appearanceCycle, undefined, 'draft cannot change saved settings');
  assert.equal(draft.appearanceCycle, 30);
  assert.equal(draft.appearanceCycleAt, undefined, 'clock starts on Apply, not while choosing');
  const row = rowsFor({ ...c, prefs:draft }, APPEARANCE).find(r => r.id === 'appearanceCycle')!;
  assert.equal(row.selected, 2);
  assert.equal(row.segs?.length, 3);
  assert.ok(row.label && row.help);
}
console.log('Appearance rotation: intervals, repeat avoidance, pause/resume deadlines and bilingual drafts passed');
