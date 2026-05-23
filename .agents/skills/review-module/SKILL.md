---
name: review-module
description: Two-Tier Review module/feature. Auto-trigger khi user nói "review", "kiểm tra", "code quality", "có ổn không", "xem lại". Tầng 1 reviewer (medium) + Confidence Report → escalate tầng 2 (deep-reviewer/security-auditor) khi cần.
---

# Quy trình Review Module (Two-Tier Pattern)

## Bước 1: Xác định phạm vi
- Đọc `.ai-memory/03_deep_knowledge/` liên quan
- List file thuộc module
- Đếm số dòng thay đổi → > 300 → TRIGGER BẮT BUỘC tầng 2

## Bước 2: Kiểm tra Critical Path
Module có thuộc vùng critical sau không?
- `auth/` — đăng nhập, đăng ký, RBAC
- `payment/` — thanh toán, ví
- `transaction/` — giao dịch DB nhiều bước
- `scheduler/` — cron, batch job
- `migration/` — DB schema migration

→ CÓ: bắt buộc gọi tầng 2 (`deep-reviewer` subagent), dù confidence là gì.

## Bước 3: Tầng 1 — Spawn `reviewer` (medium tier)
Gọi `/agent reviewer` để spawn subagent review.
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
2. Confidence Report (Logic / Concurrency / Security / Architectural fit / Performance)
3. Escalation Recommendation

## Bước 4: Đọc Confidence Report → Quyết định escalate

| Trigger | Spawn tầng 2 |
|---------|--------------|
| Security = LOW | `security-auditor` (high tier) |
| Logic / Concurrency / Architecture / Performance = LOW | `deep-reviewer` (high tier) |
| Critical path `auth/` hoặc `payment/` | **BẮT BUỘC** CẢ `security-auditor` + `deep-reviewer` |
| Critical path `transaction/`, `scheduler/`, `migration/` | **BẮT BUỘC** `deep-reviewer`. security-auditor nếu Security < HIGH |
| PR > 300 dòng | `deep-reviewer` (BẮT BUỘC) |
| User nói "review sâu" | `deep-reviewer` trực tiếp (bỏ qua tầng 1) |
| Tất cả HIGH/MEDIUM + không critical path | KHÔNG escalate → Bước 6 |

## Bước 5: Tầng 2 — Spawn subagent escalation
- `security-auditor`: OWASP, auth bypass, secret leak
- `deep-reviewer`: logic, concurrency, transaction, architectural smell
- KHÔNG gọi cả 2 cùng lúc nếu không cần

### Thứ tự khi BẮT BUỘC cả 2 (auth/payment):
1. **`security-auditor` TRƯỚC** — security finding có thể đổi cả structure code
2. Nếu security tìm BLOCKER → user fix → restart Bước 3 (reviewer review lại code đã fix)
3. **`deep-reviewer` SAU** — review trên code đã pass security
4. **Lý do thứ tự**: review logic trên code có security flaw → tốn công review code sắp bị refactor

## Bước 6: Tổng hợp báo cáo
Gộp issues tầng 1 + tầng 2, xếp theo severity:
| # | Severity | Tầng | File:Line | Issue | Suggestion |

Severity: CRITICAL/BLOCKER → WARNING/MAJOR → INFO/MINOR

## Bước 7: Cập nhật memory
Spawn `memory-keeper` để:
- Ghi log review vào `06_evolution_log.md`
- Cập nhật `03_deep_knowledge/` nếu phát hiện convention mới
- Ghi decision (nếu deep-reviewer trả về Decision Note)

## Mở rộng
- Spawn nhiều subagent reviewer song song để review nhiều file/PR cùng lúc (Codex max_threads=6)
- Adversarial review (tầng 3) — chỉ khi user explicit yêu cầu "audit kỹ", "stress test code"
