const token = /\{([a-zA-Z][a-zA-Z0-9]*)\}/g;
const params = value => [...value.matchAll(token)].map(match => match[1]);

export function validateSacredText(text) {
  if (!text || Object.keys(text).sort().join(',') !== 'adhanCall,prayerVerse' || !Array.isArray(text.prayerVerse) || text.prayerVerse.length !== 3 ||
      [...text.prayerVerse, text.adhanCall].some(value => typeof value !== 'string' || !value.trim() || !/^[\u0600-\u06ff ]+$/.test(value))) {
    throw new Error('Invalid sacred text or verse line breaks');
  }
}

export function validateStrings(strings) {
  if (!strings || typeof strings !== 'object' || Array.isArray(strings) || !Object.keys(strings).length) throw new Error('Missing text catalog');
  for (const [key, entry] of Object.entries(strings)) {
    if (!/^[a-z][a-zA-Z0-9]*$/.test(key) || !entry || typeof entry !== 'object' ||
        Object.keys(entry).some(key => !['ar', 'en', 'd'].includes(key)) ||
        entry.d !== undefined && typeof entry.d !== 'boolean') throw new Error('Invalid text entry: ' + key);
    const array = Array.isArray(entry.ar);
    if (array !== Array.isArray(entry.en) || array && (!entry.ar.length || entry.ar.length !== entry.en.length || entry.d)) throw new Error('Invalid text list: ' + key);
    const values = array ? [...entry.ar, ...entry.en] : [entry.ar, entry.en];
    if (values.some(value => typeof value !== 'string' || !value.length || /[\u0000-\u0008\u000b-\u001f]/.test(value) ||
        /[{}]/.test(value.replace(token, '')) || array && params(value).length)) throw new Error('Invalid text value: ' + key);
    if (!array && JSON.stringify([...new Set(params(entry.ar))].sort()) !== JSON.stringify([...new Set(params(entry.en))].sort())) {
      throw new Error('Mismatched text parameters: ' + key);
    }
  }
}

function expression(value) {
  if (Array.isArray(value)) return 'new String[] {' + value.map(text => JSON.stringify(text)).join(', ') + '}';
  return value.split(/(\{[a-zA-Z][a-zA-Z0-9]*\})/g)
    .filter((part, i) => part.length || i === 0)
    .map(part => /^\{\w+\}$/.test(part) ? part.slice(1, -1) : JSON.stringify(part)).join(' + ');
}

export function validateFormatText(format) {
  if (!format || Object.keys(format).sort().join(',') !== 'ar,en') throw new Error('Missing formatter locales');
  for (const entries of Object.values(format)) {
    if (!entries || typeof entries !== 'object' || Array.isArray(entries) || !Object.keys(entries).length) throw new Error('Missing formatter entries');
    for (const [key, value] of Object.entries(entries)) {
      const values = Array.isArray(value) ? value : [value];
      if (!/^[a-z][a-zA-Z0-9]*$/.test(key) || !values.length || values.some(text => typeof text !== 'string' || !text.length || /[\u0000-\u0008\u000b-\u001f]/.test(text))) {
        throw new Error('Invalid formatter literal: ' + key);
      }
    }
  }
}

export function renderLiterals(format, template) {
  validateFormatText(format);
  const seen = new Set();
  const output = template.replace(/\{\{TEXT:(ar|en):(\w+)\}\}/g, (_, lang, key) => {
    const value = format[lang][key];
    if (value === undefined) throw new Error('Missing formatter literal: ' + lang + ':' + key);
    seen.add(lang + ':' + key);
    return Array.isArray(value) ? 'new String[] {' + value.map(text => JSON.stringify(text)).join(', ') + '}' : JSON.stringify(value);
  });
  if (seen.size !== Object.values(format).reduce((count, entries) => count + Object.keys(entries).length, 0) || output.includes('{{TEXT:')) {
    throw new Error('Formatter catalog and adapter markers differ');
  }
  return output;
}

export function renderText(strings, template, format) {
  validateStrings(strings);
  if (format) template = renderLiterals(format, template);
  const seen = new Set();
  const output = template.replace(/\{\{STRINGS:(\w+)\}\}/g, (_, key, at) => {
    if (!strings[key] || seen.has(key)) throw new Error('Missing or duplicate text marker: ' + key);
    seen.add(key);
    const signature = template.slice(template.lastIndexOf('\n', at) + 1, at);
    const names = /\(([^)]*)\)/.exec(signature)?.[1].split(',').map(param => param.trim().split(/\s+/).pop()) ?? [];
    const entry = strings[key];
    if (typeof entry.ar === 'string' && params(entry.ar).some(name => !names.includes(name))) throw new Error('Undeclared text parameter: ' + key);
    const part = value => entry.d ? 'd(' + expression(value) + ')' : expression(value);
    return 'return ar ? ' + part(entry.ar) + ' : ' + part(entry.en) + ';';
  });
  if (seen.size !== Object.keys(strings).length || output.includes('{{STRINGS:')) throw new Error('Text catalog and adapter markers differ');
  return '// Generated from shared/data/{strings,format-text}.json and Text.java.template by tools/catalogs.mjs. Do not edit.\n' + output;
}
