# .codex/launch — Project-local Codex launchers

Codex 0.130.0 ignores active `[profiles.*]` in project-local `.codex/config.toml`.
This folder provides portable launch presets without requiring users to edit `~/.codex/config.toml`.

## Windows PowerShell
```powershell
.\.codex\launch\codex.ps1
.\.codex\launch\codex-xhigh.ps1
.\.codex\launch\codex-high.ps1
.\.codex\launch\codex-low.ps1
.\.codex\launch\codex-fast.ps1
.\.codex\launch\codex-review-deep.ps1
```

## macOS / Linux / WSL / Git Bash
```bash
chmod +x .codex/launch/*.sh
.codex/launch/codex.sh
.codex/launch/codex-xhigh.sh
.codex/launch/codex-high.sh
.codex/launch/codex-low.sh
.codex/launch/codex-fast.sh
.codex/launch/codex-review-deep.sh
```

## Preset mapping
- `codex` — default project config (`model_reasoning_effort = "xhigh"`)
- `codex-xhigh` — xhigh reasoning
- `codex-high` — high reasoning
- `codex-low` — low reasoning
- `codex-fast` — minimal reasoning + read-only sandbox
- `codex-review-deep` — high reasoning + on-request approval

All launchers pass `-C <project-root>` automatically, so Codex opens at the correct project root.
