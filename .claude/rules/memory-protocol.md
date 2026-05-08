# Memory Protocol
 
## ⚠️ GUARD: Kiểm tra bootstrap
Trước MỌI task, kiểm tra `01_system_architecture.md`:
- Nếu chứa `PENDING_BOOTSTRAP` → **DỪNG LẠI**, chạy `/bootstrap-memory` trước
- Nếu section `Project Convention` trống → chạy `/bootstrap-memory` để điền
- Chỉ tiếp tục task khi `01_system_architecture.md` đã có nội dung thật

## Khi nhận task mới (DAILY EXECUTION)
0. **Drift check**: Chạy `git log --oneline -5` → nếu có commit Claude không biết → chạy /detect-drift TRƯỚC
1. **Learning check**: Đọc `.ai-memory/07_learnings.md` → tránh lặp sai lầm đã biết
2. Đọc `.ai-memory/04_active_plan.md` → có task dang dở không?
3. Đọc `.ai-memory/05_active_workspace.md` → có bug/blocker không?
4. Đọc `.ai-memory/02_project_map.md` → xác định tọa độ file cần sửa
5. Đọc `.ai-memory/03_deep_knowledge/INDEX.md` → chỉ mở file được trỏ đến, KHÔNG mở tất cả

## Chiến lược đọc file
- ƯU TIÊN memory trước, mở code CHỈ KHI cần sửa hoặc memory không đủ
- Đọc INDEX.md trước → chỉ drill vào file liên quan (Progressive Disclosure)
- Đọc theo line range khi file dài, không đọc toàn bộ
- KHÔNG đọc file test/config trừ khi task yêu cầu
- TUYỆT ĐỐI KHÔNG quét lại dự án bằng list_dir

## Sau khi sửa code (SELF-SYNC)
- Cập nhật `.md` tương ứng trong `03_deep_knowledge/`
- Cập nhật `last_updated` thành ngày hiện tại
- Thêm file/module mới → cập nhật `02_project_map.md` + `INDEX.md`
- Đổi kiến trúc → cập nhật `01_system_architecture.md`
- Ghi log vào `06_evolution_log.md`
- Sau quyết định thiết kế (2+ phương án): ghi vào bảng Decision trong deep knowledge hoặc Architecture Decisions nếu cross-cutting
- Trước khi đề xuất phương án: đọc bảng Decision để tránh đề xuất phương án đã bị reject hoặc dead end

## Self-Improving (sau sai lầm)
- Khi user correction hoặc test fail 2+ vòng: trigger /reflect
- /reflect ghi pattern vào `07_learnings.md` (bảng format)
- Khi entry Lần >= 3: gợi ý /promote-learning

## Self-Healing (eval loop)
- Sau implementer/refactorer viết code → tester eval
- Eval fail → /reflect ghi learning → agent fix → tester eval lại (tối đa 3 vòng)
- Eval pass → memory-keeper sync

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
📚 Learnings: [nếu có learning mới]
⏭️ Bước tiếp: [nếu có]
```