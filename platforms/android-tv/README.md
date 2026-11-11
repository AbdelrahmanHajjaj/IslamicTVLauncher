# Android TV shell

The native Java Views launcher keeps package `tv.afnon.home`, its existing settings
and debug signing configuration. The portable domain rules come from `shared/core`;
astronomy, Android ICU dates, persistence, scheduling, playback and UI stay here.

From the repository root, with JDK 17 and Android SDK 34 installed:

```sh
./gradlew -p platforms/android-tv verifyApkSize lintDebug --no-daemon
```

Set `ANDROID_HOME` to the installed SDK for this standalone build, or use an ignored
`platforms/android-tv/local.properties`. It consumes only this project, `shared/`,
local optional recordings under `state/media/`, and the root Gradle wrapper. It does
not configure the phone installer, preview or LG project. The output remains
`platforms/android-tv/build/outputs/apk/debug/app-debug.apk`, with a strict 25 MB limit.
Use the same signing key as the installed app when upgrading to preserve its data.

The repository's aggregate build keeps the existing task names:

```sh
./gradlew :app:assembleDebug :installer:assembleDebug --no-daemon
python3 -S -B -m unittest discover -s tests -v
```

See [contribution rules](../../CONTRIBUTING.md), [native integration notes](../../CLAUDE.md)
and [guarded installation and recovery](../../tools/device.md). Edit canonical data
and design sources in `shared/` and `afnon-mihrab/`; regenerate their Java/XML/PNG
outputs through the existing tools. Keep restricted audio and build/device state
ignored. Automated checks do not replace physical TV, sound and remote acceptance.
