#!/usr/bin/env bash
# pre-bash-firewall.sh — Chặn lệnh nguy hiểm trước khi chạy
# Exit 2 = BLOCK, Exit 0 = ALLOW
set -euo pipefail

cmd=$(cat | jq -r '.tool_input.command // ""')

# Danh sách pattern bị chặn
deny_patterns=(
  'rm\s+-rf\s+/'
  'rm\s+-rf\s+\.'
  'git\s+reset\s+--hard'
  'git\s+push\s+.*--force'
  'git\s+clean\s+-fd'
  'drop\s+database'
  'drop\s+table'
  'truncate\s+table'
  'chmod\s+777'
  'curl.*\|\s*bash'
  'wget.*\|\s*bash'
)

for pat in "${deny_patterns[@]}"; do
  if echo "$cmd" | grep -Eiq "$pat"; then
    echo "BLOCKED: Lệnh khớp pattern nguy hiểm '$pat'. Dùng lệnh an toàn hơn hoặc giải thích lý do." >&2
    exit 2
  fi
done

exit 0
