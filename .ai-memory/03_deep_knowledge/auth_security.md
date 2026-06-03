# Auth & Security
> Last updated: 2026-06-03
> Source files: `utils/SecurityConfig.java`, `controller/api/ApiAuthController.java`, `entities/User.java`, `config/ultraSecureLibrary/Service/JwtService.java`, `config/ultraSecureLibrary/Service/CookieUtils.java`, `adapter/UserSecurityAdapter.java`, `config/interceptor/PermissionInterceptor.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `service/impl/UserServiceImpl.java`
> Confidence: HIGH

## Summary

Authentication uses Spring Security with stateless JWT cookie auth and Google OAuth2 login. Authorization is DB-driven: roles and permissions are loaded from RBAC tables, exposed as authorities, and enforced by `PermissionInterceptor` when a controller/view method has `@RequirePermission`.

## Main Flow

1. Local login calls `POST /api/auth/login`.
2. `ApiAuthController` validates `LoginRequest`, authenticates through `AuthenticationManager`, gets the current `User`, wraps it in `UserSecurityAdapter`, generates a JWT with `JwtService`, and writes it through `CookieUtils`.
3. Google OAuth2 uses `CustomOidcUserService`; success handler finds the DB user, rejects locked accounts, creates JWT cookie and redirects to `/`.
4. `SecurityConfig` permits public pages/APIs and requires authentication for `/admin/**`, `/api/admin/**`, `/api/profile/**`, and remaining `/api/**`.
5. Fine-grained permission checks happen in `PermissionInterceptor`, not in Spring Security `hasRole` matchers.

## Public/Auth Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/login` | Local login and JWT cookie issue | No |
| POST | `/api/auth/register/send-otp` | Store temp registration and send OTP | No |
| POST | `/api/auth/register/verify-otp` | Verify OTP and create local user | No |
| POST | `/api/auth/forgot-password/send-otp` | Send reset OTP | No |
| POST | `/api/auth/forgot-password/verify-otp` | Verify reset OTP | No |
| POST | `/api/auth/reset-password` | Reset password through session reset token | No |
| GET | `/oauth2/**` | Google OAuth2 flow | No |

## Security Rules

- `SessionCreationPolicy.STATELESS`; CSRF disabled.
- Public routes include `/`, `/home`, static assets, auth pages, `/oauth2/**`, `/api/auth/**`, `/posts/**`, `/events/**`.
- Public content APIs include `/api/posts/**`, `/api/events/**`, `/api/comments/posts/**`, `/api/comments/events/**`, `/api/comments/reaction-types`.
- Admin/API/profile routes are authenticated; method-level permission codes decide actual operation access.
- `User.getAuthorities()` adds `ROLE_` prefix to roles and raw permission strings for permission checks.
- `UserServiceImpl.napQuyenChoNguoiDung()` loads role/permission mappings and removes `CT_USER_PERMISSION_BLACKLIST` permissions in batch.
- SUPERADMIN bypass is backend `roleLevel == 0`, not role name or frontend flag.
- `PermissionInterceptor` returns a generic 403 message and does not expose the internal permission code in the response.
- Methods without `@RequirePermission` continue to work after authentication or public route matching.

## Permission Enforcement

- Interceptor paths: `/admin/**`, `/api/admin/**`, `/api/comments/**`, `/api/reports/**`, `/api/events/**`, `/api/posts/**`.
- Current code has 126 `@RequirePermission` annotations under controllers/views.
- `PermissionRegistry` has 44 permission codes and is used by `/api/admin/role-management/system-permissions` plus `DatabaseSeeder`.
- `ROLE_MANAGE` is absent from `src/main/java`, templates and static JS.

## Risks / Notes

- `application.properties` contains secret-bearing settings. Inspect by property name or masked value only.
- `SecurityConfig` still has a hardcoded remember-me secret literal; treat as security debt.
- OAuth2 writes a `temp_secret` cookie readable by JavaScript for 60 seconds as documented in code.
- Auth/RBAC changes require security review.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Permission enforcement moved to `PermissionInterceptor` | Spring Security only authenticates broad route groups | Keeps RBAC database-driven and granular | 2026-06-03 | 2026-09-03 |
| SUPERADMIN detection uses backend `roleLevel == 0` | Reject name/authority/frontend-only bypass | Prevents privilege drift | 2026-06-03 | 2026-09-03 |
