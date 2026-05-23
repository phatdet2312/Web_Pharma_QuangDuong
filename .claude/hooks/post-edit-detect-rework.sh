#!/usr/bin/env bash
# post-edit-detect-rework.sh — Detect file edited >3 times in ~30 minutes.
# Works with: PostToolUse (Edit|Write|MultiEdit)
# Writes alert to .claude/rework-alerts.log; session-start.sh reads it next session.
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+, GNU+BSD date.
set -euo pipefail

input=$(cat)
has_jq=0
command -v jq >/dev/null 2>&1 && has_jq=1

if [ "$has_jq" -eq 1 ]; then
  tool_name=$(printf '%s' "$input" | jq -r '.tool_name // ""')
  file_path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
else
  tool_name=$(printf '%s' "$input" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
fi

case "$tool_name" in
  Edit|Write|MultiEdit) ;;
  *) echo '{"continue": true}'; exit 0 ;;
esac

if [ -z "$file_path" ] \
  || [ "${file_path#*.ai-memory/}" != "$file_path" ] \
  || [ "${file_path#*.claude/}" != "$file_path" ] \
  || [ "${file_path#*.agents/}" != "$file_path" ] \
  || [ "${file_path%.log}" != "$file_path" ]; then
  echo '{"continue": true}'
  exit 0
fi

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"
HISTORY_FILE="$PROJECT_DIR/.claude/edit-history.log"
ALERT_FILE="$PROJECT_DIR/.claude/rework-alerts.log"

mkdir -p "$(dirname "$HISTORY_FILE")"
[ ! -f "$HISTORY_FILE" ] && { echo '{"continue": true}'; exit 0; }

now=$(date +%s)
threshold_ago=$((now - 1800))

parse_epoch() {
  local ts="$1"
  date -d "$ts" +%s 2>/dev/null && return 0
  date -j -f '%Y-%m-%d %H:%M:%S' "$ts" +%s 2>/dev/null && return 0
  echo "0"
}

count=0
while IFS='|' read -r ts_str _tool path_str _rest; do
  ts_str="${ts_str#"${ts_str%%[![:space:]]*}"}"
  ts_str="${ts_str%"${ts_str##*[![:space:]]}"}"
  path_str="${path_str#"${path_str%%[![:space:]]*}"}"
  path_str="${path_str%"${path_str##*[![:space:]]}"}"

  if [ "$path_str" = "$file_path" ]; then
    ts_epoch=$(parse_epoch "$ts_str")
    if [ "$ts_epoch" -gt "$threshold_ago" ] 2>/dev/null; then
      count=$((count + 1))
    fi
  fi
done < <(tail -n 200 "$HISTORY_FILE")

if [ "$count" -gt 3 ]; then
  timestamp=$(date '+%Y-%m-%d %H:%M:%S')
  mkdir -p "$(dirname "$ALERT_FILE")"
  echo "$timestamp | REWORK_ALERT | $file_path | edited $count times in 30min | hint: consider /reflect" >> "$ALERT_FILE"
fi

echo '{"continue": true}'
exit 0
