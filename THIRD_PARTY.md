# Third-party material

The MIT licence in `LICENSE` retains the original Afnon copyright and covers this
project's code contributions. These parts belong to others.

| What | Where | Licence |
|---|---|---|
| [adhan-java](https://github.com/batoulapps/adhan-java) by Batoul Apps | Gradle dependency | MIT |
| [Kotlin standard library 2.2.10](https://github.com/JetBrains/kotlin/tree/v2.2.10) by JetBrains and contributors | Shared JVM/JS core runtime, bundled in Android, LG app/service and preview | Apache 2.0 and the upstream standard-library third-party notices; full texts packaged under Android `assets/licenses/kotlin/`, LG app/service `licenses/kotlin/` and preview `licenses/kotlin/` |
| [JetBrains annotations 13.0](https://repo1.maven.org/maven2/org/jetbrains/annotations/13.0/annotations-13.0.pom) | Transitive Kotlin JVM runtime dependency | Apache 2.0; license and source attribution under `assets/licenses/kotlin/` |
| [adhan-js](https://github.com/batoulapps/adhan-js) by Batoul Apps | Direct dependency of preview and LG; bundled as ES5 in LG | MIT; full notice included in the package |
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

The fonts can be renamed or bundled under the OFL, but not sold on their own. The full text is at
https://openfontlicense.org.
