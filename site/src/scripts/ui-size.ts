import { uiFactor, uiPercent } from '../lib/ui-scale';
import type { Prefs } from '../lib/data';

export function sizeUi(stage: HTMLElement, prefs: Prefs) {
  const value = String(uiPercent(prefs.uiScale));
  if (stage.dataset.uiSize === value) return;
  stage.dataset.uiSize = value;
  stage.style.setProperty('--ui-size', String(uiFactor(prefs)));
}

/** Fit page groups without changing their spacing or maximum focus ring. */
export function fitUi(stage: HTMLElement) {
  if (stage.dataset.screen === 'screensaver') return;
  const content = stage.querySelector<HTMLElement>('.tv-ui');
  if (!content) return;
  const nodes = content.querySelectorAll<HTMLElement>(
    ':scope > :not([data-settings]), [data-settings] > :not(.side):not(.rows), .side > .head, .side .nav, .rows.scrolling, .rows:not(.scrolling) > .srow');
  for (const node of nodes) node.style.removeProperty('translate');
  const factor = Number(stage.dataset.uiSize) / 100;
  if (!(factor > 1)) return;
  const viewport = stage.getBoundingClientRect(), k = viewport.width / 1920, scale = k * factor;
  for (const node of nodes) {
    if (node.matches('.chooser, .menu, .picker, .screen-off, .scrim, .online-check') || !node.getClientRects().length) continue;
    const box = node.getBoundingClientRect();
    if (!box.width || !box.height) continue;
    let left = box.left, top = box.top, right = box.right, bottom = box.bottom;
    const focus = node.matches('.ft') ? [node] : node.matches('.scrolling') ? [] : [...node.querySelectorAll<HTMLElement>('.ft')];
    for (const control of focus) {
      if (!control.getClientRects().length) continue;
      const bounds = control.getBoundingClientRect(), style = getComputedStyle(control);
      const kind = control.matches('.srow') ? '--focus-scale-settings-row' : control.matches('.row') ? '--focus-scale-row' : '--focus-scale';
      const growth = Math.max(1, parseFloat(style.getPropertyValue(kind)) || 1);
      const ring = (parseFloat(style.getPropertyValue('--focus-ring-offset')) + parseFloat(style.getPropertyValue('--focus-ring-width'))) * scale;
      const width = control.offsetWidth * scale * growth / 2 + ring, height = control.offsetHeight * scale * growth / 2 + ring;
      const x = (bounds.left + bounds.right) / 2, y = (bounds.top + bounds.bottom) / 2;
      left = Math.min(left, x - width); right = Math.max(right, x + width);
      top = Math.min(top, y - height); bottom = Math.max(bottom, y + height);
    }
    const edge = 12 * k;
    const dx = right - left <= viewport.width - 2 * edge ? Math.max(viewport.left + edge - left, Math.min(0, viewport.right - edge - right)) : 0;
    const dy = bottom - top <= viewport.height - 2 * edge ? Math.max(viewport.top + edge - top, Math.min(0, viewport.bottom - edge - bottom)) : 0;
    if (dx || dy) node.style.translate = `${dx / scale}px ${dy / scale}px`;
  }
}
