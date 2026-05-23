---
name: review-module
description: >
  Review toàn diện 1 module/feature theo Two-Tier Pattern.
  Dùng mỗi khi user yêu cầu review, kiểm tra chất lượng, audit code,
  hoặc nhắc đến "review", "kiểm tra", "code quality", "có ổn không", "xem lại".
---

# Quy trình Review Module (Two-Tier Pattern)

## Bước 1: Xác định phạm vi
- Đọc `.ai-memory/03_deep_knowledge/` liên quan
- List tất cả file thuộc module
- Đếm số dòng thay đổi → nếu > 300 dòng: ghi nhớ để TRIGGER BẮT BUỘC tầng 2

## Bước 2: Kiểm tra Critical Path
Module có thuộc các vùng critical sau không?
- `auth/` — đăng nhập, đăng ký, RBAC
- `payment/` — thanh toán, ví
- `transaction/` — giao dịch DB nhiều bước
- `scheduler/` — cron, batch job
- `migration/` — DB schema migration

→ Nếu CÓ: bắt buộc gọi tầng 2 (`deep-reviewer`) sau tầng 1, dù confidence là gì.

## Bước 3: Tầng 1 — Gọi `reviewer` (Sonnet)
Reviewer chạy checklist + xuất **Confidence Report**.

### Checklist tầng 1
**Security (CRITICAL)**
- [ ] Hardcoded credential?
- [ ] SQL injection (raw query)?
- [ ] XSS (unescaped output)?
- [ ] Missing auth check?
- [ ] Sensitive data trong log?

**Logic (HIGH)**
- [ ] Null pointer risk?
- [ ] Edge case chưa handle?
- [ ] Exception handling đúng?
- [ ] Business rule đúng?

**Performance (MEDIUM)**
- [ ] N+1 query?
- [ ] Missing index / pagination?
- [ ] Unnecessary loop?

**Convention (LOW)**
- [ ] Naming convention?
- [ ] Method > 30 dòng?
- [ ] Dead code / duplication?

### Output bắt buộc của reviewer
1. Bảng Issues (severity + file:line)
2. **Confidence Report** (Logic / Concurrency / Security / Architectural fit / Performance)
3. Escalation Recommendation

## Bước 4: Đọc Confidence Report → Quyết định escalate

| Trigger | Gọi tầng 2 |
|---------|-----------|
| Security = LOW | `security-auditor` (Opus) |
| Logic / Concurrency / Architecture / Performance = LOW | `deep-reviewer` (Opus) |
| Module thuộc critical path `auth/` hoặc `payment/` | **BẮT BUỘC** gọi CẢ `security-auditor` VÀ `deep-reviewer` (bỏ qua confidence) |
| Module thuộc critical path `transaction/`, `scheduler/`, `migration/` | **BẮT BUỘC** `deep-reviewer` (bỏ qua confidence). security-auditor nếu Security < HIGH |
| PR > 300 dòng | `deep-reviewer` (BẮT BUỘC) |
| User yêu cầu "review sâu" / "deep review" thủ công | `deep-reviewer` (bỏ qua tầng 1) |
| Tất cả HIGH/MEDIUM + không critical path | KHÔNG gọi tầng 2 → đi thẳng Bước 6 |

## Bước 5: Tầng 2 — Gọi agent escalation
- `security-auditor`: kiểm sâu OWASP, auth bypass, secret leak
- `deep-reviewer`: kiểm sâu logic, concurrency, transaction, architectural smell
- KHÔNG gọi cả 2 cùng lúc nếu không cần — chỉ gọi agent tương ứng vùng LOW

### Thứ tự gọi khi BẮT BUỘC cả 2 (auth/payment critical path):
1. **security-auditor TRƯỚC** — vì security finding có thể đổi cả structure code (ví dụ: bỏ raw SQL → dùng PreparedStatement = đổi luồng)
2. Nếu security-auditor tìm BLOCKER → user fix → restart Bước 3 (reviewer Sonnet review lại code đã fix)
3. **deep-reviewer SAU** — review trên code đã pass security
4. Lý do thứ tự: review logic trên code có security flaw → tốn công review code sắp bị refactor

## Bước 6: Tổng hợp báo cáo
Gộp issues từ tầng 1 + tầng 2 (nếu có), xếp theo severity:

| # | Severity | Tầng | File:Line | Issue | Suggestion |
|---|----------|------|-----------|-------|------------|

Severity: CRITICAL/BLOCKER → WARNING/MAJOR → INFO/MINOR

## Bước 7: Cập nhật memory
Sau review xong, gọi `memory-keeper` để:
- Ghi log review vào `06_evolution_log.md`
- Cập nhật `03_deep_knowledge/` nếu phát hiện convention mới
- Ghi decision (nếu deep-reviewer trả về Decision Note)
