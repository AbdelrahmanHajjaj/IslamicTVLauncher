'use strict';
var fs = require('fs');
var Service = require('webos-service');
var appId = 'tv.afnon.bgprobe';
var service = new Service(appId + '.service');
var activityName = appId + '.reminder';
var dataPath = '/media/internal/' + appId;
try { fs.mkdirSync(dataPath, 448); } catch (error) { if (error.code !== 'EEXIST') { throw error; } }
var dataInfo = fs.lstatSync(dataPath);
if (!dataInfo.isDirectory() || dataInfo.uid !== process.getuid()) { throw new Error('Unsafe probe data directory'); }
var statePath = dataPath + '/probe-state.json';
var startedAt = new Date().toISOString();

function readState(path) {
  try { return JSON.parse(fs.readFileSync(path || statePath, 'utf8')); }
  catch (error) { if (error.code === 'ENOENT') { return {}; } throw error; }
}
function save(state, path) {
  path = path || statePath;
  fs.writeFileSync(path + '.tmp', JSON.stringify(state));
  fs.renameSync(path + '.tmp', path);
}
function fail(message, error) { message.respond({returnValue: false, errorText: error.message || String(error)}); }
function register(name, handler) {
  service.register(name, function (message) {
    try { handler(message); } catch (error) { fail(message, error); }
  });
}
function call(uri, params, message, callback) {
  service.call(uri, params, function (reply) {
    try { callback(reply.payload); } catch (error) { fail(message, error); }
  });
}

function launchReminder(message) {
  call('luna://com.webos.service.eim/getCurrentInput', {}, message, function (input) {
    call('luna://com.webos.service.eim/getAllInputStatus', {}, message, function (inputs) {
      var state = readState();
      state.returnTarget = null;
      state.inputCapture = {at: new Date().toISOString(), sourceId: input.mainInputSourceId || null};
      if (input.returnValue) {
        if (input.mainInputSourceId === 'DTV' || input.mainInputSourceId === 'ATV') {
          state.returnTarget = {sourceId: input.mainInputSourceId, appId: 'com.webos.app.livetv'};
        } else if (inputs.returnValue && Array.isArray(inputs.devices)) {
          inputs.devices.forEach(function (device) {
            if (device.id === input.mainInputSourceId && validInputApp(device.appId)) {
              state.returnTarget = {sourceId: device.id, appId: device.appId};
            }
          });
        }
      }
      save(state);
      if (!state.returnTarget) { throw new Error('Cannot identify current input; reminder was not opened'); }
      call('luna://com.webos.applicationManager/launch', {
        id: appId, params: {probeRunId: state.runId, firedAt: state.firedAt, mode: state.mode}
      }, message, function (result) {
        var latest = readState();
        latest.launchResult = result;
        save(latest);
        message.respond({returnValue: result.returnValue, state: latest});
      });
    });
  });
}
function validInputApp(id) {
  return typeof id === 'string' && /^com\.webos\.app\.(hdmi[1-4]|livetv|externalinput\.(av[12]|component))$/.test(id);
}

register('startNow', function (message) {
  var now = new Date().toISOString();
  save({runId: String(Date.now()), mode: 'audio', firedAt: now, trigger: 'manual'});
  launchReminder(message);
});

register('status', function (message) {
  message.respond({returnValue: true, pid: process.pid, startedAt: startedAt, state: readState(), startup: readState(bootPath), boot: bootId()});
});

register('arm', function (message) {
  var mode = message.payload.mode || 'screen';
  if (mode !== 'screen' && mode !== 'audio') { throw new Error('Unknown test mode'); }
  var now = Date.now();
  var state = {runId: String(now), mode: mode, armedAt: new Date(now).toISOString(), armPid: process.pid,
    dueAt: new Date(now + 90000).toISOString()};
  save(state);
  call('luna://com.palm.activitymanager/create', {
    activity: {
      name: activityName,
      description: 'One temporary Afnon reminder',
      type: {background: true, explicit: true, persist: false},
      schedule: {start: state.dueAt.replace('T', ' ').replace(/\.\d{3}Z$/, 'Z'), precise: true},
      callback: {method: 'luna://' + appId + '.service/fire', params: {runId: state.runId}}
    },
    start: true, subscribe: false, replace: true
  }, message, function (result) {
    state.scheduleResult = result;
    save(state);
    message.respond({returnValue: result.returnValue, state: state});
  });
});

register('fire', function (message) {
  var state = readState();
  if (!state.runId || message.payload.runId !== state.runId || state.firedAt || state.cancelledAt) {
    message.respond({returnValue: true, ignored: 'stale, duplicate or cancelled callback'});
    return;
  }
  state.firedAt = new Date().toISOString();
  state.firePid = process.pid;
  state.fireServiceStartedAt = startedAt;
  state.activity = message.payload.$activity || null;
  save(state);
  if (Date.now() > Date.parse(state.dueAt) + 120000) {
    state.expired = true;
    save(state);
    message.respond({returnValue: true, ignored: 'expired reminder'});
    return;
  }
  launchReminder(message);
});

