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
  uncertain sections also received a local large-v3-turbo-q5_0 check. Four endings
  remain inconclusive across models; preserve them pending human listening.
  Findings below record
  detected promotion, not a guarantee that automated recognition never misses speech.

## Adhan findings

Durations are seconds, rounded to two decimals; exact approved cut points and
original SHA-256 values are in [adhan-edits.json](../shared/data/adhan-edits.json).

| Voice / stable ID | Original → prepared | Finding/action |
|---|---:|---|
| Mishary Alafasy / `alafasy` | 240.58 → 233.90 | Site-credit ending removed |
| Hamdan Al-Malki / `almalki` | 266.72 → unchanged | Conflicting recognition; human ending review pending |
| Ibrahim Al-Arkani / `alarkani` | 240.60 → 233.90 | Site-credit ending removed |
| Malek Chebae, Fajr / `chebae_fajr` | 250.32 → 243.02 | Site-credit ending removed; Fajr wording retained |
| Majed Al-Hamathani / `alhamathani` | 226.03 → 218.51 | Site-credit ending removed |
| Mansoor Az-Zahrani / `alzahrani` | 190.76 → 183.51 | Site-credit ending removed; religious wording retained |
| Hamad Al-Deghreri / `deghreri` | 161.17 → 154.20 | Site-credit ending removed |
| Nasser Al-Qatami / `alqatami` | 139.38 → 132.02 | Site-credit ending removed at owner-approved cut |
| Suhaib Khatba / `khatba` | 183.10 → unchanged | No promotion detected; preserved |
| Mohammad Al-Menshawy / `menshawy` | 251.63 → unchanged | Low-fidelity recording; human ending review pending |
| Mohammad Refaat / `refaat` | 203.72 → unchanged | Low-fidelity recording; human ending review pending |
| Abdul Basit / `abdulbasit` | 200.84 → unchanged | No promotion detected; preserved |
| Ahmad Nuaina / `nuyne` | 210.12 → unchanged | Conflicting recognition; human ending review pending |
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
each of Al-Malki, Al-Menshawy, Refaat and Nuaina, in that order. It was offered for
human confirmation; the owner deferred it to 14 November and explicitly asked to
preserve these four. Do not trim them based on conflicting recognition.
LG 0.2.18 is prepared for a watched in-place
upgrade on 14 November; the TV remains on accepted 0.2.17 until then. Capture fresh
preferences/input/startup/volume state before that upgrade. Retain package/service
IDs, saved voice IDs and settings; no runtime playback/UI change is included.

Verified local candidate: `state/media-review/lg-0218-candidate/tv.afnon.webos_0.2.18_all.ipk`,
18,274,728 bytes, SHA-256
`2ece17fb1fc757b361c8d367d0c34a55803bb68a9dfbf9718d886b95bfc03b99`.
All 103 payload files match the tested build; only the seven edited recordings,
app/service version metadata and media attribution differ from accepted 0.2.17.
All 26 packaged recordings match the shared cache in both IPK and APK; no originals,
review clips or preparation stamps are bundled. Android APK is 21,159,759 bytes,
within its 25 MB budget. Detailed manifests and test logs stay in `state/media-review/`.
