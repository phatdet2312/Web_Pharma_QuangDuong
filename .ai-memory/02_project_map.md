# Project Map - Bản đồ dự án
> Last updated: YYYY-MM-DD
> Status: PENDING_BOOTSTRAP (Agent sẽ tự điền khi chạy lần đầu)
 
## Cấu trúc thư mục chính
<!-- Agent quét bằng list_dir (2 cấp) và điền vào đây -->
```
project-root/
├── src/
│   ├── main/
│   │   ├── java/com/...        # [Agent tự điền]
│   │   └── resources/          # [Agent tự điền]
│   └── test/                   # [Agent tự điền]
├── frontend/ (nếu có)          # [Agent tự điền]
└── ...
```
 
## Module Map
<!-- Mỗi module/package chính, mô tả 1 dòng -->
<!-- Đây là "bản đồ" để agent xác định TỌA ĐỘ file cần sửa -->
 
| Module/Package | Đường dẫn | Vai trò | Files chính |
|----------------|-----------|---------|-------------|
|                |           |         |             |
 
## Entry Points quan trọng
<!-- Các file "cửa ngõ" mà agent thường cần mở -->
<!-- Agent ưu tiên đọc các file này khi cần hiểu context nhanh -->
 
| Mục đích             | File path                    |
|----------------------|------------------------------|
| Main Application     |                              |
| Security Config      |                              |
| Database Config      |                              |
| Global Exception     |                              |
| API Docs / Swagger   |                              |
 
## Database Entities
<!-- Danh sách entity + quan hệ chính -->
<!-- Giúp agent hiểu data model mà không cần mở từng Entity file -->
 
| Entity | Table | Quan hệ chính |
|--------|-------|---------------|
|        |       |               |
 