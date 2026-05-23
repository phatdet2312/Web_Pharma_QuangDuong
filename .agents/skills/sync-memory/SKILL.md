---
name: sync-memory
description: Đồng bộ AI Memory với code thực tế. Dùng khi code có thay đổi cần update memory, memory bị lỗi thời (Confidence LOW), hoặc sau khi hoàn thành task lớn. Auto-trigger khi user nói "cập nhật memory", "sync memory", "đồng bộ".
---

# Quy trình Sync Memory

## Bước 1: Kiểm tra last_updated
- Đọc tất cả file `.ai-memory/03_deep_knowledge/`
- File > 14 ngày → cần verify

## Bước 2: So sánh memory vs code
- Đọc code nguồn (theo `source_files` trong header)
- So sánh: endpoints, business rules, entity schema
- Phát hiện file code mới chưa có trong memory

## Bước 3: Cập nhật
- Sửa `.md` cho khớp code thực tế
- `last_updated` = hôm nay, `Confidence` = HIGH
- Module mới → cập nhật `02_project_map.md` + `03_deep_knowledge/INDEX.md`
- Kiến trúc đổi → cập nhật `01_system_architecture.md`

## Bước 4: Ghi log
`06_evolution_log.md`: `| Ngày | DOCS | Sync memory | [files updated] |`

## QUY TẮC
Code LUÔN ĐÚNG hơn memory. Xung đột → sửa memory.

Tốt nhất là gọi subagent `memory-keeper` qua `/agent` để thực hiện skill này (low tier, tiết kiệm token).
