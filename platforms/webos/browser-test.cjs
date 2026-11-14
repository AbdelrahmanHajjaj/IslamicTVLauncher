const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const { chromium } = require('playwright');
const base = path.join(__dirname, 'build/app');
const sacred = JSON.parse(fs.readFileSync(path.join(__dirname, '../../shared/data/sacred-text.json')));
const uiGaps = JSON.parse(fs.readFileSync(path.join(__dirname, 'ui-gaps.json')));
const uiChoiceGaps = JSON.parse(fs.readFileSync(path.join(__dirname, 'ui-choice-gaps.json')));
const shots = path.resolve(__dirname, '../../state/webos-port');
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
      window.volumeCalls = [];
      window.onlineCalls = [];
      window.closeCount = 0; window.close = () => { window.closeCount++; };
      HTMLMediaElement.prototype.load = function () { window.mediaCalls.push({op: 'load', tag: this.tagName, src: this.getAttribute('src'), children: this.children.length}); };
      HTMLMediaElement.prototype.pause = function () { window.mediaCalls.push({op: 'pause', tag: this.tagName}); };
      HTMLMediaElement.prototype.play = function () {
        window.mediaCalls.push({op: 'play', tag: this.tagName, src: this.getAttribute('src'), masterVolume: window.mockMasterVolume === undefined ? 50 : window.mockMasterVolume, elementVolume: this.volume});
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
          if (method === 'onlinePrayer') {
            window.onlineCalls.push(p);
            if (p.action === 'check') {
              const finish = () => {
                reply.returnValue = !window.failOnline;
                const prefs = window.Afnon.prefs(state.prefs), times = window.Afnon.day(new Date(), prefs).times;
                const differences = [0, 1, -1, 1, 0, 0];
                reply.comparison = {date: '12-11-2026', timezone: 'Africa/Cairo', calculated: times,
                  reference: times.map((t, i) => t + differences[i] * 60000), differences};
                this.onservicecallback(JSON.stringify(reply));
              };
              if (window.deferOnline) (window.onlineReplies ||= []).push(finish);
              else setTimeout(finish, 5);
              return;
            }
          }
          if (method === 'audioVolume') {
            window.volumeCalls.push(p);
            const finish = () => {
              reply.mode = window.legacyVolume ? 'tv' : 'element';
              if (window.failVolume && p.action === 'prepare') {
                reply.returnValue = false; reply.errorText = 'Fixture volume failure';
              } else if (window.legacyVolume) {
                const owner = window.mockVolumeOwner;
                if (p.action === 'prepare') {
                  const original = owner && window.mockMasterVolume === owner.applied ? owner.original : window.mockMasterVolume;
                  const applied = state.prefs.volume;
                  window.mockMasterVolume = applied; window.mockVolumeOwner = {id: p.id, original, applied};
                } else if (p.action === 'release' && owner && owner.id === p.id) {
                  if (window.mockMasterVolume === owner.applied) window.mockMasterVolume = owner.original;
                  window.mockVolumeOwner = null;
                }
              }
              this.onservicecallback(JSON.stringify(reply));
            };
            if (window.deferVolume && p.action === 'prepare') { (window.volumeReplies ||= []).push(finish); }
            else setTimeout(finish, 5);
            return;
          }
          if (method === 'takeAlert') { reply.alert = state.pendingAlert; if (state.expiredAlert) { reply.returnValue = false; } }
          if (method === 'launchApp' && window.failAppLaunch) { reply.returnValue = false; reply.errorText = 'App unavailable'; }
          setTimeout(() => this.onservicecallback(JSON.stringify(reply)), 5);
        };
      };
    });
    async function inputRestored() {
      await page.waitForFunction(() => {
        const source = document.querySelector('#input-video source');
        return source && !source.type.includes('mediaOption=') && !document.querySelector('audio');
      });
    }
    const errors = []; page.on('pageerror', e => errors.push(e.message));
    await page.goto('http://127.0.0.1:' + server.address().port);
    assert.match(await page.title(), /^Islamic TV Launcher/);
    await page.evaluate(() => document.fonts.ready);
    await inputRestored();
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
      assert.equal(await page.locator('.verse').innerHTML(), sacred.prayerVerse.join('<br>'), 'verse DOM keeps exact Arabic text and line breaks even when the layout hides it');
      assert.deepEqual(await page.locator('.home-quick button').evaluateAll(items => items.map(el => el.dataset.action)), ['prayer-day', 'inputs', 'radio', 'tv-settings', 'settings']);
      assert.equal(await page.locator('.home-apps [data-action=native-app]').count(), 3, 'large Home tiles belong to TV apps');
      const clock = await page.locator('.home-clock .clock').boundingBox();
      for (const control of await page.locator('.home-quick button').all()) {
        const b = await control.boundingBox();
        assert.ok(b.y < 150 && b.width <= 72 && b.height <= 72, 'quick controls are compact top icons');
        assert.ok(b.x >= clock.x + clock.width || b.x + b.width <= clock.x, 'top icons do not cover the clock');
        assert.ok(await control.getAttribute('aria-label'), 'icon has an accessible name');
      }
      await homeNavigation();
    }
    async function homeNavigation() {
      const apps = page.locator('.home-apps button');
      await page.evaluate(() => {
        const api = window['tv.afnon:core'].tv.afnon.core.HomeNavigation;
        const hook = window.homeContractHook = {api, original: api.move, calls: 0};
        api.move = function (...args) { hook.calls++; return hook.original.apply(api, args); };
      });
      for (let i = 0; i < await apps.count(); i++) {
        await apps.nth(i).focus();
        await page.keyboard.press('ArrowDown');
        assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps', 'Down from the last app row selects All apps');
        await page.keyboard.press('ArrowUp');
        assert.equal(await page.locator(':focus').getAttribute('data-value'), await apps.nth(i).getAttribute('data-value'), 'Up restores the last Home app');
        await page.keyboard.press('ArrowUp');
        assert.equal(await page.locator(':focus').getAttribute('data-action'), ['radio', 'tv-settings', 'settings'][i], 'Up enters the Android top-controls map');
      }
      const calls = await page.evaluate(() => {
        const hook = window.homeContractHook;
        hook.api.move = hook.original; delete window.homeContractHook;
        return hook.calls;
      });
      assert.equal(calls, await apps.count() * 3, 'every Home arrow uses the shared contract exactly once');
      await apps.first().focus();
      await page.keyboard.press('ArrowDown');
      await page.keyboard.press('Enter');
      await page.waitForSelector('.app-grid');
      await appsNavigation();
      await page.keyboard.press('Escape');
      await page.waitForSelector('.home');
      assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps', 'Back from All apps restores its Home focus');
      await apps.first().focus();
    }
    async function appsNavigation() {
      const tiles = page.locator('.app-grid .app-tile');
      const rtl = await page.locator('html').getAttribute('dir') === 'rtl';
      const end = rtl ? 'ArrowLeft' : 'ArrowRight', start = rtl ? 'ArrowRight' : 'ArrowLeft';
      await page.evaluate(() => {
        const api = window['tv.afnon:core'].tv.afnon.core.AllAppsNavigation;
        const hook = window.appsContractHook = {api, original: api.move, calls: 0};
        api.move = function (...args) { hook.calls++; return hook.original.apply(api, args); };
      });
      for (let i = 0; i < Math.min(5, await tiles.count()); i++) {
        await tiles.nth(i).focus();
        await page.keyboard.press('ArrowUp');
        assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps-back', 'Up from every first-row column selects Back');
      }
      for (const [key, actions] of [[end, ['prayer-day', 'settings', 'apps-back']], [start, ['settings', 'prayer-day', 'apps-back']]]) {
        for (const action of actions) {
          await page.keyboard.press(key);
          assert.equal(await page.locator(':focus').getAttribute('data-action'), action, 'horizontal movement stays in the circular Apps header');
        }
      }
      for (const [action, index] of [['apps-back', 0], ['prayer-day', 4], ['settings', 4]]) {
        await page.locator(`[data-action=${action}]`).focus();
        await page.keyboard.press('ArrowDown');
        assert.ok(await tiles.nth(index).evaluate(el => el === document.activeElement), 'header Down returns to the grid');
      }
      await tiles.nth(5).focus();
      await page.keyboard.press('ArrowUp');
      assert.ok(await tiles.first().evaluate(el => el === document.activeElement), 'Up from a lower row stays in app content');
      await tiles.nth(14).focus();
      await page.keyboard.press('ArrowDown');
      assert.ok(await tiles.nth(15).evaluate(el => el === document.activeElement), 'Down clamps to the partial final row');
      await page.keyboard.press('ArrowDown');
      assert.ok(await tiles.nth(15).evaluate(el => el === document.activeElement), 'Down stays at the final row');
      await page.keyboard.press(start);
      assert.ok(await tiles.nth(15).evaluate(el => el === document.activeElement), 'horizontal grid movement stays within its row');
      const calls = await page.evaluate(() => {
        const hook = window.appsContractHook;
        hook.api.move = hook.original; delete window.appsContractHook;
        return hook.calls;
      });
      assert.equal(calls, Math.min(5, await tiles.count()) + 13, 'every All apps arrow uses the shared contract exactly once');
    }
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'native-app', 'Home starts in app content');
    await homeControls();
    await page.locator('[data-action=prayer-day]').focus();
    await page.keyboard.press('ArrowLeft');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'inputs', 'RTL remote moves along the small top row');
    await page.keyboard.press('ArrowDown');
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'native-app', 'Down reaches app tiles');
    await page.screenshot({path: path.join(shots, 'home-ar.png')});
    const arCount = await page.locator('.prayer').count(); assert.equal(arCount, 6);
    await page.locator('[data-action=settings]').click();
    async function settingsCoverage() {
      const seen = new Set();
      const offered = {};
      async function collect(section) {
        const bound = await page.locator('.settings-rows button:not([data-action=carousel-step])').evaluateAll(items => items.map(el => ({
          id: el.getAttribute('data-ui'), kind: el.getAttribute('data-ui-kind'), label: el.firstChild.textContent,
          action: el.getAttribute('data-action'), values: JSON.parse(el.getAttribute('data-ui-values'))
        })));
        assert.ok(bound.length);
        assert.equal(new Set(bound.map(item => item.id)).size, bound.length, 'no duplicate control in an actual section');
        for (const item of bound) {
          assert.ok(item.id, 'every actual settings row has a contract ID: ' + item.action);
          const expected = await page.evaluate(({id, kind, section}) => {
            const control = window['tv.afnon:core'].tv.afnon.core.UiContract.bind(id, kind);
            if (control.section !== section) throw new Error('Wrong control section: ' + id);
            const [key, index] = control.labelKey.split(':');
            const label = window.AfnonData.strings[key][document.documentElement.lang];
            return index === undefined ? label : label[Number(index)];
          }, {...item, section});
          assert.equal(item.label, expected, 'actual row uses its canonical bilingual label');
          seen.add(item.id);
          if (item.kind === 'choice') {
            assert.ok(Array.isArray(item.values), 'actual choice binding exposes values');
            offered[item.id] ||= new Set();
            item.values.forEach(value => offered[item.id].add(value));
          }
        }
      }
      for (const section of ['prayer', 'reminders', 'radio', 'display', 'appearance', 'device', 'tests']) {
        await page.locator('[data-action=section][data-value=' + section + ']').click();
        await collect(section);
      }
      await page.locator('[data-action=section][data-value=appearance]').click();
      const countdown = page.locator('[data-action=carousel][data-value=homeLayout]');
      const rtl = await page.locator('html').getAttribute('dir') === 'rtl';
      await countdown.focus();
      await page.keyboard.press(rtl ? 'ArrowRight' : 'ArrowLeft');
      await page.keyboard.press(rtl ? 'ArrowRight' : 'ArrowLeft');
      for (let mode = 0; mode < 3; mode++) {
        await collect('appearance');
        if (mode < 2) await page.keyboard.press(rtl ? 'ArrowLeft' : 'ArrowRight');
      }
      await page.locator('[data-action=cancel-appearance]').click();
      await page.locator('[data-action=section][data-value=prayer]').click();
      await page.locator('[data-action=offsets]').click();
      await collect('offsets');
      await page.locator('[data-action=section][data-value=prayer]').click();
      await page.locator('[data-action=prayer-day]').click();
      const prayerBindings = await page.locator('[data-action=bell]').evaluateAll(items => items.map(el => ({id: el.getAttribute('data-ui'), kind: el.getAttribute('data-ui-kind')})));
      assert.equal(prayerBindings.length, 5);
      assert.equal(new Set(prayerBindings.map(c => c.id)).size, 5);
      for (const control of prayerBindings) { assert.equal(control.kind, 'toggle'); seen.add(control.id); }
      const bellsBefore = await page.evaluate(() => window.Afnon.prefs(JSON.parse(localStorage.getItem('fixture')).prefs).bells);
      for (const index of [0, 2, 3, 4, 5]) {
        const bell = page.locator('[data-action=bell][data-value="' + index + '"]');
        for (const value of [!bellsBefore[index], bellsBefore[index]]) {
          const previous = await bell.elementHandle();
          await bell.click();
          // The mock persists before replying; wait for the real save callback to redraw.
          await page.waitForFunction(el => !el.isConnected, previous);
          await previous.dispose();
          assert.equal(await page.evaluate(index => JSON.parse(localStorage.getItem('fixture')).prefs.bells[index], index), value);
        }
      }
      assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs.bells), bellsBefore);
      await page.keyboard.press('Escape');
      await page.waitForSelector('.settings-page');
      assert.equal(await page.locator(':focus').getAttribute('data-ui'), 'prayer.today', 'Back restores the originating setting');
      await page.evaluate(({bound, gaps}) => window['tv.afnon:core'].tv.afnon.core.UiContract.validateCoverage(bound, gaps),
        {bound: [...seen], gaps: uiGaps.map(gap => gap.id)});
      await page.evaluate(({offered, gaps}) => {
        const core = window['tv.afnon:core'].tv.afnon.core.UiContract;
        for (const [id, values] of Object.entries(offered)) core.validateChoiceCoverage(id, values, gaps.find(g => g.id === id)?.values || []);
      }, {offered: Object.fromEntries(Object.entries(offered).map(([id, values]) => [id, [...values]])), gaps: uiChoiceGaps});
      const rejected = await page.evaluate(({bound, gaps}) => {
        try { window['tv.afnon:core'].tv.afnon.core.UiContract.validateCoverage(bound.slice(1), gaps); return false; }
        catch (error) { return /Missing UI control/.test(error.message); }
      }, {bound: [...seen], gaps: uiGaps.map(gap => gap.id)});
      assert.ok(rejected, 'losing a real binding fails coverage');
      await page.locator('[data-action=section][data-value=prayer]').click();
    }
    async function settingsValuesFit() {
      for (const [section, actions] of [['reminders', ['automatic', 'remindVoice']], ['tests', ['testPrayer', 'test-banner', 'test-card']]]) {
        await page.locator('[data-action=section][data-value=' + section + ']').click();
        for (const action of actions) {
          const bounds = await page.locator('[data-action=' + action + '] .value').evaluate(el => {
            const range = document.createRange();
            range.selectNodeContents(el);
            const text = range.getBoundingClientRect();
            let fits = true;
            // Old WebKit paints Arabic outside normal font metrics; check every clipping ancestor.
            for (let parent = el; parent; parent = parent.parentElement) {
              const style = getComputedStyle(parent), box = parent.getBoundingClientRect();
              if (style.overflowX !== 'visible') fits &&= text.left >= box.left + 1 && text.right <= box.right - 1;
              if (style.overflowY !== 'visible') fits &&= text.top >= box.top + 1 && text.bottom <= box.bottom - 1;
            }
            return {label: el.textContent, fits};
          });
          assert.ok(bounds.fits, 'settings value glyphs have paint clearance inside their clipping box: ' + bounds.label);
        }
        const wraps = await page.locator('[data-action=' + actions[0] + '] .value').evaluate(el => {
          const original = el.textContent;
          el.textContent = (original + ' ').repeat(24);
          const value = el.getBoundingClientRect(), button = el.parentElement.getBoundingClientRect();
          const result = {wrapped: value.height > parseFloat(getComputedStyle(el).fontSize) * 2,
            contained: value.left >= button.left && value.right <= button.right && value.top >= button.top && value.bottom <= button.bottom};
          el.textContent = original;
          return result;
        });
        assert.ok(wraps.wrapped && wraps.contained, 'long settings values wrap inside their row');
      }
    }
    async function onlineChecks() {
      await page.locator('[data-action=section][data-value=prayer]').click();
      const before = await page.evaluate(() => ({prefs: JSON.parse(localStorage.getItem('fixture')).prefs,
        source: document.getElementById('input-video').innerHTML, media: window.mediaCalls.length}));
      await page.locator('[data-action=online-prayer]').click();
      await page.waitForSelector('.online-check tbody tr');
      assert.equal(await page.locator('.online-check tbody tr').count(), 6);
      assert.equal(await page.locator(':focus').getAttribute('data-action'), 'online-back');
      const back = await page.locator('[data-action=online-back]').boundingBox(), brand = await page.locator('.online-check .brand').boundingBox();
      assert.ok(back.x + back.width < brand.x || brand.x + brand.width < back.x, 'Back and the brand have separate header space');
      for (const cell of await page.locator('.online-check th, .online-check td, .online-note, [data-action=online-retry]').all()) {
        const box = await cell.boundingBox();
        assert.ok(box.x >= 0 && box.y >= 0 && box.x + box.width <= 1920 && box.y + box.height <= 1080, 'online comparison fits screen');
      }
      const lang = await page.locator('html').getAttribute('lang');
      await page.screenshot({path: path.join(shots, 'online-' + lang + '.png')});
      assert.deepEqual(await page.evaluate(() => ({prefs: JSON.parse(localStorage.getItem('fixture')).prefs,
        source: document.getElementById('input-video').innerHTML, media: window.mediaCalls.length})), before, 'manual comparison preserves settings and HDMI');
      await page.keyboard.press('Escape');
      assert.equal(await page.locator(':focus').getAttribute('data-ui'), 'prayer.online');
      await page.evaluate(() => { window.failOnline = true; });
      await page.locator('[data-action=online-prayer]').click();
      await page.waitForSelector('[data-action=online-retry]');
      assert.equal(await page.locator('.online-check [role=status]').innerText(), await page.evaluate(() => window.AfnonData.strings.onlineUnavailable[document.documentElement.lang]));
      await page.evaluate(() => { window.failOnline = false; });
      await page.locator('[data-action=online-retry]').click(); await page.waitForSelector('.online-check tbody tr');
      await page.keyboard.press('Escape');
      await page.evaluate(() => { window.deferOnline = true; });
      await page.locator('[data-action=online-prayer]').click();
      await page.waitForFunction(() => window.onlineReplies?.length === 1);
      await page.keyboard.press('Escape');
      await page.evaluate(() => window.onlineReplies.shift()());
      assert.equal(await page.locator('.online-check').count(), 0, 'late result cannot reopen the comparison');
      assert.equal(await page.locator(':focus').getAttribute('data-ui'), 'prayer.online');
      assert.equal(await page.evaluate(() => window.onlineCalls.at(-1).action), 'cancel');
      await page.locator('[data-action=online-prayer]').click();
      await page.waitForFunction(() => window.onlineReplies?.length === 1);
      await page.evaluate(() => {
        const state = JSON.parse(localStorage.getItem('fixture'));
        state.pendingAlert = {id: 'online:banner:' + Date.now(), mode: 'banner', prayer: 0, test: true};
        localStorage.setItem('fixture', JSON.stringify(state));
        document.dispatchEvent(new CustomEvent('webOSRelaunch', {detail: {alertId: state.pendingAlert.id}}));
      });
      await page.waitForSelector('#overlay [data-action=dismiss-alert]');
      await page.evaluate(() => window.onlineReplies.shift()());
      assert.ok(await page.evaluate(() => document.getElementById('overlay').contains(document.activeElement)), 'late response preserves alert focus');
      await page.locator('#overlay [data-action=dismiss-alert]').click();
      await page.waitForSelector('.online-check [data-action=online-retry]');
      await page.keyboard.press('Escape');
      await page.evaluate(() => {
        const state = JSON.parse(localStorage.getItem('fixture')); delete state.pendingAlert;
        localStorage.setItem('fixture', JSON.stringify(state));
      });
      await page.locator('[data-action=online-prayer]').click();
      await page.waitForFunction(() => window.onlineReplies?.length === 1);
      await page.evaluate(() => {
        Object.defineProperty(document, 'hidden', {configurable: true, value: true}); document.dispatchEvent(new Event('visibilitychange'));
        Object.defineProperty(document, 'hidden', {configurable: true, value: false}); document.dispatchEvent(new Event('visibilitychange'));
        window.onlineReplies.shift()();
      });
      await page.waitForSelector('[data-action=online-retry]');
      assert.equal(await page.locator('.online-check tbody tr').count(), 0, 'suspend cancels and keeps a retryable view');
      await page.keyboard.press('Escape');
      await page.evaluate(() => { window.deferOnline = false; });
      assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs), before.prefs);
    }
    await settingsValuesFit();
    await settingsCoverage();
    await onlineChecks();
    await page.locator('[data-action=section][data-value=prayer]').click();
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
    await settingsValuesFit();
    await settingsCoverage();
    await onlineChecks();
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
    await page.reload(); await inputRestored();
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
    const numericBefore = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs);
    const numberCases = [
      ['prayer', 'hijriOffset', -2, 2, 1, numericBefore.hijriOffset],
      ['reminders', 'volume', 0, 100, 5, numericBefore.volume],
      ...numericBefore.offsets.map((value, i) => ['offsets', 'offset-' + i, -30, 30, 1, value])
    ];
    async function numberArrows(code, times) {
      await page.evaluate(({code, times}) => {
        for (let i = 0; i < times; i++) document.dispatchEvent(new KeyboardEvent('keydown', {keyCode: code, bubbles: true}));
      }, {code, times});
    }
    for (const [section, action, low, high, step, original] of numberCases) {
      await page.locator('[data-action=section][data-value=' + (section === 'offsets' ? 'prayer' : section) + ']').click();
      if (section === 'offsets') await page.locator('[data-action=offsets]').click();
      const row = page.locator('[data-action=' + action + ']');
      for (const [code, expected] of [[37, low], [39, high]]) {
        await row.click();
        await numberArrows(code, 100);
        assert.equal(await page.locator('.stepper strong').innerText(), expected + (action === 'volume' ? '%' : ''), 'real editor clamps ' + action);
        await page.keyboard.press('Enter');
        await row.waitFor();
      }
      await row.click(); await numberArrows(37, 100); await page.keyboard.press('Escape');
      await row.click();
      assert.equal(await page.locator('.stepper strong').innerText(), high + (action === 'volume' ? '%' : ''), 'Back preserves saved amount');
      assert.ok(Number.isInteger((high - original) / step));
      await numberArrows(37, (high - original) / step);
      await page.keyboard.press('Enter'); await row.waitFor();
    }
    assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs), numericBefore, 'all numeric save/cancel round trips restore original preferences');
    await page.locator('[data-action=section][data-value=prayer]').click();
    await page.locator('[data-action=offsets]').click();
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
    await inputRestored();
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
    assert.equal(await page.locator('#input-video source').count(), 1, 'adhan retains HDMI while audio plays');
    assert.equal(await page.locator('#input-video source').getAttribute('src'), 'ext://hdmi:2', 'adhan retains the selected HDMI picture');
    assert.deepEqual(await page.locator('#input-video source').evaluate(el => JSON.parse(decodeURI(el.type.split(';mediaOption=')[1]))),
      {option: {mediaFormat: {type: 'video_only'}}}, 'adhan suppresses programme audio while keeping HDMI video');
    await page.screenshot({path: path.join(shots, 'adhan-test-en.png')});
    assert.equal(await page.locator('.call-frame').count(), 1, 'adhan uses the Android mihrab layout');
    assert.equal(await page.locator('#overlay').evaluate(el => getComputedStyle(el).backgroundColor), 'rgba(0, 0, 0, 0)', 'only the adhan mihrab has a background');
    assert.equal(await page.locator('#screen').evaluate(el => getComputedStyle(el).visibility), 'hidden', 'adhan hides the originating page outside its mihrab');
    assert.equal(await page.locator('.call-verse').innerText(), 'حَيَّ عَلَى الصَّلَاةِ');
    await page.locator('[data-action=toggle-adhan]').click();
    assert.equal(await page.locator('[data-action=toggle-adhan]').innerText(), 'Play');
    assert.equal(await page.locator('.call-frame').count(), 1, 'Stop preserves the call screen for replay');
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => document.querySelector('[data-action=toggle-adhan]').textContent === 'Stop');
    await page.keyboard.press('Escape');
    await inputRestored();
    assert.equal(await page.locator(':focus').getAttribute('data-action'), 'test-adhan', 'adhan returns to the original test row');
    assert.equal(await page.locator('#screen').evaluate(el => getComputedStyle(el).visibility), 'visible', 'dismissing adhan restores the originating page');
    assert.equal(await page.locator('audio').count(), 0, 'audio source removed before input returns');
    assert.equal(await page.locator('#input-video source').getAttribute('src'), 'ext://hdmi:2');
    await tests();
    await page.locator('[data-action=test-spoken]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.equal(await page.locator('#input-video source').count(), 0, 'spoken playback retains the existing decoder handoff');
    assert.match(await page.locator('audio').getAttribute('src'), /before_fajr\.mp3$/);
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await inputRestored();
    await tests();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    await page.evaluate(() => {
      const audio = document.querySelector('audio');
      Object.defineProperty(audio, 'error', {value: {code: 4}}); audio.dispatchEvent(new Event('error'));
    });
    await inputRestored();
    assert.equal(await page.locator('audio').count(), 0, 'media failure releases decoder and restores input');
    const hdmiFixture = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')));
    for (const input of ['DTV', 'AV_1']) {
      await page.evaluate(input => {
        const state = JSON.parse(localStorage.getItem('fixture')); state.input = input;
        localStorage.setItem('fixture', JSON.stringify(state));
      }, input);
      await page.reload(); await inputRestored(); await tests();
      await page.locator('[data-action=test-adhan]').click();
      await page.waitForFunction(() => document.querySelector('audio')?.getAttribute('src'));
      assert.equal(await page.locator('#input-video source').count(), 0, input + ' keeps the existing audio decoder handoff');
      await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
      await inputRestored();
      assert.equal(await page.locator('#input-video source').getAttribute('type'), await page.evaluate(input => window.Afnon.input(input).type, input));
    }
    await page.evaluate(state => localStorage.setItem('fixture', JSON.stringify(state)), hdmiFixture);
    await page.reload(); await inputRestored(); await tests();
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
    for (const layout of ['mihrab', 'compact', 'hidden']) {
      const svg = page.locator('.appearance-model svg');
      assert.equal(await svg.locator('pattern, image').count(), 0, 'old WebKit preview must not clip controls through an SVG image paint server');
      assert.match(await svg.evaluate(el => getComputedStyle(el).backgroundImage), /eight_point_stars/, 'preview retains the selected CSS pattern');
      assert.deepEqual(await svg.locator('rect').evaluateAll(nodes => nodes.flatMap(el => {
        const box = el.getBBox();
        return box.x < 0 || box.y < 0 || box.x + box.width > 1920 || box.y + box.height > 1080 ? [el.outerHTML] : [];
      })), [], 'patterned ' + layout + ' preview keeps all controls in its viewport');
      if (layout !== 'hidden') { await carousel('homeLayout').focus(); await page.keyboard.press('ArrowRight'); }
    }
    await carousel('homeLayout').focus(); await page.keyboard.press('ArrowLeft'); await page.keyboard.press('ArrowLeft');
    assert.ok(await page.evaluate(() => new Promise(resolve => {
      const pattern = new Image(); pattern.onload = () => resolve(pattern.naturalWidth > 0); pattern.onerror = () => resolve(false);
      pattern.src = /url\(["']?([^"')]+)["']?\)/.exec(getComputedStyle(document.querySelector('.appearance-model svg')).backgroundImage)[1];
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
    assert.equal(await page.locator('#input-video source').count(), 0, 'radio retains the existing decoder handoff');
    await page.waitForFunction(() => document.querySelector('audio') && document.querySelector('audio').getAttribute('src'));
    assert.match(await page.locator('audio').getAttribute('src'), /^https:\/\//, 'radio uses the shared station URL');
    await page.locator('[data-action=input]').click(); await inputRestored();
    assert.equal(await page.locator('audio').count(), 0);
    await page.reload(); await inputRestored();
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
    assert.equal(await page.locator('#overlay').evaluate(el => getComputedStyle(el).backgroundColor), 'rgba(0, 0, 0, 0)', 'Arabic adhan keeps the same transparent outer area');
    assert.equal(await page.locator('#screen').evaluate(el => getComputedStyle(el).visibility), 'hidden', 'Arabic adhan hides the originating page');
    await page.screenshot({path: path.join(shots, 'adhan-test-ar.png')});
    await page.keyboard.press('Escape');
    await inputRestored();
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
          await page.reload(); await inputRestored();
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
          await homeNavigation();
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
      if (mode === 'adhan') assert.match(await page.locator('#input-video source').getAttribute('type'), /mediaOption=/, 'background adhan also retains HDMI video');
      await page.evaluate(() => { window.mediaCalls = []; });
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
    await page.reload(); await inputRestored();
    await tests();
    await page.locator('[data-action=test-adhan]').click();
    await page.waitForSelector('#overlay button');
    await page.locator('[data-action=dismiss-alert]').click();
    assert.equal(await page.evaluate(() => window.closeCount), 0, 'expired cold-start alerts do not make later in-app tests exit');
    assert.equal(await page.locator('.settings-page').count(), 1);
    await page.evaluate(() => {
      const state = JSON.parse(localStorage.getItem('fixture')); state.prefs.volume = 15;
      localStorage.setItem('fixture', JSON.stringify(state));
    });
    await page.reload(); await inputRestored();
    await page.evaluate(() => { window.legacyVolume = true; window.mockMasterVolume = 50; window.mediaCalls = []; });
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => window.mediaCalls.some(c => c.op === 'play' && c.tag === 'AUDIO'));
    const legacyPlayback = await page.evaluate(() => window.mediaCalls.find(c => c.op === 'play' && c.tag === 'AUDIO'));
    assert.equal(legacyPlayback.masterVolume, 15, 'old LG applies 15/100 independently of the captured TV level before adhan playback');
    assert.equal(legacyPlayback.elementVolume, 1, 'native volume fallback does not apply attenuation twice');
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 50);
    assert.equal(await page.locator('.call-frame').count(), 1, 'Stop restores volume while keeping the replay screen');
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('[data-action=toggle-adhan]').textContent === 'Stop');
    const owners = await page.evaluate(() => window.volumeCalls.filter(call => call.action === 'prepare').map(call => call.id));
    assert.equal(new Set(owners).size, owners.length, 'each replay has a distinct volume owner');
    await page.keyboard.press('Escape');
    await page.waitForFunction(() => window.mockMasterVolume === 50);
    await inputRestored();
    await page.evaluate(() => { window.mediaCalls = []; });
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15);
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 50);
    await page.evaluate(() => { window.deferVolume = true; });
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => window.volumeReplies && window.volumeReplies.length === 1);
    await page.waitForTimeout(700); // Cross the original 450 ms decoder-release delay while replay preparation is pending.
    assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.op === 'play' && c.tag === 'AUDIO').length), 0, 'an old play timer cannot bypass replay volume preparation');
    await page.evaluate(() => { window.deferVolume = false; window.volumeReplies.shift()(); });
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('[data-action=toggle-adhan]').textContent === 'Stop');
    await page.keyboard.press('Escape');
    await page.waitForFunction(() => window.mockMasterVolume === 50);
    await inputRestored();
    await page.evaluate(() => { window.failVolume = true; window.mediaCalls = []; });
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await inputRestored();
    assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.op === 'play' && c.tag === 'AUDIO').length), 0, 'preparation failure never falls back to loud playback');
    assert.equal(await page.evaluate(() => window.mockMasterVolume), 50);
    await page.evaluate(() => { window.failVolume = false; window.deferVolume = true; window.mediaCalls = []; });
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => window.volumeReplies && window.volumeReplies.length === 1);
    await page.keyboard.press('Escape');
    await page.evaluate(() => { window.deferVolume = false; window.volumeReplies.shift()(); });
    await page.waitForFunction(() => window.mockMasterVolume === 50 && !document.querySelector('audio'));
    assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.op === 'play' && c.tag === 'AUDIO').length), 0, 'cancelled preparation cannot start audio after its reply arrives');
    await inputRestored();
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('audio').getAttribute('src'));
    await page.evaluate(() => { window.mockMasterVolume = 22; });
    await page.keyboard.press('Escape');
    await inputRestored();
    assert.equal(await page.evaluate(() => window.mockMasterVolume), 22, 'manual remote level survives dismissal');
    await page.evaluate(() => { window.mockMasterVolume = 50; });
    await tests(); await page.locator('[data-action=test-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('audio').getAttribute('src'));
    await page.evaluate(() => {
      Object.defineProperty(document, 'hidden', {configurable: true, value: true});
      document.dispatchEvent(new Event('visibilitychange'));
    });
    await page.waitForFunction(() => window.mockMasterVolume === 50 && !document.querySelector('audio'));
    await page.evaluate(() => {
      Object.defineProperty(document, 'hidden', {configurable: true, value: false});
      document.dispatchEvent(new Event('visibilitychange'));
    });
    await inputRestored();
    await page.evaluate(() => {
      const state = JSON.parse(localStorage.getItem('fixture')); state.prefs.callClose = 1;
      localStorage.setItem('fixture', JSON.stringify(state));
    });
    await page.reload(); await inputRestored();
    await page.evaluate(() => { window.legacyVolume = true; window.mockMasterVolume = 50; });
    await tests(); await page.locator('[data-action=test-full]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('audio').getAttribute('src'));
    await page.evaluate(() => document.querySelector('audio').dispatchEvent(new Event('ended')));
    await page.waitForFunction(() => window.mockMasterVolume === 50);
    assert.equal(await page.locator('.call-frame').count(), 1, 'completed full adhan restores level while keeping its timed call screen');
    await page.locator('[data-action=toggle-adhan]').click();
    await page.waitForFunction(() => window.mockMasterVolume === 15 && document.querySelector('[data-action=toggle-adhan]').textContent === 'Stop');
    await page.evaluate(() => {
      const audio = document.querySelector('audio');
      Object.defineProperty(audio, 'error', {value: {code: 4}}); audio.dispatchEvent(new Event('error'));
    });
    await inputRestored();
    assert.equal(await page.evaluate(() => window.mockMasterVolume), 50, 'native volume also restores after decoder failure');
    assert.deepEqual(errors, []);
    async function fits(selector) {
      await page.locator(selector).first().waitFor({state:'visible'});
      assert.deepEqual(await page.evaluate(() => {
        const screen = document.getElementById('screen'), overlay = document.getElementById('overlay');
        return [document.documentElement.scrollWidth, document.documentElement.scrollHeight, screen.scrollLeft, screen.scrollTop, overlay.scrollLeft, overlay.scrollTop];
      }), [1920,1080,0,0,0,0], 'UI zoom must not enlarge or scroll the root viewport');
      const centre = await page.evaluate(() => {
        const panel = document.getElementById('screen').firstElementChild;
        if (!panel || !panel.getClientRects().length) return null;
        const box = panel.getBoundingClientRect(); return (box.left + box.right) / 2;
      });
      assert.ok(centre === null || Math.abs(centre - 960) < 0.5, 'scaled content stays horizontally centred in its physical viewport');
      const items = await page.locator(selector).all();
      assert.ok(items.length, 'expected visible controls: ' + selector);
      for (const item of items) {
        await item.evaluate(el => { if (el.matches('button')) { el.focus(); document.getElementById('screen').scrollLeft = document.getElementById('screen').scrollTop = 0; document.getElementById('overlay').scrollLeft = document.getElementById('overlay').scrollTop = 0; } else if (document.getElementById('stage').getAttribute('data-large-ui') === 'true') el.scrollIntoView({block:'center', inline:'nearest'}); });
        const b = await item.boundingBox();
        assert.ok(b && b.x >= -0.1 && b.y >= -0.1 && b.x + b.width <= 1920.1 && b.y + b.height <= 1080.1, selector + ' stays on screen: ' + JSON.stringify(b));
        const ring = await item.evaluate(el => {
          if (document.activeElement !== el) return 0;
          const css = getComputedStyle(el), scale = JSON.parse(localStorage.getItem('fixture')).prefs.uiScale / 100;
          return (parseFloat(css.outlineWidth) + parseFloat(css.outlineOffset)) * scale;
        });
        assert.ok(b.x - ring >= -0.5 && b.y - ring >= -0.5 && b.x + b.width + ring <= 1920.5 && b.y + b.height + ring <= 1080.5, 'focused outline stays on screen: ' + selector + ' ' + JSON.stringify({box:b,ring}));
      }
    }
    for (const lang of ['ar', 'en']) for (const size of [150, 80, 90, 100, 110, 120, 130, 140]) {
      await page.evaluate(lang => {
        localStorage.setItem('fixture', JSON.stringify({prefs: window.Afnon.prefs({lang, volume: 30, dockAutoHide: 0}), input: 'HDMI_2', startup: {enabled: true}}));
      }, lang);
      await page.reload(); await inputRestored();
      await page.evaluate(() => document.fonts.ready);
      await page.keyboard.press('Enter'); await page.locator('[data-action=settings]').click();
      await page.locator('[data-action=section][data-value=display]').click();
      const beforeSize = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')));
      await page.locator('[data-ui="display.size"]').click();
      assert.equal(await page.locator('.settings-page').count(), 1, 'size editing stays in the settings row');
      assert.equal(await page.locator('.picker,.stepper').count(), 0, 'no size editor page');
      await page.keyboard.press(lang === 'ar' ? 'ArrowLeft' : 'ArrowRight');
      assert.equal(await page.locator('[data-ui="display.size"] strong').innerText(), lang === 'ar' ? '١١٠%' : '110%');
      await page.keyboard.press('Escape');
      assert.equal(await page.locator(':focus').getAttribute('data-ui'), 'display.size', 'cancel size picker restores its row');
      assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs), beforeSize.prefs, 'Back does not save');
      await page.locator('[data-ui="display.size"]').click();
      await page.locator('[data-ui-scale-step="1"]').click();
      await page.locator('[data-action=settings-back]').click();
      assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs), beforeSize.prefs, 'leaving cancels the inline draft');
      await page.locator('[data-action=settings]').click();
      await page.locator('[data-action=section][data-value=display]').click();
      await page.locator('[data-ui="display.size"]').click();
      for (let i = 0; i < 4; i++) await page.locator('[data-ui-scale-step="-1"]').click();
      assert.equal(await page.locator('[data-ui="display.size"] strong').innerText(), lang === 'ar' ? '٨٠%' : '80%');
      for (let i = 0; i < 9; i++) await page.locator('[data-ui-scale-step="1"]').click();
      assert.equal(await page.locator('[data-ui="display.size"] strong').innerText(), lang === 'ar' ? '١٥٠%' : '150%');
      for (let i = 150; i > size; i -= 10) await page.locator('[data-ui-scale-step="-1"]').click();
      await page.keyboard.press('Enter');
      await page.waitForSelector('[data-ui="display.size"]:not(.editing)');
      assert.equal(await page.locator(':focus').getAttribute('data-ui'), 'display.size', 'saving size restores its row');
      const saved = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')));
      assert.deepEqual(saved.prefs, {...beforeSize.prefs, uiScale: size}, 'only global UI size changes');
      assert.deepEqual(saved.startup, beforeSize.startup); assert.equal(saved.input, beforeSize.input);
      await fits('[data-ui="display.size"],.settings-page .round-back,.settings-nav button');
      if (size === 80 || size === 110 || size === 150) await page.screenshot({path:path.join(shots, 'size-' + lang + '-' + size + '-settings.png')});
      await page.locator('[data-action=settings-back]').click();
      await fits('.home-quick button,.home-apps button,.home-links button,.verse,.city-name');
      assert.deepEqual(await page.locator('.arch .verse,.arch .verse-ref,.arch .prayer-name,.arch .prayer-time,.arch .countdown,.arch .city-name').evaluateAll(nodes => {
        const bad = [];
        for (const el of nodes) {
          const box = el.getBoundingClientRect(), text = document.createRange(); text.selectNodeContents(el);
          for (const ink of text.getClientRects()) if (ink.left < box.left - 1 || ink.right > box.right + 1) bad.push(el.className);
        }
        return bad;
      }), [], 'narrowed mihrab keeps its text inside');
      const artwork = await page.locator('.home .arch').boundingBox();
      const safeEdge = size <= 110 ? 31.5 : 0;
      assert.ok(artwork.x >= 31.5 && artwork.x + artwork.width <= 1920 - 31.5, 'Home mihrab retains horizontal TV-safe clearance');
      if (size <= 110) assert.ok(artwork.y >= safeEdge && artwork.y + artwork.height <= 1080 - safeEdge, 'fixed Home mihrab retains vertical TV-safe clearance');
      const rememberedApp = page.locator('.home-apps .app-tile').nth(1);
      await rememberedApp.focus(); await page.keyboard.press('ArrowUp');
      assert.equal(await page.locator('.home-quick button:focus').count(), 1, 'Up reaches the visible top controls');
      await page.keyboard.press('ArrowDown');
      assert.equal(await rememberedApp.evaluate(el => el === document.activeElement), true, 'Down returns to the same app');
      await page.keyboard.press('ArrowDown');
      assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps', 'Down reaches visible All apps');
      await fits('.home-links [data-action=apps]');
      assert.ok(Math.abs((await page.locator('.home-quick button').first().boundingBox()).width - 64 * size / 100) < 0.2, 'controls scale with the setting');
      if (size === 80 || size === 110 || size === 150) await page.screenshot({path:path.join(shots, 'size-' + lang + '-' + size + '-home.png')});
      await page.locator('.home-links [data-action=apps]').click();
      await fits('.apps-heading,.apps-tools');
      await page.locator('.app-tile').nth(3).focus(); await page.keyboard.press('ArrowUp');
      assert.equal(await page.locator(':focus').getAttribute('data-action'), 'apps-back');
      await page.keyboard.press(lang === 'ar' ? 'ArrowLeft' : 'ArrowRight');
      assert.equal(await page.locator(':focus').getAttribute('data-action'), 'prayer-day');
      await page.keyboard.press('Enter'); await fits('.day-row,.day-row button,.day-details button');
      await page.keyboard.press('Escape'); await page.locator('[data-action=settings]').click();
      await page.locator('[data-action=section][data-value=prayer]').click();
      await page.locator('[data-action=method]').click(); await fits('.picker button'); await page.keyboard.press('Escape');
      await page.locator('[data-action=offsets]').click(); await page.locator('[data-action=offset-0]').click();
      await fits('.stepper strong,.stepper button'); await page.keyboard.press('Escape');
      await page.locator('[data-action=settings-back]').click();
      await page.locator('[data-action=online-prayer]').click(); await page.waitForSelector('.online-check table');
      await fits('.online-check .header,.online-check table,.online-note,[data-action=online-retry]');
      await page.locator('[data-action=online-back]').click();
      await page.locator('[data-action=section][data-value=appearance]').click();
      await fits('.appearance-preview,.settings-rows,.carousel-main,.carousel-arrow');
      await page.locator('[data-action=section][data-value=reminders]').click();
      await page.locator('[data-action=voice]').click(); await fits('.voice-list,.voice-footer button');
      await page.keyboard.press('Escape');
      await page.locator('[data-action=section][data-value=display]').click();
      await page.locator('[data-action=clock-view]').click(); await fits('.clock-page button'); await page.keyboard.press('Escape');
      await tests(); const videoCalls = await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO').length);
      await page.locator('[data-action=test-banner]').click(); await fits('.banner-box,.banner-box button');
      const banner = await page.locator('.banner-box').boundingBox();
      assert.ok(Math.abs(banner.x - 64) < 0.2 && Math.abs(banner.y - 36) < 0.2 && Math.abs(banner.width - 1792) < 0.2, 'banner retains screen insets');
      assert.equal(await page.evaluate(() => window.mediaCalls.filter(c => c.tag === 'VIDEO').length), videoCalls, 'size does not disrupt HDMI behind a silent banner');
      await page.keyboard.press('Escape'); await tests();
      await page.locator('[data-action=test-card]').click(); await fits('.card-overlay .alert-box,.card-overlay button');
      await page.keyboard.press('Escape'); await tests();
      await page.locator('[data-action=test-adhan]').click(); await page.waitForFunction(() => document.querySelector('audio')?.getAttribute('src'));
      await fits('.call-column button,.call-verse,.call-name,.call-time,.call-status');
      assert.equal(await page.locator('.call-verse').innerText(), sacred.adhanCall);
      if (size === 80 || size === 110 || size === 150) await page.screenshot({path:path.join(shots, 'size-' + lang + '-' + size + '-call.png')});
      await page.keyboard.press('Escape'); await inputRestored();
      await tests(); await page.locator('[data-action=settings-back]').click();
      if (await page.locator('[data-action=apps-back]').count()) { await page.locator('[data-action=apps-back]').click(); }
      await page.locator('.home-quick [data-action=inputs]').click();
      await page.locator('[data-action=refresh]:focus').waitFor(); await fits('.picker button'); await page.keyboard.press('Escape');
      await page.locator('.home-quick [data-action=radio]').click(); await fits('.page button'); await page.keyboard.press('Escape');
      for (const position of ['top','bottom']) {
        if (!(await page.locator('[data-action=settings]').count())) { await page.keyboard.press('Enter'); }
        await page.locator('[data-action=settings]').click(); await page.locator('[data-action=section][data-value=display]').click();
        await page.locator('[data-action=dockPosition]').click(); await page.locator('[data-action=choose][data-value=' + position + ']').click();
        await page.locator('[data-action=bar]').click(); await fits('.bar,.bar button,.bar .prayer');
        const bar = await page.locator('.bar').boundingBox();
        assert.ok(Math.abs(bar.x) < 0.2 && Math.abs(bar.width - 1920) < 0.2, 'prayer bar fits full width');
        assert.ok(Math.abs(position === 'top' ? bar.y : bar.y + bar.height - 1080) < 0.2, 'bar remains attached to selected edge');
        if (size === 80 || size === 110 || size === 150) await page.screenshot({path:path.join(shots, 'size-' + lang + '-' + size + '-' + position + '-bar.png')});
        await page.keyboard.press('Escape');
      }
      const video = await page.locator('#input-video').boundingBox();
      assert.deepEqual(video,{x:0,y:0,width:1920,height:1080},'HDMI video never scales with UI');
      const beforeReload = await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs);
      await page.reload(); await inputRestored(); await page.keyboard.press('Enter');
      assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('fixture')).prefs), beforeReload, 'reopening preserves the saved size and other preferences');
      assert.ok(Math.abs((await page.locator('.bar .icon-button').first().boundingBox()).width - 72 * size / 100) < 0.2, 'saved scale is applied after reopening');
      await page.keyboard.press('Escape');
      console.log('PASS: global size',lang,size,'picker/save/cancel, Home/Apps/header/Today/online/settings/pickers/appearance/voices/clock/inputs/radio/alerts, top/bottom bar, unchanged HDMI');
    }
    for (const lang of ['ar', 'en']) for (const variant of [
      {homeLayout:'mihrab',archSide:'left'}, {homeLayout:'mihrab',archSide:'right'},
      {homeLayout:'compact',homeBar:'top'}, {homeLayout:'compact',homeBar:'bottom'}, {homeLayout:'hidden'}
    ]) {
      await page.evaluate(({lang,variant}) => {
        const fixture = JSON.parse(localStorage.getItem('fixture'));
        Object.assign(fixture.prefs, {lang,uiScale:150,quickLabels:true}, variant);
        localStorage.setItem('fixture',JSON.stringify(fixture));
      }, {lang,variant});
      await page.reload(); await inputRestored();
      await page.keyboard.press('Enter'); await page.locator('[data-action=home]').click();
      await fits('.home-quick button,.home-apps button,.home-links button');
      const quick = await page.locator('.home-quick').boundingBox(), prayer = await page.locator('.home-prayers').boundingBox();
      const apps = await page.locator('.home-actions').boundingBox();
      const clock = await page.locator('.home-clock').boundingBox();
      const intersects = (a,b) => Math.min(a.x+a.width,b.x+b.width)>Math.max(a.x,b.x)+1 && Math.min(a.y+a.height,b.y+b.height)>Math.max(a.y,b.y)+1;
      assert.ok(!intersects(clock,quick), 'large toolbar uses the space beside the clock');
      if (variant.homeLayout === 'mihrab') assert.ok(!intersects(await page.locator('.arch').boundingBox(),quick), 'large toolbar clears the mihrab');
      assert.deepEqual(await page.locator('.home-prayers .prayer').evaluateAll(nodes=>{
        const bad=[];
        for(const el of nodes){const box=el.getBoundingClientRect(),range=document.createRange();range.selectNodeContents(el);for(const ink of range.getClientRects())if(ink.left<box.left-1||ink.right>box.right+1||ink.top<box.top-1||ink.bottom>box.bottom+1){bad.push(el.textContent);break;}}
        return bad;
      }), [], 'larger Home prayer text fits inside its cells');
      assert.ok(quick.y + quick.height < prayer.y, 'large labelled controls clear prayer times');
      assert.ok(prayer.y + prayer.height < apps.y, 'large prayer times clear apps');
      if (variant.homeLayout === 'compact') {
        const arch = await page.locator('.arch').boundingBox();
        assert.ok(arch.y + arch.height < prayer.y || arch.y > apps.y + apps.height, 'large compact countdown clears prayer/app content');
      }
      await page.screenshot({path:path.join(shots,'size-'+lang+'-150-home-'+variant.homeLayout+'-'+(variant.archSide||variant.homeBar||'none')+'.png')});
    }
    assert.deepEqual(errors, []);
    console.log('PASS: Arabic/English controls, grouped settings, offsets and cancel, prayer bells, overlays preserving HDMI, audio stop/end/error recovery, radio, theme/layout, persistence and global sizing. Decoder and Luna boundaries are mocked; actual LG tests remain separate.');
    console.log('Preview screenshots: ' + shots);
  } finally { if (browser) await browser.close(); server.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
