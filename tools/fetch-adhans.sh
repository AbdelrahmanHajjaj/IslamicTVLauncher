#!/usr/bin/env bash
# Downloads the adhan recordings into app/src/main/assets/adhan/. They come from
# https://github.com/abodehq/Athan-MP3, which has no licence, so they are not committed to this repo.
# Without them Afnon still works; the call screen just stays silent.
set -e
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST="$ROOT/app/src/main/assets/adhan"
BASE="https://raw.githubusercontent.com/abodehq/Athan-MP3/master/Sounds"
mkdir -p "$DEST"
while IFS=$'\t' read -r id name; do
  [[ "$id" == \#* ]] && continue
  [ -f "$DEST/$id.mp3" ] && continue
  echo "  $id"
  curl -fsSL -o "$DEST/$id.mp3" "$BASE/$(printf '%s' "$name" | sed 's/ /%20/g')"
done < "$ROOT/tools/adhan-sources.tsv"
echo "Adhan recordings are in $DEST"
