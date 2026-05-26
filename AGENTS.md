# Project AGENTS.md

Hệ thống agent đầy đủ cho Codex CLI — Memory Bank bền vững + 17 subagents chuyên biệt
+ 14 Skills tự động hóa workflow + Hooks framework.

## ⚠️ GUARD: Kiểm tra bootstrap
Trước MỌI task, kiểm tra `.ai-memory/01_system_architecture.md`:
- Nếu chứa `PENDING_BOOTSTRAP` → **DỪNG LẠI**, chạy skill `$bootstrap-memory` trước
- Nếu section `Project Convention` trống → chạy `$bootstrap-memory` để điền
- Chỉ tiếp tục task khi `.ai-memory/01_system_architecture.md` đã có nội dung thật

## Quy tắc bắt buộc
- Tuân thủ convention của dự án (xem section "Project Convention" trong `.ai-memory/01_system_architecture.md`)
- KHÔNG hardcode password, secret key, API key trong code
- Validate TOÀN BỘ input từ client
- Trả lời bằng tiếng Việt trừ khi user yêu cầu khác

## ⛔ Path policy tập trung — `.codexignore`
Codex hooks hiện chưa có Read matcher/`permissionDecision: deny` cho tool Read. Codex có native filesystem permissions trong `.codex/config.toml`, nhưng cơ chế đó không import động từ ignore file. Vì vậy bộ này dùng `.codexignore` làm source of truth dễ quản lý, và mirror riêng phần `# sensitive` vào config để có lớp native deny-read.

MỌI agent/subagent BẮT BUỘC đọc `.codexignore` đầu phiên và hiểu tag:
- Dòng không tag: bỏ qua khi quét rộng/list workspace; vẫn có thể `Read` file cụ thể nếu task có lý do rõ.
- `# sensitive`: KHÔNG Read trừ khi user EXPLICIT yêu cầu trong prompt hiện tại + đã được cảnh báo rủi ro. Nếu buộc phải đọc: chỉ trích đoạn cần thiết, MASK giá trị secret (`API_KEY=sk-***MASKED***`), KHÔNG paste raw vào báo cáo.
- `# protected-edit`: KHÔNG tự sửa/tạo/refactor nếu chưa có xác nhận rõ từ user. Hook `pre-edit-protect` đọc tag này.
- Nếu vô tình đọc secret: dừng ngay, báo user, KHÔNG echo nội dung file vào response.
- Khi cần kiểm tra file sensitive cho task hợp lệ: ưu tiên `rg`/pattern cụ thể thay vì Read full file.

`.codexignore` là single source of truth cho danh sách path policy — KHÔNG duplicate danh sách pattern trong AGENTS.md, skills, hay scripts. Ngoại lệ duy nhất: `.codex/config.toml` phải mirror block `# sensitive` vì đây là cấu hình native của Codex, không đọc được `.codexignore`.

## Thứ tự ưu tiên nguồn thông tin
Khi có xung đột giữa các nguồn, tuân thủ thứ tự sau:
1. **Code thực tế** — LUÔN ĐÚNG NHẤT, không bao giờ sai
2. **`docs/` (Status: ACTIVE)** — tài liệu developer viết, tin cậy cao
3. **`.ai-memory/`** — agent tự tạo, có thể lỗi thời
4. **`docs/` (Status: TEMPLATE)** — BỎ QUA HOÀN TOÀN, đây chỉ là file mẫu

Nếu docs/ nói khác .ai-memory/ → kiểm tra code thực tế để xác định ai đúng.

## Quy tắc docs/
- File có `Status: TEMPLATE` → KHÔNG đọc, KHÔNG dùng, KHÔNG tham chiếu
- File có `Status: ACTIVE` → được phép đọc khi cần kiến thức chi tiết
- Agent KHÔNG TỰ SỬA file trong docs/ — chỉ developer sửa
- Khi cần tài liệu chi tiết: đọc `@docs/file.md` thay vì đoán

