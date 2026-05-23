---
name: bootstrap-memory
description: >
  Khởi tạo AI Memory Bank cho dự án mới. Dùng khi user nói "khởi tạo memory",
  "bootstrap", "init memory", "bắt đầu dự án mới", hoặc khi phát hiện
  01_system_architecture.md chứa PENDING_BOOTSTRAP.
---
 
# Quy trình Bootstrap Memory Bank
 
## Bước 0: Kiểm tra dự án TRỐNG (NEW PROJECT GUARD)
- `list_dir` root cấp 1
- Nếu KHÔNG tìm thấy build config (pom.xml/package.json/go.mod/Cargo.toml/...) VÀ KHÔNG có file code (.java/.py/.go/.ts/.rs/.cs/.rb...):
  **DỪNG BOOTSTRAP AUTO-DETECT**, chuyển sang chế độ INTERACTIVE:
  1. Hỏi user trực tiếp:
     - "Dự án trống chưa có code. Bạn dự định stack gì? (Java/Spring, Node/Express, Python/FastAPI, Go, ...)"
     - "Loại dự án? (web-backend, web-fullstack, mobile, cli, library...)"
     - "Database dự định? (PostgreSQL, MySQL, MongoDB, SQLite, không có...)"
     - "Build tool? (Maven, Gradle, npm, pip, cargo...)"
  2. Điền `01_system_architecture.md` với câu trả lời + Confidence: LOW (vì chưa có code thật để verify)
  3. Đánh dấu Project Convention "Chưa xác định — sẽ phát hiện sau khi có code"
  4. Tạo 02_project_map.md với note "Empty project — map sẽ tự cập nhật"
  5. Tạo 03_deep_knowledge/INDEX.md trống
  6. Báo user: "Bootstrap interactive xong. Khi tạo file code đầu tiên, /sync-memory sẽ refine convention"
- Nếu CÓ build config / file code → tiếp tục Bước 1

## Bước 1: Quét dự án
- `list_dir` root (2 cấp sâu), KHÔNG quét: node_modules, target, build, .git

## Bước 2: Detect Tech Stack
Tìm build config để xác định ngôn ngữ + framework:
 
| Nếu tìm thấy... | → Tech Stack |
|-------------------|-------------|
| pom.xml | Java + Maven |
| build.gradle / build.gradle.kts | Java/Kotlin + Gradle |
| package.json | Node.js (kiểm tra dependencies cho framework) |
| go.mod | Go |
| Cargo.toml | Rust |
| requirements.txt / pyproject.toml / setup.py | Python |
| *.csproj / *.sln | C# / .NET |
| Gemfile | Ruby |
| pubspec.yaml | Dart / Flutter |
| Package.swift | Swift |
 
Đọc thêm: app config (application.yml, .env, appsettings.json...), docker-compose.yml
 
## Bước 2b: Detect Project Type
Xác định từ cấu trúc thư mục + dependencies:
 
| Dấu hiệu | → Project Type |
|-----------|---------------|
| REST controller/router + DB config | web-backend |
| REST/GraphQL + frontend (React/Vue/Angular) cùng repo | web-fullstack |
| Xcode project / Android manifest / pubspec.yaml | mobile |
| Electron / Tauri / WPF / SwiftUI macOS | desktop |
| main entry + arg parser, không có server | cli |
| Unity / Unreal / Godot / MonoGame | game |
| Jupyter notebooks / pandas / sklearn / torch | data-science |
| Chỉ có src/ + tests/ + publish config, không có app entry | library |
| Nhiều packages/services trong 1 repo | monorepo |
| HAL / RTOS / Arduino / bare-metal | embedded |
 
Nếu không rõ → ghi `other` + mô tả đặc điểm trong "Đặc điểm"

## Bước 3: Detect Convention từ code thực tế
**QUAN TRỌNG** — Không đoán convention từ framework, phải ĐỌC CODE THẬT:
1. Mở 2-3 file code chính (entry point, 1 service, 1 handler/controller)
2. Phân tích và ghi nhận:
   - **Kiến trúc phân lớp**: flow request đi qua những layer nào?
   - **Naming**: camelCase? snake_case? PascalCase? convention cụ thể?
   - **Error handling**: exception? error code? Result type?
   - **Test**: framework nào? naming pattern? file test ở đâu?
   - **Validation**: dùng gì? annotation? schema? middleware?
   - **Code doc**: Javadoc? docstring? godoc? JSDoc?
3. Nếu dự án TRỐNG (chưa có code): detect từ framework mặc định

## Bước 4: Điền `01_system_architecture.md`
- **Tech Stack table**: điền đầy đủ Language, Framework, Database, Build Tool...
- **Project Type**: điền type + đặc điểm từ Bước 2b
- **Kiến trúc tổng quan**: Monolith/Microservice, luồng request chính
- **Cách chạy dự án**: lệnh build, dev, test, lint cụ thể
- **Project Convention**: điền TẤT CẢ convention phát hiện từ Bước 3
- Xóa `PENDING_BOOTSTRAP`, thay bằng ngày hôm nay
- Xóa các comment hướng dẫn `<!-- -->`, chỉ giữ nội dung thật

## Bước 5: Điền `02_project_map.md`
- Cây thư mục, Module Map, Entry Points, Entities
- Xóa PENDING_BOOTSTRAP

## Bước 6: Tạo Deep Knowledge
- Mỗi domain chính → 1 file `03_deep_knowledge/`
- Theo format `_TEMPLATE.md`, Confidence: HIGH
- Tạo `03_deep_knowledge/INDEX.md` liệt kê tất cả file vừa tạo (module | file | mô tả 1 câu)

## Bước 7: Báo cáo
```
✅ Bootstrap hoàn tất!
🔧 Tech Stack: [language] + [framework] + [database]
📦 Project Type: [type] — [đặc điểm]
📐 Convention: [tóm tắt 1 dòng kiến trúc phân lớp]
📁 Đã tạo: [số] file deep knowledge
🧠 Modules phát hiện: [danh sách]
⏭️ Sẵn sàng nhận task
```
 
## QUY TẮC BOOTSTRAP:
- ĐỌC CODE THẬT, KHÔNG đoán convention từ tên framework
- Nếu convention không rõ: ghi "Chưa xác định" và đánh Confidence: LOW
- Bootstrap CHỈ chạy 1 lần — sau đó mọi agent đọc từ memory
- Nếu bootstrap sai → user chạy /sync-memory hoặc sửa tay 01_system_architecture.md