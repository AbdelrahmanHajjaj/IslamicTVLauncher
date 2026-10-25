# Themes and ambient backgrounds

In **Settings → Display & language**, choose Mihrab (the original look), Aurora, Ember or Forest. Prayer, radio, layout, fonts and Quran text stay in place. Background choices are lattice, solid, panorama and daylight. Ambient light controls the subtle colour wash, from 0 to 100. Daylight follows the TV clock; it does not fetch weather or images. The static 480×270 background updates at most once a minute on the existing foreground clock tick. Reduce motion removes focus scaling and UI transitions; the screensaver still moves to avoid a static image.

Custom themes are local JSON, imported over ADB. Copy a preset from `app/src/main/assets/themes/`, edit it and run:

```powershell
python -S -B tools/device.py --tv 192.168.1.50:5555 theme my-theme.json
```

```json
{
  "version": 1,
  "colors": {
    "lapis-950": "#081921",
    "lapis-900": "#0B202C",
    "lapis-800": "#10313E",
    "lapis-700": "#173D49",
    "gold-400": "#8EDBD2",
    "gold-200": "#B9F0D8",
    "plaster-100": "#EFF5ED",
    "mist-400": "#B3D0D8"
  },
  "background": "panorama",
  "lighting": 35
}
```

The eight names retain their original token meanings: dark surfaces, accent, pressed accent, primary and secondary text. Omitted colours keep the defaults. Colours must be opaque `#RRGGBB`; unknown fields, files over 16 KiB, out-of-range lighting and insufficient text contrast are rejected before saving. Select Mihrab to reset the look. No image downloads, video, blur, telemetry or extra runtime dependencies were added.

`afnon-mihrab/tokens.json` remains the source for the default design. `npm run tokens` in `site/` generates both immutable defaults and the theme palette. Do not edit `Tokens.java` or `Palette.java` directly. The browser preview reads the same preset files.

## Remote buttons and screensavers

**Settings → Device settings → Remote buttons** lets you select a named button or choose **Press a button to assign**, then select an installed app, Home, All apps, Inputs or Settings. Installed apps are refreshed when the picker opens, and changing a button replaces its previous assignment. On TCL, branded buttons may be intercepted before Android delivers them to the dialog: use their named entries instead. The supported TCL firmware codes are offered even when Android does not expose their key names. An **Enable the remote button service** entry appears when the service is stopped. The browser preview uses example apps and does not change the TV. Choose **Original** for a button to return it to the firmware; **Restore original TV buttons** turns off Afnon remapping while leaving the prayer service available. Adding a new assignment after this does not re-enable the old assignments. Other applications' TCL mappings are preserved. Home, power, volume and D-pad navigation are excluded. Test using the physical remote; vendor key codes and volume hold need hardware verification.

**Screensaver** lists installed Android dream services and opens the TV's screensaver/photo settings. Choose Afnon, a custom installed saver, or Google's Ambient/Backdrop when the firmware provides it. The launcher does not download Google's private photo stream. Installing Afnon preserves your current saver; cleanup protects its package.

**Prayer and reminder service** reports whether the service is running. The optional **Recover prayer service** switch follows CleanYourTV's force-stop recovery pattern when secure-settings access was granted: reopening Home can restore a previously connected service while preserving other accessibility helpers. Deliberately disabling the service clears its recovery marker. This needs a force-stop and physical-remote test on the TV.
