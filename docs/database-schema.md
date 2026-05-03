# Database Schema
> Last updated: YYYY-MM-DD
> Author: [Tên developer]
> Status: TEMPLATE — Agent KHÔNG sử dụng file này cho đến khi Status đổi thành ACTIVE
> Database: PostgreSQL / MySQL / MariaDB [chọn 1]
 
## ER Diagram
<!-- Mô tả quan hệ tổng quan bằng text -->
<!-- Hoặc link đến diagram tool: dbdiagram.io, draw.io -->
 
## Tables
 
### users
<!-- Bảng người dùng -->
| Column       | Type         | Nullable | Default        | Ghi chú                |
|-------------|-------------|----------|----------------|------------------------|
| id           | BIGINT       | NO       | AUTO_INCREMENT | PK                     |
| email        | VARCHAR(255) | NO       |                | UNIQUE                 |
| password     | VARCHAR(255) | NO       |                | BCrypt hash            |
| full_name    | VARCHAR(100) | NO       |                |                        |
| role         | ENUM         | NO       | 'USER'         | USER, ADMIN, MODERATOR |
| is_active    | BOOLEAN      | NO       | true           |                        |
| created_at   | TIMESTAMP    | NO       | CURRENT_TIMESTAMP |                     |
| updated_at   | TIMESTAMP    | NO       | CURRENT_TIMESTAMP | ON UPDATE            |
 
**Indexes:** email (UNIQUE), role, is_active, created_at
**Relations:** 1-N → orders, 1-N → addresses
 
### [table_name]
<!-- Copy template trên cho mỗi bảng -->
 
## Conventions
- Table name: snake_case, số nhiều
- Column name: snake_case
- FK: [bảng_tham_chiếu]_id
- Mọi bảng đều có: id, created_at, updated_at
- Soft delete dùng cột is_deleted (BOOLEAN) hoặc deleted_at (TIMESTAMP)
- ENUM lưu dạng VARCHAR, validate ở application layer
## Migration History
<!-- Liệt kê các migration quan trọng -->
| Version | Ngày       | Mô tả                          |
|---------|------------|----------------------------------|
| V1      | YYYY-MM-DD | Initial schema                   |