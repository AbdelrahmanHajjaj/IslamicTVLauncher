'use strict';
var fs = require('fs');
var Service = require('webos-service');
var core = require('./core');
var appId = 'tv.afnon.webos';
var service = new Service(appId + '.service');
var activityName = appId + '.startup';
var dir = '/media/internal/' + appId;
try { fs.mkdirSync(dir, 448); } catch (error) { if (error.code !== 'EEXIST') { throw error; } }
var info = fs.lstatSync(dir);
if (!info.isDirectory() || info.uid !== process.getuid()) { throw new Error('Unsafe Afnon data directory'); }
var path = dir + '/settings.json';
function read() {
  try { return JSON.parse(fs.readFileSync(path, 'utf8')); }
  catch (error) { if (error.code === 'ENOENT') { return {prefs: core.prefs()}; } throw error; }
}
function save(state) { fs.writeFileSync(path + '.tmp', JSON.stringify(state)); fs.renameSync(path + '.tmp', path); }
function boot() { return fs.readFileSync('/proc/sys/kernel/random/boot_id', 'utf8').trim(); }
function register(name, fn) {
  service.register(name, function (message) {
    try { fn(message); } catch (error) { message.respond({returnValue: false, errorText: error.message}); }
  });
}
function call(uri, params, callback) {
  var done = false;
  var timer = setTimeout(function () { finish({returnValue: false, errorText: 'Service response timed out'}); }, 6000);
  function finish(result) { if (done) { return; } done = true; clearTimeout(timer); callback(result); }
  try { service.call(uri, params, function (reply) { finish(reply.payload || {}); }); }
  catch (error) { finish({returnValue: false, errorText: error.message}); }
}
function safe(message, callback) {
  return function (result) { try { callback(result); } catch (error) { message.respond({returnValue: false, errorText: error.message}); } };
}
var volumeQueue = [], volumeSdk = null;
function volumeId(id) { return typeof id === 'string' && /^[0-9]{1,16}-[0-9]{1,10}$/.test(id); }
function volumeLevel(value) { return typeof value === 'number' && isFinite(value) && value % 1 === 0 && value >= 0 && value <= 100; }
function volumeOwner() {
  var owner = read().audioVolume;
  if (owner && (!volumeId(owner.id) || !volumeLevel(owner.original) || !volumeLevel(owner.applied) || owner.applied > owner.original || typeof owner.confirmed !== 'boolean')) {
    throw new Error('Invalid saved audio volume');
  }
  return owner;
}
function saveVolume(owner) { var state = read(); state.audioVolume = owner; save(state); }
function newerVolumeId(a, b) {
  var aa = a.split('-').map(Number), bb = b.split('-').map(Number);
  return aa[0] > bb[0] || aa[0] === bb[0] && aa[1] > bb[1];
}
function runVolume() {
  var job = volumeQueue[0], finished = false;
  function finish(result) {
    if (finished) { return; } finished = true;
    try { job.respond(result); }
    finally { volumeQueue.shift(); if (volumeQueue.length) { runVolume(); } }
  }
  function checked(uri, params, next) {
    call(uri, params, function (reply) {
      if (finished) { return; }
      try {
        if (reply.returnValue !== true) { throw new Error(reply.errorText || 'TV audio level request failed'); }
        next(reply);
      } catch (error) { finish({returnValue: false, errorText: error.message}); }
    });
  }
  function current(next) {
    checked('luna://com.webos.audio/getVolume', {}, function (reply) {
      if (!volumeLevel(reply.volume)) { throw new Error('Unknown TV audio level'); }
      next(reply.volume);
    });
  }
  function restore() {
    var owner = volumeOwner();
    if (!owner || (job.action === 'release' ? owner.id !== job.id : !newerVolumeId(job.id, owner.id))) {
      finish({returnValue: true}); return;
    }
    current(function (level) {
      if (level !== owner.applied) {
        // An unacknowledged setter may still finish; keep its baseline for the next safe recovery.
        if (owner.confirmed) { saveVolume(null); }
        finish({returnValue: true}); return;
      }
      checked('luna://com.webos.audio/setVolume', {volume: owner.original}, function () {
        current(function (actual) {
          if (actual === owner.applied && actual !== owner.original) { throw new Error('TV audio level was not restored'); }
          saveVolume(null); finish({returnValue: true});
        });
      });
    });
  }
  function prepare() {
    if (volumeSdk >= 4) { finish({returnValue: true, mode: 'element'}); return; }
    current(function (level) {
      var previous = volumeOwner();
      if (previous && newerVolumeId(previous.id, job.id)) { throw new Error('Audio request superseded'); }
      var original = previous && level === previous.applied ? previous.original : level;
      var applied = Math.floor(original * core.prefs(read().prefs).volume / 100);
      var owner = {id: job.id, original: original, applied: applied, confirmed: false};
      saveVolume(owner);
      checked('luna://com.webos.audio/setVolume', {volume: applied}, function () {
        current(function (actual) {
          if (actual !== applied) { throw new Error('TV audio level was not applied'); }
          owner.confirmed = true; saveVolume(owner);
          finish({returnValue: true, mode: 'tv'});
        });
      });
    });
  }
  try {
    if (job.action !== 'prepare') { restore(); }
    else if (volumeSdk !== null) { prepare(); }
    else {
      checked('luna://com.webos.service.tv.systemproperty/getSystemInfo', {keys: ['sdkVersion']}, function (reply) {
        if (typeof reply.sdkVersion !== 'string' || !/^[1-9][0-9]*\.[0-9]/.test(reply.sdkVersion)) { throw new Error('Unknown TV audio support'); }
        volumeSdk = Number(reply.sdkVersion.split('.')[0]); prepare();
      });
    }
  } catch (error) { finish({returnValue: false, errorText: error.message}); }
}
register('audioVolume', function (message) {
  var p = message.payload;
  if (!volumeId(p.id) || ['prepare', 'release', 'recover'].indexOf(p.action) < 0) { throw new Error('Invalid audio volume request'); }
  // shortcut: crash recovery waits for a silent new app instance; add earlier recovery only with a reliable playback-stop signal.
  volumeQueue.push({id: p.id, action: p.action, respond: function (reply) { message.respond(reply); }});
  if (volumeQueue.length === 1) { runVolume(); }
});
register('status', function (message) { message.respond({returnValue: true, state: read(), boot: boot()}); });
var onlineJob = null;
register('onlinePrayer', function (message) {
  var p = message.payload;
  if (typeof p.id !== 'string' || !/^[0-9]{1,16}-[0-9]{1,10}$/.test(p.id) || ['check', 'cancel'].indexOf(p.action) < 0) {
    throw new Error('Invalid online check');
  }
  if (p.action === 'cancel') {
    if (onlineJob && onlineJob.id === p.id && onlineJob.cancel) { onlineJob.cancel(); }
    message.respond({returnValue: true}); return;
  }
  var prefs = core.prefs(read().prefs);
  if (onlineJob && onlineJob.cancel) { onlineJob.cancel(); }
  var job = {id: p.id, cancel: null}; onlineJob = job;
  try {
    job.cancel = require('./online-prayer')(prefs, new Date(), function (error, result) {
      if (onlineJob === job) { onlineJob = null; }
      message.respond(error ? {returnValue: false, errorText: error.message} : {returnValue: true, comparison: result});
    });
  } catch (error) { if (onlineJob === job) { onlineJob = null; } throw error; }
});
register('inputs', function (message) {
  call('luna://com.webos.service.eim/getCurrentInput', {}, safe(message, function (current) {
    call('luna://com.webos.service.eim/getAllInputStatus', {}, safe(message, function (all) {
      var inputs = [], seen = {};
      (Array.isArray(all.devices) ? all.devices : []).forEach(function (item) {
        var target = core.input(item.id);
        if (target && !seen[target.id]) { inputs.push(target); seen[target.id] = true; }
      });
      inputs.push(core.input('DTV'));
      var state = read(), selected = core.input(current.mainInputSourceId) || core.input(state.input);
      message.respond({returnValue: true, inputs: inputs, current: selected, discoveryFailed: !all.returnValue || !current.returnValue});
    }));
  }));
});
register('savePrefs', function (message) {
  if (!message.payload.prefs || typeof message.payload.prefs !== 'object' || Array.isArray(message.payload.prefs)) { throw new Error('Missing preferences'); }
  var state = read(); state.prefs = core.prefs(message.payload.prefs); save(state);
  scheduleAlerts(safe(message, function (result) {
    message.respond({returnValue: true, prefs: read().prefs, scheduleResult: result});
  }));
});
register('selectInput', function (message) {
  var input = core.input(message.payload.id);
  if (!input) { throw new Error('Unknown input'); }
  var state = read(); state.input = input.id; save(state);
  message.respond({returnValue: true, input: input});
});
register('exitToInput', function (message) {
  var input = core.input(message.payload.id);
  if (!input) { throw new Error('Unknown input'); }
  call('luna://com.webos.applicationManager/launch', {id: input.appId}, safe(message, function (result) { message.respond(result); }));
});
register('setStartup', function (message) {
  var enabled = message.payload.enabled;
  if (typeof enabled !== 'boolean') { throw new Error('Expected enabled boolean'); }
  var state = read();
  state.startup = {enabled: enabled, lastBoot: boot(), launches: state.startup ? state.startup.launches || 0 : 0};
  save(state);
  if (!enabled) {
    call('luna://com.palm.activitymanager/cancel', {activityName: activityName}, safe(message, function (result) {
      message.respond({returnValue: result.returnValue || result.errorCode === 2, enabled: false, result: result});
    }));
    return;
  }
  call('luna://com.palm.activitymanager/create', {
    activity: {name: activityName, description: 'Open Islamic TV Launcher once after TV boot',
      type: {background: true, explicit: true, persist: true}, schedule: {interval: '30s', precise: true},
      callback: {method: 'luna://' + appId + '.service/startupTick', params: {}}},
    start: true, subscribe: false, replace: true
  }, safe(message, function (result) {
    var latest = read();
    latest.startup.enabled = !!result.returnValue;
    latest.startup.scheduleResult = result;
    save(latest);
    message.respond({returnValue: !!result.returnValue, enabled: latest.startup.enabled, result: result});
  }));
});
register('startupTick', function (message) {
  if (!message.activity) { throw new Error('Missing scheduled activity'); }
  function complete(restart) {
    service.activityManager.complete(message.activity, {restart: restart}, function () { message.respond({returnValue: true}); });
  }
  var state = read(), start = state.startup;
  if (!start || !start.enabled) { complete(false); return; }
  var current = boot();
  if (current === start.lastBoot) { complete(true); return; }
  // Claim first: even an ambiguous launch response must not cause a focus-stealing loop.
  start.lastBoot = current; start.launches += 1; start.launchedAt = new Date().toISOString();
  save(state);
  call('luna://com.webos.applicationManager/launch', {id: appId, params: {reason: 'boot'}}, function (result) {
    try { var latest = read(); latest.startup.launchResult = result; save(latest); }
    finally { complete(true); }
  });
});
register('report', function (message) {
  var p = message.payload;
  if (['input', 'bar', 'home', 'settings', 'inputs', 'picker', 'prayer-day', 'online-prayer', 'radio', 'apps', 'clock', 'error', 'hidden'].indexOf(p.view) < 0 ||
      typeof p.instance !== 'string' || !/^[0-9-]{1,40}$/.test(p.instance) ||
      (p.input !== null && !core.input(p.input))) { throw new Error('Invalid app status'); }
  var state = read();
  state.ui = {at: new Date().toISOString(), view: p.view, instance: p.instance, input: p.input,
    error: typeof p.error === 'string' ? p.error.slice(0, 180) : null};
  save(state); message.respond({returnValue: true});
});

