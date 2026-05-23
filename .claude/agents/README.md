# .claude/agents/ — Đội agent chuyên biệt (multi-model)
 
## Mục đích
Mỗi agent là 1 chuyên gia với model riêng, tools riêng, chạy trong context window riêng.
Phiên chính (orchestrator) đọc CLAUDE.md → quyết định delegate cho agent nào.
 
## Cấu trúc
```
.claude/agents/
├── README.md                      ← File này
│
│   ══ Opus 4.6 — Suy nghĩ sâu ══
├── 01-architect.md                ← Thiết kế kiến trúc, chọn pattern
├── 10-security-auditor.md         ← Audit bảo mật, OWASP compliance
├── 15-planner.md                  ← Phân rã task lớn, lập kế hoạch
├── 16-deep-reviewer.md            ← Review SÂU non-security (tầng 2, escalation)
├── 17-adversarial-critic.md       ← Tìm lỗ hổng ngầm (offensive thinking)
│
│   ══ Sonnet 4.6 — Code + Phân tích ══
├── 02-implementer.md              ← Viết code mới, implement feature
├── 04-reviewer.md                 ← Review tầng 1 + Confidence Report
├── 05-debugger.md                 ← Phân tích stack trace, fix bug
├── 06-tester.md                   ← Viết + chạy test
├── 07-refactorer.md               ← Tách method, giảm duplication
├── 08-db-specialist.md            ← Thiết kế schema, migration, query
├── 09-api-designer.md             ← Thiết kế REST API, DTO convention
├── 11-performance-analyst.md      ← Tìm bottleneck, N+1, caching
│
│   ══ Haiku 4.5 — Nhanh + Rẻ ══
├── 03-explorer.md                 ← Tìm file, grep, "file nào chứa X"
├── 12-doc-writer.md               ← Viết README, javadoc, changelog
├── 13-config-manager.md           ← Sửa config, Docker, CI/CD
└── 14-memory-keeper.md            ← Đồng bộ .ai-memory/ với code
```
 
## Phân bổ model
| Model | Agents | Khi nào dùng | Chi phí |
|-------|--------|-------------|---------|
| Opus 4.6 (5) | architect, security-auditor, planner, deep-reviewer, adversarial-critic | Cần suy nghĩ sâu, escalation, offensive thinking | $$$$ |
| Sonnet 4.6 (8) | implementer, reviewer, debugger, tester, refactorer, db-specialist, api-designer, performance-analyst | Code + phân tích hàng ngày | $$ |
| Haiku 4.5 (4) | explorer, doc-writer, config-manager, memory-keeper | Việc nhanh, đơn giản | $ |

## Two-Tier Review Pattern
**Tầng 1 — `reviewer` (Sonnet)**: chạy cho MỌI PR. Xuất Confidence Report (HIGH/MEDIUM/LOW) cho từng lĩnh vực.

**Tầng 2 — Escalation (Opus)**: orchestrator đọc Confidence Report → chỉ gọi tầng 2 khi cần:
- Security LOW → `security-auditor`
- Logic / Concurrency / Architecture / Performance LOW → `deep-reviewer`
- Vấn đề kiến trúc cross-cutting → `architect`

**Trigger BẮT BUỘC tầng 2 (bỏ qua Confidence)**:
1. Critical path `auth/` hoặc `payment/` → BẮT BUỘC CẢ `security-auditor` + `deep-reviewer` (chạy tuần tự, security TRƯỚC để fix flaw trước khi review logic)
2. Critical path `transaction/`, `scheduler/`, `migration/` → BẮT BUỘC `deep-reviewer`. security-auditor chỉ nếu Security < HIGH
3. PR > 300 dòng thay đổi → BẮT BUỘC `deep-reviewer`
4. User yêu cầu "review sâu" / "deep review" thủ công → `deep-reviewer` (không có slash command — natural language trigger)

→ Kết quả: ~85% PR chỉ tốn Sonnet, chỉ ~15% PR tốn thêm Opus. Không miss bug phức tạp non-security.

## Adversarial Critic (Tầng 3 — Offensive Review)
**Khác biệt 3 tầng review:**
| Tầng | Agent | Cách nghĩ |
|------|-------|-----------|
| 1 | `reviewer` (Sonnet) | Defensive — check theo checklist |
| 2 | `deep-reviewer` / `security-auditor` (Opus) | Analytical — tìm edge case lọt |
| 3 | `adversarial-critic` (Opus) | **Offensive** — GIẢ ĐỊNH SAI, tìm test case break |

**Khi gọi adversarial-critic (CHỈ khi user EXPLICITLY yêu cầu):**
- Cụm từ trực tiếp: "adversarial review", "review đối nghịch", "tìm lỗ hổng tinh vi", "kiểm tra kỹ trước deploy", "audit kỹ", "stress test code"

**KHÔNG TỰ ĐỘNG khi user chỉ nói "deploy", "production", "review", "kiểm tra"** — đó là việc của reviewer/deep-reviewer/review-module. Chi phí Opus cao → chỉ chạy khi user MUỐN ý kiến thứ 3.

**Anti-fabrication:** Mỗi finding PHẢI có test case reproduce cụ thể. Không có test case = không phải finding.
 
## Decision Memory
- architect, planner, deep-reviewer (không có Write): trả decision trong output → orchestrator → memory-keeper ghi
- db-specialist, api-designer (có Write): tự ghi vào bảng Decision trong deep knowledge
- memory-keeper: nhận decision từ agent khác → ghi vào deep knowledge hoặc Architecture Decisions

## Cách hoạt động
1. User giao task → phiên chính đọc bảng routing trong CLAUDE.md
2. Phiên chính delegate cho agent phù hợp
3. Agent chạy trong context window riêng → không ô nhiễm context chính
4. Agent trả kết quả → phiên chính tổng hợp báo cáo user

## Luồng mẫu
- **"Thêm API mới"**: planner → db-specialist → api-designer → implementer → tester → memory-keeper
- **"Sửa bug"**: explorer → debugger → tester → memory-keeper
- **"Review module X"** (two-tier):
  ```
  explorer → reviewer (Sonnet, xuất Confidence Report)
         ├── ALL HIGH/MEDIUM, không critical path → memory-keeper (DONE)
         ├── Security LOW       → security-auditor (Opus)
         ├── Non-security LOW   → deep-reviewer (Opus)
         ├── Critical path/PR>300 dòng → deep-reviewer (Opus, bắt buộc)
         └── (sau escalation) → memory-keeper
  ```
- **"Refactor code"**: reviewer → refactorer → tester → memory-keeper
- **"Deep review thủ công"**: user nói "review sâu module X" / "deep review" → orchestrator nhận diện → gọi thẳng deep-reviewer (bỏ qua tầng 1)

## Giới hạn
- Subagent KHÔNG spawn subagent (giới hạn Claude Code)
- Phiên chính là orchestrator duy nhất
- Model version ghim qua `ANTHROPIC_DEFAULT_*_MODEL` trong settings.json
- VS Code gạch vàng dòng `model:` nếu dùng full model ID → bỏ qua, runtime vẫn đúng

## Tùy chỉnh
- Xóa agent không cần (VD: xóa frontend agents nếu chỉ làm backend)
- Thêm agent mới: tạo file `.md` với YAML frontmatter (name, description, model, tools)
- Sửa `description` để thay đổi điều kiện delegate
- Tối ưu: 12-18 agents là vùng hiệu quả nhất, >20 agents tăng overhead routing