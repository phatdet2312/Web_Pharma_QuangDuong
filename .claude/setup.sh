#!/usr/bin/env bash
# setup.sh — Chuyen settings.json sang bash/.sh hooks (Linux/macOS/WSL).
# Chay 1 lan: bash .claude/setup.sh
# Dung khi truoc do da chay setup.ps1 va muon khoi phuc ve bash.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SETTINGS="$SCRIPT_DIR/settings.json"

if [ ! -f "$SETTINGS" ]; then
    echo "[ERROR] Khong tim thay: $SETTINGS"
    exit 1
fi

if ! grep -q '"command": "powershell"' "$SETTINGS" 2>/dev/null; then
    echo "[OK] settings.json da dung bash/.sh. Khong can doi."
    exit 0
fi

cp "$SETTINGS" "${SETTINGS}.backup"
echo "[OK] Backup: ${SETTINGS}.backup"

sed -E \
    -e 's/"command":[[:space:]]*"powershell"/"command": "bash"/g' \
    -e 's/"args":[[:space:]]*\["-NoProfile",[[:space:]]*"-ExecutionPolicy",[[:space:]]*"Bypass",[[:space:]]*"-File",[[:space:]]*"([^"]+)\.ps1"\]/"args": ["\1.sh"]/g' \
    "$SETTINGS" > "${SETTINGS}.tmp" && mv "${SETTINGS}.tmp" "$SETTINGS"

count=$(grep -c '"command": "bash"' "$SETTINGS" || true)
echo "[OK] Da chuyen $count hook(s): PowerShell/.ps1 -> bash/.sh"
echo "[OK] settings.json da cap nhat. Claude Code se dung .sh hooks."
