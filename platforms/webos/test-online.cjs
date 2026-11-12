const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {EventEmitter} = require('node:events');
const core = require('./build/service/core');
const portable = require('./build/service/IslamicTVLauncher-core');
const source = fs.readFileSync(path.join(__dirname, 'service/online-prayer.js'), 'utf8');
const now = new Date('2026-11-12T12:00:00Z');
const prefs = core.prefs({utcOffset: 120});
function fixture() {
  return {code: 200, data: {date: {gregorian: {date: '12-11-2026'}},
    timings: Object.fromEntries(['Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Isha'].map((key, i) =>
      [key, '2026-11-12T' + ['04:48', '06:18', '11:39', '14:40', '17:00', '18:20'][i] + ':00+02:00'])),
    meta: {latitude: 30.0444, longitude: 31.2357, timezone: 'Africa/Cairo', method: {id: 5},
      school: 'STANDARD', latitudeAdjustmentMethod: 'MIDDLE_OF_THE_NIGHT'}}};
}
function start(p = prefs) {
  let respond, options, aborted = false, calls = 0, timeout;
  const req = new EventEmitter();
  req.abort = () => { aborted = true; req.emit('error', new Error('aborted')); };
  const results = [], mod = {exports: {}};
  vm.runInNewContext(source, {module: mod, Buffer, Float64Array, Date,
    setTimeout: fn => { timeout = fn; return 1; }, clearTimeout: () => { timeout = null; },
    require: name => name === 'https' ? {get: (opts, callback) => { options = opts; respond = callback; calls++; return req; }}
      : name === './core' ? core : name === './IslamicTVLauncher-core' ? portable : assert.fail(name)});
  const cancel = mod.exports(p, now, (error, value) => results.push({error, value}));
  function response(body = fixture(), status = 200, type = 'application/json; charset=UTF-8', complete = true) {
    const res = new EventEmitter(); res.statusCode = status; res.headers = {'content-type': type}; res.resume = () => {};
    respond(res);
    if (status === 200) {
      res.emit('data', Buffer.from(typeof body === 'string' ? body : JSON.stringify(body)));
      if (complete) res.emit('end');
      res.emit('close');
    }
    return res;
  }
  return {req, results, cancel, response, timeout: () => timeout(), get options() { return options; },
    get aborted() { return aborted; }, get calls() { return calls; }, pending: () => timeout !== null};
}
{
  const before = JSON.stringify(prefs), t = start(); t.response();
  assert.equal(t.results.length, 1); assert.equal(t.results[0].error, null);
  assert.deepEqual(Array.from(t.results[0].value.differences), [0, 1, -1, 1, 0, 0]);
  assert.equal(JSON.stringify(prefs), before);
  assert.equal(t.options.hostname, 'api.aladhan.com'); assert.equal(t.options.port, 443);
  assert.equal(t.options.rejectUnauthorized, true); assert.equal(t.options.agent, false);
  assert.match(t.options.path, /^\/v1\/timings\/12-11-2026\?latitude=30\.0444&longitude=31\.2357&method=5&school=0&/);
  assert.equal(t.pending(), false); t.cancel(); assert.equal(t.results.length, 1);
}
for (const change of [
  d => { d.date.gregorian.date = '13-11-2026'; }, d => { d.meta.latitude += 0.01; },
  d => { d.meta.longitude = String(d.meta.longitude); }, d => { d.meta.method.id = 4; },
  d => { d.meta.school = 'HANAFI'; }, d => { d.meta.latitudeAdjustmentMethod = 'ANGLE_BASED'; },
  d => { d.meta.timezone = '<img src=x>'; }, d => { delete d.timings.Fajr; },
  d => { d.timings.Asr = '2026-11-12T25:00:00+02:00'; }, d => { d.timings.Asr = '2026-02-30T14:00:00+02:00'; },
  d => { d.timings.Asr = '2026-11-12T14:00:00+14:59'; }, d => { d.timings.Asr = d.timings.Dhuhr; },
  d => { Object.keys(d.timings).forEach(k => { d.timings[k] = d.timings[k].replace('12T', '13T'); }); },
]) {
  const t = start(), body = fixture(); change(body.data); t.response(body);
  assert.ok(t.results[0].error, change.toString()); assert.equal(t.aborted, true); assert.equal(t.results.length, 1);
}
for (const [body, status, type] of [['invalid json', 200, 'application/json'], [fixture(), 302, 'application/json'],
  [fixture(), 503, 'application/json'], [fixture(), 200, 'text/html'], [' '.repeat(32769), 200, 'application/json']]) {
  const t = start(); t.response(body, status, type); assert.ok(t.results[0].error); assert.equal(t.aborted, true); assert.equal(t.calls, 1);
}
for (const kind of ['timeout', 'cancel', 'tls', 'close']) {
  const t = start();
  if (kind === 'timeout') t.timeout();
  if (kind === 'cancel') t.cancel();
  if (kind === 'tls') t.req.emit('error', new Error('CERT_HAS_EXPIRED'));
  if (kind === 'close') t.response(fixture(), 200, 'application/json', false);
  assert.ok(t.results[0].error, kind); assert.equal(t.pending(), false);
  t.response(); assert.equal(t.results.length, 1, 'late response after ' + kind);
}
{
  const t = start(core.prefs({utcOffset: 120, method: 'MOON_SIGHTING_COMMITTEE', hanafi: true})), body = fixture();
  body.data.meta.method = {id: 15, params: {shafaq: 'general'}};
  body.data.meta.school = 'HANAFI'; body.data.meta.latitudeAdjustmentMethod = 'NONE';
  body.data.timings.Isha = '2026-11-13T00:15:00+02:00'; t.response(body);
  assert.equal(t.results[0].error, null, 'Moonsighting metadata and next-date Isha are legitimate');
}
{
  const t = start(core.prefs({utcOffset: 120, offsets: [5, 0, -3, 0, 0, 0]})); t.response();
  assert.deepEqual(Array.from(t.results[0].value.differences), [-5, 1, 2, 1, 0, 0], 'compare saved offsets without changing the reference');
}
for (const [cityId, zone, offset, suffix] of [['kolkata', 'Asia/Kolkata', 330, '+05:30'],
  ['melbourne', 'Australia/Melbourne', 660, '+11:00'], ['newyork', 'America/New_York', -300, '-05:00']]) {
  const city = core.city(cityId), p = core.prefs({city: cityId, utcOffset: offset, hanafi: city.hanafi});
  const t = start(p), body = fixture();
  Object.assign(body.data.meta, {latitude: city.lat, longitude: city.lng, timezone: zone,
    method: {id: portable.tv.afnon.core.PrayerComparison.aladhanMethod(city.method)}, school: p.hanafi ? 'HANAFI' : 'STANDARD'});
  Object.keys(body.data.timings).forEach(key => { body.data.timings[key] = body.data.timings[key].replace('+02:00', suffix); });
  t.response(body); assert.equal(t.results[0].error, null, cityId);
  assert.equal(t.results[0].value.reference[0], Date.UTC(2026, 10, 12, 4, 48) - offset * 60000, 'preserve ISO offset for ' + cityId);
}
{
  const handlers = {}, jobs = [];
  const saved = JSON.stringify({prefs, input: 'HDMI_1', startup: {enabled: true}});
  function Service() {}
  Service.prototype.register = (name, fn) => { handlers[name] = fn; };
  Service.prototype.call = () => assert.fail('online check must not change native TV state');
  const fakeFs = {mkdirSync() {}, lstatSync: () => ({uid: 1, isDirectory: () => true}),
    readFileSync: () => saved, writeFileSync: () => assert.fail('online check must not save state')};
  let failStart = false;
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, 'service/service.js'), 'utf8'), {
    Date, setTimeout, clearTimeout, process: {getuid: () => 1},
    require: name => name === 'fs' ? fakeFs : name === 'webos-service' ? Service : name === './core' ? core
      : name === './online-prayer' ? (p, now, callback) => {
        if (failStart) throw new Error('Calculation unavailable');
        assert.equal(JSON.stringify(p), JSON.stringify(prefs));
        const job = {callback, cancelled: false}; jobs.push(job);
        return () => { job.cancelled = true; callback(new Error('cancelled')); };
      } : assert.fail(name),
  });
  function invoke(payload) {
    const replies = [];
    handlers.onlinePrayer({payload, respond: reply => { replies.push(reply); assert.equal(replies.length, 1); }});
    return replies;
  }
  assert.equal(invoke({action: 'check', id: '../anything'})[0].returnValue, false);
  assert.equal(invoke({action: 'other', id: '100-1'})[0].returnValue, false);
  const first = invoke({action: 'check', id: '100-1'});
  const second = invoke({action: 'check', id: '100-2'});
  assert.equal(first[0].returnValue, false); assert.equal(jobs[0].cancelled, true);
  invoke({action: 'cancel', id: '100-1'}); assert.equal(jobs[1].cancelled, false, 'stale cancel preserves new request');
  invoke({action: 'cancel', id: '100-2'}); assert.equal(second[0].returnValue, false);
  failStart = true; assert.equal(invoke({action: 'check', id: '100-3'})[0].returnValue, false);
  failStart = false;
  const final = invoke({action: 'check', id: '100-4'}); jobs[2].callback(null, {date: '12-11-2026'});
  assert.equal(final[0].returnValue, true);
}
console.log('PASS: manual online HTTPS identity/body/time/metadata validation, offsets, cancellation, timeout and late-response guards. TLS is mocked.');
