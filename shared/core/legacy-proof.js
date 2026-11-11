function verifyIslamicTVCore(core) {
  'use strict';
  var rules = core.tv.afnon.core.PrayerRules;
  var checks = 0;
  function check(condition, message) {
    if (!condition) { throw new Error(message); }
    checks++;
  }
  check(rules.normalizeOffset(4294967297) === 30 && rules.normalizeOffset(-4294967297) === -30, 'normalization overflow');
  check(rules.normalizeOffset(NaN) === 0 && rules.normalizeOffset(Infinity) === 0 && rules.normalizeOffset(1.5) === 0, 'malformed saved offsets');
  check(rules.MAX_HIJRI_OFFSET_DAYS === 2, 'Hijri adjustment limit');
  var times = new Float64Array([1000, 2000, 4000, 6000, 8000, 10000]);
  var moments = [999, 1000, 1999, 2000, 3999, 4000, 9999, 10000, 10001];
  var current = [-1, 0, 0, -1, -1, 2, 4, 5, 5];
  var next = [0, 2, 2, 2, 2, 3, 5, 0, 0];
  for (var i = 0; i < moments.length; i++) {
    var result = rules.select(times, 12000, moments[i]);
    check(result.current === current[i], 'current at ' + moments[i]);
    check(result.next === next[i], 'next at ' + moments[i]);
    check(result.nextTime === (moments[i] >= 10000 ? 12000 : times[next[i]]), 'time at ' + moments[i]);
  }
  check(times[0] === 1000 && times[5] === 10000, 'selection changed today');
  var epoch = 1794355200000;
  var shifted = new Float64Array(6);
  for (i = 0; i < times.length; i++) { shifted[i] = epoch + times[i]; }
  check(rules.select(shifted, epoch + 12000, epoch + 10000).nextTime === epoch + 12000, 'epoch precision');
  rules.validateOffsets(new Int32Array([-30, 30, 0, -1, 1, 0]));
  var rejected = 0;
  var invalid = [[], [0, 0, 0, 0, 0], [31, 0, 0, 0, 0, 0], [-2147483648, 0, 0, 0, 0, 0]];
  for (i = 0; i < invalid.length; i++) {
    try { rules.validateOffsets(new Int32Array(invalid[i])); }
    catch (error) { rejected++; }
  }
  check(rejected === invalid.length, 'invalid offsets accepted');
  rejected = 0;
  try { rules.select(times, NaN, 0); } catch (error1) { rejected++; }
  try { rules.select(times, 12000, Infinity); } catch (error2) { rejected++; }
  try { rules.select(new Float64Array(5), 12000, 0); } catch (error3) { rejected++; }
  shifted[3] = NaN;
  try { rules.select(shifted, epoch + 12000, epoch); } catch (error4) { rejected++; }
  check(rejected === 4, 'unavailable times accepted');
  var reminders = core.tv.afnon.core.ReminderRules;
  check(reminders.dueCall(times, 1000, true, 63, 120000) === 0 &&
    reminders.dueCall(times, 121000, true, 1, 120000) === -1, 'exclusive reminder window');
  check(reminders.dueAnnounce(times, 1000, true, true, 0, 120000) === 0 &&
    reminders.dueAnnounce(times, 1000, true, true, 1, 120000) === -1, 'announcement bell fallback');
  check(reminders.dueReminder(300000, 0, 5) && !reminders.dueReminder(300001, 0, 5), 'before-prayer boundary');
  check(reminders.minutesLeft(60001, 0) === 2 && reminders.minutesLeft(0, 0) === 0, 'ceiling minutes');
  return {checks: checks};
}
