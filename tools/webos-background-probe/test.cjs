'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const handlers = {};
const calls = [];
const files = {};
let currentInput = 'HDMI_1';
let currentBoot = 'boot-a';
let clock = Date.now();
class ClockDate extends Date { constructor(...args) { super(...(args.length ? args : [clock])); } static now() { return clock; } }
const completions = [];
function Service() {}
Service.prototype.activityManager = {complete(activity, options, callback) { completions.push(options); callback({returnValue: true}); }};
Service.prototype.register = (name, handler) => { handlers[name] = handler; };
Service.prototype.call = (uri, params, callback) => {
  calls.push({uri, params});
  let payload = {returnValue: true, activityId: 123};
  if (uri.endsWith('/getCurrentInput')) { payload.mainInputSourceId = currentInput; }
  if (uri.endsWith('/getAllInputStatus')) { payload.devices = [1, 2].map(n => ({id: 'HDMI_' + n, appId: 'com.webos.app.hdmi' + n})); }
  callback({payload});
};
const fakeFs = {
  mkdirSync() {},
  lstatSync() { return {uid: 5901, isDirectory() { return true; }}; },
  readFileSync(path) {
    if (path === '/proc/sys/kernel/random/boot_id') { return currentBoot; }
    if (!(path in files)) { throw Object.assign(new Error('absent'), {code: 'ENOENT'}); }
    return files[path];
  },
  writeFileSync(path, content) { files[path] = content; },
  renameSync(from, to) { files[to] = files[from]; delete files[from]; },
  unlinkSync(path) { delete files[path]; }, rmdirSync() {}
};
vm.runInNewContext(fs.readFileSync(__dirname + '/service/service.js', 'utf8'), {
  require(name) { return name === 'fs' ? fakeFs : Service; },
  __dirname: '/probe', process: {pid: 100, getuid() { return 5901; }}, Date: ClockDate
});
function invoke(name, payload = {}) {
  let result;
  handlers[name]({payload, activity: name === 'startupTick' ? {} : undefined, respond(value) { result = value; }});
  assert.ok(result, name + ' must respond');
  return result;
}
assert.equal(invoke('arm', {mode: 'invalid'}).returnValue, false);
assert.equal(calls.length, 0, 'invalid mode must not schedule');
const armed = invoke('arm', {mode: 'audio'});
assert.equal(armed.returnValue, true);
assert.equal(Date.parse(armed.state.dueAt) - Date.parse(armed.state.armedAt), 90000);
assert.match(calls[0].params.activity.schedule.start, /^\d{4}-\d\d-\d\d \d\d:\d\d:\d\dZ$/);
assert.equal(calls[0].params.subscribe, false);
assert.equal(calls[0].params.activity.type.persist, false);
invoke('fire', {runId: 'wrong'});
assert.equal(calls.length, 1, 'stale callback must not launch');
currentInput = 'HDMI_2';
invoke('fire', {runId: armed.state.runId});
assert.equal(calls[3].uri, 'luna://com.webos.applicationManager/launch');
assert.equal(calls[3].params.params.mode, 'audio');
assert.equal(invoke('status').state.returnTarget.sourceId, 'HDMI_2', 'capture source at firing, not scheduling');
invoke('fire', {runId: armed.state.runId});
assert.equal(calls.length, 4, 'duplicate callback must not launch again');
invoke('visible', {runId: armed.state.runId});
assert.ok(invoke('status').state.reminderScreenLoadedAt);
invoke('cancel');
assert.equal(calls[4].params.activityName, 'tv.afnon.bgprobe.reminder');
const failed = invoke('visible', {runId: 'wrong'});
assert.equal(failed.returnValue, false);
assert.equal(invoke('observe', {event: 'audio-playing', runId: 'wrong'}).returnValue, false);
assert.equal(invoke('observe', {event: 'unexpected'}).returnValue, false);
invoke('observe', {event: 'audio-playing', runId: armed.state.runId, currentTime: 1.2, secret: 'ignored'});
const recorded = invoke('status').state.observations[0];
assert.equal(recorded.event, 'audio-playing');
assert.equal(recorded.currentTime, 1.2);
assert.equal(recorded.secret, undefined);
assert.equal(invoke('restoreInput', {runId: 'wrong'}).returnValue, false);
invoke('restoreInput', {runId: armed.state.runId});
assert.equal(calls[calls.length - 1].uri, 'luna://com.webos.applicationManager/launch');
assert.deepEqual(JSON.parse(JSON.stringify(calls[calls.length - 1].params)), {id: 'com.webos.app.hdmi2'});
assert.equal(invoke('status').state.inputRestore.result.returnValue, true);
currentInput = 'DTV'; clock += 1;
const liveTv = invoke('startNow');
assert.equal(liveTv.state.returnTarget.appId, 'com.webos.app.livetv');
currentInput = 'unknown'; clock += 1;
const beforeInvalid = calls.filter(c => c.uri.endsWith('/launch')).length;
assert.equal(invoke('startNow').returnValue, false);
assert.equal(invoke('status').state.returnTarget, null, 'unknown input must not reuse a stale target');
assert.equal(calls.filter(c => c.uri.endsWith('/launch')).length, beforeInvalid);

