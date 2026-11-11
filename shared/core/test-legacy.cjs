const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const acorn = require('acorn');
const base = path.join(__dirname, 'build/compileSync/js/main/productionExecutable/kotlin');
const names = ['kotlin-kotlin-stdlib.js', 'IslamicTVLauncher-core.js'];
const sources = names.map(name => fs.readFileSync(path.join(base, name), 'utf8'));
const proof = fs.readFileSync(path.join(__dirname, 'legacy-proof.js'), 'utf8');
for (const source of [...sources, proof]) acorn.parse(source, {ecmaVersion: 5});

for (const commonjs of [false, true]) {
  const context = vm.createContext({});
  vm.runInContext('globalThis = undefined; Symbol = undefined; Intl = undefined; Promise = undefined; Map = undefined; Set = undefined; WeakMap = undefined; WeakSet = undefined; BigInt = undefined; Math.trunc = undefined; Math.sign = undefined; Math.imul = undefined; Math.clz32 = undefined; Number.isNaN = undefined; Number.isFinite = undefined; Array.from = undefined; Array.prototype.includes = undefined; Array.prototype.find = undefined; Object.assign = undefined; Object.setPrototypeOf = undefined; ArrayBuffer.isView = undefined;', context);
  const modules = {};
  for (let i = 0; i < names.length; i++) {
    if (commonjs) {
      context.module = {exports: {}};
      context.exports = context.module.exports;
      context.require = name => {
        assert.ok(Object.hasOwn(modules, name), 'unexpected runtime dependency: ' + name);
        return modules[name];
      };
    }
    vm.runInContext(sources[i], context, {filename: names[i]});
    if (commonjs) modules['./' + names[i]] = context.module.exports;
  }
  vm.runInContext(proof, context);
  const api = commonjs ? context.module.exports : context['tv.afnon:core'];
  assert.equal(context.verifyIslamicTVCore(api).checks, 66);
}
console.log('PASS: ES5 syntax and 66 prayer/validation/reminder/Home/UI assertions in browser and CommonJS hosts without modern runtime APIs. Runtime JavaScript: ' + sources.reduce((n, source) => n + Buffer.byteLength(source), 0) + ' bytes.');
