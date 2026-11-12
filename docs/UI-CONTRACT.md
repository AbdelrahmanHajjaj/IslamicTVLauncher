# Cross-platform UI and input contract

This specifies product behavior shared by native shells. Implementation status,
priority, platform gaps and acceptance evidence live only in
[ARCHITECTURE-QUEUE.md](ARCHITECTURE-QUEUE.md). Android is the initial parity
reference; platform renderers, OS APIs and saved preference keys remain separate.

## Shared responsibilities

| Contract | Shared definition | Platform binding and proof |
| --- | --- | --- |
| Screens/actions | Stable semantic IDs and action meaning | Bind an ID to the actual screen/control/handler; test activation and return target. |
| Settings | ID, section, value kind, choices/bounds, save/cancel behavior and capability requirement | Bind the existing preference reader/writer and native editor; test rendered coverage and round trips. |
| Input | Logical directions, initial/return focus, Home boundaries and modal ownership | Translate physical keys once, resolve shared targets to visible/enabled controls, then focus/activate native views. |
| Capabilities | Explicit shell implementation and OS conditions | Declare implemented, unimplemented or OS-unsupported; hide/lock unimplemented controls while retaining the universal definition and gap report. |
| Presentation | Canonical bilingual labels, tokens, exact religious text and accessible action names | Check text clipping, Arabic joining, RTL, focus visibility and actual TV picture/sound separately. |

The shared layer returns IDs/models/decisions and has no DOM, Android views,
filesystem, network or device callbacks. It does not draw screens. Shells retain
native layouts, media, scheduling and OS integration. Sharing rules must not reset
or rename a user's settings, input, theme or startup choice.

## Home focus

Directions below use logical start/end. A shell translates physical Left/Right
according to RTL exactly once. Up/Down retain their meaning in either language.
Prayer information and decorative artwork are not focus stops.

- The top controls are Today/Qibla, Inputs, Radio, Network/native settings and
  launcher Settings, in that logical order.
- Home starts at the first app when available, then a recent app, then All apps.
- Within an app row, start/end move one visible enabled item and stop at the edge.
  Vertical movement enters the adjacent content row, clamping incomplete rows to
  a real item. No invisible placeholder receives focus.
- Up from the first app row enters the top controls using Android's mapping:
  first column to Radio, last column to Settings, intermediate columns to Network.
  A single-column row follows Android's first-column rule. The other top controls
  remain reachable with horizontal movement. This is the owner's explicit choice.
- Down past the final app/recent row selects All apps. Decorative distance and
  other nearby buttons must not change that target.
- Moving back up from the final actions restores the last primary app focus when
  available; with no primary apps, use Today/Qibla, matching Android.
- LG's existing Watch input action remains reachable beside All apps. It does
  not replace the downward All apps target.
- OK activates the focused control. An active alert/editor owns its keys; Home
  navigation cannot escape into the background while a modal is active.
- Returning from All apps restores Home's All apps focus. Other return-focus
  behavior must use the originating semantic action, not DOM order or coordinates.

Home Back behavior must respect the shell's OS role: Android is a system HOME
launcher; LG is an app over an embedded input. Preserve that declared lifecycle
difference rather than treating it as an accidental focus-map difference.

## All apps focus

`AllAppsNavigation` owns the five-column grid and Back → Prayer → Settings header.
Up from every column of the first app row selects Back. Logical start/end cycles
through the header in either direction, including Back to Settings; horizontal
keys never descend to apps. Header Up stays put. Down from Back enters the first
visible app; Down from Prayer/Settings enters the final column of that visible row,
clamped to an existing app. Empty grids leave header focus intact.

Grid horizontal movement stops at row edges. Up/Down enters the adjacent row,
clamping a partial final row, and stays at the final row's bottom edge. Shells
retain native scrolling, activation, hold-OK menus and Back/return behavior. The
current scroll row is an explicit input; pixels and platform keys stay outside core.
Android, LG and the browser preview bind this rule and translate RTL once.

## Settings coverage

