# AI Agent System — Full Kit (Tầng 1 + 2 + 3)
 
## Kiến trúc hoàn chỉnh
 
```
project-root/
│
├── CLAUDE.md                              # Auto-load mỗi phiên
├── .claudeignore                          # Filter token
│
├── .ai-memory/                            # ═══ TẦNG 1: MEMORY BANK ═══
│   ├── 01_system_architecture.md             Kiến trúc hệ thống
│   ├── 02_project_map.md                     Bản đồ dự án + tọa độ file
│   ├── 03_deep_knowledge/                    Tri thức domain chi tiết
│   │   └── _TEMPLATE.md                      Mẫu format cho agent
│   ├── 04_active_plan.md                     Kế hoạch task đang làm
│   ├── 05_active_workspace.md                Bug, blocker, context hiện tại
│   └── 06_evolution_log.md                   Nhật ký thay đổi
│
├── docs/                                  # ═══ TÀI LIỆU ON-DEMAND ═══
│   ├── README.md                             Hướng dẫn cách dùng docs/
│   ├── api-spec.md                           Đặc tả API chi tiết
│   ├── database-schema.md                    Schema database đầy đủ
│   └── coding-guidelines.md                  Quy tắc coding mở rộng
│
└── .claude/
    ├── settings.json                      # Cấu hình hooks
    │
    ├── rules/                             # ═══ TẦNG 1: PATH-SCOPED RULES ═══
    │   ├── java-backend.md                   Chỉ load khi sửa .java
    │   ├── frontend.md                       Chỉ load khi sửa frontend
    │   ├── database.md                       Chỉ load khi sửa entity/repo
    │   ├── git.md                            Load mọi phiên (ngắn)
    │   └── memory-protocol.md                Load mọi phiên
    │
    ├── skills/                            # ═══ TẦNG 2: ON-DEMAND SKILLS ═══
    │   ├── create-api/SKILL.md               /create-api — tạo REST endpoint
    │   ├── create-entity/SKILL.md            /create-entity — tạo Entity+Repo
    │   ├── debug-flow/SKILL.md               /debug-flow — debug có hệ thống
    │   ├── review-module/SKILL.md            /review-module — review toàn diện
    │   ├── bootstrap-memory/SKILL.md         /bootstrap-memory — khởi tạo memory
    │   └── sync-memory/SKILL.md              /sync-memory — đồng bộ memory
    │
    ├── hooks/                             # ═══ TẦNG 2: LIFECYCLE HOOKS ═══
    │   ├── pre-bash-firewall.sh              Chặn rm -rf, git reset --hard...
    │   ├── pre-edit-protect.sh               Bảo vệ .env, Dockerfile...
    │   ├── post-edit-log.sh                  Log mọi file đã sửa
    │   └── post-edit-typecheck.sh            Auto compile/lint sau mỗi edit
    │
    └── agents/                            # ═══ TẦNG 3: MULTI-MODEL AGENTS ═══
        ├── 01-architect.md                   Opus  — thiết kế kiến trúc
        ├── 02-implementer.md                 Sonnet — viết code
        ├── 03-explorer.md                    Haiku  — tìm kiếm nhanh
        ├── 04-reviewer.md                    Sonnet — review code
        ├── 05-debugger.md                    Sonnet — debug + fix bug
        ├── 06-tester.md                      Sonnet — viết + chạy test
        ├── 07-refactorer.md                  Sonnet — refactor code
        ├── 08-db-specialist.md               Sonnet — database + migration
        ├── 09-api-designer.md                Sonnet — thiết kế REST API
        ├── 10-security-auditor.md            Opus  — audit bảo mật
        ├── 11-performance-analyst.md         Sonnet — tối ưu performance
        ├── 12-doc-writer.md                  Haiku  — viết documentation
        ├── 13-config-manager.md              Haiku  — config + DevOps
        ├── 14-memory-keeper.md               Haiku  — đồng bộ AI memory
        └── 15-planner.md                     Opus  — lập kế hoạch
```
 
