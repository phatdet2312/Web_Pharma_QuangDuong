# .codex/scripts/ — Lifecycle hook scripts (deterministic)

> Đối xứng `.claude/hooks/` của Claude. Bộ này đặt scripts ở `.codex/scripts/`; Codex không bắt buộc tên folder này, mà chạy đúng path đã khai báo trong `.codex/hooks.json`. 8 cặp script (`.sh` + `.ps1`) cùng JSON output schema chuẩn Codex.

## Mục đích
Hooks là shell/PowerShell script chạy TỰ ĐỘNG tại các thời điểm cụ thể trong workflow của Codex.
Khác với rules/skills (dựa vào AI hiểu ý), hooks là code xác định — không hallucinate.

## Cấu trúc
```
.codex/scripts/
├── README.md
├── pre-bash-firewall.{sh,ps1}        ← TRƯỚC Bash/shell: chặn lệnh nguy hiểm
├── pre-edit-protect.{sh,ps1}         ← TRƯỚC apply_patch/Edit: bảo vệ file quan trọng (BLOCK cứng, đối xứng Claude exit 2)
├── post-edit-log.{sh,ps1}            ← SAU Edit: log mọi file đã sửa
├── post-edit-typecheck.{sh,ps1}      ← SAU Edit: auto compile/lint multi-language
├── post-edit-detect-rework.{sh,ps1}  ← SAU Edit: cảnh báo file edit >3 lần/30 phút
├── log-agent-turn.{sh,ps1}           ← Stop event: log + telemetry CSV cho $agent-roi
├── scan-secret-in-prompt.{sh,ps1}    ← UserPromptSubmit: chặn user paste secret vào prompt (Codex extra, Claude không có)
└── session-start.{sh,ps1}            ← SessionStart: drift + rework + memory size check (Codex extra)
```

## Events Codex hỗ trợ (nguồn: developers.openai.com/codex/hooks)
- `SessionStart` — khi mở phiên (startup/resume/clear)
- `PreToolUse` — TRƯỚC tool execute, có thể CHẶN qua `permissionDecision: "deny"`
- `PermissionRequest` — khi Codex sắp hỏi approval; có thể allow/deny request trước khi prompt approve hiện ra
- `PostToolUse` — SAU tool execute, có thể block hậu kỳ qua `decision: "block"`
- `UserPromptSubmit` — khi user submit prompt
- `Stop` — kết thúc turn (tương đương `SubagentStop` của Claude — Codex chỉ có Stop)

## I/O convention
- **Input**: JSON qua stdin với fields: `session_id`, `transcript_path`, `cwd`, `hook_event_name`, `model`, `permission_mode`, `turn_id` (turn-scoped). PreToolUse/PostToolUse có thêm `tool_name`, `tool_input` (object chứa `command`, `file_path`, `path`, v.v.).
- **Output**: JSON qua stdout với fields:
  - `{"continue": true}` — allow cho `SessionStart`/`UserPromptSubmit`/`Stop`; `Stop` phải xuất JSON khi exit 0.
  - `{"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision": "deny", "permissionDecisionReason": "..."}}` — block `PreToolUse`.
  - `{"hookSpecificOutput": {"hookEventName": "PermissionRequest", "decision": {"behavior": "deny", "message": "..."}}}` — deny approval request trước khi prompt approve hiện ra.
  - `{"decision": "block", "reason": "..."}` — block hậu kỳ cho `PostToolUse` hoặc block/continue theo event tương ứng.
  - Plain text — chỉ dùng như extra developer context cho `SessionStart`/`UserPromptSubmit`; không dùng plain text cho `Stop`.

## Cấu hình
Hook events được khai báo trong `.codex/hooks.json` (file format) hoặc inline trong `.codex/config.toml`. **KHÔNG bật cả 2 nguồn** vì Codex sẽ merge.

## Typecheck auto-detect (giống Claude)
`post-edit-typecheck` tự detect loại dự án và chạy lệnh phù hợp:
- `pom.xml` → `mvn compile -q`
- `build.gradle` → `gradle compileJava -q`
- `tsconfig.json` → `npx tsc --noEmit`
- `package.json` có lint → `npm run lint --silent`
- `.py` → `mypy` hoặc `ruff check`
- `go.mod` → `go build ./...`
- `Cargo.toml` → `cargo check`
- `.csproj`/`.sln` → `dotnet build --no-restore -q`
- `build.gradle.kts` (Kotlin) → `gradle compileKotlin -q`
- `Package.swift` → `swift build`
- `pubspec.yaml` (Dart) → `dart analyze`
- Khác (.md, .xml, .yml...) → skip, exit 0
- Nếu có `Makefile` với target `check` hoặc `lint` → chạy fallback `make check`/`make lint`

## Cross-platform note
- `*.sh` chạy trên macOS / Linux / WSL / Git Bash (mặc định trong `hooks.json`)
- `*.ps1` chạy trên Windows PowerShell native (dùng `hooks.windows.json`)
- Đổi file trên Windows PowerShell: `Rename-Item -LiteralPath ".codex\hooks.json" -NewName "hooks.unix.json"; Rename-Item -LiteralPath ".codex\hooks.windows.json" -NewName "hooks.json"`

## Tùy chỉnh
- **Thêm hook mới**: tạo file `.sh` (+ `.ps1` mirror nếu cần Windows) + cấu hình trong `.codex/hooks.json`
- **Thêm file bảo vệ / sensitive**: sửa `.codexignore` bằng tag `# protected-edit` hoặc `# sensitive`; không sửa danh sách riêng trong script
- **Thêm pattern nguy hiểm**: sửa array `deny_patterns` trong `pre-bash-firewall`
- **Sửa typecheck command**: sửa `post-edit-typecheck` cho khớp build tool dự án
- **Quan trọng**: sau khi copy, chạy `chmod +x .codex/scripts/*.sh` (macOS/Linux)
