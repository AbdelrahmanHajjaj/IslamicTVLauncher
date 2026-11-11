# Releasing Islamic TV Launcher

This is the project's Android release procedure. Normal work uses a feature branch
and PR into `AbdelrahmanHajjaj/IslamicTVLauncher`'s `main`. A commit or PR request does
not authorize merging, publishing or detaching the GitHub fork. Current migration
acceptance and hardware gates live in [the durable queue](docs/ARCHITECTURE-QUEUE.md).

LG versions, tests and IPK packaging are separate; follow
[its guide](platforms/webos/README.md) and [agent rules](platforms/webos/AGENTS.md).
An Android release does not publish an LG package or prove either TV's behavior.

## Destination, versions and signing

The destination is this project's [releases page](https://github.com/AbdelrahmanHajjaj/IslamicTVLauncher/releases).
The online installer expects a stable release with `afnon.apk`; attach the matching
phone installer as `afnon-installer.apk`. An installer preview is not a TV release.
Never download or publish upstream's APK as this project's build. As checked on
11 November 2026, only the installer prerelease exists here; use local builds until
a stable TV release is explicitly published. The preview links to build instructions.

- Android version/name live in `platforms/android-tv/build.gradle`. Use a minor
  version for new functionality and a patch for fixes. Increase `versionCode` for
  each distributed update; never reuse it or move an already-published tag.
- Keep the installed app's signing key. Existing builds use debug signing with
  the maintainer's established `~/.android/debug.keystore`; a new machine's
  automatically generated key cannot update those installations. Keep the key
  private and backed up. No key or installed package ID changes during refactoring.
- Preserve `tv.afnon.home` and the debuggable recovery workflow. A signature
  mismatch must stop the update; uninstalling is not a migration strategy.

## Prepare and validate

1. Confirm the reviewed commit, version and explicit release authorization. Resolve
   required queue/hardware gates and unrelated working-tree changes first.
2. Update the Android version and `CHANGELOG.md`. Document changed permissions,
   settings, dependencies, media rights and actual tested TV versions.
3. Prepare local media if needed with `tools/fetch-adhans.sh` and
   `tools/fetch-announce.sh`. Their ignored `state/media/` outputs are personal build
   inputs; distribute recordings only with redistribution rights. Preserve original
   MIT, CleanYourTV, runtime and font notices.
4. Build with JDK 17 and SDK 34, then run affected shared/preview/LG checks:

   ```sh
   ./gradlew -p platforms/android-tv clean verifyApkSize lintDebug --no-daemon
   ./gradlew :app:assembleDebug :installer:assembleDebug :installer:lintDebug --no-daemon
   python3 -S -B -m unittest discover -s tests -v
   ```

   TV APK: `platforms/android-tv/build/outputs/apk/debug/app-debug.apk`.
   Phone APK: `installer/build/outputs/apk/debug/installer-debug.apk`. Verify each
   package/version, signing certificate and hash with installed SDK tools. Keep
   candidates/logs under ignored `state/`. The complete TV APK must stay strictly
   below 25,000,000 bytes.
5. Back up the TV's settings/input/startup state. Install over the previous version
   and compare afterward. Check Home, all screens, both languages, physical D-pad/
   Back and special/held-volume keys, prayer/media interruptions, live HDMI,
   reboot/service/screensaver behavior and crash logs. Check phone installation and
   recovery on a phone. JVM, emulator and browser results are separate evidence.
6. Commit validated source/version/docs and submit the PR into this project's
   `main`. Inspect staged files; exclude keys, recordings, binaries and private state.
   Merge and publish only when explicitly authorized.

## Publish and recover

When authorized, create the version tag/release against the reviewed commit in this
project, attach `afnon.apk` and `afnon-installer.apk`, and include hashes, signing
continuity, actual device evidence and known limits in the notes. A draft or
prerelease does not satisfy the installer's stable-release URL. Verify uploaded
artifact hashes and the installer's source before announcing availability.

If an update is faulty, ship a corrected version with a higher `versionCode` and the
same key. Preserve settings and recorded recovery state. Never silently replace
the signing key, overwrite a published tag or require users to clear app data.
