# .codex/scripts/ — Lifecycle hook scripts

> Đối xứng `.claude/hooks/` của bộ Claude, nhưng dùng hook native của Codex.
> Hooks là guardrail/telemetry chạy bằng code xác định, không phụ thuộc việc AI
> có nhớ rule hay không.

## Cấu trúc

```text
.codex/scripts/
├── pre-bash-firewall.{sh,ps1}        PreToolUse/PermissionRequest cho Bash
├── pre-edit-protect.{sh,ps1}         PreToolUse/PermissionRequest cho edit
├── post-edit-log.{sh,ps1}            PostToolUse — log file đã sửa
├── post-edit-typecheck.{sh,ps1}      PostToolUse — compile/lint
├── post-edit-detect-rework.{sh,ps1}  PostToolUse — cảnh báo rework
├── session-start.{sh,ps1}            SessionStart — drift/rework/memory check
├── scan-secret-in-prompt.{sh,ps1}    UserPromptSubmit — quét secret trong prompt
├── log-agent-turn.{sh,ps1}           Stop — telemetry turn chính
├── log-subagent-start.{sh,ps1}       SubagentStart — ghi mốc bắt đầu subagent
└── log-subagent-stop.{sh,ps1}        SubagentStop — telemetry từng subagent
```

## Events đang dùng

Theo Codex 0.133.x, bộ này cấu hình các lifecycle events:

- `SessionStart`
- `PreToolUse`
- `PermissionRequest`
- `PostToolUse`
- `UserPromptSubmit`
- `SubagentStart`
- `SubagentStop`
- `Stop`

Workaround cũ từng xem `Stop` là điểm telemetry duy nhất. Hiện tại cách đó đã
lỗi thời với subagent metrics: `SubagentStart` và `SubagentStop` đã có
`agent_id`/`agent_type`, nên `$agent-roi` phải ưu tiên `.codex/subagent-metrics.csv`.

## File telemetry

- `.codex/agent-metrics.csv`: aggregate turn chính từ `Stop`.
- `.codex/subagent-metrics.csv`: telemetry từng subagent từ `SubagentStart` + `SubagentStop`.

Token usage vẫn để `N/A` nếu hook payload chưa expose field usage ổn định.
Không xem transcript parsing là giao diện ổn định.

## I/O convention

- Input: JSON qua stdin.
- Output: JSON qua stdout cho `Stop`, `SubagentStart`, `SubagentStop`.
- Plain text chỉ dùng khi event cho phép bổ sung developer context, ví dụ `SessionStart`.

Mẫu block `PreToolUse`:

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Blocked by repository policy."
  }
}
```

Mẫu block `PermissionRequest`:

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PermissionRequest",
    "decision": {
      "behavior": "deny",
      "message": "Blocked by repository policy."
    }
  }
}
```

## Cross-platform note

- `hooks.json` gọi `.sh` cho macOS/Linux/WSL/Git Bash.
- `hooks.windows.json` gọi `.ps1` cho Windows PowerShell native.
- Trên Windows native: backup `hooks.json`, rồi đổi `hooks.windows.json` thành `hooks.json`.

## Bảo trì

- `.codexignore` vẫn là source of truth cho path policy.
- Chỉ mirror dòng `# sensitive` vào `.codex/config.toml`.
- Sau khi sửa config/hook, chạy `codex --strict-config doctor --summary`.
- Khi hook definition đổi trong trusted project, kiểm tra/trust lại bằng `/hooks`.
