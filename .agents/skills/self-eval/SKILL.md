---
name: self-eval
description: Eval loop tự compile + test + convention check sau khi viết/sửa code. Auto-trigger sau khi implementer hoặc refactorer hoàn thành task. Fail → reflect ghi learning → fix → retry (tối đa 3 vòng).
---

# Quy trình Self-Eval — Vòng lặp tự sửa

## Bước 1: Compile check
Nếu hook PostToolUse đang hoạt động và đã được trust, hook có thể đã chạy typecheck nhanh. Skill này vẫn PHẢI tự verify lại tổng thể, vì hook có thể bị tắt, chưa trust, chạy sai variant OS, hoặc fail dependency:
```bash
# Xác định build command từ "Cách chạy dự án" trong 01_system_architecture.md
# Java:       mvn compile -q 2>&1 | tail -20
# TypeScript: npx tsc --noEmit 2>&1 | tail -20
# Python:     mypy <file>  hoặc  ruff check
# Go:         go build ./...
# Rust:       cargo check
# C#:         dotnet build --no-restore -q
# Kotlin:     gradle compileKotlin -q
# Swift:      swift build
# Dart:       dart analyze
```
- Compile fail → ghi lỗi, KHÔNG cần chạy tiếp Bước 2-3

## Bước 2: Test run
- Chỉ chạy test LIÊN QUAN (không chạy toàn bộ suite — tiết kiệm token + thời gian)
- Xác định test command từ "Cách chạy dự án"
- Ghi rõ test nào fail + error message

## Bước 3: Convention check
- Đối chiếu code mới với `.codex/rules/<scope>.md` (rule chi tiết) + nested AGENTS.md của folder + "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Kiểm tra: naming, method length, exception handling

## Bước 4: Đánh giá kết quả
| Kết quả | Hành động |
|---------|-----------|
| Compile fail | Spawn subagent `debugger` qua `/agent` để fix → eval lại (vòng +1) |
| Test fail | Chạy $reflect ghi learning → debugger fix → eval lại (vòng +1) |
| Convention fail | Fix → eval lại (vòng +1) |
| Tất cả pass | Báo cáo thành công → spawn `memory-keeper` sync |

## Bước 5: Giới hạn vòng lặp
- Tối đa 3 vòng eval-fix
- Vòng 3 vẫn fail → ghi vào `05_active_workspace.md` là BLOCKED + báo user

## Báo cáo
```
🔄 Self-Eval [vòng N/3]
📋 Compile: ✅/❌
🧪 Tests: ✅ [pass]/[total] | ❌ [fail details]
📏 Convention: ✅/❌
🎯 Kết quả: PASS → memory-keeper | FAIL → retry | BLOCKED → báo user
```

## QUY TẮC
- CHỈ chạy test LIÊN QUAN, không chạy toàn bộ suite
- Mỗi vòng fail PHẢI $reflect trước khi fix
- Vòng 3 fail = BLOCKED, KHÔNG retry thêm
