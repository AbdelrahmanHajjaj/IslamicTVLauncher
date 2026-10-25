# Fork and upstream contribution plan

This is a local preparation record for `feature/improvements_and_cleanup`, not a submitted PR.
No push or PR creation is part of the documentation update. Read [CONTRIBUTING.md](../CONTRIBUTING.md)
and the [PR template](../.github/pull_request_template.md) before submission.

## Two destinations, focused upstream changes

| Destination | Scope |
|---|---|
| [IslamicTVLauncher fork](https://github.com/AbdelrahmanHajjaj/IslamicTVLauncher) | Integration of the requested launcher features, fork name, matching screensaver label, fork README and local setup/recovery policy. Keep upstream attribution and the existing Android identity. |
| [Original Afnon](https://github.com/egyjs/afnon-android-tv-launcher) | Separate feature/fix proposals based on upstream main, retaining Afnon's name, links and project identity. Each proposal gets its own small branch, validation and changelog entry. |

The contribution guide does **not** explicitly prohibit a name change. Keeping fork branding out of
upstream is a proposed scope decision, not an asserted maintainer rule. The explicit rule is **one
topic per PR**, so one large upstream PR for all changes would not follow the guide. The fork's
integration branch can be reviewed together by its owner; do not present it as a single upstream-ready patch.

The existing merge brought upstream changes through `ca457a7` into the local branch while retaining
the fork's layouts and guarded setup. This is the last fetched upstream state, not a claim that GitHub
has had no later changes. Refresh the chosen base before preparing actual submissions, when authorised.
Avoid wholesale cherry-picks of commits that mix topics or fork branding; inspect each resulting diff.

Suggested upstream sequence (subject to maintainer agreement):

1. **City and calculation data:** review Leamington Spa separately from the expanded method presets.
   Each city addition needs its own official timetable comparison; methods need cited authority and
   checks that existing results remain unchanged.
2. **Prayer offsets:** shared calculation, persistence, bilingual controls and adjusted-time preview,
   plus the browser counterpart and date/time-boundary tests.
3. **Apps and navigation:** small, independently reviewable fixes for app-list caching/filtering and
   focus, then optional Home layouts/favourite replacement. Keep unrelated visual features out.
4. **Appearance:** themes/preview, lightweight backgrounds and shared prayer icons as separately
   reviewable changes where practical, after moving new design constants into shared tokens.
5. **Quick controls and prayer bar:** Inputs/Wi-Fi controls first; the optional bar, its settings and
   remote action can follow with overlay, RTL and live-input evidence.
6. **Setup and remote safety:** separate proposals for button handling, state preservation and installer
   recovery. Review differences from upstream's current Home handover carefully; do not replace its
   policy as an incidental part of a UI PR.

Do not include the fork's private validation history or these planning notes in upstream feature PRs.
Carry over only relevant checks and sanitised screenshots in each PR's description.

## Readiness checks

| Requirement | Evidence / remaining work |
|---|---|
| Attribution and licence | MIT and third-party notices retained, including CleanYourTV. No downloaded adhan or generated announcement audio is added to Git. |
| Current city data | Native catalog and generated browser data contain 137 cities / 43 countries; [city reference](CITIES.md). Introductory browser counts now derive from the catalog. |
| New city's prayer accuracy | **Pending:** six calculated times on one named date compared with an official local mosque/authority timetable, including method, Asr, timezone and differences. Leamington Spa's MWL default is not a verified mosque timetable. |
| Bilingual UI evidence | Updated real-TV screenshots cover Home, layout cards, offsets, appearance and prayer bar in English/Arabic. Every affected screen still needs its complete physical D-pad edge/Back check for its proposed PR. |
| Shared design tokens | Generated-token consistency checks pass in the recorded build. **Pending:** review the hard-coded preview/offset sizes in `SettingsActivity.java` and bar dimensions in `PrayerDock.java`, and move applicable UI constants into `afnon-mihrab/tokens.json` where the contribution guide requires them. Passing generation checks alone does not prove this. |
| Browser parity | Native data, icons, patterns, themes and both Home layouts are shared/mirrored. **Pending:** native prayer-offset editing and other TV-only settings need an appropriate browser representation; actual OS operations stay native. |
| Build and warnings | Last native build: launcher lint 0 errors / 85 warnings; installer lint 0 errors / 14 warnings. **Pending:** remove the 16 added pattern-vector warnings (8 path / 8 raster) and compare against the selected upstream base. Do not call this "no new warnings" yet. |
| Tests and performance | Existing automated checks and site build are recorded in [local validation](LOCAL-VALIDATION.md). Limited All apps timings are not a broad speed or memory comparison with CleanYourTV. |
| Hardware coverage | Home, remote keys, held volume, radio and test adhan have been checked on TCL 50C641K / Android 11. **Pending:** live HDMI picture/audio, full phone setup/recovery on a phone, and final reboot/idle-saver checks after the latest changes. |
| Privacy | Public screenshots are cropped/redacted as documented in [capture notes](screenshots/README.md). Backups, account/network/device identifiers, signing keys and private recordings stay outside commits. |

These remaining items block describing the **whole branch** as compliant and ready for upstream.
They do not prevent preparing a smaller proposal whose own requirements have been met.

## Before opening each PR

- Select one topic and its dependency/base; keep Afnon branding and default behaviour unless the
  maintainer has agreed to a change. Link relevant issues or discussion if available.
- Fill the existing PR template and add a matching `CHANGELOG.md` Unreleased entry.
- Run the relevant tests, native build/lint and browser build/token check. Compare warnings with that
  PR's base; fix newly introduced warnings rather than masking them.
- Check real-TV navigation, both languages and any changed service behaviour. Include sanitised
  English/Arabic screenshots for visual changes and disclose untested hardware paths.
- Review staged paths for generated outputs, APKs, audio, `state/`, `local.properties` and identifiers.
- Obtain authorisation to push/open the concrete proposal. Nothing in this plan publishes a branch.
