#!/usr/bin/env bash
# Hook: SubagentStart — ghi mốc bắt đầu subagent để tính ROI.
set -euo pipefail

input=$(cat)

json_string() {
  local key="$1"
  if command -v jq >/dev/null 2>&1; then
    printf '%s' "$input" | jq -r --arg key "$key" '.[$key] // ""'
  else
    printf '%s' "$input" | sed -n "s/.*\"$key\"[[:space:]]*:[[:space:]]*\"\\([^\"]*\\)\".*/\\1/p"
  fi
}

session_id=$(json_string "session_id")
turn_id=$(json_string "turn_id")
agent_id=$(json_string "agent_id")
agent_type=$(json_string "agent_type")

[ -z "$session_id" ] && session_id="unknown"
[ -z "$agent_id" ] && agent_id="unknown"
[ -z "$agent_type" ] && agent_type="unknown"

safe_agent_id=$(printf '%s' "$agent_id" | tr -c 'A-Za-z0-9_.-' '_')
mkdir -p .codex/subagent-start

now_ms=$(($(date +%s) * 1000))
start_file=".codex/subagent-start/${safe_agent_id}.start"
printf '%s\n%s\n%s\n%s\n' "$now_ms" "$session_id" "$turn_id" "$agent_type" > "$start_file"

echo '{"continue": true}'
exit 0
