import assert from 'node:assert/strict';
import { homeLayout, homePrayers, homeChoice } from '../src/lib/home-layout';
import { DEFAULT_PREFS } from '../src/lib/data';
import { makeCtx } from '../src/lib/ctx';
import { cellsHtml } from '../src/screens/home';
import { rowsFor, pick, DISPLAY, APPEARANCE, PRAYER, rowsHtml, previewHtml } from '../src/screens/settings';

for (let homeCountdown = 0; homeCountdown < 3; homeCountdown++)
for (let homeSide = 0; homeSide < 3; homeSide++) for (let homeBar = 0; homeBar < 2; homeBar++)
for (let homePrayers = 0; homePrayers < 5; homePrayers++) for (let count = 0; count <= 9; count++)
for (const homeLayoutMode of ['classic', 'two_rows'] as const) for (const rtl of [false, true]) for (const quickLabels of [false, true]) {
  const h = homeLayout({ ...DEFAULT_PREFS, quickLabels, homeCountdown, homeSide, homeBar, homePrayers, homeLayout: homeLayoutMode }, count, rtl);
  assert.ok(h.x >= 96 && h.x + h.width <= 1824);
  assert.ok(h.recentY + 94 <= 1024);
  assert.ok(h.count === 0 || h.tileHeight >= 120);
  assert.ok(h.tileWidth > 0 && h.tileWidth <= 640);
  if (homeCountdown !== 0 && count === 6 && homeLayoutMode !== 'classic') assert.equal(h.columns, 4);
  if (h.count) assert.ok(h.favouritesY - (10 + (h.tileHeight + 20) * .03) > h.labelY + 22 * 1.4 + 8);
  if (homeCountdown === 0) assert.ok(h.panelX + 600 < h.x || h.x + h.width < h.panelX);
  if (homeCountdown === 1 && homeBar === 1) assert.ok(h.recentY + 94 < h.panelY);
}
assert.equal(homeChoice(NaN, 2), 0);
assert.deepEqual(homePrayers(3, -1, 0), [-1, 0]);
assert.deepEqual(homePrayers(0, -1, 2), [0, 1, 2, 3, 4, 5]);
for (const lang of ['ar', 'en'] as const) for (let table = 0; table < 5; table++) {
  const c = makeCtx(lang, { ...DEFAULT_PREFS, homePrayers: table }, new Date('2026-10-26T00:01:00'));
  c.day.nowIndex = -1;
  assert.equal((cellsHtml(c).match(/class="cell"/g) || []).length, [6, 1, 1, 2, 0][table]);
  if (table === 1 || table === 3) assert.ok(cellsHtml(c).includes('—'));
  const draft = pick(c, 'homeCountdown', 1)!;
  assert.equal(c.prefs.homeCountdown, undefined, 'preview never changes saved prefs');
  const rows = rowsFor({ ...c, prefs: draft }, APPEARANCE);
  assert.equal(rows.length, 10);
  assert.ok(rows.some(r => r.id === 'theme') && rows.some(r => r.id === 'background'));
  assert.equal(rows.filter(r => r.id === 'apply').length, 1);
  assert.ok(!rowsFor(c, DISPLAY).some(r => r.id === 'homeLayout'));
  assert.deepEqual(rows[1].segs, c.t.list('homeBars'));
}
console.log('Home geometry, prayer slots and settings drafts passed');

for (const lang of ['ar', 'en'] as const) {
  const c = makeCtx(lang, DEFAULT_PREFS, new Date('2026-10-26T12:00:00'));
  const shown = rowsHtml(c, PRAYER, false, { row: 2, draft: 1 });
  const alternate = makeCtx(lang, { ...DEFAULT_PREFS, hanafi: true }, c.now);
  assert.ok(shown.includes(previewHtml(alternate)), 'Asr school row previews its unsaved time');
  assert.notEqual(previewHtml(c), previewHtml(alternate));
  assert.equal(c.prefs.hanafi, false);
}
