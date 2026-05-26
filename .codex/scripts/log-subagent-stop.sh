#!/usr/bin/env bash
# Hook: SubagentStop — ghi telemetry từng subagent cho $agent-roi.
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
model=$(json_string "model")
perm_mode=$(json_string "permission_mode")
cwd=$(json_string "cwd")
last_msg=$(json_string "last_assistant_message" | head -c 120 | tr ',\n\r' '   ' | sed "s/\"/'/g")

[ -z "$session_id" ] && session_id="unknown"
[ -z "$agent_id" ] && agent_id="unknown"
[ -z "$agent_type" ] && agent_type="unknown"
[ -z "$model" ] && model="unknown"
[ -z "$perm_mode" ] && perm_mode="default"

timestamp=$(date '+%Y-%m-%d %H:%M:%S')
now_ms=$(($(date +%s) * 1000))

mkdir -p .codex/subagent-start
safe_agent_id=$(printf '%s' "$agent_id" | tr -c 'A-Za-z0-9_.-' '_')
start_file=".codex/subagent-start/${safe_agent_id}.start"

duration_ms="N/A"
if [ -f "$start_file" ]; then
  start_ms=$(sed -n '1p' "$start_file" | tr -cd '0-9')
  if [ -n "$start_ms" ] && [ "$start_ms" -gt 0 ] && [ "$now_ms" -ge "$start_ms" ]; then
    duration_ms=$((now_ms - start_ms))
  fi
  rm -f "$start_file"
fi

csv_file=".codex/subagent-metrics.csv"
if [ ! -f "$csv_file" ]; then
  echo "timestamp,session_id,turn_id,agent_id,agent_type,model,permission_mode,duration_ms,cwd,last_msg_snippet,note" > "$csv_file"
fi

note="tokens=N/A;duration=SubagentStart-SubagentStop;transcript_format=unstable"
echo "$timestamp,$session_id,$turn_id,$agent_id,$agent_type,$model,$perm_mode,$duration_ms,$cwd,\"$last_msg\",$note" >> "$csv_file"

echo '{"continue": true}'
exit 0
