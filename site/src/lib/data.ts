// The app's data, read from its Java sources by scripts/extract.mjs.
import citiesJson from '../data/cities.json';
import methodsJson from '../data/methods.json';
import stationsJson from '../data/stations.json';

export type City = (typeof citiesJson)[number];
export type Station = (typeof stationsJson)[number];

export const CITIES: City[] = citiesJson;
export const METHODS: string[] = methodsJson;
export const STATIONS: Station[] = stationsJson;

/** Viewer preferences, as Afnon Settings keeps them on the TV. Saved in this browser only. */
export type Prefs = {
  city: string;
  method: string | null; // null: the city's own method
  hanafi: boolean;
  hijriOffset: number;
  clock: 'system' | '12' | '24';
  digits: 'auto' | 'eastern' | 'western';
  remind: number;
  adhan: boolean;
  station: string;
  voice?: string; voiceFajr?: string; // adhan voices (Adhans ids); unset: the app's defaults
  radioResume?: boolean; radioDark?: number; // Settings' radio rows
};

export const DEFAULT_PREFS: Prefs = {
  city: 'cairo', method: null, hanafi: false, hijriOffset: 0, clock: 'system', digits: 'auto',
  remind: 15, adhan: true, station: 'cairo',
};

export const cityOf = (p: Prefs) => CITIES.find((c) => c.id === p.city) ?? CITIES[0];
export const methodOf = (p: Prefs) => p.method ?? cityOf(p).method;
export const stationOf = (p: Prefs) => STATIONS.find((s) => s.id === p.station) ?? STATIONS[0];
