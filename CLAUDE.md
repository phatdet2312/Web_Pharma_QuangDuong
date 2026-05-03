# Project Memory System
 
Dự án này sử dụng hệ thống AI Memory Bank tại `.ai-memory/`.
 
## Quy tắc bắt buộc
 
- Tuân thủ kiến trúc phân lớp: Controller → Service → Repository
- KHÔNG hardcode password, secret key, API key trong code
- Validate TOÀN BỘ input từ client
- Trả lời bằng tiếng Việt trừ khi user yêu cầu khác
## Memory Protocol
 
- Khi nhận task mới: đọc `.ai-memory/04_active_plan.md` và `.ai-memory/05_active_workspace.md` TRƯỚC
- Xác định tọa độ file cần sửa từ `.ai-memory/02_project_map.md`
- Đọc deep knowledge liên quan tại `.ai-memory/03_deep_knowledge/`
- TUYỆT ĐỐI KHÔNG quét lại toàn bộ dự án bằng list_dir nếu memory đã có
- Sau khi sửa code: cập nhật memory tương ứng + ghi log `.ai-memory/06_evolution_log.md`
## Bootstrap
 
Nếu `.ai-memory/01_system_architecture.md` chứa `PENDING_BOOTSTRAP`:
1. Quét dự án (list_dir 2 cấp), đọc config chính
2. Điền vào `01_system_architecture.md` và `02_project_map.md`
3. Tạo file domain trong `03_deep_knowledge/` theo mẫu `_TEMPLATE.md`
## Agent Orchestration — Hướng dẫn điều phối
 
Bạn là orchestrator. Khi nhận task từ user, phân tích rồi delegate cho đúng agent.
KHÔNG tự làm nếu có agent chuyên biệt phù hợp.
 
### Bảng routing
 
| Khi user yêu cầu...                           | Delegate cho            | Model               |
|-----------------------------------------------|-------------------------|---------------------|
| Thiết kế kiến trúc, chọn pattern, trade-off   | **architect**           | claude-opus-4-6     |
| Lập kế hoạch, phân rã task lớn, roadmap       | **planner**             | claude-opus-4-6     |
| Audit bảo mật, tìm vulnerability              | **security-auditor**    | claude-opus-4-6     |
| Viết code mới, implement feature              | **implementer**         | claude-sonnet-4-6   |
| Review code, kiểm tra chất lượng              | **reviewer**            | claude-sonnet-4-6   |
| Debug, fix bug, phân tích stack trace         | **debugger**            | claude-sonnet-4-6   |
| Viết test, chạy test                          | **tester**              | claude-sonnet-4-6   |
| Refactor, tách method, giảm duplication       | **refactorer**          | claude-sonnet-4-6   |
| Thiết kế database, entity, migration          | **db-specialist**       | claude-sonnet-4-6   |
| Thiết kế REST API, endpoint mới               | **api-designer**        | claude-sonnet-4-6   |
| Tối ưu performance, tìm bottleneck            | **performance-analyst** | claude-sonnet-4-6   |
| Tìm file, grep pattern, "file nào chứa X"     | **explorer**            | claude-haiku-4-5    |
| Viết documentation, README, javadoc           | **doc-writer**          | claude-haiku-4-5    |
| Sửa config, Docker, CI/CD, pom.xml            | **config-manager**      | claude-haiku-4-5    |
| Cập nhật memory, sync memory                  | **memory-keeper**       | claude-haiku-4-5    |
 
### Quy tắc điều phối
 
1. **Task đơn giản** (1 agent đủ): delegate trực tiếp, nhận kết quả, báo cáo user
2. **Task phức tạp** (cần nhiều agent): gọi **planner** TRƯỚC để phân rã, rồi delegate tuần tự
   - VD: "Thêm chức năng thanh toán" → planner → db-specialist → api-designer → implementer → tester → memory-keeper
3. **Task mơ hồ** (chưa rõ cần gì): gọi **explorer** tìm context TRƯỚC, rồi quyết định agent tiếp theo
4. **Sau mỗi task hoàn thành**: gọi **memory-keeper** cập nhật `.ai-memory/`
5. **KHÔNG gọi 2 agent cùng lúc** cho cùng 1 file — tuần tự để tránh xung đột
6. **Ưu tiên Haiku** cho việc nhỏ (tìm file, đọc config) — tiết kiệm token
7. **Ưu tiên Opus** CHỈ khi cần suy nghĩ sâu (kiến trúc, bảo mật, kế hoạch)
### Luồng mẫu cho task phổ biến
 
**"Thêm API mới":**
planner → db-specialist (nếu cần entity) → api-designer → implementer → tester → memory-keeper
 
**"Sửa bug":**
explorer (tìm file liên quan) → debugger (phân tích + fix) → tester (verify) → memory-keeper
 
**"Review module X":**
explorer (list files) → reviewer (code quality) → security-auditor (nếu module nhạy cảm) → memory-keeper
 
**"Refactor code":**
reviewer (đánh giá hiện trạng) → refactorer (thực hiện) → tester (verify không break) → memory-keeper
 
## Build & Run
```bash
# [Agent tự điền khi bootstrap]
```