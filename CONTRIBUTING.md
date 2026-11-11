# Contributing to Islamic TV Launcher · المساهمة في المشروع

Thank you for helping. Afnon is a home screen people look at five times a day, at prayer time, so
the bar is calm, correct and reliable. A wrong Fajr time matters more than a missing feature.

<p dir="rtl">شكرًا لمساهمتك. أفنون شاشة ينظر إليها الناس خمس مرات في اليوم عند كل صلاة، لذلك نريدها
هادئة ودقيقة ويُعتمد عليها؛ خطأ في وقت الفجر أهم من أي ميزة ناقصة. يمكنك فتح المشكلات وطلبات الدمج
بالعربية أو الإنجليزية.</p>

Issues and pull requests are welcome in Arabic or English.

This fork also includes a separate LG webOS application in `platforms/webos/`. Read
[`AGENTS.md`](AGENTS.md) for the platform map and [`platforms/webos/AGENTS.md`](platforms/webos/AGENTS.md)
for LG maintenance rules. The Java, ADB, accessibility-service and APK instructions
below apply to Android. The LG port uses ES5, a webOS JS service and IPK packages;
its build shares catalogs, design and assets with Android; its UI and system integrations are separate.
The shared core targets JVM and ES5. Building the preview or LG also compiles that
core with the repository's Gradle wrapper: install JDK 17 and Node 24 first. The
preview consumes it as a local package; LG packages the compiler's UMD files.

The native shell is in [`platforms/android-tv/`](platforms/android-tv/README.md).
With JDK 17 and `ANDROID_HOME` set to SDK 34, run
`./gradlew -p platforms/android-tv verifyApkSize lintDebug` independently. Root
`:app` tasks still work alongside `:installer`; neither build changes installed IDs.

Changes target this project's `main`. Read the [architecture](docs/ARCHITECTURE.md)
for implemented boundaries and the [durable queue](docs/ARCHITECTURE-QUEUE.md) for the
active migration scope/evidence. The [upstream contribution plan](docs/CONTRIBUTION-PLAN.md)
is historical; it does not authorize new upstream submissions or publication.

## 1. Add your city

Cities live in `shared/data/cities.json`, each with Arabic and English names,
coordinates, a calculation method and an Asr-school default. Region membership
lives in `shared/data/regions.json`.

1. Add a city beside its country's other cities, keeping existing IDs and ordering.
   Run `npm --prefix site run gen` to regenerate Android's `CityCatalog.java`, then
   `npm --prefix site test` and `npm --prefix site run data:check`. Do not edit the
   generated Java catalog. Cairo remains the fallback for old/unknown city settings.
2. Use the method your local religious authority publishes, for example Egyptian General Authority
   of Survey for Egypt, or Umm al-Qura for Saudi Arabia.
3. In the PR, compare Afnon's six times for one day with an official source (a ministry or awqaf
   site, or a local masjid's timetable) and link it. Within a minute or two is expected; say if
   anything is further off.

## 2. Add your TV

The current Android branch has historical TCL 50C641K / Android 11 evidence; upstream
reported TCL 55P7K / Android 12. Current migration hardware acceptance is tracked in
the queue. If you run it on another TV,
tell us how it went, even if everything worked:

- Model, Android version and RAM.
- Whether Home, the remote buttons service, the screensaver and the call screen survive a reboot.
- What the remote's special buttons send. `adb shell getevent -l` shows the codes; add them to the
  table in `RemoteKeys.java`.
- If your brand has its own way to turn the panel off while sound plays (like TCL's Audio Only),
  add it to `ScreenOffActivity.audioOnly()` behind a check that fails quietly elsewhere.

## 3. Improve the launcher

The Android UI stays in Java and Android Views; astronomy uses adhan-java and shared
product rules use the Kotlin core. No AndroidX or Compose. It has to stay smooth on
a TV with under 2 GB of RAM, so:

- **No blur, video backgrounds or shadows.** Animate with transform and opacity only.
- **No new timers or background services.** Prayer checks share one 15-second timer in the
  accessibility service.
- **No new network access.** The radio stream is the only connection. No analytics, no ads, no
  feeds, no "recommended" rows.
- **Design tokens.** Colours, sizes and timings live in `afnon-mihrab/tokens.json`. `Tokens.java`,
  `colors.xml` and the preview site's CSS are generated from it: edit the JSON, then run `npm run tokens`
  in `site/` (Node 24). Never edit the generated files; CI checks they match. Plain bilingual text
  comes from `shared/data/strings.json`, with formatter literals in `shared/data/format-text.json`.
  Formatting logic lives in `platforms/android-tv/src/main/templates/Text.java.template`.
- **Arabic and English together.** Every string has both. Check the layout in RTL and in LTR.
  Arabic gets no letter-spacing. Quranic text uses Amiri.
- **D-pad focus is explicit.** Each screen has a focus map, with Left and Right swapped in RTL. Test
  Up, Down, Left and Right from every edge, and Back from every screen.
- **Verify on a real TV.** Use the test hooks in the README (`--es test friday|ramadan|call|…`) and
  `tools/shot.sh`. Put screenshots in the PR, one in Arabic and one in English if layout changed.

**The web preview** (`site/`) is an Astro site that copies each screen for the browser. It reads
`shared/data/` catalogs/text directly; fonts and artwork are still
extracted at build time (`site/scripts/extract.mjs`). If you change a screen's layout, change its component in
`site/src/screens/` too: `cd site && npm install && npm run dev`.

Good fits: more cities and methods, an Inputs screen, accessibility of the call screen, more
reciters with clear licences, other brands' screen-off modes.

Not a fit: content feeds, ads, telemetry, account sign-ins, anything that phones home.

## 4. Religious content

Quranic verses, adhan wording and prayer names must be exact. For a verse, give the surah and ayah
number, and copy it from a standard mushaf text (for example from tanzil.net). If you are unsure
about a ruling (Asr schools, high-latitude rules, Hijri sighting), open an issue first rather than
choosing for everyone. Afnon gives users the choice wherever scholars differ.

## 5. Audio

Only add recordings you have the right to share, and say where each one comes from and under what
licence. Local recordings go in ignored `state/media/adhan/`; metadata and download filenames
belong in `shared/data/voices.json`. Run `node tools/catalogs.mjs` to update the
native catalog and fetch list. Do not commit recordings without permission to distribute them.

## Pull requests

- One topic per PR. Small is fine.
- Say which TV you tested on, and attach screenshots.
- Android changes: `./gradlew :app:assembleDebug` must build with no new warnings.
- LG changes: run `npm --prefix platforms/webos test` and `npm --prefix platforms/webos run test:browser`
  with the prerequisites in [`platforms/webos/README.md`](platforms/webos/README.md#build-and-check).
  Separate mocked browser evidence from actual TV picture/sound and remote checks.
  A desktop preview is not a screenshot of the TV. Run probe tests only when changing
  `tools/webos-background-probe/`; do not install a historical probe as routine QA.
- **No personal data.** Before you push, check screenshots and logs for your Wi-Fi name, IP
  addresses, serial numbers, your home city or room direction if you'd rather keep them private,
  and other apps' content playing behind Afnon. Never commit `state/`, `local.properties`,
  pairing credentials, APK/IPK output or downloaded/generated speech recordings.
- Be kind in reviews.

By contributing you agree that your work is released under the repo's MIT licence.
