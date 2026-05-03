# [Tên Module / Domain] - VÍ DỤ MẪU
> Last updated: YYYY-MM-DD
> Source files: src/main/java/com/.../XxxController.java, .../XxxService.java
> Confidence: HIGH | MEDIUM | LOW
 
## Mô tả chức năng
<!-- 2-3 câu tóm tắt module này làm gì -->
 
## Luồng xử lý chính
<!-- Mô tả flow chính, KHÔNG copy code, chỉ tóm tắt logic -->
1. Client gọi API `POST /api/xxx`
2. Controller validate input bằng @Valid
3. Service kiểm tra [điều kiện gì]
4. Repository lưu/truy vấn [bảng nào]
5. Trả về [response gì]
## Business Rules quan trọng
<!-- Những rule mà agent CẦN NHỚ khi sửa code liên quan -->
- Rule 1: ...
- Rule 2: ...
## API Endpoints
| Method | Path           | Mô tả          | Auth |
|--------|---------------|-----------------|------|
| POST   | /api/xxx      |                 | Yes  |
| GET    | /api/xxx/{id} |                 | No   |
 
## Ghi chú
<!-- Edge case, known issue, hoặc quyết định thiết kế đặc biệt -->
 