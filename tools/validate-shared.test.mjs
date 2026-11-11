import assert from 'node:assert/strict';
import { copyFileSync, cpSync, existsSync, mkdtempSync, mkdirSync, readFileSync, readdirSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { tmpdir } from 'node:os';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const scratch = realpathSync(tmpdir());
const fixture = mkdtempSync(join(scratch, 'islamic-tv-shared-'));
try {
  mkdirSync(join(fixture, 'tools'));
  for (const name of ['catalogs.mjs', 'strings.mjs', 'design.mjs', 'ui-contract.mjs', 'validate-shared.mjs']) {
    copyFileSync(join(root, 'tools', name), join(fixture, 'tools', name));
  }
  cpSync(join(root, 'shared/data'), join(fixture, 'shared/data'), {recursive: true});
  mkdirSync(join(fixture, 'shared/design'));
  for (const name of readdirSync(join(root, 'shared/design')).filter(name => name.endsWith('.json'))) {
    copyFileSync(join(root, 'shared/design', name), join(fixture, 'shared/design', name));
  }
  assert.ok(!existsSync(join(fixture, 'platforms')) && !existsSync(join(fixture, 'site')));
  const check = () => spawnSync(process.execPath, [join(fixture, 'tools/validate-shared.mjs')], {encoding: 'utf8'});
  const valid = check();
  assert.equal(valid.status, 0, valid.stderr);
  const catalog = 'shared/core/src/commonMain/kotlin/tv/afnon/core/UiCatalog.kt';
  mkdirSync(dirname(join(fixture, catalog)), {recursive: true});
  copyFileSync(join(root, catalog), join(fixture, catalog));
  const checkUi = (...args) => spawnSync(process.execPath, [join(fixture, 'tools/ui-contract.mjs'), ...args], {encoding: 'utf8'});
  assert.equal(checkUi().status, 0);
  const controlsPath = join(fixture, 'shared/data/ui-contract.json');
  const controls = JSON.parse(readFileSync(controlsPath, 'utf8'));
  controls.find(c => c.id === 'display.clock').choices.push('future-format');
  writeFileSync(controlsPath, JSON.stringify(controls));
  const stale = checkUi();
  assert.notEqual(stale.status, 0);
  assert.match(stale.stderr, /UiCatalog is stale/);
  assert.equal(checkUi('--write').status, 0);
  assert.equal(checkUi().status, 0, 'UI core generation works without platform/preview trees');
  const iconsPath = join(fixture, 'shared/design/icons.json');
  const icons = JSON.parse(readFileSync(iconsPath, 'utf8'));
  icons[Object.keys(icons)[0]] = '<script>alert(1)</script>';
  writeFileSync(iconsPath, JSON.stringify(icons));
  const invalid = check();
  assert.notEqual(invalid.status, 0);
  assert.match(invalid.stderr, /Unsupported icon markup/);
  console.log('PASS: isolated shared validation rejects active SVG; portable UI generation/check rejects stale metadata without platform/preview trees.');
} finally {
  assert.equal(dirname(realpathSync(fixture)), scratch);
  rmSync(fixture, {recursive: true, force: true});
}
