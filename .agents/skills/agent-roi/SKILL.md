---
name: agent-roi
description: Phân tích ROI của các subagent — agent nào tốn token, agent nào hữu ích. Dùng khi user hỏi "agent nào tốn token", "phân tích chi phí", "agent ROI", "đo lường agent".
---

# Quy trình phân tích ROI Subagent

## Mục đích
Dựa trên `.codex/agent-metrics.csv` (hook `log-agent-turn.sh` ghi tại event `Stop`) + `~/.codex/sessions/` (session transcript) → tính bảng ROI cho mỗi subagent → user biết agent nào đáng giữ, đáng tinh chỉnh.

## ⚠️ Giới hạn Codex 2026 (verify 16/5/2026)
Codex `Stop` event **KHÔNG expose** các field sau (theo doc `developers.openai.com/codex/hooks`):
- `usage.input_tokens` / `usage.output_tokens` (token count)
- `duration_ms` (thời gian agent xử lý)
- `agent_name` cho subagent spawn (không có event `SubagentStop` riêng)
- `task_summary`

CSV header thực tế:
```
timestamp, session_id, turn_id, model, permission_mode, duration_approx_ms, cwd, last_msg_snippet, note
```

**Workaround đã triển khai:**
- `duration_approx_ms`: tính delta giữa 2 lần `Stop` liên tiếp (xấp xỉ — KHÔNG phải duration agent thực)
- `last_msg_snippet`: 80 ký tự đầu của `last_assistant_message` (để có context turn làm gì)
- `note`: ghi `tokens=N/A;agent_name=N/A;duration=approx` để user biết giới hạn

**Lấy token chính xác:** parse `~/.codex/sessions/<session_id>/transcript.jsonl` — nhưng theo doc Codex: *"transcript format is not a stable interface for hooks and may change"*. Nếu parse được, tốt; nếu không, ghi N/A.

## Bước 1: Đọc dữ liệu
- Đọc `.codex/agent-metrics.csv`
- File không tồn tại hoặc < 5 dòng → "Chưa đủ dữ liệu, cần ít nhất 5 lần spawn agent"
- File > 2MB → đề xuất user rotation: archive thành `agent-metrics-<YYYY-MM>.csv` và bắt đầu CSV mới
- Thử mở `~/.codex/sessions/<recent_session_ids>/` để lấy token count (nếu Codex CLI vẫn ghi)

## Bước 2: Tính toán (chỉ thống kê, KHÔNG đoán)
**Per-session (luôn có data):**
- Số turn (count rows by session_id)
- Tổng `duration_approx_ms` (sum) — đây là approximation
- Models đã dùng (distinct)
- Permission mode breakdown

**Per-subagent (CHỈ nếu parse được session transcript):**
- Spawn count, tổng input/output tokens, trung bình tokens/spawn
- Chi phí ước tính theo pricing public:
  - high: $15/M input, $75/M output (verify openai.com/api/pricing)
  - medium: $3/M input, $15/M output
  - low: $0.80/M input, $4/M output
- Nếu KHÔNG parse được transcript → bỏ qua phần này, BÁO user: "Codex chưa expose token per-subagent. Chỉ có thể phân tích aggregate level từ CSV."

## Bước 3: Output bảng
**Aggregate (luôn có):**
```
## Session-level Stats — [date range]
| Session ID | Turns | Total duration_approx | Models | Permission modes |
|-----------|-------|----------------------|--------|------------------|
| abc123... | 47    | 14m                  | &lt;model-from-config&gt; | on-request      |
```

**Per-subagent (chỉ nếu có session transcript):**
```
## Subagent ROI — [date range]
| Subagent | Tier | Spawns | Avg in | Avg out | Avg time | $/call | Tổng $ |
| reviewer | medium | 47 | 4.5K | 1.2K | 18s | $0.032 | $1.50 |
```
Sắp xếp theo **Tổng $** giảm dần. **Nếu thiếu token data: chỉ sort theo Spawn count.**

## Bước 4: Phân tích (CẨN THẬN — không kết luận sai)
3 tiêu chí (chỉ áp dụng khi có đủ data):
1. **Gọi quá ít** (<3 lần/tháng): có thể xóa hoặc gộp vai trò
2. **Gọi quá nhiều** (>50 lần/tháng): có thể tinh chỉnh `description` để giảm
3. **Tỉ lệ output/input cao** (>0.5): subagent xuất nhiều — có thể quá verbose

TUYỆT ĐỐI KHÔNG kết luận "agent X vô dụng" — chỉ đưa data, user quyết định.

## Bước 5: Đề xuất (chỉ nếu có pattern rõ)
- "Subagent X chỉ gọi 1 lần trong 30 ngày — bạn có muốn xóa/gộp?"
- "Subagent Y tốn 60% ngân sách high tier — có muốn downgrade medium?"
- KHÔNG TỰ XÓA, chỉ ĐỀ XUẤT.

## Nguyên tắc
- Chỉ trình bày SỐ THỰC từ CSV + session log, KHÔNG bịa
- Khi data thiếu (vd: token=N/A): NÓI RÕ "chưa đo được do Codex 2026 không expose", không đoán
- User QUYẾT ĐỊNH cuối cùng
- Trả lời tiếng Việt
- Khi Codex CLI cập nhật và expose token usage trong Stop event tương lai → cập nhật hook `log-agent-turn` để lưu thêm, đồng thời update skill này
