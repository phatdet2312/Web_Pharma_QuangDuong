#!/usr/bin/env bash
# post-subagent-log.sh — Ghi log mỗi khi subagent chạy xong
# Giúp verify agent nào đã được gọi cho task nào
set -euo pipefail

input=$(cat)
agent_name=$(echo "$input" | jq -r '.agent_name // "unknown"')
agent_model=$(echo "$input" | jq -r '.agent_model // "unknown"')
timestamp=$(date '+%Y-%m-%d %H:%M:%S')

echo "$timestamp | SUBAGENT | $agent_name | model: $agent_model" >> "${CLAUDE_PROJECT_DIR:-.}/.claude/subagent-history.log"

exit 0
