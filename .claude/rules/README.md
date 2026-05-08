# .claude/rules/ — Quy tắc coding theo path-scoping
 
## Mục đích
Mỗi file rule chỉ load khi agent sửa file matching path trong YAML frontmatter.
Tiết kiệm token so với nhồi tất cả quy tắc vào CLAUDE.md.
 
## Cấu trúc
```
.claude/rules/
├── README.md              ← File này
├── backend.md              ← Load khi sửa code backend (mọi ngôn ngữ)
├── frontend.md            ← Chỉ load khi sửa frontend/**
├── database.md            ← Chỉ load khi sửa entity/repository/db/
├── git.md                 ← Load mọi phiên (không path-scope, ngắn)
└── memory-protocol.md     ← Load mọi phiên (quy trình memory + learning + eval + drift detection + decision memory )
```
 
## Chi tiết từng rule
 
| File | Load khi sửa | Nội dung |
|------|-------------|----------|
| `backend.md`| `src/**/*.java`, `**/*.py`, `**/*.go`, `**/*.ts`, `**/*.rs`, `**/*.cs`... | Quy tắc phân lớp chung, trỏ đến "Project Convention" trong memory |
| `frontend.md`| `frontend/**`, `templates/**`, `static/**` | Component naming, hook convention, CSS class, XSS prevention |
| `database.md`| `**/entity/**`, `**/repository/**`, `db/**` | Table/column naming, FK convention, mỗi Entity 1 Repository |
| `git.md` | Mọi phiên (không path-scope) | Commit message format: [type]: mô tả |
| `memory-protocol.md` | Mọi phiên (không path-scope) | Quy trình đọc/cập nhật memory, drift detection, learning, self-heal, handover, decision memory |
 
## Cách hoạt động
 
- File CÓ `paths:` trong YAML frontmatter → chỉ load khi agent làm việc với file matching
- File KHÔNG CÓ `paths:` → load mỗi phiên (giống CLAUDE.md)
- Nhiều rule có thể load cùng lúc nếu agent sửa nhiều loại file

## Tùy chỉnh
 
- Sửa path glob cho khớp cấu trúc dự án thực tế
- Xóa rule không cần (VD: xóa `frontend.md` nếu chỉ làm backend)
- Thêm rule mới: tạo file `.md` với YAML frontmatter có `paths:`
- Giữ mỗi file rule ngắn gọn (<30 dòng) để tối ưu token
- `backend.md` đã match mọi ngôn ngữ — convention cụ thể nằm trong `01_system_architecture.md` (bootstrap tự điền)
 
**Giữ nguyên cấu trúc**: YAML frontmatter có `paths:` + nội dung convention ngắn gọn (<30 dòng).
Các file `git.md`, `memory-protocol.md`, `database.md`, `frontend.md` dùng cho MỌI dự án.