#!/usr/bin/env bash
# Hook: PreToolUse khớp ^Bash$ — chặn lệnh nguy hiểm.
#
# Input qua stdin (JSON):
#   { "tool_name": "Bash", "tool_input": {"command": "rm -rf /"}, ... }
#
# Output qua stdout (JSON):
#   - Cho phép: { "continue": true }
#   - Chặn:     { "hookSpecificOutput": {"hookEventName":"PreToolUse",
#                  "permissionDecision":"deny",
#                  "permissionDecisionReason":"..."} }
#
# Exit code: 0 = thành công (Codex đọc stdout để quyết định)
set -euo pipefail

input=$(cat)
has_jq=0
if command -v jq >/dev/null 2>&1; then
  has_jq=1
fi

if [[ "$has_jq" -eq 1 ]]; then
  cmd=$(echo "$input" | jq -r '.tool_input.command // ""')
  hook_event=$(echo "$input" | jq -r '.hook_event_name // "PreToolUse"')
else
  # Fallback khi thiếu jq: quét raw JSON để vẫn chặn pattern nguy hiểm thay vì fail-open.
  cmd="$input"
  hook_event=$(printf '%s' "$input" | sed -n 's/.*"hook_event_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$hook_event" ] && hook_event="PreToolUse"
fi

emit_deny() {
  local reason="$1"
  if [[ "$hook_event" == "PermissionRequest" ]]; then
    if [[ "$has_jq" -eq 1 ]]; then
      jq -nc --arg message "$reason" '{
        hookSpecificOutput: {
          hookEventName: "PermissionRequest",
          decision: { behavior: "deny", message: $message }
        }
      }'
    else
      printf '%s\n' '{"hookSpecificOutput":{"hookEventName":"PermissionRequest","decision":{"behavior":"deny","message":"Dangerous approval request blocked by pre-bash-firewall fallback scanner"}}}'
    fi
    return
  fi

  if [[ "$has_jq" -eq 1 ]]; then
    jq -nc --arg reason "$reason" '{
      hookSpecificOutput: {
        hookEventName: "PreToolUse",
        permissionDecision: "deny",
        permissionDecisionReason: $reason
      }
    }'
  else
    printf '%s\n' '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Dangerous command blocked by pre-bash-firewall fallback scanner"}}'
  fi
}

deny_patterns=(
  'rm[[:space:]]+-rf[[:space:]]+/'
  'rm[[:space:]]+-rf[[:space:]]+\.'
  'git[[:space:]]+reset[[:space:]]+--hard'
  'git[[:space:]]+push[[:space:]]+.*--force'
  'git[[:space:]]+clean[[:space:]]+-fd'
  'drop[[:space:]]+database'
  'drop[[:space:]]+table'
  'truncate[[:space:]]+table'
  'chmod[[:space:]]+777'
  'curl.*\|[[:space:]]*bash'
  'wget.*\|[[:space:]]*bash'
)

for pat in "${deny_patterns[@]}"; do
  if echo "$cmd" | grep -Eiq "$pat"; then
    emit_deny "Lệnh khớp pattern nguy hiểm: $pat"
    exit 0
  fi
done

if [[ "$has_jq" -eq 1 ]]; then
  echo '{"continue": true}'
else
  echo '{"continue": true, "systemMessage": "pre-bash-firewall: jq chưa cài, đã dùng fallback raw scanner"}'
fi
exit 0
