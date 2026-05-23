# Backend Rules

Khi sửa code backend, tuân thủ convention trong "Project Convention" của `.ai-memory/01_system_architecture.md`.

Quy tắc chung (mọi ngôn ngữ):
- Tầng trên KHÔNG chứa business logic, chỉ validate + gọi tầng dưới
- Tầng data chỉ chứa query, KHÔNG gọi trực tiếp từ tầng entry
- Xử lý lỗi tập trung (global error handler / middleware)
- Response lỗi format thống nhất
- KHÔNG bắt lỗi im lặng, chỉ catch khi có xử lý cụ thể
- Mỗi function/method không quá 30 dòng logic

# ví dụ: Java Backend Rules
- Class: PascalCase, Method/Variable: camelCase, Constant: UPPER_SNAKE_CASE
- Package: lowercase theo domain (com.myapp.user.service)
- DTO: hậu tố Dto/Request/Response, Entity: không hậu tố
- Controller KHÔNG chứa business logic, chỉ validate + gọi Service
- Repository chỉ chứa query, KHÔNG gọi trực tiếp từ Controller
- Custom Exception + GlobalExceptionHandler (@RestControllerAdvice)
- Response lỗi: { code, message, details, timestamp }
- KHÔNG try-catch bừa bãi, chỉ catch khi có xử lý cụ thể
- KHÔNG wildcard import (java.util.*)
- Mỗi method không quá 30 dòng logic
