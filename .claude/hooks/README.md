# .claude/hooks/ — Lifecycle automation (deterministic)
 
## Mục đích
Hooks là script chạy TỰ ĐỘNG tại các thời điểm cụ thể trong workflow của Claude.
Khác với rules/skills (dựa vào AI hiểu ý), hooks là code xác định — không hallucinate.
Mỗi hook có cặp `.sh` (Linux/macOS) + `.ps1` (Windows) — cross-platform.
 
## Cấu trúc
```
.claude/hooks/
├── README.md                      ← File này
├── pre-bash-firewall.sh / .ps1    ← TRƯỚC Bash: chặn lệnh nguy hiểm
├── pre-edit-protect.sh / .ps1     ← TRƯỚC Edit: bảo vệ file quan trọng
├── post-edit-log.sh / .ps1        ← SAU Edit: ghi log mọi file đã sửa
├── post-edit-typecheck.sh / .ps1  ← SAU Edit: auto compile/lint
├── post-edit-detect-rework.sh / .ps1 ← SAU Edit: cảnh báo khi file edit >3 lần/30min
├── session-start.sh / .ps1        ← Khởi phiên: check drift + rework + memory
└── scan-secret-prompt.sh / .ps1   ← Kiểm tra prompt: chặn secret/credential
```
7 hooks × 2 platform = 14 script files
 
## Chi tiết từng hook
 
| Hook | Event | Chức năng | Output |
|------|-------|-----------|--------|
| `pre-bash-firewall` | PreToolUse + PermissionRequest | Chặn rm -rf, git reset --hard, sudo, drop table, curl\|bash... | JSON deny hoặc continue |
| `pre-edit-protect` | PreToolUse + PermissionRequest | Bảo vệ file theo pattern từ `.claudeignore` (tag `# protected-edit`) | JSON deny hoặc continue |
| `post-edit-log` | PostToolUse | Ghi timestamp + tool + filepath vào `.claude/edit-history.log` | JSON continue |
| `post-edit-typecheck` | PostToolUse | Chạy compile/lint theo loại file (.java/.ts/.js/.py/.go/.rs...) | JSON block (lỗi) hoặc continue |
| `post-edit-detect-rework` | PostToolUse | Đếm edit trong 30min. >3 lần → ghi REWORK_ALERT vào `.claude/rework-alerts.log` | JSON continue |
| `session-start` | SessionStart | Check git drift, rework alerts, memory bloat (>50KB) | JSON additionalContext |
| `scan-secret-prompt` | UserPromptSubmit | Chặn prompt chứa API key, token, password pattern | JSON block hoặc continue |

## Giao thức I/O (JSON stdout)
- **Input**: JSON qua stdin chứa thông tin tool (command, file_path, hook_event_name...)
- **Output**: JSON qua stdout quyết định hành vi. Luôn exit 0.
- **PreToolUse deny**: `{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"..."}}`
- **PermissionRequest deny**: `{"hookSpecificOutput":{"hookEventName":"PermissionRequest","decision":{"behavior":"deny","message":"..."}}}`
- **PostToolUse block**: `{"decision":"block","reason":"...","hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"..."}}`
- **Cho phép**: `{"continue": true}`

## Event registration (settings.json)
- `PreToolUse`: pre-bash-firewall (Bash), pre-edit-protect (Edit|Write|MultiEdit)
- `PermissionRequest`: pre-bash-firewall (Bash), pre-edit-protect (Edit|Write|MultiEdit)
- `PostToolUse`: post-edit-log, post-edit-typecheck, post-edit-detect-rework (Edit|Write|MultiEdit)
- `SessionStart`: session-start (startup|resume)
- `UserPromptSubmit`: scan-secret-prompt

## Typecheck auto-detect
`post-edit-typecheck` tự detect loại dự án và chạy lệnh phù hợp:
- `pom.xml` có → `mvn compile`
- `build.gradle` có → `gradle compileJava`
- `.ts/.tsx` → `npx tsc --noEmit`
- `.js/.jsx` → `npm run lint` (nếu có)
- `.py` → `python -m py_compile`
- `.go` → `go build ./...`
- `.rs` → `cargo check`
- `.cs` → `dotnet build`
- `.kt` → `kotlinc`
- Khác (.md, .xml, .yml...) → skip

## Cross-platform setup
- **Windows:** double-click `.claude\setup.cmd` — tự detect bash, nếu không có thì chuyển `settings.json` sang PowerShell/.ps1
- **Linux/macOS:** `chmod +x .claude/hooks/*.sh` — bash có sẵn, dùng mặc định
- **Khôi phục về bash:** `bash .claude/setup.sh`
- Scripts: `setup.cmd` → gọi `setup.ps1` ngầm. User không cần biết chi tiết.

## Tùy chỉnh
- **Thêm hook mới**: tạo cặp `.sh` + `.ps1` + cấu hình trong `settings.json`
- **Thêm file bảo vệ**: sửa `.claudeignore`, thêm tag `# protected-edit` vào dòng pattern
- **Thêm pattern nguy hiểm**: sửa array `deny_patterns` trong `pre-bash-firewall`
- **Sửa typecheck command**: sửa `post-edit-typecheck` cho khớp build tool dự án
