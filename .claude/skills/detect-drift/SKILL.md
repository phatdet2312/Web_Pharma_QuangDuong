---
name: detect-drift
description: >
  Phát hiện và đồng bộ thay đổi code xảy ra ngoài Claude.
  Dùng khi: user quay lại sau khi tự code, dùng AI khác, hoặc merge PR.
  Cũng tự động trigger khi user nói "tôi đã sửa", "tôi code tay",
  "dùng Cursor/Copilot", "đã merge", "có người khác sửa".
---
 
# Quy trình Detect Drift — Phát hiện thay đổi ngoài Claude
 
## Bước 1: Kiểm tra git history
```bash
# Xem 10 commit gần nhất
git log --oneline -10
 
# Xem file nào thay đổi so với lần cuối Claude biết
git diff --name-only HEAD~5
git diff --stat HEAD~5
```
 
## Bước 2: So sánh với memory
- Đọc `.ai-memory/06_evolution_log.md` → commit cuối Claude ghi nhận
- So sánh với `git log` → xác định commit nào Claude KHÔNG BIẾT
- List file thay đổi trong các commit đó
## Bước 3: Cập nhật có chọn lọc (KHÔNG quét toàn bộ)
- Chỉ đọc file ĐÃ THAY ĐỔI — không đọc file không liên quan
- Cập nhật `03_deep_knowledge/` tương ứng
- Nếu có file/module mới → cập nhật `02_project_map.md`
- Nếu có entity mới → cập nhật bảng Database Entities
- Nếu kiến trúc thay đổi → cập nhật `01_system_architecture.md`
## Bước 4: Dọn dẹp plan cũ
- `04_active_plan.md`: đánh dấu task đã hoàn thành ✅ hoặc CANCELLED
- `05_active_workspace.md`: xóa bug/blocker đã được giải quyết
- Ghi log vào `06_evolution_log.md`:
  `| Ngày | DRIFT | Đồng bộ [N] file thay đổi ngoài Claude | [danh sách] |`
## Bước 5: Báo cáo
```
🔄 Drift Detection hoàn tất
📊 Phát hiện: [N] commit, [M] file thay đổi ngoài Claude
📁 Files changed: [danh sách]
🧠 Memory updated: [danh sách .md]
✅ Sẵn sàng nhận task mới
```
 
## QUY TẮC:
- KHÔNG quét lại toàn bộ dự án — chỉ đọc file thay đổi
- Git log là nguồn sự thật — nếu git nói đổi mà memory nói chưa → memory SAI
- Tốn khoảng 2-5k token thay vì 15-20k token nếu quét lại toàn bộ