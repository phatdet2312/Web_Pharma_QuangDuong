---
name: reflect
description: Ghi learning sau sai lầm hoặc correction từ user. Tự động trigger khi user sửa output, nói "sai rồi"/"không đúng"/"làm lại", test fail 2+ vòng, hoặc Codex tự phát hiện output không khớp yêu cầu.
---

# Quy trình Reflect — Biến sai lầm thành learning

## Bước 1: Xác định sai lầm
- Điều gì sai? File nào bị ảnh hưởng?
- Root cause: thiếu context? hiểu sai yêu cầu? sai convention? logic error?

## Bước 2: Trừu tượng hóa
KHÔNG ghi "file UserService.java dòng 42 thiếu null check"
MÀ ghi "Sai: chưa check null từ external service. Đúng: LUÔN check null"
- Pattern tổng quát nào gây sai?
- Rule nào ngăn pattern này lặp lại?
- Viết dạng: "Sai: [pattern]. Đúng: LUÔN/KHÔNG BAO GIỜ [rule]"

## Bước 3: Ghi vào `.ai-memory/07_learnings.md`
- Kiểm tra trước: pattern đã có → tăng cột `Lần`, KHÔNG tạo dòng mới
- Chưa có → thêm dòng mới vào bảng
- Cập nhật header: `Tổng entries` và `Entries sẵn sàng promote`

## Bước 4: Kiểm tra promotion
- `Lần` >= 3 → thông báo user: "Pattern [X] lặp 3+ lần — chạy $promote-learning để đưa lên rules"

## QUY TẮC
- LUÔN trừu tượng hóa — ghi PATTERN, không ghi bug cụ thể
- Mỗi entry ≤ 1 dòng bảng
- KHÔNG duplicate: kiểm tra trước khi ghi
