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
│
│   ══ Sonnet 4.6 — Code + Phân tích ══
├── 02-implementer.md              ← Viết code mới, implement feature
├── 04-reviewer.md                 ← Review code quality, tìm code smell
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
| Opus 4.6 (3) | architect, security-auditor, planner | Cần suy nghĩ sâu, quyết định kiến trúc | $$$$ |
| Sonnet 4.6 (8) | implementer, reviewer, debugger, tester, refactorer, db-specialist, api-designer, performance-analyst | Code + phân tích hàng ngày | $$ |
| Haiku 4.5 (4) | explorer, doc-writer, config-manager, memory-keeper | Việc nhanh, đơn giản | $ |
 
## Decision Memory
- architect, planner (không có Write): trả decision trong output → orchestrator → memory-keeper ghi
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
- **"Review module X"**: explorer → reviewer → security-auditor → memory-keeper
- **"Refactor code"**: reviewer → refactorer → tester → memory-keeper

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