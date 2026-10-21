// The TV stage: scaled to its frame, driven by the arrow keys like the D-pad (WICG spatial navigation
// polyfill, contained in the stage), Enter as OK, Escape/Backspace as Back. Hover focuses, like moving the remote.
// The key hints along the bottom follow focus: each focusable (or its container) carries data-hints (JSON).
import 'spatial-navigation-polyfill';

const frame = document.querySelector<HTMLElement>('.tv-frame');
const stage = document.querySelector<HTMLElement>('.tv');

if (frame && stage) {
  const fit = () => stage.style.setProperty('--k', String(frame.clientWidth / 1920));
  new ResizeObserver(fit).observe(frame);
  fit();

  const visible = (e: HTMLElement) => e.getClientRects().length > 0;
  const start = () => stage.querySelector<HTMLElement>('[data-focus-start]')?.focus({ preventScroll: true });

  const hintsOut = stage.querySelector<HTMLElement>('[data-hints-out]');
  const showHints = (from: Element | null) => {
    if (!hintsOut) return;
    const src = from?.closest<HTMLElement>('[data-hints]') ?? stage.querySelector<HTMLElement>('[data-focus-start]');
    const list: string[] = src?.dataset.hints ? JSON.parse(src.dataset.hints) : [];
    hintsOut.replaceChildren(...list.map((h) => Object.assign(document.createElement('span'), { textContent: h })));
  };
  stage.addEventListener('focusin', (e) => showHints(e.target as Element));
  showHints(null);

  // Arrow keys: the first press anywhere on the page wakes the TV at its default focus; after that they move
  // focus inside the TV (the polyfill's navigate(), its own key handling off) and never scroll the page.
  const DIRS: Record<string, 'left' | 'right' | 'up' | 'down'> = { ArrowLeft: 'left', ArrowRight: 'right', ArrowUp: 'up', ArrowDown: 'down' };
  const spatnav = (window as any).__spatialNavigation__;
  if (spatnav) spatnav.keyMode = 'NONE';
  window.addEventListener('keydown', (e) => {
    const dir = DIRS[e.key];
    if (!dir || e.altKey || e.ctrlKey || e.metaKey || e.shiftKey) return;
    const inside = stage.contains(document.activeElement);
    if (!inside) {
      if (document.activeElement && document.activeElement !== document.body) return; // focus is elsewhere on the page
      e.preventDefault();
      start();
      return;
    }
    e.preventDefault();
    // An explicit target wins (data-nav-up=".radio-pill"), as the app's per-screen focus map.
    const key = 'nav' + dir[0].toUpperCase() + dir.slice(1);
    const sel = (document.activeElement as HTMLElement).dataset[key];
    const target = sel ? stage.querySelector<HTMLElement>(sel) : null;
    if (target && visible(target)) target.focus({ preventScroll: true });
    else {
      const from = document.activeElement as HTMLElement;
      (window as any).navigate?.(dir);
      // Nothing that way inside the TV: stay put rather than leave for the page's links.
      if (!stage.contains(document.activeElement)) from.focus({ preventScroll: true });
    }
  });

  stage.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' || e.key === 'Backspace') {
      const close = [...stage.querySelectorAll<HTMLElement>('[data-back]')].find((b) => visible(b) || visible(b.parentElement!))
        ?? stage.querySelector<HTMLElement>('[data-home]');
      if (close) { e.preventDefault(); close.click(); }
    }
  });

  stage.addEventListener('pointerover', (e) => {
    const t = (e.target as HTMLElement).closest<HTMLElement>('a[href], button, [tabindex]');
    if (t && stage.contains(t) && document.activeElement !== t && t.tabIndex >= 0) t.focus({ preventScroll: true });
  });

  // Arriving from another screen of the TV (links end in #tv): focus lands where the app puts it.
  if (location.hash === '#tv' && !stage.contains(document.activeElement)) start();
}
