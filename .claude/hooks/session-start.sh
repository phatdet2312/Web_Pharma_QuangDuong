#!/usr/bin/env bash
# session-start.sh — Drift check + rework alert + memory size check.
# Works with: SessionStart (startup|resume)
# Output: JSON stdout with additionalContext, always exit 0. Compatible Bash 3.2+.
set -euo pipefail

emit_context() {
  local ctx="$1"
  if command -v jq >/dev/null 2>&1; then
    jq -nc --arg ctx "$ctx" '{hookSpecificOutput:{hookEventName:"SessionStart",additionalContext:$ctx}}'
  else
    local escaped
    escaped=$(printf '%s' "$ctx" | sed 's/\\/\\\\/g;s/"/\\"/g' | tr '\n' ' ')
    printf '{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"%s"}}\n' "$escaped"
  fi
}

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"
messages=""
msg_count=0

add_msg() {
  if [ "$msg_count" -gt 0 ]; then
    messages="$messages
- $1"
  else
    messages="- $1"
  fi
  msg_count=$((msg_count + 1))
}

# 1. Drift check
if [ -d "$PROJECT_DIR/.git" ]; then
  last_git=$(cd "$PROJECT_DIR" && git log --format=%h -1 2>/dev/null | tr -d '[:space:]' || true)
  log_file="$PROJECT_DIR/.ai-memory/06_evolution_log.md"
  if [ -n "$last_git" ] && [ -f "$log_file" ]; then
    last_logged=$(grep -oE '\b[a-f0-9]{7,40}\b' "$log_file" 2>/dev/null | head -n1 || true)
    if [ -n "$last_logged" ] && [ "$last_logged" != "$last_git" ]; then
      add_msg "DRIFT: HEAD=$last_git but memory records $last_logged. Run drift detection."
    fi
  fi
fi

# 2. Rework alert (check edit-history.log for recent repeated edits)
history_file="$PROJECT_DIR/.claude/edit-history.log"
if [ -f "$history_file" ]; then
  repeated=$(tail -n 100 "$history_file" 2>/dev/null | awk -F'|' '{gsub(/^[[:space:]]+|[[:space:]]+$/, "", $3); print $3}' | sort | uniq -c | sort -rn | head -5 | awk '$1 > 3 {print $1 "x " $2}')
  if [ -n "$repeated" ]; then
    add_msg "REWORK: Files edited many times recently: $repeated"
  fi
fi

# 3. Memory size check
for f in "$PROJECT_DIR/.ai-memory/06_evolution_log.md" "$PROJECT_DIR/.ai-memory/07_learnings.md"; do
  if [ -f "$f" ]; then
    size=$(wc -c < "$f" | tr -d '[:space:]')
    if [ "$size" -gt 51200 ] 2>/dev/null; then
      basename_f="${f##*/}"
      add_msg "LARGE: $basename_f is ${size} bytes (>50KB). Consider compacting."
    fi
  fi
done

if [ "$msg_count" -eq 0 ]; then
  echo '{"continue": true}'
  exit 0
fi

emit_context "Session start checks:
$messages"
exit 0
