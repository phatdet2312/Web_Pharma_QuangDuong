---
name: create-entity
description: >
  Tạo Database Entity + Repository + Migration. Dùng mỗi khi user yêu cầu
  tạo bảng mới, thêm entity, thiết kế database, hoặc nhắc đến "tạo bảng",
  "thêm entity", "model mới", "database schema".
---
 
# Quy trình tạo Entity
 
## Bước 1: Thiết kế schema
- Table name: snake_case, số nhiều (users, order_items)
- Column: snake_case (created_at, user_id)
- FK: [bảng]_id
- Luôn có: id (PK), created_at, updated_at
## Bước 2: Tạo file theo thứ tự
1. **Entity class** — @Entity, @Table, @Id, @GeneratedValue, @Column, quan hệ JPA, @CreatedDate/@LastModifiedDate
2. **Repository** — extends JpaRepository, custom query methods nếu cần
3. **Migration** (nếu dùng Flyway/Liquibase) — có rollback
## Bước 3: Cập nhật memory
- Thêm vào bảng Database Entities trong `02_project_map.md`
- Tạo/cập nhật `03_deep_knowledge/`
- Ghi log `06_evolution_log.md`
 