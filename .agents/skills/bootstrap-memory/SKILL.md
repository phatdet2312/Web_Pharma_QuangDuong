---
name: bootstrap-memory
description: Khởi tạo AI Memory Bank cho dự án. Dùng khi memory chưa có hoặc 01_system_architecture.md chứa PENDING_BOOTSTRAP. Auto-trigger khi user nói "khởi tạo memory", "bootstrap", "init memory", "bắt đầu dự án mới".
---

# Quy trình Bootstrap Memory Bank

## Bước 0: Kiểm tra dự án TRỐNG (NEW PROJECT GUARD)
- List thư mục root cấp 1
- Nếu KHÔNG tìm thấy build config (pom.xml/package.json/go.mod/Cargo.toml/...) VÀ KHÔNG có file code thật (`.java`, `.py`, `.go`, `.ts`, `.tsx`, `.rs`, `.cs`, `.rb`, `.kt`, `.swift`, `.dart`...):
  **DỪNG BOOTSTRAP AUTO-DETECT**, chuyển sang chế độ INTERACTIVE:
  1. Hỏi user trực tiếp:
     - "Dự án trống. Stack dự định? (Java/Spring, Node/Express, Python/FastAPI, Go, ...)"
     - "Loại dự án? (web-backend, web-fullstack, mobile, cli, library...)"
     - "Database? (PostgreSQL, MySQL, MongoDB, SQLite, không có...)"
     - "Build tool? (Maven, Gradle, npm, pip, cargo...)"
  2. Điền `01_system_architecture.md` với câu trả lời + Confidence: LOW (vì chưa có code thật để verify)
  3. Đánh dấu Project Convention "Chưa xác định — sẽ phát hiện sau khi có code"
  4. Tạo 02_project_map.md với note "Empty project — map sẽ tự cập nhật"
  5. Tạo 03_deep_knowledge/INDEX.md trống
  6. Báo user: "Bootstrap interactive xong. Khi tạo file code đầu tiên, chạy $sync-memory để refine"
- Nếu CÓ build config / file code → tiếp tục Bước 1

## Bước 1: Quét dự án
- List dir root (2 cấp sâu), KHÔNG quét: node_modules, target, build, .git

## Bước 2: Detect Tech Stack
| Build config | Tech Stack |
|--------------|-----------|
| pom.xml | Java + Maven |
| build.gradle / .kts | Java/Kotlin + Gradle |
| package.json | Node.js (đọc dependencies cho framework) |
| go.mod | Go |
| Cargo.toml | Rust |
| requirements.txt / pyproject.toml / setup.py | Python |
| *.csproj / *.sln | C# / .NET |
| Gemfile | Ruby |
| pubspec.yaml | Dart / Flutter |
| Package.swift | Swift |

Đọc thêm: application.yml, appsettings.json, docker-compose.yml. Với `.env`/file sensitive trong `.codexignore`: KHÔNG Read raw; chỉ đọc khi user explicit cho phép, hoặc dùng pattern cụ thể và mask giá trị secret.

## Bước 2b: Detect Project Type
| Dấu hiệu | Project Type |
|----------|-------------|
| REST controller/router + DB | web-backend |
| REST + frontend (React/Vue/Angular) cùng repo | web-fullstack |
| Xcode project / Android manifest / pubspec.yaml | mobile |
| Electron / Tauri / WPF / SwiftUI | desktop |
| main entry + arg parser, không server | cli |
| Unity / Unreal / Godot | game |
| Jupyter / pandas / sklearn / torch | data-science |
| Chỉ src/ + tests/ + publish config | library |
| Nhiều packages/services trong 1 repo | monorepo |
| HAL / RTOS / Arduino | embedded |

Không rõ → ghi `other` + mô tả đặc điểm.

## Bước 3: Detect Convention từ code thực tế
**QUAN TRỌNG** — Đọc 2-3 file code chính (entry point, 1 service, 1 handler):
- Kiến trúc phân lớp: flow request đi qua những layer nào?
- Naming: camelCase? snake_case? PascalCase?
- Error handling: exception? error code? Result type?
- Test: framework? naming pattern? location?
- Validation: dùng gì? annotation? schema? middleware?
- Code doc: Javadoc? docstring? godoc? JSDoc?
- Nếu dự án TRỐNG (chưa có code): chỉ ghi convention dự kiến từ câu trả lời user/framework mặc định với Confidence: LOW, không đoán như sự thật đã verify.

## Bước 4: Điền `01_system_architecture.md`
- Tech Stack table đầy đủ
- Project Type + đặc điểm từ Bước 2b
- Kiến trúc tổng quan: Monolith/Microservice + luồng request chính
- Cách chạy: lệnh build/dev/test/lint cụ thể
- Project Convention từ Bước 3
- Xóa `PENDING_BOOTSTRAP`, thay bằng ngày hôm nay
- Xóa comment hướng dẫn `<!-- -->`

## Bước 5: Điền `02_project_map.md`
- Cây thư mục, Module Map, Entry Points, Entities
- Xóa `PENDING_BOOTSTRAP`

## Bước 6: Tạo Deep Knowledge
- Mỗi domain chính → 1 file `03_deep_knowledge/<name>.md`
- Theo format `_TEMPLATE.md`, Confidence: HIGH
- Tạo `03_deep_knowledge/INDEX.md` liệt kê tất cả file (module | file | mô tả 1 câu)

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

## QUY TẮC
- ĐỌC CODE THẬT, KHÔNG đoán convention từ tên framework
- Convention không rõ: ghi "Chưa xác định" + Confidence: LOW
- Bootstrap CHỈ chạy 1 lần — sau đó mọi subagent đọc từ memory
- Nếu sai → user chạy $sync-memory hoặc sửa tay 01_system_architecture.md
