# Admin RBAC
> Last updated: 2026-06-04
> Source files: `controller/api/ApiRoleManagementController.java`, `controller/api/ApiRolesController.java`, `controller/view/AdminViewController.java`, `service/impl/RoleManagementServiceImpl.java`, `service/impl/UserServiceImpl.java`, `service/itf/IRoleManagementService.java`, `repositories/IRepository/ICtUserPermissionBlacklistRepository.java`, `config/init/DatabaseSeeder.java`, `entities/User.java`, `entities/UserRole.java`, `entities/Permission.java`, `entities/PermissionModule.java`, `entities/CtRolePermission.java`, `entities/CtUserPermissionBlacklist.java`, `config/interceptor/PermissionInterceptor.java`, `config/ultraSecureLibrary/Service/PhienBanPhanQuyenBaoMat.java`, `config/ultraSecureLibrary/Filter/DynamicRoleFilter.java`, `config/ultraSecureLibrary/Filter/JwtAuthenticationFilter.java`, `config/PermissionRegistry.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `static/js/permission-manager.js`, `templates/admin_layout.html`, `templates/admin/users.html`, `templates/admin/role-management.html`, `templates/admin/user-details.html`
> Confidence: HIGH

## Summary

RBAC is database-driven across roles, permissions, permission modules and per-user permission blacklist. The runtime auth snapshot is now carried in JWT as 4 canonical parts: `roles`, `permissions`, `roleLevel`, `blacklist`. Backend enforcement uses `@RequirePermission` and `PermissionInterceptor`; frontend only hides/shows controls with `permission-manager.js`.

## Current Facts

- `PermissionRegistry` has 44 permission codes.
- Controllers/views contain 126 `@RequirePermission` annotations.
- `ROLE_MANAGE` has 0 matches in `src/main/java`, templates and static JS.
- `permission-manager.js`: 183 lines.
- `admin/role-management.html`: 1636 lines.
- DTO request `PermissionModuleRequest` is present and used for module create/update.
- `PhienBanPhanQuyenBaoMat` is present and canonicalizes RBAC snapshot values for JWT/DNA comparison.

## Four Layers

1. Database: `USER_ROLES`, `PERMISSIONS`, `PERMISSION_MODULES`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`.
2. Load: `UserServiceImpl.napQuyenChoNguoiDung()` loads roles, effective permissions, blacklisted permissions and strongest `roleLevel`.
3. Snapshot: `JwtService` stores the 4-part snapshot; `JwtAuthenticationFilter` restores authorities and request attrs without DB.
4. Refresh: `DynamicRoleFilter` compares token snapshot with DB only when token is old, user was flagged, or cluster mismatch occurs.
5. Enforce: `PermissionInterceptor` checks `@RequirePermission` using authorities and `roleLevel` from request attr/JWT with DB fallback only if missing.
6. UX: `permission-manager.js` reads `/api/admin/role-management/my-permissions` and applies `data-permission`.

## Core RBAC Rules

- Spring roles still use `ROLE_` prefix as authorities. RBAC management permissions use `RBAC_*`.
- `ROLE_MANAGE` must not be reintroduced for RBAC management.
- SUPERADMIN bypass is backend `roleLevel == 0`, normally read from JWT/request attr to avoid DB per request.
- Actor level 0 can manage the full system.
- Actor level > 0 cannot create/update/delete/clone/assign a role with `roleLevel <= actorLevel`.
- Actor level > 0 cannot modify roles currently assigned to self through indirect paths.
- Permission subset rule: actor level > 0 can create/update/clone/assign only roles whose permissions are within the actor's effective permission set, after blacklist.
- Permission code sent in role create/update must exist; service fails clearly if not.
- Permission create/update validates `moduleId` if present.
- Permission/module update/delete checks whether the operation would affect roles at stronger/equal level.
- Blacklist cannot target self and cannot target users stronger/equal to actor.
- `/api/admin/role-management/my-permissions` intentionally has no `@RequirePermission`; authenticated users need it for UX state.

## JWT Snapshot And Invalidation

- `User` has transient `danhSachTenRole`, `danhSachTenPermission`, `danhSachTenPermissionBlacklist`, `capBacQuyenLuc`.
- `UserSecurityAdapter` exposes those fields to `ultraSecureLibrary` as the 4 canonical snapshot parts.
- `JwtAuthenticationFilter` maps roles to `ROLE_*` authorities and permissions to raw authorities; this matches `PermissionInterceptor` checks.
- `ApiRoleManagementController` marks affected users through `MaTranNhiPhanNguyenTu.danhDauViPham()` and broadcasts `USER_BAN`-style user flag through `TramPhatSongVoTuyenP2P`.
- Affected-user invalidation is triggered for role update, permission update/delete and blacklist toggle.
- `RoleManagementServiceImpl.layUserIdDangGiuChucVu(roleId)` finds users holding a role before role update.
- `RoleManagementServiceImpl.layUserIdBiAnhHuongBoiQuyen(permissionId)` finds users affected through role-permission mappings plus direct blacklist mappings.
- `ICtUserPermissionBlacklistRepository.findByPermissionId(permissionId)` supports permission mutation impact analysis.
- Role delete/clone/create currently do not trigger the same affected-user refresh path because create/clone has no existing users and delete is blocked/handled by role management constraints.
- Inter-server sync tokens are not RBAC actors: `JwtService.generateTrojanSyncToken()` sets snapshot `roleLevel = 999`, while sync execution still depends on `sync_payload`, HMAC and replay guard.

