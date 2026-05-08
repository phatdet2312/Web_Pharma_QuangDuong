---
name: create-api
description: >
  Tạo API/endpoint hoàn chỉnh. Dùng skill này mỗi khi user yêu cầu
  tạo API mới, thêm endpoint, viết handler/controller, hoặc nhắc đến "tạo API",
  "thêm endpoint". Skill tạo đầy đủ theo convention dự án.
---
 
# Quy trình tạo API Endpoint
 
## Bước 0: Đọc convention
- Đọc "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Xác định: framework, kiến trúc phân lớp, validation pattern, response format

## Bước 1: Xác định thông tin
- Method: GET / POST / PUT / DELETE
- Path: /api/v1/[resource]
- Request body (nếu có): tạo request DTO/schema/model
- Response body: tạo response DTO/schema/model

## Bước 2: Tạo file theo thứ tự
1. **Request/Response model** với validation theo convention dự án
2. **Service/Logic layer** — business logic ở đây
3. **Handler/Controller/Router** — chỉ validate + gọi service

## Bước 3: Cập nhật memory
- Cập nhật `.ai-memory/02_project_map.md`
- Cập nhật/tạo file `03_deep_knowledge/`
- Ghi log `06_evolution_log.md`

## Response format thống nhất
Tuân thủ format trong "Project Convention". Nếu chưa có, đề xuất:
```json
{ "data": {}, "message": "Thành công", "status": 200, "timestamp": "..." }
```
 
## Error format thống nhất
Tuân thủ format trong "Project Convention". Nếu chưa có, đề xuất:
```json
{ "code": "RESOURCE_NOT_FOUND", "message": "Không tìm thấy", "details": [], "timestamp": "..." }
```