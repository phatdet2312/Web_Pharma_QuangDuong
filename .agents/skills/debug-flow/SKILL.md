---
name: debug-flow
description: Quy trình debug có hệ thống. Dùng khi gặp lỗi, exception, bug, test fail, hoặc nhắc đến "lỗi", "bug", "exception", "không chạy", "fix", "sửa lỗi", "500 error", "NullPointer", stack trace.
---

# Quy trình Debug có hệ thống

## Bước 1: Thu thập
Copy CHÍNH XÁC error message / stack trace từ user.

## Bước 2: Phân tích (KHÔNG đoán mò)
1. Đọc stack trace từ DƯỚI LÊN → tìm dòng code CỦA DỰ ÁN
2. Grep file + dòng gây lỗi
3. Đọc method đó + caller

## Bước 3: Phân loại
| Loại | Dấu hiệu | Fix thường gặp |
|------|----------|----------------|
| Null/None | NPE, None, nil, null reference | Null check / guard clause |
| Validation | Validation error, bad input | Fix input validation |
| Database | SQL/ORM exception, query error | Fix query / mapping |
| Auth | 401/403 | Fix security config |
| Config | Init failure, missing config | Fix config / env |
| Type | Type error, cast exception | Fix type mismatch |
| Logic | Kết quả sai | Trace logic step by step |

## Bước 4: Fix + Verify
1. Sửa code (1 chỗ mỗi lần) — spawn `debugger` qua `/agent` nếu cần
2. Hook PostToolUse tự chạy typecheck
3. Spawn `tester` qua `/agent` chạy test liên quan
4. Test lại scenario gây lỗi
5. Ghi bug + fix vào `.ai-memory/05_active_workspace.md`

## TUYỆT ĐỐI KHÔNG
- Đoán mà không đọc trace
- Sửa nhiều chỗ cùng lúc

## Mở rộng
- Bug production → đề xuất chuyển sang $production-feedback (tìm root cause lịch sử + ghi learning)
