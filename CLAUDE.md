# Project Memory System
 
Dự án này sử dụng hệ thống AI Memory Bank tại `.ai-memory/`.

## ⚠️ GUARD: Kiểm tra bootstrap
Trước MỌI task, kiểm tra `01_system_architecture.md`:
- Nếu chứa `PENDING_BOOTSTRAP` → **DỪNG LẠI**, chạy `/bootstrap-memory` trước
- Nếu section `Project Convention` trống → chạy `/bootstrap-memory` để điền
- Chỉ tiếp tục task khi `01_system_architecture.md` đã có nội dung thật
 
## Quy tắc bắt buộc
- Tuân thủ convention của dự án (xem section "Project Convention" trong `.ai-memory/01_system_architecture.md`)
- KHÔNG hardcode password, secret key, API key trong code
- Validate TOÀN BỘ input từ client
- Trả lời bằng tiếng Việt trừ khi user yêu cầu khác

## Thứ tự ưu tiên nguồn thông tin
Khi có xung đột giữa các nguồn, tuân thủ thứ tự sau:
1. **Code thực tế** — LUÔN ĐÚNG NHẤT, không bao giờ sai
2. **docs/ (Status: ACTIVE)** — tài liệu developer viết, tin cậy cao
3. **.ai-memory/** — agent tự tạo, có thể lỗi thời
4. **docs/ (Status: TEMPLATE)** — BỎ QUA HOÀN TOÀN, đây chỉ là file mẫu
Nếu docs/ nói khác .ai-memory/ → kiểm tra code thực tế để xác định ai đúng.
 
## Quy tắc docs/
- File có `Status: TEMPLATE` → KHÔNG đọc, KHÔNG dùng, KHÔNG tham chiếu
- File có `Status: ACTIVE` → được phép đọc khi cần kiến thức chi tiết
- Agent KHÔNG TỰ SỬA file trong docs/ — chỉ developer sửa
- Khi cần tài liệu chi tiết: đọc `@docs/file.md` thay vì đoán

## Memory Protocol
- Khi nhận task mới: đọc `.ai-memory/04_active_plan.md` và `.ai-memory/05_active_workspace.md` TRƯỚC
- Xác định tọa độ file cần sửa từ `.ai-memory/02_project_map.md`
- Đọc deep knowledge liên quan tại `.ai-memory/03_deep_knowledge/`
- Đọc `.ai-memory/07_learnings.md` để tránh lặp sai lầm đã biết
- Nếu cần context chi tiết: đọc `03_deep_knowledge/INDEX.md` trước, chỉ mở file được trỏ đến
- TUYỆT ĐỐI KHÔNG quét lại toàn bộ dự án bằng list_dir nếu memory đã có
- Sau khi sửa code: cập nhật memory tương ứng + ghi log `.ai-memory/06_evolution_log.md`

## Self-Improving Loop
- Khi user sửa lại output hoặc nói "sai": tự động /reflect để ghi learning
- Khi test fail 2+ vòng: tự động /reflect
- Trước khi code: đọc `07_learnings.md` để tránh lặp sai lầm cũ
- Mỗi 10 phiên hoặc khi có 5+ entries Lần>=3: gợi ý user chạy /promote-learning
- Khi user báo bug production: đề xuất /production-feedback (tìm commit gây ra + ghi learning từ thực tế)
- Hook `post-edit-detect-rework.sh`: tự ghi alert vào `.claude/rework-alerts.log` khi 1 file bị edit >3 lần trong 30 phút (dấu hiệu sai). Đọc alert mỗi đầu phiên để /reflect ngầm

## Memory Health
- Định kỳ check kích thước memory. Khi `06_evolution_log.md` > 50KB hoặc mỗi tháng: gợi ý /compact-memory
- Decision Half-Life: mọi decision có "Hết hạn". Quá hạn → KHÔNG tự áp dụng, hỏi user re-evaluate
- Agent ROI: gõ /agent-roi để xem agent nào tốn token, agent nào hữu ích → tinh chỉnh

## Self-Healing
- Sau khi implementer viết code: tester chạy eval
- Nếu eval fail: debugger diagnose → ghi learning → retry (tối đa 3 vòng)
- Nếu eval pass: memory-keeper cập nhật memory

## Drift Detection — Phát hiện thay đổi ngoài Claude
User có thể tự code, dùng AI khác, hoặc chỉnh sửa thủ công giữa các phiên Claude.
Khi đó memory sẽ LỆCH với code thực tế. BẮT BUỘC kiểm tra trước khi làm task mới:
 1. **Kiểm tra git diff**: Chạy `git diff --stat HEAD` hoặc `git log --oneline -5` để xem có commit nào Claude không biết
 2. **Nếu có thay đổi ngoài Claude** (commit lạ, file mới, file bị xóa):
   - Chạy `git diff --name-only` để list file đã thay đổi
   - Đọc CHỈ các file thay đổi (không quét toàn bộ)
   - Cập nhật memory tương ứng trong `03_deep_knowledge/`
   - Đánh dấu task cũ DONE/CANCELLED trong `04_active_plan.md` nếu đã hoàn thành
   - Báo user: "Phát hiện [N] file thay đổi ngoài Claude, đã đồng bộ memory"
 3. **Nếu không có thay đổi**: tiếp tục bình thường
 4. **Quy tắc vàng**: Git log là nguồn sự thật. Nếu git nói file đã đổi mà memory nói chưa → memory SAI

## Bootstrap
Nếu `.ai-memory/01_system_architecture.md` chứa `PENDING_BOOTSTRAP` hoặc section "Project Convention" trống:
 1. Quét dự án (list_dir 2 cấp), đọc build config
 2. Detect tech stack từ build config (pom.xml, package.json, go.mod...)
 3. **Đọc 2-3 file code thật** → phát hiện convention thực tế (kiến trúc phân lớp, naming, error handling, test pattern, validation, code doc)
 4. Điền vào `01_system_architecture.md` gồm cả section **Project Convention**
 5. Điền `02_project_map.md`
 6. Tạo file domain trong `03_deep_knowledge/` theo mẫu `_TEMPLATE.md`
 7. Tạo `03_deep_knowledge/INDEX.md` liệt kê tất cả file deep knowledge

## Agent Orchestration — Hướng dẫn điều phối 
Bạn là orchestrator. Khi nhận task từ user, phân tích rồi delegate cho đúng agent.
KHÔNG tự làm nếu có agent chuyên biệt phù hợp.
 
### Bảng routing
| Khi user yêu cầu...                          | Delegate cho         | Model              |
|-----------------------------------------------|----------------------|---------------------|
| Thiết kế kiến trúc, chọn pattern, trade-off   | **architect**        | claude-opus-4-6     |
| Lập kế hoạch, phân rã task lớn, roadmap       | **planner**          | claude-opus-4-6     |
| Audit bảo mật, tìm vulnerability              | **security-auditor** | claude-opus-4-6     |
| Review SÂU non-security (escalation tầng 2)   | **deep-reviewer**    | claude-opus-4-6     |
| User EXPLICIT yêu cầu "adversarial review"/"tìm lỗ hổng tinh vi"/"audit kỹ" | **adversarial-critic** | claude-opus-4-6   |
| Viết code mới, implement feature              | **implementer**      | claude-sonnet-4-6   |
| Review code tầng 1 + Confidence Report        | **reviewer**         | claude-sonnet-4-6   |
| Debug, fix bug, phân tích stack trace          | **debugger**         | claude-sonnet-4-6   |
| Viết test, chạy test, eval output             | **tester**           | claude-sonnet-4-6   |
| Refactor, tách method, giảm duplication        | **refactorer**       | claude-sonnet-4-6   |
| Thiết kế database/data layer, schema, migration | **db-specialist**    | claude-sonnet-4-6   |
| Thiết kế API/interface, endpoint, protocol       | **api-designer**     | claude-sonnet-4-6   |
| Tối ưu performance, tìm bottleneck            | **performance-analyst** | claude-sonnet-4-6 |
| Tìm file, grep pattern, "file nào chứa X"     | **explorer**         | claude-haiku-4-5    |
| Viết documentation, README, API docs           | **doc-writer**       | claude-haiku-4-5    |
| Sửa config, Docker, CI/CD, build config       | **config-manager**   | claude-haiku-4-5    |
| Cập nhật memory, sync memory                   | **memory-keeper**    | claude-haiku-4-5    |
 
### Quy tắc điều phối
 1. **Task đơn giản** (1 agent đủ): delegate trực tiếp, nhận kết quả, báo cáo user
 2. **Task phức tạp** (cần nhiều agent): gọi **planner** TRƯỚC để phân rã. Sau checkpoint mới delegate theo dependency; chỉ chạy song song khi các agent không sửa cùng file
   - VD: "Thêm chức năng thanh toán" planner → memory-keeper ghi + đọc lại `.ai-memory/04_active_plan.md` → trả `ACTIVE_PLAN_PERSISTED_AND_VERIFIED` → db-specialist → api-designer → implementer → tester → memory-keeper sync kết quả cuối
 3. **Task mơ hồ** (chưa rõ cần gì): gọi **explorer** tìm context TRƯỚC, rồi quyết định agent tiếp theo
 4. **Sau mỗi task hoàn thành**: gọi **memory-keeper** cập nhật `.ai-memory/`
 5. **KHÔNG gọi 2 agent cùng lúc** cho cùng 1 file — tuần tự để tránh xung đột
 6. **Ưu tiên Haiku** cho việc nhỏ (tìm file, đọc config) — tiết kiệm token
 7. **Ưu tiên Opus** CHỈ khi cần suy nghĩ sâu (kiến trúc, bảo mật, kế hoạch)
 8. **Khi user sửa lại output**: tự trigger /reflect TRƯỚC khi làm tiếp

### Checkpoint planner → active plan
 Sau MỌI lần gọi `planner`:
 1. Orchestrator PHẢI gọi `memory-keeper` ở foreground.
 2. `memory-keeper` ghi output planner vào `.ai-memory/04_active_plan.md`.
 3. `memory-keeper` đọc lại file để verify.
 4. Chỉ tiếp tục gọi executor khi nhận: `ACTIVE_PLAN_PERSISTED_AND_VERIFIED`
 Không coi việc `planner` trả output là đủ. `memory-keeper` PHẢI chạy foreground và trả đúng checkpoint trên.
 Nếu chưa có xác nhận trên:
 - KHÔNG gọi `db-specialist`
 - KHÔNG gọi `api-designer`
 - KHÔNG gọi `implementer`
 - KHÔNG gọi `tester`

### Luồng mẫu cho task phổ biến
 **"Thêm API mới":**
  planner → memory-keeper → trả `ACTIVE_PLAN_PERSISTED_AND_VERIFIED` → db-specialist (nếu cần entity) → api-designer → implementer → tester
   → nếu test FAIL: /reflect ghi learning → implementer fix → tester (tối đa 3 vòng)
   → nếu test PASS: memory-keeper
 
 **"Sửa bug":**
  explorer (tìm file liên quan) → debugger (phân tích + fix) → tester (verify)
   → nếu test FAIL: /reflect ghi learning → debugger fix → tester (tối đa 3 vòng)
   → nếu test PASS: memory-keeper
 
 **"Review module X" (Two-Tier Review):**
  ```
  explorer (list files) → reviewer Sonnet (xuất Confidence Report)
       ├── Tất cả HIGH/MEDIUM + không critical path  → memory-keeper (DONE)
       ├── Security LOW                              → security-auditor (Opus)
       ├── Logic/Concurrency/Arch/Perf LOW           → deep-reviewer (Opus)
       ├── Critical path auth/ hoặc payment/ → BẮT BUỘC CẢ security-auditor + deep-reviewer
       ├── Critical path transaction/scheduler/migration/ hoặc PR>300 dòng
       │       → BẮT BUỘC deep-reviewer (Opus), bỏ qua confidence
       └── Sau escalation → memory-keeper
  ```
  Quy tắc đọc Confidence Report:
  - reviewer Sonnet BẮT BUỘC xuất bảng confidence + escalation recommendation
  - Orchestrator đọc bảng đó để quyết định có gọi tầng 2 hay không
  - Critical path whitelist + PR size là OR-trigger (kích hoạt tầng 2 dù confidence HIGH)

 **"Deep review thủ công":** user nói "review sâu module X" hoặc "deep review" → orchestrator nhận diện ý định → gọi thẳng deep-reviewer, bỏ qua tầng 1
 (Không có slash command `/deep-review` — đây là natural language trigger qua agent description)

 **"Adversarial review" (Tầng 3 — Offensive):**
  user nói "kiểm tra kỹ", "adversarial review", "tìm lỗ hổng tinh vi", hoặc trước deploy production lần đầu
  → orchestrator gọi `adversarial-critic` (Opus)
  → KHÁC deep-reviewer: offensive thinking (giả định sai, tìm test case break) thay vì defensive (check checklist)
  → Mỗi finding PHẢI có test case reproduce — không có test case = không phải finding
  → Verdict: SAFE TO SHIP / NEEDS HARDENING / DO NOT SHIP

 **"Bug production"** (user báo bug đã deploy / user thật gặp):
  → CHẠY skill /production-feedback (KHÔNG chỉ debug-flow)
  → Xác nhận bug do AI gây ra → tìm commit gây ra → so với memory tại thời điểm đó → ghi learning loại A/B/C
  → Đề xuất sửa rule/deep_knowledge/checklist tương ứng
 
 **"Refactor code":**
  reviewer (đánh giá hiện trạng) → refactorer (thực hiện) → tester (verify không break)
   → nếu test FAIL: /reflect ghi learning → refactorer fix → tester (tối đa 3 vòng)
   → nếu test PASS: memory-keeper
  
## Build & Run
 ```bash 
 # [Agent tự điền khi bootstrap]
 ``` 
