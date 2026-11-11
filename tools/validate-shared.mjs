import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateCatalog, validateMethods, validateStations, validateVoices, validateThemes } from './catalogs.mjs';
import { validateStrings, validateFormatText, validateSacredText } from './strings.mjs';
import { validateIcons, validatePrayerIcons, validateAppearance } from './design.mjs';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const json = path => JSON.parse(readFileSync(resolve(root, path), 'utf8'));

export function validateShared() {
  validateMethods(json('shared/data/methods.json'));
  validateCatalog(json('shared/data/cities.json'), json('shared/data/regions.json'));
  validateStations(json('shared/data/stations.json'));
  validateVoices(json('shared/data/voices.json'));
  validateStrings(json('shared/data/strings.json'));
  validateFormatText(json('shared/data/format-text.json'));
  validateSacredText(json('shared/data/sacred-text.json'));
  validateThemes(json('shared/design/themes.json'));
  validateIcons(json('shared/design/icons.json'));
  validatePrayerIcons(json('shared/design/prayer-icons.json'));
  validateAppearance(json('shared/design/appearance.json'));
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  validateShared();
  console.log('Shared source inputs validated');
}
