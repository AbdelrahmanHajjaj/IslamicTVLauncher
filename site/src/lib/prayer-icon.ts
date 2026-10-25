import paths from '../data/prayer-icons.json';

export function prayerLabel(prayer: number, text: string): string {
  const path = paths[prayer];
  if (!path) throw new Error('Invalid prayer icon');
  return `<svg class="prayer-icon" aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="${path}"/></svg>${text}`;
}
