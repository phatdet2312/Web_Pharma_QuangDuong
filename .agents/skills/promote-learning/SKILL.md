---
name: promote-learning
description: Đưa pattern learning lặp 3+ lần thành rule enforced trong AGENTS.md hoặc Architecture Decisions. Dùng khi user gõ thủ công, hoặc 07_learnings.md có 5+ entries với Lần >= 3.
---

# Quy trình Promote Learning

## Bước 1: Scan `.ai-memory/07_learnings.md`
- Tìm entries có `Lần` >= 3
- List ra cho user review

## Bước 2: Phân loại destination (Hybrid pattern — SOURCE OF TRUTH ở `.codex/rules/`)

| Loại | Promote đến (source of truth) | Có cần update nested AGENTS.md? |
|------|-------------------------------|--------------------------------|
| CONVENTION (backend) | `.codex/rules/backend.md` | Chỉ nếu top-3 priority — cập nhật top rule trong `src/AGENTS.md` |
| CONVENTION (frontend) | `.codex/rules/frontend.md` | Chỉ nếu top-3 — cập nhật `frontend/AGENTS.md` |
| CONVENTION (database) | `.codex/rules/database.md` | Chỉ nếu top-3 — cập nhật `db/AGENTS.md` |
| ARCHITECTURE | `.ai-memory/01_system_architecture.md` → Architecture Decisions | Không |
| SECURITY | `.codex/rules/security.md` (tạo nếu chưa có) | Có — security top rule cần load mọi lúc |
| PERFORMANCE | Deep knowledge file tương ứng → Decision Log | Không |
| LOGIC | Deep knowledge file tương ứng → Decision Log | Không |

**Lý do**: theo Hybrid pattern, `.codex/rules/` là single source of truth. Nested AGENTS.md chỉ mỏng (5-10 dòng top rule + trỏ tới file chi tiết). Nếu promote vào nested → vi phạm DRY, nested phình to, mất khi clone repo khác.

## Bước 3: Xin xác nhận user
- Hiện danh sách entries + destination
- Chờ user confirm TRƯỚC khi sửa

## Bước 4: Thực hiện
- Thêm rule vào destination file (1 dòng ngắn gọn)
- Xóa entry khỏi `07_learnings.md`
- Cập nhật header count
- Ghi log `06_evolution_log.md`: `| Ngày | LEARN | Promote [N] learnings | [files] |`

## Bước 5: Báo cáo
```
🎓 Promote hoàn tất
📊 Reviewed: [tổng] | ✅ Promoted: [số] → [files]
⏭️ Còn lại: [số entries chưa đủ 3 lần]
```

## QUY TẮC
- LUÔN xin user confirm — KHÔNG tự promote
- Sau promote, kiểm tra rules file — >30 dòng → cảnh báo user
