// Settings (SettingsActivity): sections, each section's rows and the Asr preview, as HTML strings so the same
// code renders the page at build time and re-renders it in the browser after every change.
import { homeChoice } from '../lib/home-layout';
import { CYCLE_MINUTES, cycleMinutes } from '../lib/appearance-cycle';
import { THEMES, BACKGROUNDS } from '../scripts/appearance';
import voicesJson from '../data/adhans.json';
import { makeCtx, type Ctx } from '../lib/ctx';
import { CITIES, METHODS, REGIONS, STATIONS, cityOf, methodOf, stationOf, type Prefs, type Station } from '../lib/data';
import { ASR, clock, hijri, methodParams, prayerOffsets, MAX_PRAYER_OFFSET } from '../lib/prayer';
import type { Text } from '../lib/text';
import { prayerLabel } from '../lib/prayer-icon';
import { loadFacing } from './prayer-day';

export const PRAYER = 0, REMINDERS = 1, RADIO = 2, DISPLAY = 3, APPEARANCE = 4, NETWORK = 5, DEVICE = 6, SECTIONS = 7;
export type Kind = 'value' | 'seg' | 'step' | 'toggle' | 'carousel';
export const VOICES = voicesJson;

/** Every saved setting is in Prefs; the room direction is the Prayer Day screen's (loadFacing). */
export type SPrefs = Prefs;

export type Row = {
  id: string; kind: Kind; label: string; help: string;
  prayer?: number;
  value?: string; segs?: string[]; selected?: number; on?: boolean;
};

const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
const trim = (v: number) => String(+v.toFixed(2));

// ---------------------------------------------------------------- Text.java methods with logic, ported

export const sectionName = (t: Text, i: number) =>
  [t.s('sectionPrayer'), t.s('sectionReminders'), t.s('sectionRadio'), t.s('sectionDisplay'), t.s('sectionAppearance'), t.ar ? 'الشبكة' : 'Network', t.s('sectionDevice')][i];

function locationHelp(t: Text, lat: number, lng: number) {
  const ns = lat >= 0 ? (t.ar ? 'ش' : 'N') : t.ar ? 'ج' : 'S';
  const ew = lng >= 0 ? (t.ar ? 'ق' : 'E') : t.ar ? 'غ' : 'W';
  return t.d(`${Math.abs(lat).toFixed(2)}° ${ns} · ${Math.abs(lng).toFixed(2)}° ${ew}` + (t.ar ? ' · يُختار من القائمة' : ' · chosen from the list'));
}

function methodAngles(t: Text, m: string) {
  const p = methodParams(m);
  const fajr = (t.ar ? 'زاوية الفجر ' : 'Fajr angle ') + trim(p.fajrAngle) + '°';
  const isha = p.ishaInterval > 0
    ? t.ar ? `العشاء بعد المغرب بـ ${p.ishaInterval} دقيقة` : `Isha ${p.ishaInterval} min after Maghrib`
    : (t.ar ? 'العشاء ' : 'Isha ') + trim(p.ishaAngle) + '°';
  return t.d(fajr + ' · ' + isha);
}

export const stationChoice = (t: Text, s: Station) => (s.reciter ? t.station(s) : t.station(s) + t.sep() + t.stationWhere(s));
const radioRowHelp = (t: Text, s: Station) => t.stationWhere(s) + (t.ar ? ' · تحتاج إلى الإنترنت' : ' · needs the internet');
const radioDarkHelp = (t: Text) =>
  t.d(t.ar ? 'توفير الكهرباء: تنطفئ الشاشة وتستمر الإذاعة · أي زر يعيدها' : 'Saves power: the screen goes dark, the radio keeps playing · any key brings it back');
export const voiceName = (t: Text, id: string | undefined, fallback: string) => {
  const v = VOICES.find((x) => x.id === (id ?? fallback)) ?? VOICES[0];
  return t.ar ? v.ar : v.en;
};
export const placeOf = (t: Text, c: (typeof CITIES)[number]) => (t.ar ? t.place(c.ar, c.countryAr) : t.place(c.en, c.countryEn));

// ---------------------------------------------------------------- rows

