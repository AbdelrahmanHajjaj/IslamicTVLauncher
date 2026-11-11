# Third-party material

The MIT licence in `LICENSE` retains the original Afnon copyright and covers this
project's code contributions. These parts belong to others.

| What | Where | Licence |
|---|---|---|
| [adhan-java 1.2.1](https://github.com/batoulapps/adhan-java/tree/eefc4ed1b910ec144dff247f45b2b23a16c7d0c2) by Batoul Apps | Android TV Gradle dependency | MIT; full text/source reference in `assets/licenses/adhan-java-{LICENSE,SOURCE}.txt` |
| [Kotlin standard library 2.2.10](https://github.com/JetBrains/kotlin/tree/v2.2.10) by JetBrains and contributors | Shared JVM/JS core runtime, bundled in Android, LG app/service and preview | Apache 2.0 and the upstream standard-library third-party notices; full texts packaged under Android `assets/licenses/kotlin/`, LG app/service `licenses/kotlin/` and preview `licenses/kotlin/` |
| [JetBrains annotations 13.0](https://repo1.maven.org/maven2/org/jetbrains/annotations/13.0/annotations-13.0.pom) | Transitive Kotlin JVM runtime dependency | Apache 2.0; license and source attribution under `assets/licenses/kotlin/` |
| [adhan-js](https://github.com/batoulapps/adhan-js) by Batoul Apps | Direct dependency of preview and LG; bundled as ES5 in LG | MIT; full notice included in the package |
| [libadb-android 3.1.1](https://github.com/MuntashirAkon/libadb-android/tree/c849886ebc6d48e7b46d967e78a6bb65c90c3b74) by Muntashir Al-Islam | Existing phone installer ADB client | Apache-2.0 option of its dual licence, with upstream COPYING/component texts under installer `assets/licenses/libadb/` |
| [spake2-java / spake2-android 2.2.1](https://github.com/MuntashirAkon/spake2-java/tree/7615ddd680b990e14513ebb66eac4cb0dbf82464) and [native submodule](https://github.com/MuntashirAkon/spake2-c/tree/0d15933e5ba3e662cb01245a7ac0dc9fca3eac31) | Existing transitive installer pairing dependency | LGPL-3.0, with retained LGPL-2.1-or-later/ISC/MIT/component attribution; full texts and pinned source/build references under installer `assets/licenses/` |
| [Conscrypt Android 2.5.3](https://github.com/google/conscrypt/tree/fd209de6167201e4f3bca5b99c6db9f08d1843c6) by the Android Open Source Project | Existing installer TLS provider | Apache-2.0; full LICENSE, NOTICE and Harmony/Netty texts under installer `assets/licenses/conscrypt/` |
| [BoringSSL](https://github.com/google/boringssl/tree/49f0329110a1d93a5febc2bceceedc655d995420), revision recorded in that Conscrypt AAR | Existing native Conscrypt component | Full BoringSSL/OpenSSL/ISC/component text under installer `assets/licenses/conscrypt/boringssl/` |
| [Amiri](https://github.com/aliftype/amiri) by Khaled Hosny | `shared/design/resources/font/amiri_*.ttf` | SIL Open Font Licence 1.1 |
| [Alexandria](https://fonts.google.com/specimen/Alexandria) by Mohamed Gaber and Julieta Ulanovsky | `shared/design/resources/font/alexandria_*.ttf` | SIL Open Font Licence 1.1 |
| [Marcellus](https://fonts.google.com/specimen/Marcellus) by Astigmatic | `shared/design/resources/font/marcellus_regular.ttf` | SIL Open Font Licence 1.1 |
| Adhan recordings from [abodehq/Athan-MP3](https://github.com/abodehq/Athan-MP3) | Not in the source. `tools/fetch-adhans.sh` downloads them; release APKs include them | None stated. Credited here; any recording is removed on request from its rights holder. |
| Spoken reminder lines (`tools/announce-lines.txt`) voiced by Microsoft's ar-SA-HamedNeural voice through [edge-tts](https://github.com/rany2/edge-tts) | Not in the source. `tools/fetch-announce.sh` records them; release APKs include them | Microsoft's speech service terms. Replaced on request. |
| Quran radio stations: إذاعة القرآن الكريم in Cairo, Nablus, Qatar (two channels) and Jordan, and Shabab FM Quran (Nablus) | Played from each station's public stream, never stored | Belongs to each station. Afnon is not affiliated with any of them. |
| Reciter channels from [mp3quran.net](https://mp3quran.net) (`backup.qurango.net`) | Played from the public stream, never stored | Belongs to mp3quran.net and the reciters. Afnon is not affiliated with it. |

The local LG webOS development package also copies the existing adhan and spoken
announcement assets, with the same source and rights status stated above; it does
not add them to version control. Both platforms read ignored `state/media/` inputs.
Voice metadata comes from `shared/data/voices.json`;
its radio list comes from `shared/data/stations.json`.

The stream addresses are in `shared/data/stations.json`. A station or reciter is removed on request from its rights holder.

The existing full font notices live in `shared/design/assets/licenses/`. Android
packages them under `assets/licenses/`; LG under `app/licenses/`; the preview build
copies them to `public/licenses/`.

Original project and CleanYourTV full MIT texts are generated from unchanged root
`LICENSE` and `docs/CLEANYOURTV-LICENSE.txt` into shared and installer asset sources
by `tools/catalogs.mjs`. They ship in both Android APKs, LG app/service and preview.
Do not hand-edit these generated copies. Installer `assets/licenses/SOURCES.txt`
records exact existing dependency source revisions and rebuild references; full
dependency texts are retained verbatim from those revisions. No dependency update
or relicensing is part of this notice audit.

The fonts can be renamed or bundled under the OFL, but not sold on their own. The full text is at
https://openfontlicense.org.
