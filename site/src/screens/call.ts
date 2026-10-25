import { prayerLabel } from '../lib/prayer-icon';
// The call (NowActivity): the prayer whose time has come, with its time. The page's clock starts 2 s past it.
import tokens from '../../../afnon-mihrab/tokens.json';
import type { Ctx, Model } from '../lib/ctx';
import { clock } from '../lib/prayer';
import { prayerNames } from './home';

const UNIT = { ms: 1, s: 1000, min: 60000 } as const;
const ms = (v: string) => { const m = /^([\d.]+)(ms|s|min)$/.exec(v)!; return Number(m[1]) * UNIT[m[2] as keyof typeof UNIT]; };
/** motion.call-auto-dismiss: the call closes by itself after this. */
export const AUTO_DISMISS_MS = ms(tokens.motion['call-auto-dismiss'].$value);

export function callModel(c: Ctx): Model {
  const { t, day, h24 } = c;
  // nowIndex is the prayer just begun; before the clock reaches it (never, in this mode) fall back to the next.
  const i = day.nowIndex >= 0 ? day.nowIndex : day.nextIndex;
  return {
    html: {
      name: prayerLabel(i, prayerNames(c)[i]),
      time: clock(t, day.times[i], h24, 0.5),
    },
  };
}
