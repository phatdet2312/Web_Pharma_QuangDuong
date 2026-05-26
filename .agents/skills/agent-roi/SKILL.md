---
name: agent-roi
description: Phân tích ROI của các subagent — agent nào tốn token, agent nào hữu ích. Dùng khi user hỏi "agent nào tốn token", "phân tích chi phí", "agent ROI", "đo lường agent".
---

# Quy trình phân tích ROI Subagent

## Mục đích

Dựa trên telemetry thật từ Codex hooks để đánh giá tần suất dùng, thời gian chạy
và chi phí tương đối của từng subagent. KHÔNG đoán token/cost nếu Codex chưa
expose dữ liệu usage trong hook payload.

Nguồn dữ liệu:
- `.codex/subagent-metrics.csv`: ghi từ `SubagentStart` + `SubagentStop`.
- `.codex/agent-metrics.csv`: aggregate turn chính từ `Stop`.
- `~/.codex/sessions/`: chỉ dùng như fallback thủ công vì transcript format không ổn định cho hooks.

## Giới hạn Codex 0.133.x

`SubagentStop` đã expose field hữu ích:
- `agent_id`
- `agent_type`
- `agent_transcript_path`
- `last_assistant_message`

Nhưng hook payload vẫn chưa expose trực tiếp:
- `usage.input_tokens`
- `usage.output_tokens`
- chi phí USD đã tính sẵn

Vì vậy:
- Được tính `spawn count`, `duration_ms`, `agent_type`.
- KHÔNG bịa token/cost nếu không có dữ liệu thật.
- Nếu parse transcript để lấy token thì phải nói rõ đây là fallback không ổn định.

## Bước 1: Đọc dữ liệu

1. Đọc `.codex/subagent-metrics.csv`.
2. Nếu file không tồn tại hoặc < 5 dòng: báo "Chưa đủ dữ liệu, cần ít nhất 5 lần spawn agent".
3. Nếu file > 2MB: đề xuất archive thành `subagent-metrics-<YYYY-MM>.csv`.
4. Đọc thêm `.codex/agent-metrics.csv` nếu cần context aggregate theo session.

CSV header chính:

```text
timestamp,session_id,turn_id,agent_id,agent_type,model,permission_mode,duration_ms,cwd,last_msg_snippet,note
```

## Bước 2: Tính toán

Per-subagent, luôn có nếu hook đã chạy:
- Spawn count theo `agent_type`
- Tổng `duration_ms`
- Trung bình `duration_ms`
- Model đã dùng
- Breakdown theo `permission_mode`
- Snippet output gần nhất để nhận diện agent đang làm việc gì

Token/cost:
- Chỉ tính nếu có token thật từ transcript hoặc nguồn log ổn định.
- Nếu không có token: hiển thị `N/A`, sắp xếp bảng theo `Spawn count` hoặc `Total duration`.

## Bước 3: Output bảng

```markdown
## Subagent ROI — [date range]
| Subagent | Spawns | Avg time | Total time | Models | Permission modes | Token/cost |
|----------|--------|----------|------------|--------|------------------|------------|
| reviewer | 12 | 18s | 216s | gpt-5.5 | default | N/A |
```

Nếu có transcript token thật:

```markdown
| Subagent | Spawns | Avg input | Avg output | Avg time | Est cost |
|----------|--------|-----------|------------|----------|----------|
| reviewer | 12 | 4.5K | 1.2K | 18s | $1.50 |
```

## Bước 4: Phân tích cẩn thận

Chỉ đưa khuyến nghị khi có pattern rõ:
- Gọi quá ít: `<3` lần/tháng, có thể gộp role hoặc giữ nếu là critical path.
- Gọi quá nhiều: `>50` lần/tháng, nên tinh chỉnh `description`/routing.
- Thời gian cao bất thường: cần xem prompt, agent role và output snippet.

TUYỆT ĐỐI KHÔNG kết luận "agent X vô dụng" nếu chỉ có telemetry thấp. Chỉ đưa
dữ liệu và để user quyết định.

## Nguyên tắc

- Chỉ trình bày số thật từ CSV/session log.
- Không bịa token, không bịa chi phí.
- Nếu token = `N/A`, nói rõ: "Codex hook chưa expose token per-subagent".
- Trả lời tiếng Việt.
- Khi Codex expose token usage trong hook payload, cập nhật `log-subagent-stop`
  và file skill này để tính cost trực tiếp.
