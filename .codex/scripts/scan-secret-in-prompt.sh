#!/usr/bin/env bash
# Hook: UserPromptSubmit — quét secret trong prompt user.
# Output: decision=block nếu phát hiện pattern secret.
# Mirror logic của scan-secret-in-prompt.ps1 — cùng JSON output schema.
set -euo pipefail

payload=$(cat)
has_jq=0
if command -v jq >/dev/null 2>&1; then
  has_jq=1
fi

if [[ "$has_jq" -eq 1 ]]; then
  prompt=$(printf '%s' "$payload" | jq -r '.prompt // ""' 2>/dev/null || echo "")
else
  # Fallback khi thiếu jq: quét raw JSON để không fail-open với secret.
  prompt="$payload"
fi

if [ -z "${prompt// /}" ]; then
  echo '{"continue": true}'
  exit 0
fi

# 7 pattern secret — IDENTICAL với scan-secret-in-prompt.ps1
# Dùng grep -E -i để case-insensitive match
patterns=(
  'sk-[A-Za-z0-9_-]{20,}'                                          # OpenAI / Anthropic key
  'ghp_[A-Za-z0-9]{36,}'                                           # GitHub personal token
  'ghs_[A-Za-z0-9]{36,}'                                           # GitHub server token
  'AKIA[0-9A-Z]{16}'                                               # AWS access key
  'AIzaSy[A-Za-z0-9_-]{33}'                                        # Google API key
  'xox[baprs]-[A-Za-z0-9-]{10,}'                                   # Slack token
  '(password|passwd|pwd|api[_-]?key|secret)[[:space:]]*[:=][[:space:]]*[A-Za-z0-9_/+=-]{8,}'
)

for pat in "${patterns[@]}"; do
  if printf '%s' "$prompt" | grep -E -i -q "$pat"; then
    if [[ "$has_jq" -eq 1 ]]; then
      jq -nc --arg reason "Prompt có pattern giống secret/credential: $pat. Xóa secret trước khi gửi (Codex sẽ ghi vào history)." '{
        decision: "block",
        reason: $reason
      }'
    else
      printf '%s\n' '{"decision":"block","reason":"Prompt có pattern giống secret/credential. Xóa secret trước khi gửi."}'
    fi
    exit 0
  fi
done

if [[ "$has_jq" -eq 1 ]]; then
  echo '{"continue": true}'
else
  echo '{"continue": true, "systemMessage": "scan-secret-in-prompt: jq chưa cài, đã dùng fallback raw scanner"}'
fi
exit 0
