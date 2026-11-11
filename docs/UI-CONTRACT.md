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
| Capabilities | Explicit conditions for OS-dependent behavior | Declare supported or unsupported with a concrete reason/evidence; missing portable work remains a gap. |
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

## Settings coverage

Common product concepts include location/calculation, six prayer adjustments,
Hijri correction, Qibla orientation, reminders/adhan/voices, station/resume behavior,
language/digits/clock, Home layout/prayer information/favourites, appearance and
save/cancel/reset actions. Test-alert actions and their bounded cleanup also have
stable IDs. Existing differences in defaults or option sets need explicit mappings.

OS-owned Home selection, global key interception, screensaver selection, panel-off
audio and global overlays require capabilities. App-local navigation/appearance
work cannot be excused as an OS limitation merely because it has not been ported.
An already static renderer needs an explicit reduced-motion policy; absence of a
toggle alone does not establish a behavioral failure or capability restriction.

## Enforcement

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
5. Expose existing missing common features as explicit migration gaps. Unsupported
   declarations require a platform reason; they cannot silently turn gaps green.

Build/host tests complement physical TV checks. None alone proves native key
interception, screen overscan, Arabic glyph painting, decoder/audio or power behavior.
