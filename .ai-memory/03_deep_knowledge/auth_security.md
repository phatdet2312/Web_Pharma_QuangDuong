# Auth & Security
> Last updated: 2026-05-18
> Source files: `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/SecurityConfig.java`, `controller/api/ApiAuthController.java`, `entities/User.java`, `config/ultraSecureLibrary/Service/JwtService.java`, `config/ultraSecureLibrary/Service/CookieUtils.java`, `adapter/UserSecurityAdapter.java`
> Confidence: HIGH

## Mô tả chức năng

Module xác thực/ủy quyền dùng Spring Security với JWT cookie, OAuth2 Google và hệ thống role/permission động. `User` implements `UserDetails`; quyền được nạp từ danh sách transient role/permission thay vì enum cố định.

## Luồng xử lý chính

1. Client gọi `POST /api/auth/login` với `LoginRequest`.
2. `ApiAuthController` validate bằng `@Valid`, gọi `AuthenticationManager`.
3. Sau khi authenticate, controller lấy `User`, bọc bằng `UserSecurityAdapter`, tạo `clientSecret` random và JWT qua `JwtService`.
4. `CookieUtils` ghi JWT vào cookie; response trả `ApiResponse<Map<String,Object>>` gồm token/clientSecret/redirectUrl.
5. OAuth2 Google chạy qua `SecurityConfig.oauth2Login`, `CustomOidcUserService`, success handler tạo JWT cookie và redirect về `/`.
6. Authorization matcher trong `SecurityConfig` phân nhóm public route, admin route, profile route và `/api/**` authenticated route.

## Business Rules quan trọng

- `SecurityConfig` đang cấu hình `SessionCreationPolicy.STATELESS` và `csrf().disable()`.
- Public routes gồm `/`, `/home`, static `/css/**`, `/js/**`, `/images/**`, auth pages, `/api/auth/**`, `/posts/**`, `/events/**`, một số API public posts/events/comments.
- Admin APIs `/api/admin/**` yêu cầu role `ADMIN` hoặc `SUPERADMIN`; trang admin rộng hơn cho `EMPLOYEE`, `ADMIN`, `SUPERADMIN`.
- `User.getAuthorities()` thêm prefix `ROLE_` cho role, permission giữ nguyên string.
- Lỗi xác thực nghiệp vụ ném `AppException` với status 401/403/400.
- Không hardcode secret/key mới. `application.properties` chứa secret-bearing keys; khi kiểm tra config phải đọc targeted key và mask value.
- Auth/security là critical path; sửa lớn cần security review.

## API Endpoints

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| POST | `/api/auth/login` | Local login, tạo JWT cookie | No |
| POST | `/api/auth/register/send-otp` | Lưu đăng ký tạm vào session, gửi OTP | No |
| POST | `/api/auth/register/verify-otp` | Xác minh OTP, tạo user local | No |
| POST | `/api/auth/forgot-password/send-otp` | Gửi OTP reset password | No |
| POST | `/api/auth/forgot-password/verify-otp` | Xác minh OTP reset | No |
| POST | `/api/auth/reset-password` | Đổi mật khẩu bằng session reset token | No |
| GET | `/oauth2/**` | Google OAuth2 flow | No |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |

## Ghi chú

- `SecurityConfig` có hardcoded remember-me key literal; không nhân rộng pattern này.
- `temp_secret` cookie OAuth2 sống 60 giây và JavaScript có thể đọc theo comment trong code.
- User lock được check trong OAuth2 success handler trước khi cấp JWT.
