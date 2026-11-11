<!-- Arabic or English, either is fine. يمكنك الكتابة بالعربية أو الإنجليزية. -->

## What and why

<!-- One topic per PR. Link the issue if there is one: Fixes #123 -->

## Tested on

<!-- Platform, TV model, OS/firmware and tested app version. Distinguish physical TV,
     live DOM/service diagnostics and mocked browser tests. Say "Build only" when applicable. -->

## Screenshots

<!-- Arabic and English when layout changed. Android: tools/shot.sh. LG: label desktop
     previews honestly; webOS 2.2 screenshot APIs are unreliable. Remove personal content. -->

## Checklist

- [ ] Relevant checks pass: Android Gradle/regressions, LG `npm --prefix platforms/webos test`
      and `npm --prefix platforms/webos run test:browser`, or shared-data/site tests. Mark N/A explicitly.
- [ ] Arabic and English are checked in RTL/LTR; Android strings use `Text.java`,
      LG uses shared strings and bilingual labels in `platforms/webos/app/app.js`.
- [ ] Colours, sizes and timings come from `afnon-mihrab/tokens.json` (`npm run tokens` in `site/`).
- [ ] D-pad focus works from every edge, and Back from every screen.
- [ ] Dependencies, timers, services and network changes are documented. Android's
      shared timer policy and LG's Activity Manager lifecycle are preserved.
- [ ] Quranic text and prayer names are exact (surah:ayah given).
- [ ] A shared/Android layout change checks the preview and LG renderer as applicable;
      platform-only fixes are identified as such.
- [ ] `CHANGELOG.md` has a line under Unreleased.
- [ ] No personal data in screenshots/logs, pairing credentials, `state/`,
      `local.properties`, APK/IPK/build output or non-redistributable audio.
