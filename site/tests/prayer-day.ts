import assert from 'node:assert/strict';
import { DEFAULT_PREFS } from '../src/lib/data';
import { makeCtx } from '../src/lib/ctx';
import { loadBells, prayerDayModel, prayerRules } from '../src/screens/prayer-day';
import { prayerNames } from '../src/screens/home';

for (const lang of ['ar', 'en'] as const) {
  const c = makeCtx(lang, DEFAULT_PREFS, new Date('2026-11-11T12:00:00'));
  const names = prayerNames(c);
  const model = prayerDayModel(c);
  for (let i = 0; i < 6; i++) {
    assert.equal(loadBells()[i], prayerRules.isPrayer(i));
    assert.equal(model.attr!['r' + i]['aria-label'], prayerRules.isPrayer(i) ? `${names[i]} · ${c.t.bell(true)}` : names[i]);
  }
}
console.log('Prayer preview exposes five adhan switches; sunrise remains information');
