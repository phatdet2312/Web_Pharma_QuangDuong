---
name: production-feedback
description: "Học từ bug production thật. Dùng khi user báo \"bug trên production\", \"lỗi đã deploy\", \"user thật gặp\", \"có lỗi sau khi merge\". KHÁC $debug-flow ở chỗ: tìm commit gây ra + ghi learning từ thất bại để không lặp."
---

# Quy trình Production Feedback Loop

## Mục đích
Đây là **feedback loop MẠNH NHẤT** trong hệ thống. Production bug là HẬU QUẢ THẬT (không phải test giả lập).
Codex hiểu bug → tìm commit gây ra → so với memory tại thời điểm đó → ghi learning để KHÔNG LẶP LẠI.

## ⚠️ NGUYÊN TẮC AN TOÀN
1. Yêu cầu user XÁC NHẬN "bug này có phải do AI/Codex gây ra không?" trước khi ghi learning. Nếu "không, tôi tự sửa" → skill KẾT THÚC.
2. KHÔNG đoán commit gây ra — phải `git log` + `git blame` + verify
3. KHÔNG ghi learning sai context

## Bước 1: Thu thập thông tin bug
Hỏi user:
- Bug xảy ra khi nào? (ngày/giờ)
- Module/feature nào?
- Triệu chứng (error message, stack trace)?
- Đã fix chưa hay đang debug?

## Bước 2: Xác nhận bug do AI gây ra
Hỏi thẳng:
```
Trước khi tôi phân tích sâu để học từ bug này, xin xác nhận:
- Đây là bug do code Codex/agent tạo/sửa, đúng không?
- Hay code bạn tự sửa tay / dùng tool khác / merge từ người khác?

Nếu KHÔNG do agent → tôi chỉ $debug-flow bình thường, không ghi learning.
Nếu DO agent → tôi phân tích sâu + ghi learning.
```
CHỜ user trả lời. Không phải bug AI → chuyển $debug-flow, kết thúc skill.

## Bước 3: Tìm commit gây ra
```bash
git log --oneline --all -- <file_path> | head -20
git blame <file_path> | head -30

# Hoặc bisect nếu có repro script
git bisect start
git bisect bad HEAD
git bisect good <known_good_commit>
```

## Bước 4: Đọc memory tại thời điểm commit đó
```bash
git show <commit_hash>:.ai-memory/03_deep_knowledge/<module>.md
git show <commit_hash>:.ai-memory/01_system_architecture.md
```

So sánh:
- Memory tại thời điểm đó CÓ nói convention/constraint mà commit vi phạm KHÔNG?
- CÓ + AI vẫn vi phạm → memory đầy đủ nhưng AI không follow → **learning về AI compliance**
- KHÔNG → memory thiếu → **learning về memory completeness**

## Bước 5: Phân loại pattern lỗi

**A. AI bypass convention dù memory có ghi**
- VD: memory ghi "luôn dùng PreparedStatement", AI viết Statement
- Action: Tăng rule trong nested AGENTS.md hoặc bullet rules trong AGENTS.md root
- Hoặc thêm pattern vào checklist của `reviewer`

**B. Memory thiếu thông tin business**
- VD: convention thực tế là "User chỉ admin mới đổi role", memory không ghi
- Action: Cập nhật deep_knowledge file tương ứng
- Nếu nguyên nhân là bootstrap/sync-memory không phát hiện được constraint này, đề xuất bổ sung heuristic/checklist cho `$bootstrap-memory` hoặc `$sync-memory` để dự án sau không lặp lại thiếu sót.

**C. Edge case AI không nghĩ tới**
- VD: input null từ legacy data
- Action: Ghi vào 07_learnings.md, đề xuất thêm checklist vào `adversarial-critic`

## Bước 6: Ghi learning
File: `.ai-memory/07_learnings.md`

Format:
```
| ID | Ngày | Pattern | Module | Loại (A/B/C) | Lần | Action đã làm | Verify |
|----|------|---------|--------|--------------|-----|---------------|--------|
| PROD-001 | 2026-05-15 | Quên PreparedStatement khi query có user input | post | A | 1 | Thêm rule | NO |
```

Pattern lặp từ entry cũ (cùng Pattern + Module) → tăng "Lần", đề xuất $promote-learning khi Lần >= 3.

## Bước 7: Đề xuất hành động sửa hệ thống (Hybrid pattern)
- **Loại A** (AI bypass convention): đề xuất user thêm rule vào `.codex/rules/<scope>.md` (source of truth — `backend.md`, `frontend.md`, `database.md`, hoặc tạo mới như `security.md`). NẾU rule đó cần "load ngay khi cwd vào folder code" → THÊM 1 dòng vào nested AGENTS.md tương ứng (top rule). KHÔNG promote chỉ vào nested AGENTS.md (vi phạm DRY).
- **Loại B** (memory thiếu thông tin): đề xuất user mở deep_knowledge file để bổ sung; nếu thiếu do bootstrap/sync-memory không bắt được pattern, đề xuất cập nhật checklist của `$bootstrap-memory`/`$sync-memory`
- **Loại C** (edge case AI không nghĩ tới): đề xuất thêm checklist vào `.codex/agents/adversarial-critic.toml` (5 dimensions A-E)

KHÔNG TỰ SỬA — chỉ ĐỀ XUẤT, user quyết định.

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
- User review đề xuất
- User approve → memory-keeper sync
```

## Giới hạn
- KHÔNG phân tích khi user không nhớ commit hash + ngày
- KHÔNG ghi learning nếu user không xác nhận bug do AI
- Phụ thuộc git history — force push / squash → giới hạn
