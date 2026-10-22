# Security policy

## Supported versions

Only the latest release gets fixes. Afnon is debug-signed on purpose, so every release installs over the
previous one: update rather than staying on an old APK.

## What counts

Afnon runs with more access than most apps, so these matter most:

- **The accessibility service** (`RemoteKeys`). It can see key presses. Anything that makes it read, keep or
  send more than the buttons it handles is a security bug.
- **Secure settings.** With the `WRITE_SECURE_SETTINGS` grant Afnon writes TCL's button mappings
  (`CustomKeyList`, `CustomKey_<n>`). A way to make it write other settings, or mappings that crash or take
  over the TV, is a security bug.
- **Network.** The only connection allowed is the chosen radio station's stream (`Radio.STATIONS`), with plain
  HTTP for the Cairo stream only. Any other request, or a way to point the radio elsewhere, is a security bug.
- **Exported components and intents** that let another app start the call, change settings or launch apps
  as Afnon.
- **The web preview** (`site/`) if it can be made to run script from somewhere else or leak what a visitor stores.

Not in scope: the TV's firmware itself, someone with physical access to the remote or to ADB, and the radio
stations' own servers.

## Reporting

Please don't open a public issue. Report privately through GitHub:
[**Report a vulnerability**](https://github.com/egyjs/afnon-android-tv-launcher/security/advisories/new).

Include the TV model and Android version, the Afnon version, steps to reproduce and what an attacker gains.
Leave out your Wi-Fi name, IP addresses and serial numbers.

You'll get a reply within a week. Once a fix is released, the advisory is published with credit to you,
unless you'd rather stay anonymous.
