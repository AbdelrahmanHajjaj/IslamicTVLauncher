(function (root, factory) {
  if (typeof module === 'object' && module.exports) { require('./compat'); module.exports = factory(require('./data.json'), require('./adhan'), require('./IslamicTVLauncher-core')); }
  else { root.Afnon = factory(root.AfnonData, root.adhan, root['tv.afnon:core']); }
}(this, function (data, adhan, portable) {
  'use strict';
  var rules = portable.tv.afnon.core.PrayerRules;
  var reminders = portable.tv.afnon.core.ReminderRules;
  var defaults = {lang: 'ar', city: 'cairo', method: null, hanafi: false, utcOffset: null,
    automatic: false, adhan: true, remind: 15, remindVoice: false, announce: true, callClose: 0,
    voice: 'alafasy', fajrVoice: 'chebae_fajr', volume: 60, bells: [true, false, true, true, true, true],
    offsets: [0, 0, 0, 0, 0, 0], hijriOffset: 0, digits: 'auto', clock: '24',
    dockPosition: 'top', dockAutoHide: 20, theme: 'mihrab', background: 'plain',
    homeLayout: 'mihrab', archSide: 'auto', homeBar: 'top', prayerMode: 'all', quickLabels: false,
    station: 'cairo', radioResume: true};
  var methods = {EGYPTIAN: 'Egyptian', UMM_AL_QURA: 'UmmAlQura', MUSLIM_WORLD_LEAGUE: 'MuslimWorldLeague',
    KARACHI: 'Karachi', DUBAI: 'Dubai', KUWAIT: 'Kuwait', QATAR: 'Qatar', NORTH_AMERICA: 'NorthAmerica',
    MOON_SIGHTING_COMMITTEE: 'MoonsightingCommittee', SINGAPORE: 'Singapore', TURKEY: 'Turkey'};
  function city(id) {
    for (var i = 0; i < data.cities.length; i++) { if (data.cities[i].id === id) { return data.cities[i]; } }
    return null;
  }
  function prefs(value) {
    value = value || {};
    var result = {};
    Object.keys(defaults).forEach(function (k) {
      var v = value[k] === undefined ? defaults[k] : value[k]; result[k] = Array.isArray(v) ? v.slice() : v;
    });
    if ((result.lang !== 'ar' && result.lang !== 'en') || !city(result.city) ||
        (result.method !== null && data.methods.indexOf(result.method) < 0) || typeof result.hanafi !== 'boolean' ||
        (result.utcOffset !== null && (typeof result.utcOffset !== 'number' || !isFinite(result.utcOffset) ||
          result.utcOffset % 15 !== 0 || result.utcOffset < -720 || result.utcOffset > 840))) {
      throw new Error('Invalid preferences');
    }
    function invalid(test) { if (test) { throw new Error('Invalid preferences'); } }
    function integer(n, min, max) { return typeof n === 'number' && isFinite(n) && n % 1 === 0 && n >= min && n <= max; }
    ['automatic', 'adhan', 'remindVoice', 'announce', 'radioResume', 'quickLabels'].forEach(function (k) { invalid(typeof result[k] !== 'boolean'); });
    invalid([0, 5, 10, 15, 20, 30].indexOf(result.remind) < 0 || [0, 1, 5, 10].indexOf(result.callClose) < 0);
    invalid(!integer(result.volume, 0, 100) || !integer(result.hijriOffset, -rules.MAX_HIJRI_OFFSET_DAYS, rules.MAX_HIJRI_OFFSET_DAYS));
    invalid(!Array.isArray(result.bells) || result.bells.length !== 6 || result.bells.some(function (v) { return typeof v !== 'boolean'; }) || result.bells[1]);
    invalid(!Array.isArray(result.offsets) || result.offsets.length !== 6);
    for (var i = 0; i < 6; i++) { invalid(!integer(result.offsets[i], -2147483648, 2147483647)); }
    try { rules.validateOffsets(new Int32Array(result.offsets)); }
    catch (error) { throw new Error('Invalid preferences'); }
    invalid(!voice(result.voice) || !voice(result.fajrVoice) || !station(result.station));
    invalid(['auto', 'western', 'eastern'].indexOf(result.digits) < 0 || ['12', '24'].indexOf(result.clock) < 0);
    invalid(['top', 'bottom'].indexOf(result.dockPosition) < 0 || [0, 10, 20, 30].indexOf(result.dockAutoHide) < 0);
    invalid(data.themes.indexOf(result.theme) < 0 || result.background !== 'plain' && data.patterns.indexOf(result.background) < 0);
    invalid(['mihrab', 'compact', 'hidden'].indexOf(result.homeLayout) < 0 || ['auto', 'left', 'right'].indexOf(result.archSide) < 0 ||
      ['top', 'bottom'].indexOf(result.homeBar) < 0 || ['all', 'current', 'next', 'both', 'none'].indexOf(result.prayerMode) < 0);
    return result;
  }
  function voice(id) { for (var i = 0; i < data.voices.length; i++) { if (data.voices[i].id === id) { return data.voices[i]; } } return null; }
  function station(id) { for (var i = 0; i < data.stations.length; i++) { if (data.stations[i].id === id) { return data.stations[i]; } } return null; }
  // shortcut: snapshot of the LF6300's visible apps; refresh after installing/removing apps until LG permits enumeration.
  function app(id) { for (var i = 0; i < data.apps.length; i++) { if (data.apps[i].id === id) { return data.apps[i]; } } return null; }
  function input(id) {
    var m = /^HDMI_([1-4])$/.exec(id);
    if (m) { return {id: id, label: 'HDMI ' + m[1], url: 'ext://hdmi:' + m[1], type: 'service/webos-external', appId: 'com.webos.app.hdmi' + m[1]}; }
    if (id === 'DTV' || id === 'ATV') { return {id: id, label: 'Live TV', url: 'tv://', type: 'service/webos-broadcast', appId: 'com.webos.app.livetv'}; }
    m = /^(AV|COMP)_([12])$/.exec(id);
    if (m && (m[1] !== 'COMP' || m[2] === '1')) {
      return {id: id, label: m[1] + ' ' + m[2], url: 'ext://' + (m[1] === 'AV' ? 'av:' : 'comp:') + m[2],
        type: 'service/webos-external', appId: 'com.webos.app.externalinput.' + (m[1] === 'AV' ? 'av' + m[2] : 'component')};
    }
    return null;
  }
  function wall(time, p) { return new Date(+time + (p.utcOffset === null ? -new Date(+time).getTimezoneOffset() : p.utcOffset) * 60000); }
  function pad(n) { return (n < 10 ? '0' : '') + n; }
  function clock(time, p) {
    var d = wall(time, p), h = d.getUTCHours();
    return (p.clock === '12' ? (h % 12 || 12) : pad(h)) + ':' + pad(d.getUTCMinutes()) +
      (p.clock === '12' ? ' ' + (p.lang === 'ar' ? (h < 12 ? 'ص' : 'م') : (h < 12 ? 'AM' : 'PM')) : '');
  }
  function day(now, p) {
    var c = city(p.city), name = p.method || c.method;
    var params = methods[name] ? adhan.CalculationMethod[methods[name]]() : adhan.CalculationMethod.Other();
    var extra = data.presets[name];
    if (extra) {
      params.fajrAngle = extra.fajr; params.ishaAngle = extra.isha; params.ishaInterval = extra.interval;
      if (extra.methodAdjustments) {
        var a = extra.methodAdjustments;
        params.methodAdjustments = {fajr: a[0], sunrise: a[1], dhuhr: a[2], asr: a[3], maghrib: a[4], isha: a[5]};
      }
    }
    params.madhab = p.hanafi ? adhan.Madhab.Hanafi : adhan.Madhab.Shafi;
    params.rounding = adhan.Rounding.None;
    params.adjustments = {fajr: p.offsets[0], sunrise: p.offsets[1], dhuhr: p.offsets[2], asr: p.offsets[3], maghrib: p.offsets[4], isha: p.offsets[5]};
    var date = wall(now, p), coords = new adhan.Coordinates(c.lat, c.lng);
    function calculate(delta) {
      return new adhan.PrayerTimes(coords, new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + delta), params);
    }
    var today = calculate(0), tomorrow = calculate(1);
    var times = ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'].map(function (name) { return +today[name]; });
    if (times.some(function (time) { return !isFinite(time); }) || !isFinite(+tomorrow.fajr)) { throw new Error('Prayer times unavailable for this location and date'); }
    return {times: times.map(function (time) { return rules.floorToMinute(time); }), tomorrowFajr: rules.floorToMinute(+tomorrow.fajr)};
  }
  function select(day, now) { return rules.select(new Float64Array(day.times), day.tomorrowFajr, +now); }
  function next(day, now) {
    var selected = select(day, now);
    return {index: selected.next, time: selected.nextTime};
  }
  function duration(ms) {
    var s = Math.max(0, Math.ceil(ms / 1000));
    return Math.floor(s / 3600) + ':' + pad(Math.floor(s / 60) % 60) + ':' + pad(s % 60);
  }
  function hijri(now, p) {
    var d = wall(now, p), n = Math.floor(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()) / 86400000) + p.hijriOffset;
    var months = data.hijri, low = 0, high = months.length - 1;
    if (n < months[0][0] || n >= months[high][0]) { return null; }
    while (low + 1 < high) { var mid = Math.floor((low + high) / 2); if (months[mid][0] <= n) { low = mid; } else { high = mid; } }
    return {day: n - months[low][0] + 1, month: months[low][2] - 1, year: months[low][1]};
  }
  function qibla(p) { var c = city(p.city); return adhan.Qibla(new adhan.Coordinates(c.lat, c.lng)); }
  function nextEvent(now, p, handled) {
    if (!p.automatic) { return null; }
    handled = handled || [];
    if (!Array.isArray(handled)) { throw new Error('Invalid handled events'); }
    for (var h = 0; h < handled.length; h++) { if (typeof handled[h] !== 'string') { throw new Error('Invalid handled events'); } }
    var days = [], mask = 0;
    for (var i = 0; i < 6; i++) { if (p.bells[i]) { mask |= 1 << i; } }
    for (var delta = 0; delta < 3; delta++) {
      days.push(new Float64Array(day(new Date(+now + delta * 86400000), p).times));
    }
    var event = reminders.nextScheduledEvent(days, +now, p.remind, mask, p.adhan || p.announce, handled);
    return event ? {id: event.id, kind: event.kind, prayer: event.prayer, time: event.time, prayerTime: event.prayerTime} : null;
  }
  function deliveryState(time, now) {
    if (typeof time !== 'number' || typeof now !== 'number') { throw new Error('Invalid delivery window'); }
    return reminders.deliveryState(time, now, 90000);
  }
  return {defaults: defaults, maxPrayerOffset: rules.MAX_OFFSET_MINUTES, maxHijriOffset: rules.MAX_HIJRI_OFFSET_DAYS,
    prefs: prefs, city: city, input: input, wall: wall, clock: clock, day: day, next: next, select: select,
    duration: duration, voice: voice, station: station, app: app, hijri: hijri, qibla: qibla, nextEvent: nextEvent, deliveryState: deliveryState};
}));
