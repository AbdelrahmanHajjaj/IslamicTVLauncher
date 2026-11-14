# Local audio audit

Reviewed 13 November 2026: all 14 catalog adhan recordings and all 12 spoken
announcements. Audio, transcripts, originals and build outputs remain ignored.
This is recording maintenance, separate from the completed architecture work.

## Method and limits

- Decode every complete audio stream with FFmpeg 9.0.2, treating decode errors as
  failures. Inventory original duration and SHA-256 before making any edits.
- Screen all 50 minutes with local multilingual Whisper small. Separately screen
  each adhan's final 30 seconds, opening 8 seconds and two final 8-second windows.
  No recordings are uploaded. The isolated-window pass detects the known
  Al-Qatami promotion that the full-recitation transcription misses.
- Confirm the repeated spoken TVQuran site-credit tag using isolated transcripts
  and waveform matching. Cut within the measured silence after the final adhan
  words, before the credit starts. Do not change religious text or supplication.
- Compare every retained MP3 packet with the corresponding original packet:
  edits preserve the encoded audio prefix, without re-encoding or normalization.
  Unedited files must remain byte-identical. Decode the prepared set again.
- Transcription makes errors on melismatic/older recordings; it is screening,
  not a word-for-word religious-text certification or human listening to all files.
  Al-Qatami's ending was explicitly heard and accepted by the owner. Additional
  uncertain sections also received a local large-v3-turbo-q5_0 check. The owner
  listened to the four inconclusive endings on 14 November, confirmed they are
  correct and accepted keeping them unchanged. Findings below record
  detected promotion, not a guarantee that automated recognition never misses speech.

## Adhan findings

Durations are seconds, rounded to two decimals; exact approved cut points and
original SHA-256 values are in [adhan-edits.json](../shared/data/adhan-edits.json).

| Voice / stable ID | Original → prepared | Finding/action |
|---|---:|---|
| Mishary Alafasy / `alafasy` | 240.58 → 233.90 | Site-credit ending removed |
| Hamdan Al-Malki / `almalki` | 266.72 → unchanged | Owner reviewed ending; correct and preserved |
| Ibrahim Al-Arkani / `alarkani` | 240.60 → 233.90 | Site-credit ending removed |
| Malek Chebae, Fajr / `chebae_fajr` | 250.32 → 243.02 | Site-credit ending removed; Fajr wording retained |
| Majed Al-Hamathani / `alhamathani` | 226.03 → 218.51 | Site-credit ending removed |
| Mansoor Az-Zahrani / `alzahrani` | 190.76 → 183.51 | Site-credit ending removed; religious wording retained |
| Hamad Al-Deghreri / `deghreri` | 161.17 → 154.20 | Site-credit ending removed |
| Nasser Al-Qatami / `alqatami` | 139.38 → 132.02 | Site-credit ending removed at owner-approved cut |
| Suhaib Khatba / `khatba` | 183.10 → unchanged | No promotion detected; preserved |
| Mohammad Al-Menshawy / `menshawy` | 251.63 → unchanged | Owner reviewed ending; correct and preserved |
| Mohammad Refaat / `refaat` | 203.72 → unchanged | Owner reviewed ending; correct and preserved |
| Abdul Basit / `abdulbasit` | 200.84 → unchanged | No promotion detected; preserved |
| Ahmad Nuaina / `nuyne` | 210.12 → unchanged | Owner reviewed ending; correct and preserved |
| Makkah / `makkah` | 202.53 → unchanged | No promotion detected; preserved |

The source collection has some JPEG covers mislabeled as PNG. Prepared cuts copy
only the audio stream, omitting unused embedded artwork; originals retain it.
This avoids cover parsing/muxing errors without changing the sound. Those warnings
are distinguished from audio decode failures.

## Spoken announcements

All 12 clips decode, contain the expected short announcement with no detected
promotion, and remain byte-identical. Canonical wording remains in
[announce-lines.txt](../tools/announce-lines.txt); recognition spelling errors are
not substituted for that source.

| Prayer | Advance clip | At-time clip |
|---|---|---|
| Fajr | `before_fajr` | `now_fajr` |
| Dhuhr | `before_dhuhr` | `now_dhuhr` |
| Jumu'ah | `before_jumuah` | `now_jumuah` |
| Asr | `before_asr` | `now_asr` |
| Maghrib | `before_maghrib` | `now_maghrib` |
| Isha | `before_isha` | `now_isha` |

## Reproduction and handoff

`bash tools/fetch-adhans.sh` downloads missing sources and prepares reviewed edits.
For an existing cache, run `node tools/prepare-adhans.mjs` with Node 24 and FFmpeg/
ffprobe installed. Exact source hashes guard each edit; unknown input or a mismatched
backup fails without overwriting it. Originals and output-hash stamps live under
`state/media-originals/adhan/`, outside Android/LG packaged media.

Android's pre-build check and LG's build run the same read-only `--check` validator.
Neither build silently edits recordings. Empty optional Android media stays optional.
The focused `node tools/prepare-adhans.test.mjs` test uses synthetic audio, including
a deliberately mislabeled cover, and verifies unchanged frames, backups,
idempotence, read-only checks and rejection of unknown/unsafe edits.

All 26 originals and prepared files pass audio decoding. All seven cuts end in the
measured silent gaps and retain identical encoded MP3 packet prefixes; all other
19 files remain byte-identical. The synthetic preparation test, LG ES5/service/
volume/online and complete browser suites, shared/preview tests, and standalone
Android build/lint/25 MB budget check pass. Hardware playback is separate.

Raw inventory, transcripts, waveform/silence checks and prepared-file hashes are in
ignored `state/media-review/`. `uncertain-endings-review.mp3` contains 15 seconds
each of Al-Malki, Al-Menshawy, Refaat and Nuaina, in that order. The owner confirmed
all four endings correct on 14 November; preserve them unchanged. Conflicting
recognition alone is not evidence for a cut.
LG 0.2.18 was installed in place on 14 November with exact payload verification,
31 preferences/input/startup/boot preserved, and owner-confirmed short adhan,
picture/transparency and sound restoration. Al-Qatami's installed ending reaches
natural EOF and returns normally. The owner then requested absolute TV volume:
0.2.19 sets 30% to native level 30/100, restoring the captured previous level.
The owner confirms the installed absolute level and normal restoration. Version
0.2.20 retains these recordings and volume behavior while unifying numeric editor
rules in core and rendering LG numeric adjustments inline. It is installed with
103 exact payload matches and 93 core checks on both native Node and WebKit.
Temporary Al-Qatami/Chebae previews reach natural EOF at the current native level
25, with normal HDMI and level 50 restored. Actual inline save 25→30→25 preserves
all other preferences. The owner's current Al-Menshawy selections for both roles
remain unchanged. Final state is clean and the inspector is closed. Physical
post-0.2.20 picture/sound, inline-remote and ending feedback remains pending.

Verified local candidate: `state/media-review/lg-0220-candidate/tv.afnon.webos_0.2.20_all.ipk`,
18,275,282 bytes, SHA-256
`7ce5101cd6d315a5561a41ab8ea092adc582b745a6144497058c838072c59dfe`.
All 103 payload files match the tested build; only the shared core, numeric
renderer and app/service version metadata differ from installed 0.2.19.
All recordings and the verified absolute-volume service are unchanged.
All 26 packaged recordings match the shared cache in both IPK and APK; no originals,
review clips or preparation stamps are bundled. Android APK is 20,957,070 bytes,
within its 25 MB budget. Detailed manifests and test logs stay in `state/media-review/`.
