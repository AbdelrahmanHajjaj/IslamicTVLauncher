const path = require('node:path');
const { execFileSync } = require('node:child_process');

const root = path.resolve(__dirname, '../..');
const task = ':core:compileProductionExecutableKotlinJs';
if (process.platform === 'win32') {
  execFileSync('cmd.exe', ['/d', '/s', '/c', 'gradlew.bat ' + task + ' --no-daemon'], {cwd: root, stdio: 'inherit', windowsHide: true});
} else {
  execFileSync('sh', ['./gradlew', task, '--no-daemon'], {cwd: root, stdio: 'inherit'});
}
