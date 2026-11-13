const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');

const readJson = file => JSON.parse(fs.readFileSync(path.join(__dirname, file), 'utf8'));
const version = readJson('package.json').version;
for (const file of ['package-lock.json', 'app/appinfo.json', 'service/package.json']) {
  assert.equal(readJson(file).version, version, file + ' must match the app version');
}
assert.equal(readJson('package-lock.json').packages[''].version, version);

const out = path.join(__dirname, 'build');
const sentinels = ['app', 'service'].map(part => path.join(out, part, 'stale-test-output.txt'));
if (fs.existsSync(out)) assert.equal(fs.realpathSync(out), out, 'test output must stay inside the project');
for (const file of sentinels) {
  fs.mkdirSync(path.dirname(file), {recursive: true});
  assert.equal(fs.realpathSync(path.dirname(file)), path.dirname(file));
  fs.writeFileSync(file, 'An earlier build left this file behind.');
}
execFileSync(process.execPath, [path.join(__dirname, 'build.cjs')], {stdio: 'inherit'});
for (const file of sentinels) assert.ok(!fs.existsSync(file), 'stale generated output must not be packaged');
assert.deepEqual(fs.readFileSync(path.join(out, 'service/trust/usertrust-rsa.pem')),
  fs.readFileSync(path.join(__dirname, 'service/trust/usertrust-rsa.pem')), 'package the exact verified public HTTPS root');
console.log('PASS: rebuild removes stale app/service output.');
