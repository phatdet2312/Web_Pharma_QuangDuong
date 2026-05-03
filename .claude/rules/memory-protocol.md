# Memory Protocol
 
## Khi nhận task mới (DAILY EXECUTION)
1. Đọc `.ai-memory/04_active_plan.md` → có task dang dở không?
2. Đọc `.ai-memory/05_active_workspace.md` → có bug/blocker không?
3. Đọc `.ai-memory/02_project_map.md` → xác định tọa độ file cần sửa
4. Đọc `.ai-memory/03_deep_knowledge/xxx.md` → CHỈ file liên quan task
## Chiến lược đọc file
- ƯU TIÊN memory trước, mở code CHỈ KHI cần sửa hoặc memory không đủ
- Đọc theo line range khi file dài, không đọc toàn bộ
- KHÔNG đọc file test/config trừ khi task yêu cầu
- TUYỆT ĐỐI KHÔNG quét lại dự án bằng list_dir
## Sau khi sửa code (SELF-SYNC)
- Cập nhật `.md` tương ứng trong `03_deep_knowledge/`
- Cập nhật `last_updated` thành ngày hiện tại
- Thêm file/module mới → cập nhật `02_project_map.md`
- Đổi kiến trúc → cập nhật `01_system_architecture.md`
- Ghi log vào `06_evolution_log.md`
## Xung đột memory vs code
- Code thực tế LUÔN ĐÚNG. Memory có thể lỗi thời.
- Phát hiện xung đột → SỬA MEMORY, KHÔNG sửa code
- Báo user: "Phát hiện memory lỗi thời tại [file], đã cập nhật."
## Bàn giao phiên (HANDOVER)
- `04_active_plan.md`: đánh dấu task hoàn thành, ghi task tiếp theo
- `05_active_workspace.md`: bug kẹt, context cho phiên sau
- `06_evolution_log.md`: Ngày | Type | Mô tả | Files changed
## Báo cáo hoàn thành task
```
✅ Đã làm: [mô tả]
📁 Files changed: [danh sách]
🧠 Memory updated: [file .md đã cập nhật]
⏭️ Bước tiếp: [nếu có]
```
 