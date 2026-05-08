---
paths:
  - "**/entity/**"
  - "**/entities/**"
  - "**/model/**"
  - "**/models/**"
  - "**/repository/**"
  - "**/repositories/**"
  - "**/migration/**"
  - "**/migrations/**"
  - "**/schema/**"
  - "**/db/**"
  - "**/*.sql"
---
 
# Database Rules
 
- Naming convention theo "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Foreign key: [bảng_tham_chiếu]_id (SQL) hoặc ref field (NoSQL)
- Mỗi Entity/Model một Repository/Store riêng
- Migration phải có rollback
 
 
# ví dụ Java Database Rules
 
- Table: snake_case, số nhiều (users, order_items)
- Column: snake_case (created_at, user_id)
- Foreign key: [bảng_tham_chiếu]_id
- Mỗi Entity một Repository riêng
 