#!/usr/bin/env bash
# post-edit-log.sh — Ghi log mọi file đã sửa vào .claude/edit-history.log
# Chạy SAU khi tool Edit/Write hoàn thành
set -euo pipefail

input=$(cat)
file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // "unknown"')
tool_name=$(echo "$input" | jq -r '.tool_name // "unknown"')
timestamp=$(date '+%Y-%m-%d %H:%M:%S')

echo "$timestamp | $tool_name | $file_path" >> "${CLAUDE_PROJECT_DIR:-.}/.claude/edit-history.log"

exit 0
