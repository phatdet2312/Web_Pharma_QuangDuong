# .claude/skills/ — Workflow tái sử dụng (on-demand)
 
## Mục đích
Skills là workflow có hệ thống, CHỈ load khi được gọi hoặc auto-detect.
Không tốn token khi không dùng — khác với rules (load theo path) và CLAUDE.md (load mọi phiên).
 
## Cấu trúc
```
.claude/skills/
├── README.md                      ← File này
├── create-api/SKILL.md            ← /create-api — tạo REST endpoint đầy đủ
├── create-entity/SKILL.md         ← /create-entity — tạo Entity + Repo + Migration
├── debug-flow/SKILL.md            ← /debug-flow — debug có hệ thống, không đoán mò
├── review-module/SKILL.md         ← /review-module — review toàn diện (security→logic→perf)
├── bootstrap-memory/SKILL.md      ← /bootstrap-memory — khởi tạo memory bank lần đầu
├── sync-memory/SKILL.md           ← /sync-memory — đồng bộ memory với code thực tế
├── detect-drift/SKILL.md          ← /detect-drift — phát hiện thay đổi ngoài Claude
├── rollback/SKILL.md              ← /rollback — rollback nguyên tử code + memory
├── reflect/SKILL.md               ← /reflect — ghi learning sau sai lầm
├── promote-learning/SKILL.md      ← /promote-learning — đưa learning lên rules
└── self-eval/SKILL.md             ← /self-eval — eval loop tự compile/test/check
```

## Chi tiết từng skill
 
| Skill | Slash command | Trigger tự động khi |
|-------|--------------|---------------------|
| `create-api/` | /create-api | User nhắc "tạo API", "thêm endpoint", "viết REST" |
| `create-entity/` | /create-entity | User nhắc "tạo bảng", "thêm entity", "model mới" |
| `debug-flow/` | /debug-flow | User nhắc "lỗi", "bug", "exception", "fix", stack trace |
| `review-module/` | /review-module | User nhắc "review", "kiểm tra", "code quality" |
| `bootstrap-memory/` | /bootstrap-memory | Phát hiện `PENDING_BOOTSTRAP` trong memory |
| `sync-memory/` | /sync-memory | User nhắc "cập nhật memory", "đồng bộ" |
| `detect-drift/` | /detect-drift | User nhắc "tôi đã sửa", "code tay", "dùng AI khác" |
| `rollback/` | /rollback | User nhắc "hoàn tác", "undo", "rollback", "revert", "bỏ thay đổi" |
| `reflect/` | /reflect | User sửa output, nói "sai rồi", test fail 2+ vòng |
| `promote-learning/` | /promote-learning | Có 5+ entries Lần>=3, hoặc mỗi 10 phiên |
| `self-eval/` | /self-eval | Sau implementer/refactorer hoàn thành task |
 
## Cách hoạt động
 
1. **Auto-detect**: Agent đọc `description` trong SKILL.md → nếu khớp yêu cầu user → tự load
2. **Thủ công**: User gõ `/tên-skill` trong chat
3. **Từ agent khác**: Agent orchestrator có thể trigger skill cho subagent
## So sánh Skills vs Rules vs CLAUDE.md
 
| Cơ chế | Load khi nào | Tốn token khi không dùng? | Ai quản lý |
|--------|-------------|---------------------------|------------|
| CLAUDE.md | Mọi phiên | Có (luôn load) | Developer |
| Rules | Khi sửa file matching path | Có (khi match) | Developer |
| Skills | Khi invoke/auto-detect | KHÔNG | Developer |
 
## Tùy chỉnh
 
- Thêm skill mới: tạo thư mục + `SKILL.md` trong `.claude/skills/`
- Sửa `description` để thay đổi điều kiện auto-trigger
- Mỗi SKILL.md nên có: mục đích, các bước, quy tắc, format báo cáo

## Lưu ý tech stack
`create-api/` và `create-entity/` chứa ví dụ mẫu cho **Java/Spring Boot**.
Agent sẽ tự điều chỉnh theo convention trong `01_system_architecture.md` (bootstrap tự điền).
Nếu muốn tùy chỉnh sâu hơn, sửa nội dung bên trong SKILL.md cho khớp stack thực tế. 
`self-eval/` đã liệt kê build/test command cho nhiều ngôn ngữ — agent chọn đúng lệnh từ memory.
Các skill khác (reflect, rollback, sync-memory, detect-drift, promote-learning, bootstrap-memory, review-module, debug-flow) dùng cho **MỌI dự án**.