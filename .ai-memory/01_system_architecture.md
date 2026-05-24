# System Architecture
> Last updated: 2026-05-25
> Status: BOOTSTRAPPED

## Tech Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf | Spring Boot 4.0.2 |
| Database | Microsoft SQL Server via `mssql-jdbc`, Hibernate SQL Server dialect | Driver managed by Spring Boot BOM |
| Cache | Chưa phát hiện cache chuyên dụng | N/A |
| Auth | Spring Security, JWT cookie, Google OAuth2 Client, dynamic roles/permissions | `jjwt` 0.12.6 |
| Frontend | Thymeleaf templates, Bootstrap, jQuery, custom static JS/CSS | Server-rendered + JSON API |
| Build Tool | Maven wrapper | Maven via `mvnw` / `mvnw.cmd` |
| Mail | Spring Mail + Gmail SMTP | Config in `application.properties` |
| Payment | VNPay integration classes and config keys | Internal package `config/vnpay` |

## Project Type

- **Type:** web-fullstack
- **Đặc điểm:** Spring Boot modular monolith: cùng repo chứa backend API JSON, Spring Security, JPA entities/repositories, Thymeleaf HTML views, static JS/CSS, SQL Server schema mapping.
- **Evidence:** Có `pom.xml`, `src/main/java/.../controller/api`, `src/main/java/.../controller/view`, `src/main/resources/templates`, `src/main/resources/static`, `src/main/resources/application.properties`.

## Kiến trúc tổng quan

Ứng dụng là modular monolith Spring Boot. Luồng thường gặp:

1. Browser mở route view như `/posts`, `/events`, `/admin/...`.
2. `controller.view` trả về Thymeleaf template.
3. JavaScript trong template gọi JSON API dưới `/api/...`.
4. `controller.api` nhận request, validate DTO bằng Jakarta Validation, trả `ApiResponse<T>`.
5. Service interface trong `service.itf` được implement ở `service.impl`; service chứa business rule, mapping DTO thủ công và transaction.
6. `service.support` chứa policy/helper dùng chung giữa service (VD: `NguCanhNguoiDung`, `EventStatusDisplayPolicy`); không truy cập DB trực tiếp.
7. Repository trong `repositories.IRepository` dùng Spring Data JPA, derived query hoặc JPQL `@Query`.
8. Entity trong `entities` ánh xạ bảng SQL Server bằng JPA annotation.

Security flow:

1. `SecurityConfig` đăng ký `SecurityFilterChain`, custom security configurer/filter, OAuth2 login, JWT cookie và authorization matcher.
2. Local login: `ApiAuthController` dùng `AuthenticationManager`, tạo JWT bằng `JwtService`, ghi cookie bằng `CookieUtils`.
3. Google OAuth2: success handler tìm user DB, tạo JWT cookie, redirect về `/`.
4. `User` implements `UserDetails`; authorities được tạo động từ `danhSachTenRole` và `danhSachTenPermission`.

## Ports & Endpoints

| Service | Port | Base URL |
|---------|------|----------|
| Backend API | 8080 mặc định Spring Boot (không thấy `server.port`) | `http://localhost:8080/api` |
| Frontend | 8080 cùng Spring Boot process | `http://localhost:8080/` |
| Database | SQL Server local instance; port không khai báo trực tiếp | `jdbc:sqlserver://LAPTOP-S1U5MI7D\\SQLEXPRESS;databaseName=Web_Pharma_QuangDuong;...` |

## External Dependencies

- Microsoft SQL Server: datasource trong `src/main/resources/application.properties`.
- Gmail SMTP: `spring.mail.*` keys có trong config. Không đọc/paste raw password.
- Google OAuth2 Client: `spring.security.oauth2.client.registration.google.*` keys có trong config. Không đọc/paste raw client secret.
- VNPay: package `config/vnpay` và keys `vnpay.*` trong config. Không đọc/paste raw hash secret.
- Custom internal security library: `config/ultraSecureLibrary`.

## Cách chạy dự án

```powershell
# Build
.\mvnw.cmd package

# Chạy dev
.\mvnw.cmd spring-boot:run

# Test
.\mvnw.cmd test

# Lint/typecheck
# Chưa phát hiện linter riêng; dùng Maven compile/test làm kiểm tra chính.
.\mvnw.cmd test
```

