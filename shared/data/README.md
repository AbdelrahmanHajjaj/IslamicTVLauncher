# Shared catalogs

`cities.json`, `regions.json`, `methods.json`, `stations.json` and `voices.json` are canonical inputs for Android,
LG and the browser preview. Keep existing city/method IDs and ordering so upgrades retain selected locations;
Cairo is the fallback. The migration's remaining catalogs and decisions are tracked
only in [`docs/ARCHITECTURE-QUEUE.md`](../../docs/ARCHITECTURE-QUEUE.md).

Run `node tools/catalogs.mjs` from the repository root to regenerate Android's
`CityCatalog.java`, `PrayerMethod.java`, `RadioCatalog.java`, `VoiceCatalog.java` and
`tools/adhan-sources.tsv`, or `npm --prefix site run gen` for all current generators.
`node tools/catalogs.mjs --check` checks without writing. `npm --prefix site test`
checks catalog validation, Java data fidelity and all method parameters against
the preserved adapter baseline.

The Android `Prefs.City` interface and persisted setting IDs are unchanged. The
preview and LG read JSON directly. `methods.json` owns ordered IDs, bilingual
labels, custom angles/intervals and method corrections. Built-in calculation
behavior stays in adhan-java/adhan-js. Edit Android adapter code in
`app/src/main/templates/PrayerMethod.java.template`, then regenerate; do not edit
the generated enum.

`stations.json` preserves radio/reciter IDs, order, exact stream URLs, bilingual
names/locations and the grouping flag. Cairo remains the fallback. `Radio.Station`
and playback stay native; changing a catalog does not validate a stream's TLS or
codec on any TV. Broadcast streams come from the stations' players; reciters use
mp3quran.net's 24/7 channels. Sharjah remains excluded because its raw ADTS AAC
stream never starts with Android's MediaPlayer, and its official HLS is video.

`voices.json` owns the ordered adhan IDs/names and original download filenames.
The generated TSV lets `tools/fetch-adhans.sh` keep working with Bash/curl alone.
Local assets remain `adhan/<id>.mp3`; regular/Fajr defaults remain `alafasy` and
`chebae_fajr`. The recordings' rights status is unchanged (see `THIRD_PARTY.md`);
do not commit the MP3s.
