# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-06-01

## Trang thai hien tai

- **IDLE** — Phase 5 (Phan quyen Dong) DA HOAN CHINH. Commit `035a691` (2026-06-01) dong bo memory.
- He thong phan quyen dong 4 lop: CSDL → Nap quyen → PermissionInterceptor + @RequirePermission → PermissionManager JS.
- 100+ annotation @RequirePermission da gan tren 12 controller (admin + public write endpoints).
- Entity moi: `PermissionModule` (nhom chuc nang), Permission co FK `moduleId`.
- DTO moi: `MyPermissionResponse`, `PermissionModuleResponse`, `PermissionRequest` (co moduleId), `PermissionResponse` (co moduleId/moduleCode/moduleName/riskLevel).
- Config moi: `PermissionInterceptor`, `PermissionRegistry`, `RequirePermission` annotation.
- JS moi: `permission-manager.js` an/hien UI theo quyen.
- Admin posts module: 33 public methods, 32 controller endpoints, Rich Content Editor, 2564 dong frontend.
- Admin events module: Rich Content Editor, 3663 dong frontend.
- Admin comments module hoan chinh tu 2026-05-29.

## Context cho phien sau

- `mvnw.cmd` dang loi wrapper PowerShell; dung `bash mvnw` cho compile/test.
- Hai file `posts/detail.html` va `events/detail.html` co comment system tuong dong — khi sua 1 ben nen kiem tra ben kia.
- Admin comments reply dung prefix `adm-` cho CSS class de tranh xung dot voi user-side.
- Rich Content Editor dung bien `rceHienTaiEditorId`/`rceHienTaiPreviewId` ho tro nhieu editor tren 1 trang.
- WebMvcConfig dang ky PermissionInterceptor cho: `/api/admin/**`, `/api/comments/**`, `/api/reports/**`, `/api/events/**`, `/api/posts/**`.
- Endpoint khong co @RequirePermission van hoat dong binh thuong (backward compatible).

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
2026-05-27: Codex rewrite admin event module (+49 files), commit 871f7dd.
2026-05-28: User fix codex tach ImagePathUtil + PagingUtil, commit 2d64c50.
2026-05-28: Admin comments rewrite API-driven + PageTransitionManager, commit ffcced1 + ecf601d.
2026-05-29: Fix LOAI_LIKE upload + reply tree branch, commit 1bb9565.
2026-05-31: Nang cap admin post va event + Phase 5 phan quyen dong, commit 89977f7 + 035a691.
```
