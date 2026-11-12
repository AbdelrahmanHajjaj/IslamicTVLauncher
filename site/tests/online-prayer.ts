import assert from 'node:assert/strict';
import {DEFAULT_PREFS, CITIES, METHOD_DEFINITIONS} from '../src/lib/data';
import {prayerDay} from '../src/lib/prayer';
import {makeCtx} from '../src/lib/ctx';
import {onlineHtml, rowsFor, rowHtml, PRAYER} from '../src/screens/settings';
import {onlineRequest, referenceUrl, parseReference, fetchComparison, ONLINE_MAX_BYTES} from '../src/lib/online-prayer';

const now = new Date('2026-11-12T23:00:00');
const request = onlineRequest(now, DEFAULT_PREFS);
function fixture(): any {
  return {code: 200, data: {date: {gregorian: {date: '12-11-2026'}},
    timings: Object.fromEntries(['Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Isha'].map((key, i) =>
      [key, '2026-11-12T' + ['04:48', '06:18', '11:39', '14:40', '17:00', '18:20'][i] + ':00+02:00'])),
    meta: {latitude: 30.0444, longitude: 31.2357, timezone: 'Africa/Cairo', method: {id: 5, params: {shafaq: 'general'}},
      school: 'STANDARD', latitudeAdjustmentMethod: 'MIDDLE_OF_THE_NIGHT'}}};
}
const originalPrefs = JSON.stringify(DEFAULT_PREFS);
const parsed = parseReference(request, fixture());
assert.deepEqual(parsed.differences, [0, 1, -1, 1, 0, 0]);
assert.equal(request.calculated[0], Date.parse('2026-11-12T04:48:00+02:00'));
assert.notEqual(request.calculated[0], +prayerDay(now, DEFAULT_PREFS).times[0], 'reference uses civil Fajr after Isha');
assert.equal(JSON.stringify(DEFAULT_PREFS), originalPrefs);
const shifted = onlineRequest(now, {...DEFAULT_PREFS, prayerOffsets: [5, 0, -3, 0, 0, 0]});
assert.deepEqual(parseReference(shifted, fixture()).differences, [-5, 1, 2, 1, 0, 0]);
assert.throws(() => onlineRequest(now, {...DEFAULT_PREFS, method: 'UNKNOWN'}));
assert.throws(() => onlineRequest(new Date(NaN), DEFAULT_PREFS));
const invalidResponses: Array<(body: any) => void> = [
  b => { b.code = '200'; }, b => { b.data.date.gregorian.date = '13-11-2026'; },
  b => { b.data.meta.latitude = String(b.data.meta.latitude); }, b => { b.data.meta.longitude += 0.1; },
  b => { b.data.meta.method.id = 4; }, b => { b.data.meta.school = 'HANAFI'; },
  b => { b.data.meta.latitudeAdjustmentMethod = 'ANGLE_BASED'; }, b => { b.data.meta.timezone = '<img src=x>'; },
  b => { delete b.data.timings.Fajr; }, b => { b.data.timings.Asr = '2026-02-30T14:00:00+02:00'; },
  b => { b.data.timings.Asr = '2026-11-12T25:00:00+02:00'; }, b => { b.data.timings.Asr = '2026-11-12T14:00:00+14:59'; },
  b => { b.data.timings.Asr = b.data.timings.Dhuhr; }, b => { b.data.timings.Asr = '2026-11-13T14:00:00+02:00'; },
];
for (const change of invalidResponses) {
  const body = fixture(); change(body); assert.throws(() => parseReference(request, body));
}
assert.throws(() => parseReference(request, null));
for (const method of METHOD_DEFINITIONS) {
  const query = onlineRequest(now, {...DEFAULT_PREFS, method: method.id, hanafi: true});
  const body = fixture();
  body.data.meta.method.id = method.aladhanId;
  body.data.meta.school = 'HANAFI'; body.data.meta.latitudeAdjustmentMethod = method.aladhanId === 15 ? 'NONE' : 'MIDDLE_OF_THE_NIGHT';
  body.data.timings.Isha = '2026-11-13T00:15:00+02:00';
  assert.equal(parseReference(query, body).reference.length, 6, method.id);
  assert.match(referenceUrl(query), new RegExp(`&method=${method.aladhanId}&school=1&`));
}
for (const [cityId, offset, suffix, zone] of [['kolkata', 330, '+05:30', 'Asia/Kolkata'],
  ['melbourne', 660, '+11:00', 'Australia/Melbourne'], ['newyork', -300, '-05:00', 'America/New_York']] as const) {
  const city = CITIES.find(c => c.id === cityId)!;
  const query = onlineRequest(now, {...DEFAULT_PREFS, city: cityId, hanafi: city.hanafi});
  const body = fixture(), method = METHOD_DEFINITIONS.find(m => m.id === city.method)!;
  Object.assign(body.data.meta, {latitude: city.lat, longitude: city.lng, timezone: zone,
    method: {id: method.aladhanId}, school: city.hanafi ? 'HANAFI' : 'STANDARD'});
  for (const key of Object.keys(body.data.timings)) body.data.timings[key] = body.data.timings[key].replace('+02:00', suffix);
  assert.equal(parseReference(query, body).reference[0], Date.UTC(2026, 10, 12, 4, 48) - offset * 60000);
}
for (const lang of ['ar', 'en'] as const) {
  const ctx = makeCtx(lang, {...DEFAULT_PREFS, clock: '24'}, now);
  const rows = rowsFor(ctx, PRAYER), row = rows.find(r => r.id === 'prayer.online')!;
  assert.equal(row.label, ctx.t.s('checkOnline'));
  assert.match(rowHtml(ctx.t, row, rows.indexOf(row)), /data-ui="prayer.online" data-ui-kind="action"/);
  const html = onlineHtml(ctx, parsed);
  assert.equal((html.match(/scope="row"/g) ?? []).length, 6);
  const expected = new Date(request.calculated[4]);
  assert.ok(html.includes(ctx.t.d(String(expected.getHours()).padStart(2, '0') + ':' + String(expected.getMinutes()).padStart(2, '0'))));
  const unsafe = {...parsed, timezone: '<img src=x>'};
  assert.ok(onlineHtml(ctx, unsafe).includes('&lt;img src=x&gt;'));
}

