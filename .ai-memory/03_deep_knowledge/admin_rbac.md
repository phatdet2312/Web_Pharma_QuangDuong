# Admin RBAC - Phan quyen dong 100% Database-Driven
> Last updated: 2026-06-03
> Source files: `controller/api/ApiRoleManagementController.java`, `controller/api/ApiRolesController.java`, `controller/view/AdminViewController.java`, `service/impl/RoleManagementServiceImpl.java`, `service/impl/UserServiceImpl.java`, `config/init/DatabaseSeeder.java`, `entities/User.java`, `entities/UserRole.java`, `entities/Permission.java`, `entities/PermissionModule.java`, `entities/CtRolePermission.java`, `entities/CtUserPermissionBlacklist.java`, `config/interceptor/PermissionInterceptor.java`, `config/PermissionRegistry.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `static/js/permission-manager.js`, `templates/admin_layout.html`, `templates/admin/users.html`, `templates/admin/role-management.html`, `templates/admin/user-details.html`
> Confidence: HIGH

## Mo ta chuc nang

Module phan quyen dong 100% database-driven. Role, permission hat luu va permission module duoc quan tri qua giao dien admin. Cac ma permission backend dang enforce duoc khai bao trong `PermissionRegistry` de lam registry he thong va seed idempotent khi DB thieu.

Kien truc 4 lop:
1. CSDL: `USER_ROLES`, `PERMISSIONS`, `PERMISSION_MODULES`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`.
2. Nap quyen: `napQuyenChoNguoiDung()` query role/permission, loc blacklist ca nhan, bom roles + permissions vao principal/JWT.
3. Enforce: `PermissionInterceptor` doc `@RequirePermission` tren controller method va view route.
4. UX: `permission-manager.js` va `data-permission` an/hien UI. Frontend chi la UX, backend van enforce.

## Business Rules quan trong

- Spring Security role authority dang dung prefix `ROLE_`; permission quan tri RBAC dung prefix `RBAC_*` de tranh va cham.
- `ROLE_MANAGE` da bi bo khoi `src/main/java`, templates va static JS. Khong them lai permission tong nay cho endpoint/control chinh.
- SUPERADMIN duoc xac dinh bang backend `roleLevel == 0` cho bypass; khong dua vao frontend.
- Actor level 0 bypass cac invariant quan tri RBAC. Actor khac level 0 khong duoc tao/sua/clone/xoa/gan role co `roleLevel <= actorLevel`, khong duoc tu sua role dang cap quyen cho chinh minh.
- Permission subset rule: actor khac level 0 chi duoc tao/sua/clone/gan role chua cac permission nam trong tap quyen hieu luc cua chinh actor, co xet blacklist ca nhan.
- Tao/sua role fail ro neu permission code khong ton tai; tao/sua permission fail ro neu `moduleId` khong ton tai.
- Blacklist ca nhan chan target user ngang/manh hon actor, chan tu thao tac chinh minh va validate request bang `@Valid`.
- Endpoint khong co `@RequirePermission` annotation khong bi permission check rieng de giu backward compatible.
- `GET /api/admin/role-management/my-permissions` khong gan annotation rieng; user authenticated can goi duoc de frontend biet UX permission.
- `GET /api/admin/role-management/system-permissions` dung `RBAC_PERMISSION_VIEW`.

## Granular RBAC Permission Codes

| Nhom | Permission codes |
|------|------------------|
| Role | `RBAC_ROLE_VIEW`, `RBAC_ROLE_CREATE`, `RBAC_ROLE_UPDATE`, `RBAC_ROLE_DELETE`, `RBAC_ROLE_CLONE` |
| Permission | `RBAC_PERMISSION_VIEW`, `RBAC_PERMISSION_CREATE`, `RBAC_PERMISSION_UPDATE`, `RBAC_PERMISSION_DELETE` |
| Module | `RBAC_MODULE_VIEW`, `RBAC_MODULE_CREATE`, `RBAC_MODULE_UPDATE`, `RBAC_MODULE_DELETE` |
| Blacklist | `RBAC_BLACKLIST_VIEW`, `RBAC_BLACKLIST_TOGGLE` |

## API Endpoints

| Method | Path | Mo ta | @RequirePermission |
|--------|------|-------|-------------------|
| GET | `/api/admin/role-management/my-permissions` | Quyen user hien tai cho frontend UX | Khong |
| GET | `/api/admin/role-management/system-permissions` | Danh sach ma quyen tu PermissionRegistry | `RBAC_PERMISSION_VIEW` |
| GET | `/api/admin/role-management/roles` | Danh sach role | `RBAC_ROLE_VIEW` |
| POST | `/api/admin/role-management/roles` | Tao role | `RBAC_ROLE_CREATE` |
| PUT | `/api/admin/role-management/roles/{id}` | Cap nhat role | `RBAC_ROLE_UPDATE` |
| DELETE | `/api/admin/role-management/roles/{id}` | Xoa role | `RBAC_ROLE_DELETE` |
| POST | `/api/admin/role-management/roles/{id}/clone` | Clone role | `RBAC_ROLE_CLONE` |
| GET | `/api/admin/role-management/permissions` | Danh sach permission | `RBAC_PERMISSION_VIEW` |
| POST | `/api/admin/role-management/permissions` | Tao permission | `RBAC_PERMISSION_CREATE` |
| PUT | `/api/admin/role-management/permissions/{id}` | Cap nhat permission | `RBAC_PERMISSION_UPDATE` |
| DELETE | `/api/admin/role-management/permissions/{id}` | Xoa permission | `RBAC_PERMISSION_DELETE` |
| GET | `/api/admin/role-management/modules` | Danh sach nhom chuc nang | `RBAC_MODULE_VIEW` |
| POST | `/api/admin/role-management/modules` | Tao nhom chuc nang | `RBAC_MODULE_CREATE` |
| PUT | `/api/admin/role-management/modules/{id}` | Cap nhat nhom chuc nang | `RBAC_MODULE_UPDATE` |
| DELETE | `/api/admin/role-management/modules/{id}` | Xoa nhom chuc nang | `RBAC_MODULE_DELETE` |
| GET | `/api/admin/role-management/blacklist/users/{userId}` | Permission blacklist cua user | `RBAC_BLACKLIST_VIEW` |
| POST | `/api/admin/role-management/blacklist/users/{userId}` | Toggle permission blacklist | `RBAC_BLACKLIST_TOGGLE` |
| GET/POST | `/api/admin/users/**` | User list/search/detail/role/lock/bulk-lock | `USER_VIEW`, `USER_ASSIGN_ROLE`, `USER_LOCK` |

