#!/usr/bin/env bash
# pre-bash-firewall.sh — Block dangerous commands before execution.
# Works with: PreToolUse (Bash) + PermissionRequest (Bash)
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+.
set -euo pipefail

input=$(cat)
has_jq=0
command -v jq >/dev/null 2>&1 && has_jq=1

if [ "$has_jq" -eq 1 ]; then
  cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // ""')
  hook_event=$(printf '%s' "$input" | jq -r '.hook_event_name // "PreToolUse"')
else
  cmd="$input"
  hook_event=$(printf '%s' "$input" | sed -n 's/.*"hook_event_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$hook_event" ] && hook_event="PreToolUse"
fi

emit_deny() {
  local reason="$1"
  if [ "$hook_event" = "PermissionRequest" ]; then
    if [ "$has_jq" -eq 1 ]; then
      jq -nc --arg m "$reason" '{hookSpecificOutput:{hookEventName:"PermissionRequest",decision:{behavior:"deny",message:$m}}}'
    else
      printf '{"hookSpecificOutput":{"hookEventName":"PermissionRequest","decision":{"behavior":"deny","message":"Dangerous command blocked by firewall"}}}\n'
    fi
  else
    if [ "$has_jq" -eq 1 ]; then
      jq -nc --arg r "$reason" '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"deny",permissionDecisionReason:$r}}'
    else
      printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Dangerous command blocked by firewall"}}\n'
    fi
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
  if printf '%s' "$cmd" | grep -Eiq "$pat"; then
    emit_deny "Blocked: command matches dangerous pattern: $pat"
    exit 0
  fi
done

echo '{"continue": true}'
exit 0
