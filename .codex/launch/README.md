# .codex/launch — Launcher Codex cấp project

Thư mục này chứa launcher portable cho Codex 0.133.x. Mục tiêu là giữ preset
ngay trong project, không bắt từng máy sửa `~/.codex/config.toml`.

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

## Bảng preset
- `codex` — config mặc định của project (`model_reasoning_effort = "xhigh"`)
- `codex-xhigh` — xhigh reasoning
- `codex-high` — high reasoning
- `codex-low` — low reasoning
- `codex-fast` — low reasoning + `default_permissions = ":read-only"`
- `codex-review-deep` — high reasoning + `approval_policy = "on-request"`

Tất cả launcher tự truyền `-C <project-root>`, nên Codex mở đúng root của project.
