#!/usr/bin/env bash
# Hook: Stop — ghi telemetry cuối turn vào .codex/agent-metrics.csv (cho $agent-roi).
#
# GIỚI HẠN CODEX 2026 (verify 16/5/2026 — developers.openai.com/codex/hooks):
# Stop event CHỈ expose: session_id, transcript_path, cwd, hook_event_name, model,
# permission_mode, turn_id, stop_hook_active, last_assistant_message.
# KHÔNG có: usage (input/output_tokens), duration_ms, agent_name (cho subagent), task_summary.
#
# Workaround:
# - Token count → ghi N/A. Skill $agent-roi parse transcript_path nếu cần (caveat: format không stable).
# - Duration → tính approximation từ delta giữa 2 lần Stop liên tiếp (lưu last_stop_epoch).
# - agent_name → null. Codex 2026 không có event SubagentStop riêng → không gắn metric vào subagent name được.
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
  # Snippet của last_assistant_message để có context (cut 80 chars, escape)
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
LAST_STOP_FILE=".codex/.last_stop_epoch"
CSV_FILE=".codex/agent-metrics.csv"

# Tính approximation duration: delta từ Stop trước (không phải start of turn — chỉ ước lượng)
duration_approx_ms="N/A"
if [ -f "$LAST_STOP_FILE" ]; then
  last_epoch=$(tr -cd '0-9' < "$LAST_STOP_FILE" 2>/dev/null || echo 0)
  [ -z "$last_epoch" ] && last_epoch=0
  if [ "$last_epoch" -gt 0 ]; then
    delta=$((now_epoch - last_epoch))
    # Chỉ ghi nếu < 1 giờ (loại bỏ trường hợp gap dài giữa các phiên)
    if [ "$delta" -lt 3600 ]; then
      duration_approx_ms=$((delta * 1000))
    fi
  fi
fi
echo "$now_epoch" > "$LAST_STOP_FILE"

# CSV header
if [ ! -f "$CSV_FILE" ]; then
  echo "timestamp,session_id,turn_id,model,permission_mode,duration_approx_ms,cwd,last_msg_snippet,note" > "$CSV_FILE"
fi

# Note: ghi rõ tokens N/A vì Codex 2026 chưa expose
note="tokens=N/A;agent_name=N/A;duration=approx"

echo "$timestamp,$session_id,$turn_id,$model,$perm_mode,$duration_approx_ms,$cwd,\"$last_msg\",$note" >> "$CSV_FILE"

echo '{"continue": true}'
exit 0