/** Settings → Reminders, in the order of Text.remindChoices (a list on the TV: six are too many for a seg). */
export const REMIND = [5, 10, 15, 20, 30, 0];
const remindIndex = (p: SPrefs) => (REMIND.indexOf(p.remind) < 0 ? 2 : REMIND.indexOf(p.remind));
export const DOCK_MODES = ['hidden', 'tab', 'off'] as const;
const DARK = [0, 15, -1];
const CLOCKS = ['system', '12', '24'] as const;

export function rowsFor(c: Ctx, section: number, radioOn = false, offsetsPage = false): Row[] {
  const { t } = c;
  const p = c.prefs as SPrefs;
  const facing = loadFacing();
  if (offsetsPage) return t.list('prayerNames').map((label, prayer) => ({
    id: `offset${prayer}`, kind: 'step', prayer, label,
    help: clock(t, c.day.times[prayer], c.h24), value: t.offsetMinutes(prayerOffsets(p)[prayer]),
  }));
  const city = cityOf(p);
  const st = stationOf(p);
  const mode = homeChoice(p.homeCountdown, 2);
  switch (section) {
    case PRAYER: return [
      { id: 'city', kind: 'value', label: t.s('location'), help: locationHelp(t, city.lat, city.lng), value: placeOf(t, city) },
      { id: 'method', kind: 'value', label: t.s('method'), help: methodAngles(t, methodOf(p)), value: t.methodName(methodOf(p)) },
      { id: 'school', kind: 'seg', label: t.s('asrSchool'), help: t.s('preview') + t.sep() + previewHtml(c), segs: t.list('asrSchools'), selected: p.hanafi ? 1 : 0 },
      { id: 'offsets', kind: 'value', label: t.s('prayerOffsets'), help: t.s('prayerOffsetsHelp'), value: t.offsetCount(prayerOffsets(p).filter(n => n !== 0).length) },
      { id: 'hijri', kind: 'step', label: t.s('hijriAdjust'), help: (t.ar ? 'اليوم: ' : 'Today: ') + hijri(t, c.day), value: t.days(p.hijriOffset) },
      { id: 'room', kind: 'value', label: t.s('roomTitle'), help: t.s('roomHelp'), value: facing < 0 ? t.s('notSet') : t.bearingLabel(facing) },
    ];
    case REMINDERS: return [
      { id: 'remind', kind: 'value', label: t.s('remind'), help: t.s('remindHelp'), value: t.d(t.list('remindChoices')[remindIndex(p)]), selected: remindIndex(p) },
      { id: 'adhan', kind: 'toggle', label: t.s('adhanRow'), help: t.s('adhanHelp'), on: p.adhan },
      { id: 'voice', kind: 'value', label: t.s('voiceRow'), help: t.s('voiceHelp'), value: voiceName(t, p.voice, 'alafasy') },
      { id: 'voiceFajr', kind: 'value', label: t.s('fajrVoiceRow'), help: t.s('fajrVoiceHelp'), value: voiceName(t, p.voiceFajr, 'chebae_fajr') },
    ];
    case RADIO: return [
      { id: 'radio', kind: 'toggle', label: t.station(st), help: radioRowHelp(t, st), on: radioOn },
      { id: 'station', kind: 'value', label: t.s('stationRow'), help: t.list('stationGroups')[st.reciter ? 1 : 0], value: stationChoice(t, st) },
      { id: 'resume', kind: 'seg', label: t.s('afterAdhanRow'), help: t.s('afterAdhanHelp'), segs: t.list('afterAdhanChoices'), selected: p.radioResume === false ? 1 : 0 },
      { id: 'dark', kind: 'seg', label: t.s('radioDarkRow'), help: radioDarkHelp(t), segs: t.list('radioDarkChoices').map(t.d), selected: Math.max(0, DARK.indexOf(p.radioDark ?? 0)) },
    ];
    case DISPLAY: return [
      { id: 'lang', kind: 'seg', label: t.s('language'), help: t.s('languageHelp'), segs: [t.ar ? 'التلفاز' : 'TV', 'العربية', 'English'], selected: t.ar ? 1 : 2 },
      { id: 'digits', kind: 'seg', label: t.s('digits'), help: '', segs: ['0123', '٠١٢٣'], selected: t.eastern ? 1 : 0 },
      { id: 'clock', kind: 'seg', label: t.s('clockRow'), help: t.s('clockHelp'), segs: t.list('clocks').map(t.d), selected: Math.max(0, CLOCKS.indexOf(p.clock)) },
      { id: 'quickLabels', kind: 'toggle', label: t.s('quickLabels'), help: '', on: p.quickLabels ?? false },
      { id: 'dockMode', kind: 'value', label: t.s('prayerDock'), help: '', value: t.list('dockModes')[DOCK_MODES.indexOf(p.dockMode ?? 'hidden')] },
      { id: 'dockPosition', kind: 'seg', label: t.s('dockPosition'), help: '', segs: t.list('dockPositions'), selected: p.dockPosition === 'top' ? 0 : 1 },

    ];
    case APPEARANCE: return [
      { id: 'homeCountdown', kind: 'carousel', label: t.s('homeCountdown'), help: '', segs: t.list('homeCountdowns'), selected: mode },
      { id: 'homePosition', kind: 'carousel', label: t.s('homePosition'), help: '', segs: mode === 2 ? ['—'] : t.list(mode === 1 ? 'homeBars' : 'homeSides'), selected: mode === 2 ? 0 : homeChoice(mode === 1 ? p.homeBar : p.homeSide, mode === 1 ? 1 : 2) },
      { id: 'homePrayers', kind: 'carousel', label: t.s('homePrayers'), help: '', segs: t.list('homePrayerModes'), selected: homeChoice(p.homePrayers, 4) },
      { id: 'homeFavourites', kind: 'carousel', label: t.s('homeFavourites'), help: '', segs: t.list('homeLayouts'), selected: p.homeLayout === 'classic' ? 1 : 0 },
      { id: 'theme', kind: 'carousel', label: t.s('themeRow'), help: '', segs: t.list('themeNames').slice(0, THEMES.length), selected: Math.max(0, THEMES.indexOf(p.theme ?? 'mihrab')) },
      { id: 'background', kind: 'carousel', label: t.s('backgroundRow'), help: '', segs: t.list('backgroundNames'), selected: Math.max(0, BACKGROUNDS.indexOf(p.background ?? 'theme')) },
      { id: 'lighting', kind: 'step', label: t.s('lightingRow'), help: '', value: t.d(`${p.lighting ?? 35}%`) },
      { id: 'appearanceCycle', kind: 'carousel', label: t.s('appearanceCycle'), help: t.s('appearanceCycleHelp'), segs: t.list('appearanceCycleChoices'), selected: CYCLE_MINUTES.indexOf(cycleMinutes(p.appearanceCycle)) },
      { id: 'reduceMotion', kind: 'toggle', label: t.s('reduceMotion'), help: '', on: p.reduceMotion ?? false },
      { id: 'homeReset', kind: 'value', label: t.s('homeReset'), help: '' },
      { id: 'apply', kind: 'value', label: t.s('applyAppearance'), help: '' },
    ];
    case NETWORK: return [{ id: 'network', kind: 'value', label: t.s('networkRow'), help: t.s('networkHelp'), value: '' }];
    default: return [
      { id: 'device', kind: 'value', label: t.s('deviceRow'), help: t.s('deviceHelp'), value: '' },
      ...['homeSettings', 'inputsTitle', 'screensaver', 'remoteButtons', 'serviceHealth'].map(id => ({ id, kind: 'value' as Kind, label: t.s(id), help: id === 'serviceHealth' ? t.s('serviceHelp') : '', value: '' })),
      { id: 'repairService', kind: 'toggle', label: t.s('repairService'), help: t.s('repairHelp'), on: p.repairService ?? false },
    ];
  }
}

