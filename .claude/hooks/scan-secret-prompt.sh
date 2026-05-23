#!/usr/bin/env bash
# scan-secret-prompt.sh — Block user prompts containing secrets/credentials.
# Works with: UserPromptSubmit
# Output: JSON stdout, always exit 0. Compatible Bash 3.2+.
set -euo pipefail

payload=$(cat)
has_jq=0
command -v jq >/dev/null 2>&1 && has_jq=1

if [ "$has_jq" -eq 1 ]; then
  prompt=$(printf '%s' "$payload" | jq -r '.prompt // ""' 2>/dev/null || echo "")
else
  prompt="$payload"
fi

if [ -z "${prompt// /}" ]; then
  echo '{"continue": true}'
  exit 0
fi

patterns=(
  'sk-[A-Za-z0-9_-]{20,}'
  'ghp_[A-Za-z0-9]{36,}'
  'ghs_[A-Za-z0-9]{36,}'
  'AKIA[0-9A-Z]{16}'
  'AIzaSy[A-Za-z0-9_-]{33}'
  'xox[baprs]-[A-Za-z0-9-]{10,}'
  '(password|passwd|pwd|api[_-]?key|secret)[[:space:]]*[:=][[:space:]]*[A-Za-z0-9_/+=-]{8,}'
)

for pat in "${patterns[@]}"; do
  if printf '%s' "$prompt" | grep -E -i -q "$pat"; then
    if [ "$has_jq" -eq 1 ]; then
      jq -nc --arg r "Prompt contains pattern resembling a secret/credential: $pat. Remove the secret before sending." '{decision:"block",reason:$r}'
    else
      printf '{"decision":"block","reason":"Prompt contains pattern resembling a secret/credential. Remove before sending."}\n'
    fi
    exit 0
  fi
done

echo '{"continue": true}'
exit 0
