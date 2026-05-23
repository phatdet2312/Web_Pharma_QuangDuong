# Memory Protocol (chi tiết)

> File chi tiết — root `AGENTS.md` trỏ tới đây để giữ orchestrator gọn.

## ⚠️ GUARD: Kiểm tra bootstrap
Trước MỌI task, kiểm tra `.ai-memory/01_system_architecture.md`:
- Chứa `PENDING_BOOTSTRAP` → DỪNG, chạy `$bootstrap-memory` trước
- Section `Project Convention` trống → chạy `$bootstrap-memory` để điền

## Khi nhận task mới (DAILY EXECUTION)
0. **Drift check** (BẮT BUỘC trước task mới): `git log --oneline -5` HOẶC `git diff --stat HEAD` → nếu có commit Codex không biết:
   - Chạy `git diff --name-only` để list file đã thay đổi
   - Đọc CHỈ các file thay đổi (KHÔNG quét toàn bộ dự án)
   - Cập nhật memory tương ứng trong `03_deep_knowledge/`
   - Đánh dấu task cũ DONE/CANCELLED trong `04_active_plan.md` nếu đã hoàn thành ngoài Codex
   - Báo user: "Phát hiện [N] file thay đổi ngoài Codex, đã đồng bộ memory"
   - **Quy tắc vàng**: Git log là nguồn sự thật. Nếu git nói file đã đổi mà memory nói chưa → **memory SAI**, sửa memory không sửa code.
   - Hoặc gọi skill `$detect-drift` để tự động hoá quy trình trên
0b. **Rework alert check**: `.codex/rework-alerts.log` có dòng mới trong 24h → đọc + `$reflect` ngầm
0c. **Memory size check** (1 lần mỗi 10 phiên hoặc đầu phiên mới sau >24h):
    - Chạy `ls -l .ai-memory/06_evolution_log.md .ai-memory/07_learnings.md`
    - Nếu `06_evolution_log.md` > 50KB hoặc `07_learnings.md` > 30KB → BÁO user: "Memory đã lớn ([size]). Đề xuất chạy `$compact-memory` để nén log cũ."
    - KHÔNG tự chạy `$compact-memory` — chỉ đề xuất, user quyết định
1. Đọc `.ai-memory/07_learnings.md` → tránh lặp sai lầm
2. Đọc `.ai-memory/04_active_plan.md` → có task dang dở không?
3. Đọc `.ai-memory/05_active_workspace.md` → có bug/blocker không?
4. Đọc `.ai-memory/02_project_map.md` → xác định tọa độ file cần sửa
5. Đọc `.ai-memory/03_deep_knowledge/INDEX.md` → chỉ mở file được trỏ đến

## Chiến lược đọc file
- ƯU TIÊN memory trước, mở code CHỈ KHI cần sửa hoặc memory không đủ
- Đọc INDEX.md trước → chỉ drill vào file liên quan (Progressive Disclosure)
- Đọc theo line range khi file dài, KHÔNG đọc toàn bộ
- KHÔNG đọc file test/config trừ khi task yêu cầu
- TUYỆT ĐỐI KHÔNG quét lại dự án bằng list_dir nếu memory đã có

## Sau khi sửa code (SELF-SYNC)
- Cập nhật `.md` tương ứng trong `03_deep_knowledge/`
- `last_updated` = ngày hiện tại
- File/module mới → cập nhật `02_project_map.md` + `INDEX.md`
- Đổi kiến trúc → cập nhật `01_system_architecture.md`
- Ghi log `06_evolution_log.md`
- Sau quyết định thiết kế (2+ phương án): ghi vào bảng Decision (deep knowledge) hoặc Architecture Decisions (cross-cutting)
- BẮT BUỘC 5 cột: Quyết định | Phương án | Lý do | Ngày ghi | Hết hạn (+3 tháng module / +6 tháng cross-cutting)
- Trước khi đề xuất phương án: đọc bảng Decision để tránh đề xuất phương án đã bị reject hoặc dead end

## Decision Half-Life
- Mọi Decision có "Hết hạn"
- Trước khi áp dụng decision cũ: check ngày hiện tại vs "Hết hạn"
  - Còn hạn → áp dụng
  - Quá hạn → KHÔNG tự áp dụng. Báo user: "Decision X đã hết hạn YYYY-MM-DD. Re-evaluate hay extend?"
- `memory-keeper` sync: mark `[EXPIRED]` các decision quá hạn

## Self-Improving
- User correction hoặc test fail 2+ vòng → `$reflect`
- Pattern `Lần >= 3` trong `07_learnings.md` → gợi ý `$promote-learning`

## Self-Healing (eval loop)
- Sau implementer/refactorer → `$self-eval` (compile + test + convention)
- Eval fail → `$reflect` → fix → eval lại (max 3 vòng)
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
🧠 Memory updated: [file .md]
📚 Learnings: [nếu có]
⏭️ Bước tiếp: [nếu có]
```
