# Shared catalogs

`cities.json` and `regions.json` are canonical inputs for Android, LG and the browser
preview. Keep existing city IDs and ordering so upgrades retain selected locations;
Cairo is the fallback. The migration's remaining catalogs and decisions are tracked
only in [`docs/ARCHITECTURE-QUEUE.md`](../../docs/ARCHITECTURE-QUEUE.md).

Run `node tools/catalogs.mjs` from the repository root to regenerate Android's
`CityCatalog.java`, or `npm --prefix site run gen` for all current generators.
`node tools/catalogs.mjs --check` checks without writing. `npm --prefix site test`
checks catalog validation, Java data fidelity and the preview behavior.

The Android `Prefs.City` interface and persisted setting IDs are unchanged. The
preview and LG read JSON directly. Prayer-method adapters are still native during
this migration slice; the generator validates city methods against that adapter.
