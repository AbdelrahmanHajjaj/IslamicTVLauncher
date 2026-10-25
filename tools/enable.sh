#!/usr/bin/env bash
# Reversible enable; see tools/device.md. Python 3.9+ required.
set -euo pipefail
. "$(dirname "$0")/env.sh"
exec "${PYTHON:-python3}" "$ROOT/tools/device.py" --tv "$TV" --adb "$ADB_BIN" enable "$@"
