import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { validateUiContract, validateUiGaps, validateUiChoiceGaps, uiChoices, renderUiCatalog, renderUiGaps } from './ui-contract.mjs';

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
  c => { c.range = 'percent'; },
  c => { c.choices = []; },
  c => { c.choices = '__proto__'; },
  c => { c.choices = ['same', 'same']; },
  c => { c.choices = ['bad/value']; },
]) {
  const modified = structuredClone(controls); change(modified[0]);
  assert.throws(() => validateUiContract(modified, strings), /Invalid UI control/);
}
assert.throws(() => validateUiContract([...controls, controls[0]], strings), /Invalid UI control/);
assert.throws(() => validateUiContract([], strings), /Missing UI contract/);
for (const value of [undefined, 'unknown', ['prayer-offset']]) {
  const modified = structuredClone(controls);
  modified.find(c => c.kind === 'number').range = value;
  assert.throws(() => validateUiContract(modified, strings), /Invalid UI control/);
}
const profiles = [
  ['Android TV', json('platforms/android-tv/src/androidTest/assets/ui-gaps.json'), json('platforms/android-tv/src/androidTest/assets/ui-choice-gaps.json')],
  ['LG webOS', json('platforms/webos/ui-gaps.json'), json('platforms/webos/ui-choice-gaps.json')],
];
for (const [, gaps, choices] of profiles) {
  validateUiGaps(gaps, controls);
  validateUiChoiceGaps(choices, controls, gaps);
  for (const change of [g => { g.values = []; }, g => { g.values = ['unknown']; }, g => { g.values.push(g.values[0]); }]) {
    const modified = structuredClone(choices); change(modified[0]);
    assert.throws(() => validateUiChoiceGaps(modified, controls, gaps), /Invalid UI choice gap/);
  }
  for (const change of [g => { g.id = 'unknown.control'; }, g => { g.reason = ''; }, g => { g.state = 'supported'; }]) {
    const modified = structuredClone(gaps); change(modified[0]);
    assert.throws(() => validateUiGaps(modified, controls), /Invalid UI gap/);
  }
  assert.throws(() => validateUiGaps([...gaps, gaps[0]], controls), /Invalid UI gap/);
}
for (const [id, source] of [['prayer.location', 'cities'], ['reminders.voice', 'voices'], ['radio.station', 'stations']]) {
  assert.deepEqual(uiChoices(controls.find(c => c.id === id)), json('shared/data/' + source + '.json').map(c => c.id));
}
assert.equal(read('shared/core/src/commonMain/kotlin/tv/afnon/core/UiCatalog.kt'), renderUiCatalog(controls));
assert.equal(read('docs/UI-GAPS.md'), renderUiGaps(profiles));
console.log('PASS: semantic UI catalog, bilingual label references, explicit gap reasons and generated core/report.');
