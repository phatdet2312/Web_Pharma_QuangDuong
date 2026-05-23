# AI Agent System — Full Kit (Tầng 1 + 2 + 3)
 
## Kiến trúc hoàn chỉnh
 
```
project-root/
│
├── CLAUDE.md                              # Auto-load mỗi phiên
├── .claudeignore                          # Filter token
│
├── .ai-memory/                            # ═══ TẦNG 1: MEMORY BANK ═══
│   ├── 01_system_architecture.md             Kiến trúc hệ thống  + Architecture Decisions
│   ├── 02_project_map.md                     Bản đồ dự án + tọa độ file
│   ├── 03_deep_knowledge/                    Tri thức domain chi tiết
│   │   ├── INDEX.md                          Router — agent đọc trước, drill sau
│   │   └── _TEMPLATE.md                      Mẫu format cho agent + Decision Log
│   ├── 04_active_plan.md                     Kế hoạch task đang làm
│   ├── 05_active_workspace.md                Bug, blocker, context hiện tại
│   ├── 06_evolution_log.md                   Nhật ký thay đổi
│   └── 07_learnings.md                       Bài học từ sai lầm (self-improving)
│ 
├── docs/                                  # ═══ TÀI LIỆU ON-DEMAND ═══
│   ├── README.md                             Hướng dẫn cách dùng docs/
│   ├── api-spec.md                           Đặc tả API chi tiết
│   ├── database-schema.md                    Schema database đầy đủ
│   └── coding-guidelines.md                  Quy tắc coding mở rộng
│
└── .claude/
    ├── settings.json                      # Cấu hình hooks + permissions + env
    ├── setup.cmd                          # Windows: double-click → auto-detect bash/powershell
    ├── setup.ps1                          # (được setup.cmd gọi ngầm)
    ├── setup.sh                           # Linux/macOS: khôi phục về bash nếu cần
    │
    ├── rules/                             # ═══ TẦNG 1: PATH-SCOPED RULES ═══
    │   ├── backend.md                       Load khi sửa code backend (mọi ngôn ngữ)
    │   ├── frontend.md                       Chỉ load khi sửa frontend
    │   ├── database.md                       Chỉ load khi sửa entity/repo
    │   ├── git.md                            Load mọi phiên (ngắn)
    │   └── memory-protocol.md                Load mọi phiên (memory + learning + eval)
    │
    ├── skills/                            # ═══ TẦNG 2: ON-DEMAND SKILLS ═══
    │   ├── create-api/SKILL.md               /create-api — tạo REST endpoint
    │   ├── create-entity/SKILL.md            /create-entity — tạo Entity+Repo
    │   ├── debug-flow/SKILL.md               /debug-flow — debug có hệ thống
    │   ├── review-module/SKILL.md            /review-module — Two-Tier Review
    │   ├── bootstrap-memory/SKILL.md         /bootstrap-memory — khởi tạo memory
    │   ├── sync-memory/SKILL.md              /sync-memory — đồng bộ memory
    │   ├── detect-drift/SKILL.md             /detect-drift - phát hiện thay đổi ngoài Claude
    │   ├── rollback/SKILL.md                 /rollback — rollback nguyên tử code + memory
    │   ├── reflect/SKILL.md                  /reflect — ghi learning sau sai lầm
    │   ├── promote-learning/SKILL.md         /promote-learning — đưa learning lên rules
    │   ├── self-eval/SKILL.md                /self-eval — eval loop tự sửa
    │   ├── agent-roi/SKILL.md                /agent-roi — phân tích chi phí + ROI mỗi agent
    │   ├── compact-memory/SKILL.md           /compact-memory — nén memory cũ, archive raw
    │   └── production-feedback/SKILL.md      /production-feedback — học từ bug production thật
    │
    ├── hooks/                             # ═══ TẦNG 2: LIFECYCLE HOOKS ═══
    │   ├── README.md                         Hướng dẫn hooks
    │   ├── pre-bash-firewall.sh / .ps1       Chặn rm -rf, git reset --hard...
    │   ├── pre-edit-protect.sh / .ps1        Bảo vệ file theo .claudeignore
    │   ├── post-edit-log.sh / .ps1           Log mọi file đã sửa
    │   ├── post-edit-typecheck.sh / .ps1     Auto compile/lint sau mỗi edit
    │   ├── post-edit-detect-rework.sh / .ps1 Cảnh báo khi file bị edit >3 lần/30min
    │   ├── session-start.sh / .ps1           Check drift + rework + memory bloat
    │   └── scan-secret-prompt.sh / .ps1      Chặn secret/credential trong prompt
    │
    └── agents/                            # ═══ TẦNG 3: MULTI-MODEL AGENTS ═══
        ├── README.md                         Hướng dẫn agents
        ├── 01-architect.md                   Opus  — thiết kế kiến trúc
        ├── 02-implementer.md                 Sonnet — viết code
        ├── 03-explorer.md                    Haiku  — tìm kiếm nhanh
        ├── 04-reviewer.md                    Sonnet — review tầng 1 + Confidence Report
        ├── 05-debugger.md                    Sonnet — debug + fix bug
        ├── 06-tester.md                      Sonnet — viết + chạy test + eval
        ├── 07-refactorer.md                  Sonnet — refactor code
        ├── 08-db-specialist.md               Sonnet — database + migration
        ├── 09-api-designer.md                Sonnet — thiết kế REST API
        ├── 10-security-auditor.md            Opus  — audit bảo mật
        ├── 11-performance-analyst.md         Sonnet — tối ưu performance
        ├── 12-doc-writer.md                  Haiku  — viết documentation
        ├── 13-config-manager.md              Haiku  — config + DevOps
        ├── 14-memory-keeper.md               Haiku  — đồng bộ AI memory
        ├── 15-planner.md                     Opus  — lập kế hoạch
        ├── 16-deep-reviewer.md               Opus  — review SÂU non-security (tầng 2)
        └── 17-adversarial-critic.md          Opus  — offensive review (tầng 3, tìm assumption ngầm)
```
 
