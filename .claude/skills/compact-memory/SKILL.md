---
name: compact-memory
description: >
  Nén memory dài hạn để tránh phình to và nhiễu loạn. Dùng khi:
  user nói "nén memory", "compact memory", "memory quá lớn", "dọn dẹp memory",
  hoặc tự động trigger khi 06_evolution_log.md > 50KB, hoặc mỗi tháng 1 lần.
---

# Quy trình Nén Memory (Memory Compaction)

## Mục đích
Tránh memory phình to vô hạn theo thời gian (sau 6 tháng, `06_evolution_log.md` có thể > 100KB).
Nén theo nguyên tắc **summarize old, keep new, archive raw**.

## ⚠️ NGUYÊN TẮC AN TOÀN
1. **KHÔNG bao giờ xóa thông tin** — chỉ archive ra `.ai-memory/_archive/`
2. **Raw log vẫn còn trong git history** — có thể recover bằng `git log`
3. **Trước khi nén: BACKUP commit** — `git add .ai-memory/ && git commit -m "chore: pre-compact backup"`
4. **Yêu cầu user xác nhận** trước khi nén — không tự ý

## Bước 1: Kiểm tra điều kiện
- Đọc kích thước `06_evolution_log.md`, `07_learnings.md`, mỗi file trong `03_deep_knowledge/`
- Nếu KHÔNG có file > 50KB và chưa đến thời điểm hàng tháng → báo: "Memory còn lành mạnh, không cần nén"
- Nếu CÓ → tiếp tục, báo user kích thước

## Bước 2: Xin xác nhận user
Báo:
```
Memory đang chiếm: 87KB
File lớn: 06_evolution_log.md (62KB, 156 entry)
Đề xuất nén:
- Entry < 1 tháng: GIỮ NGUYÊN (24 entry)
- Entry 1-3 tháng: TÓM TẮT theo tuần (78 entry → 12 tuần tóm tắt)
- Entry > 3 tháng: TÓM TẮT theo tháng (54 entry → 4 tháng tóm tắt)
- Raw log → archive vào _archive/evolution_2024_Q4.md

Tiếp tục? (yes/no)
```
**CHỜ user xác nhận** rồi mới làm.

## Bước 3: Backup
```bash
git add .ai-memory/
git commit -m "chore: pre-compact memory backup"
```

## Bước 4: Tạo folder archive (nếu chưa có)
- `.ai-memory/_archive/` — chứa raw log đã nén

## Bước 5: Nén 06_evolution_log.md
- Đọc toàn bộ file
- Phân loại theo ngày so với hôm nay
- Entry < 1 tháng: GIỮ trong file gốc
- Entry 1-3 tháng: tạo block "Tuần XX/YYYY: [tóm tắt 1-2 dòng]"
- Entry > 3 tháng: tạo block "Tháng MM/YYYY: [tóm tắt 1 dòng]"
- Ghi raw lên file `_archive/evolution_<period>.md`
- Viết lại `06_evolution_log.md` chỉ với phần nén + entry < 1 tháng

## Bước 6: Nén 07_learnings.md
- Entry "Lần >= 5" + ổn định > 1 tháng → đề xuất `/promote-learning` để chuyển sang `.claude/rules/learned.md`
- Entry "Lần 1, > 3 tháng cũ, không lặp lại" → archive vào `_archive/learnings_<period>.md`
- KHÔNG tự promote — chỉ đề xuất

## Bước 7: Nén deep_knowledge cũ
- Mỗi file trong `03_deep_knowledge/` có `Last updated > 6 tháng` → KIỂM TRA xem code module đó còn không
- Nếu module đã bị xóa khỏi codebase → archive file deep_knowledge tương ứng
- Nếu module còn → giữ nguyên (deep_knowledge KHÔNG nên nén theo thời gian, chỉ nén khi module chết)

## Bước 8: Báo cáo
```
✅ Đã nén memory:
- 06_evolution_log.md: 62KB → 18KB (24 entry mới + 16 block tóm tắt)
- 07_learnings.md: ổn định, đề xuất /promote-learning cho 3 entry
- Archive: _archive/evolution_2024_Q4.md (raw 156 entry)
- Commit backup: <hash>

Phiên sau: AI vẫn nhớ pattern quan trọng, raw có thể recover từ archive
```

## Giới hạn
- KHÔNG nén `01_system_architecture.md`, `02_project_map.md` — đây là active state
- KHÔNG nén `04_active_plan.md`, `05_active_workspace.md` — đây là daily state
- CHỈ nén file LOG/HISTORY (06, 07) và deep_knowledge của module chết

## Recovery nếu nén nhầm
```bash
git revert HEAD  # quay lại trước nén
# hoặc đọc lại từ _archive/
```