Common product concepts include location/calculation, six prayer adjustments,
Hijri correction, Qibla orientation, reminders/adhan/voices, station/resume behavior,
language/digits/clock, Home layout/prayer information/favourites, appearance and
save/cancel/reset actions. Test-alert actions and their bounded cleanup also have
stable IDs. Existing differences in defaults or option sets need explicit mappings.

OS-owned Home selection, global key interception, screensaver selection, panel-off
audio and global overlays require capabilities. App-local navigation/appearance
work cannot be excused as an OS limitation merely because it has not been ported.
The owner permits those features to remain explicitly unimplemented and hidden or
locked; initial LG policy hides them. Implemented controls must have actual working
bindings. This applies to every shell, without narrowing the universal definition.
An already static renderer needs an explicit reduced-motion policy; absence of a
toggle alone does not establish a behavioral failure or capability restriction.

## Enforcement

`shared/data/ui-contract.json` defines the Settings controls, six-offset editor,
five per-prayer adhan switches and LG test pane. Sunrise is informational, with
no adhan control or focus stop. `tools/catalogs.mjs` generates the portable `UiCatalog`; `UiContract`
checks IDs, kinds and coverage. Android Row objects tag their actual views; LG
rows/carousels expose `data-ui` and use the catalog's canonical label references.
The native `check=ui` instrumentation and LG browser suite enumerate those controls
in Arabic and English, then require every definition to be bound or declared in a
shell's gap file. See [declared gaps](UI-GAPS.md). These checks enforce control
coverage. Numeric range references reuse the core prayer/Hijri limits and shared
percentage bounds, plus room bearing 0–359°; native steppers validate their ranges and LG's editor consumes
them through its actual binding. Semantic choice values resolve canonical catalog
references during generation. Both renderers validate actual option models; native
popup lists also check their IDs/counts and LG validates the chosen offered value
before saving. Coverage aggregates countdown-position and imported-theme variants,
then requires explicit partial-option gaps. Stored keys, aliases and choice ordering
remain platform-owned. The actual five switches are exercised in both languages
in each shell, including persisted values and return focus. Native room-direction
checks cover physical compass directions in RTL/LTR, wraparound, save/cancel and
return to the originating control. Clockwise remains physical Right in either
language. The unset saved bearing stays unset until confirmed. LG's absent room
editor remains an explicit gap. Other linked actions require their own conformance.

Favourite menu actions use shared Add/Remove/Move earlier/Move later/Replace/Cancel
IDs and canonical labels; the native Uninstall entry has a separate app action ID.
Native bindings preserve existing package validation and four-to-nine-slot policy.
Conformance opens real long-press menus, checks the opening release cannot select
an action, and exercises mutations, visible favourite markers, cancellation and
return focus in both languages. Replacement confines focus to available slots;
save retains the chosen slot and Back retains the original order. The Uninstall
entry is checked without activating the OS operation. A test-APK fixture supplies
a removable app; preferences are restored exactly. LG's fixed shortcuts expose
none of these editing/uninstall actions, with explicit unimplemented gaps.

The implementation must provide one portable definition, shared focus rules and
per-shell bindings. Conformance checks must:

1. Fail on unknown/duplicate IDs and any required supported setting/action without
   a binding. Enumerate the actual registered/rendered controls, not a second
   hand-maintained list claiming those controls exist.
2. Exercise value kinds/choices/bounds and save/cancel round trips without changing
   established storage keys. Preserve deliberate platform defaults.
3. Run the same focus vectors through the JVM and ES5 rules, including empty and
   partial rows, RTL mapping, optional controls and return focus.
4. Test Android/LG adapters themselves, including Home Up/Down boundaries and modal
   key ownership. A passing pure-core test does not prove its renderer uses it.
5. Expose unimplemented features in the gap report even when their controls are
   hidden. OS-unsupported declarations require a platform reason; implementation
   state cannot be invented merely to make the report look complete.

Build/host tests complement physical TV checks. None alone proves native key
interception, screen overscan, Arabic glyph painting, decoder/audio or power behavior.
