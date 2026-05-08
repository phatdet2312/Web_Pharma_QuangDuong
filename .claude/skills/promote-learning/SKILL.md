---
name: promote-learning
description: >
  Review learnings đã tích lũy và promote pattern lặp 3+ lần thành
  rule enforced. Dùng khi: user gõ /promote-learning, hoặc khi
  07_learnings.md có 5+ entries với Lần >= 3.
---
 
# Quy trình Promote Learning
 
## Bước 1: Scan `07_learnings.md`
- Tìm entries có `Lần` >= 3
- List ra cho user review
## Bước 2: Phân loại destination
 
| Loại | Promote đến |
|------|-------------|
| CONVENTION | `.claude/rules/java-backend.md` hoặc rule tương ứng |
| ARCHITECTURE | `01_system_architecture.md` → Architecture Decisions |
| SECURITY | `.claude/rules/` (tạo `security.md` nếu chưa có) |
| PERFORMANCE | Deep knowledge file tương ứng → Decision Log |
| LOGIC | Deep knowledge file tương ứng → Decision Log |
 
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
 
## QUY TẮC:
- LUÔN xin user confirm — KHÔNG tự promote
- Sau promote, kiểm tra rules file — nếu >30 dòng, cảnh báo user