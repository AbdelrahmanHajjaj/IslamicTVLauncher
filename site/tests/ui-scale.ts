import assert from 'node:assert/strict';
import { UI_SIZES, uiPercent, safeInline } from '../src/lib/ui-scale';
import { DEFAULT_PREFS } from '../src/lib/data';
import { makeCtx } from '../src/lib/ctx';
import { DISPLAY, rowsFor, rowHtml, commit, moveDraft, pick } from '../src/screens/settings';
import { loadPrefs, savePrefs } from '../src/scripts/live';

assert.deepEqual(UI_SIZES, ['80', '90', '100', '110']);
for (const value of [undefined, null, '110', true, 0, 85, 111, NaN, Infinity]) assert.equal(uiPercent(value), 100);
const descriptor = Object.getOwnPropertyDescriptor(globalThis, 'localStorage');
let stored: string | null = null;
Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: {
  getItem: () => stored, setItem: (_: string, value: string) => { stored = value; },
} });
try {
  const saved = { ...DEFAULT_PREFS, prayerOffsets: [3, 0, -2, 0, 1, 0], theme: 'aurora', volume: 30, extra: 'preserved' };
  for (const invalid of ['110', null, 85, 111, false]) {
    stored = JSON.stringify({ ...saved, uiScale: invalid });
    assert.deepEqual(loadPrefs(), { ...saved, uiScale: 100 });
    const before = stored;
    assert.throws(() => savePrefs(JSON.parse(stored!)), /Invalid interface size/);
    assert.equal(stored, before, 'Invalid writes preserve saved preferences');
  }
  stored = '{'; assert.equal(loadPrefs().uiScale, 100);
  for (const language of ['ar', 'en'] as const) for (const size of UI_SIZES.map(Number)) {
    stored = JSON.stringify({ ...saved, uiScale: size });
    const prefs = loadPrefs();
    assert.equal(uiPercent(prefs.uiScale), size);
    assert.ok(safeInline(prefs) >= 96 && safeInline(prefs) <= 128.1);
    const ctx = makeCtx(language, { ...prefs, uiScale: 100 }, new Date('2026-11-12T12:00:00Z'));
    const row = rowsFor(ctx, DISPLAY).find(row => row.id === 'display.size')!;
    assert.ok(row && row.kind === 'seg');
    assert.equal(row.label, ctx.t.s('uiSize'));
    assert.deepEqual(row.segs, UI_SIZES.map(value => ctx.t.d(value + '%')));
    assert.match(rowHtml(ctx.t, row, 3), /data-ui="display.size" data-ui-kind="choice"/);
    assert.equal(moveDraft(row, 0, -1), 0); assert.equal(moveDraft(row, 3, 1), 3);
    const next = commit(ctx, row, UI_SIZES.indexOf(String(size)));
    assert.deepEqual(next, size === 100 ? null : { ...prefs, uiScale: size });
    if (next) savePrefs(next);
    assert.deepEqual(JSON.parse(stored!), { ...saved, uiScale: size });
  }
  const ctx = makeCtx('en', DEFAULT_PREFS, new Date('2026-11-12T12:00:00Z'));
  for (const index of [-1, 4, .5, NaN]) assert.throws(() => pick(ctx, 'display.size', index), /Invalid interface size/);
} finally {
  if (descriptor) Object.defineProperty(globalThis, 'localStorage', descriptor);
  else Reflect.deleteProperty(globalThis, 'localStorage');
}
console.log('Preview interface sizing: policy, canonical binding, drafts, validated persistence and other settings preserved.');