```bash
# Unix tương đương
./mvnw package
./mvnw spring-boot:run
./mvnw test
```

## Project Convention

### Kiến trúc phân lớp

- API JSON: `controller.api` -> `service.itf` interface -> `service.impl` implementation -> `repositories.IRepository` -> `entities`.
- View route: `controller.view` chỉ trả template name, không chứa business logic hay truy vấn DB.
- DTO tách `dto.request` và `dto.response`; response API bọc bằng `ApiResponse<T>`.
- Transaction đặt tại service method có ghi dữ liệu hoặc quy trình nhiều bước (`@Transactional`).
- Mapping entity sang DTO đang làm thủ công bằng vòng lặp và setter, chưa dùng MapStruct/ModelMapper.
- Repository dùng interface prefix `I`, ví dụ `IPostRepository`, `IUserRepository`; service interface cũng dùng prefix `I`, ví dụ `IPostService`.

### Naming Convention

- Java class/interface: PascalCase.
- Method/field/local variable: camelCase; dự án có nhiều tên tiếng Việt không dấu/tiếng Việt camelCase như `layChiTietBaiViet`, `timKiemSuKien`.
- Entity table/column mapping: tên bảng/cột SQL Server uppercase snake/camel theo DB, ví dụ `POSTS`, `CT_EVENTS`, `USER_ROLES`.
- Controller API đặt prefix `Api...Controller`; view controller đặt theo domain như `PostViewController`.
- Admin API dùng path `/api/admin/...`; public API dùng `/api/posts`, `/api/events`, `/api/comments`, `/api/auth`.

### Error Handling

- Business error ném `AppException(status, message)`.
- `GlobalExceptionHandler` dùng `@RestControllerAdvice` để chuyển exception thành `ApiResponse.loi(...)`.
- Validation error `MethodArgumentNotValidException` được gom field error thành message HTTP 400.
- Runtime/Exception fallback trả HTTP 500; hiện có `System.err`/`printStackTrace`, chưa thấy logging framework wrapper.

### Test Framework & Pattern

- Test framework hiện tại: JUnit 5 + Spring Boot Test.
- Test hiện có: `PhatdevPharmaceuticalsWebApplicationTests.contextLoads()`.
- Chưa thấy unit test/service/controller test chuyên sâu; khi sửa logic critical path nên thêm test tập trung hoặc ít nhất chạy `.\mvnw.cmd test`.

### Validation

- Controller API thường dùng `@Valid @RequestBody` cho request DTO public/auth/profile/comment/event/post.
- DTO request dùng Jakarta Validation như `@NotBlank`, `@NotNull`, `@Size`, `@Email`, custom `@ValidUsername`.
- Một số admin endpoint nhận `@RequestBody` chưa có `@Valid`; khi thêm/sửa endpoint mới phải validate toàn bộ input từ client.

### Code Doc Style

- Code có nhiều Javadoc/block comment tiếng Việt mô tả nhiệm vụ file, flow và business rule.
- Comment được dùng để giải thích domain/security rule; khi thêm comment mới, giữ ngắn và tập trung vào rule khó hiểu.

## Architecture Decisions

Chưa ghi decision cross-cutting mới trong bootstrap. Các mục trên là convention quan sát từ code hiện tại, không phải decision đã cân nhắc giữa nhiều phương án.

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn |
|-----------|------------------------|-------|----------|---------|
| Chưa có | N/A | N/A | 2026-05-18 | N/A |

## Ghi chú quan trọng

- Phát hiện 51 file đã thay đổi ngoài Codex trước bootstrap; chủ yếu là chuyển bộ agent từ `.claude` sang `.codex/.agents`, cập nhật README/docs và memory template. Không revert các thay đổi này.
- `application.properties` chứa nhiều key có thể liên quan secret (`password`, `secret`, `token`, `key`); chỉ đọc targeted key và phải mask raw value khi báo cáo.
- Auth, role/permission, event registration, payment và migration/schema là critical path; cần review sâu hoặc security review khi sửa lớn.
- `SecurityConfig` hiện có hardcoded remember-me key dạng literal; khi sửa auth/security cần xử lý như debt bảo mật, không nhân rộng pattern này.
