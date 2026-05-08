---
name: rollback
description: >
  Rollback code VÀ memory về trạng thái trước đó một cách nguyên tử.
  Dùng khi user yêu cầu: "hoàn tác", "undo", "quay lại", "rollback",
  "revert", "bỏ thay đổi vừa làm", hoặc khi agent tạo code sai cần revert.
  QUAN TRỌNG: Phải revert CẢ code VÀ memory cùng lúc để tránh lệch trạng thái.
---
 
# Quy trình Rollback — Nguyên tử Code + Memory
 
## Tại sao cần skill này?
Khi code đi A→B, memory cũng A'→B'. Nếu chỉ `git checkout` (code về A)
mà không revert memory (vẫn ở B'), phiên sau agent đọc memory B' + code A → suy luận sai.
Skill này đảm bảo code VÀ memory quay về CÙNG LÚC.
 
## Bước 1: Xác định điểm rollback
```bash
# Xem lịch sử commit gần nhất
git log --oneline -10

# Xem chi tiết thay đổi của commit cụ thể
git show --stat <commit-hash>
```
- Hỏi user: rollback đến commit nào?
- Hỏi user: lý do rollback? (ghi vào log)
 
## Bước 2: Xác định phạm vi ảnh hưởng
- `git diff --name-only <target-commit>..HEAD` → list file sẽ bị revert
- Đọc `.ai-memory/06_evolution_log.md` → xác định task nào sẽ bị undo
- Đọc `.ai-memory/03_deep_knowledge/` liên quan → chuẩn bị cập nhật
 
## Bước 3: Revert code
```bash
# Cách 1: Revert an toàn (tạo commit mới, giữ history)
git revert <commit-hash> --no-edit

# Cách 2: Revert nhiều commit
git revert <oldest-commit>..<newest-commit> --no-edit
```
- ƯU TIÊN `git revert` (tạo commit mới) hơn `git reset` (xóa history)
- Chỉ dùng `git reset` nếu user yêu cầu rõ ràng
 
## Bước 4: Revert memory (QUAN TRỌNG — bước này tạo tính nguyên tử)
- Đọc code SAU rollback (trạng thái mới = trạng thái cũ)
- Cập nhật `03_deep_knowledge/` cho khớp code hiện tại
- Cập nhật `02_project_map.md` nếu có file/module bị xóa hoặc khôi phục
- Cập nhật `01_system_architecture.md` nếu kiến trúc thay đổi
 
## Bước 5: Cập nhật plan + workspace
- `04_active_plan.md`: đánh dấu task bị rollback → CANCELLED, ghi lý do
- `05_active_workspace.md`: ghi context "đã rollback feature X vì [lý do]"
 
## Bước 6: Ghi log + Báo cáo
- `06_evolution_log.md`:
  `| Ngày | ROLLBACK | Revert [mô tả] vì [lý do] | [files changed] |`
 
```
⏪ Rollback hoàn tất
📍 Reverted to: [commit hash]
📁 Files reverted: [danh sách]
🧠 Memory updated: [danh sách .md]
📝 Lý do: [lý do từ user]
✅ Code + Memory đồng bộ
```
 
## QUY TẮC:
- LUÔN revert memory CÙNG LÚC với code — KHÔNG BAO GIỜ chỉ revert code
- Ưu tiên `git revert` (an toàn) hơn `git reset` (nguy hiểm)
- Ghi rõ lý do rollback trong evolution log — context này quan trọng cho phiên sau
- Nếu rollback ảnh hưởng nhiều module → cập nhật TẤT CẢ deep knowledge liên quan
