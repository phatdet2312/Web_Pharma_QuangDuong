---
name: create-entity
description: Tạo Database Entity/Model + Repository/Store + Migration. Auto-trigger khi user nói "tạo bảng", "thêm entity", "model mới", "database schema", "thiết kế bảng".
---

# Quy trình tạo Entity

## Bước 0: Đọc convention
- Đọc "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Xác định: ORM/ODM, migration tool, naming convention database

## Bước 1: Thiết kế schema
- Naming convention theo "Project Convention" (snake_case cho SQL, camelCase cho MongoDB)
- Luôn có: id (PK), created_at, updated_at
- FK/ref: theo convention dự án

## Bước 2: Spawn `db-specialist` qua `/agent`
Subagent `db-specialist` (medium tier) thực hiện:
1. **Entity/Model** — fields, relations, validation theo ORM/ODM
2. **Repository/Store/DAO** — data access layer
3. **Migration** (nếu có migration tool) — kèm rollback

## Bước 3: Cập nhật memory
Spawn `memory-keeper`:
- Thêm vào bảng Database Entities trong `.ai-memory/02_project_map.md`
- Tạo/cập nhật `.ai-memory/03_deep_knowledge/<entity>.md`
- Ghi log `06_evolution_log.md`

## Bước 4: Self-Eval (BẮT BUỘC)
- Chạy $self-eval để verify compile + migration script đúng
- Pass → memory-keeper sync
- Fail → fix