Tổng: **50 file** | 3 Opus + 8 Sonnet + 4 Haiku agents | 7 skills | 4 hooks | 4 docs | 7 READMEs
 
---
 
## Cách dùng
 
### Bước 1: Copy vào dự án
Copy toàn bộ vào root dự án: CLAUDE.md, .claudeignore, .ai-memory/, .claude/
 
### Bước 2: Cấp quyền cho hooks
```bash
chmod +x .claude/hooks/*.sh
```
 
### Bước 3: Tùy chỉnh
- Sửa `CLAUDE.md`: thêm build command, quy tắc riêng của dự án
- Sửa `.claude/rules/java-backend.md`: đổi nếu dùng ngôn ngữ khác
- Xóa rules/agents/skills không cần (VD: xóa frontend.md nếu chỉ làm backend)
### Bước 4: Bootstrap
Mở Claude Code, gõ: **"Khởi tạo memory bank cho dự án này"**
hoặc: **/bootstrap-memory**
 
### Bước 5: Làm việc hàng ngày
Giao task bình thường trong chat. Hệ thống tự vận hành:
- CLAUDE.md auto-load → agent biết đọc memory
- Rules auto-load theo path file đang sửa
- Skills auto-trigger hoặc gọi bằng /slash-command
- Hooks tự chạy mỗi khi tool execute
- Agents tự delegate khi nhận ra task phù hợp
---
 
## Tổng quan 3 tầng
 
### Tầng 1 — Memory Bank + Rules + Docs
**Mục đích:** Agent nhớ dự án, tiết kiệm token, tuân thủ convention
- CLAUDE.md: Claude Code đọc tự động, không tốn token đọc thủ công
- .claudeignore: Loại bỏ file noise (node_modules, *.jar, lock files...)
- .ai-memory/: Bộ nhớ bền vững qua các phiên (agent tự quản lý)
- .claude/rules/: Quy tắc coding, chỉ load khi sửa file matching path
- docs/: Tài liệu chi tiết on-demand, agent đọc khi cần bằng @docs/file.md
### Tầng 2 — Skills + Hooks + Permissions
**Mục đích:** Workflow tái sử dụng + automation không hallucinate + giảm prompt
- Skills: Load on-demand, KHÔNG tốn token khi không dùng
- Hooks: Chạy shell script deterministic, chặn lệnh nguy hiểm, log edit, auto typecheck
- Permissions: Allow/deny list giảm "Allow?" prompt, tăng tốc workflow
### Tầng 3 — Multi-Model Agents
**Mục đích:** Đúng model cho đúng việc, bảo tồn context window
- Opus 4.6 (3): architect, security-auditor, planner — suy nghĩ sâu
- Sonnet 4.6 (8): implementer, reviewer, debugger... — code + phân tích
- Haiku 4.5 (4): explorer, doc-writer, config-manager... — nhanh + rẻ
- Mỗi agent chạy context window riêng → context chính sạch
---
 
## Lưu ý quan trọng
 
1. **settings.json**: Nếu đã có sẵn, MERGE phần hooks + permissions vào, KHÔNG ghi đè
2. **Chi phí**: Multi-agent tốn 3-5x token so với single-agent.
   Haiku agents giảm ~40-50% chi phí so với dùng Sonnet cho mọi thứ
3. **Subagent KHÔNG spawn subagent**: Giới hạn của Claude Code
4. **Memory luôn cần sync**: Sau task lớn, gõ /sync-memory hoặc agent tự làm
5. **Code luôn đúng hơn memory**: Khi xung đột, sửa memory chứ không sửa code
6. **docs/ do developer quản lý**: Agent KHÔNG tự sửa file trong docs/
7. **Typecheck hook**: Tự detect Java/Node, chạy compile/lint sau mỗi edit.
   Nếu fail → Claude phải sửa lỗi trước khi tiếp tục
8. **Permissions**: Tùy chỉnh allow/deny list theo dự án thực tế.
   Thêm lệnh build/test riêng vào allow, thêm lệnh nguy hiểm vào deny
9. **chmod**: Sau khi copy, chạy `chmod +x .claude/hooks/*.sh`