# Temporary LG background probe

This checks Activity Manager callbacks, short audio playback, and embedded HDMI 1
on the LG LF6300 without root. It does not implement prayer calculations, Home
interception or standby wake.

The `arm` service method creates one non-persistent activity for 90 seconds later.
The callback records its process ID, launches the probe app, and completes through
the webos-service response lifecycle. The screen acknowledges that its JavaScript
ran. Stale/duplicate/cancelled callbacks do not launch it; callbacks over two minutes
late are discarded. The reminder does not repeat; the separate bounded startup
experiment is described below.

Run the local check with `node tools/webos-background-probe/test.cjs`.
Package `app` and `service` together with `ares-package`, then install the IPK with
`ares-install`. The temporary icon is a plain locally generated tile.

Device commands use the configured device `afnon-lg-lf6300`:

For a noninteractive SSH call on this TV, keep standard input open briefly:
`sleep 5 | luna-send-pub -w 4000 -n 1 -f luna://tv.afnon.bgprobe.service/status '{}'`.
Without that pipe, `luna-send-pub` can exit on input EOF before returning its reply.
Replace `status` with `arm` or `cancel` for the other test operations.

`arm` accepts `{"mode":"audio"}` for a four-second generated test tone, at element
volume 0.25, followed by an explicit switch to the recorded input and app closure
after 12 seconds. The service reads `getCurrentInput` immediately before opening
the reminder and maps it through `getAllInputStatus` (DTV/ATV use Live TV). Unknown
inputs prevent launch rather than reusing an old target. It leaves the TV's master volume untouched. The
default mode remains the silent screen test.
Use the service's `startNow` method for an immediate audio test with input capture;
directly launching audio mode cannot establish a valid return target.
The included MP3 is an original synthesized tone, not an adhan recording.

Launch the app with `{"mode":"hdmi"}` to show HDMI 1 alongside a ticking counter.
The HDMI probe closes after five minutes or Back. This uses the Homebrew-documented
`service/webos-external` / `ext://hdmi:1` mechanism, not a public LG consumer API.
HDMI mode must not load the audio file: this TV reported a decoder error when both
sources were present, even with the audio element set to `preload="none"`.
The service records a bounded set of media events, not continuous playback logs.
In HDMI mode, a test banner appears over the video from seconds 4 to 100. Check its
actual composition and continued sound on the physical display; DOM events alone
cannot confirm the hardware video plane's stacking order.

1. Launch `tv.afnon.bgprobe` and confirm its initial screen acknowledgement with
   `luna://tv.afnon.bgprobe.service/status`.
2. Invoke `luna://tv.afnon.bgprobe.service/arm` with `{}`.
3. Close `tv.afnon.bgprobe`. Verify it is absent from the running-app list and
   that the service exits before the due time. Do not poll `status` during the
   wait because that would restart the service.
4. After the due time, inspect `status` and compare the recorded timestamps with
   the diagnostic request time. The state is private to the service under
   `/media/internal/tv.afnon.bgprobe`; reading status does not launch the screen.
   Compare arm/fire process IDs and confirm both
   `launchResult.returnValue` and `reminderScreenLoadedAt`.
5. Close the app, then invoke `cancel`, `disableStartupTest`, and `cleanupData`.
   Uninstall `tv.afnon.bgprobe`. Keep results
   locally under ignored `state/`, and summarize the outcome in the port research.
   Earlier probe versions left `probe-state.json` and `startup-state.json` in
   `/media/developer/apps/usr/palm/services/tv.afnon.bgprobe.service/`. Inspect that
   directory, remove those known generated files, then remove the
   directory only if empty. `cleanupData` removes the current private data directory.

`enableStartupTest` creates a persistent 30-second interval activity with a
20-minute expiry. It remembers the current kernel boot ID and launches the silent
startup screen at most once for each different boot ID, claiming the boot on disk
before launching. The startup screen closes after two minutes or Back. This is a
temporary experiment, not a permanent startup installation. The `status` method
reports the saved startup ticks, launch result, boot ID and service start time.
Invoking status starts the service if needed but does not execute a startup tick
or launch an app; compare recorded launch times with this diagnostic request.
Always call `disableStartupTest` before removing the package, including after a
failed or interrupted power test.

The installed service directory became unwritable after a real reboot on the
LF6300. Version 0.0.7 uses LG's documented `/media/internal` storage location,
with a service-owned private directory and atomic file replacement. The installer
directory's initial write permissions are not a persistence guarantee.

The 10 November 2026 trial passed on firmware `04.06.75`: the original service
exited, a new process ran the scheduled callback, and the relaunched screen
acknowledged loading about two seconds after the scheduled second. The temporary
package and generated state were removed. See the
[port research](../../docs/WEBOS-TIZEN-PORT-RESEARCH.md#background-prayer-initial-scheduler-test-passed).

In a watched retry, the owner confirmed the banner drawing above HDMI without
interrupting picture/sound, and heard the tone. Merely closing the app returned to
Live TV, so version 0.0.5 explicitly launches `com.webos.app.hdmi1` when audio mode
closes. The owner confirmed HDMI picture and sound returned after that fix. This
retry launched audio mode directly to check the audible/visible behavior; the
earlier scheduler trial remains separate evidence.

Version 0.0.6 captured and restored DTV and HDMI 2. After the storage correction,
version 0.0.7 also opened automatically on a new kernel boot, confirmed on the
physical TV. Closing it did not cause subsequent interval callbacks to reopen it.
This establishes the mechanisms for a port, not production reliability or a
replacement for the physical Home button.

Platform references: [JS services](https://webostv.developer.lge.com/develop/guides/js-service-usage),
[Activity Manager](https://webostv.developer.lge.com/develop/references/activity-manager),
[service callback completion](https://webostv.developer.lge.com/develop/references/webos-service-reference),
[Homebrew external input embedding](https://www.webosbrew.org/develop/guides/web-app/).
