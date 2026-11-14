<!-- Arabic or English. Target AbdelrahmanHajjaj/IslamicTVLauncher:main.
     Use CONTRIBUTING.md and TESTING.md; remove instructions when filling this in. -->

## Change and reason

<!-- Describe the problem, resulting behavior and relevant issue. -->

## Architecture and compatibility

<!-- Name affected modules/consumers: core, data/design, Android TV, LG, preview,
     installer/tools, docs. For shared behavior, state the canonical source and
     each binding/gap. Describe preference/ID/signing, lifecycle, dependency,
     permission or network changes; write "None" where appropriate. -->

## Verification

<!-- List the relevant TESTING.md checks with actual results. Include commit/app
     version, device/model/OS and evidence type. Write pending or N/A with a reason;
     do not turn emulator, mock or DOM evidence into physical-TV confirmation. -->

| Check / command | Runtime or device | Result |
| --- | --- | --- |
| | | |

<!-- For UI/input: Arabic/English, RTL/LTR, size bounds, OK/Apply/Back and return
     focus. For hardware changes: preserving upgrade and affected media, remote,
     input/startup/lifecycle checks. Attach sanitized screenshots when useful,
     labeled browser/emulator/TV; record human LG picture/sound observations. -->

## Remaining limits

<!-- Untested paths, unavailable hardware and known risks, or "None". -->

## Before review

- [ ] Required checks are recorded above; pending acceptance gates stay explicit.
- [ ] Shared UI/input rules use core/catalog IDs; affected consumers and gaps agree.
- [ ] Existing settings, installed identities and third-party notices are preserved.
- [ ] Owning docs are current; user-visible changes have a Changelog entry.
- [ ] Diff contains no private device/account data, keys, local configuration,
      `state/`, build outputs, APK/IPK files or restricted recordings.