## Memory Protocol
- Khi nhận task mới: đọc `.ai-memory/04_active_plan.md` và `.ai-memory/05_active_workspace.md` TRƯỚC
- Xác định tọa độ file cần sửa từ `.ai-memory/02_project_map.md`
- Đọc deep knowledge liên quan tại `.ai-memory/03_deep_knowledge/`
- Đọc `.ai-memory/07_learnings.md` để tránh lặp sai lầm đã biết
- Nếu cần context chi tiết: đọc `.ai-memory/03_deep_knowledge/INDEX.md` trước, chỉ mở file được trỏ đến
- TUYỆT ĐỐI KHÔNG quét lại toàn bộ dự án bằng list_dir nếu memory đã có
- Sau khi sửa code: cập nhật memory tương ứng + ghi log `.ai-memory/06_evolution_log.md`

> Chi tiết đầy đủ (Decision Half-Life, Self-Improving loop, Self-Healing eval, format báo cáo, HANDOVER): đọc `@.codex/rules/memory-protocol.md`

## Drift Detection — Phát hiện thay đổi ngoài Codex
User có thể tự code, dùng AI khác, hoặc chỉnh sửa thủ công giữa các phiên Codex.
Khi đó memory sẽ LỆCH với code thực tế. BẮT BUỘC kiểm tra trước khi làm task mới:
 1. **Kiểm tra git diff**: Chạy `git diff --stat HEAD` hoặc `git log --oneline -5` để xem có commit nào Codex không biết
 2. **Nếu có thay đổi ngoài Codex** (commit lạ, file mới, file bị xóa):
   - Chạy `git diff --name-only` để list file đã thay đổi
   - Đọc CHỈ các file thay đổi (không quét toàn bộ)
   - Cập nhật memory tương ứng trong `.ai-memory/03_deep_knowledge/`
   - Đánh dấu task cũ DONE/CANCELLED trong `.ai-memory/04_active_plan.md` nếu đã hoàn thành
   - Báo user: "Phát hiện [N] file thay đổi ngoài Codex, đã đồng bộ memory"
 3. **Nếu không có thay đổi**: tiếp tục bình thường
 4. **Quy tắc vàng**: Git log là nguồn sự thật. Nếu git nói file đã đổi mà memory nói chưa → memory SAI

## Bootstrap
Nếu `.ai-memory/01_system_architecture.md` chứa `PENDING_BOOTSTRAP` hoặc section "Project Convention" trống:
 1. Quét dự án (list_dir 2 cấp), đọc build config
 2. Detect tech stack từ build config (pom.xml, package.json, go.mod...)
 3. **Đọc 2-3 file code thật** → phát hiện convention thực tế (kiến trúc phân lớp, naming, error handling, test pattern, validation, code doc)
 4. Điền vào `.ai-memory/01_system_architecture.md` gồm cả section **Project Convention**
 5. Điền `.ai-memory/02_project_map.md`
 6. Tạo file domain trong `.ai-memory/03_deep_knowledge/` theo mẫu `_TEMPLATE.md`
 7. Tạo `.ai-memory/03_deep_knowledge/INDEX.md` liệt kê tất cả file deep knowledge

## Path-scoped rules (Hybrid pattern)
Rule được tổ chức theo 2 lớp để tận dụng cả tập trung VÀ Codex native cascade:

**Lớp 1 — Source of truth (tập trung):** `.codex/rules/`
- `backend.md` — rule chi tiết cho code backend (mọi ngôn ngữ + ví dụ Java/Python/Go)
- `frontend.md` — rule chi tiết cho UI / component / a11y
- `database.md` — rule chi tiết cho entity / migration / SQL / NoSQL
- `git.md` — commit message convention
- `memory-protocol.md` — quy trình memory đầy đủ (mở rộng từ section ngắn ở root)

**Lớp 2 — Nested AGENTS.md (mỏng, load native):** đặt trong folder code thật
- VD: `src/AGENTS.md`, `frontend/AGENTS.md`, `db/AGENTS.md`
- Mỗi file chỉ 5-10 dòng: top-3 rule + trỏ `@.codex/rules/<scope>.md`
- Template copy sẵn: `docs/agents-md-examples/`

**Cách Codex áp dụng (KHÔNG cần hook custom):**
1. Codex cascade load `AGENTS.md` root + nested `AGENTS.md` trong folder cwd (native)
2. Đọc top rule ngay → áp dụng nhanh
3. Khi cần chi tiết → tự `Read @.codex/rules/<scope>.md`
4. Khi dev sửa rule chi tiết → chỉ sửa 1 file ở `.codex/rules/`, áp dụng toàn dự án

