import portable from '@islamic-tv/core';
import {cityOf, methodOf, type City, type Prefs} from './data';
import {civilPrayerTimes} from './prayer';

const comparison = portable.tv.afnon.core.PrayerComparison;
const names = ['Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Isha'];
export const ONLINE_DEADLINE_MS = 12000, ONLINE_MAX_BYTES = 32768;
export type OnlineRequest = {city: City; method: string; hanafi: boolean; date: string; isoDate: string; calculated: number[]};
export type OnlineResult = {source: OnlineRequest; timezone: string; reference: number[]; differences: number[]};

const pad = (n: number) => String(n).padStart(2, '0');
export function onlineRequest(now: Date, prefs: Prefs): OnlineRequest {
  const method = methodOf(prefs);
  comparison.aladhanMethod(method);
  if (!Number.isFinite(+now) || now.getFullYear() < 2000 || now.getFullYear() > 9999 || typeof prefs.hanafi !== 'boolean') throw new Error('Invalid reference request');
  return {city: {...cityOf(prefs)}, method, hanafi: prefs.hanafi,
    date: `${pad(now.getDate())}-${pad(now.getMonth() + 1)}-${now.getFullYear()}`,
    isoDate: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`,
    calculated: civilPrayerTimes(now, prefs).map(Number)};
}

export function referenceUrl(request: OnlineRequest) {
  return `https://api.aladhan.com/v1/timings/${request.date}?latitude=${request.city.lat}&longitude=${request.city.lng}`
    + `&method=${comparison.aladhanMethod(request.method)}&school=${request.hanafi ? 1 : 0}&latitudeAdjustmentMethod=1&shafaq=general&iso8601=true`;
}

function isoTime(text: unknown) {
  const m = typeof text === 'string' && /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})([+-])(\d{2}):(\d{2})$/.exec(text);
  if (!m || +m[1] < 2000 || +m[2] < 1 || +m[2] > 12 || +m[3] < 1 || +m[3] > 31 || +m[4] > 23 || +m[5] > 59 ||
    +m[6] > 59 || +m[8] > 14 || +m[9] > 59 || +m[8] === 14 && +m[9] !== 0) throw new Error('Invalid reference time');
  const day = Date.UTC(+m[1], +m[2] - 1, +m[3]), d = new Date(day);
  if (d.getUTCMonth() !== +m[2] - 1 || d.getUTCDate() !== +m[3]) throw new Error('Invalid reference date');
  return day + (+m[4] * 3600 + +m[5] * 60 + +m[6]) * 1000 - (m[7] === '+' ? 1 : -1) * (+m[8] * 60 + +m[9]) * 60000;
}

/** Untrusted provider JSON; inspect primitive identities before using any reference time. */
export function parseReference(request: OnlineRequest, body: any): OnlineResult {
  const data = body?.data, meta = data?.meta, id = comparison.aladhanMethod(request.method);
  if (body?.code !== 200 || data?.date?.gregorian?.date !== request.date || !meta ||
    typeof meta.latitude !== 'number' || !Number.isFinite(meta.latitude) || typeof meta.longitude !== 'number' || !Number.isFinite(meta.longitude) ||
    Math.abs(meta.latitude - request.city.lat) > 0.00001 || Math.abs(meta.longitude - request.city.lng) > 0.00001 || meta.method?.id !== id ||
    meta.school !== (request.hanafi ? 'HANAFI' : 'STANDARD') || meta.latitudeAdjustmentMethod !== (id === 15 ? 'NONE' : 'MIDDLE_OF_THE_NIGHT') ||
    id === 15 && meta.method?.params?.shafaq !== 'general' || typeof meta.timezone !== 'string' || !/^[A-Za-z0-9_+\-/]{1,80}$/.test(meta.timezone) || !data.timings) {
    throw new Error('Mismatched reference response');
  }
  const reference = names.map(name => isoTime(data.timings[name]));
  const anchor = isoTime(request.isoDate + 'T12:00:00+00:00');
  for (let i = 0; i < 6; i++) {
    if (Math.abs(reference[i] - anchor) > 36 * 3600000 || i && reference[i] <= reference[i - 1] ||
      i > 0 && i < 5 && data.timings[names[i]].slice(0, 10) !== request.isoDate) throw new Error('Invalid reference day');
  }
  return {source: request, timezone: meta.timezone, reference,
    differences: Array.from(comparison.minutes(new Float64Array(request.calculated), new Float64Array(reference)))};
}

export async function fetchComparison(request: OnlineRequest, signal: AbortSignal): Promise<OnlineResult> {
  if (signal.aborted) throw new Error('Online check cancelled');
  const controller = new AbortController(), abort = () => controller.abort();
  signal.addEventListener('abort', abort, {once: true});
  const timer = setTimeout(abort, ONLINE_DEADLINE_MS);
  let reader: ReadableStreamDefaultReader<Uint8Array> | undefined;
  try {
    const response = await fetch(referenceUrl(request), {signal: controller.signal, credentials: 'omit', cache: 'no-store',
      referrerPolicy: 'no-referrer', redirect: 'error', headers: {Accept: 'application/json'}});
    if (response.status !== 200 || !/^application\/json(?:;|$)/i.test(response.headers.get('content-type') ?? '') || !response.body) throw new Error('Online reference unavailable');
    reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8', {fatal: true});
    let bytes = 0, text = '';
    while (true) {
      const part = await reader.read();
      if (controller.signal.aborted) throw new Error('Online check cancelled');
      if (part.done) break;
      bytes += part.value.byteLength;
      if (bytes > ONLINE_MAX_BYTES) throw new Error('Reference response too large');
      text += decoder.decode(part.value, {stream: true});
    }
    return parseReference(request, JSON.parse(text + decoder.decode()));
  } finally {
    clearTimeout(timer); signal.removeEventListener('abort', abort); controller.abort(); reader?.releaseLock();
  }
}
