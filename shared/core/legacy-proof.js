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
  var days = [];
  for (var d = 0; d < 3; d++) {
    var raw = new Float64Array(6);
    for (i = 0; i < 6; i++) { raw[i] = epoch + d * 86400000 + times[i]; }
    days.push(raw);
  }
  var event = reminders.nextScheduledEvent(days, epoch - 60000, 1, 63, true, []);
  check(event.id === 'before:0:' + (epoch + 1000) && event.time === epoch - 59000, 'persisted epoch ID');
  check(reminders.nextScheduledEvent(days, epoch - 60000, 1, 63, true, [event.id]).prayer === 2, 'handled event exclusion');
  check(reminders.nextScheduledEvent(days, epoch + 10000, 0, 63, true, []).time === epoch + 86401000, 'scheduled tomorrow Fajr');
  check(reminders.nextScheduledEvent(days, epoch, 0, 63, false, []) === null, 'disabled future events');
  check(reminders.deliveryState(1000, 999, 90000) === 'early' && reminders.deliveryState(1000, 1000, 90000) === 'due' &&
    reminders.deliveryState(1000, 91000, 90000) === 'due' && reminders.deliveryState(1000, 91001, 90000) === 'expired', 'inclusive delivery window');
  var edge = [];
  for (d = 0; d < 3; d++) { edge.push(new Float64Array([9007199254740991, 0, 0, 0, 0, 0])); }
  check(reminders.nextScheduledEvent(edge, 0, 0, 1, true, []).id === 'call:0:9007199254740991', 'safe integer epoch text');
  var home = core.tv.afnon.core.HomeNavigation;
  check(home.initial(3, 0) === home.FIRST_APP && home.initial(0, 2) === home.FIRST_RECENT, 'initial Home content');
  check(home.move(home.FIRST_APP, 3, 0, 3, home.FIRST_APP, 0, -1, true) === home.RADIO &&
    home.move(home.FIRST_APP + 1, 3, 0, 3, home.FIRST_APP, 0, -1, true) === home.NETWORK &&
    home.move(home.FIRST_APP + 2, 3, 0, 3, home.FIRST_APP, 0, -1, true) === home.SETTINGS, 'top Home controls');
  check(home.move(home.FIRST_APP, 3, 0, 3, home.FIRST_APP, 0, 1, true) === home.ALL_APPS, 'downward All apps');
  check(home.move(home.ALL_APPS, 3, 0, 3, home.FIRST_APP + 2, 0, -1, true) === home.FIRST_APP + 2, 'last app focus');
  check(home.move(home.ALL_APPS, 3, 0, 3, home.FIRST_APP, -1, 0, true) === home.WATCH_INPUT &&
    home.move(home.WATCH_INPUT, 3, 0, 3, home.FIRST_APP, 1, 0, true) === home.ALL_APPS, 'optional input action');
  check(home.initial(0, 0) === home.ALL_APPS && home.move(home.SETTINGS, 0, 2, 1, home.FIRST_APP, 0, 1, false) === home.FIRST_RECENT, 'empty Home rows');
  var ui = core.tv.afnon.core.UiContract, controls = ui.controls(), ids = [];
  for (i = 0; i < controls.length; i++) { ids.push(controls[i].id); }
  check(ui.bind('appearance.countdown', 'choice').save === 'appearance', 'shared UI appearance draft');
  check(ui.control('appearance.favourites').id !== ui.control('appearance.countdown').id, 'distinct Home concepts');
  ui.validateCoverage(ids.slice(0, 3), ids.slice(3)); check(true, 'UI binding and gap coverage');
  var rejected = false;
  try { ui.validateCoverage(ids.slice(1), []); } catch (missing) { rejected = true; }
  check(rejected, 'missing UI control rejected');
  rejected = false;
  try { ui.bind('appearance.countdown', 'toggle'); } catch (kind) { rejected = true; }
  check(rejected, 'wrong UI control kind rejected');
  check(ui.minimum('offsets.fajr') === -rules.MAX_OFFSET_MINUTES && ui.maximum('offsets.isha') === rules.MAX_OFFSET_MINUTES, 'UI prayer-offset bounds');
  check(ui.minimum('prayer.hijri') === -rules.MAX_HIJRI_OFFSET_DAYS && ui.maximum('prayer.hijri') === rules.MAX_HIJRI_OFFSET_DAYS, 'UI Hijri bounds');
  check(ui.minimum('reminders.volume') === 0 && ui.maximum('appearance.lighting') === 100, 'UI percent bounds');
  check(ui.choices('appearance.countdown')[2] === 'hidden' && ui.choices('appearance.favourites')[0] === 'two_rows', 'semantic UI choices');
  ui.validateChoiceCoverage('display.language', ['ar', 'en'], ['system']);
  check(ui.choices('prayer.zone').length === 106, 'UI choice coverage and time zones');
  rejected = false;
  try { ui.validateChoices('display.language', ['unknown']); } catch (choice) { rejected = true; }
  check(rejected, 'unknown UI choice rejected');
  var languageChoices = ui.choices('display.language'); languageChoices[0] = 'changed';
  check(ui.choices('display.language')[0] === 'system', 'UI choice snapshot isolation');
  check(rules.isPrayer(0) && rules.isPrayer(5) && !rules.isPrayer(1), 'five prayer indexes only');
  check(!rules.isPrayer(0.5) && !rules.isPrayer(4294967297) && !rules.isPrayer(NaN), 'untrusted JS prayer indexes');
  rejected = false;
  try { ui.prayerAdhanId(1); } catch (sunrise) { rejected = true; }
  check(ui.prayerAdhanId(2) === 'prayer.adhan-dhuhr' && rejected, 'per-prayer UI mapping excludes sunrise');
  check(ui.minimum('prayer.facing') === 0 && ui.maximum('prayer.facing') === 359, 'numeric bearing contract');
  check(rules.floorToMinute(30000) === 0 && rules.floorToMinute(59999) === 0, 'prayer minute rounds down');
  check(rules.floorToMinute(-1) === -60000, 'pre-epoch prayer minute floors');
  rejected = false;
  try { rules.floorToMinute(NaN); } catch (badTime) { rejected = true; }
  check(rejected, 'invalid rounding input rejected');
  var apps = core.tv.afnon.core.AllAppsNavigation;
  check(apps.move(4, 16, 0, 0, -1) === apps.BACK, 'all first-row columns reach Back');
  check(apps.move(apps.BACK, 16, 0, 1, 0) === apps.PRAYER && apps.move(apps.BACK, 16, 0, -1, 0) === apps.SETTINGS, 'circular Apps header');
  check(apps.move(14, 16, 0, 0, 1) === 15 && apps.move(15, 16, 0, 0, -1) === 10, 'partial Apps row');
  var comparison = core.tv.afnon.core.PrayerComparison;
  var referenceIds = [5, 4, 3, 1, 16, 9, 10, 2, 15, 11, 12, 13, 14, 17, 20, 18, 19, 21, 8];
  check(ui.choices('prayer.method').filter(function (name) { return name !== 'city-default'; }).every(function (name, i) { return comparison.aladhanMethod(name) === referenceIds[i]; }), 'all online authority IDs');
  var calculated = new Float64Array([30000, 60000, 120000, 180000, 240000, 300000]);
  var reference = new Float64Array([59999, 0, 180000, 179999, 300000, 300000]);
  check(Array.prototype.join.call(comparison.minutes(calculated, reference)) === '0,-1,1,-1,1,0', 'signed online differences round down');
  check(calculated[0] === 30000 && reference[0] === 59999, 'comparison preserves both schedules');
  rejected = false;
  try { comparison.minutes(calculated, new Float64Array([NaN, 0, 0, 0, 0, 0])); } catch (badReference) { rejected = true; }
  check(rejected, 'unavailable online times rejected');
  rejected = false;
  try { comparison.aladhanMethod('UNKNOWN'); } catch (badMethod) { rejected = true; }
  check(rejected, 'no fallback for unknown online authority');
  var uiScale = core.tv.afnon.core.UiScale;
  check(uiScale.DEFAULT_PERCENT === 100, 'original UI size is the default');
  check(uiScale.percent(80) === 80 && uiScale.percent(90) === 90 && uiScale.percent(100) === 100 && uiScale.percent(110) === 110, 'shared UI size choices');
  check(uiScale.percent(NaN) === 100 && uiScale.percent(Infinity) === 100 && uiScale.percent(99.9) === 100 && uiScale.percent(120) === 100, 'invalid size fallback');
  return {checks: checks};
}
