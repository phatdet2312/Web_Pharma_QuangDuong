---
name: detect-drift
description: Phát hiện thay đổi code xảy ra ngoài Codex. Dùng khi user quay lại sau khi tự code, dùng AI khác, hoặc merge PR. Auto-trigger khi user nói "tôi đã sửa", "code tay", "dùng AI khác", "đã merge", "có người khác sửa".
---

# Quy trình Detect Drift

## Bước 1: Kiểm tra git history
```bash
git log --oneline -10
git diff --name-only HEAD~5
git diff --stat HEAD~5
```

## Bước 2: So sánh với memory
- Đọc `.ai-memory/06_evolution_log.md` → commit cuối Codex ghi nhận
- So sánh với `git log` → xác định commit nào Codex KHÔNG BIẾT
- List file thay đổi trong các commit đó

## Bước 3: Cập nhật có chọn lọc (KHÔNG quét toàn bộ)
- Chỉ đọc file ĐÃ THAY ĐỔI — không đọc file không liên quan
- Cập nhật `03_deep_knowledge/` tương ứng
- File/module mới → cập nhật `02_project_map.md`
- Entity mới → cập nhật bảng Database Entities
- Kiến trúc thay đổi → cập nhật `01_system_architecture.md`

## Bước 4: Dọn dẹp plan cũ
- `04_active_plan.md`: đánh dấu task hoàn thành ✅ hoặc CANCELLED
- `05_active_workspace.md`: xóa bug/blocker đã giải quyết
- Log: `| Ngày | DRIFT | Đồng bộ [N] file thay đổi ngoài Codex | [danh sách] |`

## Bước 5: Báo cáo
```
🔄 Drift Detection hoàn tất
📊 Phát hiện: [N] commit, [M] file thay đổi ngoài Codex
📁 Files changed: [danh sách]
🧠 Memory updated: [danh sách .md]
✅ Sẵn sàng nhận task mới
```

## QUY TẮC
- KHÔNG quét lại toàn bộ dự án — chỉ đọc file thay đổi
- Git log là nguồn sự thật — git nói đổi mà memory nói chưa → memory SAI
- Tốn ~2-5k token thay vì 15-20k token nếu quét lại toàn bộ
