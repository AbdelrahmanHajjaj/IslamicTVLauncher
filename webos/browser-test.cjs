const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const { chromium } = require('playwright');
const base = path.join(__dirname, 'build/app');
const shots = path.resolve(__dirname, '../state/webos-port');
fs.mkdirSync(shots, { recursive: true });
const server = http.createServer((req, res) => {
  const file = path.resolve(base, '.' + (req.url === '/' ? '/index.html' : req.url));
  if (!file.startsWith(base + path.sep) || !fs.existsSync(file)) { res.writeHead(404); res.end(); return; }
  const types = {'.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.ttf': 'font/ttf', '.svg': 'image/svg+xml'};
  res.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
  res.end(fs.readFileSync(file));
});
(async () => {
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  let browser;
  try {
    browser = await chromium.launch({channel: 'msedge', headless: true});
    const page = await browser.newPage({viewport: {width: 1920, height: 1080}});
    await page.addInitScript(() => {
      // Mock the TV boundary and decoder events; the physical LG validates actual picture/sound.
      window.mediaCalls = [];
      window.closeCount = 0; window.close = () => { window.closeCount++; };
      HTMLMediaElement.prototype.load = function () { window.mediaCalls.push({op: 'load', tag: this.tagName, src: this.getAttribute('src'), children: this.children.length}); };
      HTMLMediaElement.prototype.pause = function () { window.mediaCalls.push({op: 'pause', tag: this.tagName}); };
      HTMLMediaElement.prototype.play = function () {
        window.mediaCalls.push({op: 'play', tag: this.tagName, src: this.getAttribute('src')});
        setTimeout(() => this.dispatchEvent(new Event('playing')), 0); return Promise.resolve();
      };
      window.PalmServiceBridge = function () {
        this.call = (uri, raw) => {
          const p = JSON.parse(raw), method = uri.split('/').pop();
          const state = JSON.parse(localStorage.getItem('fixture') || '{}');
          if (method === 'savePrefs') state.prefs = p.prefs;
          if (method === 'setStartup') state.startup = {enabled: p.enabled};
          if (method === 'selectInput') state.input = p.id;
          if (method === 'media') state.media = p;
          if (method === 'launchApp') state.launchedApp = p.id;
          localStorage.setItem('fixture', JSON.stringify(state));
          const reply = {returnValue: true, state, prefs: state.prefs, enabled: state.startup && state.startup.enabled,
            current: window.Afnon.input(state.input || 'HDMI_1'), inputs: ['HDMI_1', 'HDMI_2', 'HDMI_3', 'DTV'].map(window.Afnon.input)};
          if (method === 'takeAlert') { reply.alert = state.pendingAlert; if (state.expiredAlert) { reply.returnValue = false; } }
          if (method === 'launchApp' && window.failAppLaunch) { reply.returnValue = false; reply.errorText = 'App unavailable'; }
          setTimeout(() => this.onservicecallback(JSON.stringify(reply)), 5);
        };
      };
    });
    const errors = []; page.on('pageerror', e => errors.push(e.message));
    await page.goto('http://127.0.0.1:' + server.address().port);
    assert.match(await page.title(), /^Islamic TV Launcher/);
    await page.evaluate(() => document.fonts.ready);
    await page.waitForSelector('#input-video source', {state: 'attached'});
    await page.evaluate(() => { window.originalVideo = document.getElementById('input-video'); });
    // Magic Remote OK becomes a click while its pointer is visible.
    await page.mouse.click(960, 540);
    await page.waitForSelector('.bar', {timeout: 1500});
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('button').count(), 0);
    await page.keyboard.press('Enter');
    await page.waitForSelector('.bar');
    const barBounds = await page.locator('.bar').boundingBox();
    assert.equal(barBounds.y, 0, 'prayer bar stays at top like the Android dock');
    assert.ok(barBounds.height < 140, 'dock stays compact over HDMI');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'home');
    await page.keyboard.press('ArrowLeft');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'inputs');
    await page.keyboard.press('ArrowRight');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'home');
    await page.keyboard.press('Enter');
    await page.waitForSelector('.arch');
    assert.equal(await page.locator('html').getAttribute('dir'), 'rtl');
    assert.match(await page.locator('[data-countdown]').innerText(), /[٠-٩]/);
    assert.equal(await page.locator('.prayer-name').getAttribute('dir'), 'rtl');
    assert.match(await page.locator('.prayer-name').evaluate(el => getComputedStyle(el).fontFamily), /Alexandria/);
    assert.doesNotMatch(await page.locator('body').innerText(), /Afnon|أفنون/);
    async function homeControls() {
      assert.deepEqual(await page.locator('.home-quick button').evaluateAll(items => items.map(el => el.dataset.action)), ['prayer-day', 'inputs', 'radio', 'tv-settings', 'settings']);
      assert.equal(await page.locator('.home-apps [data-action=native-app]').count(), 3, 'large Home tiles belong to TV apps');
      const clock = await page.locator('.home-clock .clock').boundingBox();
      for (const control of await page.locator('.home-quick button').all()) {
        const b = await control.boundingBox();
        assert.ok(b.y < 150 && b.width <= 72 && b.height <= 72, 'quick controls are compact top icons');
        assert.ok(b.x >= clock.x + clock.width || b.x + b.width <= clock.x, 'top icons do not cover the clock');
        assert.ok(await control.getAttribute('aria-label'), 'icon has an accessible name');
      }
    }
    await homeControls();
    await page.locator('[data-action=prayer-day]').focus();
    await page.keyboard.press('ArrowLeft');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'inputs', 'RTL remote moves along the small top row');
    await page.keyboard.press('ArrowDown');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'native-app', 'Down reaches app tiles');
    await page.screenshot({path: path.join(shots, 'home-ar.png')});
    const arCount = await page.locator('.prayer').count(); assert.equal(arCount, 6);
    await page.locator('[data-action=settings]').click();
    await page.screenshot({path: path.join(shots, 'settings-ar.png')});
    await page.keyboard.press('ArrowLeft');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'city', 'RTL direction enters settings rows');
    await page.keyboard.press('Escape');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'section', 'Back returns to sidebar');
    await page.locator('[data-action=section][data-value=reminders]').click();
    await page.locator('[data-action=automatic]').focus();
    for (let i = 0; i < 10; i++) await page.keyboard.press('ArrowDown');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'tests', 'remote reaches the last scrolling reminder setting');
    const bottomSetting = await page.locator(':focus').boundingBox();
    assert.ok(bottomSetting.y >= 172 && bottomSetting.y + bottomSetting.height <= 972, 'focused row scrolls fully into view');
    await page.screenshot({path: path.join(shots, 'reminders-ar.png')});
    await page.locator('[data-action=section][data-value=display]').click();
    await page.locator('[data-action=language]').click();
    await page.waitForFunction(() => document.documentElement.dir === 'ltr');
    assert.equal(await page.locator('html').getAttribute('dir'), 'ltr');
    await page.locator('[data-action=section][data-value=prayer]').click();
    await page.locator('[data-action=city]').click();
    await page.locator('[data-action=choose][data-value=Egypt]').click();
    await page.locator('[data-action=choose][data-value=giza]').click();
    await page.waitForSelector('[data-action=city]');
    assert.match(await page.locator('[data-action=city]').innerText(), /Giza/);
    await page.locator('[data-action=hanafi]').click();
    await page.waitForFunction(() => document.querySelector('[data-action=hanafi]').textContent.includes('Hanafi'));
    assert.match(await page.locator('[data-action=hanafi]').innerText(), /Hanafi/);
    await page.locator('[data-action=section][data-value=device]').click();
    await page.locator('[data-action=startup]').click();
    await page.waitForFunction(() => document.querySelector('[data-action=startup]').textContent.includes('On'));
    assert.match(await page.locator('[data-action=startup]').innerText(), /On/);
    await page.screenshot({path: path.join(shots, 'settings-en.png')});
    await page.keyboard.press('Escape');
    await page.keyboard.press('Escape');
    await page.waitForSelector('.arch');
    await homeControls();
    await page.locator('[data-action=prayer-day]').focus();
    await page.keyboard.press('ArrowRight');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'inputs', 'LTR remote moves along the small top row');
    await page.screenshot({path: path.join(shots, 'home-en.png')});
    await page.locator('[data-action=apps]').click();
    assert.equal(await page.locator('.app-grid button').count(), 16);
    await page.screenshot({path: path.join(shots, 'apps-en.png')});
    assert.equal(await page.locator('[data-action=apps-back]').getAttribute('aria-label'), 'Back');
    await page.locator('[data-action=apps-back]').click();
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps', 'visible Back restores Home focus');
    await page.keyboard.press('Enter');
    await page.locator('.apps-tools [data-action=settings]').click();
    assert.equal(await page.locator('[data-action=settings-back]').getAttribute('aria-label'), 'Back');
    await page.locator('[data-action=settings-back]').click();
    assert.equal(await page.locator('.apps-page').count(), 1, 'Settings visible Back returns to All apps');
    await page.locator('.apps-tools [data-action=settings]').click();
    await page.keyboard.press('Escape');
    await page.waitForSelector('.apps-page');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'settings', 'Settings opened from apps returns to apps');
    await page.locator('.apps-tools [data-action=prayer-day]').click();
    await page.keyboard.press('Escape');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'prayer-day', 'prayer chip returns to apps');
    assert.equal(await page.locator('[data-value="com.webos.app.adapp"]').count(), 0);
    await page.locator('[data-value="netflix"]').focus();
    for (let i = 0; i < 3; i++) await page.keyboard.press('ArrowDown');
    assert.equal(await page.locator(':focus').getAttribute('data-value'), 'com.palmdts.devmode', 'remote reaches the last app row in the five-column grid');
    await page.locator('[data-value="com.webos.app.browser"]').click();
    assert.equal(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).launchedApp), 'com.webos.app.browser');
    await page.evaluate(() => { window.failAppLaunch = true; });
    await page.locator('[data-value="com.webos.app.smartshare"]').click();
    await page.waitForSelector('#notice', {state: 'visible'});
    assert.match(await page.locator('#notice').innerText(), /Could not open/);
    assert.equal(await page.locator('.app-grid').count(), 1, 'failed launch leaves app list usable');
    await page.evaluate(() => { window.failAppLaunch = false; });
    await page.screenshot({path: path.join(shots, 'apps-error-en.png')});
    await page.keyboard.press('Escape');
    await page.waitForSelector('.home');
    await page.locator('[data-action=inputs]').click();
    await page.locator('[data-value=HDMI_2]').click();
    await page.waitForSelector('#screen button', {state: 'detached'});
    assert.equal(await page.locator('button').count(), 0);
    assert.equal(await page.evaluate(() => window.originalVideo === document.getElementById('input-video')), true, 'internal navigation retains video element');
    await page.keyboard.press('Enter');
    assert.match(await page.locator('.bar').getAttribute('aria-label'), /HDMI 2/);
    await page.screenshot({path: path.join(shots, 'bar-en.png')});
    const buttons = await page.locator('button').all();
    for (const button of buttons) {
      const bounds = await button.boundingBox();
      assert.ok(bounds.x >= 0 && bounds.y >= 0 && bounds.x + bounds.width <= 1920 && bounds.y + bounds.height <= 1080, 'button fits screen');
    }
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('button').count(), 0, 'Back dismisses bar without closing app');
    await page.reload(); await page.waitForSelector('#input-video source', {state: 'attached'});
    await page.keyboard.press('Enter');
    assert.match(await page.locator('.bar').getAttribute('aria-label'), /HDMI 2.*Giza/);
    await page.locator('[data-action=input]').click();
    assert.equal(await page.locator('button').count(), 0, 'clicking Hide does not reopen the dock');
    await page.locator('#screen').click({position: {x: 960, y: 540}});
    await page.waitForSelector('.bar');
    await page.locator('[data-action=settings]').click();
    await page.locator('[data-action=section][data-value=prayer]').click();
    await page.locator('[data-action=offsets]').click();
    await page.locator('[data-action=offset-0]').click();
    await page.keyboard.press('ArrowRight');
    await page.keyboard.press('ArrowRight');
    await page.keyboard.press('Enter');
    await page.waitForSelector('[data-action=offset-0]');
    assert.match(await page.locator('[data-action=offset-0]').innerText(), /2 min/);
    await page.locator('[data-action=offset-0]').click();
    await page.keyboard.press('ArrowRight');
    await page.keyboard.press('Escape');
    assert.match(await page.locator('[data-action=offset-0]').innerText(), /2 min/, 'Back cancels an unsaved stepper draft');
    await page.keyboard.press('Escape');
    await page.locator('[data-action=prayer-day]').click();
    assert.equal(await page.locator('[data-action=bell]').count(), 5, 'no sunrise bell');
    await page.locator('[data-action=bell][data-value="0"]').click();
    await page.waitForFunction(() => document.querySelector('[data-action=bell][data-value="0"]').textContent === 'Off');
    await page.screenshot({path: path.join(shots, 'prayer-day-en.png')});
    await page.locator('[data-action=tests]').click();
    await page.locator('[data-action=section][data-value=reminders]').click();
    await page.locator('[data-action=voice]').click();
    assert.equal(await page.locator('.voice-name').count(), 14, 'every Android adhan voice is in one scrollable list');
    assert.equal(await page.locator('.voice-preview').count(), 14, 'each voice has a preview control');
    await page.locator('[data-action=preview-voice][data-value=makkah]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.match(await page.locator('audio').getAttribute('src'), /makkah\.mp3$/);
    assert.equal(await page.locator('.voice-list').count(), 1, 'preview stays in the voice list');
    const voiceScroll = await page.locator('.voice-list').evaluate(el => el.scrollTop);
    assert.ok(voiceScroll > 0, 'last voice is reachable by scrolling');
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await page.waitForSelector('#input-video source', {state: 'attached'});
    assert.equal(await page.locator('.voice-list').evaluate(el => el.scrollTop), voiceScroll, 'preview completion preserves list position');
    await page.screenshot({path: path.join(shots, 'voices-en.png')});
    await page.keyboard.press('Escape');
    await page.locator('[data-action=section][data-value=tests]').click();
    const loadsBefore = await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO').length);
    await page.locator('[data-action=test-banner]').click();
    await page.waitForSelector('.banner-overlay .alert-box');
    assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO').length), loadsBefore, 'silent banner never pauses, unloads or reloads HDMI');
    assert.equal(await page.locator('#input-video source').getAttribute('src'), 'ext://hdmi:2');
    await page.screenshot({path: path.join(shots, 'banner-en.png')});
    await page.locator('[data-action=dismiss-alert]').click();
    assert.equal(await page.locator('.bar').count(), 0, 'pointer dismiss must not open the bar underneath');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'test-banner', 'banner returns to the test that opened it');
    async function tests() {
      if (!(await page.locator('.settings-page').count())) {
        if (!(await page.locator('[data-action=settings]').count())) { await page.keyboard.press('Enter'); }
        await page.locator('[data-action=settings]').click();
      }
      await page.locator('[data-action=section][data-value=tests]').click();
    }
    await tests();
    await page.locator('[data-action=test-card]').click();
    assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO').length), loadsBefore, 'silent card preserves HDMI');
    await page.keyboard.press('Escape');
    await tests();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.match(await page.locator('audio').getAttribute('src'), /chebae_fajr\.mp3$/);
    assert.equal(await page.locator('#input-video source').count(), 0, 'HDMI decoder released before MP3');
    await page.screenshot({path: path.join(shots, 'adhan-test-en.png')});
    assert.equal(await page.locator('.call-frame').count(), 1, 'adhan uses the Android mihrab layout');
    assert.equal(await page.locator('.call-verse').innerText(), 'حَيَّ عَلَى الصَّلَاةِ');
    await page.locator('[data-action=toggle-adhan]').click();
    assert.equal(await page.locator('[data-action=toggle-adhan]').innerText(), 'Play');
    assert.equal(await page.locator('.call-frame').count(), 1, 'Stop preserves the call screen for replay');
    await page.locator('[data-action=toggle-adhan]').click();
    assert.equal(await page.locator('[data-action=toggle-adhan]').innerText(), 'Stop');
    await page.keyboard.press('Escape');
    await page.waitForSelector('#input-video source', {state: 'attached'});
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'test-adhan', 'adhan returns to the original test row');
    assert.equal(await page.locator('audio').count(), 0, 'audio source removed before input returns');
    assert.equal(await page.locator('#input-video source').getAttribute('src'), 'ext://hdmi:2');
    await tests();
    await page.locator('[data-action=test-spoken]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.match(await page.locator('audio').getAttribute('src'), /before_fajr\.mp3$/);
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await page.waitForSelector('#input-video source', {state: 'attached'});
    await tests();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    await page.evaluate(() => {
      const audio = document.querySelector('audio');
      Object.defineProperty(audio, 'error', {value: {code: 4}}); audio.dispatchEvent(new Event('error'));
    });
    await page.waitForSelector('#input-video source', {state: 'attached'});
    assert.equal(await page.locator('audio').count(), 0, 'media failure releases decoder and restores input');
    await tests();
    await page.locator('[data-action=section][data-value=appearance]').click();
    await page.waitForSelector('#notice', {state: 'hidden'});
    assert.equal(await page.locator('.appearance-preview svg').count(), 1);
    assert.equal((await page.locator('.appearance-preview svg').boundingBox()).height, 396, 'larger preview stays within the reserved panel');
    const carousel = key => page.locator('[data-action=carousel][data-value=' + key + ']');
    await carousel('theme').focus();
    for (let i = 0; i < 19; i++) await page.keyboard.press('ArrowRight');
    assert.equal(await carousel('theme').locator('.carousel-choice').innerText(), 'Mosaic', 'remote reaches all 20 themes inline');
    assert.match(await page.locator('.appearance-preview').innerText(), /Mosaic/);
    await page.keyboard.press('ArrowRight');
    assert.equal(await carousel('theme').locator('.carousel-choice').innerText(), 'Mosaic', 'carousel clamps at the end');
    assert.equal(await page.locator('#theme-style').getAttribute('href'), 'style.css', 'preview does not save the draft');
    await page.screenshot({path: path.join(shots, 'themes-en.png')});
    await page.keyboard.press('ArrowDown');
    assert.equal(await page.locator(':focus').getAttribute('data-value'), 'background', 'Down moves between carousel rows');
    await page.keyboard.press('Escape');
    assert.equal(await carousel('theme').locator('.carousel-choice').innerText(), 'Mihrab', 'Back discards the unsaved appearance draft');
    await page.locator('[data-action=carousel-step][data-value="theme:1"]').click();
    assert.notEqual(await carousel('theme').locator('.carousel-choice').innerText(), 'Mihrab', 'pointer arrow updates the preview inline');
    await page.locator('[data-action=cancel-appearance]').click();
    assert.equal(await carousel('theme').locator('.carousel-choice').innerText(), 'Mihrab');
    const forestIndex = await page.evaluate(() => window.AfnonData.themes.indexOf('forest'));
    await carousel('theme').focus();
    for (let i = 0; i < forestIndex; i++) await page.keyboard.press('ArrowRight');
    await carousel('background').focus(); await page.keyboard.press('ArrowRight');
    assert.ok(await page.evaluate(() => new Promise(resolve => {
      const pattern = new Image(); pattern.onload = () => resolve(pattern.naturalWidth > 0); pattern.onerror = () => resolve(false);
      pattern.src = document.querySelector('.appearance-preview image').getAttribute('xlink:href');
    })), 'preview pattern asset decodes');
    await carousel('homeLayout').focus(); await page.keyboard.press('ArrowRight');
    await carousel('homeBar').focus(); await page.keyboard.press('ArrowRight');
    await carousel('prayerMode').focus();
    for (let i = 0; i < 3; i++) await page.keyboard.press('ArrowRight');
    await carousel('theme').focus();
    await page.screenshot({path: path.join(shots, 'appearance-en.png')});
    await page.locator('[data-action=apply-appearance]').click();
    await page.waitForFunction(() => document.getElementById('theme-style').getAttribute('href') === 'theme-forest.css');
    assert.equal(await page.locator('.settings-page').evaluate(el => getComputedStyle(el).backgroundImage), 'none', 'Home patterns do not leak onto Settings');
    assert.equal(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs.prayerMode), 'both');
    await page.waitForSelector('[data-action=home]');
    await page.locator('[data-action=home]').click();
    assert.match(await page.locator('.home').evaluate(el => getComputedStyle(el).backgroundImage), /eight_point_stars/, 'saved pattern still appears on Home');
    await homeControls();
    await page.screenshot({path: path.join(shots, 'home-compact-en.png')});
    const dateBox = await page.locator('[data-gregorian]').boundingBox(), compactArch = await page.locator('.arch').boundingBox();
    assert.ok(dateBox.y + dateBox.height < compactArch.y, 'both dates fit above the compact countdown');
    assert.equal(compactArch.y, 916, 'compact countdown can be placed at the bottom');
    assert.ok(await page.locator('.home-prayers .prayer:visible').count() <= 2, 'current/next mode filters the prayer strip');
    if (await page.locator('.home-prayers .prayer.current').count()) {
      assert.match(await page.locator('.home-prayers .prayer').first().getAttribute('class'), /current/, 'current prayer precedes next, including after Isha');
    }
    for (const button of await page.locator('#screen button').all()) {
      const b = await button.boundingBox(); assert.ok(b.x >= 0 && b.y >= 0 && b.x + b.width <= 1920 && b.y + b.height <= 1080, 'compact Home controls fit');
    }
    await page.locator('[data-action=radio]').click(); await page.locator('[data-action=radio-play]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.match(await page.locator('audio').getAttribute('src'), /^https:\/\//, 'radio uses the shared station URL');
    await page.locator('[data-action=input]').click(); await page.waitForSelector('#input-video source', {state: 'attached'});
    assert.equal(await page.locator('audio').count(), 0);
    await page.reload(); await page.waitForSelector('#input-video source', {state: 'attached'});
    const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs);
    assert.equal(saved.offsets[0], 2); assert.equal(saved.bells[0], false); assert.equal(saved.theme, 'forest');
    assert.equal(saved.automatic, false, 'test buttons do not enable real alarms');
    await tests();
    await page.locator('[data-action=section][data-value=display]').click();
    await page.locator('[data-action=language]').click();
    await page.waitForFunction(() => document.documentElement.dir === 'rtl');
    await page.locator('[data-action=section][data-value=tests]').click();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    await page.screenshot({path: path.join(shots, 'adhan-test-ar.png')});
    await page.keyboard.press('Escape');
    await page.waitForSelector('#input-video source', {state: 'attached'});
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'test-adhan');
    await page.locator('[data-action=section][data-value=appearance]').click();
    await carousel('theme').focus();
    await page.keyboard.press('ArrowLeft');
    const rtlTheme = await carousel('theme').locator('.carousel-choice').innerText();
    await page.keyboard.press('ArrowRight');
    assert.notEqual(await carousel('theme').locator('.carousel-choice').innerText(), rtlTheme, 'Arabic carousel arrows follow RTL');
    await page.screenshot({path: path.join(shots, 'themes-ar.png')});
    await page.keyboard.press('Escape');
    await page.locator('[data-action=section][data-value=display]').click();
    await page.locator('[data-action=quickLabels]').click();
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('fixture')).prefs.quickLabels);
    await page.keyboard.press('Escape'); await page.keyboard.press('Escape');
    for (const lang of ['ar', 'en']) {
      for (const layout of ['mihrab', 'compact', 'hidden']) {
        for (const position of layout === 'compact' ? ['top', 'bottom'] : ['top']) {
          await page.evaluate(({lang, layout, position}) => {
            const fixture = JSON.parse(localStorage.getItem('fixture'));
            Object.assign(fixture.prefs, {lang, homeLayout: layout, homeBar: position});
            localStorage.setItem('fixture', JSON.stringify(fixture));
          }, {lang, layout, position});
          await page.reload(); await page.waitForSelector('#input-video source', {state: 'attached'});
          await page.keyboard.press('Enter'); await page.locator('[data-action=home]').click();
          for (const label of await page.locator('.quick-caption').all()) {
            assert.ok(await label.isVisible(), 'quick labels stay visible without focus');
            assert.ok(await label.evaluate(el => { const a = el.getBoundingClientRect(), b = el.parentNode.getBoundingClientRect(); return a.left >= b.left && a.right <= b.right && a.top >= b.top && a.bottom <= b.bottom; }), 'quick label fits its button');
          }
          for (const control of await page.locator('.home button').all()) {
            const b = await control.boundingBox(); assert.ok(b.y >= 0 && b.y + b.height <= 1080, 'labeled Home fits the screen');
          }
          const prayer = await page.locator('.home-prayers').boundingBox(), quick = await page.locator('.home-quick button').first().boundingBox();
          assert.ok(quick.y + quick.height < prayer.y, 'labeled buttons stay above prayer times: ' + [lang, layout, position, quick.y, prayer.y].join('/'));
          if (layout === 'compact') {
            const arch = await page.locator('.arch').boundingBox(), links = await page.locator('.home-links').boundingBox();
            assert.ok(arch.y > links.y + links.height || arch.y + arch.height < prayer.y, 'countdown stays clear of prayer times and app controls');
          }
          await page.screenshot({path: path.join(shots, 'labels-' + lang + '-' + layout + '-' + position + '.png')});
        }
      }
    }
    async function scheduled(mode, external) {
      await page.evaluate(({mode, external}) => {
        const state = JSON.parse(localStorage.getItem('fixture'));
        state.pendingAlert = {id: 'scheduled:' + Date.now(), mode, prayer: 0, test: true, background: true, returnToPrevious: external};
        localStorage.setItem('fixture', JSON.stringify(state));
        window.mediaCalls = [];
        document.dispatchEvent(new CustomEvent('webOSRelaunch', {detail: {alertId: state.pendingAlert.id}}));
      }, {mode, external});
      await page.waitForSelector('#overlay button');
    }
    await tests();
    await scheduled('adhan', false);
    await page.waitForSelector('audio', {state: 'attached'});
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await page.waitForSelector('[data-action=test-adhan]');
    assert.equal(await page.evaluate(() => window.closeCount), 0, 'scheduled adhan in foreground preserves Settings');
    for (const mode of ['adhan', 'banner']) {
      await scheduled(mode, true);
      if (mode === 'adhan') {
        await page.waitForSelector('audio', {state: 'attached'});
        await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
      } else { await page.locator('[data-action=dismiss-alert]').click(); }
      await page.waitForFunction(expected => window.closeCount === expected, mode === 'adhan' ? 1 : 2);
      await page.waitForTimeout(550);
      assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO' && c.op === 'play').length), 0, 'external alert closes without starting HDMI again');
    }
    await page.evaluate(() => {
      const state = JSON.parse(localStorage.getItem('fixture'));
      state.pendingAlert = {id: 'cold:1', mode: 'adhan', prayer: 0, test: true, returnToPrevious: false};
      localStorage.setItem('fixture', JSON.stringify(state));
    });
    await page.reload();
    await page.waitForSelector('#overlay button');
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await page.waitForFunction(() => window.closeCount === 1);
    await page.evaluate(() => {
      const state = JSON.parse(localStorage.getItem('fixture')); state.expiredAlert = true;
      localStorage.setItem('fixture', JSON.stringify(state));
    });
    await page.reload(); await page.waitForSelector('#input-video source', {state: 'attached'});
    await tests();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForSelector('#overlay button');
    await page.locator('[data-action=dismiss-alert]').click();
    assert.equal(await page.evaluate(() => window.closeCount), 0, 'expired cold-start alerts do not make later in-app tests exit');
    assert.equal(await page.locator('.settings-page').count(), 1);
    assert.deepEqual(errors, []);
    console.log('PASS: Arabic/English controls, grouped settings, offsets and cancel, prayer bells, overlays preserving HDMI, audio stop/end/error recovery, radio, theme/layout and persistence. Decoder and Luna boundaries are mocked; actual LG tests remain separate.');
    console.log('Preview screenshots: ' + shots);
  } finally { if (browser) await browser.close(); server.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
