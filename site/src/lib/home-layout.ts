import tokens from '../../../afnon-mihrab/tokens.json';
import type { Prefs } from './data';

export const HOME = Object.fromEntries(Object.entries(tokens.layout.home).map(([k, v]) => [k, parseFloat(v.$value)])) as Record<keyof typeof tokens.layout.home, number>;
const margin = parseFloat(tokens.space['safe-inline'].$value);
export const homeChoice = (n: number | undefined, max: number) => Number.isInteger(n) ? Math.max(0, Math.min(max, n!)) : 0;
export function homePrayers(table: number | undefined, current: number, next: number): number[] {
  return [[0, 1, 2, 3, 4, 5], [current], [next], [current, next], []][homeChoice(table, 4)];
}

/** Mirrors HomeLayout.java; all visual dimensions come from the shared tokens. */
export function homeLayout(p: Prefs, count: number, rtl: boolean) {
  const mode = homeChoice(p.homeCountdown, 2), table = homeChoice(p.homePrayers, 4), side = homeChoice(p.homeSide, 2);
  const classic = p.homeLayout === 'classic', gap = HOME.gap;
  count = Math.max(0, Math.min(classic ? 4 : 9, count));
  const columns = Math.max(1, classic ? count : mode === 0 ? Math.ceil(count / 2) : Math.max(Math.ceil(count / 2), Math.min(count, 4))), rows = Math.ceil(count / columns);
  const full = HOME.width - 2 * margin, panelAtStart = side === 0 || (side === 1) !== rtl;
  const width = mode === 0 ? full - HOME['mihrab-width'] - HOME['panel-gap'] : full;
  const x = mode === 0 && panelAtStart ? margin + HOME['mihrab-width'] + HOME['panel-gap'] : margin;
  const panelX = panelAtStart ? margin : margin + width + HOME['panel-gap'];
  const panelY = mode === 0 ? parseFloat(tokens.layout.arch.top.$value) : p.homeBar === 1 ? HOME.bottom - HOME['bar-height'] : HOME.top;
  const quickY = mode === 0 && p.quickLabels ? HOME['labelled-controls-top'] : 56;
  let top = mode === 0 ? (p.quickLabels ? quickY + parseFloat(tokens.layout['button-height'].$value) + gap : HOME['mihrab-content-top']) : HOME.top, bottom = HOME.bottom;
  if (mode === 1) { if (p.homeBar === 1) bottom = panelY - gap; else top += HOME['bar-height'] + gap; }
  const tableY = top, labelY = top + (table === 4 ? 0 : HOME['table-height'] + gap);
  const favouritesY = labelY + (rows === 0 ? 0 : HOME['label-height']);
  const tileWidth = Math.min(HOME['tile-max-width'], (width - (columns - 1) * gap) / columns);
  const available = bottom - favouritesY - (rows === 0 ? 0 : gap) - HOME['label-height'] - HOME['recent-height'];
  const tileHeight = rows === 0 ? 0 : Math.min(classic ? HOME['tile-classic-height'] : HOME['tile-max-height'], (available - (rows - 1) * gap) / rows);
  const recentLabelY = favouritesY + (rows === 0 ? 0 : rows * tileHeight + (rows - 1) * gap + gap);
  return { mode, table, count, columns, rows, width, x, panelX, panelY, quickY, tableY, labelY, favouritesY, tileWidth, tileHeight, recentLabelY, recentY: recentLabelY + HOME['label-height'] };
}

export function homePreview(p: Prefs, rtl: boolean) {
  const h = homeLayout(p, 6, rtl);
  const box = (x: number, y: number, w: number, height: number, color = 'lapis-700') => `<rect x="${x}" y="${y}" width="${w}" height="${height}" rx="16" fill="var(--color-${color})"/>`;
  const slots = homePrayers(h.table, -1, 0).length, cw = Math.min(HOME['tile-max-width'], (h.width - (slots - 1) * 12) / Math.max(1, slots));
  return `<svg viewBox="0 0 1920 1080" aria-hidden="true"><g${rtl ? ' transform="translate(1920 0) scale(-1 1)"' : ''}>`
    + box(h.x, 56, 400, 100, 'plaster-100')
    + Array.from({length: 5}, (_, i) => box(h.x + h.width - 472 + i * 100, h.quickY, 72, 72, i === 0 ? 'gold-400' : 'lapis-700')).join('')
    + (h.mode === 0 ? box(h.panelX, h.panelY, HOME['mihrab-width'], HOME['mihrab-height']) : h.mode === 1 ? box(h.x, h.panelY, h.width, HOME['bar-height']) : '')
    + Array.from({length: slots}, (_, i) => box(h.x + i * (cw + 12), h.tableY, cw, HOME['table-height'], 'gold-400')).join('')
    + Array.from({length: h.count}, (_, i) => box(h.x + i % h.columns * (h.tileWidth + HOME.gap), h.favouritesY + Math.floor(i / h.columns) * (h.tileHeight + HOME.gap), h.tileWidth, h.tileHeight)).join('')
    + Array.from({length: 6}, (_, i) => box(h.x + i * (h.width + 20) / 6, h.recentY, (h.width - 100) / 6, HOME['recent-height'], 'lapis-800')).join('') + '</g></svg>';
}
