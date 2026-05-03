# .ai-memory/ — Bộ nhớ bền vững cho AI Agent
 
## Mục đích
Thư mục này là "trí nhớ dài hạn" của agent. Agent tự đọc và tự cập nhật
các file ở đây qua các phiên làm việc, giúp không phải quét lại dự án mỗi lần.
 
## Cấu trúc
```
.ai-memory/
├── README.md                    ← File này
├── 01_system_architecture.md    ← Tech stack, ports, cách chạy dự án
├── 02_project_map.md            ← Cây thư mục, module map, entry points
├── 03_deep_knowledge/           ← Tri thức domain chi tiết
│   └── _TEMPLATE.md             ← Mẫu format (agent tạo file mới theo mẫu này)
├── 04_active_plan.md            ← Kế hoạch task đang thực hiện
├── 05_active_workspace.md       ← Bug, blocker, context cho phiên sau
└── 06_evolution_log.md          ← Nhật ký thay đổi code theo thời gian
```
 
## Vai trò từng file
 
| File | Vai trò | Ai quản lý |
|------|---------|------------|
| `01_system_architecture.md` | Tech stack, ports, cách chạy dự án | Agent tự điền khi bootstrap |
| `02_project_map.md` | Cây thư mục, module map, entry points, entities | Agent tự điền khi bootstrap |
| `03_deep_knowledge/` | Tri thức domain chi tiết (mỗi module 1 file) | Agent tự tạo + cập nhật |
| `03_deep_knowledge/_TEMPLATE.md` | Mẫu format cho file deep knowledge | Developer tạo, agent không sửa |
| `04_active_plan.md` | Kế hoạch task đang thực hiện | Agent cập nhật liên tục |
| `05_active_workspace.md` | Bug, blocker, context cho phiên sau | Agent cập nhật mỗi phiên |
| `06_evolution_log.md` | Nhật ký thay đổi code theo thời gian | Agent ghi log mỗi task |
 
## Vòng đời
 
1. **Bootstrap** (lần đầu): Agent quét dự án → điền 01, 02, tạo file trong 03
2. **Daily**: Agent đọc memory thay vì quét lại → tiết kiệm 60-75% token
3. **Self-sync**: Sau mỗi thay đổi code → agent cập nhật memory tương ứng
4. **Drift detection**: Khi phát hiện code thay đổi ngoài Claude → đồng bộ lại
## Quy tắc quan trọng
 
- `Status: PENDING_BOOTSTRAP` trong 01 hoặc 02 → agent chưa quét, cần chạy bootstrap
- `Confidence: LOW` (>14 ngày không cập nhật) → agent nên verify lại với code thực tế
- Code thực tế LUÔN ĐÚNG hơn memory → xung đột thì sửa memory, KHÔNG sửa code
- Agent KHÔNG BAO GIỜ xóa file memory — chỉ cập nhật nội dung