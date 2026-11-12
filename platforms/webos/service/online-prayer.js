'use strict';
var https = require('https');
var core = require('./core');
var comparison = require('./IslamicTVLauncher-core').tv.afnon.core.PrayerComparison;
var names = ['Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Isha'];

function pad(n) { return (n < 10 ? '0' : '') + n; }
function isoTime(text) {
  var m = typeof text === 'string' && /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})([+-])(\d{2}):(\d{2})$/.exec(text);
  if (!m || +m[1] < 2000 || +m[2] < 1 || +m[2] > 12 || +m[3] < 1 || +m[3] > 31 ||
      +m[4] > 23 || +m[5] > 59 || +m[6] > 59 || +m[8] > 14 || +m[9] > 59 || +m[8] === 14 && +m[9] !== 0) {
    throw new Error('Invalid reference time');
  }
  var day = Date.UTC(+m[1], +m[2] - 1, +m[3]), d = new Date(day);
  if (d.getUTCMonth() !== +m[2] - 1 || d.getUTCDate() !== +m[3]) { throw new Error('Invalid reference date'); }
  return day + (+m[4] * 3600 + +m[5] * 60 + +m[6]) * 1000 - (m[7] === '+' ? 1 : -1) * (+m[8] * 60 + +m[9]) * 60000;
}

module.exports = function (prefs, now, callback) {
  var p = core.prefs(prefs), city = core.city(p.city), method = comparison.aladhanMethod(p.method || city.method);
  var wall = core.wall(now, p), date = pad(wall.getUTCDate()) + '-' + pad(wall.getUTCMonth() + 1) + '-' + wall.getUTCFullYear();
  var calculated = core.day(now, p).times, finished = false, req, timer;
  function finish(error, result) {
    if (finished) { return; } finished = true; clearTimeout(timer);
    if (error && req) { req.abort(); }
    callback(error, result);
  }
  timer = setTimeout(function () { finish(new Error('Online check timed out')); }, 12000);
  try {
    req = https.get({hostname: 'api.aladhan.com', port: 443, agent: false, rejectUnauthorized: true,
      path: '/v1/timings/' + date + '?latitude=' + city.lat + '&longitude=' + city.lng + '&method=' + method +
        '&school=' + (p.hanafi ? 1 : 0) + '&latitudeAdjustmentMethod=1&shafaq=general&iso8601=true',
      headers: {Accept: 'application/json', 'Accept-Encoding': 'identity'}}, function (res) {
      res.on('error', function (error) { finish(error); });
      res.on('close', function () { if (!finished) { finish(new Error('Incomplete reference response')); } });
      if (finished) { res.resume(); return; }
      if (res.statusCode !== 200 || !/^application\/json(?:;|$)/i.test(res.headers['content-type'] || '')) {
        finish(new Error('Online reference unavailable')); return;
      }
      var chunks = [], bytes = 0;
      res.on('data', function (chunk) {
        if (finished) { return; } bytes += chunk.length;
        if (bytes > 32768) { finish(new Error('Reference response too large')); return; }
        chunks.push(chunk);
      });
      res.on('end', function () {
        if (finished) { return; }
        try {
          var body = JSON.parse(Buffer.concat(chunks).toString('utf8')), data = body.data, meta = data && data.meta;
          if (body.code !== 200 || !meta || !data.date || !data.date.gregorian || data.date.gregorian.date !== date ||
              typeof meta.latitude !== 'number' || typeof meta.longitude !== 'number' ||
              Math.abs(meta.latitude - city.lat) > 0.00001 || Math.abs(meta.longitude - city.lng) > 0.00001 ||
              !isFinite(meta.latitude) || !isFinite(meta.longitude) || !meta.method || meta.method.id !== method ||
              method === 15 && (!meta.method.params || meta.method.params.shafaq !== 'general') ||
              meta.school !== (p.hanafi ? 'HANAFI' : 'STANDARD') ||
              meta.latitudeAdjustmentMethod !== (method === 15 ? 'NONE' : 'MIDDLE_OF_THE_NIGHT') ||
              typeof meta.timezone !== 'string' || !/^[A-Za-z0-9_+\-/]{1,80}$/.test(meta.timezone) || !data.timings) {
            throw new Error('Mismatched reference response');
          }
          var times = names.map(function (name) { return isoTime(data.timings[name]); });
          // ISO offsets identify instants; a genuine late Isha may cross civil midnight.
          var isoDate = wall.getUTCFullYear() + '-' + pad(wall.getUTCMonth() + 1) + '-' + pad(wall.getUTCDate());
          var anchor = isoTime(isoDate + 'T12:00:00+00:00');
          for (var i = 0; i < 6; i++) {
            if (Math.abs(times[i] - anchor) > 36 * 3600000 || i && times[i] <= times[i - 1] ||
                i > 0 && i < 5 && data.timings[names[i]].slice(0, 10) !== isoDate) { throw new Error('Invalid reference day'); }
          }
          var differences = comparison.minutes(new Float64Array(calculated), new Float64Array(times));
          finish(null, {date: date, timezone: meta.timezone, calculated: calculated, reference: times,
            differences: Array.prototype.slice.call(differences)});
        } catch (error) { finish(error); }
      });
    });
    req.on('error', function (error) { finish(error); });
  } catch (error) { finish(error); }
  return function () { finish(new Error('Online check cancelled')); };
};