Tổng: **73 file** | 5 Opus + 8 Sonnet + 4 Haiku agents | 14 skills | 7 hooks (×2 .sh/.ps1) | 3 setup scripts | 4 docs | 7 READMEs
 
---
 
## Yêu cầu hệ thống
| Thành phần | Yêu cầu | Kiểm tra |
|-----------|---------|----------|
| Node.js | v18+ | `node --version` |
| npm | đi kèm Node.js | `npm --version` |
| Claude Code | CLI tool | `claude --version` |
| API key | Anthropic API key | Đăng nhập khi chạy `claude` lần đầu |

**Cài Node.js:** https://nodejs.org (chọn LTS, cài xong mở terminal mới)

**Cài Claude Code:**
```bash
npm install -g @anthropic-ai/claude-code
```

**Claude Code chạy trên mọi terminal:** PowerShell, CMD, Git Bash, Windows Terminal, macOS Terminal, Linux shell. Không yêu cầu shell cụ thể.

---

## Cách dùng
### Bước 1: Copy vào dự án
Copy toàn bộ vào root dự án: `CLAUDE.md`, `.claudeignore`, `.ai-memory/`, `.claude/`

### Bước 2: Setup hooks (chạy 1 lần)
- **Windows:** double-click `.claude\setup.cmd` (tự detect bash hay PowerShell, không cần biết chi tiết)
- **Linux/macOS:** `chmod +x .claude/hooks/*.sh .claude/setup.sh`

### Bước 3: Tùy chỉnh (tùy chọn)
- Sửa `CLAUDE.md`: thêm build command, quy tắc riêng của dự án
- Xóa rules/agents/skills không cần (VD: xóa `frontend.md` nếu chỉ làm backend)
- `create-api/`, `create-entity/` chứa ví dụ Java — agent tự điều chỉnh theo convention từ memory, nhưng bạn có thể sửa nội dung cho khớp stack nếu muốn cụ thể hơn

### Bước 4: Khởi chạy Claude Code
```bash
cd <thu-muc-du-an>
claude
```
Lần đầu sẽ hỏi đăng nhập API key. Sau đó gõ: **"Khởi tạo memory bank cho dự án này"** hoặc **/bootstrap-memory**

### Bước 5: Làm việc hàng ngày
Giao task bình thường trong chat. Hệ thống tự vận hành:
- `CLAUDE.md` auto-load → agent biết đọc memory
- Rules auto-load theo path file đang sửa
- Skills auto-trigger hoặc gọi bằng /slash-command
- Hooks tự chạy mỗi khi tool execute
- Agents tự delegate khi nhận ra task phù hợp
---

## Prompt chuẩn — Kích hoạt hệ thống

> Copy prompt phù hợp, paste vào Claude Code. Thay `[...]` bằng nội dung thực tế.
> **Không cần ra lệnh từng bước** — CLAUDE.md + rules + hooks đã cài sẵn toàn bộ quy trình.
> Bạn chỉ cần mô tả rõ **CÁI GÌ**, hệ thống tự quyết định **LÀM THẾ NÀO**.

---

### FORM 1: Khởi tạo dự án mới (chạy 1 lần duy nhất)

```
/bootstrap-memory
```

---

### FORM 2: Giao task hàng ngày (dùng nhiều nhất)

```
[MÔ TẢ TASK — viết rõ: làm gì, ở đâu, input/output mong muốn]
```

**Ví dụ:**
```
Tạo API endpoint POST /api/v1/orders cho chức năng đặt hàng.
Request body: { productId, quantity, customerId }.
Validate input, lưu vào database, trả về order ID.
```

> Hệ thống tự: đọc memory → delegate agent → code → eval → sync memory.

---

### FORM 3: Fix bug

```
Bug: [MÔ TẢ HIỆN TƯỢNG]
Bước tái hiện: [1... 2... 3...]
Expected: [KẾT QUẢ ĐÚNG]
Actual: [KẾT QUẢ SAI]
```

> Hệ thống tự: check learnings cũ → debug-flow → fix → eval → reflect nếu cần.

---

### FORM 4: Review trước deploy

