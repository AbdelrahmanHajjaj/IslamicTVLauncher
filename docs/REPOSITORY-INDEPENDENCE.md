# Repository independence

Islamic TV Launcher owns its roadmap, architecture, tests, reviews and releases
in [AbdelrahmanHajjaj/IslamicTVLauncher](https://github.com/AbdelrahmanHajjaj/IslamicTVLauncher).
The shared Kotlin core, canonical sources and separate Android/LG shells now
define this project. Contributions target this project's `main`.

**Prepared, not executed.** This branch updates the maintained documentation and
workflow. It does not change GitHub's fork relationship, delete a repository,
rewrite history, create a replacement remote, merge or publish anything.

## Retained provenance and identity

- Credit [egyjs/Afnon](https://github.com/egyjs/afnon-android-tv-launcher) for the
  original launcher, mihrab design and project foundation in the README. The
  current architecture includes retained Android code/assets and third-party
  libraries; do not describe it as an entirely original or clean-room codebase.
- Keep [LICENSE](../LICENSE), [CleanYourTV credit](CLEANYOURTV-LICENSE.txt),
  [third-party notices](../THIRD_PARTY.md), source references and Git author history.
  Existing generators continue to package those texts in the applications.
- Keep `tv.afnon.home`, `tv.afnon.webos`, `tv.afnon.webos.service`, settings keys,
  writable directories and signing keys. Repository affiliation is independent
  of installed identity; existing users must retain their update path.
- Keep this project's repository, support and download targets. `origin` already
  points here, with no local `upstream` remote configured as of 14 November 2026.
  Removing or renaming a Git remote would not detach a GitHub fork.

The contribution guide and test policy are maintained here. Historical upstream
plans grant no active permission or obligation to submit changes there. Original
Afnon links remain attribution, not this project's build or support destination.

## Administrative decision

Preserve the existing repository and history rather than start a fresh codebase.
Before execution, inventory repository metadata, branches/tags, releases/assets,
open PRs/issues, Pages, workflow settings and protections, and back up what matters.
Confirm the current fork/parent/child-fork state with admin access; public metadata
alone does not establish eligibility or what GitHub will retain.

[GitHub's detachment guide](https://docs.github.com/en/pull-requests/how-tos/work-with-forks/detaching-a-fork)
currently offers **Settings → General → Danger Zone → Leave fork network** for
public forks below 1 GB with no child forks. It describes detachment as permanent
and warns of repository-metadata loss while retaining Git commit metadata.
Do not assume issues, PRs, stars or settings survive. GitHub also describes
support-assisted options that can preserve issue/PR history; confirm the applicable
route and effects with Support before execution if that history must survive.

The owner must approve the concrete route and its confirmed effects. Do not use
delete/recreate or a mirror push as an automatic fallback. This preparation grants
no permission to contact Support on the owner's behalf.

## Execution checklist for the approved route

1. Recheck GitHub's current guidance, inventory and backup; record the chosen
   route and expected metadata effects. Keep credentials and exports private.
2. Complete only the approved administrative action. Retain repository name/URL
   where the route permits; if it requires a new URL, inventory all consumers first.
3. Verify fork affiliation, default branch/history, surviving issues/PRs,
   releases/assets, permissions/protections, Actions and Pages configuration.
   Re-enabling a publishing or catalog-writing workflow is a separate action.
4. Check README/support links, preview base URL and installer download/catalog
   targets. Run affected [test requirements](../TESTING.md) if any source/config
   changes. Do not substitute an original-project APK for this project's release.
5. Record the actual outcome here and summarize it in the durable queue. Keep the
   Android hardware gate open until its own acceptance passes.

Current preparation changes documentation and templates only. It leaves existing
CI jobs, scheduled Pages/catalog workflows and remote configuration untouched.