## View va UI Guards

- `AdminViewController` route `/admin/role-management` dung `@RequirePermission("RBAC_ROLE_VIEW")`.
- `admin_layout.html`, `admin/users.html`, `admin/role-management.html`, `admin/user-details.html` dung `RBAC_*` trong `data-permission` va guard JS.
- `user-details.html` khong hien trang thai blacklist neu thieu `RBAC_BLACKLIST_VIEW`.
- Blacklist toggle dung `RBAC_BLACKLIST_TOGGLE`.
- User role assignment va lock UI guard ro bang `USER_ASSIGN_ROLE` va `USER_LOCK`.

## DatabaseSeeder

`DatabaseSeeder.khoiTaoQuyenHeThongTuRegistry()` seed idempotent toan bo permission code trong `PermissionRegistry` neu DB thieu:

- Chi tao permission con thieu.
- Khong gan role.
- Khong ghi de permission da co, bao gom mo ta admin da sua.
- Set `moduleId` neu `moduleCode` trong registry ton tai trong `PERMISSION_MODULES`.

## @RequirePermission da gan tren controller

| Controller | Permission codes su dung |
|------------|--------------------------|
| `ApiAdminPostController` | POST_VIEW, POST_CREATE, POST_EDIT, POST_DELETE, POST_MANAGE_CATEGORY, POST_MANAGE_TAG |
| `ApiAdminEventController` | EVENT_VIEW, EVENT_CREATE, EVENT_EDIT, EVENT_DELETE, EVENT_MANAGE_TYPE, EVENT_MANAGE_LOCATION |
| `ApiAdminCommentController` | COMMENT_VIEW, COMMENT_MODERATE, COMMENT_DELETE, COMMENT_MANAGE_REACTION |
| `ApiAdminSpeakerAgendaController` | EVENT_MANAGE_SPEAKER, EVENT_MANAGE_AGENDA |
| `ApiAdminReportController` | REPORT_VIEW, REPORT_RESOLVE |
| `ApiRoleManagementController` | RBAC_ROLE_VIEW/CREATE/UPDATE/DELETE/CLONE, RBAC_PERMISSION_VIEW/CREATE/UPDATE/DELETE, RBAC_MODULE_VIEW/CREATE/UPDATE/DELETE, RBAC_BLACKLIST_VIEW/TOGGLE |
| `ApiRolesController` | USER_VIEW, USER_ASSIGN_ROLE, USER_LOCK |
| `ApiAuditController` | AUDIT_VIEW |
| Public write controllers | USER_COMMENT, USER_REACT, USER_REGISTER, USER_REPORT |

## Verification 2026-06-03

- `rg ROLE_MANAGE src/main/java src/main/resources/templates src/main/resources/static/js` khong con ket qua.
- `rg RBAC_ROLE_|RBAC_PERMISSION_|RBAC_MODULE_|RBAC_BLACKLIST_ src/main/java src/main/resources/templates src/main/resources/static/js` xac nhan backend/view/UI dang dung permission granular.
- `node --check src/main/resources/static/js/permission-manager.js` pass.
- `bash mvnw -q -DskipTests compile` blocked do Maven Central parent POM khong tai duoc: `Permission denied: getsockopt`.

## Decision Log

| Quyet dinh | Phuong an | Ly do | Ngay ghi | Het han |
|-----------|-----------|-------|----------|---------|
| Dung prefix `RBAC_*` cho permission quan tri role/permission/module/blacklist | Chon `RBAC_*`; bo `ROLE_MANAGE` va khong dung `ROLE_VIEW/ROLE_CREATE` | Tranh va cham voi Spring Security role authority `ROLE_`, dong thoi tach quyen quan tri RBAC theo action hat luu | 2026-06-03 | 2026-09-03 |
| DatabaseSeeder seed PermissionRegistry idempotent | Chi insert permission con thieu, khong gan role, khong ghi de permission da co | Dam bao backend-enforced permission co trong DB sau deploy ma khong pha tuy bien cua admin | 2026-06-03 | 2026-09-03 |

## Ghi chu

- Khi them endpoint admin moi: them `@RequirePermission("CODE")`, cap nhat `PermissionRegistry`, va cap nhat UI guard neu co.
- Permission code nen theo convention `MODULE_ACTION`; rieng permission quan tri RBAC dung `RBAC_*`.
- Day la critical path authorization; sua logic phan quyen can review bao mat.
