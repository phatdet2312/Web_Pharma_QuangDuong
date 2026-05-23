#!/usr/bin/env bash
# pre-edit-protect.sh — Block edits to protected files.
# Works with: PreToolUse (Edit|Write|MultiEdit) + PermissionRequest
# Reads protected patterns from .claudeignore (# protected-edit tag).
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+.
set -euo pipefail

input=$(cat)
has_jq=0
command -v jq >/dev/null 2>&1 && has_jq=1

if [ "$has_jq" -eq 1 ]; then
  tool_name=$(printf '%s' "$input" | jq -r '.tool_name // ""')
  hook_event=$(printf '%s' "$input" | jq -r '.hook_event_name // "PreToolUse"')
  file_path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
else
  tool_name="raw"
  hook_event=$(printf '%s' "$input" | sed -n 's/.*"hook_event_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$hook_event" ] && hook_event="PreToolUse"
  file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  if [ -z "$file_path" ]; then
    file_path=$(printf '%s' "$input" | sed -n 's/.*"path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  fi
fi

if [ -z "$file_path" ]; then
  echo '{"continue": true}'
  exit 0
fi

emit_deny() {
  local pf="$1"
  local reason="BLOCKED: File '$pf' is protected. User confirmation required."
  if [ "$hook_event" = "PermissionRequest" ]; then
    if [ "$has_jq" -eq 1 ]; then
      jq -nc --arg m "$reason" '{hookSpecificOutput:{hookEventName:"PermissionRequest",decision:{behavior:"deny",message:$m}}}'
    else
      printf '{"hookSpecificOutput":{"hookEventName":"PermissionRequest","decision":{"behavior":"deny","message":"Protected file blocked by hook"}}}\n'
    fi
  else
    if [ "$has_jq" -eq 1 ]; then
      jq -nc --arg r "$reason" '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"deny",permissionDecisionReason:$r}}'
    else
      printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Protected file blocked by hook"}}\n'
    fi
  fi
}

load_protected_patterns() {
  local policy_file="${CLAUDE_PROJECT_DIR:-.}/.claudeignore"
  local patterns_found=0

  if [ -f "$policy_file" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
      case "$line" in
        ""|\#*) continue ;;
      esac
      case "$line" in
        *"# sensitive"*|*"# protected-edit"*|*"#sensitive"*|*"#protected-edit"*)
          local pat
          pat=$(printf '%s' "$line" | sed 's/[[:space:]]*#.*$//' | sed 's/^[[:space:]]*//' | sed 's/[[:space:]]*$//')
          if [ -n "$pat" ]; then
            printf '%s\n' "$pat"
            patterns_found=1
          fi
          ;;
      esac
    done < "$policy_file"
  fi

  if [ "$patterns_found" -eq 0 ]; then
    printf '%s\n' ".env" ".env.*" ".claude/settings.json" ".claude/settings.local.json" ".claudeignore"
  fi
}

normalized_target="${file_path//\\//}"

while IFS= read -r pf; do
  [ -z "$pf" ] && continue
  normalized_pf="${pf//\\//}"
  case "$normalized_target" in
    *$normalized_pf*)
      emit_deny "$pf"
      exit 0
      ;;
  esac
done < <(load_protected_patterns)

echo '{"continue": true}'
exit 0