## Granular RBAC Codes

| Group | Codes |
|-------|-------|
| Role | `RBAC_ROLE_VIEW`, `RBAC_ROLE_CREATE`, `RBAC_ROLE_UPDATE`, `RBAC_ROLE_DELETE`, `RBAC_ROLE_CLONE` |
| Permission | `RBAC_PERMISSION_VIEW`, `RBAC_PERMISSION_CREATE`, `RBAC_PERMISSION_UPDATE`, `RBAC_PERMISSION_DELETE` |
| Module | `RBAC_MODULE_VIEW`, `RBAC_MODULE_CREATE`, `RBAC_MODULE_UPDATE`, `RBAC_MODULE_DELETE` |
| Blacklist | `RBAC_BLACKLIST_VIEW`, `RBAC_BLACKLIST_TOGGLE` |

## API Endpoints

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/api/admin/role-management/my-permissions` | Current user's roles/permissions/roleLevel/superAdmin | Auth only |
| GET | `/api/admin/role-management/system-permissions` | PermissionRegistry codes | `RBAC_PERMISSION_VIEW` |
| GET | `/api/admin/role-management/roles` | Role list | `RBAC_ROLE_VIEW` |
| POST | `/api/admin/role-management/roles` | Create role | `RBAC_ROLE_CREATE` |
| PUT | `/api/admin/role-management/roles/{id}` | Update role | `RBAC_ROLE_UPDATE` |
| DELETE | `/api/admin/role-management/roles/{id}` | Delete role | `RBAC_ROLE_DELETE` |
| POST | `/api/admin/role-management/roles/{id}/clone` | Clone role | `RBAC_ROLE_CLONE` |
| GET | `/api/admin/role-management/permissions` | Permission list | `RBAC_PERMISSION_VIEW` |
| POST | `/api/admin/role-management/permissions` | Create permission | `RBAC_PERMISSION_CREATE` |
| PUT | `/api/admin/role-management/permissions/{id}` | Update permission | `RBAC_PERMISSION_UPDATE` |
| DELETE | `/api/admin/role-management/permissions/{id}` | Delete permission | `RBAC_PERMISSION_DELETE` |
| GET | `/api/admin/role-management/blacklist/users/{userId}` | User permission blacklist | `RBAC_BLACKLIST_VIEW` |
| POST | `/api/admin/role-management/blacklist/users/{userId}` | Toggle blacklist | `RBAC_BLACKLIST_TOGGLE` |
| GET | `/api/admin/role-management/modules` | Permission modules | `RBAC_MODULE_VIEW` |
| POST | `/api/admin/role-management/modules` | Create module | `RBAC_MODULE_CREATE` |
| PUT | `/api/admin/role-management/modules/{id}` | Update module | `RBAC_MODULE_UPDATE` |
| DELETE | `/api/admin/role-management/modules/{id}` | Delete module | `RBAC_MODULE_DELETE` |

## View/UI Guards

- `/admin/role-management` requires `RBAC_ROLE_VIEW`.
- `/admin/users` and `/admin/users/phan-quyen/{id}` require `USER_VIEW`.
- `/admin/posts`, `/admin/events`, `/admin/comments` require `POST_VIEW`, `EVENT_VIEW`, `COMMENT_VIEW`.
- `admin_layout.html`, `admin/users.html`, `admin/role-management.html`, `admin/user-details.html` use `data-permission`.
- `user-details.html` hides blacklist state if lacking `RBAC_BLACKLIST_VIEW` and only enables toggle with `RBAC_BLACKLIST_TOGGLE`.

## DatabaseSeeder

`DatabaseSeeder.khoiTaoQuyenHeThongTuRegistry()`:

- Iterates all `PermissionRegistry.layDanhSachQuyenHeThong()` rows.
- Inserts only missing permissions.
- Does not assign roles.
- Does not overwrite descriptions/admin customization.
- Sets `moduleId` when `moduleCode` exists in `PERMISSION_MODULES`.

## Verification Notes

- `git diff --check` passed after the sync-token `roleLevel` hardening.
- Compile verification for the 2026-06-04 RBAC snapshot upgrade remains `BLOCKED_BY_NETWORK`.
- Attempted command: `bash mvnw -q -DskipTests compile` in sandbox and escalated runs.
- Blocker: Maven could not download parent POM from Maven Central due to `Permission denied: getsockopt`.
- Do not record this implementation as compile-pass until Maven network access is available or dependencies are cached.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Use `RBAC_*` for RBAC management | Drop `ROLE_MANAGE` | Avoid Spring Security `ROLE_` collision and support action-level permissions | 2026-06-03 | 2026-09-03 |
| Seed PermissionRegistry idempotently | Insert missing permissions only | Keeps backend-enforced codes available without overwriting admin edits | 2026-06-03 | 2026-09-03 |
| Enforce permission subset for non-level-0 actors | Actor can only grant permissions they effectively have | Prevents delegated privilege escalation | 2026-06-03 | 2026-09-03 |
| Invalidate affected users on RBAC mutations | Mark impacted user sessions and let `DynamicRoleFilter` self-heal token snapshot | Keeps no-DB-per-request behavior while still applying role/permission/blacklist changes dynamically | 2026-06-04 | 2026-09-04 |
| Sync token is not SUPERADMIN | Use `roleLevel = 999` for Trojan sync JWT | Prevents `PermissionInterceptor` from treating an inter-server sync token as a user-level superadmin if it leaves the intended sync path | 2026-06-04 | 2026-09-04 |
