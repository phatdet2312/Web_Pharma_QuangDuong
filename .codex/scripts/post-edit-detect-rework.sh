#!/usr/bin/env bash
# Hook: PostToolUse — phát hiện file edit >3 lần trong 30 phút.
# Ghi alert vào .codex/rework-alerts.log để session-start.sh tải đầu phiên sau.
set -euo pipefail

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  tool_name=$(echo "$input" | jq -r '.tool_name // ""')
  file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
  patch_text=$(echo "$input" | jq -r '.tool_input.input // .tool_input.patch // .tool_input.command // ""')
else
  tool_name=$(printf '%s' "$input" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
  patch_text="$input"
fi

# Bỏ qua nếu không phải edit thật, hoặc đường dẫn vùng meta
case "$tool_name" in
  Edit|Write|MultiEdit|apply_patch) ;;
  *) echo '{"continue": true}'; exit 0 ;;
esac

extract_patch_paths() {
  printf '%s\n' "$patch_text" \
    | grep -oE '\*\*\* (Update|Add|Delete) File: [^\\"]+' 2>/dev/null \
    | sed -E 's/^\*\*\* (Update|Add|Delete) File: //'
}

paths=()
if [[ -n "$file_path" ]]; then
  paths+=("$file_path")
else
  while IFS= read -r p; do
    [ -n "$p" ] && paths+=("$p")
  done < <(extract_patch_paths)
fi
[ "${#paths[@]}" -eq 0 ] && { echo '{"continue": true}'; exit 0; }

HISTORY_FILE=".codex/edit-history.log"
ALERT_FILE=".codex/rework-alerts.log"
mkdir -p "$(dirname "$HISTORY_FILE")"
[ ! -f "$HISTORY_FILE" ] && touch "$HISTORY_FILE"
[ ! -f "$ALERT_FILE" ] && touch "$ALERT_FILE"

now=$(date +%s)
threshold_ago=$((now - 1800))   # 30 phút

for file_path in "${paths[@]}"; do
  if [[ -z "$file_path" ]] \
    || [[ "$file_path" == *".ai-memory/"* ]] \
    || [[ "$file_path" == *".codex/"* ]] \
    || [[ "$file_path" == *".agents/"* ]] \
    || [[ "$file_path" == *".log" ]]; then
    continue
  fi

  count=0
  while IFS='|' read -r ts_str tool path_str rest; do
    ts_str=$(echo "$ts_str" | xargs)
    path_str=$(echo "$path_str" | xargs)
    if [[ "$path_str" == "$file_path" ]]; then
      ts_epoch=$(date -d "$ts_str" +%s 2>/dev/null || echo 0)
      if [ "$ts_epoch" -gt "$threshold_ago" ]; then
        count=$((count + 1))
      fi
    fi
  done < <(tail -n 200 "$HISTORY_FILE")

  if [ "$count" -gt 3 ]; then
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "$timestamp | REWORK_ALERT | $file_path | edited $count times in 30min | hint: consider \$reflect to capture pattern" >> "$ALERT_FILE"
  fi
done

echo '{"continue": true}'
exit 0
