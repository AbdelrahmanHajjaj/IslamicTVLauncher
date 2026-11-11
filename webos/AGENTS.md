# LG webOS port

Inherit the root `AGENTS.md`. Read `README.md` here for current hardware evidence,
settings, setup and limitations. `../docs/WEBOS-TIZEN-PORT-RESEARCH.md` preserves
earlier experiments; later versioned validation in this README takes precedence.

## Architecture and compatibility

- Target: LG LF6300, webOS 2.2 / WebKit 538.2, service Node 0.10.25. Build tools use
  Node 24. Shipped app **and** service code must parse as ES5 and avoid unavailable
  runtime APIs. A successful modern-browser run does not prove LG compatibility.
- `app/app.js`, `app/style.css`: separate TV renderer, D-pad/pointer handling, embedded
  input and audio. Keep Arabic/English together; check RTL and LTR.
- `core.js`: LG adapter shared between its frontend and service. Uses `shared/core`
  for offset validation and prayer selection; retains astronomy, preferences,
  time/date helpers and input/app allowlists.
- `service/service.js`: private atomic state, Activity Manager startup/prayer callbacks
  and allowlisted LG integration. Avoid duplicate or expired audible alerts.
- `build.cjs`: reads shared catalogs/icons/fonts/themes, plus Android audio and `site/` pattern tooling;
  transpiles adhan-js and emits literal CSS tokens. Never edit `build/` as source.
- Keep `package.json`, lockfile package version, `app/appinfo.json` and
  `service/package.json` versions aligned. Keep `tv.afnon.webos` and its service ID.
- `tv-apps.json` is a sanitized snapshot of installed app shortcuts, not live discovery.
  Public inventory/foreground APIs are unavailable on this firmware; do not ship an
  SSAP pairing credential or require the development PC for daily operation.

## Behavior to preserve

- HDMI is a video source inside our app. Silent banners keep picture/sound alive.
  Audible alerts release that decoder before playing local audio, then restore it.
- In-app tests return to the same screen, focused row and scroll position. An alert
  interrupting another app closes our window afterward so LG can restore its history.
  This is not a guaranteed app/playback-position snapshot. Keep the cold-start,
  expired-alert and foreground-return regressions.
- Startup launches once per new kernel boot, not every time the user closes the app.
  Prayer activities can relaunch a closed app while the TV is on. The JS service is
  demand-started; it is not an always-running global remote-key listener.
- No supported colored-button launch shortcut while closed, no system Home replacement,
  no arbitrary-app embedding, and no standby wake. Root/remapping compatibility remains
  unverified; do not apply Android accessibility/TCL commands or root-only recipes.
- Appearance uses inline Left/Right carousels, an unsaved draft, Apply and cancellation.
  Patterns belong on Home and its preview. Keep visible Back controls and pointer clicks.
- Old WebKit needs explicit SVG preview dimensions; `height:auto` was incorrect on TV.

## Build and validation

From the repository root:

```sh
npm --prefix site ci
npm --prefix webos ci
# Obtain local media first if absent; see tools/fetch-adhans.sh and tools/fetch-announce.sh.
npm --prefix webos test
npm --prefix webos run test:browser
node tools/webos-background-probe/test.cjs  # when touching the historical probe
```

The build needs JDK 17 for the shared core, plus the 14 adhan MP3s and 12 spoken recordings in Android's ignored
asset directories. Do not add them or built IPKs to Git. Browser checks use an
installed Microsoft Edge with a temporary profile; screenshots go under ignored
`state/webos-port/`. Luna and audio/video are mocked there. New scheduling changes
must cover asynchronous callbacks, overlap, expiry, duplication and cancellation.

Package/deploy with the user's configured webOS CLI device alias (see this README).
Use the current appinfo version for the IPK filename. Before upgrading an authorized
TV, capture preferences/input/startup to ignored local state; close, install over
the existing package, relaunch and compare. Do not uninstall merely to upgrade.
Keep private writable data under `/media/internal/tv.afnon.webos`; the installed
service directory becomes root-owned after reboot.

Use live service diagnostics and, when available, Web Inspector DOM reads. TV
screenshot APIs are not reliable here; get human confirmation for picture, sound,
remote behavior and power cycles. Run audible/power tests sequentially with a watcher.
The test activity is a bounded one-shot; cancel it if abandoning a pending test.
Do not run historical probes on a user's TV as routine validation.

## Publishing

Publish source, tests, lockfile and notices. Exclude `node_modules/`, `build/`, IPKs,
device state and media recordings. Android APK release steps in `../gh.md` do not
publish LG packages. A tested local development IPK is not an LG Store release.
