(function () {
  'use strict';
  var A = window.Afnon, D = window.AfnonData;
  var homeNavigation = window['tv.afnon:core'].tv.afnon.core.HomeNavigation;
  var uiContract = window['tv.afnon:core'].tv.afnon.core.UiContract;
  var tv = typeof window.PalmServiceBridge !== 'undefined';
  var prefs = A.prefs(), startup = false, loaded = false, busy = false;
  var input = null, inputs = [], view = 'input', picker = null, page = 0;
  var screen = document.getElementById('screen'), video = document.getElementById('input-video');
  var bridges = [], instance = String(Date.now()), cachedDay = null, cacheKey = '', noticeTimer;
  var lastError = null;
  var section = 'prayer', testPrayer = 0, dockTimer, overlayTimer, audioTimer, mediaTimer, media = null;
  var pendingLaunch = null, seenLaunch = null, radioBeforeCall = false, audioSerial = 0, editValue = 0, country = null, dayViewMinute = -1, previewTestTimer;
  var returnContext = null, voicePreview = null, coldAlert = false;
  var appearanceDraft = null, settingsParent = 'home', dayParent = 'home';
  var homeNodes = [], homeAppCount = 0, lastHomeApp = homeNavigation.FIRST_APP;
  var pickerControl = null;
  var appearanceKeys = ['theme', 'background', 'homeLayout', 'archSide', 'homeBar', 'prayerMode'];
  var overlay = document.getElementById('overlay');
  var sections = ['prayer', 'reminders', 'radio', 'display', 'appearance', 'device', 'tests'];
  function sectionName(id) { return {
    prayer: t('الصلاة والقبلة', 'Prayer & Qibla'), reminders: t('الأذان والتنبيهات', 'Adhan & reminders'),
    radio: t('إذاعة القرآن', 'Quran radio'), display: t('العرض والتحكم', 'Display & controls'),
    appearance: t('الرئيسية والمظهر', 'Home & appearance'), device: t('التلفاز والتشغيل', 'TV & startup'),
    tests: t('اختبار التنبيهات', 'Test alerts')
  }[id]; }
  function t(ar, en) { return prefs.lang === 'ar' ? ar : en; }
  function s(key) { return D.strings[key][prefs.lang]; }
  function f(key) { return D.formatText[prefs.lang][key]; }
  function escape(value) { return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/"/g, '&quot;'); }
  function digits(value) { return prefs.digits === 'eastern' || prefs.digits === 'auto' && prefs.lang === 'ar' ? String(value).replace(/[0-9]/g, function (n) { return '٠١٢٣٤٥٦٧٨٩'.charAt(Number(n)); }) : String(value); }
  function button(action, label, value, className, accessibleLabel, control, choiceValues) {
    return '<button data-action="' + action + '" data-value="' + escape(value === undefined ? '' : value) + '" class="' + (className || '') + '"' +
      (control ? ' data-ui="' + control.id + '" data-ui-kind="' + control.kind + '"' : '') +
      (choiceValues ? ' data-ui-values="' + escape(JSON.stringify(choiceValues)) + '"' : '') +
      (accessibleLabel ? ' aria-label="' + escape(accessibleLabel) + '" title="' + escape(accessibleLabel) + '"' : '') + '>' + label + '</button>';
  }
  function icon(name) {
    var shape = name === 'home' ? '<path d="M3 11l9-8 9 8M5 9v12h5v-7h4v7h5V9"/>' : name === 'minus' ? '<path d="M5 12h14"/>' : D.icons[name] || '<circle cx="12" cy="12" r="9"/><path d="M12 6v6l4 2"/>';
    return '<svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + shape + '</svg>';
  }
  function dockActions() {
    return '<div class="dock-actions">' + button('home', icon('home'), '', 'icon-button', t('الرئيسية', 'Home')) +
      button('inputs', icon('input'), '', 'icon-button', t('المداخل', 'Inputs')) +
      button('settings', icon('gear'), '', 'icon-button', t('الإعدادات', 'Settings')) +
      button('input', icon('minus'), '', 'icon-button', t('إخفاء الشريط', 'Hide prayer bar')) + '</div>';
  }
  function homeControls() {
    return '<nav class="home-quick courtyard" aria-label="' + t('الوصول السريع', 'Quick access') + '">' + [
      ['prayer-day', 'compass', t('اليوم والقبلة', 'Today & Qibla')],
      ['inputs', 'input', t('المداخل', 'Inputs')],
      ['radio', 'radio', t('إذاعة القرآن', 'Quran radio')],
      ['tv-settings', 'wifi', t('إعدادات تلفاز LG والشبكة', 'LG TV & network settings')],
      ['settings', 'gear', t('الإعدادات', 'Settings')]
    ].map(function (item) { return button(item[0], icon(item[1]) + '<span class="quick-caption">' + (prefs.quickLabels && item[0] === 'tv-settings' ? f('networkLabel') : item[2]) + '</span>', '', 'quick-button', item[2]); }).join('') + '</nav>';
  }
  function appTile(item) {
    return button('native-app', icon(item.icon) + '<span>' + escape(item[prefs.lang]) + '</span>', item.id, 'app-tile');
  }
  function preview(method, data) {
    var state;
    try { state = JSON.parse(localStorage.getItem('afnon-webos-preview') || '{}'); } catch (error) { state = {}; }
    if (method === 'savePrefs') { state.prefs = A.prefs(data.prefs); }
    if (method === 'setStartup') { state.startup = {enabled: data.enabled}; }
    if (method === 'selectInput') { state.input = data.id; }
    if (method === 'testAlert') {
      clearTimeout(previewTestTimer);
      previewTestTimer = setTimeout(function () { startAlert({mode: data.mode, prayer: data.prayer, test: true}); }, 30000);
      return {returnValue: true, due: Date.now() + 30000};
    }
    if (method === 'cancelTest') { clearTimeout(previewTestTimer); }
    if (method !== 'report') { localStorage.setItem('afnon-webos-preview', JSON.stringify(state)); }
    return {returnValue: true, state: state, prefs: state.prefs, enabled: state.startup && state.startup.enabled,
      current: A.input(state.input || 'HDMI_1'), inputs: [A.input('HDMI_1'), A.input('HDMI_2'), A.input('HDMI_3'), A.input('DTV')]};
  }
  function request(method, data, callback) {
    callback = callback || function () {};
    if (!tv) {
      try { callback(preview(method, data)); } catch (error) { callback({returnValue: false, errorText: error.message}); }
      return;
    }
    var bridge = new PalmServiceBridge(), done = false;
    bridges.push(bridge);
    var timeout = setTimeout(function () { finish({returnValue: false, errorText: 'Service response timed out'}); }, 16000);
    function finish(reply) {
      if (done) { return; } done = true; clearTimeout(timeout);
      var index = bridges.indexOf(bridge); if (index >= 0) { bridges.splice(index, 1); }
      callback(reply);
    }
    bridge.onservicecallback = function (raw) {
      var reply;
      try { reply = JSON.parse(raw); } catch (error) { reply = {returnValue: false, errorText: 'Invalid service response'}; }
      finish(reply || {returnValue: false});
    };
    try { bridge.call('luna://tv.afnon.webos.service/' + method, JSON.stringify(data)); }
    catch (error) { finish({returnValue: false, errorText: error.message}); }
  }
  function report(name) { request('report', {view: name || view, instance: instance, input: input ? input.id : null, error: lastError}); }
  function notify(text, error) {
    var el = document.getElementById('notice'); el.textContent = text; el.style.display = 'block';
    clearTimeout(noticeTimer); noticeTimer = setTimeout(function () { el.style.display = 'none'; }, 9000);
    if (error) { lastError = error; report('error'); }
  }
  function schedule(now) {
    var date = A.wall(now, prefs), key = date.getUTCFullYear() + '-' + date.getUTCMonth() + '-' + date.getUTCDate() + JSON.stringify(prefs);
    if (key !== cacheKey) { cachedDay = A.day(now, prefs); cacheKey = key; }
    return cachedDay;
  }
  function prayers(now) {
    var day = schedule(now), selected = A.select(day, now), next = {index: selected.next, time: selected.nextTime}, labels = s('prayerNames');
    var times = day.times.slice();
    if (view === 'home' && (prefs.prayerMode === 'next' || prefs.prayerMode === 'both')) { times[next.index] = next.time; }
    var current = selected.current;
    if (view === 'home' && prefs.prayerMode === 'current' && current < 0) { return '<p class="muted">' + t('لا توجد صلاة حالية', 'No current prayer') + '</p>'; }
    var indices = [0, 1, 2, 3, 4, 5];
    if (view === 'home' && prefs.prayerMode === 'current') { indices = [current]; }
    if (view === 'home' && prefs.prayerMode === 'next') { indices = [next.index]; }
    if (view === 'home' && prefs.prayerMode === 'both') { indices = current < 0 ? [next.index] : [current, next.index]; }
    return '<div class="prayers">' + indices.map(function (i) {
      var time = times[i];
      var upcoming = i === next.index && time === next.time;
      return '<div class="prayer' + (upcoming ? ' next' : i === current ? ' current' : time < +now ? ' past' : '') + '">' +
        '<span class="prayer-tag">' + (upcoming ? t('التالية', 'Next') : i === current ? t('الآن', 'Now') : '&nbsp;') + '</span>' + escape(labels[i]) +
        '<span class="time">' + digits(A.clock(time, prefs)) + '</span></div>';
    }).join('') + '</div>';
  }
  function inputName() { return input ? (input.id === 'DTV' || input.id === 'ATV' ? t('البث التلفزيوني', 'Live TV') : input.label) : t('اختر المدخل', 'Choose an input'); }
  function actions(home) {
    return '<div class="actions">' + button('input', t('متابعة المشاهدة', 'Watch input')) +
      (home ? '' : button('home', t('الرئيسية', 'Home'))) +
      button('inputs', t('المداخل', 'Inputs')) + button('settings', t('الإعدادات', 'Settings')) +
      (home ? button('exit', t('الخروج من المشغل', 'Exit launcher')) : '') + '</div>';
  }
  function header(title, back) {
    return '<div class="header">' + (back ? button(back, icon('back'), '', 'round-back', t('رجوع', 'Back')) : '') + '<span class="brand">' + t('واجهة التلفاز الإسلامي', 'Islamic TV Launcher') + '</span><span class="subtitle">' + title +
      '</span><span class="clock" data-clock></span></div>';
  }
  function zoneName() {
    var offset = prefs.utcOffset === null ? -new Date().getTimezoneOffset() : prefs.utcOffset;
    return (prefs.utcOffset === null ? t('توقيت التلفاز', 'TV time zone') + ' · ' : '') + 'UTC' +
      (offset < 0 ? '−' : '+') + Math.floor(Math.abs(offset) / 60) + ':' + (Math.abs(offset) % 60 < 10 ? '0' : '') + Math.abs(offset) % 60;
  }
  function controlLabel(control) {
    var parts = control.labelKey.split(':'), label = s(parts[0]);
    return parts.length === 1 ? label : label[Number(parts[1])];
  }
  function row(id, action, value, kind) {
    var control = uiContract.bind(id, kind || 'action');
    var values = control.kind === 'choice' ? choiceValues(id, choices(action, true)) : null;
    return button(action, escape(controlLabel(control)) + '<span class="value">' + escape(value) + '</span>', '', '', '', control, values);
  }
  function choiceValues(id, items) {
    var values = items.map(function (item) {
      if (id === 'prayer.method' && item.value === '') { return 'city-default'; }
      if (id === 'prayer.zone' && item.value === '') { return 'system'; }
      if (id === 'prayer.asr') { return item.value ? 'hanafi' : 'shafi'; }
      if (id === 'appearance.background' && item.value === 'plain') { return 'solid'; }
      return String(item.value);
    });
    uiContract.validateChoices(id, values);
    return values;
  }
  function carousel(id, key) {
    var control = uiContract.bind(id, 'choice'), label = controlLabel(control);
    var list = choices(key), index = 0, rtl = prefs.lang === 'ar';
    list.forEach(function (item, i) { if (item.value === appearanceDraft[key]) { index = i; } });
    function arrow(delta, disabled) {
      return '<button tabindex="-1" data-action="carousel-step" data-value="' + key + ':' + delta + '" class="carousel-arrow ' + (delta < 0 ? 'previous' : 'next') + '"' +
        (disabled ? ' disabled' : '') + ' aria-label="' + escape(label + ': ' + (delta < 0 ? t('السابق', 'Previous') : t('التالي', 'Next'))) + '">' + ((delta < 0) !== rtl ? '‹' : '›') + '</button>';
    }
    return '<div class="carousel-row">' + button('carousel', escape(label) + '<span class="carousel-choice">' + escape(list[index].label) + '</span>', key, 'carousel-main', '', control, choiceValues(id, list)) +
      arrow(-1, index === 0) + arrow(1, index === list.length - 1) + '</div>';
  }
  function changeAppearance(key, delta) {
    var list = choices(key), index = 0, scroll = screen.querySelector('.settings-rows').scrollTop;
    list.forEach(function (item, i) { if (item.value === appearanceDraft[key]) { index = i; } });
    appearanceDraft[key] = list[Math.max(0, Math.min(list.length - 1, index + delta))].value;
    render('[data-action=carousel][data-value=' + key + ']');
    screen.querySelector('.settings-rows').scrollTop = scroll;
  }
  function methodName(id) { return D.methodNames[id][prefs.lang]; }
  function onOff(value) { return value ? t('مفعّل', 'On') : t('متوقف', 'Off'); }
  function settingRows() {
    var city = A.city(prefs.city), result = '';
    if (section === 'prayer') {
      result = row('prayer.location', 'city', city[prefs.lang], 'choice') +
        row('prayer.method', 'method', methodName(prefs.method || city.method), 'choice') +
        row('prayer.asr', 'hanafi', prefs.hanafi ? t('حنفي', 'Hanafi') : t('شافعي / قياسي', 'Shafi / Standard'), 'choice') +
        row('prayer.zone', 'zone', zoneName(), 'choice') +
        row('prayer.offsets', 'offsets', t('بالدقائق', 'Minutes')) +
        row('prayer.hijri', 'hijriOffset', digits(prefs.hijriOffset), 'number') +
        row('prayer.today', 'prayer-day', '');
    } else if (section === 'reminders') {
      result = row('reminders.enabled', 'automatic', onOff(prefs.automatic), 'toggle') +
        row('reminders.before', 'remind', prefs.remind ? digits(prefs.remind) + f('minuteUnit') : onOff(false), 'choice') +
        row('reminders.spoken', 'remindVoice', onOff(prefs.remindVoice), 'toggle') +
        row('reminders.adhan', 'adhan', onOff(prefs.adhan), 'toggle') +
        row('reminders.announce', 'announce', onOff(prefs.announce), 'toggle') +
        row('reminders.voice', 'voice', A.voice(prefs.voice)[prefs.lang], 'choice') +
        row('reminders.fajr-voice', 'fajrVoice', A.voice(prefs.fajrVoice)[prefs.lang], 'choice') +
        row('reminders.volume', 'volume', digits(prefs.volume) + '%', 'number') +
        row('reminders.close', 'callClose', prefs.callClose ? digits(prefs.callClose) + f('minuteUnit') : t('عند نهاية الصوت', 'When audio ends'), 'choice') +
        row('reminders.per-prayer', 'prayer-day', '') + row('tests.open', 'tests', '');
    } else if (section === 'radio') {
      result = row('radio.station', 'station', A.station(prefs.station)[prefs.lang], 'choice') +
        row('radio.play', 'radio-play', '') + row('radio.stop', 'radio-stop', '') +
        row('radio.resume', 'radioResume', onOff(prefs.radioResume), 'toggle');
    } else if (section === 'display') {
      result = row('display.language', 'language', t('العربية', 'English'), 'choice') +
        row('display.digits', 'digits', prefs.digits === 'auto' ? t('حسب اللغة', 'Follow language') : prefs.digits === 'eastern' ? '٠١٢٣' : '0123', 'choice') +
        row('display.clock', 'clock', digits(prefs.clock), 'choice') +
        row('display.quick-labels', 'quickLabels', onOff(prefs.quickLabels), 'toggle') +
        row('display.bar-position', 'dockPosition', prefs.dockPosition === 'top' ? t('أعلى', 'Top') : t('أسفل', 'Bottom'), 'choice') +
        row('display.bar-timeout', 'dockAutoHide', prefs.dockAutoHide ? digits(prefs.dockAutoHide) + t(' ثانية', ' sec') : t('يدويًا', 'Manually'), 'choice') +
        row('display.open-bar', 'bar', '') + row('display.open-clock', 'clock-view', '');
    } else if (section === 'appearance') {
      var draft = appearanceDraft;
      result = carousel('appearance.theme', 'theme') +
        carousel('appearance.background', 'background') +
        carousel('appearance.countdown', 'homeLayout') +
        (draft.homeLayout === 'compact' ? carousel('appearance.position', 'homeBar') :
          draft.homeLayout === 'mihrab' ? carousel('appearance.position', 'archSide') : '') +
        carousel('appearance.prayers', 'prayerMode') +
        row('appearance.reset', 'reset-layout', '') +
        row('appearance.apply', 'apply-appearance', '') + row('appearance.cancel', 'cancel-appearance', '') +
        row('appearance.home', 'home', '');
    } else if (section === 'device') {
      result = row('device.startup', 'startup', onOff(startup), 'toggle') +
        row('device.inputs', 'inputs', inputName()) + row('device.apps', 'apps', '') +
        row('device.settings', 'tv-settings', '') +
        row('device.health', 'health', '') + row('device.exit-input', 'exit', '');
    } else if (section === 'tests') {
      result = row('tests.prayer', 'testPrayer', s('prayerNames')[testPrayer], 'choice') +
        row('tests.banner', 'test-banner', t('بلا صوت', 'Silent')) +
        row('tests.card', 'test-card', t('بلا صوت', 'Silent')) +
        row('tests.adhan-short', 'test-adhan', '') +
        row('tests.adhan-full', 'test-full', '') +
        row('tests.spoken', 'test-spoken', '') +
        row('tests.announcement', 'test-announcement', '') +
        row('tests.background', 'test-background', '') +
        row('tests.cancel', 'cancel-test', '');
    } else if (section === 'offsets') {
      for (var i = 0; i < 6; i++) { result += row('offsets.' + ['fajr', 'sunrise', 'dhuhr', 'asr', 'maghrib', 'isha'][i], 'offset-' + i, digits(prefs.offsets[i]) + f('minuteUnit'), 'number'); }
      result += row('offsets.reset', 'reset-offsets', '');
    }
    return result;
  }
  function settingsMarkup() {
    if (section === 'appearance' && !appearanceDraft) { appearanceDraft = A.prefs(prefs); }
    return '<section class="page settings-page' + (section === 'appearance' ? ' appearance-settings' : '') + '">' + header(t('الإعدادات', 'Settings'), 'settings-back') +
      '<nav class="settings-nav" aria-label="' + t('الأقسام', 'Sections') + '">' + sections.map(function (id) {
        return button('section', sectionName(id), id, id === section || section === 'offsets' && id === 'prayer' ? 'selected' : '');
      }).join('') + '</nav>' + (section === 'appearance' ? '<div class="appearance-preview">' + appearancePreview(appearanceDraft) + '</div>' : '') + '<div class="settings-rows"><h2>' + (section === 'offsets' ? t('تعديل المواقيت', 'Time adjustments') : sectionName(section)) +
      '</h2>' + settingRows() + '</div></section>';
  }
  function appearancePreview(p) {
    var c = D.palettes[p.theme], rtl = prefs.lang === 'ar', name = s('themeNames')[D.themes.indexOf(p.theme)];
    var mihrab = p.homeLayout === 'mihrab', compact = p.homeLayout === 'compact';
    var onRight = p.archSide === 'right' || p.archSide === 'auto' && rtl;
    var x = mihrab && !onRight ? 744 : 96, width = mihrab ? 1080 : 1728, archX = onRight ? 1224 : 96;
    var cells = p.prayerMode === 'none' ? 0 : p.prayerMode === 'all' ? 6 : p.prayerMode === 'both' ? 2 : 1;
    var top = compact && p.homeBar === 'top' ? 370 : 290, tiles = compact && p.homeBar === 'top' ? 540 : 452;
    if (p.quickLabels) { top = compact && p.homeBar === 'top' ? 466 : 362; tiles = compact ? p.homeBar === 'top' ? 616 : 504 : 520; }
    function rect(rx, ry, rw, rh, fill, stroke) {
      return '<rect x="' + rx + '" y="' + ry + '" width="' + rw + '" height="' + rh + '" rx="14" fill="' + fill + '"' + (stroke ? ' stroke="' + stroke + '" stroke-width="3"' : '') + '/>';
    }
    var svg = rect(0, 0, 1920, 1080, c['lapis-900']);
    if (p.background !== 'plain') {
      svg += '<defs><pattern id="preview-pattern" width="960" height="540" patternUnits="userSpaceOnUse"><image xlink:href="patterns/' + p.background + '.svg" width="960" height="540"/></pattern></defs><rect width="1920" height="1080" fill="url(#preview-pattern)"/>';
    }
    if (mihrab) {
      svg += '<path transform="translate(' + archX + ' 124)" d="M0 900 L0 330 C0 170 140 70 300 0 C460 70 600 170 600 330 L600 900 Z" fill="' + c['lapis-800'] + '" stroke="' + c['gold-400'] + '" stroke-width="3"/>' +
        '<text x="' + (archX + 300) + '" y="570" text-anchor="middle" font-size="76" fill="' + c['gold-400'] + '">' + escape(s('nextPrayer')) + '</text>';
    } else if (compact) { svg += rect(96, p.homeBar === 'bottom' ? 916 : p.quickLabels ? 342 : 242, 1728, 100, c['lapis-800'], c['gold-400']); }
    svg += '<text x="' + (rtl ? x + width : x) + '" y="150" text-anchor="' + (rtl ? 'end' : 'start') + '" direction="ltr" font-size="88" fill="' + c['plaster-100'] + '">' + digits(A.clock(new Date(), prefs)) + '</text>';
    for (var i = 0; i < 5; i++) { svg += rect(p.quickLabels ? x + i * width / 5 : rtl ? x + i * 80 : x + width - 64 - i * 80, p.quickLabels ? compact ? 250 : 270 : 56, p.quickLabels ? width / 5 - 12 : 64, 64, c['lapis-700']); }
    for (i = 0; i < cells; i++) { svg += rect(x + i * (width / cells), top, width / cells - 12, 110, c['lapis-800'], i === cells - 1 ? c['gold-400'] : c['lapis-700']); }
    for (i = 0; i < 3; i++) { svg += rect(x + i * (width / 3), tiles + 48, width / 3 - 24, 200, c['lapis-800'], c['lapis-700']); }
    svg += rect(x, tiles + 276, 300, 76, c['lapis-800']) + rect(x + 320, tiles + 276, 260, 76, c['lapis-800']);
    return '<div class="appearance-model"><svg width="1920" height="1080" viewBox="0 0 1920 1080" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" role="img" aria-label="' + escape(t('معاينة تخطيط ', 'Layout preview: ') + name) + '">' + svg + '</svg></div><div class="appearance-caption"><h2>' + escape(name) + '</h2><div class="swatches">' +
      ['lapis-900', 'lapis-800', 'gold-400', 'plaster-100'].map(function (key) { return '<span style="background:' + c[key] + '"></span>'; }).join('') +
      '</div></div>';
  }
  function bindHomeFocus() {
    homeNodes = [];
    homeNodes[homeNavigation.TODAY] = screen.querySelector('.home-quick [data-action=prayer-day]');
    homeNodes[homeNavigation.INPUTS] = screen.querySelector('.home-quick [data-action=inputs]');
    homeNodes[homeNavigation.RADIO] = screen.querySelector('.home-quick [data-action=radio]');
    homeNodes[homeNavigation.NETWORK] = screen.querySelector('.home-quick [data-action=tv-settings]');
    homeNodes[homeNavigation.SETTINGS] = screen.querySelector('.home-quick [data-action=settings]');
    homeNodes[homeNavigation.ALL_APPS] = screen.querySelector('.home-links [data-action=apps]');
    homeNodes[homeNavigation.WATCH_INPUT] = screen.querySelector('.home-links [data-action=input]');
    var apps = screen.querySelectorAll('.home-apps button');
    homeAppCount = apps.length;
    for (var i = 0; i < apps.length; i++) { homeNodes[homeNavigation.FIRST_APP + i] = apps[i]; }
    homeNodes.forEach(function (node, id) { if (node) { node.setAttribute('data-home-focus', String(id)); } });
  }
  function render(focusAction) {
    document.documentElement.lang = prefs.lang; document.documentElement.dir = prefs.lang === 'ar' ? 'rtl' : 'ltr';
    var stage = document.getElementById('stage'), sheet = prefs.theme === 'mihrab' ? 'style.css' : 'theme-' + prefs.theme + '.css';
    if (document.getElementById('theme-style').getAttribute('href') !== sheet) { document.getElementById('theme-style').setAttribute('href', sheet); }
    stage.setAttribute('data-layout', prefs.homeLayout); stage.setAttribute('data-side', prefs.archSide);
    stage.setAttribute('data-pattern', prefs.background); stage.setAttribute('data-prayers', prefs.prayerMode);
    stage.setAttribute('data-home-bar', prefs.homeBar); stage.setAttribute('data-quick-labels', String(prefs.quickLabels));
    var html = '', now = new Date(), city = A.city(prefs.city);
    try {
      if (view === 'bar') {
        html = '<section class="bar ' + prefs.dockPosition + '" aria-label="' + escape(inputName() + ' · ' + city[prefs.lang]) + '"><div class="dock-prayers" data-prayers>' + prayers(now) + '</div>' + dockActions() + '</section>';
      } else if (view === 'home') {
        html = '<section class="page home"><div class="arch"><svg class="arch-shape" viewBox="0 0 600 900" aria-hidden="true">' +
          '<path d="M0 900 L0 330 C0 170 140 70 300 0 C460 70 600 170 600 330 L600 900 Z"/>' +
          '<path class="inner" d="M18 900 L18 334 C18 184 150 92 300 22 C450 92 582 184 582 334 L582 900"/></svg>' +
          '<div class="verse" lang="ar" dir="rtl">' + D.sacred.prayerVerse.map(escape).join('<br>') + '</div>' +
          '<div class="verse-ref">' + digits(s('ref')) + '</div><div class="arch-info"><p class="eyebrow">' + s('nextPrayer') +
          '</p><div class="prayer-name" dir="' + (prefs.lang === 'ar' ? 'rtl' : 'ltr') + '" data-next-label></div>' +
          '<p class="prayer-time" data-next-time></p><div class="countdown" data-countdown></div><p class="city-name">' + escape(city[prefs.lang]) + '</p></div></div>' +
          '<div class="home-clock courtyard"><div class="clock" data-clock></div><div class="muted" data-hijri></div><div class="muted" data-gregorian></div></div>' + homeControls() +
          '<div class="home-prayers courtyard" data-prayers>' + prayers(now) + '</div><div class="home-actions courtyard"><p class="eyebrow">' +
          t('تطبيقات التلفاز', 'TV apps') + '</p><div class="home-apps">' + D.apps.slice(0, 3).map(appTile).join('') + '</div><div class="home-links">' +
          button('input', icon('input') + '<span>' + t('متابعة ', 'Watch ') + escape(inputName()) + '</span>') +
          button('apps', icon('folder') + '<span>' + t('كل التطبيقات', 'All apps') + ' · ' + digits(D.apps.length) + '</span>') +
          '</div></div></section>';
      } else if (view === 'settings') {
        html = settingsMarkup();
      } else if (view === 'prayer-day') {
        dayViewMinute = Math.floor(+now / 60000);
        var today = schedule(now), h = A.hijri(now, prefs), bearing = A.qibla(prefs);
        html = '<section class="page">' + header(t('اليوم والقبلة', 'Today & Qibla')) + '<div class="day-times">' + today.times.map(function (time, i) {
          return '<div class="day-row"><span>' + s('prayerNames')[i] + '</span><strong>' + digits(A.clock(time, prefs)) + '</strong>' +
            (i === 1 ? '<span class="muted">' + t('لا أذان للشروق', 'No adhan') + '</span>' : button('bell', onOff(prefs.bells[i]), i, '', t('أذان ', 'Adhan for ') + s('prayerNames')[i])) + '</div>';
        }).join('') + '</div><div class="day-details"><h2>' + escape(city[prefs.lang]) + '</h2><p>' +
          (h ? digits(h.day + ' ' + s('hijriMonths')[h.month] + ' ' + h.year) : t('التاريخ الهجري غير متاح', 'Hijri date unavailable')) +
          '</p><div class="qibla-arrow" style="-webkit-transform:rotate(' + bearing + 'deg);transform:rotate(' + bearing + 'deg)">↑</div><h2>' +
          t('القبلة ', 'Qibla ') + digits(Math.round(bearing)) + '°</h2><p class="footnote">' +
          t('الزاوية من الشمال الحقيقي، وليست اتجاه شاشة التلفاز.', 'Bearing clockwise from true north, not from the TV screen.') + '</p>' +
          button('tests', t('اختبار التنبيهات', 'Test alerts')) + button('home', t('الرئيسية', 'Home')) + '</div></section>';
      } else if (view === 'radio') {
        var station = A.station(prefs.station);
        html = '<section class="page">' + header(t('إذاعة القرآن', 'Quran radio')) + '<div class="media-page"><h1>' + escape(station[prefs.lang]) +
          '</h1><p>' + escape(station[prefs.lang === 'ar' ? 'whereAr' : 'whereEn'] || '') + '</p><p data-radio-status>' +
          (media && media.kind === 'radio' ? t('جارٍ التشغيل', 'Playing') : t('متوقفة', 'Stopped')) + '</p><div class="actions">' +
          button('radio-play', t('تشغيل', 'Play')) + button('radio-stop', t('إيقاف', 'Stop')) + button('radio-settings', t('اختيار المحطة', 'Choose station')) +
          button('input', t('العودة للمشاهدة', 'Back to input')) + '</div></div></section>';
      } else if (view === 'apps') {
        html = '<section class="page apps-page"><div class="apps-heading">' + button('apps-back', icon('back'), '', 'round-back', t('رجوع', 'Back')) +
          '<h1>' + t('كل التطبيقات', 'All apps') + '</h1><span class="apps-count">' + digits(D.apps.length) + '</span></div><div class="apps-tools">' +
          button('prayer-day', '<span data-app-prayer></span><span data-app-time></span>', '', 'next-chip', t('اليوم والقبلة', 'Today & Qibla')) +
          button('settings', icon('gear'), '', 'round-back', t('الإعدادات', 'Settings')) + '</div><div class="app-grid">' + D.apps.map(appTile).join('') +
          '</div></section>';
      } else if (view === 'clock') {
        html = '<section class="page clock-page"><div data-clock></div><p data-date></p>' + button('input', t('العودة للمشاهدة', 'Back to input')) + '</section>';
      } else if (view === 'inputs') {
        html = '<section class="page">' + header(t('المداخل', 'Inputs')) + '<div class="picker">' + inputs.map(function (item) {
          return button('select-input', escape(item.id === 'DTV' ? t('البث التلفزيوني', 'Live TV') : item.label) + (input && item.id === input.id ? ' ✓' : ''), item.id, 'item');
        }).join('') + button('refresh', t('تحديث المداخل', 'Refresh inputs')) + ' ' + button('input', t('العودة للمشاهدة', 'Back to input')) + '</div></section>';
      } else if (view === 'picker') {
        if (picker === 'voice' || picker === 'fajrVoice') {
          html = '<section class="page">' + header(picker === 'fajrVoice' ? t('صوت أذان الفجر', 'Fajr voice') : t('صوت الأذان', 'Adhan voice')) +
            '<p class="voice-count">' + digits(D.voices.length) + t(' صوتًا', ' voices') + '</p><div class="voice-list">' +
            D.voices.map(function (v) { return '<div class="voice-row">' + button('choose', escape(v[prefs.lang]) + (prefs[picker] === v.id ? ' ✓' : ''), v.id, 'voice-name') +
              button('preview-voice', voicePreview === v.id ? t('إيقاف', 'Stop') : t('معاينة', 'Preview'), v.id, 'voice-preview', (voicePreview === v.id ? t('إيقاف ', 'Stop ') : t('معاينة ', 'Preview ')) + v[prefs.lang]) + '</div>'; }).join('') +
            '</div><div class="voice-footer">' + button('settings', t('رجوع', 'Back')) + '</div></section>';
        } else if (stepper()) {
          html = '<section class="page">' + header(pickerTitle()) + '<div class="stepper"><strong>' + digits(editValue) +
            (picker === 'volume' ? '%' : '') + '</strong><div class="actions">' + button('step-down', '−') + button('step-up', '+') +
            button('save-step', t('حفظ', 'Save')) + button('settings', t('إلغاء', 'Cancel')) + '</div></div></section>';
        } else {
          var list = choices(), start = page * 6;
          html = '<section class="page">' + header(pickerTitle()) +
          '<div class="picker">' + list.slice(start, start + 6).map(function (item) { return button('choose', escape(item.label), item.value, 'item'); }).join('') +
          '<div class="actions">' + (page > 0 ? button('previous', t('السابق', 'Previous')) : '') +
          (start + 6 < list.length ? button('next', t('التالي', 'Next')) : '') + button('settings', t('رجوع', 'Back')) + '</div></div></section>';
        }
      }
    } catch (error) { html = '<section class="page">' + header(t('تعذر عرض الصفحة', 'Page unavailable')) + actions(false) + '</section>'; lastError = error.message; }
    screen.innerHTML = html;
    if (view === 'home') { bindHomeFocus(); }
    if (view !== 'input') {
      var target = focusAction && screen.querySelector(focusAction.charAt(0) === '[' ? focusAction : '[data-action="' + focusAction + '"]');
      target = target || (view === 'settings' ? screen.querySelector('.settings-nav .selected') :
        view === 'home' ? homeNodes[homeNavigation.initial(homeAppCount, 0)] :
        view === 'apps' ? screen.querySelector('.app-tile') : view === 'picker' && stepper() ? screen.querySelector('[data-action=save-step]') : screen.querySelector('button')); if (target) { target.focus(); }
    }
    updateClock(); report();
  }
  function armDock() { clearTimeout(dockTimer); if (view === 'bar' && prefs.dockAutoHide) { dockTimer = setTimeout(function () { show('input'); }, prefs.dockAutoHide * 1000); } }
  function show(nextView, focusAction) {
    clearTimeout(noticeTimer); document.getElementById('notice').style.display = 'none';
    if (nextView !== 'picker' && (nextView !== 'settings' || section !== 'appearance')) { appearanceDraft = null; }
    view = nextView; render(focusAction); armDock();
  }
  function updateClock() {
    if (document.hidden || document.webkitHidden || view === 'input') { return; }
    var now = new Date();
    function put(selector, text) { var list = screen.querySelectorAll(selector); for (var i = 0; i < list.length; i++) { list[i].textContent = text; } }
    put('[data-clock]', digits(A.clock(now, prefs)));
    if (view === 'apps') { var appNext = A.next(schedule(now), now); put('[data-app-prayer]', s('prayerNames')[appNext.index]); put('[data-app-time]', digits(A.clock(appNext.time, prefs))); }
    if (view === 'prayer-day' && dayViewMinute !== Math.floor(+now / 60000)) {
      var focused = document.activeElement, selector = focused && focused.getAttribute('data-action') === 'bell' ? '[data-action=bell][data-value="' + focused.getAttribute('data-value') + '"]' : 'home';
      render(selector); return;
    }
    if (view !== 'home' && view !== 'bar' && view !== 'clock') { return; }
    try {
      var next = A.next(schedule(now), now);
      put('[data-next-label]', (view === 'bar' ? ' · ' : '') + s('prayerNames')[next.index]);
      put('[data-countdown]', digits(A.duration(next.time - +now)));
      put('[data-next-time]', (next.time === cachedDay.tomorrowFajr ? t('غدًا · ', 'Tomorrow · ') : '') + digits(A.clock(next.time, prefs)));
      var rows = screen.querySelectorAll('[data-prayers]');
      var date = A.wall(now, prefs);
      var h = A.hijri(now, prefs);
      put('[data-hijri]', h ? digits(h.day + ' ' + s('hijriMonths')[h.month] + ' ' + h.year) : '');
      put('[data-gregorian]', digits(date.getUTCFullYear() + '/' + (date.getUTCMonth() + 1) + '/' + date.getUTCDate()));
      put('[data-date]', (h ? digits(h.day + ' ' + s('hijriMonths')[h.month] + ' ' + h.year) + ' · ' : '') + digits(date.getUTCFullYear() + '/' + (date.getUTCMonth() + 1) + '/' + date.getUTCDate()));
      var key = cacheKey + ':' + next.index + ':' + Math.floor(+now / 60000);
      for (var i = 0; i < rows.length; i++) {
        if (rows[i].getAttribute('data-key') !== key) { rows[i].innerHTML = prayers(now); rows[i].setAttribute('data-key', key); }
      }
    } catch (error) { put('[data-countdown]', '—'); }
  }
  function choices(key, allCountries) {
    key = key || picker;
    if (key === 'country') {
      var seen = {}, countries = [];
      D.cities.forEach(function (c) { if (!seen[c.countryEn]) { seen[c.countryEn] = true; countries.push({value: c.countryEn, label: c[prefs.lang === 'ar' ? 'countryAr' : 'countryEn']}); } });
      return countries;
    }
    if (key === 'city') { return D.cities.filter(function (c) { return allCountries || !country || c.countryEn === country; }).map(function (c) { return {value: c.id, label: c[prefs.lang]}; }); }
    if (key === 'method') {
      return [{value: '', label: t('طريقة المدينة الافتراضية', 'City default') }].concat(D.methods.map(function (id) { return {value: id, label: methodName(id)}; }));
    }
    if (key === 'voice' || key === 'fajrVoice') { return D.voices.map(function (v) { return {value: v.id, label: v[prefs.lang]}; }); }
    if (key === 'station') { return D.stations.map(function (v) { return {value: v.id, label: v[prefs.lang] + ' · ' + (v[prefs.lang === 'ar' ? 'whereAr' : 'whereEn'] || '')}; }); }
    if (key === 'theme') { return D.themes.map(function (id, i) { return {value: id, label: s('themeNames')[i]}; }); }
    if (key === 'testPrayer') { return [0, 2, 3, 4, 5].map(function (i) { return {value: i, label: s('prayerNames')[i]}; }); }
    var options = {
      language: [['ar', 'العربية'], ['en', 'English']],
      hanafi: [[false, t('شافعي / قياسي', 'Shafi / Standard')], [true, t('حنفي', 'Hanafi')]],
      remind: [[0, t('متوقف', 'Off')], [5, '5'], [10, '10'], [15, '15'], [20, '20'], [30, '30']],
      callClose: [[0, t('عند نهاية الصوت', 'When audio ends')], [1, t('دقيقة', '1 minute')], [5, t('٥ دقائق', '5 minutes')], [10, t('١٠ دقائق', '10 minutes')]],
      digits: [['auto', t('حسب اللغة', 'Follow language')], ['western', '0123'], ['eastern', '٠١٢٣']],
      clock: [['12', t('١٢ ساعة', '12-hour')], ['24', t('٢٤ ساعة', '24-hour')]],
      dockPosition: [['top', t('أعلى', 'Top')], ['bottom', t('أسفل', 'Bottom')]],
      dockAutoHide: [[0, t('يدويًا', 'Manually')], [10, '10'], [20, '20'], [30, '30']],
      homeLayout: [['mihrab', t('محراب', 'Mihrab')], ['compact', t('شريط مختصر', 'Compact strip')], ['hidden', t('مخفي', 'Hidden')]],
      archSide: [['auto', t('حسب اللغة', 'Follow language')], ['left', t('يسار', 'Left')], ['right', t('يمين', 'Right')]],
      homeBar: [['top', t('أعلى', 'Top')], ['bottom', t('أسفل', 'Bottom')]],
      prayerMode: [['all', t('كل المواقيت', 'All prayers')], ['current', t('الحالية فقط', 'Current only')], ['next', t('الصلاة التالية', 'Next prayer')], ['both', t('الحالية والقادمة', 'Current and next')], ['none', t('مخفية', 'Hidden')]],
      background: [['plain', t('سادة', 'Plain')], ['eight_point_stars', t('نجوم ثمانية', 'Eight-point stars')], ['mashrabiya', t('مشربية', 'Mashrabiya')], ['arches', t('أقواس', 'Arches')],
        ['arabesque', t('أرابيسك', 'Arabesque')], ['star_tiles', t('بلاط النجوم', 'Star tiles')], ['rosettes', t('وريدات', 'Rosettes')], ['octagon_lattice', t('شبكة مثمنة', 'Octagon lattice')], ['corner_medallions', t('زخارف الأركان', 'Corner medallions')]]
    };
    if (options[key]) { return options[key].map(function (v) { return {value: v[0], label: v[1]}; }); }
    return uiContract.choices('prayer.zone').map(function (value) {
      if (value === 'system') { return {value: '', label: t('توقيت التلفاز', 'TV time zone')}; }
      var offset = Number(value);
      return {value: value, label: 'UTC' + (offset < 0 ? '−' : '+') + Math.floor(Math.abs(offset) / 60) + ':' + (Math.abs(offset) % 60 < 10 ? '0' : '') + Math.abs(offset) % 60};
    });
  }
  function pickerTitle() {
    if (picker === 'country') { return t('الدولة', 'Country'); }
    if (picker === 'city') { return t('المدينة', 'City'); }
    if (picker === 'method') { return t('طريقة الحساب', 'Calculation method'); }
    if (picker === 'zone') { return t('التوقيت', 'Time zone'); }
    if (/^offset-/.test(picker)) { return s('prayerNames')[Number(picker.slice(7))]; }
    return sectionName(section) || t('اختر', 'Choose');
  }
  function stepper() { return /^offset-/.test(picker) || picker === 'volume' || picker === 'hijriOffset'; }
  function openPicker(key) {
    picker = key; page = 0;
    var source = screen.querySelector('.settings-rows [data-action="' + (key === 'country' ? 'city' : key) + '"][data-ui]');
    if (source) { pickerControl = source.getAttribute('data-ui'); }
    else if (!((key === 'country' || key === 'city') && pickerControl === 'prayer.location')) { throw new Error('Missing UI editor binding: ' + key); }
    uiContract.bind(pickerControl, stepper() ? 'number' : 'choice');
    var selected = key === 'country' ? A.city(prefs.city).countryEn : key === 'zone' ? prefs.utcOffset : key === 'testPrayer' ? testPrayer : (appearanceDraft || prefs)[key];
    if (stepper()) {
      editValue = /^offset-/.test(key) ? prefs.offsets[Number(key.slice(7))] : prefs[key];
    }
    else {
      var list = choices();
      if (key !== 'country') { choiceValues(pickerControl, list); }
      for (var i = 0; i < list.length; i++) { if (String(list[i].value) === String(selected === null ? '' : selected)) { page = Math.floor(i / 6); } }
    }
    show('picker');
  }
  function changeStep(delta) {
    var min = uiContract.minimum(pickerControl), max = uiContract.maximum(pickerControl);
    editValue = Math.max(min, Math.min(max, editValue + delta * (picker === 'volume' ? 5 : 1))); render('save-step');
  }
  function savePrefs(next, focusAction, returnView) {
    if (!loaded || busy) { return; } busy = true;
    request('savePrefs', {prefs: next}, function (reply) {
      busy = false;
      if (reply.returnValue) {
        if (focusAction === 'apply-appearance') { appearanceDraft = null; }
        prefs = A.prefs(reply.prefs); cacheKey = ''; view = returnView || 'settings'; render(focusAction);
        notify(reply.scheduleResult && !reply.scheduleResult.returnValue ? t('تم الحفظ لكن تعذر جدولة التنبيهات. راجع حالة الخدمة.', 'Saved, but alert scheduling failed. Check service status.') : t('تم الحفظ', 'Saved'));
      }
      else { notify(t('لم تُحفظ الإعدادات. حاول مرة أخرى.', 'Settings were not saved. Please try again.'), reply.errorText); }
    });
  }
  function play(target) {
    if (!target) { show('inputs'); return; }
    input = target; lastError = null;
    if (tv && !media && !(document.hidden || document.webkitHidden)) {
      video.style.display = 'block';
      video.pause(); while (video.firstChild) { video.removeChild(video.firstChild); }
      var source = document.createElement('source'); source.type = target.type; source.src = target.url;
      video.appendChild(source); video.load(); video.play();
    }
    report();
  }
  function discover(initial, retry) {
    request('inputs', {}, function (reply) {
      if (!reply.returnValue) {
        if (!retry) { setTimeout(function () { discover(initial, true); }, 1000); return; }
        notify(t('تعذر قراءة المداخل. افتح المداخل للتحديث.', 'Could not read inputs. Open Inputs to refresh.'), reply.errorText); show('home'); return;
      }
      inputs = reply.inputs.filter(function (item) { return !!A.input(item.id); });
      if (initial) {
        var target = reply.current && A.input(reply.current.id);
        if (target) { request('selectInput', {id: target.id}); play(target); show('input'); consumeAlert(); }
        else { show('inputs'); }
      } else if (view === 'inputs') { render('refresh'); }
      if (reply.discoveryFailed) { notify(t('تعذر تحديث بعض المداخل؛ يمكنك المحاولة مجددًا.', 'Some inputs could not be refreshed. Please try again.')); }
    });
  }
  function releaseVideo() {
    video.pause();
    while (video.firstChild) { video.removeChild(video.firstChild); }
    video.removeAttribute('src'); if (tv) { video.load(); } video.style.display = 'none';
  }
  function rememberContext() {
    var focused = document.activeElement, list = screen.querySelector('.voice-list') || screen.querySelector('.settings-rows');
    return {view: view, section: section, picker: picker, page: page, country: country, scroll: list ? list.scrollTop : 0,
      action: focused && focused.getAttribute('data-action'), value: focused && focused.getAttribute('data-value')};
  }
  function restoreContext() {
    var context = returnContext; returnContext = null;
    if (context && context.external && tv) {
      releaseVideo(); radioBeforeCall = false; report('hidden'); window.close(); return false;
    }
    if (!context) { show('input'); return; }
    section = context.section; picker = context.picker; page = context.page; country = context.country; view = context.view;
    render(context.action ? '[data-action="' + context.action + '"][data-value="' + escape(context.value || '') + '"]' : null);
    var list = screen.querySelector('.voice-list') || screen.querySelector('.settings-rows'); if (list) { list.scrollTop = context.scroll; }
    armDock();
  }
  function finishOverlay() { dismissOverlay(); restoreContext(); }
  function mediaReport(event, detail) {
    if (media) { request('media', {kind: media.kind, event: event, time: isFinite(media.audio.currentTime) ? media.audio.currentTime : 0, detail: detail || ''}); }
  }
  function dismissOverlay() { clearTimeout(overlayTimer); overlay.innerHTML = ''; overlay.className = ''; }
  function stopAudio(reason, restore, detail) {
    if (!media) { if (restore) { finishOverlay(); } return; }
    mediaReport(reason || 'stopped', detail);
    var old = media; media = null; voicePreview = null; audioSerial++;
    clearTimeout(audioTimer); clearTimeout(mediaTimer); clearInterval(old.progressTimer); clearTimeout(old.deadline);
    old.audio.pause(); old.audio.removeAttribute('src'); old.audio.load();
    if (old.audio.parentNode) { old.audio.parentNode.removeChild(old.audio); }
    dismissOverlay();
    if (restore && !(document.hidden || document.webkitHidden)) {
      var keepView = returnContext && returnContext.view !== 'input';
      if (restoreContext() === false) { return; }
      var serial = audioSerial;
      mediaTimer = setTimeout(function () {
        if (serial !== audioSerial || media) { return; }
        if (radioBeforeCall && prefs.radioResume) { radioBeforeCall = false; startRadio(keepView); }
        else { radioBeforeCall = false; play(input); }
      }, 450);
    }
  }
  function startAudio(kind, url, seconds) {
    if (media) { stopAudio('stopped', false); }
    releaseVideo();
    var audio = document.createElement('audio'), serial = ++audioSerial;
    audio.setAttribute('preload', 'none'); audio.volume = prefs.volume / 100;
    media = {kind: kind, audio: audio, serial: serial, started: false};
    document.getElementById('stage').appendChild(audio);
    function active() { return media && media.serial === serial; }
    function failed(detail) {
      if (!active()) { return; }
      stopAudio('error', true, detail);
      notify(t('تعذر تشغيل الصوت. تمت العودة للمدخل.', 'Audio could not play. Returning to input.'), detail);
    }
    audio.addEventListener('playing', function () {
      if (!active()) { return; } media.started = true; clearTimeout(audioTimer); mediaReport('playing');
      var status = overlay.querySelector('[data-audio-status]') || screen.querySelector('[data-radio-status]');
      if (status) { status.textContent = t('جارٍ التشغيل', 'Playing'); }
      var toggle = overlay.querySelector('[data-action=toggle-adhan]'); if (toggle) { toggle.textContent = t('إيقاف', 'Stop'); }
    });
    audio.addEventListener('error', function () { failed('Media error ' + (audio.error ? audio.error.code : 'unknown')); });
    audio.addEventListener('ended', function () {
      if (!active()) { return; } mediaReport('ended');
      media.ended = true;
      if (kind === 'radio') { stopAudio('ended', true); }
      else if (!seconds && prefs.callClose > 0 && kind === 'adhan') {
        var status = overlay.querySelector('[data-audio-status]'); if (status) { status.textContent = t('انتهى الأذان', 'Adhan finished'); }
        var toggle = overlay.querySelector('[data-action=toggle-adhan]'); if (toggle) { toggle.textContent = t('تشغيل', 'Play'); }
      } else { stopAudio('ended', true); }
    });
    var progressCount = 0;
    media.progressTimer = setInterval(function () {
      if (!active()) { return; }
      progressCount++;
      if (progressCount === 2 || progressCount === 15) { mediaReport('progress'); }
      if (progressCount >= 15) { clearInterval(media.progressTimer); }
    }, 1000);
    if (kind !== 'radio') {
      media.deadline = setTimeout(function () { if (active()) { stopAudio('stopped', true); } }, (seconds || (prefs.callClose ? prefs.callClose * 60 : 600)) * 1000);
    }
    mediaReport('loading');
    // This LG needs the HDMI source unloaded before it can allocate an audio decoder.
    mediaTimer = setTimeout(function () {
      if (!active()) { return; }
      audio.src = url; audio.load();
      try { var started = audio.play(); if (started && started.catch) { started.catch(function (error) { failed(error.message); }); } }
      catch (error) { failed(error.message); }
      if (active() && !media.started) {
        audioTimer = setTimeout(function () { failed('Audio did not start within 15 seconds'); }, 15000);
      }
    }, 450);
  }
  function startRadio(keepView) {
    radioBeforeCall = false; dismissOverlay(); startAudio('radio', A.station(prefs.station).url, 0); if (!keepView) { show('radio'); }
  }
  function startAlert(alert) {
    if ([0, 2, 3, 4, 5].indexOf(alert.prayer) < 0 || ['banner', 'card', 'adhan', 'spoken', 'announcement'].indexOf(alert.mode) < 0) { return; }
    var audible = ['adhan', 'spoken', 'announcement'].indexOf(alert.mode) >= 0, hadAudio = !!media;
    var changedInput = alert.input && (!input || input.id !== alert.input);
    returnContext = alert.returnToPrevious ? {external: true} : rememberContext();
    if (alert.input && A.input(alert.input)) { input = A.input(alert.input); }
    if (audible && media && media.kind === 'radio') { radioBeforeCall = true; }
    if (media && (audible || alert.test)) { stopAudio('stopped', false); }
    dismissOverlay();
    if (!audible && (!media || alert.test) || view === 'input') { show('input'); }
    clearTimeout(noticeTimer); document.getElementById('notice').style.display = 'none';
    if (!audible && !media && (hadAudio || changedInput)) { play(input); }
    var title = alert.mode === 'banner' || alert.mode === 'card' || alert.mode === 'spoken' ? t('اقترب موعد صلاة ', 'Prayer approaching: ') : t('حان الآن موعد صلاة ', 'It is time for ');
    var label = s('prayerNames')[alert.prayer], isFriday = A.wall(new Date(), prefs).getUTCDay() === 5;
    if (isFriday && alert.prayer === 2) { label = t('الجمعة', 'Jumuah'); }
    if (audible) {
      var file;
      if (alert.mode === 'adhan') { file = 'audio/adhan/' + (alert.prayer === 0 ? prefs.fajrVoice : prefs.voice) + '.mp3'; }
      else { file = 'audio/announce/' + (alert.mode === 'spoken' ? 'before_' : 'now_') + (isFriday && alert.prayer === 2 ? 'jumuah' : ['fajr', '', 'dhuhr', 'asr', 'maghrib', 'isha'][alert.prayer]) + '.mp3'; }
      startAudio(alert.mode, file, alert.test && !alert.full ? 12 : 0);
    }
    overlay.className = audible ? 'audio-overlay' : alert.mode === 'card' ? 'card-overlay' : 'banner-overlay';
    if (alert.mode === 'adhan') {
      overlay.innerHTML = '<svg class="call-frame" width="720" height="1080" viewBox="0 0 600 900" aria-hidden="true"><path class="outer" d="M0 900 L0 330 C0 170 140 70 300 0 C460 70 600 170 600 330 L600 900 Z"/>' +
        '<path class="inner" d="M18 900 L18 334 C18 184 150 92 300 22 C450 92 582 184 582 334 L582 900"/></svg><section class="call-column">' +
        '<div class="call-verse" lang="ar" dir="rtl">' + escape(D.sacred.adhanCall) + '</div><p class="call-label">' + (alert.test ? t('اختبار الأذان', 'Adhan test') : t('حان وقت الصلاة', 'Time for prayer')) +
        '</p><div class="call-name">' + escape(label) + '</div><div class="call-time">' + digits(A.clock(alert.prayerTime || new Date(), prefs)) + '</div>' +
        button('toggle-adhan', t('إيقاف', 'Stop'), '', 'call-play') + button('dismiss-alert', t('إغلاق', 'Dismiss'), '', 'call-dismiss') +
        '<p class="call-status" data-audio-status>' + t('جارٍ تحميل الصوت…', 'Loading audio…') + '</p></section>';
    } else {
      overlay.innerHTML = '<section class="alert-box"><p class="eyebrow">' + (alert.test ? t('اختبار · واجهة التلفاز الإسلامي', 'Test · Islamic TV Launcher') : t('تذكير بالصلاة', 'Prayer reminder')) +
      '</p><h1>' + title + escape(label) + '</h1><p>' + escape(A.city(prefs.city)[prefs.lang]) + ' · ' + digits(A.clock(alert.prayerTime || new Date(), prefs)) +
      '</p>' + (audible ? '<p data-audio-status>' + t('جارٍ تحميل الصوت…', 'Loading audio…') + '</p>' : '') +
      button('dismiss-alert', audible ? t('إيقاف والعودة', 'Stop & return') : t('إغلاق', 'Dismiss')) + '</section>';
    }
    overlay.querySelector('button').focus();
    if (!audible) { overlayTimer = setTimeout(finishOverlay, alert.test ? 16000 : 20000); }
  }
  function consumeAlert() {
    if (!loaded || !pendingLaunch || pendingLaunch === seenLaunch) { return; }
    var id = pendingLaunch, cold = coldAlert; seenLaunch = id; coldAlert = false;
    request('takeAlert', {id: id}, function (reply) {
      if (reply.returnValue) { if (cold) { reply.alert.returnToPrevious = true; } startAlert(reply.alert); }
    });
  }
  function activate(action, value) {
    if (busy) { return; }
    armDock();
    if (voicePreview && action !== 'preview-voice') { stopAudio('stopped', true); }
    if (action === 'apps-back') { show('home', 'apps'); return; }
    if (action === 'settings-back') {
      if (section === 'offsets') { section = 'prayer'; render('offsets'); }
      else { show(settingsParent, 'settings'); }
      return;
    }
    if (action === 'carousel') { return; }
    if (action === 'carousel-step') { var step = value.split(':'); changeAppearance(step[0], Number(step[1])); return; }
    if (action === 'apply-appearance') {
      var appearance = A.prefs(prefs);
      appearanceKeys.forEach(function (key) { appearance[key] = appearanceDraft[key]; });
      savePrefs(appearance, 'apply-appearance'); return;
    }
    if (action === 'cancel-appearance') { appearanceDraft = A.prefs(prefs); render('[data-action=carousel][data-value=theme]'); return; }
    if (action === 'reset-layout') {
      ['homeLayout', 'archSide', 'homeBar', 'prayerMode'].forEach(function (key) { appearanceDraft[key] = A.defaults[key]; });
      render('reset-layout'); return;
    }
    if (action === 'dismiss-alert') { if (media && media.kind !== 'radio') { stopAudio('stopped', true); } else { finishOverlay(); } return; }
    if (action === 'preview-voice') {
      if (!A.voice(value)) { return; }
      if (voicePreview === value) { stopAudio('stopped', true); return; }
      returnContext = rememberContext();
      startAudio('adhan', 'audio/adhan/' + value + '.mp3', 12); voicePreview = value;
      var scroll = returnContext.scroll; render('[data-action=preview-voice][data-value="' + value + '"]');
      screen.querySelector('.voice-list').scrollTop = scroll; return;
    }
    if (action === 'toggle-adhan' && media) {
      var toggle = overlay.querySelector('[data-action=toggle-adhan]');
      if (media.paused || media.ended) {
        clearTimeout(overlayTimer); media.paused = false; media.ended = false;
        try {
          media.audio.currentTime = 0;
          var playing = media.audio.play();
          if (playing && playing.catch) { playing.catch(function (error) { stopAudio('error', true, error.message); }); }
          toggle.textContent = t('إيقاف', 'Stop');
        } catch (error) { stopAudio('error', true, error.message); }
      } else {
        media.audio.pause(); media.paused = true; mediaReport('stopped'); toggle.textContent = t('تشغيل', 'Play');
        if (prefs.callClose === 0) { overlayTimer = setTimeout(function () { stopAudio('stopped', true); }, 10000); }
      }
      return;
    }
    if (['input', 'home', 'settings', 'inputs', 'bar', 'prayer-day', 'radio', 'apps'].indexOf(action) >= 0) {
      if (action === 'input' && media) { radioBeforeCall = false; stopAudio('stopped', true); return; }
      if (action === 'settings' && view !== 'picker' && view !== 'settings') { settingsParent = view === 'apps' ? 'apps' : 'home'; }
      if (action === 'prayer-day') { dayParent = view === 'apps' || view === 'settings' ? view : 'home'; }
      show(action); if (action === 'inputs') { discover(false); } return;
    }
    var next = A.prefs(prefs);
    if (action === 'section') { if (section !== value) { appearanceDraft = null; } section = value; render('[data-action=section][data-value=' + value + ']'); return; }
    if (action === 'tests') { section = 'tests'; show('settings'); return; }
    if (action === 'offsets') { section = 'offsets'; render(); return; }
    if (action === 'reset-offsets') { next.offsets = [0, 0, 0, 0, 0, 0]; savePrefs(next, action); return; }
    if (action === 'bell') { if ([0, 2, 3, 4, 5].indexOf(Number(value)) < 0) { return; } next.bells[Number(value)] = !next.bells[Number(value)]; savePrefs(next, '[data-action=bell][data-value="' + value + '"]', 'prayer-day'); return; }
    if (['automatic', 'adhan', 'remindVoice', 'announce', 'radioResume', 'quickLabels'].indexOf(action) >= 0) { next[action] = !next[action]; savePrefs(next, action); return; }
    if (action === 'language') { next.lang = prefs.lang === 'ar' ? 'en' : 'ar'; savePrefs(next, action); }
    if (action === 'hanafi') { next.hanafi = !next.hanafi; savePrefs(next, action); }
    if (action === 'city') { openPicker('country'); }
    if (['method', 'zone', 'voice', 'fajrVoice', 'station', 'testPrayer', 'remind', 'callClose', 'digits', 'clock', 'dockPosition',
      'dockAutoHide', 'volume', 'hijriOffset'].indexOf(action) >= 0 || /^offset-[0-5]$/.test(action)) { openPicker(action); }
    if (action === 'step-down' || action === 'step-up') { changeStep(action === 'step-down' ? -1 : 1); }
    if (action === 'save-step') {
      if (/^offset-/.test(picker)) { next.offsets[Number(picker.slice(7))] = editValue; } else { next[picker] = editValue; }
      savePrefs(next, picker);
    }
    if (action === 'next' || action === 'previous') { page += action === 'next' ? 1 : -1; render(); }
    if (action === 'choose') {
      if (!choices().some(function (item) { return String(item.value) === String(value); })) { throw new Error('Unknown offered UI value'); }
      if (picker !== 'country') { choiceValues(pickerControl, [{value: value}]); }
      if (picker === 'country') { country = value; openPicker('city'); return; }
      if (picker === 'testPrayer') { testPrayer = Number(value); show('settings'); return; }
      if (picker === 'city') { next.city = value; next.method = null; next.hanafi = A.city(value).hanafi; }
      else if (picker === 'method') { next.method = value || null; }
      else if (picker === 'zone') { next.utcOffset = value === '' ? null : Number(value); }
      else { next[picker] = typeof A.defaults[picker] === 'number' ? Number(value) : value; }
      savePrefs(next, picker);
    }
    if (action === 'startup') {
      if (!loaded) { return; } busy = true;
      request('setStartup', {enabled: !startup}, function (reply) {
        busy = false; if (typeof reply.enabled === 'boolean') { startup = reply.enabled; }
        render('startup');
        if (!reply.returnValue) { notify(t('تعذر تحديث التشغيل التلقائي.', 'Could not update automatic startup.'), reply.errorText || 'Startup activity failed'); }
      });
    }
    if (action === 'select-input') {
      if (media) { radioBeforeCall = false; stopAudio('stopped', false); }
      busy = true; request('selectInput', {id: value}, function (reply) {
        busy = false;
        if (reply.returnValue) { play(A.input(value)); show('input'); }
        else { notify(t('تعذر حفظ المدخل.', 'Could not save the input.'), reply.errorText); }
      });
    }
    if (action === 'refresh') { discover(false); }
    if (action === 'clock-view') { show('clock'); }
    if (action === 'radio-play') { startRadio(); }
    if (action === 'radio-stop') { radioBeforeCall = false; stopAudio('stopped', true); }
    if (action === 'radio-settings') { section = 'radio'; show('settings'); openPicker('station'); }
    if (/^test-(banner|card|adhan|full|spoken|announcement)$/.test(action)) {
      startAlert({mode: action === 'test-full' ? 'adhan' : action.slice(5), prayer: testPrayer, test: true, full: action === 'test-full'});
    }
    if (action === 'test-background') {
      request('testAlert', {mode: 'adhan', prayer: testPrayer}, function (reply) {
        if (reply.returnValue) { show('input'); notify(t('اختبار الأذان بعد ٣٠ ثانية', 'Adhan test in 30 seconds')); }
        else { notify(t('تعذر جدولة الاختبار', 'Could not schedule the test'), reply.errorText); }
      });
    }
    if (action === 'cancel-test') { request('cancelTest', {}, function (reply) { notify(reply.returnValue ? t('تم إلغاء الاختبار', 'Test cancelled') : t('تعذر الإلغاء', 'Could not cancel')); }); }
    if (action === 'native-app' || action === 'tv-settings') {
      if (busy) { return; } busy = true;
      request('launchApp', {id: action === 'tv-settings' ? 'com.palm.app.settings' : value}, function (reply) {
        busy = false;
        if (!reply.returnValue) { notify(t('تعذر فتح تطبيق التلفاز', 'Could not open the TV app'), reply.errorText); }
      });
    }
    if (action === 'health') {
      request('status', {}, function (reply) {
        var state = reply.state || {};
        notify(t('التشغيل التلقائي: ', 'Auto-start: ') + onOff(!!(state.startup && state.startup.enabled)) + ' · ' +
          t('التنبيه التالي: ', 'Next alert: ') + (state.alarm ? digits(A.clock(state.alarm.time, prefs)) : t('لا يوجد', 'None')) +
          (state.alarmResult && !state.alarmResult.returnValue ? ' · ' + t('خطأ في الجدولة', 'Scheduling error') : ''));
      });
    }
    if (action === 'exit') {
      if (!input) { if (tv) { window.close(); } return; }
      busy = true; request('exitToInput', {id: input.id}, function (reply) {
        busy = false;
        if (reply.returnValue) { video.pause(); if (tv) { window.close(); } }
        else { notify(t('تعذر الخروج إلى المدخل. استخدم زر المداخل في الريموت.', 'Could not return to the input. Use the remote’s Input button.'), reply.errorText); }
      });
    }
  }
  document.getElementById('stage').addEventListener('click', function (event) {
    if (busy) { return; }
    var el = event.target;
    while (el && el !== document.getElementById('stage') && el.tagName !== 'BUTTON') { el = el.parentNode; }
    if (el && el.tagName === 'BUTTON') { activate(el.getAttribute('data-action'), el.getAttribute('data-value')); }
    else if (view === 'input' && !overlay.firstChild) { show('bar'); }
  });
  screen.addEventListener('focus', function (event) {
    var id = Number(event.target.getAttribute('data-home-focus'));
    if (view === 'home' && id >= homeNavigation.FIRST_APP && id < homeNavigation.FIRST_APP + homeAppCount) { lastHomeApp = id; }
  }, true);
  function move(key) {
    var current = document.activeElement;
    if (view === 'home') {
      var dx = key === 37 ? -1 : key === 39 ? 1 : 0, dy = key === 38 ? -1 : key === 40 ? 1 : 0;
      if (prefs.lang === 'ar') { dx = -dx; }
      var target = homeNavigation.move(homeNodes.indexOf(current), homeAppCount, 0, Math.max(1, homeAppCount), lastHomeApp, dx, dy, !!homeNodes[homeNavigation.WATCH_INPUT]);
      if (homeNodes[target] && !homeNodes[target].disabled) { homeNodes[target].focus(); }
      return;
    }
    var items = screen.querySelectorAll('button');
    if (!current || current.tagName !== 'BUTTON') { if (items[0]) { items[0].focus(); } return; }
    var from = current.getBoundingClientRect(), x = (from.left + from.right) / 2, y = (from.top + from.bottom) / 2;
    var best = null, score = Infinity;
    for (var i = 0; i < items.length; i++) {
      if (items[i] === current || items[i].disabled || items[i].getAttribute('tabindex') === '-1') { continue; }
      var rect = items[i].getBoundingClientRect(), dx = (rect.left + rect.right) / 2 - x, dy = (rect.top + rect.bottom) / 2 - y;
      var primary = key === 37 ? -dx : key === 39 ? dx : key === 38 ? -dy : dy;
      var secondary = key === 37 || key === 39 ? Math.abs(dy) : Math.abs(dx);
      if (primary > 1 && secondary < primary * 2 && primary + secondary * 3 < score) { score = primary + secondary * 3; best = items[i]; }
    }
    if (best) { best.focus(); }
  }
  document.addEventListener('keydown', function (event) {
    var key = event.keyCode;
    if ([13, 27, 37, 38, 39, 40, 461, 413].indexOf(key) < 0) { return; }
    event.preventDefault();
    if (busy) { return; }
    armDock();
    if (key === 413) { if (media) { radioBeforeCall = false; stopAudio('stopped', true); } return; }
    if (overlay.firstChild) {
      if (key === 461 || key === 27) { activate('dismiss-alert'); }
      else if (key === 13) { var focusedOverlay = document.activeElement; if (focusedOverlay && overlay.contains(focusedOverlay) && focusedOverlay.tagName === 'BUTTON') { focusedOverlay.click(); } }
      else if (key === 38 || key === 40) {
        var overlayButtons = overlay.querySelectorAll('button'); if (overlayButtons.length) { overlayButtons[key === 38 ? 0 : overlayButtons.length - 1].focus(); }
      }
      return;
    }
    if (view === 'picker' && stepper() && (key === 37 || key === 39)) { changeStep(key === 37 ? -1 : 1); return; }
    if (view === 'settings' && document.activeElement && document.activeElement.getAttribute('data-action') === 'carousel' && (key === 37 || key === 39)) {
      changeAppearance(document.activeElement.getAttribute('data-value'), (key === 37) !== (prefs.lang === 'ar') ? -1 : 1); return;
    }
    if (key === 461 || key === 27) {
      if (voicePreview) { stopAudio('stopped', true); }
      if (view === 'picker' && picker === 'city') { openPicker('country'); }
      else if (view === 'settings' && section === 'offsets') { section = 'prayer'; render('offsets'); }
      else if (view === 'settings' && document.activeElement && ['section', 'settings-back'].indexOf(document.activeElement.getAttribute('data-action')) < 0) { appearanceDraft = null; render('[data-action=section][data-value=' + section + ']'); }
      else if (view === 'apps') { show('home', 'apps'); }
      else if (view === 'settings') { show(settingsParent, 'settings'); }
      else if (view === 'prayer-day') { show(dayParent, 'prayer-day'); }
      else {
        var backView = view === 'picker' ? 'settings' : ['settings', 'inputs', 'prayer-day', 'radio', 'apps'].indexOf(view) >= 0 ? 'home' : view === 'input' ? 'bar' : 'input';
        if (backView === 'input' && media) { radioBeforeCall = false; stopAudio('stopped', true); } else { show(backView); }
      }
    }
    else if (view === 'input') { show('bar'); }
    else if (key === 13) { if (document.activeElement && document.activeElement.tagName === 'BUTTON') { document.activeElement.click(); } }
    else { move(key); }
  });
  video.addEventListener('error', function () {
    if (media || !video.firstChild || document.hidden || document.webkitHidden) { return; }
    notify(t('تعذر عرض المدخل. اضغط OK لاختيار مدخل آخر.', 'Input playback failed. Press OK to choose another input.'), 'Media error ' + (video.error ? video.error.code : 'unknown'));
  });
  function visibility() {
    if (document.hidden || document.webkitHidden) {
      returnContext = null; radioBeforeCall = false; if (media) { stopAudio('stopped', false); } dismissOverlay(); releaseVideo(); report('hidden');
    } else if (input && !media) { play(input); updateClock(); report(); }
  }
  document.addEventListener('visibilitychange', visibility);
  document.addEventListener('webkitvisibilitychange', visibility);
  function launched(event) {
    var p = event.detail || {};
    if (typeof p === 'string') { try { p = JSON.parse(p); } catch (error) { p = {}; } }
    if (typeof p.alertId === 'string' && p.alertId.length < 120) { pendingLaunch = p.alertId; consumeAlert(); }
    else if (loaded && !media) { discover(true); }
  }
  document.addEventListener('webOSRelaunch', launched);
  document.addEventListener('webOSLaunch', launched);
  if (tv && window.PalmSystem && PalmSystem.launchParams) { launched({detail: PalmSystem.launchParams}); }
  function scale() {
    var ratio = Math.min(window.innerWidth / 1920, window.innerHeight / 1080);
    document.getElementById('stage').style.webkitTransform = 'scale(' + ratio + ')';
    document.getElementById('stage').style.transform = 'scale(' + ratio + ')';
  }
  window.addEventListener('resize', scale); scale();
  if (!tv) { document.getElementById('preview').style.display = 'block'; }
  function load(retry) {
    request('status', {}, function (reply) {
      if (!reply.returnValue) {
        if (!retry) { setTimeout(function () { load(true); }, 1000); return; }
        show('home'); notify(t('تعذر تحميل الإعدادات. أعد فتح المشغل للمحاولة.', 'Settings could not be loaded. Reopen the launcher to retry.'), reply.errorText); return;
      }
      try { prefs = A.prefs(reply.state.prefs); } catch (error) { notify(t('تعذر قراءة الإعدادات المحفوظة.', 'Saved settings could not be read.'), error.message); return; }
      startup = !!(reply.state.startup && reply.state.startup.enabled); loaded = true;
      if (reply.state.pendingAlert && !reply.state.pendingAlert.displayedAt) { pendingLaunch = reply.state.pendingAlert.id; coldAlert = true; }
      discover(true);
      if (prefs.automatic) { request('syncAlerts', {}, function (result) { if (!result.returnValue) { notify(t('تعذر جدولة التنبيهات', 'Could not schedule alerts'), result.errorText); } }); }
    });
  }
  load(false); setInterval(updateClock, 1000);
}());