## Subagents & Routing
Khi user giao task có yêu cầu rõ về subagent/parallel agent, gọi subagent phù hợp qua `/agent` hoặc yêu cầu Codex dùng subagent cụ thể. `description` trong TOML dùng để chọn đúng role khi đã có yêu cầu dùng subagent; KHÔNG claim Codex luôn tự spawn chỉ nhờ description.
Định nghĩa 17 persona ở `.codex/agents/*.toml`:

| Task | Subagent | Reasoning tier |
|------|----------|----------------|
| Thiết kế kiến trúc, trade-off | `architect` | high |
| Phân rã task lớn, roadmap (analyst — trả output, không tự ghi) | `planner` | high |
| Audit bảo mật, OWASP | `security-auditor` | high |
| Review SÂU non-security (tầng 2) | `deep-reviewer` | high |
| Adversarial review (tầng 3) — chỉ khi user EXPLICIT nói "adversarial review", "review đối nghịch", "tìm lỗ hổng tinh vi", "kiểm tra kỹ trước deploy", "audit kỹ", "stress test code" | `adversarial-critic` | xhigh |
| Viết code mới, implement | `implementer` | medium |
| Review code tầng 1 + Confidence | `reviewer` | medium |
| Debug, phân tích root cause (analyst — đề xuất fix, không tự sửa) | `debugger` | medium |
| Viết + chạy test | `tester` | medium |
| Refactor | `refactorer` | medium |
| Database/schema/migration | `db-specialist` | medium |
| Thiết kế API/REST | `api-designer` | medium |
| Tối ưu performance, N+1 | `performance-analyst` | medium |
| Tìm file, grep | `explorer` | low |
| Viết doc, README | `doc-writer` | low |
| Sửa config, Docker, CI/CD | `config-manager` | low |
| Cập nhật memory | `memory-keeper` | low |

**Cách spawn**: gõ `/agent` rồi chọn, hoặc bảo Codex "dùng subagent reviewer review module auth".
Subagents chạy thread riêng, kết quả gộp về phản hồi chính.

### Phân loại Role (analyst vs executor) & Decision Memory routing
**ANALYST agents (`default_permissions = ":read-only"`)** — chỉ phân tích, đề xuất, KHÔNG tự ghi memory:
- `architect`, `planner`, `deep-reviewer`, `adversarial-critic`, `reviewer`, `security-auditor`, `performance-analyst`, `explorer`, `debugger`
- Khi có decision (2+ phương án cân nhắc): trả **Decision Note** trong output → orchestrator gọi `memory-keeper` để ghi vào deep_knowledge (module-specific) hoặc `01_system_architecture.md` (cross-cutting)

**EXECUTOR agents (kế thừa profile `workspace-secure` của phiên chính)** — có quyền sửa file, tự ghi memory khi cần:
- `implementer`, `tester`, `refactorer`, `db-specialist`, `api-designer`, `doc-writer`, `config-manager`, `memory-keeper`
- `db-specialist` + `api-designer`: tự ghi Decision Log vào deep_knowledge tương ứng (vì có Write)
- `memory-keeper`: trung tâm — nhận output từ analyst → ghi vào memory (đảm bảo format chuẩn 5 cột: Quyết định | Phương án | Lý do | Ngày ghi | Hết hạn)

**Quy tắc decision routing:**
1. Analyst phát hiện cần ghi memory → trả Decision Note → orchestrator → memory-keeper (BẮT BUỘC qua memory-keeper, không skip)
2. Riêng `planner`: khi trả Task Breakdown / Critical Path / Risk, orchestrator BẮT BUỘC persist vào `.ai-memory/04_active_plan.md` trước khi gọi executor. Đây là active plan, không phải Decision Note.
3. Executor (db-specialist, api-designer) phát hiện cần ghi → tự ghi vào file trực tiếp (nhanh hơn, đỡ overhead)
4. Cross-cutting decision (ảnh hưởng nhiều module) → BẮT BUỘC qua memory-keeper để ghi `01_system_architecture.md` → Architecture Decisions

