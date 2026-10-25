# Themes and ambient backgrounds

In **Settings → Themes & backgrounds**, choose Mihrab (the original look), Aurora, Ember, Forest, Midnight, Rose, Sand or Slate. Use Left/Right on the horizontal theme and background lists to update a miniature Home preview immediately. **Apply changes** saves the whole page; leaving it without applying keeps the active theme and settings unchanged. Imported Custom themes remain available. Prayer, radio, layout, fonts and Quran text stay in place. Background choices include lattice, solid, panorama, daylight, mountains, dunes, halo and night sky, plus eight line patterns: Arches, Eight-point stars, Mashrabiya, Arabesque, Star tiles, Rosettes, Octagon lattice and Corner medallions. Every pattern takes its line colour from the selected theme; Ambient light also adjusts line strength. They use small local vector resources drawn into the existing cached 480×270 bitmap, with no wallpaper downloads or animation loop. The browser preview extracts the same vector paths. Pattern names and selection controls are available in Arabic and English. Ambient light controls the subtle colour wash, from 0 to 100. Daylight follows the TV clock; it does not fetch weather or images. The static 480×270 background updates at most once a minute on the existing foreground clock tick. Reduce motion removes focus scaling and UI transitions; the screensaver still moves to avoid a static image.

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

## App lighting, favourites and prayer offsets

**Themes & backgrounds → App focus lighting** adds a subtle wash using the focused app's banner or icon colours. It is off by default. Colours are sampled from a 12×12 image and cached for the current Home view, following CleanYourTV's colour sampling approach (MIT; `CLEANYOURTV-LICENSE.txt`). Ambient light controls its strength. A focus change transitions for 280 ms, then stops; Reduce motion makes the change immediate. There is no idle animation loop. It works with panorama, daylight and the other backgrounds.

Home supports up to nine favourites in two balanced rows. **Display & language → Home layout** offers horizontal cards with miniature previews: **Two favourite rows** (default) and **Classic — one row**, which displays the first four saved favourites as larger tiles. Left/Right browses, OK applies and Back cancels. Switching layouts preserves the full favourites list, and both retain recent apps and All apps. Arabic mirrors the cards and navigation. Below them, up to five recently opened apps appear beside All apps. Recent history stays on the TV and records launches made through Afnon, including its assigned remote buttons; apps opened directly by the TV firmware are not tracked. **All apps → hold OK** adds or reorders a favourite; extra favourites can also be removed. **Replace a favourite** returns to Home: select a favourite tile and press OK to replace it, or Back to cancel. **Uninstall** opens Android’s confirmation for removable apps; system apps and the launcher are excluded. The phone installer reads the TV's supported capacity, retaining compatibility with older four-slot builds.

**Display & language → Hide system apps in All apps** filters out system utilities while keeping media apps, games and Play Store visible, including preinstalled YouTube, YouTube Music, Play Movies, Play Games, Netflix and TCL Gallery/Media Player. It uses Android app categories with a small fallback list for those common TV apps. It is off by default. Home favourites and recent apps remain visible, and nothing is disabled or removed from the TV. All apps displays four rows and scrolls by row; tiles are created as they enter view. The Home, All apps, prayer and settings footer instructions and adhan dismissal hints have been removed; buttons and automatic dismissal still work.

**Prayer times → Calculation method** offers [19 offline presets](PRAYER-METHODS.md), shared by all prayer displays and alerts. **Reminders & adhan → Test reminders & adhan** previews the reminder banner or adhan popup using the current sound settings, without changing scheduled alerts.

**Prayer times → Prayer time offsets** shows one **− / number / +** row per prayer (including the sunrise marker), matching Hijri date adjustment. The adjusted prayer time appears in larger type at the centre of each card, in the same relative position in English and Arabic. It updates immediately while editing and follows the selected 12-hour (AM/PM) or 24-hour format; Back restores the saved time. Press OK to edit, Left/Right to adjust between −30 and +30 minutes, OK to save or Back to cancel. Back again returns to Prayer times. Negative means earlier, positive means later, and zero restores the selected calculation method's time. Displays, reminders and adhan use the same offsets. Offsets are preserved when changing location or method, so review them then. A fixed offset is not a seasonal mosque timetable. Leamington Spa is available in the UK city list; location discovery is not implemented.

