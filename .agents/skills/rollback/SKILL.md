---
name: rollback
description: Rollback nguyên tử code VÀ memory về trạng thái trước. Dùng khi user yêu cầu hoàn tác/undo/revert/quay lại/bỏ thay đổi, hoặc khi Codex tạo code sai cần revert. BẮT BUỘC revert cả code lẫn memory cùng lúc.
---

# Quy trình Rollback — Nguyên tử Code + Memory

## Tại sao skill này tồn tại
Khi code đi A→B, memory cũng A'→B'. Chỉ `git checkout` (code về A) mà KHÔNG revert memory (vẫn ở B') → phiên sau Codex đọc memory B' + code A → suy luận sai.
Skill đảm bảo code VÀ memory quay về CÙNG LÚC.

## Bước 1: Xác định điểm rollback
```bash
git log --oneline -10
git show --stat <commit-hash>
```
- Hỏi user: rollback đến commit nào?
- Hỏi user: lý do rollback? (ghi vào log)

## Bước 2: Xác định phạm vi
- `git diff --name-only <target>..HEAD` → list file sẽ bị revert
- Đọc `06_evolution_log.md` → task nào sẽ bị undo
- Đọc `03_deep_knowledge/` liên quan → chuẩn bị cập nhật

## Bước 3: Revert code
```bash
# Ưu tiên revert an toàn (tạo commit mới, giữ history)
git revert <commit-hash> --no-edit

# Revert nhiều commit
git revert <oldest>..<newest> --no-edit
```
- ƯU TIÊN `git revert` hơn `git reset` (xóa history nguy hiểm)
- `git reset --hard` bị hook `pre-bash-firewall` chặn — nếu thật cần, user phải approve

## Bước 4: Revert memory (QUAN TRỌNG — tạo tính nguyên tử)
- Đọc code SAU rollback (trạng thái mới = trạng thái cũ)
- Cập nhật `03_deep_knowledge/` cho khớp code hiện tại
- Cập nhật `02_project_map.md` nếu có file/module bị xóa hoặc khôi phục
- Cập nhật `01_system_architecture.md` nếu kiến trúc thay đổi

## Bước 5: Cập nhật plan + workspace
- `04_active_plan.md`: đánh dấu task bị rollback → CANCELLED + lý do
- `05_active_workspace.md`: ghi context "đã rollback feature X vì [lý do]"

## Bước 6: Ghi log + Báo cáo
- `06_evolution_log.md`: `| Ngày | ROLLBACK | Revert [mô tả] vì [lý do] | [files] |`

```
⏪ Rollback hoàn tất
📍 Reverted to: [commit hash]
📁 Files reverted: [danh sách]
🧠 Memory updated: [danh sách .md]
📝 Lý do: [lý do từ user]
✅ Code + Memory đồng bộ
```

## QUY TẮC
- LUÔN revert memory CÙNG LÚC với code
- Ưu tiên `git revert` (an toàn) hơn `git reset`
- Ghi rõ lý do rollback — context cho phiên sau
- Rollback ảnh hưởng nhiều module → cập nhật TẤT CẢ deep knowledge liên quan
