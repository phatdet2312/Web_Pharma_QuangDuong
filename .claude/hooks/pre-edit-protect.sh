#!/usr/bin/env bash
# pre-edit-protect.sh — Chặn sửa file quan trọng mà không xác nhận
# Exit 2 = BLOCK, Exit 0 = ALLOW
set -euo pipefail

input=$(cat)
file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // ""')

# File KHÔNG ĐƯỢC agent tự sửa
protected_files=(
  ".env"
  ".env.production"
  "docker-compose.yml"
  "Dockerfile"
  ".claude/settings.json"
  ".ai-memory/00_core_rules.xml"
)

for pf in "${protected_files[@]}"; do
  if [[ "$file_path" == *"$pf"* ]]; then
    echo "BLOCKED: File '$pf' được bảo vệ. Yêu cầu user xác nhận trước khi sửa." >&2
    exit 2
  fi
done

exit 0
