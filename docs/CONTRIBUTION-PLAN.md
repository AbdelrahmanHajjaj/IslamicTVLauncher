# Fork and upstream contribution plan

The integration branch `feature/improvements_and_cleanup` is submitted as [fork PR #1](https://github.com/AbdelrahmanHajjaj/IslamicTVLauncher/pull/1).
Only that fork PR is in scope; upstream proposals are deferred at the owner's request.
Read [CONTRIBUTING.md](../CONTRIBUTING.md) and the [PR template](../.github/pull_request_template.md) for each future submission.

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
| New city's prayer accuracy | **Not independently verified:** the local mosque has no available published link. The owner configured manual offsets; they survived the final update and restart. Leamington Spa's MWL default remains an editable starting point, not a verified mosque timetable. An upstream city PR still needs the required comparison. |
| Bilingual UI evidence | Updated real-TV screenshots cover Home, layout cards, offsets, appearance and prayer bar in English/Arabic. Every affected screen still needs its complete physical D-pad edge/Back check for its proposed PR. |
| Shared design tokens | Settings rows, offset time sizing, appearance previews, layout cards, quick menus and prayer-bar dimensions now use shared tokens. Native generation and browser consistency checks pass. Decorative miniature drawing coordinates remain local to their renderers. |
| Browser parity | Native data, icons, patterns, themes, both Home layouts and six prayer-offset controls are shared/mirrored. English/Arabic save/cancel, navigation and centred times passed real-browser checks. Android inputs, Wi-Fi, overlays, service and recovery operations require the TV. |
| Build and warnings | Final native build: launcher lint 0 errors / 56 warnings; installer lint 0 errors / 14 warnings. All sixteen added pattern-vector warnings were removed, and further touched-code findings were fixed. No lint findings map to added/changed lines against the fork base. A separate baseline build was not run because the local export guard refused its source archive; this is a diff-based comparison, not a complete diagnostic-set equivalence claim. |
| Tests and performance | Existing automated checks and site build are recorded in [local validation](LOCAL-VALIDATION.md). Limited All apps timings are not a broad speed or memory comparison with CleanYourTV. |
| Hardware coverage | Home, remote keys, held volume, radio and test adhan have been checked on TCL 50C641K / Android 11. The final APK retained settings and disabled packages; Home and the prayer/button service returned after a restart. Automatic saver start, physical Home wake, and the post-restart Home/Blue/Back/Settings/Input/held-volume checks passed. Live HDMI picture/audio and complete phone setup/recovery still need hardware coverage. |
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
