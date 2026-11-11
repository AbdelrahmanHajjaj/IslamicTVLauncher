const pathData = /^[MmLlHhVvCcSsQqTtAaZz0-9., +\-Ee]+$/;
const numeric = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/;
const attributes = {
  path: ['d', 'fill', 'stroke'],
  circle: ['cx', 'cy', 'r', 'fill', 'stroke'],
  ellipse: ['cx', 'cy', 'rx', 'ry', 'fill', 'stroke'],
  rect: ['x', 'y', 'width', 'height', 'rx', 'ry', 'fill', 'stroke'],
};

export function validateIcons(icons) {
  if (!icons || typeof icons !== 'object' || Array.isArray(icons) || !Object.keys(icons).length) throw new Error('Missing icon catalog');
  for (const [id, svg] of Object.entries(icons)) {
    if (!/^[a-z][a-z0-9_]*$/.test(id) || typeof svg !== 'string' || !svg.length) throw new Error('Invalid icon identity or geometry');
    let end = 0;
    for (const shape of svg.matchAll(/<(path|circle|ellipse|rect)((?: [a-z]+="[^"<>]*")*)\/>/g)) {
      if (shape.index !== end) throw new Error('Unsupported icon markup: ' + id);
      end = shape.index + shape[0].length;
      const seen = new Set();
      for (const [, name, value] of shape[2].matchAll(/ ([a-z]+)="([^"]*)"/g)) {
        if (!attributes[shape[1]].includes(name) || seen.has(name) ||
            (name === 'd' ? !value.trim() || !pathData.test(value) :
              ['fill', 'stroke'].includes(name) ? !['none', 'currentColor'].includes(value) : !numeric.test(value) || !Number.isFinite(Number(value)))) {
          throw new Error('Unsupported icon attribute: ' + id);
        }
        seen.add(name);
      }
      if (shape[1] === 'path' && !seen.has('d')) throw new Error('Missing icon path: ' + id);
    }
    if (end !== svg.length) throw new Error('Unsupported icon markup: ' + id);
  }
}

export function validatePrayerIcons(icons) {
  if (!icons || Object.keys(icons).sort().join(',') !== 'prayers,size,strokeWidth' ||
      !Number.isFinite(icons.size) || icons.size <= 0 || icons.size > 256 || !Number.isFinite(icons.strokeWidth) || icons.strokeWidth <= 0 || icons.strokeWidth > icons.size ||
      !Array.isArray(icons.prayers) || icons.prayers.map(prayer => prayer?.id).join(',') !== 'fajr,sunrise,dhuhr,asr,maghrib,isha' ||
      icons.prayers.some(prayer => Object.keys(prayer).sort().join(',') !== 'id,path' || typeof prayer.path !== 'string' || !prayer.path.trim() || !pathData.test(prayer.path))) {
    throw new Error('Invalid prayer icon order, geometry or dimensions');
  }
}
