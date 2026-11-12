import portable from '@islamic-tv/core';
import tokens from '../../../afnon-mihrab/tokens.json';
import type { Prefs } from './data';

const core = portable.tv.afnon.core;
export const UI_SIZES: string[] = core.UiContract.choices('display.size');
export const uiPercent = (value: unknown): number => typeof value === 'number' ? core.UiScale.percent(value) : core.UiScale.DEFAULT_PERCENT;
export const uiFactor = (prefs: Prefs) => uiPercent(prefs.uiScale) / 100;

/** Home/All apps preserve their grid gaps and the largest focused tile at 110%. */
export function safeInline(prefs: Prefs) {
  const factor = uiFactor(prefs), margin = parseFloat(tokens.space['safe-inline'].$value);
  if (factor <= 1) return margin;
  const reach = parseFloat(tokens.focus['ring-width'].$value) + parseFloat(tokens.focus['ring-offset'].$value);
  const width = parseFloat(tokens.layout.home['tile-max-width'].$value), growth = (tokens.focus.scale.$value - 1) / 2;
  return Math.max(margin, 960 - 960 / factor + reach + (width + 2 * reach) * growth + parseFloat(tokens.space['2'].$value) / factor);
}
