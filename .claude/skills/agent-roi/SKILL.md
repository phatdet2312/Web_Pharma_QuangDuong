---
name: agent-roi
description: >
  Phân tích ROI của các agent trong hệ thống. Dùng khi user muốn biết:
  agent nào hữu ích, agent nào lãng phí token, tỉ lệ "có giá trị" mỗi agent.
  Cũng dùng khi user nói "phân tích chi phí", "agent nào tốn token",
  "agent ROI", "telemetry", "đo lường agent".
---

# Quy trình phân tích ROI Agent

## Mục đích
Dựa trên `.claude/agent-metrics.csv` (do user hoặc script bên ngoài ghi — không tự sinh),
tính toán bảng ROI cho từng agent → user biết agent nào đáng giữ, đáng tinh chỉnh.

## Bước 1: Đọc dữ liệu
- Đọc `.claude/agent-metrics.csv`
- Nếu file không tồn tại hoặc < 5 dòng → báo: "Chưa đủ dữ liệu, cần ít nhất 5 lần spawn agent"
- **Kiểm tra kích thước file**: nếu > 2MB (~40K rows) → trước khi phân tích, đề xuất user:
  "CSV đã > 2MB. Bạn muốn rotation? Tôi sẽ archive thành `.claude/agent-metrics-<YYYY-MM>.csv` và bắt đầu CSV mới."
  Sau khi user OK → đổi tên file + tạo file mới với header. Tiếp tục phân tích trên CSV mới.
- Nếu có dữ liệu → tiếp tục

## Bước 2: Tính toán (chỉ thống kê, KHÔNG đoán)
Cho mỗi agent, tính:
- **Số lần gọi** (count)
- **Tổng input tokens, output tokens**
- **Trung bình tokens/lần**
- **Trung bình duration**
- **Chi phí ước tính** ($/lần) dựa trên model:
  - opus: $15/M input, $75/M output
  - sonnet: $3/M input, $15/M output
  - haiku: $0.80/M input, $4/M output

## Bước 3: Output bảng

```
## Agent ROI Report — [date range]

| Agent | Model | Calls | Avg in | Avg out | Avg time | $/call | Tổng $ |
|-------|-------|-------|--------|---------|----------|--------|--------|
| reviewer | sonnet | 47 | 4.5K | 1.2K | 18s | $0.032 | $1.50 |
| ... | ... | ... | ... | ... | ... | ... | ... |
```

Sắp xếp theo **Tổng $** giảm dần.

## Bước 4: Phân tích (CẨN THẬN — không kết luận sai)
Đánh giá MỖI agent theo 3 tiêu chí:
1. **Gọi quá ít** (< 3 lần/tháng): có thể xóa hoặc gộp vai trò
2. **Gọi quá nhiều** (> 50 lần/tháng): có thể tinh chỉnh trigger để giảm
3. **Tỉ lệ output/input cao** (> 0.5): agent xuất ra nhiều — có thể quá verbose

TUYỆT ĐỐI KHÔNG kết luận "agent X vô dụng" — chỉ đưa data, user quyết định.

## Bước 5: Đề xuất (chỉ nếu có pattern rõ)
- "Agent X chỉ gọi 1 lần trong 30 ngày — bạn có muốn xem xét xóa/gộp?"
- "Agent Y tốn 60% ngân sách Opus — có muốn xem có thể downgrade Sonnet?"
- KHÔNG TỰ XÓA, chỉ ĐỀ XUẤT

## Nguyên tắc
- Chỉ trình bày SỐ THỰC từ CSV, không bịa
- Để user QUYẾT ĐỊNH cuối cùng
- Không đề xuất xóa agent nếu user chưa xác nhận
- Trả lời bằng tiếng Việt

## Giới hạn
- Token count chính xác phụ thuộc dữ liệu CSV — file này không tự sinh, cần user hoặc script ngoài ghi
- Chi phí ước tính theo public pricing, có thể khác nếu user có enterprise plan
