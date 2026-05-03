---
name: sync-memory
description: >
  Đồng bộ AI Memory với code thực tế. Dùng khi user nói "cập nhật memory",
  "sync memory", "đồng bộ", hoặc sau khi hoàn thành task lớn.
  Cũng tự động dùng khi phát hiện memory lỗi thời (Confidence: LOW).
---
 
# Quy trình Sync Memory
 
## Bước 1: Kiểm tra last_updated
- Đọc tất cả file `03_deep_knowledge/`
- File nào > 14 ngày → cần verify
## Bước 2: So sánh memory vs code
- Đọc code nguồn (theo `source_files` trong header)
- So sánh: endpoints, business rules, entity schema
- Phát hiện file code mới chưa có trong memory
## Bước 3: Cập nhật
- Sửa `.md` cho khớp code thực tế
- `last_updated` = hôm nay, `Confidence` = HIGH
- Module mới → cập nhật `02_project_map.md`
- Kiến trúc đổi → cập nhật `01_system_architecture.md`
## Bước 4: Ghi log
- `06_evolution_log.md`: DOCS | Sync memory | [files updated]
## QUY TẮC: Code LUÔN ĐÚNG hơn memory. Xung đột → sửa memory.
 