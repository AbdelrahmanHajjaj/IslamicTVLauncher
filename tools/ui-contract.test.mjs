import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { validateUiContract, validateUiGaps, renderUiCatalog, renderUiGaps } from './ui-contract.mjs';

const read = path => readFileSync(new URL('../' + path, import.meta.url), 'utf8');
const json = path => JSON.parse(read(path));
const controls = json('shared/data/ui-contract.json'), strings = json('shared/data/strings.json');
validateUiContract(controls, strings);
for (const change of [
  c => { c.id = '../invalid'; },
  c => { c.id = ['prayer.location']; },
  c => { c.section = 'unknown'; },
  c => { c.kind = 'widget'; },
  c => { c.label = 'unknownLabel'; },
  c => { c.label = 'prayerNames:6'; },
  c => { c.label = 'prayerNames:0:extra'; },
  c => { c.label = 'prayerNames'; },
  c => { c.save = 'action'; },
  c => { c.storageKey = 'home_layout'; },
]) {
  const modified = structuredClone(controls); change(modified[0]);
  assert.throws(() => validateUiContract(modified, strings), /Invalid UI control/);
}
assert.throws(() => validateUiContract([...controls, controls[0]], strings), /Invalid UI control/);
assert.throws(() => validateUiContract([], strings), /Missing UI contract/);
const profiles = [
  ['Android TV', json('platforms/android-tv/src/androidTest/assets/ui-gaps.json')],
  ['LG webOS', json('platforms/webos/ui-gaps.json')],
];
for (const [, gaps] of profiles) {
  validateUiGaps(gaps, controls);
  for (const change of [g => { g.id = 'unknown.control'; }, g => { g.reason = ''; }, g => { g.state = 'supported'; }]) {
    const modified = structuredClone(gaps); change(modified[0]);
    assert.throws(() => validateUiGaps(modified, controls), /Invalid UI gap/);
  }
  assert.throws(() => validateUiGaps([...gaps, gaps[0]], controls), /Invalid UI gap/);
}
assert.equal(read('shared/core/src/commonMain/kotlin/tv/afnon/core/UiCatalog.kt'), renderUiCatalog(controls));
assert.equal(read('docs/UI-GAPS.md'), renderUiGaps(profiles));
console.log('PASS: semantic UI catalog, bilingual label references, explicit gap reasons and generated core/report.');
