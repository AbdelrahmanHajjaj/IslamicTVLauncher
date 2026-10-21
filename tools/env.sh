# Shared settings for the tools. Set TV to your TV's address first, for example:
#   export TV=192.168.1.50:5555
# ADB_BIN defaults to the adb on your PATH.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ADB_BIN="${ADB_BIN:-adb}"
if [ -z "$TV" ]; then
  echo "Set TV=<ip>:5555 first (the TV's address, shown in its network settings)." >&2
  exit 1
fi
adb() { "$ADB_BIN" -s "$TV" "$@"; }
