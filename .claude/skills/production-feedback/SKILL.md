---
name: production-feedback
description: >
  Phân tích bug từ thực tế production để AI tự học từ hậu quả thật.
  Dùng khi user báo: "bug trên production", "lỗi đã deploy", "user gặp lỗi",
  "có lỗi sau khi merge", "bug thực tế", "cần phân tích bug đã ra mắt".
  Skill này KHÁC /debug-flow ở chỗ: tập trung TÌM ROOT CAUSE LỊCH SỬ
  (commit nào gây ra) + GHI LEARNING từ thất bại để không lặp lại.
---

# Quy trình Production Feedback Loop

## Mục đích
Đây là **feedback loop MẠNH NHẤT** trong hệ thống. Khác với test (giả lập), production bug là HẬU QUẢ THẬT.
Mục tiêu: AI hiểu bug, tìm commit gây ra, so với memory tại thời điểm đó → ghi learning để **không lặp lại**.

## ⚠️ NGUYÊN TẮC AN TOÀN
1. **Yêu cầu user XÁC NHẬN** "đây có phải bug do AI/agent gây ra không?" trước khi ghi learning.
   Nếu user nói "không, bug do tôi tự sửa tay" → skill này KẾT THÚC, không ghi gì.
2. **KHÔNG đoán commit gây ra** — phải đọc git log + git blame + verify
3. **KHÔNG ghi learning sai context** — chỉ ghi khi xác định được pattern AI vi phạm

## Bước 1: Thu thập thông tin bug từ user
Hỏi user:
- Bug xảy ra khi nào? (ngày/giờ nếu nhớ được)
- Module/feature nào?
- Triệu chứng cụ thể? (error message, stack trace nếu có)
- Đã fix chưa hay đang debug?

## Bước 2: Xác nhận bug do AI gây ra
Hỏi user thẳng:
```
"Trước khi tôi phân tích sâu để học từ bug này, xin xác nhận:
 - Đây là bug do code Claude/agent đã tạo/sửa, đúng không?
 - Hay là code bạn tự sửa tay / dùng tool khác / merge từ người khác?
 
 Nếu KHÔNG do agent gây ra → tôi sẽ chỉ debug-flow bình thường, không ghi learning.
 Nếu DO agent → tôi sẽ phân tích sâu + ghi learning."
```

**CHỜ user trả lời.** Nếu không phải bug AI → chuyển sang /debug-flow, kết thúc skill.

## Bước 3: Tìm commit gây ra (nếu user xác nhận do AI)
```bash
# Tìm file liên quan
git log --oneline --all -- <file_path> | head -20

# Tìm commit gần đây nhất sửa logic liên quan
git blame <file_path> | head -30

# Hoặc dùng bisect nếu bug có repro script
git bisect start
git bisect bad HEAD
git bisect good <known_good_commit>
```

Xác định: **commit nào** đã gây ra bug.

## Bước 4: Đọc memory tại thời điểm commit đó
```bash
# Xem memory tại commit cụ thể
git show <commit_hash>:.ai-memory/03_deep_knowledge/<module>.md
git show <commit_hash>:.ai-memory/01_system_architecture.md
```

So sánh:
- Memory tại thời điểm đó **có nói rõ** convention/constraint mà commit vi phạm KHÔNG?
- Nếu **CÓ** mà AI vẫn vi phạm → memory đầy đủ nhưng AI không follow → **learning về AI compliance**
- Nếu **KHÔNG** → memory thiếu thông tin → **learning về memory completeness**

## Bước 5: Phân loại pattern lỗi
Đặt vào 1 trong 3 nhóm:

**A. AI bypass convention dù memory có ghi**
- VD: memory ghi "luôn dùng PreparedStatement", AI viết Statement
- Action: Tăng cường rule trong `.claude/rules/` (path-scoped)
- Hoặc thêm pattern vào checklist của reviewer

**B. Memory thiếu thông tin business**
- VD: convention thực tế là "User chỉ admin mới đổi role", memory không ghi
- Action: Cập nhật deep_knowledge file tương ứng
- Cảnh báo cho bootstrap-memory ở dự án sau

**C. Edge case AI không nghĩ tới**
- VD: input null từ legacy data
- Action: Ghi vào 07_learnings.md, đề xuất thêm vào adversarial-critic checklist

## Bước 6: Ghi learning
File: `.ai-memory/07_learnings.md`

Format:
```
| ID | Ngày | Pattern | Module | Loại (A/B/C) | Lần | Action đã làm | Verify |
|----|------|---------|--------|--------------|-----|---------------|--------|
| PROD-001 | 2026-05-09 | Quên dùng PreparedStatement khi query có user input | post | A | 1 | Thêm rule database.md | NO |
```

Nếu pattern lặp từ entry cũ (cùng "Pattern" + "Module") → tăng "Lần", đề xuất `/promote-learning` khi Lần >= 3.

## Bước 7: Đề xuất hành động sửa hệ thống
Sau khi ghi learning:
- **Loại A**: đề xuất user thêm vào `.claude/rules/<scope>.md` 1 dòng rule rõ ràng
- **Loại B**: đề xuất user mở deep_knowledge file để bổ sung
- **Loại C**: đề xuất thêm checklist vào adversarial-critic.md mục tương ứng

**KHÔNG TỰ SỬA** — chỉ ĐỀ XUẤT, user quyết định.

## Bước 8: Báo cáo
```
✅ Production Feedback Analysis:
- Bug: [mô tả ngắn]
- Commit gây ra: [hash + ngày]
- Loại pattern: [A/B/C]
- Memory tại thời điểm đó: [đầy đủ/thiếu]
- Learning đã ghi: PROD-XXX (Lần Y)
- Đề xuất hành động: [...]

⏭️ Bước tiếp:
- User review đề xuất trên
- Sau khi user approve → memory-keeper sync
```

## Giới hạn
- KHÔNG phân tích bug khi user không nhớ commit hash + ngày → cần info tối thiểu
- KHÔNG ghi learning nếu user không xác nhận bug do AI gây ra
- Phụ thuộc git history — nếu force push / squash làm mất history → giới hạn
- Trả lời bằng tiếng Việt
