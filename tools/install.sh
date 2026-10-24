#!/usr/bin/env bash
# Builds Afnon, installs it over the running copy and brings it straight back as the home screen. Also sets
# what Afnon needs from the system: HOME, the remote buttons service, the screensaver, TCL's auto-start
# grants, two permissions the radio uses and location for the Wi-Fi name.
set -e
. "$(dirname "$0")/env.sh"
[ -n "$(ls "$ROOT/app/src/main/assets/adhan/"*.mp3 2>/dev/null)" ] || bash "$ROOT/tools/fetch-adhans.sh"
[ -n "$(ls "$ROOT/app/src/main/assets/announce/"*.mp3 2>/dev/null)" ] || bash "$ROOT/tools/fetch-announce.sh" || true
(cd "$ROOT" && ./gradlew :app:assembleDebug -q)
adb install -r "$ROOT/app/build/outputs/apk/debug/app-debug.apk" | tail -1
# The setup steps, shared with the phone installer: tools/setup-steps.txt (required|en|ar|command).
grep -v '^#' "$ROOT/tools/setup-steps.txt" | while IFS='|' read -r required en ar cmd; do
  [ -n "$cmd" ] || continue
  if adb shell "$cmd" </dev/null >/dev/null 2>&1; then echo "  ok   $en"
  elif [ "$required" = 1 ]; then echo "  FAIL $en" >&2; exit 1
  else echo "  skip $en"; fi
done
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME >/dev/null
echo "Afnon installed and on screen."
