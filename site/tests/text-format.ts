import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { makeText } from '../src/lib/text';
import { METHODS } from '../src/lib/data';
import expected from '../../tests/fixtures/site-text-format-hashes.json';

export function textFormatHashes(make = makeText) {
  const hashes: Record<string, string> = {};
  for (const lang of ['ar', 'en'] as const) for (const eastern of [false, true]) {
    const t = make(lang, eastern), values: unknown[] = [];
    const add = (name: string, value: unknown) => values.push([name, value]);
    for (const sample of ['sample 7 · 8', '', null]) {
      add('d', t.d(sample as string));
      add('allText', t.hintOk(sample as string));
      add('in', t.in(sample as string));
      add('apSub', t.apSub(sample as string));
      add('bannerSub', t.bannerSub(sample as string, 17));
      add('chip', t.chip(sample as string));
      add('place', t.place(sample as string, sample as string));
      for (const minutes of [-1, 0, 1, 2, 3, 10, 11, 59, 60, 61, 150]) {
        add('inMinutes', t.inMinutes(minutes, sample as string));
        add('apTitle', t.apTitle(sample as string, minutes, sample as string));
      }
    }
    for (const n of [-2147483648, -31, -30, -2, -1, 0, 1, 2, 3, 10, 30, 31, 2147483647]) {
      add('all', t.all(n)); add('days', t.days(n)); add('offsetMinutes', t.offsetMinutes(n));
      add('offsetCount', t.offsetCount(n)); add('bearingLabel', t.bearingLabel(n));
    }
    for (const flag of [false, true]) {
      add('ampm', t.ampm(flag)); add('radioStatus', t.radioStatus(flag)); add('school', t.school(flag));
      add('bell', t.bell(flag)); add('passed', t.passed(flag)); add('roomButton', t.roomButton(flag));
    }
    for (const angle of [-720, -360, -22.500001, -22.5, -0, 0, 22.499999, 22.5, 44.9, 337.5, 360, 720]) {
      add('octant', t.octant(angle)); add('qiblaRelative', t.qiblaRelative(angle));
    }
    for (const method of [...METHODS, 'UNKNOWN']) add('methodName', t.methodName(method));
    const station = {id: 'test', ar: 'إذاعة 7 · 8', en: 'Station 7 · 8', whereAr: 'مكان 7 · 8', whereEn: 'Place 7 · 8', url: '', reciter: false};
    add('station', t.station(station)); add('stationWhere', t.stationWhere(station));
    add('radioNote', t.radioNote()); add('screenOffNote', t.screenOffNote()); add('nowHint', t.nowHint()); add('sep', t.sep());
    hashes[lang + '-' + eastern] = createHash('sha256').update(JSON.stringify(values)).digest('hex');
  }
  return hashes;
}

assert.deepEqual(textFormatHashes(), expected, 'Formatter output must preserve the pre-extraction baseline');
console.log('PASS: bilingual formatter edge cases, plurals, nulls, signs and compass boundaries');
