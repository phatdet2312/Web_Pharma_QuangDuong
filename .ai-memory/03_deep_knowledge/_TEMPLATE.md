# [Tên Module / Domain] - VÍ DỤ MẪU
> Last updated: YYYY-MM-DD
> Source files: [đường dẫn đến file chính của module, VD: src/user/service.py, handlers/user.go]
> Confidence: HIGH | MEDIUM | LOW
 
## Mô tả chức năng
<!-- 2-3 câu tóm tắt module này làm gì -->
 
## Luồng xử lý chính
<!-- Mô tả flow chính, KHÔNG copy code, chỉ tóm tắt logic -->
1. Client gọi API `POST /api/xxx`
2. Handler/Controller validate input
3. Service/Logic layer kiểm tra [điều kiện gì]
4. Data layer lưu/truy vấn [bảng/collection nào]
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
 
## Decision Log
<!-- CHỈ ghi khi có 2+ phương án được cân nhắc -->
<!-- KHÔNG ghi convention (đã có trong rules/) -->
<!-- Agent có Write tự ghi, agent không có Write trả output để memory-keeper ghi -->
<!-- HALF-LIFE: Mỗi decision có Ngày ghi + Hết hạn (mặc định +3 tháng).
     Agent gặp tình huống tương tự PHẢI check "Hết hạn" trước khi áp dụng.
     Nếu quá hạn → BUỘC re-evaluate, hỏi user "Decision X đã hết hạn YYYY-MM-DD.
     Muốn re-evaluate hay extend?" -->

| Quyết định | Phương án (✅ chọn / ❌ bỏ) | Lý do | Ngày ghi | Hết hạn   | Dead End |
|-----------|---------------------------|-------|----------|-----------|----------|
|           |                           |       | YYYY-MM-DD | YYYY-MM-DD |          |
 
## Ghi chú
<!-- Edge case, known issue, hoặc đặc biệt cần lưu ý -->