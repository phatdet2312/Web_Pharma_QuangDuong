# Active Workspace
> Last updated: 2026-06-03
> Status: IDLE
> Sync basis: Current code snapshot, not git history.

## Current State

- AI Memory has been comprehensively synchronized from current workspace code.
- Source code was not changed during this sync.
- Memory now reflects current inventory: 15 API controllers, 7 view controllers, 55 entities, 56 repositories, 36 request DTOs, 61 response DTOs.
- RBAC current state: 126 `@RequirePermission` annotations, 44 permission registry codes, `ROLE_MANAGE` absent from Java/templates/static JS.
- Admin posts module: `admin/posts.html` 2565L, `AdminPostServiceImpl` 1288L, admin upload/gallery/files/bulk/featured/link-event endpoints.
- Admin events module: `admin/events.html` 3663L, `AdminEventServiceImpl` 1318L, upload endpoints `/media/campaign-thumbnail` and `/media/speaker-avatar`.
- Admin comments module: `admin/comments.html` 2620L, `CommentServiceImpl` 1595L, reaction icon upload under `/uploads/comments/reaction-icons/`.
- RBAC module: `admin/role-management.html` 1636L, `permission-manager.js` 183L, `PermissionModuleRequest` is present.

## Context For Next Session

- Read `.ai-memory/03_deep_knowledge/INDEX.md` first, then the module-specific memory file.
- Do not rely on git history as the source of truth if user asks for current workspace behavior.
- `bash mvnw` is the preferred Maven wrapper path in this environment; previous compile/test attempts can be blocked by Maven Central network access (`Permission denied: getsockopt`).
- `application.properties` contains secret-bearing settings. Inspect only targeted property names and mask values.
- `SecurityConfig` still contains a hardcoded remember-me secret literal; treat as security debt if working in auth.
- Critical paths: auth/RBAC, event registration/paywall, payment, moderation/report delete lifecycle, schema/migration.

## Known Technical Anchors

- Security: `SecurityConfig`, `PermissionInterceptor`, `RequirePermission`, `PermissionRegistry`, `UserServiceImpl.napQuyenChoNguoiDung()`.
- RBAC service: `RoleManagementServiceImpl` enforces role level guard and permission subset guard for non-level-0 actors.
- Event context: `NguCanhNguoiDung` and `NguCanhNguoiDungFactory` are the access-aware public event context.
- UI utilities: `PageTransitionManager`, `PermissionManager`, `RichContentEditor`.

## Handover

No active plan is pending. For new work, create/update an active plan only when the task is substantial or user requests planning.