```
Review module [TÊN MODULE hoặc BRANCH].
```

Thêm ngữ cảnh nếu cần:
```
Review module payment. Đây là lần deploy production đầu tiên.
```

> Hệ thống tự: Three-Tier Review → escalate nếu LOW confidence hoặc critical path.

---

### FORM 5: Refactor

```
Refactor [TÊN MODULE / FILE].
Mục tiêu: [giảm duplication / tách responsibility / cải thiện performance / ...]
```

---

### FORM 6: Bảo trì hệ thống (1-2 tuần/lần)

```
Chạy bảo trì hệ thống agent. Báo cáo tình trạng cho tôi.
```

> Hệ thống tự biết: compact memory, detect drift, check learnings, agent ROI.

---

### FORM 7: Bug production (bug từ user thật)

```
Bug production: [MÔ TẢ TỪ USER / LOG / MONITORING]
```

> Hệ thống tự: /production-feedback → tìm commit → phân loại A/B/C → ghi learning.

---

### Nguyên tắc viết prompt hiệu quả
- **Mô tả rõ CÁI GÌ** — input, output, ràng buộc, vị trí file nếu biết
- **Không ra lệnh từng bước** — CLAUDE.md đã định nghĩa quy trình, ghi đè sẽ gây xung đột
- **Thêm ngữ cảnh đặc biệt nếu có** — "lần đầu deploy", "module core", "deadline gấp"
- **Nếu Claude bỏ sót gì** — nhắc bằng ngôn ngữ tự nhiên: "Bạn quên review chưa?" thay vì ra lệnh "/review-module"

---
 
## Tổng quan 3 tầng cấu trúc
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
- Opus 4.6 (5): architect, security-auditor, planner, deep-reviewer, adversarial-critic — suy nghĩ sâu + escalation + offensive
- Sonnet 4.6 (8): implementer, reviewer, debugger... — code + phân tích
- Haiku 4.5 (4): explorer, doc-writer, config-manager... — nhanh + rẻ
- Mỗi agent chạy context window riêng → context chính sạch
- **Three-Tier Review**:
  - Tầng 1 reviewer (Sonnet, defensive) — mọi PR, xuất Confidence Report
  - Tầng 2 deep-reviewer/security-auditor (Opus, analytical) — escalate khi LOW hoặc critical path
  - Tầng 3 adversarial-critic (Opus, offensive) — thủ công, trước deploy production lần đầu

## Tổng quan 3 tầng đột phá
### Tầng A — Self-Improving Loop (MỚI)
- `07_learnings.md`: ghi pattern sai lầm, tích lũy qua phiên
- `/reflect`: tự phản ánh sau correction/test fail → ghi learning
- `/promote-learning`: đưa learning lặp 3+ lần lên rules enforced
- **Kết quả: chất lượng = hàm tăng theo thời gian, không hằng số**
### Tầng B — Self-Healing Eval Loop (MỚI)
- `/self-eval`: compile → test → convention check tự động
- Fail → /reflect ghi learning → fix → eval lại (tối đa 3 vòng)
- **Kết quả: agent tự phát hiện + tự sửa lỗi trước khi báo user**
### Tầng C — Progressive Disclosure (MỚI)
- `INDEX.md`: router cho deep knowledge (~200 token thay vì mở tất cả ~3000+)
- Agent đọc INDEX trước → chỉ drill vào file liên quan
- **Kết quả: tiết kiệm token + chính xác hơn khi chọn file đọc**
### Decision Memory (với Half-Life)
- Architecture Decisions trong `01_system_architecture.md`
- Decision Log trong mỗi deep knowledge file
- 4 agents ghi decision, memory-keeper tổng hợp
- **Half-Life**: mỗi decision có "Hết hạn" (+3 tháng module, +6 tháng cross-cutting). Quá hạn → không tự áp dụng, re-evaluate

### Tầng D — Real-World Feedback Loop (MỚI NHẤT)
- **`/production-feedback`**: khi user báo bug production → tìm commit gây ra → so với memory thời điểm đó → ghi learning loại A (AI bypass convention) / B (memory thiếu) / C (edge case)
- **Hook `post-edit-detect-rework.sh`**: cảnh báo khi file bị edit >3 lần/30min (dấu hiệu sai)
- **`adversarial-critic` (Tầng 3 review)**: offensive thinking — giả định code SAI, tìm test case break
- **Kết quả: AI học từ HẬU QUẢ THẬT (production), không chỉ từ test giả lập**

### Tầng E — Memory Sustainability (MỚI NHẤT)
- **`/compact-memory`**: nén log cũ thành tóm tắt, archive raw → memory không phình to vô hạn
- **`/agent-roi`**: telemetry CSV → biết agent nào tốn token, agent nào không dùng
- **Kết quả: hệ thống bền vững dài hạn, không bị "memory bloat" sau 6 tháng**

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
9. **Setup hooks**: Windows → double-click `.claude\setup.cmd`. Linux/macOS → `chmod +x .claude/hooks/*.sh`
10. **Self-improving**: mỗi sai lầm → learning → rule → lỗi biến mất vĩnh viễn