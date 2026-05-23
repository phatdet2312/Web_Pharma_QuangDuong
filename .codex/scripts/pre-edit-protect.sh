#!/usr/bin/env bash
# Hook: PreToolUse khớp ^(apply_patch|Edit|Write|MultiEdit)$ — bảo vệ file quan trọng.
# Khớp logic Claude: substring match + BLOCK CỨNG (Claude exit 2).
#
# Input qua stdin (JSON):
#   - apply_patch: tool_input.input có chứa đường dẫn file
#   - Edit/Write : tool_input.file_path
#
# Output: permissionDecision="deny" (BLOCK cứng, tương đương exit 2 của Claude).
set -euo pipefail

input=$(cat)
has_jq=0
if command -v jq >/dev/null 2>&1; then
  has_jq=1
fi

if [[ "$has_jq" -eq 1 ]]; then
  tool_name=$(echo "$input" | jq -r '.tool_name // ""')
  hook_event=$(echo "$input" | jq -r '.hook_event_name // "PreToolUse"')
else
  tool_name="raw"
  hook_event=$(printf '%s' "$input" | sed -n 's/.*"hook_event_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
  [ -z "$hook_event" ] && hook_event="PreToolUse"
fi

# Trích xuất path tùy theo tool
case "$tool_name" in
  apply_patch)
    target=$(echo "$input" | jq -r '.tool_input.input // .tool_input.patch // .tool_input.command // ""')
    ;;
  Edit|Write|MultiEdit)
    target=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')
    ;;
  *)
    target="$input"
    ;;
esac

emit_deny() {
  local pf="$1"
  if [[ "$hook_event" == "PermissionRequest" ]]; then
    if [[ "$has_jq" -eq 1 ]]; then
      jq -nc --arg message "BLOCKED: File '$pf' được bảo vệ. Yêu cầu user xác nhận trước khi sửa." '{
        hookSpecificOutput: {
          hookEventName: "PermissionRequest",
          decision: { behavior: "deny", message: $message }
        }
      }'
    else
      printf '%s\n' '{"hookSpecificOutput":{"hookEventName":"PermissionRequest","decision":{"behavior":"deny","message":"Protected file approval request blocked by pre-edit-protect fallback scanner"}}}'
    fi
    return
  fi

  if [[ "$has_jq" -eq 1 ]]; then
    jq -nc --arg reason "BLOCKED: File '$pf' được bảo vệ. Yêu cầu user xác nhận trước khi sửa." '{
      hookSpecificOutput: {
        hookEventName: "PreToolUse",
        permissionDecision: "deny",
        permissionDecisionReason: $reason
      }
    }'
  else
    printf '%s\n' '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Protected file blocked by pre-edit-protect fallback scanner"}}'
  fi
}

load_protected_patterns() {
  local policy_file="${CODEX_PROJECT_ROOT:-$(pwd)}/.codexignore"
  local patterns=()

  if [[ -f "$policy_file" ]]; then
    while IFS= read -r line || [[ -n "$line" ]]; do
      [[ -z "${line//[[:space:]]/}" ]] && continue
      [[ "$line" =~ ^[[:space:]]*# ]] && continue

      if [[ "$line" == *"# sensitive"* || "$line" == *"# protected-edit"* ]]; then
        local pat="${line%%#*}"
        pat="$(printf '%s' "$pat" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
        [[ -n "$pat" ]] && patterns+=("$pat")
      fi
    done < "$policy_file"
  fi

  # Fail-soft fallback nếu .codexignore bị thiếu/hỏng. Danh sách chính thức vẫn nằm ở .codexignore.
  if [[ ${#patterns[@]} -eq 0 ]]; then
    patterns=(".env" ".env.*" ".codex/config.toml" ".codex/hooks.json" ".codex/hooks.windows.json" ".codexignore")
  fi

  printf '%s\n' "${patterns[@]}"
}

mapfile -t protected_files < <(load_protected_patterns)
normalized_target="${target//\\//}"

for pf in "${protected_files[@]}"; do
  normalized_pf="${pf//\\//}"
  case "$normalized_target" in
    *$normalized_pf*)
    emit_deny "$pf"
    exit 0
    ;;
  esac
done

if [[ "$has_jq" -eq 1 ]]; then
  echo '{"continue": true}'
else
  echo '{"continue": true, "systemMessage": "pre-edit-protect: jq chưa cài, đã dùng fallback raw scanner"}'
fi
exit 0
