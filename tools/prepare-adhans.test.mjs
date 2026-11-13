import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, mkdtempSync, readFileSync, realpathSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { prepareAdhans } from './prepare-adhans.mjs';

const scratch = realpathSync(tmpdir());
const root = mkdtempSync(join(scratch, 'afnon-audio-'));
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
const writeJson = (file, value) => writeFileSync(file, JSON.stringify(value));
try {
  mkdirSync(join(root, 'shared/data'), {recursive: true});
  mkdirSync(join(root, 'state/media/adhan'), {recursive: true});
  const input = join(root, 'state/media/adhan/test.mp3');
  const manifest = join(root, 'shared/data/adhan-edits.json');
  writeJson(join(root, 'shared/data/voices.json'), [{id: 'test'}]);
  writeJson(manifest, {test: {sourceSha256: '0'.repeat(64), endSeconds: 1.2}});
  prepareAdhans(root, true); // Missing optional media stays optional.
  const tone = join(root, 'tone.mp3');
  const cover = join(root, 'cover.jpg');
  execFileSync('ffmpeg', ['-v', 'error', '-f', 'lavfi', '-i', 'sine=frequency=440:duration=3', '-c:a', 'libmp3lame', tone]);
  execFileSync('ffmpeg', ['-v', 'error', '-f', 'lavfi', '-i', 'color=c=red:s=16x16', '-frames:v', '1', cover]);
  execFileSync('ffmpeg', ['-v', 'error', '-i', tone, '-i', cover, '-map', '0:a', '-map', '1:v', '-c', 'copy', '-id3v2_version', '3', '-disposition:v', 'attached_pic', input]);
  const original = readFileSync(input);
  const mime = original.indexOf(Buffer.from('image/jpeg'));
  assert.ok(mime >= 0);
  original.write('image/png\0', mime, 'ascii'); // Reproduce the source collection's mislabeled cover.
  writeFileSync(input, original);
  const edit = {sourceSha256: hash(original), endSeconds: 1.2};
  writeJson(manifest, {test: edit});
  assert.throws(() => prepareAdhans(root, true), /Unprepared adhan/);
  assert.ok(!existsSync(join(root, 'state/media-originals')), 'check mode must not write');
  prepareAdhans(root);
  const backup = join(root, 'state/media-originals/adhan/test.mp3');
  const stamp = join(root, 'state/media-originals/adhan/test.json');
  assert.deepEqual(readFileSync(backup), original, 'original bytes retained outside assets');
  const cleaned = readFileSync(input);
  assert.ok(cleaned.length < original.length);
  const packets = file => JSON.parse(execFileSync('ffprobe', ['-v', 'error', '-select_streams', 'a:0', '-show_packets', '-show_data_hash', 'sha256', '-show_entries', 'packet=data_hash', '-of', 'json', file], {encoding: 'utf8'})).packets.map(p => p.data_hash);
  const kept = packets(input);
  assert.deepEqual(kept, packets(backup).slice(0, kept.length), 'encoded audio prefix must be unchanged');
  const before = [input, backup, stamp].map(p => statSync(p).mtimeMs);
  prepareAdhans(root, true);
  prepareAdhans(root);
  assert.deepEqual([input, backup, stamp].map(p => statSync(p).mtimeMs), before, 'idempotent preparation and read-only build check');
  writeFileSync(input, 'unrecognized replacement');
  assert.throws(() => prepareAdhans(root, true), /Unprepared adhan/);
  assert.throws(() => prepareAdhans(root), /Unknown recording/);
  assert.equal(readFileSync(input, 'utf8'), 'unrecognized replacement');
  assert.deepEqual(readFileSync(backup), original);
  writeFileSync(input, original);
  writeFileSync(backup, 'wrong backup');
  assert.throws(() => prepareAdhans(root), /backup mismatch/);
  assert.deepEqual(readFileSync(input), original, 'bad backup cannot overwrite input');
  writeJson(manifest, {'../outside': edit});
  assert.throws(() => prepareAdhans(root), /Invalid adhan edit/);
  writeJson(manifest, {test: {...edit, endSeconds: -1}});
  assert.throws(() => prepareAdhans(root), /Invalid adhan edit/);
  console.log('PASS: audio cuts retain encoded frames/originals; checks are read-only; preparation is idempotent and rejects unknown input/unsafe edits.');
} finally {
  assert.equal(dirname(realpathSync(root)), scratch);
  rmSync(root, {recursive: true, force: true});
}
