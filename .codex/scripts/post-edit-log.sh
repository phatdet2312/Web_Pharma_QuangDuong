#!/usr/bin/env bash
# Hook: PostToolUse khớp ^(apply_patch|Edit|Write|MultiEdit)$
# Ghi log mọi file đã sửa vào .codex/edit-history.log
set -euo pipefail

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  tool_name=$(echo "$input" | jq -r '.tool_name // "unknown"')
  file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // "(patch)"')
  patch_text=$(echo "$input" | jq -r '.tool_input.input // .tool_input.patch // .tool_input.command // ""')
else
  tool_name=$(printf '%s' "$input" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$tool_name" ] && tool_name="unknown"
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
  [ -z "$file_path" ] && file_path="(patch)"
  patch_text="$input"
fi

extract_patch_paths() {
  printf '%s\n' "$patch_text" \
    | grep -oE '\*\*\* (Update|Add|Delete) File: [^\\"]+' 2>/dev/null \
    | sed -E 's/^\*\*\* (Update|Add|Delete) File: //'
}

paths=()
if [ "$file_path" != "(patch)" ] && [ -n "$file_path" ]; then
  paths+=("$file_path")
else
  while IFS= read -r p; do
    [ -n "$p" ] && paths+=("$p")
  done < <(extract_patch_paths)
fi
[ "${#paths[@]}" -eq 0 ] && paths+=("(patch)")

timestamp=$(date '+%Y-%m-%d %H:%M:%S')

LOG_FILE=".codex/edit-history.log"
mkdir -p "$(dirname "$LOG_FILE")"
for p in "${paths[@]}"; do
  echo "$timestamp | $tool_name | $p" >> "$LOG_FILE"
done

echo '{"continue": true}'
exit 0
