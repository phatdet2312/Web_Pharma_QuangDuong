# .ai-memory/ — Bộ nhớ bền vững cho AI Agent
 
## Mục đích
Thư mục này là "trí nhớ dài hạn" của agent. Agent tự đọc và tự cập nhật
các file ở đây qua các phiên làm việc, giúp không phải quét lại dự án mỗi lần.
 
 ## Cấu trúc
```
.ai-memory/
├── README.md                    ← File này
├── 01_system_architecture.md    ← Tech stack, ports, Architecture Decisions
├── 02_project_map.md            ← Cây thư mục, module map, entry points
├── 03_deep_knowledge/           ← Tri thức domain chi tiết
│   ├── INDEX.md                 ← Router — agent đọc trước, drill sau
│   └── _TEMPLATE.md             ← Mẫu format (agent tạo file mới theo mẫu này) + Decision Log
├── 04_active_plan.md            ← Kế hoạch task đang thực hiện
├── 05_active_workspace.md       ← Bug, blocker, context cho phiên sau
├── 06_evolution_log.md          ← Nhật ký thay đổi code theo thời gian
├── 07_learnings.md              ← Bài học từ sai lầm (self-improving)
└── _archive/                    ← Raw log đã nén (do /compact-memory tạo); agent KHÔNG đọc
```

## Vai trò từng file
 
|               File                |                      Vai trò                              |          Ai quản lý                      |
|----------------------------- -----|-----------------------------------------------------------|------------------------------------------|
| `01_system_architecture.md`       | Tech stack, ports, cách chạy dự án, Architecture Decisions| Agent tự điền khi bootstrap + cập nhật   |
| `02_project_map.md`               | Cây thư mục, module map, entry points, entities           | Agent tự điền khi bootstrap + cập nhật   |
| `03_deep_knowledge/`              | Tri thức domain chi tiết (mỗi module 1 file)              | Agent tự tạo + cập nhật                  |
| `03_deep_knowledge/INDEX.md`      | Router — liệt kê module + link file                       | Agent tạo khi bootstrap                  |
| `03_deep_knowledge/_TEMPLATE.md`  | Mẫu format cho file deep knowledge   + Decision Log       | Developer tạo, agent không sửa           |
| `04_active_plan.md`               | Kế hoạch task đang thực hiện                              | Agent cập nhật liên tục                  |
| `05_active_workspace.md`          | Bug, blocker, context cho phiên sau                       | Agent cập nhật mỗi phiên                 |
| `06_evolution_log.md`             | Nhật ký thay đổi code theo thời gian                      | Agent ghi log mỗi task                   |
| `07_learnings.md`                 | Bài học từ sai lầm (pattern → rule)                       | Agent ghi qua /reflect                   |
| `_archive/`                       | Raw log đã nén từ /compact-memory; KHÔNG đọc daily        | /compact-memory tạo, agent không quét    |
   
## Vòng đời
 1. **Bootstrap** (lần đầu): Agent quét dự án → điền 01, 02, tạo INDEX + file trong 03
 2. **Daily**: Agent đọc memory thay vì quét lại → tiết kiệm 60-75% token
 3. **Self-sync**: Sau mỗi thay đổi code → agent cập nhật memory tương ứng
 4. **Self-improving**: Sau sai lầm → /reflect ghi vào 07 → /promote-learning đưa lên rules
 5. **Drift detection**: Khi phát hiện code thay đổi ngoài Codex → đồng bộ lại

## Quy tắc quan trọng
- `Status: PENDING_BOOTSTRAP` trong 01 hoặc 02 → agent chưa quét, cần chạy bootstrap
- `Confidence: LOW` (>14 ngày không cập nhật) → agent nên verify lại với code thực tế
- Code thực tế LUÔN ĐÚNG hơn memory → xung đột thì sửa memory, KHÔNG sửa code
- Agent KHÔNG BAO GIỜ xóa file memory — chỉ cập nhật nội dung
- Decision Log là section riêng `## Decision Log` trong mỗi deep knowledge file — CHỈ ghi khi có 2+ phương án
- Architecture Decisions nằm trong `01_system_architecture.md` — cho quyết định cross-cutting
- Progressive Disclosure: đọc INDEX.md trước, chỉ mở file được trỏ đến