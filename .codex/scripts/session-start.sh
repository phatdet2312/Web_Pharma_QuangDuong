#!/usr/bin/env bash
# Hook: SessionStart — drift + rework alert + memory size check.
# Output: hookSpecificOutput.additionalContext (Codex chèn vào đầu phiên).
# Mirror logic của session-start.ps1 — cùng JSON output schema.
set -euo pipefail

emit_context() {
  local ctx="$1"
  if command -v jq >/dev/null 2>&1; then
    jq -nc --arg ctx "$ctx" '{
      hookSpecificOutput: {
        hookEventName: "SessionStart",
        additionalContext: $ctx
      }
    }'
  else
    local escaped
    escaped=$(printf '%s' "$ctx" | sed ':a;N;$!ba;s/\\/\\\\/g;s/"/\\"/g;s/\r//g;s/\n/\\n/g')
    printf '{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"%s"}}\n' "$escaped"
  fi
}

messages=()

# 1. Drift check (chỉ nếu có .git)
if [ -d ".git" ]; then
  last_git=$(git log --format=%h -1 2>/dev/null | tr -d '[:space:]' || true)
  log_file=".ai-memory/06_evolution_log.md"
  if [ -n "$last_git" ] && [ -f "$log_file" ]; then
    # Tìm SHA đầu tiên (7-40 hex chars) trong evolution log
    last_logged=$(grep -oE '\b[a-f0-9]{7,40}\b' "$log_file" 2>/dev/null | head -n1 || true)
    if [ -n "$last_logged" ] && [ "$last_logged" != "$last_git" ]; then
      messages+=("⚠️ Drift detected: HEAD=$last_git, memory ghi nhận khác. Đề xuất chạy \$detect-drift.")
    fi
  fi
fi

# 2. Rework alert trong 24h
alert_file=".codex/rework-alerts.log"
if [ -f "$alert_file" ]; then
  # Cutoff = now - 24h (epoch seconds)
  if cutoff=$(date -d '24 hours ago' +%s 2>/dev/null); then
    :
  else
    # macOS BSD date fallback
    cutoff=$(date -v-24H +%s 2>/dev/null || echo 0)
  fi

  count=0
  while IFS= read -r line; do
    # Format dòng: "<timestamp> | ..."
    ts=$(printf '%s' "$line" | cut -d'|' -f1 | sed 's/[[:space:]]*$//')
    [ -z "$ts" ] && continue
    if ts_epoch=$(date -d "$ts" +%s 2>/dev/null); then
      :
    else
      ts_epoch=$(date -j -f '%Y-%m-%d %H:%M:%S' "$ts" +%s 2>/dev/null || echo 0)
    fi
    if [ "$ts_epoch" -gt "$cutoff" ] 2>/dev/null; then
      count=$((count + 1))
    fi
  done < "$alert_file"

  if [ "$count" -gt 0 ]; then
    messages+=("⚠️ Rework alerts trong 24h: $count file bị edit nhiều lần. Đề xuất chạy \$reflect ngầm.")
  fi
fi

# 3. Memory size check
for f in ".ai-memory/06_evolution_log.md" ".ai-memory/07_learnings.md"; do
  if [ -f "$f" ]; then
    # wc -c chính xác cross-platform
    size=$(wc -c < "$f" | tr -d '[:space:]')
    if [ "$size" -gt 51200 ] 2>/dev/null; then
      messages+=("📦 $f đã > 50KB. Đề xuất chạy \$compact-memory để nén log cũ.")
    fi
  fi
done

if [ "${#messages[@]}" -eq 0 ]; then
  echo '{"continue": true}'
  exit 0
fi

# Build additionalContext: "Session start checks:\n- msg1\n- msg2..."
ctx="Session start checks:"
for m in "${messages[@]}"; do
  ctx+=$'\n- '"$m"
done

emit_context "$ctx"
exit 0
