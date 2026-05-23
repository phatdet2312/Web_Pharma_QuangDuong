#!/usr/bin/env bash
# post-edit-log.sh — Log every edited file to .claude/edit-history.log.
# Works with: PostToolUse (Edit|Write|MultiEdit)
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+.
set -euo pipefail

input=$(cat)
if command -v jq >/dev/null 2>&1; then
  tool_name=$(printf '%s' "$input" | jq -r '.tool_name // "unknown"')
  file_path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // .tool_input.path // "unknown"')
else
  tool_name=$(printf '%s' "$input" | sed -n 's/.*"tool_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$tool_name" ] && tool_name="unknown"
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
  [ -z "$file_path" ] && file_path="unknown"
fi

timestamp=$(date '+%Y-%m-%d %H:%M:%S')

LOG_DIR="${CLAUDE_PROJECT_DIR:-.}/.claude"
LOG_FILE="$LOG_DIR/edit-history.log"
mkdir -p "$LOG_DIR"

echo "$timestamp | $tool_name | $file_path" >> "$LOG_FILE"

echo '{"continue": true}'
exit 0
