#!/usr/bin/env bash
# Hook: Stop — ghi telemetry turn chính vào .codex/agent-metrics.csv.
#
# Codex 0.133 đã có SubagentStart/SubagentStop, nên telemetry từng subagent
# được ghi bởi log-subagent-start/stop vào .codex/subagent-metrics.csv.
# Script này chỉ giữ aggregate telemetry cho turn chính.
set -euo pipefail

input=$(cat)

extract_json_string() {
  local key="$1"
  printf '%s' "$input" | sed -n "s/.*\"$key\"[[:space:]]*:[[:space:]]*\"\\([^\"]*\\)\".*/\\1/p"
}

if command -v jq >/dev/null 2>&1; then
  session_id=$(echo "$input" | jq -r '.session_id // "unknown"')
  turn_id=$(echo "$input" | jq -r '.turn_id // ""')
  model=$(echo "$input" | jq -r '.model // "unknown"')
  perm_mode=$(echo "$input" | jq -r '.permission_mode // "default"')
  cwd=$(echo "$input" | jq -r '.cwd // ""')
  last_msg=$(echo "$input" | jq -r '.last_assistant_message // ""' \
    | head -c 80 | tr ',\n\r' '   ' | sed "s/\"/'/g")
else
  session_id=$(extract_json_string "session_id")
  turn_id=$(extract_json_string "turn_id")
  model=$(extract_json_string "model")
  perm_mode=$(extract_json_string "permission_mode")
  cwd=$(extract_json_string "cwd")
  last_msg=$(extract_json_string "last_assistant_message" | head -c 80 | tr ',\n\r' '   ' | sed "s/\"/'/g")
  [ -z "$session_id" ] && session_id="unknown"
  [ -z "$model" ] && model="unknown"
  [ -z "$perm_mode" ] && perm_mode="default"
fi

timestamp=$(date '+%Y-%m-%d %H:%M:%S')
now_epoch=$(date +%s)

mkdir -p .codex
last_stop_file=".codex/.last_stop_epoch"
csv_file=".codex/agent-metrics.csv"

duration_approx_ms="N/A"
if [ -f "$last_stop_file" ]; then
  last_epoch=$(tr -cd '0-9' < "$last_stop_file" 2>/dev/null || echo 0)
  [ -z "$last_epoch" ] && last_epoch=0
  if [ "$last_epoch" -gt 0 ]; then
    delta=$((now_epoch - last_epoch))
    if [ "$delta" -lt 3600 ]; then
      duration_approx_ms=$((delta * 1000))
    fi
  fi
fi
echo "$now_epoch" > "$last_stop_file"

if [ ! -f "$csv_file" ]; then
  echo "timestamp,session_id,turn_id,model,permission_mode,duration_approx_ms,cwd,last_msg_snippet,note" > "$csv_file"
fi

# Stop hook payload chưa expose token usage. Transcript parsing không phải
# interface ổn định cho hook, nên CSV aggregate giữ token là N/A.
note="tokens=N/A;scope=main-turn;duration=approx"
echo "$timestamp,$session_id,$turn_id,$model,$perm_mode,$duration_approx_ms,$cwd,\"$last_msg\",$note" >> "$csv_file"

echo '{"continue": true}'
exit 0
