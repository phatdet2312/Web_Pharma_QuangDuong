---
name: self-eval
description: >
  Chạy eval tự động sau khi code được viết/sửa. Tự động trigger khi:
  implementer hoặc refactorer hoàn thành task, hoặc user gõ /self-eval.
  Eval gồm: compile check, test run, convention check.
  Nếu fail → diagnose → /reflect ghi learning → retry.
---
 
# Quy trình Self-Eval — Vòng lặp tự sửa
 
## Bước 1: Compile check
```bash
# Chạy build/compile command phù hợp với dự án:
# Java:       mvn compile -q 2>&1 | tail -20
# TypeScript: npx tsc --noEmit 2>&1 | tail -20
# Python:     mypy [file] 2>&1 | tail -20   (hoặc ruff check)
# Go:         go build ./... 2>&1 | tail -20
# Rust:       cargo check 2>&1 | tail -20
# C#:         dotnet build --no-restore -q 2>&1 | tail -20
```
- Xác định build command từ "Cách chạy dự án" trong `.ai-memory/01_system_architecture.md`
- Nếu compile fail → ghi lỗi, KHÔNG cần chạy tiếp bước 2-3

## Bước 2: Test run
```bash
# Chạy test liên quan (KHÔNG chạy toàn bộ test suite)
# Xác định test command từ "Cách chạy dự án" trong 01_system_architecture.md
```
- Xác định test nào fail, error message là gì

## Bước 3: Convention check
- Đối chiếu code mới với `.claude/rules/` đang active
- Kiểm tra: naming, method length, exception handlin

## Bước 4: Đánh giá kết quả
| Kết quả | Hành động |
|---------|-----------|
| Compile fail | Ghi lỗi → agent fix → eval lại (vòng +1) |
| Test fail | /reflect ghi learning → agent fix → eval lại (vòng +1) |
| Convention fail | Agent fix → eval lại (vòng +1) |
| Tất cả pass | Báo cáo thành công → memory-keeper sync |
 
## Bước 5: Giới hạn vòng lặp
- Tối đa 3 vòng eval-fix
- Nếu vòng 3 vẫn fail → ghi vào `05_active_workspace.md` là BLOCKED + báo user

## Báo cáo
```
🔄 Self-Eval [vòng N/3]
📋 Compile: ✅/❌
🧪 Tests: ✅ [pass]/[total] | ❌ [fail details]
📏 Convention: ✅/❌
🎯 Kết quả: PASS → memory-keeper | FAIL → retry | BLOCKED → báo user
```
 
## QUY TẮC:
- CHỈ chạy test LIÊN QUAN, không chạy toàn bộ suite (tiết kiệm token + thời gian)
- Mỗi vòng fail PHẢI /reflect trước khi fix — đảm bảo learning được ghi
- Vòng 3 fail = BLOCKED, KHÔNG retry thêm — tránh vòng lặp vô hạn