import icons from '../../../shared/design/prayer-icons.json';

export function prayerLabel(prayer: number, text: string): string {
  const path = icons.prayers[prayer]?.path;
  if (!path) throw new Error('Invalid prayer icon');
  return `<svg class="prayer-icon" aria-hidden="true" viewBox="0 0 ${icons.size} ${icons.size}" fill="none" stroke="currentColor" stroke-width="${icons.strokeWidth}" stroke-linecap="round" stroke-linejoin="round"><path d="${path}"/></svg>${text}`;
}
