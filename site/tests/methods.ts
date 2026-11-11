import assert from 'node:assert/strict';
import expected from '../../tests/fixtures/method-parameters.json';
import { METHOD_DEFINITIONS, METHODS } from '../src/lib/data';
import { methodParams } from '../src/lib/prayer';
import { makeText } from '../src/lib/text';

const names = ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'] as const;
assert.deepEqual(METHODS, expected.map(method => method.id));
for (const method of expected) {
  const params = methodParams(method.id);
  assert.deepEqual({
    id: method.id, method: params.method, fajr: params.fajrAngle, isha: params.ishaAngle,
    interval: params.ishaInterval, methodAdjustments: names.map(name => params.methodAdjustments[name] || 0),
  }, method, method.id + ' changed calculation parameters');
}
assert.deepEqual(methodParams('UNKNOWN'), methodParams('EGYPTIAN'));
const altered = methodParams('TURKEY');
altered.methodAdjustments.sunrise = 99;
assert.equal(methodParams('TURKEY').methodAdjustments.sunrise, -7, 'method parameters must not share mutable corrections');
for (const lang of ['ar', 'en'] as const) {
  const text = makeText(lang, false);
  for (const method of METHOD_DEFINITIONS) assert.equal(text.methodName(method.id), method[lang]);
  assert.equal(text.methodName('UNKNOWN'), METHOD_DEFINITIONS[0][lang]);
}
console.log('PASS: all 19 method parameters, order, labels, fallback and independent corrections');