register('visible', function (message) {
  var state = readState();
  if (message.payload.runId && message.payload.runId !== state.runId) {
    message.respond({returnValue: false, errorText: 'Unknown test run'});
    return;
  }
  state[message.payload.runId ? 'reminderScreenLoadedAt' : 'initialScreenLoadedAt'] = new Date().toISOString();
  save(state);
  message.respond({returnValue: true});
});

register('observe', function (message) {
  var data = message.payload;
  var state = readState();
  if (data.runId && data.runId !== state.runId) { throw new Error('Unknown test run'); }
  if (typeof data.event !== 'string' ||
      !/^(app-(visible|hidden|closing|banner-shown|banner-hidden)|audio-(loadedmetadata|playing|ended|pause|error|timeout|progress)|hdmi-(loadedmetadata|playing|pause|error|progress))$/.test(data.event)) {
    throw new Error('Unknown observation');
  }
  var observation = {event: data.event, at: new Date().toISOString()};
  ['currentTime', 'width', 'height', 'errorCode', 'ticks'].forEach(function (key) {
    if (typeof data[key] === 'number' && isFinite(data[key])) { observation[key] = data[key]; }
  });
  state.observations = (state.observations || []).slice(-59);
  state.observations.push(observation);
  save(state);
  message.respond({returnValue: true});
});

register('cancel', function (message) {
  var state = readState();
  state.cancelledAt = new Date().toISOString();
  save(state);
  call('luna://com.palm.activitymanager/cancel', {activityName: activityName}, message, function (result) {
    message.respond({returnValue: result.returnValue || result.errorCode === 2, cancelResult: result});
  });
});

register('restoreInput', function (message) {
  var state = readState();
  if (!state.runId || message.payload.runId !== state.runId || !state.returnTarget || !validInputApp(state.returnTarget.appId)) {
    throw new Error('No valid input recorded for this reminder');
  }
  call('luna://com.webos.applicationManager/launch', {id: state.returnTarget.appId}, message, function (result) {
    state = readState();
    state.inputRestore = {at: new Date().toISOString(), result: result};
    save(state);
    message.respond(result);
  });
});

var bootPath = dataPath + '/startup-state.json';
var bootActivityName = appId + '.startup-test';
function bootId() { return fs.readFileSync('/proc/sys/kernel/random/boot_id', 'utf8').trim(); }
register('enableStartupTest', function (message) {
  var now = Date.now();
  var state = {enabled: true, enabledAt: new Date(now).toISOString(), expiresAt: now + 1200000,
    lastBoot: bootId(), ticks: 0, launches: 0};
  save(state, bootPath);
  call('luna://com.palm.activitymanager/create', {
    activity: {name: bootActivityName, description: 'Temporary once-per-boot Afnon test',
      type: {background: true, explicit: true, persist: true},
      schedule: {interval: '30s', precise: true, end: new Date(state.expiresAt).toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, 'Z')},
      callback: {method: 'luna://' + appId + '.service/startupTick', params: {}}},
    start: true, subscribe: false, replace: true
  }, message, function (result) {
    var latest = readState(bootPath);
    latest.scheduleResult = result;
    if (!result.returnValue) { latest.enabled = false; }
    save(latest, bootPath);
    message.respond({returnValue: result.returnValue, state: latest});
  });
});
register('startupTick', function (message) {
  function complete(restart) {
    if (message.activity) {
      service.activityManager.complete(message.activity, {restart: restart}, function () {
        message.respond({returnValue: true});
      });
    } else { message.respond({returnValue: false, errorText: 'Missing scheduled activity'}); }
  }
  var state = readState(bootPath);
  if (!state.enabled || Date.now() >= state.expiresAt) { complete(false); return; }
  var currentBoot = bootId();
  state.ticks += 1;
  state.lastTickAt = new Date().toISOString();
  state.lastTickBoot = currentBoot;
  var changed = currentBoot !== state.lastBoot;
  if (changed) {
    // Claim this boot before launching so retries cannot repeatedly take the screen.
    state.lastBoot = currentBoot;
    state.launches += 1;
    state.claimedAt = state.lastTickAt;
  }
  save(state, bootPath);
  if (!changed) { complete(true); return; }
  call('luna://com.webos.applicationManager/launch', {id: appId, params: {mode: 'startup', boot: currentBoot}}, message, function (result) {
    var latest = readState(bootPath);
    latest.launchResult = result;
    save(latest, bootPath);
    complete(true);
  });
});
register('disableStartupTest', function (message) {
  var state = readState(bootPath);
  state.enabled = false;
  save(state, bootPath);
  call('luna://com.palm.activitymanager/cancel', {activityName: bootActivityName}, message, function (result) {
    message.respond({returnValue: result.returnValue || result.errorCode === 2, cancelResult: result});
  });
});

register('cleanupData', function (message) {
  if (readState(bootPath).enabled) { throw new Error('Disable startup test before cleanup'); }
  [statePath, bootPath, statePath + '.tmp', bootPath + '.tmp'].forEach(function (path) {
    try { fs.unlinkSync(path); } catch (error) { if (error.code !== 'ENOENT') { throw error; } }
  });
  fs.rmdirSync(dataPath);
  message.respond({returnValue: true});
});
