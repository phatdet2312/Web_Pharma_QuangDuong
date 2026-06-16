# System Architecture
> Last updated: 2026-06-04
> Status: BOOTSTRAPPED
> Sync basis: Current code snapshot under `src/` and `pom.xml`; git history was not used.

## Tech Stack

| Layer | Technology | Version / note |
|-------|------------|----------------|
| Language | Java | 21 |
| Framework | Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf | Spring Boot `4.0.2` from `pom.xml` |
| Database | Microsoft SQL Server via `mssql-jdbc`, Hibernate SQL Server dialect | Schema managed outside Hibernate |
| Auth | Spring Security, JWT cookie, Google OAuth2 Client, dynamic DB-driven roles/permissions with JWT RBAC snapshot | `jjwt` 0.12.6 |
| Frontend | Thymeleaf templates, Bootstrap, jQuery, custom JS/CSS | Server-rendered pages plus JSON APIs |
| Build Tool | Maven wrapper | Use `bash mvnw` when wrapper/network behavior is unreliable |
| Mail | Spring Mail + Gmail SMTP settings | Values in config may be sensitive |
| Payment | VNPay integration | `config/vnpay` |

## Project Type

- Modular monolith full-stack web app.
- Backend API, security, JPA data access, Thymeleaf views, static JS/CSS and SQL Server schema mapping live in the same repository.
- Evidence: `pom.xml`, `src/main/java/.../controller/api`, `controller/view`, `entities`, `repositories/IRepository`, `src/main/resources/templates`, `src/main/resources/static`.

## Runtime Architecture

1. Browser opens a view route such as `/posts`, `/events`, `/admin/posts`, `/admin/role-management`.
2. `controller.view` returns only a Thymeleaf template name.
3. Template JavaScript calls JSON APIs under `/api/**`.
4. `controller.api` validates request DTOs with Jakarta Validation and returns `ApiResponse<T>`.
5. Service interfaces live in `service.itf`; implementations in `service.impl` hold business rules, mapping and transactions.
6. `service.support` holds reusable policy/context helpers such as `NguCanhNguoiDung`, `NguCanhNguoiDungFactory`, `EventStatusDisplayPolicy`.
7. Repositories in `repositories.IRepository` use Spring Data JPA.
8. Entities in `entities` map SQL Server tables. `spring.jpa.hibernate.ddl-auto=none`, so schema/script is the DB source of truth.

## Security Architecture

- `SecurityConfig` configures stateless sessions, JWT cookie, OAuth2 Google login, public routes and authenticated routes.
- SecurityConfig distinguishes public vs authenticated routes; fine-grained authorization is handled by `PermissionInterceptor` + `@RequirePermission`.
- JWT stores a canonical RBAC snapshot with four parts: `roles`, `permissions`, `roleLevel`, `blacklist`.
- `JwtAuthenticationFilter` restores role authorities with `ROLE_` prefix and raw permission authorities from that snapshot.
- `DynamicRoleFilter` rechecks DB only when token snapshot is old/missing, user flag/cluster state requires it, or RBAC snapshot drift is detected.
- `PermissionInterceptor` is registered for `/admin/**`, `/api/admin/**`, `/api/comments/**`, `/api/reports/**`, `/api/events/**`, `/api/posts/**`.
- Methods without `@RequirePermission` pass through for backward compatibility.
- SUPERADMIN bypass is determined by backend role level `roleLevel == 0`, normally from `JWT_ROLE_LEVEL` request attr with DB fallback only when missing.
- `User.getAuthorities()` emits roles with `ROLE_` prefix and permission codes as raw authorities.
- RBAC admin permissions use `RBAC_*` to avoid collision with Spring Security role authority naming.

## Dynamic RBAC Layers