const startup = invoke('enableStartupTest');
assert.equal(startup.returnValue, true);
assert.equal(calls[calls.length - 1].params.activity.type.persist, true);
assert.equal(startup.state.expiresAt - clock, 1200000);
const beforeBoot = calls.filter(c => c.uri.endsWith('/launch')).length;
invoke('startupTick');
assert.equal(calls.filter(c => c.uri.endsWith('/launch')).length, beforeBoot, 'must not launch during the enabling boot');
currentBoot = 'boot-b';
invoke('startupTick'); invoke('startupTick');
assert.equal(calls.filter(c => c.uri.endsWith('/launch')).length, beforeBoot + 1, 'at most one launch per boot');
assert.equal(JSON.parse(files['/media/internal/tv.afnon.bgprobe/startup-state.json']).launches, 1);
clock = startup.state.expiresAt;
currentBoot = 'boot-c';
invoke('startupTick');
assert.equal(calls.filter(c => c.uri.endsWith('/launch')).length, beforeBoot + 1, 'expired startup test must not launch');
assert.equal(completions[completions.length - 1].restart, false);
invoke('disableStartupTest');
assert.equal(calls[calls.length - 1].params.activityName, 'tv.afnon.bgprobe.startup-test');
assert.equal(JSON.parse(files['/media/internal/tv.afnon.bgprobe/startup-state.json']).enabled, false);
invoke('cleanupData');
assert.equal(Object.keys(files).length, 0, 'cleanup removes only the probe data files');
function checkScreen(mode) {
  const nodes = {};
  const timers = [];
  const requests = [];
  function Bridge() {}
  Bridge.prototype.call = function (uri, body) { requests.push({uri, body}); this.onservicecallback('{"returnValue":true}'); };
  ['tone', 'hdmi', 'result', 'counter', 'banner'].forEach(id => {
    nodes[id] = {style: {}, listeners: {}, currentTime: 0, children: [],
      addEventListener(name, fn) { this.listeners[name] = fn; },
      appendChild(child) { this.children.push(child); },
      load() {}, play() { this.played = true; }, pause() { this.paused = true; }};
  });
  const window = {PalmSystem: {launchParams: JSON.stringify({mode})}, close() { this.closed = true; }};
  vm.runInNewContext(fs.readFileSync(__dirname + '/app/probe.js', 'utf8'), {
    window, PalmServiceBridge: Bridge,
    document: {getElementById(id) { return nodes[id]; }, createElement() { return {}; }, addEventListener() {}},
    setTimeout(fn, ms) { timers.push({fn, ms}); }, setInterval() {}
  });
  return {nodes, timers, window, requests};
}
const hdmi = checkScreen('hdmi');
assert.equal(hdmi.nodes.hdmi.children[0].src, 'ext://hdmi:1');
assert.equal(hdmi.nodes.tone.src, undefined, 'HDMI must not load the competing audio decoder');
assert.equal(hdmi.nodes.tone.played, undefined);
assert.ok(hdmi.timers.some(t => t.ms === 300000), 'HDMI probe must be bounded');
hdmi.timers.find(t => t.ms === 4000).fn();
assert.equal(hdmi.nodes.banner.style.display, 'block');
hdmi.timers.find(t => t.ms === 100000).fn();
assert.equal(hdmi.nodes.banner.style.display, 'none');
const audioScreen = checkScreen('audio');
assert.equal(audioScreen.nodes.hdmi.children.length, 0);
assert.equal(audioScreen.nodes.tone.src, 'test-tone.mp3');
assert.equal(audioScreen.nodes.tone.volume, 0.25);
audioScreen.timers.find(t => t.ms === 12000).fn();
assert.equal(audioScreen.nodes.tone.paused, true);
assert.ok(audioScreen.requests.some(r => r.uri.endsWith('/restoreInput')), 'audio exit must restore the recorded input');
audioScreen.timers.find(t => t.ms === 300).fn();
assert.equal(audioScreen.window.closed, true, 'audio probe must close after its timeout');
console.log('Probe checks passed: scheduling, deduplication, validated observations, separate media paths and bounded cleanup.');