/** Editing a seg or step row (OK, then ◀▶): the row and its unsaved choice or amount. */
export type Edit = { row: number; draft: number };

/** What editing starts from: the chosen segment, or the Hijri offset. */
export const savedDraft = (c: Ctx, row: Row) => (row.kind === 'seg' ? row.selected! : row.prayer !== undefined ? prayerOffsets(c.prefs)[row.prayer] : row.id === 'lighting' ? c.prefs.lighting ?? 35 : (c.prefs as SPrefs).hijriOffset);

/** ◀▶ while editing (d = +1 toward the end), held to the row's range. */
export const moveDraft = (row: Row, draft: number, d: number) =>
  row.kind === 'seg' ? Math.max(0, Math.min(row.segs!.length - 1, draft + d)) : row.prayer !== undefined ? Math.max(-MAX_PRAYER_OFFSET, Math.min(MAX_PRAYER_OFFSET, draft + d)) : row.id === 'lighting' ? Math.max(0, Math.min(100, draft + d)) : Math.max(-2, Math.min(2, draft + d));

/** OK while editing: the new prefs, or null when the draft is what is already saved. 'lang' is the page's. */
export function commit(c: Ctx, row: Row, draft: number): SPrefs | null {
  if (draft === savedDraft(c, row)) return null;
  if (row.prayer !== undefined) {
    const offsets = prayerOffsets(c.prefs);
    offsets[row.prayer] = moveDraft(row, draft, 0);
    return { ...c.prefs, prayerOffsets: offsets };
  }
  return row.kind === 'seg' ? pick(c, row.id, draft) : row.id === 'lighting' ? { ...c.prefs, lighting: draft } : { ...(c.prefs as SPrefs), hijriOffset: draft };
}

