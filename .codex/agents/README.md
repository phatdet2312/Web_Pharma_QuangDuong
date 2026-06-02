# .codex/agents/ — Đội subagent chuyên biệt (Codex 0.133.x)

## Mục đích
Mỗi subagent là 1 chuyên gia với `model_reasoning_effort` riêng, quyền đọc/ghi riêng qua `default_permissions` hoặc kế thừa parent, chạy trong thread riêng (context window cách ly).
Orchestrator (phiên chính) đọc AGENTS.md → quyết định delegate qua `/agent <name>` cho subagent nào.

## Cấu trúc
```
.codex/agents/
├── README.md                      ← File này
│
│   ══ high effort — Suy nghĩ sâu ══
├── architect.toml                ← Thiết kế kiến trúc, chọn pattern
├── security-auditor.toml         ← Audit bảo mật, OWASP compliance
├── planner.toml                  ← Phân rã task lớn, lập kế hoạch
├── deep-reviewer.toml            ← Review SÂU non-security (tầng 2, escalation)
├── adversarial-critic.toml       ← Tìm lỗ hổng ngầm (offensive thinking, xhigh)
│
│   ══ medium effort — Code + Phân tích ══
├── implementer.toml              ← Viết code mới, implement feature
├── reviewer.toml                 ← Review tầng 1 + Confidence Report
├── debugger.toml                 ← Phân tích stack trace, đề xuất fix (analyst role)
├── tester.toml                   ← Viết + chạy test + Self-Eval mode
├── refactorer.toml               ← Tách method, giảm duplication
├── db-specialist.toml            ← Thiết kế schema, migration, query
├── api-designer.toml             ← Thiết kế REST API, DTO convention
├── performance-analyst.toml      ← Tìm bottleneck, N+1, caching
│
│   ══ low effort — Nhanh + Rẻ ══
├── explorer.toml                 ← Tìm file, grep, "file nào chứa X"
├── doc-writer.toml               ← Viết README, javadoc, changelog
├── config-manager.toml           ← Sửa config, Docker, CI/CD
└── memory-keeper.toml            ← Đồng bộ `.ai-memory/` với code
```

## Phân bổ reasoning effort (Codex 0.133.x dùng 1 model ghim ở `.codex/config.toml` + `model_reasoning_effort` per agent thay 3-model của Claude)
| Effort | Subagents | Khi nào dùng | Chi phí |
|--------|-----------|-------------|---------|
| high (5) | architect, security-auditor, planner, deep-reviewer, adversarial-critic (xhigh) | Cần suy nghĩ sâu, escalation, offensive thinking | $$$$ |
| medium (8) | implementer, reviewer, debugger, tester, refactorer, db-specialist, api-designer, performance-analyst | Code + phân tích hàng ngày | $$ |
| low (4) | explorer, doc-writer, config-manager, memory-keeper | Việc nhanh, đơn giản | $ |

## Two-Tier Review Pattern
**Tầng 1 — `reviewer` (medium effort, `default_permissions = ":read-only"`)**: chạy cho MỌI PR. Xuất Confidence Report (HIGH/MEDIUM/LOW) cho từng lĩnh vực.

**Tầng 2 — Escalation (high effort)**: orchestrator đọc Confidence Report → chỉ gọi tầng 2 khi cần:
- Security LOW → `security-auditor`
- Logic / Concurrency / Architecture / Performance LOW → `deep-reviewer`
- Vấn đề kiến trúc cross-cutting → `architect`

**Trigger BẮT BUỘC tầng 2 (bỏ qua Confidence)**:
1. Critical path `auth/` hoặc `payment/` → BẮT BUỘC CẢ `security-auditor` + `deep-reviewer` (chạy tuần tự, security TRƯỚC để fix flaw trước khi review logic)
2. Critical path `transaction/`, `scheduler/`, `migration/` → BẮT BUỘC `deep-reviewer`. security-auditor chỉ nếu Security < HIGH
3. PR > 300 dòng thay đổi → BẮT BUỘC `deep-reviewer`
4. User yêu cầu "review sâu" / "deep review" thủ công → `deep-reviewer` (natural language trigger qua description)

→ Kết quả: ~85% PR chỉ tốn medium, chỉ ~15% PR tốn thêm high. Không miss bug phức tạp non-security.

## Adversarial Critic (Tầng 3 — Offensive Review)
**Khác biệt 3 tầng review:**
| Tầng | Subagent | Cách nghĩ |
|------|----------|-----------|
| 1 | `reviewer` (medium) | Defensive — check theo checklist |
| 2 | `deep-reviewer` / `security-auditor` (high) | Analytical — tìm edge case lọt |
| 3 | `adversarial-critic` (xhigh) | **Offensive** — GIẢ ĐỊNH SAI, tìm test case break |

