---
name: create-api
description: >
  Tạo REST API endpoint hoàn chỉnh. Dùng skill này mỗi khi user yêu cầu
  tạo API mới, thêm endpoint, viết controller, hoặc nhắc đến "tạo API",
  "thêm endpoint", "viết REST". Skill tạo đầy đủ: DTO, Controller, Service.
---
 
# Quy trình tạo API Endpoint
 
## Bước 1: Xác định thông tin
- Method: GET / POST / PUT / DELETE
- Path: /api/v1/[resource]
- Request body (nếu có): tạo RequestDto
- Response body: tạo ResponseDto
## Bước 2: Tạo file theo thứ tự
1. **RequestDto** với @Valid annotation (@NotBlank, @Email, @Size...)
2. **ResponseDto**
3. **Service interface** (nếu chưa có)
4. **Service implementation** — business logic ở đây
5. **Controller** — chỉ validate + gọi service, dùng ResponseEntity
## Bước 3: Cập nhật memory
- Cập nhật `.ai-memory/02_project_map.md`
- Cập nhật/tạo file `03_deep_knowledge/`
- Ghi log `06_evolution_log.md`
## Response format thống nhất
```json
{ "data": {}, "message": "Thành công", "status": 200, "timestamp": "..." }
```
 
## Error format thống nhất
```json
{ "code": "RESOURCE_NOT_FOUND", "message": "Không tìm thấy", "details": [], "timestamp": "..." }
```