/** Choosing item i of a seg row. 'lang' is handled by the page (it moves to the other language's page). */
export function pick(c: Ctx, id: string, i: number): SPrefs | null {
  const p = { ...(c.prefs as SPrefs) };
  switch (id) {
    case 'homeCountdown': return { ...p, homeCountdown: homeChoice(i, 2) };
    case 'homePosition': return p.homeCountdown === 2 ? p : p.homeCountdown === 1 ? { ...p, homeBar: homeChoice(i, 1) } : { ...p, homeSide: homeChoice(i, 2) };
    case 'homePrayers': return { ...p, homePrayers: homeChoice(i, 4) };
    case 'homeFavourites': return { ...p, homeLayout: i === 1 ? 'classic' : 'two_rows' };
    case 'theme': return { ...p, theme: THEMES[i] };
    case 'background': return { ...p, background: BACKGROUNDS[i] };
    case 'appearanceCycle': return { ...p, appearanceCycle: CYCLE_MINUTES[i] ?? 0 };
    case 'school': return { ...p, hanafi: i === 1 };
    case 'remind': return { ...p, remind: REMIND[i] };
    case 'resume': return { ...p, radioResume: i === 0 };
    case 'dark': return { ...p, radioDark: DARK[i] };
    case 'digits': return { ...p, digits: i === 1 ? 'eastern' : 'western' };
    case 'dockPosition': return { ...p, dockPosition: i === 0 ? 'top' : 'bottom' };
    case 'clock': return { ...p, clock: CLOCKS[i] };
    default: return null;
  }
}

// ---------------------------------------------------------------- HTML

const chevron = '<svg class="chev" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 5l7 7-7 7"/></svg>';

export function rowHtml(t: Text, r: Row, i: number, editing = false, top = 'var(--layout-settings-rows-top)') {
  let control = '';
  if (r.kind === 'value') control = `<span class="val">${esc(r.value ?? '')}</span>${chevron}`;
  else if (r.kind === 'carousel') {
    control = [r.selected! - 1, r.selected!, r.selected! + 1].map(k => {
      const valid = k >= 0 && k < r.segs!.length;
      const label = k !== r.selected ? (valid ? ((k < r.selected!) !== t.ar ? '‹' : '›') : '') : r.segs![k] ?? '';
      return `<span class="slide${k === r.selected ? ' on' : ''} home-choice" ${valid ? `data-seg="${k}"` : ''}>${esc(label)}</span>`;
    }).join('');
  }
  else if (r.kind === 'seg')
    control = `<span class="track">${r.segs!.map((s, k) => `<span class="seg${k === r.selected ? ' on' : ''}" data-seg="${k}">${esc(s)}</span>`).join('')}</span>`;
  else if (r.kind === 'step')
    control = `<span class="stepb" data-step="-1">−</span><span class="stepv">${esc(r.value ?? '')}</span><span class="stepb" data-step="1">+</span>`;
  else control = `<span class="switch${r.on ? ' on' : ''}"><i></i></span>`;
  return `<button class="ft row srow${editing ? ' editing' : ''}${r.prayer !== undefined ? ' offset-row' : ''}" data-row="${i}" data-id="${r.id}" data-kind="${r.kind}" style="top:calc(${top} + ${i} * (var(--layout-settings-row-height) + var(--layout-settings-row-gap)))">`
    + `<span class="words"><span class="rl">${r.prayer !== undefined ? prayerLabel(r.prayer, esc(r.label)) : esc(r.label)}</span>${r.help && r.prayer === undefined ? `<span class="rh">${esc(r.help)}</span>` : ''}</span>
    ${r.prayer !== undefined ? `<span class="offset-time num" dir="ltr">${esc(r.help)}</span>` : ''}`
    + `<span class="ctl">${control}</span></button>`;
}

