---
name: debug-flow
description: >
  Quy trình debug có hệ thống. Dùng mỗi khi user gặp lỗi, exception,
  bug, test fail, hoặc nhắc đến "lỗi", "bug", "exception", "không chạy",
  "fix", "sửa lỗi", "500 error", "NullPointer", stack trace.
---
 
# Quy trình Debug có hệ thống
 
## Bước 1: Thu thập — copy CHÍNH XÁC error message / stack trace
 
## Bước 2: Phân tích (KHÔNG đoán mò)
1. Đọc stack trace từ DƯỚI LÊN → tìm dòng code CỦA DỰ ÁN
2. Grep file + dòng gây lỗi
3. Đọc method đó + caller
## Bước 3: Phân loại
| Loại         | Dấu hiệu             | Fix thường gặp          |
|--------------|-----------------------|-------------------------|
| NullPointer  | NPE in trace          | Null check / Optional   |
| Validation   | ConstraintViolation   | Fix DTO validation      |
| Database     | SQL/JPA exception     | Fix query / mapping     |
| Auth         | 401/403               | Fix security config     |
| Config       | Bean creation failure | Fix application.yml     |
| Logic        | Kết quả sai           | Trace logic step by step|
 
## Bước 4: Fix + Verify
1. Sửa code (1 chỗ mỗi lần)
2. Chạy test liên quan
3. Test lại scenario gây lỗi
4. Ghi bug + fix vào `05_active_workspace.md`
## TUYỆT ĐỐI KHÔNG: Đoán mà không đọc trace, sửa nhiều chỗ cùng lúc
 