# Local TV setup and recovery

These tools install your locally built Afnon. They never fetch or push Git. Python 3.9+, JDK 17 and Android SDK 34 are required. The phone installer can also install an APK you copy to the phone: choose **Install a local Afnon APK**.

Tested on TCL 50C641K, Android 11 (API 30), firmware V8-R51MT02-LF1V903.021315. Installation, exclusive Home and reboot passed; see [validation and remaining hardware checks](../docs/LOCAL-VALIDATION.md). The tools read the actual API version and refuse anything below 26.

## Install from Windows

Enable debugging, connect with Android platform-tools and approve the TV prompt. Use the same address and backup folder throughout. Replace the example address with your TV address.

```powershell
adb connect 192.168.1.50:5555
$env:TV = '192.168.1.50:5555'
./tools/install.ps1
python -S -B tools/device.py configure
```

Linux/macOS: set `TV`, then `bash tools/install.sh`; use `python3 tools/device.py configure`. Install builds source, saves the original state, installs with replacement allowed only for a matching signature, starts Afnon and checks the selected Home. It preserves the screensaver. It never uninstalls an app. Download the optional audio separately for personal use with the existing audio-fetch scripts before building if you want recorded adhan/announcements; the launcher still builds without those recordings.

After checking Home, Settings, HDMI and the remote, disable known stock Home packages:

```powershell
python -S -B tools/device.py home --exclusive
```

An initial install can also use `./tools/install.ps1 -Exclusive`. A handover failure attempts to restore the original Home. When Afnon is already Home, an ordinary update verifies it without repeating the original handover or restoring earlier cleanup/helper settings. Wake the TV and leave Home visible during installation; a failed visibility check stops the command. Unknown launchers, CVTE boards and TVs without a verified foreground Afnon are refused. On CVTE, verify Home and ADB after a reboot before enabling any helper. Package removal is never used.

## Back up and undo

The first operation saves packages, original Home, settings and device properties in ignored `state/backups/`. A retry preserves the original snapshot. `--backup-dir <folder>` before the command selects a private folder on the PC or external storage; keep using that same folder. Backups contain private device information: do not commit or publish them.

```powershell
python -S -B tools/device.py backup
python -S -B tools/device.py disable com.example.optional
python -S -B tools/device.py restore
```

Disable only apps the household has agreed to remove from use. Core services, the active Home, selected screensaver, input hardware, Wi-Fi-owning setup wizard and CVTE Home are protected. Each change is recorded before disabling it. Restore re-enables only recorded packages that were originally enabled, verifies the original Home, restores settings still owned by this setup, and leaves Afnon installed. Keep the backup after restore. A later user choice is preserved; inspect any skipped settings on the TV.

The phone installer writes the same recovery format into its private storage before setup. **Save recovery backup** exports `recovery.json` to the phone's chosen location. **Restore original TV setup** uses that record while connected. Keep an exported copy before clearing installer data. For PC recovery, put the exported file in a folder and pass that folder with `--backup-dir`; the device identity must match. The phone backup includes diagnostic snapshots inside the JSON.

Old `state/disabled.log` files are not automatically imported: they do not identify a TV or its original Home. Review those records manually before restoring an older installation.

## Screensavers

Setup preserves the existing selection, including Google Ambient/Backdrop if installed. List services or select Afnon:

```powershell
python -S -B tools/device.py screensaver
python -S -B tools/device.py screensaver afnon
```

Pass a listed `package/service` to select a custom installed screensaver. Google photo choices remain in Google's screensaver settings and depend on the TV firmware and installed services. Do not disable the package providing the selected saver.

## Finish on the real TV

Verify Home from another app and after reboot; HDMI/source menu, volume hold, Bluetooth pairing, voice, Wi-Fi, prayer alerts, radio and saver wake-up. Test remapped buttons on the physical remote: injected ADB keys do not prove accessibility interception works. Keep the stock launcher enabled until these checks pass. Compare startup and navigation before making speed claims. After installation, turn debugging off, revoke authorisations, reboot and confirm ADB can no longer connect.

## Local checks

`python -S -B -m unittest discover -s tests -v` runs simulated-TV checks; it never connects to a TV. Build both apps with `./gradlew :app:assembleDebug :installer:assembleDebug`. Hardware checks remain separate.


### Device-test cleanup

Android instrumentation can stop the launcher process and leave its enabled accessibility service unbound. UI Automator hierarchy dumps can also suspend accessibility services temporarily and close their overlays. Capture prayer-bar screenshots directly with `screencap`, without a hierarchy dump between opening and capturing the bar.

After instrumentation or UI automation, inspect `dumpsys accessibility`: an enabled entry alone does not prove the prayer/button service is running. Check that Afnon is bound and absent from the crashed-services list. If it was enabled before the test but remains disconnected, reconnect only Afnon through TV accessibility settings, preserving every other service. Verify Open prayer bar in Afnon Settings and the physical blue button, restore changed test preferences, and remove the test-only APK if the test installed it. Do not enable services that were disabled before testing or change the user's automatic-repair preference.