**Khi gọi adversarial-critic (CHỈ khi user EXPLICITLY yêu cầu):**
- Cụm từ trực tiếp: "adversarial review", "review đối nghịch", "tìm lỗ hổng tinh vi", "kiểm tra kỹ trước deploy", "audit kỹ", "stress test code"

**KHÔNG TỰ ĐỘNG khi user chỉ nói "deploy", "production", "review", "kiểm tra"** — đó là việc của reviewer/deep-reviewer/review-module. Chi phí xhigh cao → chỉ chạy khi user MUỐN ý kiến thứ 3.

**Anti-fabrication:** Mỗi finding PHẢI có test case reproduce cụ thể. Không có test case = không phải finding.

## Decision Memory (Role analyst vs executor)
**ANALYST agents (`default_permissions = ":read-only"`)** — trả decision trong output, KHÔNG tự ghi:
- architect, planner, deep-reviewer, adversarial-critic, reviewer, security-auditor, performance-analyst, explorer, debugger
- Khi có decision → orchestrator → memory-keeper ghi vào deep knowledge / Architecture Decisions
- Riêng planner: Task Breakdown / Active Plan Update phải được memory-keeper persist + đọc lại verify `.ai-memory/04_active_plan.md` trước khi gọi executor

**EXECUTOR agents (kế thừa profile `workspace-secure` của phiên chính)** — có quyền sửa file:
- implementer, tester, refactorer, db-specialist, api-designer, doc-writer, config-manager, memory-keeper
- `db-specialist`, `api-designer`: tự ghi Decision Log vào deep knowledge (có Write)
- `memory-keeper`: trung tâm — nhận decision từ analyst → ghi với format chuẩn 5 cột; nhận Task Breakdown từ planner → ghi `04_active_plan.md`

## Cách hoạt động
1. User giao task → orchestrator đọc bảng routing trong AGENTS.md
2. Orchestrator delegate qua `/agent <name>` cho subagent phù hợp
3. Subagent chạy trong thread riêng → không ô nhiễm context chính
4. Subagent trả kết quả → orchestrator tổng hợp báo cáo user
5. Nếu subagent là `planner` → gọi memory-keeper foreground → chỉ gọi executor sau `ACTIVE_PLAN_PERSISTED_AND_VERIFIED`

## Luồng mẫu
- **"Thêm API mới"**: planner → memory-keeper → `ACTIVE_PLAN_PERSISTED_AND_VERIFIED` → db-specialist → api-designer → implementer → tester → memory-keeper
- **"Sửa bug"**: explorer → debugger → implementer (thực hiện fix) → tester → memory-keeper
- **"Review module X"** (two-tier):
  ```
  explorer → reviewer (medium, xuất Confidence Report)
         ├── ALL HIGH/MEDIUM, không critical path → memory-keeper (DONE)
         ├── Security LOW       → security-auditor (high)
         ├── Non-security LOW   → deep-reviewer (high)
         ├── Critical path/PR>300 dòng → deep-reviewer (high, bắt buộc)
         └── (sau escalation) → memory-keeper
  ```
- **"Refactor code"**: reviewer → refactorer → tester → memory-keeper
- **"Deep review thủ công"**: user nói "review sâu module X" / "deep review" → orchestrator nhận diện → gọi thẳng deep-reviewer (bỏ qua tầng 1)

## Giới hạn
- Codex 0.133.x hỗ trợ `agents.max_threads = 6`; bộ này đặt `max_depth = 1` để phiên chính spawn subagent trực tiếp và tránh spawn lồng nhau ngoài ý muốn
- Orchestrator là phiên chính
- Model version ghim ở `.codex/config.toml` (field `model`). Override per-agent qua field `model` trong TOML
- Codex 0.133.x có `SubagentStart` và `SubagentStop`; telemetry per-subagent được ghi vào `.codex/subagent-metrics.csv`.

## Tùy chỉnh
- Xóa subagent không cần (VD: xóa frontend agents nếu chỉ làm backend)
- Thêm subagent mới: tạo file `.toml` với fields `name`, `description`, `developer_instructions`, optional `model`, `model_reasoning_effort`, `default_permissions`, `mcp_servers`, `nickname_candidates`
- Sửa `description` để giúp user/orchestrator chọn đúng role khi đã yêu cầu dùng subagent; không claim Codex tự spawn chỉ nhờ description
- Tối ưu: 12-18 subagents là vùng hiệu quả nhất, >20 tăng overhead routing