## Quy tắc điều phối (9 quy tắc — orchestrator BẮT BUỘC tuân thủ)
1. **Task đơn giản** (1 agent đủ): delegate trực tiếp, nhận kết quả, báo cáo user
2. **Task phức tạp** (cần nhiều agent): gọi `planner` TRƯỚC để phân rã, rồi delegate tuần tự
   - VD: "Thêm chức năng thanh toán" → planner → memory-keeper (persist `.ai-memory/04_active_plan.md`) → db-specialist → api-designer → implementer → tester → memory-keeper
3. **Persist Planner Output (BẮT BUỘC, không rập khuôn format)**: sau mọi lần gọi `planner`, orchestrator KHÔNG được tiếp tục implement ngay. Phải nhận kế hoạch từ `planner`, gọi `memory-keeper` hoặc tự cập nhật `.ai-memory/04_active_plan.md`, rồi verify file đã có đủ ý nghĩa điều phối: mục tiêu, hướng làm, trạng thái hiện tại, dependency/risk/blocker/câu hỏi nếu có, agent/subagent dự kiến nếu cần. Không ép bảng cố định; planner được chọn checklist, phase, milestone, bảng, hoặc narrative plan tùy task. Nếu chưa persist plan → KHÔNG gọi `implementer` / `db-specialist` / `api-designer` / `tester`.
4. **Task mơ hồ** (chưa rõ cần gì): gọi `explorer` tìm context TRƯỚC, rồi quyết định agent tiếp theo
5. **Sau mỗi task hoàn thành**: gọi `memory-keeper` cập nhật `.ai-memory/` (BẮT BUỘC, không skip)
6. **KHÔNG gọi 2 agent cùng lúc** cho cùng 1 file — tuần tự để tránh xung đột ghi (Codex `agents.max_threads=6` cho phép song song, nhưng phải khác file)
7. **Ưu tiên `low` effort** cho việc nhỏ (tìm file, đọc config, viết README) — tiết kiệm token
8. **Ưu tiên `high`/`xhigh` effort** CHỈ khi cần suy nghĩ sâu (kiến trúc, bảo mật, deep review, adversarial) — chi phí cao
9. **Khi user sửa lại output** (correction): tự trigger `$reflect` **TRƯỚC khi làm tiếp** — đảm bảo learning được ghi trước khi context lỗi mất, không lặp lại sai lầm

## Luồng mẫu cho task phổ biến

**"Thêm API mới":**
```
planner → memory-keeper (persist 04_active_plan.md) → db-specialist (nếu cần entity) → api-designer → implementer → tester
  ├── test FAIL: $reflect ghi learning → implementer fix → tester (max 3 vòng)
  └── test PASS: memory-keeper
```

**"Sửa bug":**
```
explorer (tìm file liên quan) → debugger (analyst — đề xuất fix, `default_permissions = ":read-only"`)
   → implementer (thực hiện fix) → tester (verify)
  ├── test FAIL: $reflect → debugger phân tích lại → implementer fix → tester (max 3 vòng)
  └── test PASS: memory-keeper
```
> Tách rõ analyst/executor: `debugger` chỉ ĐỀ XUẤT fix (quyền chỉ đọc), `implementer` mới THỰC HIỆN fix. Đồng bộ với `debugger.toml` (`default_permissions = ":read-only"`).

**"Review module X" (Two-Tier Review):**
```
explorer (list files) → reviewer (medium, xuất Confidence Report)
    ├── ALL HIGH/MEDIUM + không critical path  → memory-keeper (DONE)
    ├── Security LOW                            → security-auditor (high)
    ├── Logic/Concurrency/Arch/Perf LOW         → deep-reviewer (high)
    ├── Critical path auth/ hoặc payment/       → BẮT BUỘC CẢ security-auditor + deep-reviewer
    │       (thứ tự: security TRƯỚC để fix flaw → reviewer review lại code đã fix → deep-reviewer SAU)
    ├── Critical path transaction/scheduler/migration/ hoặc PR > 300 dòng
    │       → BẮT BUỘC deep-reviewer (bỏ qua confidence)
    └── Sau escalation → memory-keeper
```
Quy tắc đọc Confidence Report: orchestrator đọc bảng do reviewer xuất → quyết định gọi tầng 2. Critical path + PR size là OR-trigger (kích hoạt tầng 2 dù confidence HIGH).

**"Deep review thủ công":** user nói "review sâu module X" / "deep review" → orchestrator gọi thẳng `deep-reviewer`, bỏ qua tầng 1.

