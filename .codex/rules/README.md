# .codex/rules/ — Quy tắc coding chi tiết, tập trung

## Mục đích
Mirror folder `.claude/rules/` của bộ Claude, nhưng bỏ YAML frontmatter `paths:` vì Codex không dùng format đó trong rule file. Các file trong `.codex/rules/` là **source of truth chi tiết** cho rule chung theo scope (`backend`, `frontend`, `database`, `git`, `memory-protocol`). File có thể dài nếu cần giữ đủ logic đa nền tảng.

> Triết lý: rule chung/đa nền tảng nằm tập trung ở đây; convention thật của dự án vẫn ưu tiên section "Project Convention" trong `.ai-memory/01_system_architecture.md` (do `$bootstrap-memory` detect và điền). Nested `AGENTS.md` chỉ là file mỏng để Codex auto-load top rules và trỏ về source of truth này.

## Cấu trúc
```
.codex/rules/
├── README.md              ← File này
├── backend.md             ← Quy tắc chung backend đa ngôn ngữ
├── frontend.md            ← Component/UI/mobile/a11y/test đa nền tảng
├── database.md            ← SQL/NoSQL/mobile DB/cache/vector/migration/security
├── git.md                 ← Convention commit message
└── memory-protocol.md     ← Quy trình memory + drift + learning + handover
```

## Khác biệt với bộ Claude — vì sao Codex KHÔNG có YAML frontmatter `paths:`

Bộ Claude rule có YAML frontmatter `paths:` để Claude tự load rule theo file đang sửa (native path-scoping). **Codex CLI 2026 chưa hỗ trợ cơ chế này** — rule files không tự load theo path.

Codex thay thế bằng **nested AGENTS.md cascade native** (verify [Codex docs](https://developers.openai.com/codex/guides/agents-md)):
- Tạo `AGENTS.md` mỏng (5-10 dòng) ngay trong folder code (vd: `src/AGENTS.md`, `frontend/AGENTS.md`)
- Mỗi nested AGENTS.md có top-3 rule + trỏ tới file chi tiết: `> Chi tiết: đọc @.codex/rules/backend.md`
- Codex tự cascade load `AGENTS.md` theo git-root → cwd, không cần hook custom
- Template có sẵn: `docs/agents-md-examples/`

## Khi nào cập nhật file rule

| Bạn vừa | Sửa file nào? |
|---------|--------------|
| Đổi cách viết controller / repository / handler | `backend.md` |
| Đổi naming convention table / column | `database.md` |
| Đổi component / hook pattern | `frontend.md` |
| Đổi format commit message | `git.md` |
| Đổi quy trình memory | `memory-protocol.md` |

Sau khi sửa rule, **chỉ cập nhật nested AGENTS.md nếu top-3 priority thay đổi** — còn lại Codex tự đọc file rule khi nested AGENTS.md trỏ tới.

## Tùy chỉnh khi áp vào dự án mới (Bước-by-Bước)

1. Mở từng file `backend.md` / `frontend.md` / `database.md`
2. XÓA phần "ví dụ: Java ..." mặc định
3. Điền lại theo ngôn ngữ / framework thật của dự án (Python/Go/Rust/TS...)
4. Convention cụ thể (vd: error response format, ORM, framework) → để `$bootstrap-memory` tự detect và điền vào `01_system_architecture.md`, KHÔNG nhồi vào file rule này
5. Tạo nested AGENTS.md ở folder code thật (copy từ `docs/agents-md-examples/`)

## Quy tắc giữ nested AGENTS.md mỏng
- File cần ngắn là **nested `AGENTS.md`** trong folder code thật: chỉ 5-10 dòng, gồm top-3 rule + dòng trỏ tới `.codex/rules/<scope>.md`
- File `.codex/rules/*.md` được phép dài hơn 30 dòng nếu cần giữ đủ logic đã kiểm nghiệm từ Claude
- Rule cụ thể từng dự án → để memory `01_system_architecture.md`, KHÔNG nhồi vào `.codex/rules/`
- Sau khi sửa rule chi tiết, chỉ cập nhật nested `AGENTS.md` nếu top-3 priority thay đổi