1. Database: `USER_ROLES`, `PERMISSIONS`, `PERMISSION_MODULES`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`.
2. Permission loading: `UserServiceImpl.napQuyenChoNguoiDung()` loads roles, effective permissions, blacklist and strongest role level.
3. Snapshot: `UserSecurityAdapter` and `JwtService` write `roles`, `permissions`, `roleLevel`, `blacklist` into JWT.
4. Restore: `JwtAuthenticationFilter` creates Spring authorities and request attributes from JWT without DB per request.
5. Refresh: `DynamicRoleFilter` compares token snapshot against DB when invalidation/drift requires refresh.
6. Enforcement: `PermissionInterceptor` reads `@RequirePermission` from controller/view methods.
7. UX: `permission-manager.js` and `data-permission` hide/show UI only; backend remains the source of truth.

## Core Modules

- Auth/security/profile.
- Public posts and admin posts.
- Public events/registrations and admin events.
- Comments/replies/reactions/reports/moderation.
- Admin RBAC/user management/audit.
- Profile/address/partner profile.

## Project Convention

### Layering

- API JSON flow: `controller.api` -> `service.itf` -> `service.impl` -> `repositories.IRepository` -> `entities`.
- View controllers should return template names only.
- DTOs are split into `dto.request` and `dto.response`.
- API responses are wrapped in `ApiResponse<T>`.
- Write/multi-step business flows should be transactional at service layer.
- Mapping is manual with setters/builders; no MapStruct/ModelMapper detected.
- Service and repository interfaces use `I` prefix, for example `IAdminPostService`, `IPostRepository`.

### Naming

- Java class/interface: PascalCase.
- Method/field/local variable: camelCase; Vietnamese non-accent names are common, for example `layDanhSachBaiViet`, `capNhatChucVu`.
- SQL table/column names are mapped explicitly to uppercase/snake/camel DB names.
- Admin API controllers use `ApiAdmin...Controller`; public APIs use `Api...Controller`.
- Public API paths use `/api/posts`, `/api/events`, `/api/comments`, `/api/auth`; admin paths use `/api/admin/...`.

### Validation And Errors

- Request DTOs use Jakarta Validation such as `@Valid`, `@NotBlank`, `@NotNull`, `@Size`, `@Email`.
- Business errors throw `AppException(status, message)`.
- `GlobalExceptionHandler` converts exceptions to `ApiResponse.loi(...)`.
- Some older comments still mention role names such as `ROLE_ADMIN`; trust executable annotations and service logic over stale comments.

### Frontend

- Thymeleaf templates contain substantial inline JS for domain pages.
- Common static utilities: `security-core.js`, `auth-sync.js`, `page-transition-manager.js`, `permission-manager.js`, `rich-content-editor.js`.
- Admin posts/events/comments use PageTransitionManager to avoid flicker.
- Rich Content Editor is shared by admin posts and admin events.

### Testing

- Test framework: JUnit 5 + Spring Boot Test.
- Existing test entry: `AuctionSystemNhom6ApplicationTests.contextLoads()`.
- For code changes, prefer `bash mvnw -q -DskipTests compile`, then `bash mvnw -q test` when dependency/network access works.
- Current known blocker from previous runs: Maven Central access can fail with `Permission denied: getsockopt`.

## External Dependencies And Sensitive Config

- `application.properties` contains datasource, mail, OAuth2, JWT and VNPay settings. Never paste raw sensitive values.
- During memory syncs, config must be inspected by property names or masked values only.
- `SecurityConfig` still contains a hardcoded remember-me secret literal; do not copy this pattern.

## Architecture Decisions

| Quyet dinh | Phuong an | Ly do | Ngay ghi | Het han |
|-----------|-----------|-------|----------|---------|
| Fine-grained authorization via `@RequirePermission` | Spring Security authenticates broad route groups; `PermissionInterceptor` enforces DB permission codes | Keeps authorization database-driven and editable through admin RBAC | 2026-06-03 | 2026-09-03 |
| SUPERADMIN source of truth is `roleLevel == 0` | Do not rely on role name, authority string or frontend state | Prevents bypass drift when role names/authorities change | 2026-06-03 | 2026-09-03 |
| RBAC admin permissions use `RBAC_*` | Do not use `ROLE_MANAGE` or `ROLE_*` permission codes for RBAC management | Avoids collision with Spring Security `ROLE_` authority prefix and supports granular actions | 2026-06-03 | 2026-09-03 |
| JWT security state uses neutral typed snapshot | Library stores `securityAuthorities`, `securityFingerprint`, `securityExposedAttributes`; app may add typed claims | Keeps the shared security library reusable while preserving no-DB permission checks and dynamic token refresh | 2026-06-04 | 2026-12-04 |
| Sync token is not an RBAC actor | Use neutral sync authority/fingerprint without app `roleLevel` claims | Prevents sync tokens from being treated as user-level superadmin sessions | 2026-06-04 | 2026-12-04 |

## Important Notes

- Auth, RBAC, payment, registration, migration/schema and moderation are critical paths.
- Code is more authoritative than memory and docs when there is conflict.
- This memory snapshot was synchronized from code currently present in the workspace, not from git history.
