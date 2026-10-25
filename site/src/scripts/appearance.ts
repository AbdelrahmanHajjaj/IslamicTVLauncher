import tokens from '../../../afnon-mihrab/tokens.json';
import aurora from '../../../app/src/main/assets/themes/aurora.json';
import ember from '../../../app/src/main/assets/themes/ember.json';
import forest from '../../../app/src/main/assets/themes/forest.json';
import midnight from '../../../app/src/main/assets/themes/midnight.json';
import rose from '../../../app/src/main/assets/themes/rose.json';
import sand from '../../../app/src/main/assets/themes/sand.json';
import slate from '../../../app/src/main/assets/themes/slate.json';
import patterns from '../data/background-patterns.json';
import type { Prefs } from '../lib/data';
const presets: Record<string, typeof aurora> = { aurora, ember, forest, midnight, rose, sand, slate };
export const THEMES = ['mihrab', ...Object.keys(presets)];
export const BACKGROUNDS = ['theme', 'lattice', 'solid', 'panorama', 'daylight', 'mountains', 'dunes', 'halo', 'night_sky', ...Object.keys(patterns)];
const rendered = new WeakMap<HTMLElement, string>();
export function appearance(stage: HTMLElement, prefs: Prefs) {
  const theme = presets[prefs.theme ?? 'mihrab'];
  const mode = prefs.background && prefs.background !== 'theme' ? prefs.background : theme?.background ?? 'lattice';
  const key = JSON.stringify([prefs.theme, prefs.background, prefs.lighting, prefs.reduceMotion, mode === 'daylight' ? Math.floor(Date.now() / 60000) : 0]);
  if (key === rendered.get(stage)) return;
  rendered.set(stage, key);
  for (const name of Object.keys(aurora.colors)) {
    if (theme) stage.style.setProperty('--color-' + name, theme.colors[name as keyof typeof aurora.colors]);
    else stage.style.setProperty('--color-' + name, tokens.color[name as keyof typeof aurora.colors].$value);
  }
  stage.dataset.background = mode;
  const css = getComputedStyle(stage);
  const color = (name: string) => css.getPropertyValue('--color-' + name).trim();
  const dark = color('lapis-950'), ground = color('lapis-900'), accent = color('gold-400');
  const hour = new Date().getHours() + new Date().getMinutes() / 60;
  const daylight = mode === 'daylight' ? Math.max(.15, Math.sin((hour - 6) / 12 * Math.PI)) : .7;
  const light = (prefs.lighting ?? theme?.lighting ?? 35) / 100 * daylight;
  let art = '';
  const pattern = patterns[mode as keyof typeof patterns];
  if (pattern) art = `<path d="${pattern.path}" fill="none" stroke="${accent}" stroke-width="${pattern.stroke}" stroke-linecap="round" stroke-linejoin="round" opacity="${(18 + Math.floor((prefs.lighting ?? theme?.lighting ?? 35) / 4)) / 255}"/>`;
  else if (mode === 'mountains') {
    for (let layer = 0; layer < 3; layer++) {
      let points = '0,270 ';
      for (let x = 0; x <= 480; x += 40) points += `${x},${125 + layer * 38 + ((x / 40 * 37 + layer * 17) % 67)} `;
      art += `<polygon points="${points}480,270" fill="${dark}" opacity="${.45 + layer * .2}"/>`;
    }
  } else if (mode === 'dunes') {
    for (let layer = 0; layer < 3; layer++) art += `<path d="M0 ${150 + layer * 35} C140 ${80 + layer * 50} 260 ${270 - layer * 15} 480 ${140 + layer * 38} V270 H0Z" fill="${dark}" opacity="${.4 + layer * .2}"/>`;
  } else if (mode === 'halo') art = `<ellipse cx="345" cy="115" rx="130" ry="130" fill="url(#glow)"/>`;
  else if (mode === 'night_sky') {
    for (let i = 0; i < 65; i++) art += `<circle cx="${(i * 73 + 11) % 480}" cy="${(i * 47 + 19) % 190}" r="${i % 5 === 0 ? .8 : .4}" fill="${color('plaster-100')}" opacity="${.2 + light * .4}"/>`;
  }
  if (['panorama','daylight','night_sky'].includes(mode)) art += `<path d="M0 225 C110 160 160 265 280 215 C360 180 400 210 480 155 V270 H0Z" fill="${dark}"/>`;
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 480 270"><defs><linearGradient id="base" x2="1" y2="1"><stop stop-color="${dark}"/><stop offset="1" stop-color="${ground}"/></linearGradient><radialGradient id="glow"><stop stop-color="${accent}" stop-opacity="${light * .3}"/><stop offset="1" stop-color="${accent}" stop-opacity="0"/></radialGradient></defs><path fill="url(#base)" d="M0 0H480V270H0Z"/><ellipse cx="360" cy="190" rx="210" ry="210" fill="url(#glow)"/>${art}</svg>`;
  stage.style.setProperty('--scene-image', mode === 'solid' ? 'none' : mode === 'lattice' ? 'repeating-conic-gradient(from 45deg, transparent 0 25%, #ffffff08 0 50%) 0 / 36px 36px' : `url("data:image/svg+xml,${encodeURIComponent(svg)}")`);
  stage.dataset.reduced = String(!!prefs.reduceMotion);
  stage.style.setProperty('--ambient-light', `${Math.round((prefs.lighting ?? 35) * .3)}%`);
}
