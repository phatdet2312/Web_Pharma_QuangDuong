---
name: create-api
description: Tạo API endpoint đầy đủ (request DTO + response DTO + service + handler/controller) theo convention dự án. Auto-trigger khi user nói "tạo API", "thêm endpoint", "viết handler/controller", "thêm REST".
---

# Quy trình tạo API Endpoint

## Bước 0: Đọc convention
- Đọc "Project Convention" trong `.ai-memory/01_system_architecture.md`
- Xác định: framework, kiến trúc phân lớp, validation pattern, response format

## Bước 1: Xác định thông tin
- Method: GET / POST / PUT / DELETE
- Path: /api/v1/[resource]
- Request body: tạo request DTO/schema/model
- Response body: tạo response DTO/schema/model

## Bước 2: Spawn subagent thực hiện (thứ tự)
Gọi `/agent` để spawn từng subagent:

1. **`api-designer`** (medium tier) — Thiết kế request/response model với validation
2. **`implementer`** (medium tier) — Code Service/Logic layer (business logic)
3. **`implementer`** — Code Handler/Controller/Router (chỉ validate + gọi service)

## Bước 3: Cập nhật memory
Spawn `memory-keeper` để:
- Cập nhật `.ai-memory/02_project_map.md`
- Cập nhật/tạo file `.ai-memory/03_deep_knowledge/<module>.md`
- Ghi log `06_evolution_log.md`

## Bước 4: Self-Eval (BẮT BUỘC)
- Chạy $self-eval để compile + test + convention check
- Pass → memory-keeper sync
- Fail → eval loop max 3 vòng

## Response format thống nhất
Tuân thủ "Project Convention". Nếu chưa có, đề xuất:
```json
{ "data": {}, "message": "Thành công", "status": 200, "timestamp": "..." }
```

## Error format thống nhất
Tuân thủ "Project Convention". Nếu chưa có, đề xuất:
```json
{ "code": "RESOURCE_NOT_FOUND", "message": "Không tìm thấy", "details": [], "timestamp": "..." }
```
