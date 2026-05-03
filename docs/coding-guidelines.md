# Coding Guidelines (Chi tiết)
> Last updated: YYYY-MM-DD
> Author: [Tên developer]
> Status: TEMPLATE — Agent KHÔNG sử dụng file này cho đến khi Status đổi thành ACTIVE
 
File này chứa quy tắc coding MỞ RỘNG — chi tiết hơn .claude/rules/.
Agent đọc file này khi cần hiểu sâu về convention, KHÔNG load mỗi phiên.
 
## Java / Spring Boot
 
### Package Structure
```
com.myapp/
├── config/           # @Configuration, Security, CORS, Swagger
├── common/           # Shared utilities, constants, base classes
│   ├── exception/    # Custom exceptions + GlobalExceptionHandler
│   ├── dto/          # Common DTOs (ApiResponse, PageResponse)
│   └── util/         # Helper classes
├── [domain]/         # Mỗi domain 1 package
│   ├── controller/   # @RestController
│   ├── service/      # Interface + Implementation
│   ├── repository/   # JpaRepository
│   ├── entity/       # @Entity
│   ├── dto/          # Request/Response DTOs cho domain này
│   └── mapper/       # Entity ↔ DTO mapping
└── Application.java  # @SpringBootApplication
```
 
### Service Layer Patterns
- Interface + Implementation (UserService + UserServiceImpl)
- @Transactional ở method level, KHÔNG ở class level
- Validate business logic ở Service, KHÔNG ở Controller
- Throw custom exception, KHÔNG return null
### Repository Patterns
- Dùng Spring Data JPA method naming: findByEmailAndIsActive()
- Query phức tạp dùng @Query với JPQL
- Native query CHỈ khi JPQL không đủ
- Pagination: Pageable parameter, trả về Page<Entity>
### DTO Patterns
- Request DTO: @Valid + constraint annotations
- Response DTO: KHÔNG expose Entity trực tiếp
- Mapping: dùng MapStruct hoặc manual mapping method
- KHÔNG dùng Entity trong Controller request/response
### Exception Handling
```java
// Custom exception
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
 
// Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        // return 404 with standard error format
    }
}
```
 
### Logging
- SLF4J + Logback
- Log level: ERROR (bug), WARN (unexpected nhưng xử lý được), INFO (business event), DEBUG (dev only)
- KHÔNG log sensitive data (password, token, credit card)
- Log format: timestamp | level | class | message | context
## Frontend (nếu có)
<!-- Thêm quy tắc frontend chi tiết ở đây -->
 
## Testing
- Unit test: JUnit 5 + Mockito
- Naming: should_[expected]_when_[condition]
- Mỗi test method test 1 behavior
- AAA pattern: Arrange → Act → Assert
- Mock external dependencies, test behavior không test implementation
- Coverage target: >80% cho Service layer
 