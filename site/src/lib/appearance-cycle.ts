import tokens from '../../../afnon-mihrab/tokens.json';
import type { Prefs } from './data';
import { THEMES, BACKGROUNDS, backgroundOf } from '../scripts/appearance';

export const CYCLE_MINUTES = [0, parseFloat(tokens.timing['appearance-short'].$value), parseFloat(tokens.timing['appearance-long'].$value)];
export const cycleMinutes = (value: number | undefined) => CYCLE_MINUTES.includes(value ?? 0) ? value ?? 0 : 0;

/** Manual Apply starts a full interval; Off retains the displayed theme/background. */
export function armCycle(p: Prefs, now: number): Prefs {
  const next = { ...p }, minutes = cycleMinutes(p.appearanceCycle);
  if (minutes) { next.appearanceCycle = minutes; next.appearanceCycleAt = now + minutes * 60_000; }
  else { delete next.appearanceCycle; delete next.appearanceCycleAt; }
  return next;
}

/** Caller owns foreground/menu guards and the existing clock tick. */
export function cycleAppearance(p: Prefs, now: number, random = Math.random): Prefs {
  const minutes = cycleMinutes(p.appearanceCycle);
  if (!minutes) return p;
  const at = p.appearanceCycleAt ?? 0;
  if (!Number.isFinite(at) || at <= 0 || at > now + minutes * 60_000) return armCycle(p, now);
  if (now < at) return p;
  const choose = (values: string[], current: string) => {
    const choices = values.filter(v => v !== current);
    return choices[Math.floor(random() * choices.length)];
  };
  return armCycle({ ...p, theme: choose(THEMES, p.theme ?? 'mihrab'), background: choose(BACKGROUNDS.slice(1), backgroundOf(p)) }, now);
}