export function navHtml(t: Text, section: number) {
  return Array.from({ length: SECTIONS }, (_, i) =>
    `<button class="ft row nav${i === section ? ' open' : ''}" data-nav="${i}"${i === section ? ' data-focus-start' : ''} style="top:${152 + i * 86}px">${esc(sectionName(t, i))}</button>`).join('');
}

/** The rows; the one being edited shows its draft. */
export const rowsHtml = (c: Ctx, section: number, radioOn = false, edit: Edit | null = null, offsetsPage = false) =>
  rowsFor(c, section, radioOn, offsetsPage).map((r, i) => {
    if (edit?.row !== i) return rowHtml(c.t, r, i, false, section === APPEARANCE ? '0px' : 'var(--layout-settings-rows-top)');
    const shown = r.prayer !== undefined ? { ...r, value: c.t.offsetMinutes(edit.draft), help: clock(c.t, new Date(c.day.times[r.prayer].getTime() + (edit.draft - savedDraft(c, r)) * 60000), c.h24) } : r.kind === 'seg' ? { ...r, selected: edit.draft } : { ...r, value: r.id === 'lighting' ? c.t.d(`${edit.draft}%`) : c.t.days(edit.draft) };
    if (r.id === 'school') shown.help = c.t.s('preview') + c.t.sep() + previewHtml(makeCtx(c.lang, { ...c.prefs, hanafi: edit.draft === 1 }, c.now, c.mode));
    return rowHtml(c.t, shown, i, true, section === APPEARANCE ? '0px' : 'var(--layout-settings-rows-top)');
  }).join('');
export const previewHtml = (c: Ctx) => clock(c.t, c.day.times[ASR], c.h24);

// ---------------------------------------------------------------- chooser lists

export type Item = { label: string; chosen: boolean };

/** The region a city's country is in (index into REGIONS). */
export const regionOf = (c: (typeof CITIES)[number]) => REGIONS.findIndex((r) => r.includes(c.countryEn));
/** One region's countries, A to Z in the page's language ("ال" not counted). */
export const countries = (t: Text, region: number) =>
  REGIONS[region]
    .map((en) => CITIES.find((c) => c.countryEn === en)!)
    .map((c) => (t.ar ? c.countryAr : c.countryEn))
    .sort((a, b) => new Intl.Collator(t.ar ? 'ar' : 'en').compare(a.replace(/^ال/, ''), b.replace(/^ال/, '')));
export const citiesOf = (t: Text, country: string) => CITIES.filter((c) => (t.ar ? c.countryAr : c.countryEn) === country);
export const methodItems = (t: Text, p: Prefs): Item[] => METHODS.map((m) => ({ label: t.methodName(m), chosen: m === methodOf(p) }));
export const stationsOf = (reciters: boolean) => STATIONS.filter((s) => !!s.reciter === reciters);

export function chooserHtml(t: Text, title: string, items: Item[]) {
  return `<div class="ch-card"><div class="ch-title display-bold">${esc(title)}</div><div class="ch-list">`
    + items.map((it, i) => `<button class="ft ch-item${it.chosen ? ' chosen' : ''}" data-item="${i}">${esc(it.label)}</button>`).join('')
    + '</div></div>';
}
