const path = require('node:path');
const { execFileSync } = require('node:child_process');

const root = path.resolve(__dirname, '../..');
execFileSync(process.execPath, [path.join(root, 'tools/ui-contract.mjs')], {cwd: root, stdio: 'inherit'});
execFileSync(process.execPath, [path.join(root, 'tools/catalogs.mjs'), '--check-reference'], {cwd: root, stdio: 'inherit'});
const task = 'compileProductionExecutableKotlinJs';
if (process.platform === 'win32') {
  execFileSync('cmd.exe', ['/d', '/s', '/c', 'gradlew.bat -p shared/core ' + task + ' --no-daemon'], {cwd: root, stdio: 'inherit', windowsHide: true});
} else {
  execFileSync('sh', ['./gradlew', '-p', 'shared/core', task, '--no-daemon'], {cwd: root, stdio: 'inherit'});
}
