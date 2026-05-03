---
paths:
  - "src/main/java/**/entity/**"
  - "src/main/java/**/model/**"
  - "src/main/java/**/repository/**"
  - "src/main/resources/db/**"
---
 
# Database Rules
 
- Table: snake_case, số nhiều (users, order_items)
- Column: snake_case (created_at, user_id)
- Foreign key: [bảng_tham_chiếu]_id
- Mỗi Entity một Repository riêng
 