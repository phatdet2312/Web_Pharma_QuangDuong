# API Specification
> Last updated: YYYY-MM-DD
> Author: [Tên developer]
> Status: TEMPLATE — Agent KHÔNG sử dụng file này cho đến khi Status đổi thành ACTIVE
 
## Base URL
```
Development: http://localhost:8080/api/v1
Staging:     https://staging.example.com/api/v1
Production:  https://api.example.com/api/v1
```
 
## Authentication
<!-- Mô tả cơ chế auth: JWT / OAuth2 / API Key -->
<!-- Header format, token lifetime, refresh flow -->
 
## Common Response Format
```json
{
  "data": {},
  "message": "Thành công",
  "status": 200,
  "timestamp": "2026-01-01T00:00:00Z"
}
```
 
## Common Error Format
```json
{
  "code": "ERROR_CODE",
  "message": "Mô tả lỗi",
  "details": [],
  "timestamp": "2026-01-01T00:00:00Z"
}
```
 
## Error Codes
<!-- Liệt kê tất cả error code của dự án -->
| Code                    | HTTP Status | Mô tả                    |
|-------------------------|-------------|--------------------------|
| RESOURCE_NOT_FOUND      | 404         | Không tìm thấy tài nguyên |
| VALIDATION_ERROR        | 400         | Dữ liệu input không hợp lệ|
| UNAUTHORIZED            | 401         | Chưa đăng nhập            |
| FORBIDDEN               | 403         | Không có quyền             |
| INTERNAL_ERROR          | 500         | Lỗi server                |
 
## Endpoints
<!-- Agent đọc phần này khi cần biết chi tiết request/response -->
<!-- Mỗi endpoint ghi rõ: method, path, headers, request body, response body, error cases -->
 
### [Module Name]
 
#### POST /api/v1/[resource]
**Mô tả:** [Chức năng]
**Auth:** Required / Public
**Request Body:**
```json
{
  "field1": "string (required)",
  "field2": "number (optional, default: 0)"
}
```
**Response 200:**
```json
{
  "data": { "id": 1, "field1": "value" },
  "message": "Tạo thành công"
}
```
**Errors:** VALIDATION_ERROR, UNAUTHORIZED