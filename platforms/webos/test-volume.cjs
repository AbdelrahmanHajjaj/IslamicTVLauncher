const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const core = require('./build/service/core');
const source = fs.readFileSync(path.join(__dirname, 'service/service.js'), 'utf8');
const settingsPath = '/media/internal/tv.afnon.webos/settings.json';

function host(sdk = '2.2.0', previous) {
  const env = {sdk, volume: 50, muted: false, calls: [], pending: [], defer: false,
    files: {[settingsPath]: JSON.stringify({prefs: core.prefs({volume: 15}), input: 'HDMI_2', startup: {enabled: true}})}};
  if (previous) { env.files = previous.files; env.volume = previous.volume; env.muted = previous.muted; }
  const handlers = {};
  function Service() {}
  Service.prototype.register = (name, callback) => { handlers[name] = callback; };
  Service.prototype.call = (uri, params, callback) => {
    env.calls.push({uri, params});
    const respond = () => {
      let payload = {returnValue: true};
      if (uri.endsWith('/getSystemInfo')) payload.sdkVersion = env.sdk;
      else if (uri.endsWith('/getVolume')) payload = {returnValue: true, volume: env.volume, muted: env.muted};
      else if (uri.endsWith('/setVolume')) {
        assert.ok(env.state().audioVolume, 'baseline is durable before any native volume write');
        assert.equal(params.muted, undefined, 'volume adapter never changes mute');
        if (!env.failSet && !env.ignoreSet || env.failAfterSet) env.volume = params.volume;
        if (env.failSet || env.failAfterSet) payload = {returnValue: false, errorText: 'Volume unavailable'};
      } else throw Error('Unexpected native call: ' + uri);
      callback({payload});
      if (env.duplicateReplies) callback({payload});
    };
    if (env.defer) env.pending.push(respond); else respond();
  };
  const fakeFs = {
    mkdirSync() {}, lstatSync() { return {uid: 5901, isDirectory: () => true}; },
    readFileSync(file) {
      if (file === '/proc/sys/kernel/random/boot_id') return 'boot';
      if (!(file in env.files)) throw Object.assign(Error('Missing'), {code: 'ENOENT'});
      return env.files[file];
    },
    writeFileSync(file, value) { if (env.failWrite) throw Error('Disk error'); env.files[file] = value; },
    renameSync(from, to) { env.files[to] = env.files[from]; delete env.files[from]; }
  };
  vm.runInNewContext(source, {
    require: name => name === 'fs' ? fakeFs : name === 'webos-service' ? Service : core,
    process: {getuid: () => 5901}, Date, setTimeout, clearTimeout
  });
  env.state = () => JSON.parse(env.files[settingsPath]);
  env.write = state => { env.files[settingsPath] = JSON.stringify(state); };
  env.begin = (action, id = '100-1') => {
    const replies = [];
    handlers.audioVolume({payload: {action, id}, respond(reply) { replies.push(reply); assert.equal(replies.length, 1); }});
    return replies;
  };
  env.invoke = (action, id) => {
    const replies = env.begin(action, id); assert.equal(replies.length, 1);
    return replies[0];
  };
  env.sets = () => env.calls.filter(call => call.uri.endsWith('/setVolume')).map(call => call.params.volume);
  return env;
}

