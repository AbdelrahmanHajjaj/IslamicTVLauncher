// Browser side: prefs saved in this browser, and a once-a-second loop that re-renders the screen model into
// the page's [data-k] (innerHTML) and [data-a] (attributes) elements.
import { appearance } from './appearance';
import { cycleAppearance } from '../lib/appearance-cycle';
import { DEFAULT_PREFS, type Prefs } from '../lib/data';
import { makeCtx, type Ctx, type Model } from '../lib/ctx';
import { shiftFor } from '../lib/prayer';
import type { Lang } from '../lib/text';
import { uiPercent } from '../lib/ui-scale';
import { sizeUi, fitUi } from './ui-size';

const KEY = 'afnon-prefs';

export function loadPrefs(): Prefs {
  try {
    const prefs = { ...DEFAULT_PREFS, ...JSON.parse(localStorage.getItem(KEY) ?? '{}') };
    return { ...prefs, uiScale: uiPercent(prefs.uiScale) };
  } catch {
    return { ...DEFAULT_PREFS, uiScale: uiPercent(undefined) };
  }
}

export function savePrefs(p: Prefs) {
  if (p.uiScale !== undefined && uiPercent(p.uiScale) !== p.uiScale) throw new RangeError('Invalid interface size');
  try { localStorage.setItem(KEY, JSON.stringify(p)); } catch { /* private mode: this visit only */ }
}

export function bind(root: ParentNode, m: Model) {
  for (const el of root.querySelectorAll<HTMLElement>('[data-k]')) {
    const v = m.html[el.dataset.k!];
    if (v !== undefined && el.innerHTML !== v) el.innerHTML = v;
  }
  for (const el of root.querySelectorAll<HTMLElement>('[data-a]')) {
    const attrs = m.attr?.[el.dataset.a!];
    if (attrs) for (const [k, v] of Object.entries(attrs)) if (el.getAttribute(k) !== v) el.setAttribute(k, v);
  }
}

/**
 * Runs `model` every second for this page's language and test mode (`data-mode` on the stage), the clock
 * shifted as the app's test intents do. Returns a function that re-renders at once (after a prefs change).
 */
export function live(model: (c: Ctx) => Model, onTick?: (c: Ctx) => void) {
  const stage = document.querySelector<HTMLElement>('.tv')!;
  const lang = document.documentElement.lang as Lang;
  const mode = stage.dataset.mode || undefined;
  let prefs = loadPrefs();
  let shift = mode ? shiftFor(mode, prefs) : 0;
  // New rows and opened panels fit before paint; the fitter's style writes are not observed.
  new MutationObserver(() => fitUi(stage)).observe(stage.querySelector('.tv-ui')!, { childList: true, subtree: true, attributes: true, attributeFilter: ['hidden'] });
  document.fonts.ready.then(() => fitUi(stage));
  const render = () => {
    const c = makeCtx(lang, prefs, new Date(Date.now() + shift), mode);
    sizeUi(stage, prefs);
    appearance(stage, prefs);
    bind(stage, model(c));
    onTick?.(c);
    fitUi(stage);
    // Keep draft settings, prayer reminders and quick menus steady. No extra timer or reload.
    if (stage.dataset.screen === 'home' && !document.hidden && !stage.querySelector('[data-card]:not([hidden]), [data-radio-card]:not([hidden]), [data-quick]:not([hidden]), [data-screen-off-view]:not([hidden])')) {
      const next = cycleAppearance(prefs, Date.now());
      if (next !== prefs) { prefs = next; savePrefs(prefs); appearance(stage, prefs); }
    }
  };
  render();
  setInterval(render, 1000);
  return (p?: Prefs) => {
    if (p) { prefs = p; savePrefs(p); shift = mode ? shiftFor(mode, prefs) : 0; }
    render();
  };
}
