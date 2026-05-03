# docs/ — Tài liệu On-Demand cho AI Agent
 
## Mục đích
Thư mục này chứa tài liệu chi tiết mà agent CHỈ đọc khi cần.
Khác với CLAUDE.md (load mỗi phiên) và .ai-memory/ (agent tự quản lý),
docs/ chứa tài liệu TĨNH do developer viết — agent không tự sửa.
 
## Khi nào agent đọc docs/?
- Khi user gõ `@docs/ten-file.md` trong chat
- Khi skill/agent tham chiếu đến docs/
- Khi agent cần kiến thức chuyên sâu mà memory tóm tắt không đủ
## Cách tổ chức
```
docs/
├── README.md              ← File này
├── api-spec.md            ← Đặc tả API chi tiết (endpoints, request/response)
├── database-schema.md     ← Schema database đầy đủ, quan hệ, index
├── coding-guidelines.md   ← Quy tắc coding mở rộng (chi tiết hơn rules/)
├── deployment-guide.md    ← Hướng dẫn deploy, CI/CD pipeline
├── architecture-decisions/ ← ADR (Architecture Decision Records)
│   ├── 001-choose-jwt.md
│   ├── 002-monolith-first.md
│   └── ...
└── onboarding.md          ← Hướng dẫn cho developer mới (hoặc agent mới)
```
 
## Quy tắc
- Agent KHÔNG TỰ SỬA file trong docs/ (chỉ developer sửa)
- Mỗi file nên có header: mục đích, last updated, author
- Nội dung chi tiết — đây là nơi ghi đầy đủ, không cần tóm tắt
- KHÔNG duplicate với .ai-memory/ — memory là tóm tắt, docs là chi tiết
## Cơ chế TEMPLATE vs ACTIVE
Mỗi file docs/ có dòng `> Status:` trong header:
- `Status: TEMPLATE` → File mẫu, CHƯA được developer điền nội dung thật.
  Agent PHẢI BỎ QUA file này, KHÔNG dùng làm tham chiếu.
- `Status: ACTIVE` → File đã được developer điền đầy đủ.
  Agent được phép đọc và dùng làm tài liệu tham chiếu.
Khi developer điền xong nội dung thật cho 1 file:
1. Xóa nội dung mẫu, thay bằng nội dung thật
2. Đổi `Status: TEMPLATE` → `Status: ACTIVE`
3. Cập nhật `Last updated` thành ngày hiện tại
## So sánh với .ai-memory/
 
| Đặc điểm         | docs/                   | .ai-memory/              |
|-------------------|-------------------------|--------------------------|
| Ai viết?          | Developer               | Agent tự tạo/cập nhật    |
| Ai sửa?           | Developer               | Agent tự đồng bộ         |
| Load khi nào?     | On-demand (@docs/...)   | Mỗi phiên theo protocol  |
| Nội dung?         | Chi tiết, đầy đủ        | Tóm tắt, ngắn gọn       |
| Lỗi thời?         | Developer chịu trách nhiệm | Agent tự phát hiện     |
 