{
  const e = host(), original = e.state();
  assert.equal(e.invoke('prepare').mode, 'tv'); assert.equal(e.volume, 7);
  assert.equal(e.state().audioVolume.confirmed, true);
  assert.equal(e.invoke('release').returnValue, true); assert.equal(e.volume, 50);
  const after = e.state(); delete after.audioVolume;
  assert.deepEqual(after, original, 'preferences, input and startup are untouched');
  assert.deepEqual(e.sets(), [7, 50]);
}
for (const percent of [0, 5, 15, 60, 100]) {
  const e = host(), state = e.state(); state.prefs.volume = percent; e.write(state); e.muted = true;
  assert.equal(e.invoke('prepare').returnValue, true);
  assert.equal(e.volume, Math.floor(50 * percent / 100)); assert.equal(e.muted, true);
  assert.equal(e.invoke('release').returnValue, true); assert.equal(e.volume, 50); assert.equal(e.muted, true);
}
{
  const e = host(); e.invoke('prepare', '100-1');
  e.invoke('prepare', '100-2'); assert.equal(e.volume, 7, 'overlap does not compound attenuation');
  assert.equal(e.invoke('release', '100-1').returnValue, true); assert.equal(e.volume, 7, 'old stop cannot restore over new playback');
  assert.equal(e.invoke('prepare', '100-1').returnValue, false); assert.equal(e.volume, 7, 'older preparation cannot replace a newer owner');
  e.invoke('release', '100-2'); assert.equal(e.volume, 50);
}
{
  const e = host(); e.invoke('prepare'); e.volume = 22; e.muted = true;
  e.invoke('release'); assert.equal(e.volume, 22, 'remote changes win over restoration'); assert.equal(e.muted, true);
  assert.equal(e.state().audioVolume, null);
  e.invoke('prepare', '100-2'); assert.equal(e.volume, 3, 'next playback uses the newly chosen baseline');
  e.invoke('release', '100-2'); assert.equal(e.volume, 22);
}
{
  const before = host(); before.invoke('prepare');
  const restarted = host('2.2.0', before);
  assert.equal(restarted.volume, 7, 'loading the service never raises volume over possibly live audio');
  restarted.invoke('recover', '99-0'); assert.equal(restarted.volume, 7);
  restarted.invoke('recover', '100-0'); assert.equal(restarted.volume, 7);
  restarted.invoke('recover', '200-0'); assert.equal(restarted.volume, 50, 'new silent app instance restores a persisted baseline');
  assert.equal(restarted.state().audioVolume, null);
}
{
  const before = host(); before.invoke('prepare'); before.volume = 19;
  const restarted = host('2.2.0', before); restarted.invoke('recover', '200-0');
  assert.equal(restarted.volume, 19, 'recovery also preserves manual volume changes');
}
for (const version of ['4.0.0', '8.3.0', '10.0.0']) {
  const e = host(version); assert.equal(e.invoke('prepare').mode, 'element');
  e.invoke('release'); assert.equal(e.volume, 50);
  assert.deepEqual(e.sets(), []); assert.equal(e.calls.some(call => call.uri.endsWith('/getVolume')), false);
}
{
  const e = host(); e.invoke('prepare'); e.invoke('release'); e.invoke('prepare', '100-2'); e.invoke('release', '100-2');
  assert.equal(e.calls.filter(call => call.uri.endsWith('/getSystemInfo')).length, 1, 'SDK mode is cached only for this service instance');
}
for (const version of ['', undefined, 'unknown', '0.0.0']) {
  const e = host(version); e.sdk = version;
  assert.equal(e.invoke('prepare').returnValue, false); assert.deepEqual(e.sets(), []);
}
{
  const e = host(); e.volume = '50';
  assert.equal(e.invoke('prepare').returnValue, false); assert.deepEqual(e.sets(), []);
}
{
  const e = host(); e.failWrite = true;
  assert.equal(e.invoke('prepare').returnValue, false); assert.deepEqual(e.sets(), []);
  e.failWrite = false; assert.equal(e.invoke('prepare').returnValue, true);
  e.failSet = true; assert.equal(e.invoke('release').returnValue, false);
  assert.equal(e.state().audioVolume.original, 50, 'failed restoration keeps its recovery record');
  e.failSet = false; e.invoke('release'); assert.equal(e.volume, 50);
}
{
  const e = host(); e.ignoreSet = true;
  assert.equal(e.invoke('prepare').returnValue, false, 'acknowledged but ineffective attenuation blocks playback');
  assert.equal(e.volume, 50); assert.equal(e.state().audioVolume.confirmed, false);
}
{
  const e = host(); e.invoke('prepare'); e.ignoreSet = true;
  assert.equal(e.invoke('release').returnValue, false, 'ineffective restoration is not recorded as complete');
  assert.equal(e.state().audioVolume.original, 50);
  e.ignoreSet = false; e.invoke('release'); assert.equal(e.volume, 50);
}
{
  const e = host(); e.failAfterSet = true;
  assert.equal(e.invoke('prepare').returnValue, false); assert.equal(e.volume, 7);
  e.failAfterSet = false; e.invoke('release'); assert.equal(e.volume, 50, 'ambiguous preparation remains recoverable');
}
{
  const e = host(), state = e.state();
  state.audioVolume = {id: '100-1', original: 200, applied: 7, confirmed: true}; e.write(state);
  assert.equal(e.invoke('release').returnValue, false); assert.deepEqual(e.sets(), []);
  assert.equal(e.invoke('prepare', '../100').returnValue, false);
  assert.equal(e.invoke('unsupported').returnValue, false);
}
{
  const e = host(); e.defer = true;
  const a = e.begin('prepare', '100-1'), stop = e.begin('release', '100-1'), b = e.begin('prepare', '100-2');
  assert.equal(e.pending.length, 1, 'native volume jobs are serialized');
  while (e.pending.length) { assert.equal(e.pending.length, 1); e.pending.shift()(); }
  assert.equal(a[0].returnValue, true); assert.equal(stop[0].returnValue, true); assert.equal(b[0].returnValue, true);
  assert.equal(e.volume, 7); assert.equal(e.state().audioVolume.original, 50);
  e.defer = false; e.invoke('release', '100-2'); assert.equal(e.volume, 50);
}
{
  const e = host(); e.defer = true;
  const pending = e.begin('prepare');
  e.pending.shift()(); e.pending.shift()(); // SDK and captured current volume, then setter is pending.
  const state = e.state(); state.prefs.lang = 'en'; state.input = 'HDMI_3'; e.write(state);
  while (e.pending.length) e.pending.shift()();
  assert.equal(pending[0].returnValue, true);
  assert.equal(e.state().prefs.lang, 'en'); assert.equal(e.state().input, 'HDMI_3', 'async volume writes preserve newer settings/input');
}
{
  const e = host(); e.duplicateReplies = true;
  assert.equal(e.invoke('prepare').returnValue, true); assert.equal(e.invoke('release').returnValue, true);
  assert.deepEqual(e.sets(), [7, 50], 'duplicate native callbacks do not replay a volume write');
}
console.log('PASS: old-LG audio attenuation/verification, restoration, mute/manual overrides, serialized overlap, cold-start recovery, SDK modes and failures.');
