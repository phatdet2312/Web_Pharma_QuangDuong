---
name: bootstrap-memory
description: >
  Khởi tạo AI Memory Bank cho dự án mới. Dùng khi user nói "khởi tạo memory",
  "bootstrap", "init memory", "bắt đầu dự án mới", hoặc khi phát hiện
  01_system_architecture.md chứa PENDING_BOOTSTRAP.
---
 
# Quy trình Bootstrap Memory Bank
 
## Bước 1: Quét dự án
- `list_dir` root (2 cấp sâu), KHÔNG quét: node_modules, target, build, .git
## Bước 2: Đọc config chính
- pom.xml / package.json → tech stack
- application.yml / .env → ports, database
- docker-compose.yml → services
## Bước 3: Điền `01_system_architecture.md`
- Tech Stack, kiến trúc, ports, cách chạy
- Xóa PENDING_BOOTSTRAP, thay bằng ngày hôm nay
## Bước 4: Điền `02_project_map.md`
- Cây thư mục, Module Map, Entry Points, Entities
- Xóa PENDING_BOOTSTRAP
## Bước 5: Tạo Deep Knowledge
- Mỗi domain chính → 1 file `03_deep_knowledge/`
- Theo format `_TEMPLATE.md`, Confidence: HIGH
## Bước 6: Báo cáo
```
✅ Bootstrap hoàn tất!
📁 Đã tạo: [số] file deep knowledge
🧠 Modules phát hiện: [danh sách]
⏭️ Sẵn sàng nhận task
```
 