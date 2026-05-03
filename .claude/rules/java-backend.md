---
paths:
  - "src/main/java/**/*.java"
  - "src/test/java/**/*.java"
---
 
# Java Backend Rules
 
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