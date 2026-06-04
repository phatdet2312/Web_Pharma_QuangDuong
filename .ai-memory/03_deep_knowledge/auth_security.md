# Auth & Security
> Last updated: 2026-06-04
> Source files: `utils/SecurityConfig.java`, `controller/api/ApiAuthController.java`, `entities/User.java`, `config/ultraSecureLibrary/Service/JwtService.java`, `config/ultraSecureLibrary/Service/CookieUtils.java`, `config/ultraSecureLibrary/Service/PhienBanPhanQuyenBaoMat.java`, `config/ultraSecureLibrary/Filter/JwtAuthenticationFilter.java`, `config/ultraSecureLibrary/Filter/DynamicRoleFilter.java`, `config/ultraSecureLibrary/Adapter/ISecurityUserAdapter.java`, `adapter/UserSecurityAdapter.java`, `config/interceptor/PermissionInterceptor.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `service/impl/UserServiceImpl.java`
> Confidence: HIGH

## Summary

Authentication uses Spring Security with stateless JWT cookie auth and Google OAuth2 login. Authorization is DB-driven at login/refresh/mutation time, then carried in JWT as a canonical 4-part RBAC snapshot: `roles`, `permissions`, `roleLevel`, `blacklist`. `PermissionInterceptor` enforces `@RequirePermission` from token/request authorities and avoids a DB query on each request when `roleLevel` is present.

## Main Flow

1. Local login calls `POST /api/auth/login`.
2. `ApiAuthController` validates `LoginRequest`, authenticates through `AuthenticationManager`, gets the current `User`, wraps it in `UserSecurityAdapter`, generates a JWT with `JwtService`, and writes it through `CookieUtils`.
3. Google OAuth2 uses `CustomOidcUserService`; success handler finds the DB user, rejects locked accounts, creates JWT cookie and redirects to `/`.
4. `SecurityConfig` permits public pages/APIs and requires authentication for `/admin/**`, `/api/admin/**`, `/api/profile/**`, and remaining `/api/**`.
5. `JwtAuthenticationFilter` reads snapshot claims and creates authorities: roles get `ROLE_` prefix, permissions stay raw.
6. `DynamicRoleFilter` reuses `JWT_CLAIMS`, rebuilds DNA from all four snapshot parts and refreshes token only when old/missing snapshot, user flag, cluster mismatch or RBAC snapshot drift requires DB recheck.
7. Fine-grained permission checks happen in `PermissionInterceptor`, not in Spring Security `hasRole` matchers.

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
- `User.getAuthorities()` and `JwtAuthenticationFilter` add `ROLE_` prefix to roles and raw permission strings for permission checks.
- `ISecurityUserAdapter` keeps legacy `layDanhSachQuyen()` and adds four canonical parts through `layDanhSachChucVu()`, `layDanhSachQuyenThaoTac()`, `layCapBacQuyenLuc()`, `layDanhSachQuyenBiChan()`.
- `UserServiceImpl.napQuyenChoNguoiDung()` loads roles, effective permissions, blacklisted permissions and strongest role level into transient `User` fields.
- `PhienBanPhanQuyenBaoMat` canonicalizes/sorts/deduplicates snapshot lists so JWT claims and DNA do not depend on DB query order.
- SUPERADMIN bypass is backend `roleLevel == 0`, read from request attr/JWT when available, not role name or frontend flag.
- `PermissionInterceptor` returns a generic 403 message and does not expose the internal permission code in the response.
- Methods without `@RequirePermission` continue to work after authentication or public route matching.

## Dynamic JWT RBAC Snapshot

- JWT claim contract now has 4 RBAC parts: `roles`, `permissions`, `roleLevel`, `blacklist`.
- Legacy aggregate role/permission behavior is preserved through `ISecurityUserAdapter.layDanhSachQuyen()`.
- `JwtService.generateToken()` writes all 4 claims and computes permission DNA with `PhienBanPhanQuyenBaoMat.taoDna(userId, roles, permissions, roleLevel, blacklist, v_adn)`.
- `JwtService.generateTrojanSyncToken()` also writes all 4 claims for sync tokens, but uses `roleLevel = 999` because sync tokens are inter-server commands, not user/superadmin sessions.
- `JwtAuthenticationFilter` places `roles`, `permissions` and `JWT_ROLE_LEVEL` into request attributes for downstream authorization.
- `DynamicRoleFilter` treats tokens missing any canonical snapshot part as old tokens and refreshes them through the existing self-healing path.
- Existing body hash, replay protection, graveyard, user flag and P2P sync mechanisms remain in the same filter chain; no new filter was added.
- Sync-token authority must remain non-privileged. Inter-server sync trust is based on `sync_payload`, HMAC and replay guard, not on `roleLevel == 0`.

## Permission Enforcement

- Interceptor paths: `/admin/**`, `/api/admin/**`, `/api/comments/**`, `/api/reports/**`, `/api/events/**`, `/api/posts/**`.
- Current code has 126 `@RequirePermission` annotations under controllers/views.
- `PermissionRegistry` has 44 permission codes and is used by `/api/admin/role-management/system-permissions` plus `DatabaseSeeder`.
- `ROLE_MANAGE` is absent from `src/main/java`, templates and static JS.
- `PermissionInterceptor` first reads `JWT_ROLE_LEVEL` from request attributes. It only falls back to `userService.getCurrentAuthenticatedUser()` / DB-derived role level when the attribute is absent.

## Risks / Notes

- `application.properties` contains secret-bearing settings. Inspect by property name or masked value only.
- `SecurityConfig` still has a hardcoded remember-me secret literal; treat as security debt.
- OAuth2 writes a `temp_secret` cookie readable by JavaScript for 60 seconds as documented in code.
- Auth/RBAC changes require security review.
- Verification for the 2026-06-04 RBAC snapshot upgrade: `git diff --check` passed. `bash mvnw -q -DskipTests compile` remains `BLOCKED_BY_NETWORK` in sandbox and escalated runs because Maven could not download the parent POM from Maven Central due to `Permission denied: getsockopt`.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Permission enforcement moved to `PermissionInterceptor` | Spring Security only authenticates broad route groups | Keeps RBAC database-driven and granular | 2026-06-03 | 2026-09-03 |
| SUPERADMIN detection uses backend `roleLevel == 0` | Reject name/authority/frontend-only bypass | Prevents privilege drift | 2026-06-03 | 2026-09-03 |
| JWT RBAC state uses 4-part canonical snapshot | Store `roles`, `permissions`, `roleLevel`, `blacklist` instead of one aggregate roles list | Enables no-DB permission checks per request while preserving dynamic invalidation and token refresh | 2026-06-04 | 2026-09-04 |
| Sync token `roleLevel` is non-privileged | Use `roleLevel = 999` for `generateTrojanSyncToken()` instead of `0` | Prevents a sync token from being interpreted as SUPERADMIN if it escapes the intended sync flow | 2026-06-04 | 2026-09-04 |
