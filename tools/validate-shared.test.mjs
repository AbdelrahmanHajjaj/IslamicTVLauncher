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
  for (const name of ['catalogs.mjs', 'strings.mjs', 'design.mjs', 'validate-shared.mjs']) {
    copyFileSync(join(root, 'tools', name), join(fixture, 'tools', name));
  }
  cpSync(join(root, 'shared/data'), join(fixture, 'shared/data'), {recursive: true});
  mkdirSync(join(fixture, 'shared/design'));
  for (const name of readdirSync(join(root, 'shared/design')).filter(name => name.endsWith('.json'))) {
    copyFileSync(join(root, 'shared/design', name), join(fixture, 'shared/design', name));
  }
  assert.ok(!existsSync(join(fixture, 'app')) && !existsSync(join(fixture, 'site')));
  const check = () => spawnSync(process.execPath, [join(fixture, 'tools/validate-shared.mjs')], {encoding: 'utf8'});
  const valid = check();
  assert.equal(valid.status, 0, valid.stderr);
  const iconsPath = join(fixture, 'shared/design/icons.json');
  const icons = JSON.parse(readFileSync(iconsPath, 'utf8'));
  icons[Object.keys(icons)[0]] = '<script>alert(1)</script>';
  writeFileSync(iconsPath, JSON.stringify(icons));
  const invalid = check();
  assert.notEqual(invalid.status, 0);
  assert.match(invalid.stderr, /Unsupported icon markup/);
  console.log('PASS: shared-input validation works without platform/preview trees and rejects active SVG markup.');
} finally {
  assert.equal(dirname(realpathSync(fixture)), scratch);
  rmSync(fixture, {recursive: true, force: true});
}
