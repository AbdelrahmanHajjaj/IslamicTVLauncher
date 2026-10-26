# TV Launcher reference research

Investigated 26 October 2026 in the working copy based on commit `b96e90a` on
`feature/improvements_and_cleanup`. Other work was editing this shared checkout during the
investigation; findings describe the files inspected, not a frozen release snapshot.

The strongest opportunities for Afnon are more recognizable app cards, easier favourite editing,
an accessible reading layout, and better app organization. Preserve the mihrab, prayer strip,
Arabic-first design and offline operation. These recommendations do not require a framework change.

This is comparative research using published screenshots, listing text and local source inspection.
It is not a runtime, performance, security or accessibility audit of the competing launcher.
This investigation changed no application code or TV settings.

## Evidence and its limits

- [Arben Apura project page](https://arbenger.com/projects/tv-launcher/): read the page and inspected
  all six gallery images. Images 1 and 6 are duplicates. These show an older presentation.
- [Current Google Play listing](https://play.google.com/store/apps/details?id=com.premium.tv.launcher.ui&hl=en):
  inspected eight distinct promotional images, the description, release notes and visible reviews.
  The listing showed an update date of 17 October 2026. Promotional images are evidence of design
  and advertised features, not proof of runtime behavior.
- Afnon source and the repository's documented native captures from 25 October 2026 were inspected.
  Those captures are historical reference material; no new real-TV validation was performed.
- Reference captures are saved locally in the Git-ignored directory
  `state/research/tv-launcher-2026-10-26/`. The source links below remain usable without that folder.
  Third-party artwork has not been added to app assets or the tracked screenshot directory.

The portfolio identifies a Svelte/Capacitor implementation. A developer reply on the current Play
listing says it was rewritten natively. Treat these as potentially different generations; the
current implementation and comparative speed cannot be established from these sources.

The Play description's free/no-purchase and no-data claims also differ from its in-app-purchase
badge and data-safety declarations. This is an inconsistency to verify, not evidence of wrongdoing.
Afnon should continue explaining its own permissions and actual network behavior precisely.

## The downloaded APK is a different app

Static inspection of `D:\Dev\premium-tv-launcher-ui.apk` found:

| Field | Result |
|---|---|
| Application label | Aptoide |
| Package | `cm.aptoide.pt` |
| Version | `10.0.0` / code `13001` |
| Expected launcher package | `com.premium.tv.launcher.ui` |
| Size | 13,074,189 bytes |
| SHA-256 | `CD3A042BB2135040024CDD4E7B0DFD5F2DB950E6FC111F529B5E5887FBF7FC47` |

The Android SDK's `aapt dump badging` and ZIP inventory were sufficient to establish the mismatch.
The file was not installed or executed. It cannot substantiate the launcher's architecture,
permissions or performance. This check does not determine whether the Aptoide binary is authentic
or malicious. Further launcher-specific binary analysis needs the matching package.

## Screenshot observations

These numbered observations describe the published surfaces, not an exercised user journey.

Reference: labeled groups and recognizable app artwork.

![Reference launcher category rail and app artwork](https://arbenger.com/projects/tv-launcher/screenshot-2.webp)

Afnon: stronger prayer hierarchy and calmer surfaces; app artwork occupies much less of each card.
The black rectangles are the existing documentation redactions.

![Afnon's documented English Home](screenshots/home-en.png)

| Step | Surface and evidence | Assessment | Afnon implication |
|---|---|---|---|
| 1 | [Home artwork](https://arbenger.com/projects/tv-launcher/screenshot-1.webp), plus Play image 6 | Strong recognition from branded app artwork; many competing visual elements | Borrow the recognizable cards while retaining Afnon's calm prayer hierarchy |
| 2 | [Expanded category rail](https://arbenger.com/projects/tv-launcher/screenshot-2.webp) | Text beside category icons makes destinations clearer; the rail also occupies substantial space | Put optional filters in All apps, where they do not displace the mihrab |
| 3 | [Appearance controls](https://arbenger.com/projects/tv-launcher/screenshot-3.webp) | Sizes/styles are previewed visually; secondary copy appears small at this capture scale | Offer one tested reading preset before adding many independent switches |
| 4 | [Parental controls](https://arbenger.com/projects/tv-launcher/screenshot-4.webp), Play images 3 and 7 | Family intent is clear; enforcement and recovery are unverified | Defer full profiles and access restrictions until their actual scope is agreed |
| 5 | [Language selector](https://arbenger.com/projects/tv-launcher/screenshot-5.webp) | Language names are visible; this screenshot does not demonstrate Arabic layout quality | Keep Afnon's real RTL treatment and bilingual validation as strengths |
| 6 | Play images 2, 4, 5 and 8: widgets, cameras, backgrounds and tools | Broad customization; substantial visual and operational complexity | Prefer focused launcher controls over a general dashboard or widget platform |

Their translucent surfaces over detailed photos create a plausible readability risk. Exact
contrast, text legibility from a sofa, screen-reader semantics, focus order and remote behavior
cannot be certified from promotional screenshots. Afnon already has a strong filled focus state
and ring in [FocusTile.java](../app/src/main/java/tv/afnon/home/FocusTile.java).

## Ranked improvements

### 1. Make app cards easier to recognize

**High value; medium implementation scope.** Afnon's two-row Home uses 40-design-pixel icons inside
wide cards; All apps uses 56-pixel icons. Recent apps are text-only. These choices are visible in
[Home](screenshots/home-en.png) and [All apps](screenshots/all-apps.png), and confirmed by
`MainActivity.favouriteTile`, `moreTile` and `AllAppsActivity.appTile`.

Start with a larger icon and clearer label in the existing card bounds. Then trial a banner-based
card where an installed TV app supplies suitable artwork, with an icon-and-name fallback for other
apps. Keep labels and the existing focus ring. Preserve the artwork's aspect ratio; do not stretch
a square icon or tint the logo into the focus fill. No downloaded icon catalog is needed.

Android defines both square icons and 16:9 TV banners. Its package APIs expose banner resources,
which may be absent. This makes installed assets a feasible source, not proof that every installed
app will have useful artwork. [TV artwork guidance](https://developer.android.com/design/ui/tv/guides/system/tv-app-icon-guidelines),
[PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager#getApplicationBanner(java.lang.String)).

**Reach:** [Apps.java](../app/src/main/java/tv/afnon/home/Apps.java),
[MainActivity.java](../app/src/main/java/tv/afnon/home/MainActivity.java),
[AllAppsActivity.java](../app/src/main/java/tv/afnon/home/AllAppsActivity.java), shared tokens, and the
Home/Apps browser counterparts. Preserve lazy row creation and bounded artwork loading.

**Acceptance:** banners and missing-banner fallbacks work; long Arabic/English names remain legible;
focus survives app launch/return; scrolling stays smooth on the target TCL. Compare warm navigation
and memory with the current build before extending artwork across the whole grid.

### 2. Make favourite editing discoverable from Home

**High value; small-to-medium scope.** Afnon already supports adding, moving, replacing and removing
favourites in the All apps hold-OK menu. Home only activates tiles. The opportunity is fewer steps
and clearer discovery, not another favourites system.

Provide a visible “Edit favourites” entry and let hold-OK on a Home favourite reach the same actions.
Reuse the saved order, limits and replacement behavior. A minimal first slice can open All apps
focused on the selected package and its existing menu; direct Home editing can follow if that extra
screen is still inconvenient. A small contextual hint is preferable to restoring a permanent large
instruction footer that the fork deliberately removed.

**Reach:** `MainActivity.dispatchKeyEvent`, `AllAppsActivity.okKey/openMenu`, `Apps.saveFavourites`,
[FavouriteChoice.java](../app/src/main/java/tv/afnon/home/FavouriteChoice.java), `Text.java`, and the
browser Home/Apps interactions. Keep the guard that prevents releasing a long press from selecting
the menu's first action. Existing pure favourite tests belong in `tests/FavouriteChoiceCheck.java`.

**Acceptance:** short OK launches once; hold-OK does not launch; Back cancels and restores the same
tile; both Home layouts preserve all favourites; incomplete rows and RTL navigation remain correct.

### 3. Offer an “Easy read” preset

**High value for distant viewing; medium scope.** The reference advertises a projector mode; the
app-appearance screenshot visibly offers icon sizing. Afnon has a Classic Home layout, label toggle,
reduced motion and validated theme colors, but these are separate controls. All apps is fixed at
five columns and four visible rows.

Combine existing choices into a previewable preset, with larger labels/icons, visible quick-control
labels, a quiet background, and a less dense All apps layout. Reuse Classic where it helps; remember
that it shows only the first four favourites and must retain the rest. Allow the user to restore
their previous appearance settings. Do not silently change their motion preference.

There is also a concrete accessibility gap to investigate: `Screen.text()` sets text in physical
pixels derived from screen width; `Ui.init()` does not apply the user's font scale. Larger text needs
layout reflow, not just multiplication that clips Arabic or pushes controls off screen.
[Android TV text scaling guidance](https://developer.android.com/training/tv/accessibility).

**Reach:** `SettingsActivity`, `Prefs.key`, `Screen`, `Ui`, `AllAppsActivity`, `Text`,
[tokens.json](../afnon-mihrab/tokens.json), and corresponding browser screens. Generate token outputs
normally rather than editing generated Java/CSS.

**Acceptance:** check long names, Arabic glyph height, AM/PM times, every D-pad edge and Back at both
normal and larger text sizes. Validate from the actual seating distance and on a projector if a
projector-specific claim is made. This should not be sold as complete accessibility compliance.

### 4. Add lightweight groups inside All apps

**Useful for larger app collections; medium scope.** `Apps.allApps()` currently supplies favourites
first followed by the remaining apps, with an optional system-utility filter. There is no grouping
UI. The reference's category rail shows how grouping can shorten navigation.

Start with a small filter row: All, Quran & Islamic, Video, Audio, Games, Tools. Keep All as the
default and allow a package's group to be assigned through the existing app menu. A religion-specific
group needs explicit assignment; do not infer it from Android's generic category or an app's name.
If automatic categories are added, unknown apps must remain reachable and user choices must win.

**Reach:** `Apps`, `AllAppsActivity`, `Text`, and `site/src/screens/Apps.astro` / `apps.ts`.
Store grouping by package, independently from favourites and recent history. Add focused tests for
unknown apps, empty groups, uninstall/reinstall and focus restoration after switching groups.

**Acceptance:** no installed app disappears from All; favourites remain unchanged; Back from a
filter behaves predictably; left/right navigation mirrors in Arabic. Do not add a permanent Home
sidebar merely to match the reference.

### 5. Surface actionable prayer-service status

**High reliability value; small-to-medium scope.** Afnon already exposes service running/stopped
under Device settings, offers test reminders, and has opt-in recovery. The useful adaptation of the
reference's status/tools concept is to show the one failure that affects Afnon's central promise.

When reminders or adhan are enabled but the service is unavailable, show a compact actionable Home
notice explaining that automatic alerts need attention. Open the existing service settings/help.
Keep it absent when healthy, and distinguish an intentionally disabled feature from a stopped
service. Offline prayer calculation and automatic alert delivery are different capabilities.

**Reach:** `MainActivity`, the existing `SettingsActivity` health/test rows, `RemoteKeys.watch`,
`Text`, and the browser representation. Reuse lifecycle checks or the existing tick; no new polling
service. Do not silently grant permissions or override a user's deliberate service disablement.

**Acceptance:** service on/off, adhan/reminders intentionally off, return from system settings,
reboot, and recovery failure. Displaying a running service must not promise that every firmware
will deliver every alert.

### 6. Add a local settings export when portability becomes a priority

**Useful later; medium scope.** The listing advertises backup. Afnon already has installation and
recovery records, but those serve a different purpose from a portable launcher-preferences file.

A local export could contain appearance, favourite order, radio choice and explicitly selected
prayer preferences. Use a versioned allowlist and validate the entire import before changing
anything. Preview city, method and offsets before accepting them; resolve cities by stable identity
rather than blindly carrying a catalog index. Keep firmware mappings, permissions and recovery
state device-specific. This stays within the current offline product policy.

**Reach:** `Prefs`, `SettingsActivity`, `Appearance` validation and preference persistence tests.
Only involve the DUMP-protected `SetupProvider` and phone installer if phone-assisted transfer is
actually included; preserve their existing contract and version it for additions.

## Features to retain, defer or skip

| Reference idea | Afnon decision |
|---|---|
| Favourites, recent apps, sideloaded app visibility | Already present; improve discovery and presentation |
| Themes, layout previews, reduced motion, app-colored lighting | Already present; polish and validate rather than replace |
| Inputs, Wi-Fi, remote shortcuts, screensaver | Already present; finish documented hardware checks |
| Family profiles | Defer until multiple distinct home configurations are needed; prayer scheduling should remain a coherent household setting |
| PINs and screen-time controls | Defer. A launcher-only lock can be bypassed by other entry points and must not be advertised as device-wide parental control. TCL firmware mappings also need explicit examination |
| Weather, camera feeds, cloud backup, online wallpaper galleries | Outside the current radio-only network policy and core purpose |
| Blur, video wallpaper, hundreds of widgets | Conflict with the current rendering/performance constraints or introduce disproportionate scope |
| RAM cleaners, speed-test dashboards | Do not add to Home; keep useful diagnostics focused and on demand |
| Different framework or “zero lag” claim | No basis for a migration or performance claim from this research |

Visible Play reviews mention startup/app-switching delays, resets and update regressions. These are
individual reports, not measured failure rates or proof about the latest build. They reinforce the
value of validating the lifecycle and upgrades before expanding customization.

## Agreed Home customization scope

Implemented on `feature/home_customization`: native Home, the transactional settings preview,
permanent Today & Qibla quick access, and browser parity. See [local validation](LOCAL-VALIDATION.md) for checks and
hardware limits. The later app-card, favourite-editing, grouping and backup ideas remain separate work.

The follow-up discussion agreed to make Home adapt to configurable prayer information before
finalizing the app-card improvements. Cameras, video backgrounds and a widget platform are excluded.
This section records the implemented design.

| Control | Choices | Layout behavior |
|---|---|---|
| Prayer countdown panel | Mihrab, compact bar, hidden | Mihrab supports either side; compact bar supports top or bottom; hidden releases the entire panel area |
| Prayer information table | Current only, next only, current and next, all, hidden | One/two entries use compact cards; all retains the six timetable entries; hidden releases the table's space |
| Today & Qibla access | Permanent upper quick-control button beside Wi-Fi | Available in every Home configuration; opens the existing Today & Qibla screen |

Remove the Today & Qibla button from the mihrab once the upper button exists. Use the existing
compass icon, bilingual accessible label and quick-control label preference. The new button remains
visible when the countdown, the table, or both are hidden. Mirror the quick-control order in Arabic.

The countdown panel and information table are independent. Their visibility changes Home
presentation only; adhan, reminders, prayer calculation, the overlay bar and screensaver retain
their own behavior and settings.

Calculate the remaining Home regions from the selected panel and table modes. Reflow the clock,
quick controls, favourites and recent apps into those regions, preserving comfortable tile sizes,
spacing and safe screen margins. Do not leave an empty mihrab column or table row. Keep all saved
favourites and the existing app-order rules. Provide a preview, Apply, Back-to-cancel and restoration
of the default layout through the existing settings patterns.

Implementation detail to preserve: `PrayerDay.nowIndex` currently has no entry before Fajr or
between sunrise and Dhuhr, while `nextIndex` always excludes sunrise. For the new filtered table,
use a neutral dash in the Current slot when the model has no current entry; do not relabel the next
prayer as current or change prayer-time semantics as part of layout work. Current-and-next keeps
its two slots, avoiding layout jumps at those boundaries. All continues to show sunrise as a
timetable marker. These are display rules, not a new religious calculation policy.

Scope includes `MainActivity`, its Home-specific countdown rendering, `FavouriteChoice` where grid
geometry changes, `Prefs`, `SettingsActivity`, `Text`, shared tokens and native/browser Home previews.
The shared arch/countdown helpers in `Screen` are also used by `PrayerDayActivity`; keep that screen's
existing presentation intact. No new dependency or background timer is required.

Acceptance must cover every supported panel position and table mode in Arabic and English,
including both hidden, long labels, different favourite counts, midnight, sunrise, prayer rollover,
Friday/Ramadan labels, and return from another app. D-pad paths must match the visible arrangement,
never target a hidden panel, preserve a sensible focused app across layout changes, and always
reach Today & Qibla, Settings and All apps. Prayer-table updates must not steal focus.

## Suggested delivery order

1. Build and validate the adaptable Home layout, independent prayer display controls and permanent
   Today & Qibla quick button described above.
2. Improve icon scale and labels within the resulting Home cards, then evaluate banner artwork.
3. Add the Home entry to existing favourite management and the actionable service-status notice.
4. Trial Easy read on the physical TV in Arabic and English.
5. Add groups only if navigating the real installed-app collection remains tedious.
6. Consider preference export and family controls separately, after defining their scope.

For any UI implementation: update native and browser counterparts, use existing tokens and
bilingual text, run the affected checks, and capture sanitized real-TV evidence. Full navigation,
live HDMI/audio interactions, reboot and idle screensaver behavior remain separate hardware checks;
published screenshots do not substitute for them. See [local validation](LOCAL-VALIDATION.md).

The original research pass was limited to source/screenshot inspection, APK identity, document links
and the Git diff. The subsequent Home implementation and its build, browser and TV checks are recorded
in [local validation](LOCAL-VALIDATION.md).
