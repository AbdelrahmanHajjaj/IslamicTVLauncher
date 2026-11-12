// Canonical interface strings; formatting helpers still mirror the native adapter.
import S from '../../../shared/data/strings.json';
import F from '../../../shared/data/format-text.json';
import { METHOD_DEFINITIONS, type Station } from './data';

export type Lang = 'ar' | 'en';
type Entry = { ar: string | string[]; en: string | string[]; d?: boolean };
// Preserve the old web extractor's compact literal spacing before filling parameters.
const compact = (value: string | string[]) => Array.isArray(value) ? value.map(text => text.replace(/\s+/g, ' ')) : value.replace(/\s+/g, ' ');
const STRINGS = Object.fromEntries(Object.entries(S).map(([key, entry]) => [key, {...entry, ar: compact(entry.ar), en: compact(entry.en)}])) as Record<string, Entry>;
const EASTERN = '٠١٢٣٤٥٦٧٨٩';

export type Text = ReturnType<typeof makeText>;

/** Text.java for one language; `eastern` is the Eastern Arabic digits setting (on by default in Arabic). */
export function makeText(lang: Lang, eastern = lang === 'ar') {
  const ar = lang === 'ar';
  const copy = F[lang];
  const d = (s: string | number) => {
    s = String(s);
    if (!eastern) return s;
    // "·" beside Eastern digits reads as a zero (٠); Arabic punctuation takes its own comma instead.
    return s.replaceAll(' · ', '، ').replace(/[0-9]/g, (c) => EASTERN[+c]);
  };
  /** A canonical string by key, with {params} filled in. */
  const s = (key: string, params: Record<string, string | number> = {}): string => {
    const e = STRINGS[key];
    if (!e) throw new Error(`no string "${key}" in shared/data/strings.json`);
    const v = e[lang] as string;
    const out = v.replace(/\{(\w+)\}/g, (_, n) => String(params[n] ?? ''));
    return e.d ? d(out) : out;
  };
  /** A canonical list. */
  const list = (key: string): string[] => STRINGS[key][lang] as string[];

  const sector = (deg: number) => Math.round((((deg % 360) + 360) % 360) / 45) % 8;
  const octant = (bearing: number) => copy.octants[sector(bearing)];
  const inMinutes = (minutes: number, hoursAndMinutes: string) => {
    if (minutes >= 60) return d(copy.relativePrefix + hoursAndMinutes);
    if (!ar) return d(F.en.relativePrefix + minutes + F.en.minuteUnit);
    if (minutes <= 1) return F.ar.minutesOne;
    if (minutes === 2) return F.ar.minutesTwo;
    return d(F.ar.relativePrefix + minutes + (minutes <= 10 ? F.ar.minutesUnit : F.ar.minuteUnit));
  };
  const station = (st: Station) => d(ar ? st.ar : st.en);
  const stationWhere = (st: Station) => d(ar ? st.whereAr : st.whereEn);
  const sep = () => d(' · ');
  const school = (hanafi: boolean) => hanafi ? copy.asrHanafi : copy.asrStandard;
  const methodName = (m: string) => {
    const method = METHOD_DEFINITIONS.find(value => value.id === m) ?? METHOD_DEFINITIONS[0];
    return ar ? method.ar : method.en;
  };

  return {
    lang, ar, eastern, d, s, list,
    dir: ar ? 'rtl' : 'ltr',
    sep,
    ampm: (am: boolean) => am ? copy.am : copy.pm,
    all: (n: number) => d(s('allTitle') + '\n' + n),
    hintOk: (what: string) => 'OK  ' + what,
    inMinutes,
    in: (duration: string) => d(copy.relativePrefix + duration),
    apTitle: (prayer: string, minutes: number, hoursAndMinutes: string) =>
      !ar && minutes < 60
        ? d(prayer + F.en.prayerInSeparator + minutes + (minutes === 1 ? F.en.minuteSingular : F.en.minutesUnit))
        : prayer + ' ' + inMinutes(minutes, hoursAndMinutes),
    apSub: (time: string) => d(copy.callTimePrefix + time),
    bannerSub: (time: string, seconds: number) =>
      d((ar ? 'الأذان ' : 'Adhan ') + time + (ar ? ` · يختفي خلال ${seconds} ث` : ` · hides in ${seconds} s`)),
    station, stationWhere,
    radioStatus: (playing: boolean) => playing ? copy.radioOn : copy.radioOff,
    radioNote: () =>
      d(ar ? 'تستمر في الرئيسية وشاشة التوقف · تتوقف إذا شغّل تطبيق آخر صوتًا'
           : 'Keeps playing on Home and the screensaver · stops when another app plays sound'),
    screenOffNote: () =>
      d(ar ? 'الشاشة مطفأة والإذاعة تعمل · اضغط أي زر للعودة' : 'Screen off, radio playing · press any key to come back'),
    nowHint: () => d(ar ? 'رجوع للإغلاق · يُغلق تلقائيًا بعد 10 دقائق' : 'Back to dismiss · closes by itself after 10 min'),
    chip: (prayer: string) => s('chip', {prayer: String(prayer)}),
    school, methodName,
    onlineContext: (place: string, method: string, hanafi: boolean, date: string, zone: string) =>
      [place, methodName(method), school(hanafi), d(date), zone].join(sep()),
    place: (city: string, country: string) => city + copy.placeSeparator + country,
    offsetMinutes: (n: number) => d(`${n > 0 ? '+' : ''}${n}` + copy.offsetMinuteSuffix),
    offsetCount: (n: number) => d(n === 0 ? copy.offsetAllZero : n + copy.offsetChangedSuffix),
    days: (n: number) => {
      const sign = n > 0 ? '+' : '';
      if (!ar) return d(sign + n + (Math.abs(n) === 1 ? F.en.dayUnit : F.en.daysUnit));
      return d(sign + n + (Math.abs(n) === 2 ? F.ar.dayDual : F.ar.dayUnit));
    },
    bell: (on: boolean) => on ? copy.bellOn : copy.bellOff,
    passed: (sunrise: boolean) => ar ? (sunrise ? F.ar.passedSunrise : F.ar.passedPrayer) : F.en.passed,
    octant,
    qiblaRelative: (clockwise: number) => copy.qiblaPrefix + copy.relativeDirections[sector(clockwise)],
    bearingLabel: (deg: number) => d(octant(deg) + ' · ' + deg + '°'),
    roomButton: (set: boolean) => set ? copy.roomChange : copy.roomSet,
  };
}
