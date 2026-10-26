import { prayerLabel } from '../lib/prayer-icon';
// All apps (AllAppsActivity): example apps, the live next-prayer chip and the favourite notes.
import type { Ctx, Model } from '../lib/ctx';
import { duration, secondsUntil } from '../lib/prayer';
import type { Text } from '../lib/text';
import { prayerNames } from './home';

export type AppIcon = 'tv' | 'film' | 'book' | 'globe' | 'music' | 'bag' | 'play' | 'folder' | 'gear' | 'gamepad' | 'image' | 'sun' | 'news' | 'radio';

/** Example apps, favourites first (as Home), then the rest. On the TV these are the viewer's own. */
export const APPS: { ar: string; en: string; icon: AppIcon }[] = [
  { ar: 'يوتيوب', en: 'YouTube', icon: 'tv' },
  { ar: 'شاهد', en: 'Shahid', icon: 'film' },
  { ar: 'القرآن الكريم', en: 'Quran', icon: 'book' },
  { ar: 'المتصفح', en: 'Browser', icon: 'globe' },
  { ar: 'يوتيوب ميوزك', en: 'YouTube Music', icon: 'music' },
  { ar: 'متجر Play', en: 'Play Store', icon: 'bag' },
  { ar: 'مشغل الوسائط', en: 'Media Player', icon: 'play' },
  { ar: 'مدير الملفات', en: 'File Manager', icon: 'folder' },
  { ar: 'الإعدادات', en: 'Settings', icon: 'gear' },
  { ar: 'الألعاب', en: 'Games', icon: 'gamepad' },
  { ar: 'الصور', en: 'Photos', icon: 'image' },
  { ar: 'الطقس', en: 'Weather', icon: 'sun' },
  { ar: 'الأخبار', en: 'News', icon: 'news' },
  { ar: 'البودكاست', en: 'Podcasts', icon: 'radio' },
];
export const FAVS = 4;

/** Text.favNote: "In favourites · position 2". */
export function favNote(t: Text, position: number) {
  const ord = ['الأول', 'الثاني', 'الثالث', 'الرابع'];
  return t.ar ? 'في المفضلة · ' + ord[position - 1] : t.d('In favourites · position ' + position);
}

export function appsModel(c: Ctx): Model {
  const { t, day, now } = c;
  return {
    html: {
      chipLabel: prayerLabel(day.nextIndex, t.chip(prayerNames(c)[day.nextIndex])),
      chipTime: t.d(duration(secondsUntil(day.nextTime, now), false)),
    },
  };
}