var alarmName = appId + '.prayers', testName = appId + '.test', scheduleQueue = [];
function scheduleAlerts(callback) {
  scheduleQueue.push(callback);
  if (scheduleQueue.length === 1) { runSchedule(); }
}
function runSchedule() {
  var completed = false;
  function finish(result) {
    if (completed) { return; } completed = true;
    var callback = scheduleQueue[0];
    try { callback(result); }
    finally { scheduleQueue.shift(); if (scheduleQueue.length) { runSchedule(); } }
  }
  try { scheduleNextAlert(finish); }
  catch (error) { finish({returnValue: false, errorText: error.message}); }
}
function scheduleNextAlert(callback) {
  var state = read(), p = core.prefs(state.prefs), next, calculationError = null;
  try { next = core.nextEvent(new Date(), p, state.handled); }
  catch (error) { next = null; calculationError = error.message; }
  var generation = (state.alarmGeneration || 0) + 1;
  state.alarmGeneration = generation; state.alarm = next; save(state);
  if (!next) {
    state.alarmResult = calculationError ? {returnValue: false, errorText: calculationError} : {returnValue: true}; save(state);
    call('luna://com.palm.activitymanager/cancel', {activityName: alarmName}, function (result) {
      callback({returnValue: !calculationError && (!!result.returnValue || result.errorCode === 2), next: null, errorText: calculationError || result.errorText});
    });
    return;
  }
  call('luna://com.palm.activitymanager/create', {
    activity: {name: alarmName, description: 'Islamic TV Launcher next prayer alert',
      type: {background: true, explicit: true, persist: true},
      schedule: {start: new Date(next.time).toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, 'Z'), precise: true},
      callback: {method: 'luna://' + appId + '.service/firePrayer', params: {generation: generation, id: next.id}}},
    start: true, subscribe: false, replace: true
  }, function (result) {
    try { var latest = read(); latest.alarmResult = result; save(latest); }
    catch (error) { callback({returnValue: false, errorText: error.message}); return; }
    callback({returnValue: !!result.returnValue, next: next, errorText: result.errorText});
  });
}
register('syncAlerts', function (message) { scheduleAlerts(safe(message, function (result) { message.respond(result); })); });
function deliver(event, callback) {
  call('luna://com.webos.service.eim/getCurrentInput', {}, function (current) {
    var state = read(), target = core.input(current.mainInputSourceId) || core.input(state.input);
    if (!target) { callback({returnValue: false, errorText: 'Cannot identify the input to restore'}); return; }
    // shortcut: webOS 2 has no public foreground lookup; use our last visibility report and LG's app history.
    event.returnToPrevious = !state.ui || state.ui.view === 'hidden';
    event.input = target.id; event.createdAt = Date.now(); state.pendingAlert = event; save(state);
    call('luna://com.webos.applicationManager/launch', {id: appId, params: {alertId: event.id}}, function (result) {
      var latest = read(); latest.alertLaunch = result; save(latest); callback(result);
    });
  });
}
function complete(message, callback) {
  service.activityManager.complete(message.activity, {restart: false}, callback);
}
register('firePrayer', function (message) {
  if (!message.activity) { throw new Error('Missing scheduled activity'); }
  var state = read(), event = state.alarm, p = core.prefs(state.prefs);
  if (!p.automatic || !event || message.payload.generation !== state.alarmGeneration || message.payload.id !== event.id ||
      (state.handled || []).indexOf(event.id) >= 0) {
    complete(message, function () { message.respond({returnValue: true, ignored: true}); }); return;
  }
  function finish(result) {
    complete(message, function () { scheduleAlerts(safe(message, function (scheduled) {
      message.respond({returnValue: !!result.returnValue, delivery: result, scheduleResult: scheduled});
    })); });
  }
  if (core.deliveryState(event.time, Date.now()) === 'early') { finish({returnValue: true, ignored: 'early'}); return; }
  // Claim before launch; retries and clock rollback must never repeat an audible call.
  state.handled = (state.handled || []).slice(-23); state.handled.push(event.id); save(state);
  if (core.deliveryState(event.time, Date.now()) === 'expired') { finish({returnValue: true, ignored: 'expired'}); return; }
  event.mode = event.kind === 'before' ? (p.remindVoice ? 'spoken' : 'banner') : p.adhan ? 'adhan' : 'announcement';
  deliver(event, finish);
});
register('takeAlert', function (message) {
  var state = read(), event = state.pendingAlert;
  if (!event || event.id !== message.payload.id || event.displayedAt || core.deliveryState(event.createdAt, Date.now()) !== 'due') {
    message.respond({returnValue: false, errorText: 'Alert is expired or already displayed'}); return;
  }
  event.displayedAt = Date.now(); save(state); message.respond({returnValue: true, alert: event});
});
register('testAlert', function (message) {
  var p = message.payload;
  if (['banner', 'card', 'adhan', 'spoken', 'announcement'].indexOf(p.mode) < 0 || [0, 2, 3, 4, 5].indexOf(p.prayer) < 0) { throw new Error('Invalid test'); }
  var state = read(), event = {id: 'test:' + Date.now(), mode: p.mode, prayer: p.prayer, test: true, background: true, time: Date.now() + 30000};
  state.test = event; save(state);
  call('luna://com.palm.activitymanager/create', {
    activity: {name: testName, description: 'One Islamic TV Launcher test in 30 seconds',
      type: {background: true, explicit: true, persist: false},
      schedule: {start: new Date(event.time).toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, 'Z'), precise: true},
      callback: {method: 'luna://' + appId + '.service/fireTest', params: {id: event.id}}},
    start: true, subscribe: false, replace: true
  }, safe(message, function (result) { message.respond({returnValue: !!result.returnValue, due: event.time, errorText: result.errorText}); }));
});
register('fireTest', function (message) {
  if (!message.activity) { throw new Error('Missing scheduled activity'); }
  var state = read(), event = state.test;
  function done(result) { complete(message, function () { message.respond(result); }); }
  if (!event || event.id !== message.payload.id || event.fired || core.deliveryState(event.time, Date.now()) !== 'due') {
    done({returnValue: true, ignored: true}); return;
  }
  event.fired = true; save(state); deliver(event, done);
});
register('cancelTest', function (message) {
  var state = read(); state.test = null; save(state);
  call('luna://com.palm.activitymanager/cancel', {activityName: testName}, safe(message, function (result) {
    message.respond({returnValue: !!result.returnValue || result.errorCode === 2});
  }));
});
register('media', function (message) {
  var p = message.payload;
  if (['loading', 'playing', 'progress', 'ended', 'stopped', 'error', 'timeout'].indexOf(p.event) < 0 ||
      ['adhan', 'spoken', 'announcement', 'radio'].indexOf(p.kind) < 0 ||
      typeof p.time !== 'number' || !isFinite(p.time) || p.time < 0 || p.time > 86400000) { throw new Error('Invalid media event'); }
  var state = read();
  state.media = {event: p.event, kind: p.kind, time: p.time, at: new Date().toISOString(),
    detail: typeof p.detail === 'string' ? p.detail.slice(0, 120) : null};
  save(state); message.respond({returnValue: true});
});
register('launchApp', function (message) {
  if (message.payload.id !== 'com.palm.app.settings' && !core.app(message.payload.id)) { throw new Error('Unsupported app'); }
  call('luna://com.webos.applicationManager/launch', {id: message.payload.id}, safe(message, function (result) { message.respond(result); }));
});
