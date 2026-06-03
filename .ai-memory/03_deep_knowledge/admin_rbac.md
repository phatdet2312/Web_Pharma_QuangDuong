# Admin RBAC
> Last updated: 2026-06-03
> Source files: `controller/api/ApiRoleManagementController.java`, `controller/api/ApiRolesController.java`, `controller/view/AdminViewController.java`, `service/impl/RoleManagementServiceImpl.java`, `service/impl/UserServiceImpl.java`, `config/init/DatabaseSeeder.java`, `entities/User.java`, `entities/UserRole.java`, `entities/Permission.java`, `entities/PermissionModule.java`, `entities/CtRolePermission.java`, `entities/CtUserPermissionBlacklist.java`, `config/interceptor/PermissionInterceptor.java`, `config/PermissionRegistry.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `static/js/permission-manager.js`, `templates/admin_layout.html`, `templates/admin/users.html`, `templates/admin/role-management.html`, `templates/admin/user-details.html`
> Confidence: HIGH

## Summary

RBAC is database-driven across roles, permissions, permission modules and per-user permission blacklist. Backend enforcement uses `@RequirePermission` and `PermissionInterceptor`; frontend only hides/shows controls with `permission-manager.js`.

## Current Facts

- `PermissionRegistry` has 44 permission codes.
- Controllers/views contain 126 `@RequirePermission` annotations.
- `ROLE_MANAGE` has 0 matches in `src/main/java`, templates and static JS.
- `permission-manager.js`: 183 lines.
- `admin/role-management.html`: 1636 lines.
- DTO request `PermissionModuleRequest` is present and used for module create/update.

## Four Layers

1. Database: `USER_ROLES`, `PERMISSIONS`, `PERMISSION_MODULES`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`.
2. Load: `UserServiceImpl.napQuyenChoNguoiDung()` loads role/permission authorities and removes blacklisted permissions.
3. Enforce: `PermissionInterceptor` checks `@RequirePermission`.
4. UX: `permission-manager.js` reads `/api/admin/role-management/my-permissions` and applies `data-permission`.

## Core RBAC Rules

- Spring roles still use `ROLE_` prefix as authorities. RBAC management permissions use `RBAC_*`.
- `ROLE_MANAGE` must not be reintroduced for RBAC management.
- SUPERADMIN bypass is backend `roleLevel == 0`.
- Actor level 0 can manage the full system.
- Actor level > 0 cannot create/update/delete/clone/assign a role with `roleLevel <= actorLevel`.
- Actor level > 0 cannot modify roles currently assigned to self through indirect paths.
- Permission subset rule: actor level > 0 can create/update/clone/assign only roles whose permissions are within the actor's effective permission set, after blacklist.
- Permission code sent in role create/update must exist; service fails clearly if not.
- Permission create/update validates `moduleId` if present.
- Permission/module update/delete checks whether the operation would affect roles at stronger/equal level.
- Blacklist cannot target self and cannot target users stronger/equal to actor.
- `/api/admin/role-management/my-permissions` intentionally has no `@RequirePermission`; authenticated users need it for UX state.

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

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Use `RBAC_*` for RBAC management | Drop `ROLE_MANAGE` | Avoid Spring Security `ROLE_` collision and support action-level permissions | 2026-06-03 | 2026-09-03 |
| Seed PermissionRegistry idempotently | Insert missing permissions only | Keeps backend-enforced codes available without overwriting admin edits | 2026-06-03 | 2026-09-03 |
| Enforce permission subset for non-level-0 actors | Actor can only grant permissions they effectively have | Prevents delegated privilege escalation | 2026-06-03 | 2026-09-03 |
