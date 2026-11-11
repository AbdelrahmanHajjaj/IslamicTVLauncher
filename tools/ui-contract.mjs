import { readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const data = path => JSON.parse(readFileSync(new URL('../shared/' + path, import.meta.url), 'utf8'));
const appearance = data('design/appearance.json');
const choiceSources = {
  cities: data('data/cities.json').map(c => c.id),
  methods: ['city-default', ...data('data/methods.json').map(c => c.id)],
  voices: data('data/voices.json').map(c => c.id),
  stations: data('data/stations.json').map(c => c.id),
  themes: data('design/themes.json'),
  backgrounds: ['theme', ...appearance.proceduralBackgrounds, ...appearance.patterns],
  zones: ['system', ...Array.from({length: 105}, (_, i) => String(-720 + i * 15))],
};
const sections = ['prayer', 'reminders', 'radio', 'display', 'appearance', 'network', 'device', 'tests', 'offsets'];
const kinds = ['choice', 'toggle', 'number', 'action'];

export function uiChoices(control) {
  return (Array.isArray(control.choices) ? control.choices
    : typeof control.choices === 'string' && Object.hasOwn(choiceSources, control.choices) ? choiceSources[control.choices] : []).slice();
}

export function validateUiContract(controls, strings) {
  if (!Array.isArray(controls) || !controls.length) throw new Error('Missing UI contract');
  const ids = new Set();
  for (const control of controls) {
    const {id, section, kind, label, save, range, choices} = control || {};
    const [key, index] = typeof label === 'string' ? label.split(':') : [];
    const text = strings[key];
    if (typeof id !== 'string' || !/^[a-z]+(?:\.[a-z][a-z0-9-]*)+$/.test(id) || ids.has(id) ||
        !sections.includes(section) || !kinds.includes(kind) ||
        !['immediate', 'appearance', 'action'].includes(save) || (kind === 'action') !== (save === 'action') ||
        (kind === 'number' ? !['prayer-offset', 'hijri-offset', 'percent'].includes(range) : range !== undefined) ||
        (kind === 'choice' ? !uiChoices(control).length ||
          uiChoices(control).some(value => typeof value !== 'string' || !/^-?[a-zA-Z0-9_][a-zA-Z0-9_-]*$/.test(value)) ||
          new Set(uiChoices(control)).size !== uiChoices(control).length : choices !== undefined) ||
        typeof label !== 'string' || !/^[a-z][a-zA-Z0-9]*(?::[0-5])?$/.test(label) ||
        !text || !['ar', 'en'].every(lang => index === undefined
          ? typeof text[lang] === 'string' && !/[{}]/.test(text[lang])
          : /^[0-5]$/.test(index) && Array.isArray(text[lang]) && typeof text[lang][Number(index)] === 'string') ||
        Object.keys(control).sort().join(',') !== (kind === 'number' ? 'id,kind,label,range,save,section'
          : kind === 'choice' ? 'choices,id,kind,label,save,section' : 'id,kind,label,save,section')) {
      throw new Error('Invalid UI control: ' + id);
    }
    ids.add(id);
  }
}

export function validateUiGaps(gaps, controls) {
  if (!Array.isArray(gaps)) throw new Error('Missing UI gaps');
  const ids = new Set(controls.map(control => control.id)), seen = new Set();
  for (const gap of gaps) {
    if (!gap || !ids.has(gap.id) || seen.has(gap.id) ||
        !['unimplemented', 'os-unsupported'].includes(gap.state) ||
        typeof gap.reason !== 'string' || !gap.reason.trim() || /[\r\n|]/.test(gap.reason) ||
        Object.keys(gap).sort().join(',') !== 'id,reason,state') throw new Error('Invalid UI gap: ' + gap?.id);
    seen.add(gap.id);
  }
}

export function validateUiChoiceGaps(gaps, controls, wholeGaps) {
  if (!Array.isArray(gaps) || gaps.some(gap => !gap)) throw new Error('Missing UI choice gaps');
  validateUiGaps(gaps.map(({id, state, reason}) => ({id, state, reason})), controls);
  for (const gap of gaps) {
    const control = controls.find(c => c.id === gap.id);
    if (control.kind !== 'choice' || wholeGaps.some(g => g.id === gap.id) ||
        !Array.isArray(gap.values) || !gap.values.length || new Set(gap.values).size !== gap.values.length ||
        gap.values.some(value => !uiChoices(control).includes(value)) ||
        Object.keys(gap).sort().join(',') !== 'id,reason,state,values') throw new Error('Invalid UI choice gap: ' + gap.id);
  }
}

export function renderUiCatalog(controls) {
  return '// Generated from shared/data/ui-contract.json and its canonical catalogs by tools/ui-contract.mjs. Do not edit.\n' +
    'package tv.afnon.core\n\ninternal object UiCatalog {\n    val controls = arrayOf(\n' +
    controls.map(c => '        UiControl(' + [c.id, c.section, c.kind, c.label, c.save, c.range || ''].map(value => JSON.stringify(value)).join(', ') +
      ', ' + (c.kind === 'choice' ? 'arrayOf(' + uiChoices(c).map(value => JSON.stringify(value)).join(', ') + ')' : 'emptyArray()') + '),').join('\n') +
    '\n    )\n}\n';
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  if (process.argv.length > 3 || process.argv[2] && process.argv[2] !== '--write') throw new Error('Usage: node tools/ui-contract.mjs [--write]');
  const controls = data('data/ui-contract.json');
  validateUiContract(controls, data('data/strings.json'));
  const destination = new URL('../shared/core/src/commonMain/kotlin/tv/afnon/core/UiCatalog.kt', import.meta.url);
  const output = renderUiCatalog(controls);
  if (process.argv[2] === '--write') writeFileSync(destination, output);
  else if (readFileSync(destination, 'utf8') !== output) throw new Error('UiCatalog is stale; run node tools/ui-contract.mjs --write');
  console.log(process.argv[2] === '--write' ? 'Generated portable UI catalog' : 'Portable UI catalog matches canonical inputs');
}

export function renderUiGaps(profiles) {
  return '# Declared UI gaps\n\nGenerated by tools/catalogs.mjs from shell gap declarations. Do not edit.\n' +
    'The universal control catalog stays intact. Unimplemented controls are hidden;\n' +
    'an OS restriction has a separate reason. Actual rendered-binding coverage tests\n' +
    'must account for every other ID; this report alone does not prove implementation.\n' +
    'Migration acceptance and evidence live only in ARCHITECTURE-QUEUE.md.\n\n' +
    profiles.map(([name, gaps, choices = []]) => '## ' + name + '\n\n| Control | State | Reason |\n| --- | --- | --- |\n' +
      gaps.map(gap => '| `' + gap.id + '` | ' + gap.state + ' | ' + gap.reason + ' |').join('\n') + '\n\n' +
      '### Choice values\n\n| Control | Missing values | State | Reason |\n| --- | --- | --- | --- |\n' +
      choices.map(gap => '| `' + gap.id + '` | ' + gap.values.map(v => '`' + v + '`').join(', ') + ' | ' + gap.state + ' | ' + gap.reason + ' |').join('\n') + '\n').join('\n');
}
