# AI Memory Kit v2 — Đại trùng tu
 
## Khác biệt so với v1
 
### v1 (cũ): Tự quản lý hoàn toàn
```
.ai-memory/
├── .clinerules          ← Agent phải tự nhớ đọc
├── .clinesignore        ← Không phải format native
└── 00_core_rules.xml    ← Load toàn bộ mỗi phiên
```
 
### v2 (mới): Tích hợp hệ sinh thái Claude Code
```
project-root/
├── CLAUDE.md              ← Claude Code TỰ ĐỘNG đọc mỗi phiên
├── .claudeignore          ← Format native, Claude Code nhận diện
├── .claude/
│   └── rules/             ← Modular rules, path-scoped
│       ├── java-backend.md    (chỉ load khi sửa .java)
│       ├── frontend.md        (chỉ load khi sửa frontend)
│       ├── database.md        (chỉ load khi sửa entity/repo)
│       ├── git.md             (load mọi phiên, ngắn)
│       └── memory-protocol.md (load mọi phiên, hướng dẫn agent)
└── .ai-memory/            ← Memory bank giữ nguyên cấu trúc
    ├── 01_system_architecture.md
    ├── 02_project_map.md
    ├── 03_deep_knowledge/
    │   └── _TEMPLATE.md
    ├── 04_active_plan.md
    ├── 05_active_workspace.md
    └── 06_evolution_log.md
```
 
## 5 cải tiến chính
 
1. CLAUDE.md ở root → Claude Code tự đọc, không cần agent nhớ
2. .claudeignore native → thay .clinesignore, Claude Code nhận diện
3. Path-scoped rules → Java rules chỉ load khi sửa .java (tiết kiệm ~40% token)
4. Tách 00_core_rules.xml → 4 file rules nhỏ theo domain
5. Giữ nguyên .ai-memory/ → tương thích ngược, không mất data
## Cách dùng
 
### Bước 1: Copy vào dự án
Copy toàn bộ: CLAUDE.md, .claudeignore, .claude/, .ai-memory/
 
### Bước 2: Tùy chỉnh
- Sửa CLAUDE.md: thêm build command, quy tắc riêng
- Sửa .claude/rules/java-backend.md: đổi nếu dùng ngôn ngữ khác
- Xóa rules không cần (VD: xóa frontend.md nếu chỉ có backend)
### Bước 3: Bootstrap
Mở Claude Code, gõ: "Khởi tạo memory bank cho dự án này"
 
### Bước 4: Làm việc hàng ngày
Giao task bình thường. Agent tự đọc CLAUDE.md + memory + rules phù hợp.
 
## Ước tính token tiết kiệm thêm so với v1
 
| Kịch bản             | v1        | v2        | Tiết kiệm |
|-----------------------|-----------|-----------|------------|
| Sửa 1 file Java      | ~12k tok  | ~8k tok   | ~33%       |
| Sửa 1 file frontend  | ~12k tok  | ~7k tok   | ~42%       |
| Task liên quan DB     | ~12k tok  | ~7k tok   | ~42%       |
| So với không dùng gì  | ~45k tok  | ~8k tok   | ~82%       |