const originalFetch = globalThis.fetch;
try {
  let calls = 0, options: RequestInit | undefined;
  globalThis.fetch = async (url, init) => {
    calls++; options = init; assert.equal(String(url), referenceUrl(request));
    return new Response(JSON.stringify(fixture()), {headers: {'Content-Type': 'application/json; charset=utf-8'}});
  };
  assert.deepEqual((await fetchComparison(request, new AbortController().signal)).differences, parsed.differences);
  assert.equal(options?.credentials, 'omit'); assert.equal(options?.referrerPolicy, 'no-referrer');
  assert.equal(options?.cache, 'no-store'); assert.equal(options?.redirect, 'error');
  assert.equal(options?.signal?.aborted, true, 'connection/stream ownership released on success');
  const cancelled = new AbortController(); cancelled.abort();
  await assert.rejects(() => fetchComparison(request, cancelled.signal)); assert.equal(calls, 1, 'no fetch after prior cancellation');
  for (const [body, status, type] of [['invalid json', 200, 'application/json'], ['{}', 302, 'application/json'],
    ['{}', 503, 'application/json'], ['{}', 200, 'text/html'], [' '.repeat(ONLINE_MAX_BYTES + 1), 200, 'application/json']] as const) {
    globalThis.fetch = async (_url, init) => { options = init; return new Response(body, {status, headers: {'Content-Type': type}}); };
    await assert.rejects(() => fetchComparison(request, new AbortController().signal));
    assert.equal(options?.signal?.aborted, true, 'failed response stream aborted');
  }
  globalThis.fetch = async () => { throw new TypeError('Fixture TLS/network failure'); };
  await assert.rejects(() => fetchComparison(request, new AbortController().signal));
  const encoded = new TextEncoder().encode(JSON.stringify(fixture()));
  globalThis.fetch = async () => new Response(new ReadableStream<Uint8Array>({start(controller) {
    for (let i = 0; i < encoded.length; i += 7) controller.enqueue(encoded.slice(i, i + 7));
    controller.close();
  }}), {headers: {'Content-Type': 'application/json'}});
  assert.deepEqual((await fetchComparison(request, new AbortController().signal)).differences, parsed.differences);
  globalThis.fetch = async () => new Response(new Uint8Array([0xff]), {headers: {'Content-Type': 'application/json'}});
  await assert.rejects(() => fetchComparison(request, new AbortController().signal));
  globalThis.fetch = async (_url, init) => new Response(new ReadableStream<Uint8Array>({start(controller) {
    init!.signal!.addEventListener('abort', () => controller.error(new Error('Aborted while reading')), {once: true});
  }}), {headers: {'Content-Type': 'application/json'}});
  const controller = new AbortController(), pending = fetchComparison(request, controller.signal);
  await Promise.resolve(); controller.abort(); await assert.rejects(() => pending);
} finally { globalThis.fetch = originalFetch; }
console.log('Online preview: strict metadata/ISO/19 methods, raw civil Fajr/offsets, shared action, escaped six-row report, bounded streaming and cancellation passed');
