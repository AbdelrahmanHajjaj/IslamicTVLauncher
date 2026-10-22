<!-- Arabic or English, either is fine. يمكنك الكتابة بالعربية أو الإنجليزية. -->

## What and why

<!-- One topic per PR. Link the issue if there is one: Fixes #123 -->

## Tested on

<!-- TV model, Android version. "Build only" if you couldn't test on a TV. -->

## Screenshots

<!-- One in Arabic and one in English if the layout changed (tools/shot.sh). -->

## Checklist

- [ ] `./gradlew assembleDebug` builds with no new warnings.
- [ ] Strings are in `Text.java` in Arabic and English; checked in RTL and LTR.
- [ ] Colours, sizes and timings come from `afnon-mihrab/tokens.json` (`npm run tokens` in `site/`).
- [ ] D-pad focus works from every edge, and Back from every screen.
- [ ] No new dependencies, timers, services or network access.
- [ ] Quranic text and prayer names are exact (surah:ayah given).
- [ ] If a screen's layout changed, its component in `site/src/screens/` is updated too.
- [ ] `CHANGELOG.md` has a line under Unreleased.
- [ ] No personal data in screenshots or logs (Wi-Fi names, IPs, serials), and no `state/` or `local.properties`.
