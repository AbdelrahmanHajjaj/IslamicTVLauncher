import aurora from '../../../app/src/main/assets/themes/aurora.json';
import ember from '../../../app/src/main/assets/themes/ember.json';
import forest from '../../../app/src/main/assets/themes/forest.json';
import type { Prefs } from '../lib/data';
const presets: Record<string, typeof aurora> = { aurora, ember, forest };
let last = '';
export function appearance(stage: HTMLElement, prefs: Prefs) {
  const key = JSON.stringify([prefs.theme, prefs.background, prefs.lighting, prefs.reduceMotion, new Date().getHours()]);
  if (key === last) return;
  last = key;
  const theme = presets[prefs.theme ?? 'mihrab'];
  for (const name of Object.keys(aurora.colors)) {
    if (theme) stage.style.setProperty('--color-' + name, theme.colors[name as keyof typeof aurora.colors]);
    else stage.style.removeProperty('--color-' + name);
  }
  const mode = prefs.background && prefs.background !== 'theme' ? prefs.background : theme?.background ?? 'lattice';
  stage.dataset.background = mode;
  stage.dataset.reduced = String(!!prefs.reduceMotion);
  stage.style.setProperty('--ambient-light', `${Math.round((prefs.lighting ?? 35) * .3)}%`);
}
