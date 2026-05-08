---
name: create-entity
description: >
  Tạo Database Entity/Model + Repository/Store + Migration. Dùng mỗi khi user yêu cầu
  tạo bảng/collection mới, thêm entity/model, thiết kế database, hoặc nhắc đến "tạo bảng",
  "thêm entity", "model mới", "database schema".
---
 
# Quy trình tạo Entity
 
 ## Bước 0: Đọc convention
- Đọc "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Xác định: ORM/ODM, migration tool, naming convention cho database

## Bước 1: Thiết kế schema
- Naming convention theo "Project Convention" (VD: snake_case cho SQL, camelCase cho MongoDB)
- Luôn có: id (PK), created_at, updated_at
- FK/ref: theo convention dự án


## Bước 2: Tạo file theo thứ tự (adapt theo convention dự án)
1. **Entity/Model** — định nghĩa fields, relations, validation theo ORM/ODM dự án
2. **Repository/Store/DAO** — data access layer theo pattern dự án
3. **Migration** (nếu có migration tool) — có rollback

## Bước 3: Cập nhật memory
- Thêm vào bảng Database Entities trong `02_project_map.md`
- Tạo/cập nhật `03_deep_knowledge/`
- Ghi log `06_evolution_log.md`
 