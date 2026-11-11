const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const original = require('../site/node_modules/adhan/lib/cjs/Adhan.js');
const legacy = vm.createContext({});
vm.runInContext('Symbol = undefined; Math.trunc = undefined; Math.sign = undefined; Number.isNaN = undefined; Intl = undefined;', legacy);
for (const name of ['compat.js', 'adhan.js', 'data.js', 'kotlin-kotlin-stdlib.js', 'IslamicTVLauncher-core.js', 'core.js']) {
  vm.runInContext(fs.readFileSync(path.join(__dirname, 'build/app', name), 'utf8'), legacy);
}
const A = legacy.Afnon, data = legacy.AfnonData;
const prefs = A.prefs({utcOffset: 120});
const now = new Date('2026-11-10T10:00:00Z');
const day = A.day(now, prefs);
const expected = new original.PrayerTimes(new original.Coordinates(30.0444, 31.2357), new Date(2026, 10, 10), original.CalculationMethod.Egyptian());
assert.equal(day.times[0], Math.floor(+expected.fajr / 60000) * 60000);
assert.equal(A.clock(new Date('2026-11-10T10:30:00Z'), prefs), '12:30');
assert.equal(A.wall(new Date('2026-11-10T23:30:00Z'), prefs).getUTCDate(), 11);
assert.equal(A.next(day, day.times[0]).index, 2, 'sunrise is not a prayer notification');
assert.equal(A.next(day, day.times[5]).time, day.tomorrowFajr);
assert.equal(day.times[0], Math.floor(+expected.fajr / 60000) * 60000, 'after Isha, keep today’s Fajr in today’s row');
for (const city of data.cities) {
  const result = A.day(now, A.prefs({city: city.id, hanafi: city.hanafi, utcOffset: 120}));
  assert.ok(result.times.every(Number.isFinite), city.id);
}
for (const method of data.methods) assert.ok(A.day(now, A.prefs({method})).times.every(Number.isFinite), method);
assert.throws(() => A.prefs({utcOffset: 9999}), /Invalid/);
assert.throws(() => A.prefs({city: '<script>'}), /Invalid/);
assert.throws(() => A.prefs({hanafi: 'false'}), /Invalid/);
assert.equal(A.input('HDMI_2').url, 'ext://hdmi:2');
assert.equal(A.input('DTV').appId, 'com.webos.app.livetv');
assert.equal(A.input('com.webos.app.settings'), null);
assert.equal(A.prefs().automatic, false, 'an upgrade does not silently enable scheduled interruptions');
assert.throws(() => A.prefs({volume: -1}), /Invalid/);
assert.throws(() => A.prefs({offsets: [0, 0, 31, 0, 0, 0]}), /Invalid/);
assert.throws(() => A.prefs({bells: [true, true, true, true, true, true]}), /Invalid/);
assert.throws(() => A.prefs({voice: '../settings.json'}), /Invalid/);
assert.throws(() => A.prefs({station: 'https://untrusted.example'}), /Invalid/);
assert.equal(A.prefs({theme: 'aurora'}).homeBar, 'top', 'old preferences gain safe layout defaults');
assert.equal(A.prefs().quickLabels, false);
for (const mode of ['all', 'current', 'next', 'both', 'none']) assert.equal(A.prefs({prayerMode: mode}).prayerMode, mode);
for (const pattern of data.patterns) assert.equal(A.prefs({background: pattern}).background, pattern);
assert.equal(data.patterns.length, 8);
assert.throws(() => A.prefs({quickLabels: 'yes'}), /Invalid/);
assert.throws(() => A.prefs({homeBar: 'left'}), /Invalid/);
assert.throws(() => A.prefs({background: '../outside'}), /Invalid/);
const changed = A.prefs(); changed.bells[0] = false;
assert.equal(A.prefs().bells[0], true, 'editing a preference cannot mutate defaults');
const shifted = A.day(now, A.prefs({utcOffset: 120, offsets: [4, 0, -3, 0, 0, 0]}));
assert.equal(shifted.times[0], day.times[0] + 4 * 60000);
assert.equal(shifted.times[2], day.times[2] - 3 * 60000);
assert.equal(shifted.tomorrowFajr, day.tomorrowFajr + 4 * 60000);
assert.equal(A.clock(new Date('2026-11-10T22:30:00Z'), A.prefs({utcOffset: 120, clock: '12', lang: 'en'})), '12:30 AM');
for (const date of ['2026-02-18T12:00:00Z', '2026-11-10T23:30:00Z', '2030-12-31T12:00:00Z']) {
  for (const offset of [-2, 0, 2]) {
    const p = A.prefs({utcOffset: 120, hijriOffset: offset}), h = A.hijri(new Date(date), p);
    const expectedDate = new Date(+A.wall(new Date(date), p) + offset * 86400000);
    const parts = Object.fromEntries(new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn', {timeZone: 'UTC', year: 'numeric', month: 'numeric', day: 'numeric'}).formatToParts(expectedDate).map(p => [p.type, p.value]));
    assert.equal(h.day, +parts.day); assert.equal(h.month, +parts.month - 1); assert.equal(h.year, +parts.year);
  }
}
assert.equal(A.hijri(new Date('2200-01-01'), prefs), null, 'out-of-table dates are explicit');
assert.ok(A.qibla(prefs) > 130 && A.qibla(prefs) < 140, 'Cairo bearing is southeast');
const enabled = A.prefs({automatic: true, utcOffset: 120});
let event = A.nextEvent(new Date(day.times[0] - 16 * 60000), enabled);
assert.equal(event.kind, 'before'); assert.equal(event.time, day.times[0] - 15 * 60000);
event = A.nextEvent(new Date(day.times[0] - 1), enabled);
assert.equal(event.kind, 'call'); assert.equal(event.time, day.times[0]);
assert.notEqual(A.nextEvent(new Date(day.times[0]), enabled).prayer, 1, 'sunrise never schedules an alert');
assert.notEqual(A.nextEvent(new Date(day.times[0] - 1), enabled, [event.id]).id, event.id, 'clock rollback cannot repeat a handled event');
assert.equal(A.nextEvent(now, A.prefs({automatic: true, bells: [false, false, false, false, false, false]})), null);
assert.equal(A.nextEvent(now, prefs), null);
const serviceCore = require('./build/service/core');
const checkReminders = require('./test-reminders.cjs');
checkReminders(A);
checkReminders(serviceCore);
const checkCalendar = require('./test-calendar.cjs');
checkCalendar(A, data);
checkCalendar(serviceCore, data);
assert.equal(serviceCore.day(now, serviceCore.prefs({utcOffset: 120})).times[0], day.times[0], 'Node service and TV UI use the same calculation');
for (const core of [A, serviceCore]) {
  assert.equal(core.maxPrayerOffset, 30);
  assert.equal(core.maxHijriOffset, 2);
  for (const value of [-3, 3, 0.5, Infinity, '1']) assert.throws(() => core.prefs({hijriOffset: value}), /Invalid/);
  for (const value of [-2, 0, 2]) assert.equal(core.prefs({hijriOffset: value}).hijriOffset, value);
  for (const value of [31, -31, 0.5, 4294967297, -4294967297, NaN, Infinity, null, true, '1', undefined]) {
    assert.throws(() => core.prefs({offsets: [0, 0, value, 0, 0, 0]}), /Invalid/, 'reject before integer conversion: ' + value);
  }
  for (const offsets of [[], Array(6), Array(7).fill(0), '000000']) {
    assert.throws(() => core.prefs({offsets}), /Invalid/, 'reject malformed offset arrays');
  }
  assert.deepEqual(Array.from(core.prefs({offsets: [-30, 30, 0, -1, 1, 0]}).offsets), [-30, 30, 0, -1, 1, 0]);
  const raw = {times: [10, 20, 30, 40, 50, 60], tomorrowFajr: 100};
  for (const [time, current, next, nextTime] of [[9, -1, 0, 10], [10, 0, 2, 30], [20, -1, 2, 30],
    [30, 2, 3, 40], [40, 3, 4, 50], [50, 4, 5, 60], [60, 5, 0, 100], [99, 5, 0, 100]]) {
    const selected = core.select(raw, time);
    assert.deepEqual([selected.current, selected.next, selected.nextTime], [current, next, nextTime]);
    assert.deepEqual({...core.next(raw, time)}, {index: next, time: nextTime});
  }
  assert.deepEqual(raw.times, [10, 20, 30, 40, 50, 60], 'selection preserves the raw today row');
}

const handlers = {}, calls = [], files = {}, completions = [];
let currentBoot = 'boot-a', currentInput = 'HDMI_2', failLaunch = false, failCreate = false, failWrite = false;
let deferActivities = false;
const activityReplies = [];
let clockNow = +now;
class TestDate extends Date { constructor(...args) { super(...(args.length ? args : [clockNow])); } static now() { return clockNow; } }
function Service() {}
Service.prototype.register = (name, fn) => { handlers[name] = fn; };
Service.prototype.activityManager = {complete(activity, options, callback) { completions.push(options); callback({returnValue: true}); }};
Service.prototype.call = (uri, params, callback) => {
  calls.push({uri, params});
  let payload = {returnValue: true};
  if (uri.endsWith('/getCurrentInput')) payload.mainInputSourceId = currentInput;
  if (uri.endsWith('/getAllInputStatus')) payload.devices = [{id: 'HDMI_1'}, {id: 'HDMI_2'}, {id: 'untrusted'}];
  if (uri.endsWith('/launch') && failLaunch) payload = {returnValue: false, errorText: 'Message status unknown.'};
  if (uri.endsWith('/create') && failCreate) payload = {returnValue: false, errorText: 'Unavailable'};
  if (deferActivities && /activitymanager\/(create|cancel)$/.test(uri)) { activityReplies.push(() => callback({payload})); return; }
  callback({payload});
};
const fakeFs = {
  mkdirSync() {}, lstatSync() { return {uid: 5901, isDirectory() { return true; }}; },
  readFileSync(p) {
    if (p === '/proc/sys/kernel/random/boot_id') return currentBoot;
    if (!(p in files)) throw Object.assign(new Error('missing'), {code: 'ENOENT'});
    return files[p];
  },
  writeFileSync(p, value) { if (failWrite) throw new Error('EACCES'); files[p] = value; },
  renameSync(from, to) { files[to] = files[from]; delete files[from]; }
};
vm.runInNewContext(fs.readFileSync(path.join(__dirname, 'service/service.js'), 'utf8'), {
  require(name) { return name === 'fs' ? fakeFs : name === 'webos-service' ? Service : A; },
  process: {getuid() { return 5901; }}, setTimeout, clearTimeout, Date: TestDate
});
function invoke(name, payload = {}, activity = ['startupTick', 'firePrayer', 'fireTest'].includes(name) ? {} : undefined) {
  let result;
  handlers[name]({payload, activity, respond(value) { assert.equal(result, undefined, 'respond once'); result = value; }});
  assert.ok(result, name + ' responds'); return result;
}
assert.equal(invoke('inputs').current.id, 'HDMI_2');
assert.equal(invoke('inputs').inputs.length, 3, 'ignore untrusted source IDs');
assert.equal(invoke('selectInput', {id: 'HDMI_3'}).returnValue, true);
currentInput = null;
assert.equal(invoke('inputs').current.id, 'HDMI_3', 'saved input survives missing native input');
assert.equal(invoke('selectInput', {id: 'random-app'}).returnValue, false);
assert.equal(invoke('exitToInput', {id: 'HDMI_3'}).returnValue, true);
assert.equal(calls.at(-1).params.id, 'com.webos.app.hdmi3');
const before = files['/media/internal/tv.afnon.webos/settings.json'];
assert.equal(invoke('savePrefs', {prefs: {city: 'invalid'}}).returnValue, false);
assert.equal(files['/media/internal/tv.afnon.webos/settings.json'], before, 'invalid write preserves saved state');
failWrite = true;
assert.equal(invoke('savePrefs', {prefs: {lang: 'en'}}).returnValue, false);
assert.equal(files['/media/internal/tv.afnon.webos/settings.json'], before, 'failed write preserves saved state');
failWrite = false;
assert.equal(invoke('setStartup', {enabled: true}).returnValue, true);
assert.equal(calls.at(-1).params.activity.type.persist, true);
assert.equal(calls.at(-1).params.activity.schedule.end, undefined, 'production startup does not expire after a probe');
const launches = () => calls.filter(c => c.uri.endsWith('/launch')).length;
const beforeBoot = launches();
invoke('startupTick'); assert.equal(launches(), beforeBoot);
currentBoot = 'boot-b'; invoke('startupTick'); invoke('startupTick');
assert.equal(launches(), beforeBoot + 1, 'one launch per new boot');
failLaunch = true; currentBoot = 'boot-c'; invoke('startupTick'); invoke('startupTick');
assert.equal(launches(), beforeBoot + 2, 'ambiguous responses cannot repeatedly steal focus');
assert.equal(invoke('status').state.startup.launchResult.returnValue, false);
invoke('setStartup', {enabled: false}); currentBoot = 'boot-d'; invoke('startupTick');
assert.equal(launches(), beforeBoot + 2); assert.equal(completions.at(-1).restart, false);
assert.equal(calls.at(-1).params.activityName, 'tv.afnon.webos.startup');
failCreate = true;
assert.equal(invoke('setStartup', {enabled: true}).returnValue, false);
assert.equal(invoke('status').state.startup.enabled, false);
assert.equal(invoke('report', {view: 'home', instance: '123', input: 'HDMI_3'}).returnValue, true);
assert.equal(invoke('report', {view: 'bad', instance: '123', input: null}).returnValue, false);
failCreate = false; failLaunch = false;
assert.equal(invoke('savePrefs', {prefs: {automatic: true, utcOffset: 120}}).scheduleResult.returnValue, true);
let alarmState = invoke('status').state;
const earlyId = alarmState.alarm.id;
invoke('firePrayer', {id: earlyId, generation: alarmState.alarmGeneration});
assert.equal(invoke('status').state.alarm.id, earlyId, 'early callback keeps the future prayer scheduled');
assert.equal((invoke('status').state.handled || []).includes(earlyId), false, 'clock rollback does not consume a future alarm');
alarmState = invoke('status').state;
const alarmParams = {id: alarmState.alarm.id, generation: alarmState.alarmGeneration};
clockNow = alarmState.alarm.time + 2000;
let count = launches();
assert.equal(invoke('firePrayer', alarmParams).returnValue, true);
assert.equal(launches(), count + 1);
invoke('firePrayer', alarmParams); assert.equal(launches(), count + 1, 'duplicate callback cannot replay adhan');
const pending = invoke('status').state.pendingAlert;
assert.equal(pending.input, 'HDMI_3', 'capture last usable input');
assert.equal(pending.returnToPrevious, false, 'an alert over our visible Home stays inside the launcher');
assert.equal(invoke('takeAlert', {id: pending.id}).returnValue, true);
assert.equal(invoke('takeAlert', {id: pending.id}).returnValue, false, 'relaunch cannot display an alert twice');
alarmState = invoke('status').state; clockNow = alarmState.alarm.time + 91000; count = launches();
invoke('firePrayer', {id: alarmState.alarm.id, generation: alarmState.alarmGeneration});
assert.equal(launches(), count, 'missed standby alerts expire without sound');
assert.ok(invoke('status').state.alarm.time > clockNow, 'expired callback schedules the next future event');
alarmState = invoke('status').state;
invoke('savePrefs', {prefs: {automatic: false}}); clockNow = alarmState.alarm.time;
invoke('firePrayer', {id: alarmState.alarm.id, generation: alarmState.alarmGeneration}); assert.equal(launches(), count, 'off cancels delivery');
assert.equal(invoke('testAlert', {mode: 'adhan', prayer: 1}).returnValue, false);
invoke('report', {view: 'hidden', instance: '123', input: 'HDMI_3'});
assert.equal(invoke('testAlert', {mode: 'adhan', prayer: 0}).returnValue, true);
let test = invoke('status').state.test; clockNow = test.time + 1000;
invoke('fireTest', {id: test.id}); invoke('fireTest', {id: test.id});
assert.equal(launches(), count + 1, 'background test is one-shot');
assert.equal(invoke('status').state.pendingAlert.returnToPrevious, true, 'capture external return before launch changes visibility');
invoke('testAlert', {mode: 'banner', prayer: 2}); test = invoke('status').state.test;
invoke('cancelTest'); clockNow = test.time + 1000; invoke('fireTest', {id: test.id});
assert.equal(launches(), count + 1, 'cancelled test stays cancelled');
assert.equal(invoke('launchApp', {id: 'com.palm.app.firstuse'}).returnValue, false);
assert.equal(invoke('launchApp', {id: 'com.webos.app.adapp'}).returnValue, false, 'hidden LG utilities stay outside the catalog');
assert.equal(invoke('launchApp', {id: '../netflix'}).returnValue, false);
assert.equal(invoke('launchApp', {id: null}).returnValue, false);
for (const app of data.apps) {
  assert.equal(invoke('launchApp', {id: app.id}).returnValue, true);
  assert.equal(calls.at(-1).params.id, app.id, 'shortcut passes only its allowlisted app ID');
}
failLaunch = true;
assert.equal(invoke('launchApp', {id: 'com.webos.app.browser'}).returnValue, false, 'unavailable apps report launch failure');
failLaunch = false;
assert.equal(invoke('media', {kind: 'adhan', event: 'playing', time: 1}).returnValue, true);
assert.equal(invoke('media', {kind: 'adhan', event: 'playing', time: Infinity}).returnValue, false);
// Real Luna replies are asynchronous; overlapping changes must finish in order.
invoke('savePrefs', {prefs: {automatic: true}});
deferActivities = true;
function begin(name, payload = {}) {
  const replies = [];
  handlers[name]({payload, respond(reply) { replies.push(reply); assert.equal(replies.length, 1, 'one asynchronous response'); }});
  return replies;
}
const refreshA = begin('syncAlerts'), refreshB = begin('syncAlerts');
const disabled = begin('savePrefs', {prefs: {automatic: false}});
assert.equal(activityReplies.length, 1, 'only one Activity Manager request may be in flight');
for (let i = 0; i < 3; i++) {
  assert.equal(activityReplies.length, 1);
  activityReplies.shift()();
}
assert.equal(activityReplies.length, 0, 'overlapping requests settle without rescheduling each other');
for (const reply of [refreshA, refreshB, disabled]) { assert.equal(reply.length, 1); assert.equal(reply[0].returnValue, true); }
assert.equal(invoke('status').state.alarm, null, 'a queued disable wins over in-flight creates');
assert.equal(calls.at(-1).uri, 'luna://com.palm.activitymanager/cancel');
const queuedEnable = begin('savePrefs', {prefs: {automatic: true}});
const recovery = begin('syncAlerts');
failWrite = true; activityReplies.shift()(); failWrite = false;
assert.equal(queuedEnable[0].scheduleResult.returnValue, false, 'asynchronous disk errors are reported');
assert.equal(recovery[0].returnValue, false, 'a failed queued read/write does not leave a pending request');
const afterFailure = begin('syncAlerts'); activityReplies.shift()();
assert.equal(afterFailure[0].returnValue, true, 'queue remains usable after failures');
assert.equal(activityReplies.length, 0);
deferActivities = false;
for (const lateness of [-1, 0, 90000, 90001]) {
  invoke('savePrefs', {prefs: {automatic: true, utcOffset: 120}});
  const state = invoke('status').state, alarm = state.alarm;
  const params = {id: alarm.id, generation: state.alarmGeneration};
  const before = launches();
  clockNow = alarm.time + lateness;
  invoke('firePrayer', params);
  const delivered = lateness >= 0 && lateness <= 90000;
  assert.equal(launches(), before + Number(delivered), 'production delivery boundary: ' + lateness);
  assert.equal((invoke('status').state.handled || []).includes(alarm.id), lateness >= 0,
    'early remains pending; due/expired events are claimed before delivery');
  invoke('firePrayer', params);
  assert.equal(launches(), before + Number(delivered), 'stale generation/duplicate does not replay');
  if (delivered) {
    const pending = invoke('status').state.pendingAlert;
    clockNow = pending.createdAt - 1;
    assert.equal(invoke('takeAlert', {id: pending.id}).returnValue, false, 'clock rollback cannot display a future alert');
    clockNow = pending.createdAt + 90000;
    assert.equal(invoke('takeAlert', {id: pending.id}).returnValue, true, 'pending alert allows exactly 90 seconds');
    assert.equal(invoke('takeAlert', {id: pending.id}).returnValue, false, 'display claim survives a repeated request');
  }
}
invoke('testAlert', {mode: 'banner', prayer: 0});
test = invoke('status').state.test;
clockNow = test.time + 90000; count = launches();
invoke('fireTest', {id: test.id});
assert.equal(launches(), count + 1, 'manual test allows exactly 90 seconds');
const latePending = invoke('status').state.pendingAlert;
clockNow = latePending.createdAt + 90001;
assert.equal(invoke('takeAlert', {id: latePending.id}).returnValue, false, 'pending display expires after 90 seconds');
invoke('testAlert', {mode: 'banner', prayer: 0});
test = invoke('status').state.test; clockNow = test.time + 90001; count = launches();
invoke('fireTest', {id: test.id});
assert.equal(launches(), count, 'expired manual test stays silent');
console.log('PASS: legacy calculations, preferences, atomic state, startup, exact prayer scheduling, asynchronous overlap/failure recovery, expired/duplicate/cancelled alerts and media diagnostics.');
