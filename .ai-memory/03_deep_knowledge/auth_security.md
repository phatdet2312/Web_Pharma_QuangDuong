# Auth & Security
> Last updated: 2026-06-04
> Source files: `utils/SecurityConfig.java`, `controller/api/ApiAuthController.java`, `entities/User.java`, `config/ultraSecureLibrary/Service/JwtService.java`, `config/ultraSecureLibrary/Service/CookieUtils.java`, `config/ultraSecureLibrary/Model/SecurityAuthoritySnapshot.java`, `config/ultraSecureLibrary/Model/SecurityTokenClaim.java`, `config/ultraSecureLibrary/Model/SecurityTokenVersion.java`, `config/ultraSecureLibrary/Filter/JwtAuthenticationFilter.java`, `config/ultraSecureLibrary/Filter/DynamicRoleFilter.java`, `config/ultraSecureLibrary/Adapter/ISecurityUserAdapter.java`, `adapter/UserSecurityAdapter.java`, `config/rbac/RbacSecuritySnapshot.java`, `config/interceptor/PermissionInterceptor.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `service/impl/UserServiceImpl.java`
> Confidence: HIGH

## Summary

Authentication uses Spring Security with stateless JWT cookie auth and Google OAuth2 login. Authorization is DB-driven at login/refresh/mutation time. The app packages RBAC state into typed `RbacSecuritySnapshot`; `ultraSecureLibrary` only receives neutral authorities, typed extra claims and an opaque security fingerprint. `PermissionInterceptor` enforces `@RequirePermission` from token/request authorities and avoids a DB query on each request when `roleLevel` is present.

## Main Flow

1. Local login calls `POST /api/auth/login`.
2. `ApiAuthController` validates `LoginRequest`, authenticates through `AuthenticationManager`, gets the current `User`, wraps it in `UserSecurityAdapter`, generates a JWT with `JwtService`, and writes it through `CookieUtils`.
3. Google OAuth2 uses `CustomOidcUserService`; success handler finds the DB user, rejects locked accounts, creates JWT cookie and redirects to `/`.
4. `SecurityConfig` permits public pages/APIs and requires authentication for `/admin/**`, `/api/admin/**`, `/api/profile/**`, and remaining `/api/**`.
5. `JwtAuthenticationFilter` first reads neutral `securityAuthorities`; for old tokens it falls back to legacy `roles` and converts them to `ROLE_*`.
6. `DynamicRoleFilter` reuses `JWT_CLAIMS`, rebuilds DNA from the opaque `securityFingerprint` and refreshes token only when old/missing fingerprint, user flag, cluster mismatch or snapshot drift requires DB recheck.
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
- `ISecurityUserAdapter` keeps legacy `layDanhSachQuyen()` and adds typed `layAnhChupBaoMat()`; the library interface no longer exposes app-specific `roleLevel`, blacklist or permission-list methods.
- `UserServiceImpl.napQuyenChoNguoiDung()` loads roles, effective permissions, blacklisted permissions and strongest role level into transient `User` fields.
- `RbacSecuritySnapshot` canonicalizes/sorts/deduplicates app RBAC lists; `SecurityTokenVersion` in the library treats the resulting fingerprint as opaque.
- SUPERADMIN bypass is backend `roleLevel == 0`, read from request attr/JWT when available, not role name or frontend flag.
- `PermissionInterceptor` returns a generic 403 message and does not expose the internal permission code in the response.
- Methods without `@RequirePermission` continue to work after authentication or public route matching.

## Dynamic JWT RBAC Snapshot

- JWT library contract now has neutral technical claims: `securityAuthorities`, `securityFingerprint`, `securityExposedAttributes`; app-level typed claims may still include `roles`, `permissions`, `roleLevel`, `JWT_ROLE_LEVEL`, `blacklist`.
- Legacy aggregate role behavior is preserved through default `ISecurityUserAdapter.layAnhChupBaoMat()` which converts `layDanhSachQuyen()` to `ROLE_*` authorities.
- `JwtService.generateToken()` writes neutral authorities/fingerprint plus typed extra claims from `SecurityAuthoritySnapshot`; it computes DNA with `SecurityTokenVersion.taoDna(userId, fingerprint, v_adn)`.
- `RbacSecuritySnapshot` builds the RBAC fingerprint as `roles=...|permissions=...|roleLevel=...|blacklist=...`, matching the old dynamic DNA core while keeping that knowledge outside `ultraSecureLibrary`.
- `JwtService.generateTrojanSyncToken()` writes a neutral sync authority and fingerprint; sync tokens remain inter-server commands, not user/superadmin sessions.
- `JwtAuthenticationFilter` exposes only claim names listed in `securityExposedAttributes`; for app RBAC this includes `roleLevel` and `JWT_ROLE_LEVEL`.
- `DynamicRoleFilter` treats tokens missing `securityFingerprint` as old tokens and refreshes them through the existing self-healing path; legacy commit-8 `roles` DNA is still checked against the graveyard.
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
| JWT security state uses neutral typed snapshot | Store `securityAuthorities`, `securityFingerprint`, `securityExposedAttributes`; app may add typed RBAC claims | Enables no-DB permission checks per request while keeping RBAC semantics outside library core | 2026-06-04 | 2026-12-04 |
| Sync token is not an RBAC actor | Use neutral sync authority/fingerprint without app `roleLevel` claims | Prevents a sync token from being interpreted as SUPERADMIN if it escapes the intended sync flow | 2026-06-04 | 2026-12-04 |
| Keep `ultraSecureLibrary` RBAC-neutral | Move RBAC snapshot construction to app package `config/rbac` and pass typed neutral snapshot into the library | Preserves library reuse across future Java projects while keeping dynamic RBAC behavior in this app | 2026-06-04 | 2026-12-04 |
