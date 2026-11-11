# Shared catalogs

`cities.json`, `regions.json` and `methods.json` are canonical inputs for Android,
LG and the browser preview. Keep existing city/method IDs and ordering so upgrades retain selected locations;
Cairo is the fallback. The migration's remaining catalogs and decisions are tracked
only in [`docs/ARCHITECTURE-QUEUE.md`](../../docs/ARCHITECTURE-QUEUE.md).

Run `node tools/catalogs.mjs` from the repository root to regenerate Android's
`CityCatalog.java` and `PrayerMethod.java`, or `npm --prefix site run gen` for all current generators.
`node tools/catalogs.mjs --check` checks without writing. `npm --prefix site test`
checks catalog validation, Java data fidelity and all method parameters against
the preserved adapter baseline.

The Android `Prefs.City` interface and persisted setting IDs are unchanged. The
preview and LG read JSON directly. `methods.json` owns ordered IDs, bilingual
labels, custom angles/intervals and method corrections. Built-in calculation
behavior stays in adhan-java/adhan-js. Edit Android adapter code in
`app/src/main/templates/PrayerMethod.java.template`, then regenerate; do not edit
the generated enum.
