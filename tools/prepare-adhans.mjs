import { constants, copyFileSync, existsSync, mkdirSync, readFileSync, renameSync, rmSync, writeFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const project = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const sha256 = file => createHash('sha256').update(readFileSync(file)).digest('hex');
const json = file => JSON.parse(readFileSync(file, 'utf8'));

export function prepareAdhans(root = project, check = false) {
  const edits = json(join(root, 'shared/data/adhan-edits.json'));
  const voices = new Set(json(join(root, 'shared/data/voices.json')).map(v => v.id));
  const originals = join(root, 'state/media-originals/adhan');
  for (const [id, edit] of Object.entries(edits)) {
    if (!/^[a-z0-9_]+$/.test(id) || !voices.has(id) || !/^[a-f0-9]{64}$/.test(edit.sourceSha256) ||
        !Number.isFinite(edit.endSeconds) || edit.endSeconds <= 0) throw new Error('Invalid adhan edit: ' + id);
    const input = join(root, 'state/media/adhan', id + '.mp3');
    if (!existsSync(input)) continue; // Android permits builds without optional recordings.
    const original = join(originals, id + '.mp3');
    const stamp = join(originals, id + '.json');
    const currentHash = sha256(input);
    if (existsSync(stamp)) {
      const saved = json(stamp);
      if (saved.sourceSha256 === edit.sourceSha256 && saved.endSeconds === edit.endSeconds && saved.outputSha256 === currentHash) continue;
    }
    const help = id + ': run node tools/prepare-adhans.mjs (FFmpeg required)';
    if (check) throw new Error('Unprepared adhan recording: ' + help);
    if (currentHash !== edit.sourceSha256) throw new Error('Unknown recording; refusing to trim or overwrite: ' + id);
    mkdirSync(originals, {recursive: true});
    if (!existsSync(original)) copyFileSync(input, original, constants.COPYFILE_EXCL);
    if (sha256(original) !== edit.sourceSha256) throw new Error('Original backup mismatch: ' + id);
    const output = join(originals, id + '.prepared.mp3');
    const pendingStamp = stamp + '.tmp';
    try {
      // Some source files mislabel JPEG covers as PNG; package only the unchanged audio stream.
      execFileSync('ffmpeg', ['-v', 'error', '-y', '-i', original, '-t', String(edit.endSeconds), '-map', '0:a:0', '-vn', '-c:a', 'copy', output], {stdio: 'pipe'});
      const probe = JSON.parse(execFileSync('ffprobe', ['-v', 'error', '-show_format', '-show_streams', '-of', 'json', output], {encoding: 'utf8'}));
      if (!probe.streams.some(s => s.codec_type === 'audio') || Math.abs(+probe.format.duration - edit.endSeconds) > 0.1) throw new Error('Unexpected trimmed duration: ' + id);
      execFileSync('ffmpeg', ['-v', 'error', '-xerror', '-i', output, '-map', '0:a:0', '-vn', '-f', 'null', '-'], {stdio: 'pipe'});
      const result = {...edit, outputSha256: sha256(output)};
      writeFileSync(pendingStamp, JSON.stringify(result, null, 2) + '\n');
      renameSync(output, input);
      renameSync(pendingStamp, stamp);
      console.log('Prepared ' + id + '; original retained outside packaged assets.');
    } finally {
      rmSync(output, {force: true});
      rmSync(pendingStamp, {force: true});
    }
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  if (process.argv.slice(2).some(arg => arg !== '--check')) throw new Error('Usage: node tools/prepare-adhans.mjs [--check]');
  prepareAdhans(project, process.argv.includes('--check'));
}