The browser preview includes the new theme page and scenes. It includes both Home layouts and their card picker. Prayer-offset editing remains available in the native TV app only.


## Quick controls and prayer bar

Home has Inputs, Radio and Wi-Fi icons beside Settings. **Display & language → Quick control labels** controls labels on both Home and the prayer bar; icons are the default. All apps also has a Settings cog beside the next-prayer chip.

**Inputs** lists Home, the TV's visible HDMI/passthrough sources and its hardware Live TV tuner. Connected HDMI-CEC devices replace their parent port entry to avoid duplicates. The **⋯ Advanced inputs** button beside the popup title opens the firmware's source chooser. The first selected row remains Home; press Up to reach Advanced inputs. This uses the TV's registered inputs rather than assuming a number of HDMI ports. Home returns to the first favourite; Down moves through favourites and recent apps, then highlights All apps. Left from the first column reaches Today & Qibla. Returning from an app can retain the previous tile when Home has not rebuilt.

**Wi-Fi** opens Android's network panel directly, with Wi-Fi settings as a fallback on older TVs. The panel shows the connected network and available networks and handles passwords. Android restricts network names and scans when location is off; the system panel works without enabling location for the launcher. Afnon does not scan in the background or save network credentials.

The prayer/button accessibility service hosts a slim, full-width **Prayer bar** above other apps and TV inputs. Its expanded view uses one compact row: six bordered prayer tiles fill the available space, with Home, Inputs, Wi-Fi, Settings and a **−** minimize button at the right. The current prayer has a light fill and **Now** label; the next prayer has a theme-coloured border and **Next** label. Arabic mirrors the prayer order, controls and popup layout, placing the controls at the left. Back also minimizes it; Back from its Inputs list returns to the bar. Settings are under **Display & language**:

- **Hidden until opened** (default): no bar until its assigned button is pressed.
- **Small prayer tab**: a thin strip with the next prayer and time while another app/input is visible. It cannot receive focus, touches or remote navigation.
- **Off**: disables the bar, independently of reminders and adhan.
- **Prayer bar position → Top / Bottom**: applies to both the strip and expanded bar; Bottom is the default.
- **Open prayer bar**: opens it from Settings, or opens accessibility settings if its service is unavailable.

Under **Device settings → Remote buttons**, assign Red, Green, Yellow, Blue or another supported shortcut to **Prayer bar**. There is no universal default button; a device's saved assignment survives updates. Essential Home, Back, power, volume and navigation keys remain protected. The bar reuses the existing prayer clock check and refreshes its text once per minute; it adds no idle animation or new service.

Reminder banners continue over the current app/input, and adhan opens its existing call screen. Back from the call returns to the previous activity. An external HDMI device is separate from Android: whether its sound is ducked or mixed during adhan depends on the TV's audio routing and needs a live-source test. Apps can also restrict overlays. The browser preview demonstrates quick controls and settings; it cannot show an Android overlay or operate a TV input/network.

### Prayer icons

Six line icons, sized to the neighbouring text, identify Fajr (dawn), Sunrise (rising sun), Dhuhr (midday sun), Asr (afternoon sun), Maghrib (setting sun) and Isha (crescent and star). Names remain visible. The same vectors appear in Home, Today & Qibla, All apps' next-prayer chip, offsets, the settings preview, reminders, adhan, the prayer bar and Afnon's screensaver. Friday uses Dhuhr's icon. Icons inherit the text colour, including theme and focus states. The browser preview extracts the paths from the Android vector resources; there are no icon downloads or new dependencies.

### Pattern design references

The original generated linework draws on the repeated stars, polygons and vegetal forms described in [the Met's geometric-pattern introduction](https://www.metmuseum.org/essays/geometric-patterns-in-islamic-art) and [the V&A's Islamic tile examples](https://www.vam.ac.uk/articles/design-and-make-your-own-islamic-tile-and-printed-pattern). Museum photographs are references only and are not bundled. Corner medallions leave the centre quiet; the other patterns repeat across the screen. All are selectable independently of the colour palette.