**"Adversarial review" (Tầng 3):** user nói "kiểm tra kỹ", "adversarial review", "tìm lỗ hổng tinh vi", "stress test code", "audit kỹ trước deploy" → gọi `adversarial-critic` (xhigh). Mỗi finding PHẢI có test case reproduce. Verdict: SAFE TO SHIP / NEEDS HARDENING / DO NOT SHIP.

**"Refactor code":**
```
reviewer (đánh giá hiện trạng) → refactorer (thực hiện) → tester (verify không break)
  ├── test FAIL: $reflect → refactorer fix → tester (max 3 vòng)
  └── test PASS: memory-keeper
```

**"Bug production"** (user báo bug đã deploy / user thật gặp):
→ CHẠY skill `$production-feedback` (KHÔNG chỉ `$debug-flow`)
→ Xác nhận bug do AI gây ra → tìm commit gây ra → so với memory tại thời điểm đó → ghi learning loại A/B/C
→ Đề xuất sửa rule / deep_knowledge / checklist tương ứng

## Memory Health (định kỳ)
- Khi `06_evolution_log.md` > 50KB hoặc mỗi tháng 1 lần: ĐỀ XUẤT user chạy `$compact-memory` (không tự chạy)
- Decision Half-Life: mọi decision có "Hết hạn" (+3 tháng module / +6 tháng cross-cutting). Quá hạn → KHÔNG tự áp dụng, hỏi user re-evaluate
- Token usage / chi phí: user có thể gõ `$agent-roi` để xem agent nào tốn token, agent nào ít dùng → tinh chỉnh routing

## Self-improving / Self-healing
- User sửa output / nói "sai rồi" / test fail 2+ vòng → chạy `$reflect` **TRƯỚC khi làm tiếp** (đảm bảo learning được ghi trước khi context lỗi mất, không lặp lại sai lầm)
- Sau `implementer`/`refactorer` → chạy `$self-eval` (compile + test + convention check)
- Eval fail → `debugger` fix → eval lại (max 3 vòng); nếu vòng 3 vẫn fail → ghi BLOCKED vào `05_active_workspace.md` + báo user
- Pattern lặp 3+ lần trong `07_learnings.md` → đề xuất user chạy `$promote-learning`
- Bug production → chạy `$production-feedback` (KHÔNG chỉ `$debug-flow`)
- Hook `post-edit-detect-rework` tự ghi alert vào `.codex/rework-alerts.log` khi file edit > 3 lần / 30 phút — đọc alert đầu phiên để `$reflect` ngầm

## Three-tier review
- **Tầng 1**: `reviewer` xuất Confidence Report cho mọi PR
- **Tầng 2**: escalate `security-auditor` (Security LOW) hoặc `deep-reviewer` (non-security LOW, hoặc PR > 300 dòng, hoặc critical path: auth/payment/transaction/scheduler/migration)
- **Tầng 3**: `adversarial-critic` — CHỈ khi user nói rõ "adversarial review", "tìm lỗ hổng tinh vi", "kiểm tra kỹ trước deploy"

## Skills (14) — invoke `$<name>` hoặc `/skills`
`bootstrap-memory` · `sync-memory` · `detect-drift` · `rollback` · `reflect` · `promote-learning` ·
`self-eval` · `agent-roi` · `compact-memory` · `production-feedback` · `debug-flow` · `review-module` ·
`create-api` · `create-entity`

## Hooks (đã cấu hình trong `.codex/hooks.json`)
- **PreToolUse Bash**: chặn pattern nguy hiểm (`rm -rf /`, `git reset --hard`, `drop table`...)
- **PreToolUse apply_patch/Edit/Write**: bảo vệ path có tag `# sensitive` hoặc `# protected-edit` trong `.codexignore`
- **PermissionRequest Bash/apply_patch**: chặn approval request nguy hiểm/protected trước khi prompt approve hiện ra
- **PostToolUse apply_patch/Edit/Write**: log mọi edit + auto-typecheck file code (multi-language)
- **PostToolUse apply_patch/Edit/Write**: phát hiện rework (>3 edit/30min) → ghi `.codex/rework-alerts.log`
- **UserPromptSubmit**: quét prompt user để chặn secret/credential trước khi vào history

## Build & Run
```bash
# [Agent tự điền khi bootstrap]
```
