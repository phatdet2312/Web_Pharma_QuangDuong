# Admin RBAC — Phân quyền Động 100% Database-Driven
> Last updated: 2026-06-03
> Source files: `controller/api/ApiRoleManagementController.java`, `controller/api/ApiRolesController.java`, `service/impl/RoleManagementServiceImpl.java`, `service/impl/RolesServiceImpl.java`, `entities/User.java`, `entities/UserRole.java`, `entities/Permission.java`, `entities/PermissionModule.java`, `entities/CtRolePermission.java`, `entities/CtUserPermissionBlacklist.java`, `config/interceptor/PermissionInterceptor.java`, `config/PermissionRegistry.java`, `validators/annotations/RequirePermission.java`, `config/WebMvcConfig.java`, `static/js/permission-manager.js`, `repositories/IRepository/IPermissionModuleRepository.java`, `dto/request/PermissionRequest.java`, `dto/response/PermissionResponse.java`, `dto/response/PermissionModuleResponse.java`, `dto/response/MyPermissionResponse.java`
> Confidence: HIGH

## Mô tả chức năng

Module phân quyền động 100% database-driven. Mọi role, permission hạt lựu đều do SUPERADMIN tạo và gán qua giao diện admin. Thêm module mới chỉ cần admin tạo permission mới trên web — KHÔNG sửa code.

**Kiến trúc 4 lớp:**
1. **Lớp 1 (CSDL)**: Admin tạo role/permission/module, gán qua 6 bảng RBAC (`USER_ROLES`, `PERMISSIONS`, `PERMISSION_MODULES`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`)
2. **Lớp 2 (Nạp quyền)**: `napQuyenChoNguoiDung()` query 4 bảng + lọc blacklist → bơm vào JWT
3. **Lớp 3 (Enforce)**: `PermissionInterceptor` + `@RequirePermission` check quyền mỗi endpoint
4. **Lớp 4 (UX)**: `permission-manager.js` ẩn/hiện UI (chỉ UX, backend vẫn enforce)

## Luồng xử lý chính

1. SUPERADMIN tạo Role/Permission/gán qua `/api/admin/role-management` (11+ endpoint).
2. User đăng nhập → `napQuyenChoNguoiDung()` query CT_USER_ROLES → USER_ROLES → CT_ROLE_PERMISSIONS → PERMISSIONS, LỌC CT_USER_PERMISSION_BLACKLIST, bơm vào @Transient.
3. `User.getAuthorities()` trả `ROLE_X` + permission codes → nhúng vào JWT.
4. `DynamicRoleFilter` (ultraSecureLibrary) tự sync JWT khi quyền thay đổi giữa phiên.
5. Mỗi request admin → `PermissionInterceptor.preHandle()` đọc `@RequirePermission` annotation → SUPERADMIN bypass → check authorities → 403 nếu thiếu quyền.
6. Frontend gọi `GET /api/admin/my-permissions` → `PermissionManager.coQuyen()` ẩn/hiện UI.

## Business Rules quan trọng

- SecurityConfig KHÔNG còn hardcode role — chỉ `permitAll()` vs `authenticated()`. Mọi check quyền cụ thể do PermissionInterceptor xử lý.
- SUPERADMIN (roleLevel=0) = GOD MODE — backend `roleLevel` là nguồn sự thật duy nhất cho bypass, không dựa vào tên role hoặc frontend.
- Hardening 2026-06-03: các hàm ghi role (`taoChucVuMoi`, `capNhatChucVu`, `xoaChucVu`, `nhanBanChucVu`) nhận `currentUser` và kiểm tra cấp bậc trước khi lưu. Actor level 0 được phép thao tác toàn hệ thống; actor khác level 0 không được tạo/sửa/clone/xóa role mạnh hơn hoặc ngang mình (`targetRoleLevel <= actorLevel`) và không được sửa role đang gán cho chính actor.
- Hardening 2026-06-03: gán role cho user khác trong `UserServiceImpl.updateUserRoles()` chặn target ngang/mạnh hơn và chặn gán role ngang/mạnh hơn actor; actor level 0 là ngoại lệ. Tự đổi role của chính mình vẫn bị chặn.
- Hardening 2026-06-03: blacklist quyền cá nhân chặn target user ngang/mạnh hơn actor, chặn tự thao tác chính mình và validate request bằng `@Valid`.
- Hardening 2026-06-03: tạo/sửa role fail rõ nếu permission code không tồn tại; tạo/sửa permission fail rõ nếu `moduleId` không tồn tại.
- Hardening 2026-06-03: permission subset rule. Actor non-level-0 chỉ được tạo/sửa/clone/gán role chứa các permission nằm trong tập quyền hiệu lực của chính actor, có xét `CT_USER_PERMISSION_BLACKLIST`. Actor level 0 bypass rule này. Rule này thay cho hướng phân loại độ nhạy permission.
- Permission entity có FK `moduleId` tới bảng `PERMISSION_MODULES` (entity `PermissionModule`). Admin nhóm quyền theo module (VD: POST, EVENT, COMMENT, SYSTEM). Implementation dùng bảng riêng thay vì cột VARCHAR — khác plan ban đầu.
- Blacklist hoạt động thật: `napQuyenChoNguoiDung()` lọc CT_USER_PERMISSION_BLACKLIST trước khi bơm authorities.
- Endpoint không có `@RequirePermission` annotation = không bị check (backward compatible).
- `GET /my-permissions` KHÔNG gán annotation — mọi user authenticated đều gọi được.
- Role cần prefix `ROLE_` khi đưa vào Spring Security authority; permission không cần prefix.
- Đây là critical path auth/authorization; sửa logic phân quyền phải review bảo mật.
- KHÔNG SỬA ultraSecureLibrary — chỉ gọi và dùng.

## API Endpoints

| Method | Path | Mô tả | Auth | @RequirePermission |
|--------|------|-------|------|-------------------|
| GET | `/api/admin/role-management/my-permissions` | Quyền user hiện tại (frontend UX) | Authenticated | Không (mọi user gọi được) |
| GET | `/api/admin/role-management/system-permissions` | Danh sách mã quyền từ PermissionRegistry | Authenticated | Không |
| GET | `/api/admin/role-management/roles` | Danh sách role | Admin | ROLE_MANAGE |
| POST | `/api/admin/role-management/roles` | Tạo role | Admin | ROLE_MANAGE |
| PUT | `/api/admin/role-management/roles/{id}` | Cập nhật role | Admin | ROLE_MANAGE |
| DELETE | `/api/admin/role-management/roles/{id}` | Xóa role | Admin | ROLE_MANAGE |
| POST | `/api/admin/role-management/roles/{id}/clone` | Clone role | Admin | ROLE_MANAGE |
| GET | `/api/admin/role-management/permissions` | Danh sách permission | Admin | ROLE_MANAGE |
| POST | `/api/admin/role-management/permissions` | Tạo permission | Admin | ROLE_MANAGE |
| PUT | `/api/admin/role-management/permissions/{id}` | Cập nhật permission | Admin | ROLE_MANAGE |
| DELETE | `/api/admin/role-management/permissions/{id}` | Xóa permission | Admin | ROLE_MANAGE |
| GET | `/api/admin/role-management/modules` | Danh sách nhóm chức năng | Admin | ROLE_MANAGE |
| POST | `/api/admin/role-management/modules` | Tạo nhóm chức năng | Admin | ROLE_MANAGE |
| PUT | `/api/admin/role-management/modules/{id}` | Cập nhật nhóm chức năng | Admin | ROLE_MANAGE |
| DELETE | `/api/admin/role-management/modules/{id}` | Xóa nhóm chức năng (FK check) | Admin | ROLE_MANAGE |
| GET | `/api/admin/role-management/blacklist/users/{userId}` | Permission blacklist của user | Admin | ROLE_MANAGE |
| POST | `/api/admin/role-management/blacklist/users/{userId}` | Toggle permission blacklist | Admin | ROLE_MANAGE |
| GET/POST | `/api/admin/users/**` | User list/search/detail/role/lock/bulk-lock | Admin | USER_VIEW/USER_ASSIGN_ROLE/USER_LOCK |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |

## @RequirePermission đã gán trên controller (tính tới 2026-06-01)

| Controller | Permission codes sử dụng |
|------------|-------------------------|
| `ApiAdminPostController` | POST_VIEW, POST_CREATE, POST_EDIT, POST_DELETE, POST_MANAGE_CATEGORY, POST_MANAGE_TAG |
| `ApiAdminEventController` | EVENT_VIEW, EVENT_CREATE, EVENT_EDIT, EVENT_DELETE, EVENT_MANAGE_TYPE, EVENT_MANAGE_LOCATION |
| `ApiAdminCommentController` | COMMENT_VIEW, COMMENT_MODERATE, COMMENT_DELETE, COMMENT_MANAGE_REACTION |
| `ApiAdminSpeakerAgendaController` | EVENT_MANAGE_SPEAKER, EVENT_MANAGE_AGENDA |
| `ApiAdminReportController` | REPORT_VIEW, REPORT_RESOLVE |
| `ApiRoleManagementController` | ROLE_MANAGE (tất cả CRUD role/permission/blacklist/module) |
| `ApiRolesController` | USER_VIEW, USER_ASSIGN_ROLE, USER_LOCK |
| `ApiAuditController` | AUDIT_VIEW |
| `ApiCommentController` | USER_COMMENT (write), USER_REACT (reaction) |
| `ApiPostController` | USER_REACT |
| `ApiEventController` | USER_REGISTER, USER_COMMENT |
| `ApiReportController` | USER_REPORT |

## PermissionRegistry

`config/PermissionRegistry.java` khai báo danh sách tĩnh 30 mã quyền chia 7 nhóm: POST (6), EVENT (8), COMMENT (4), USER (4), REPORT (2), USER_ACTION (4), SYSTEM (3). Mỗi entry gồm `{mã_quyền, mô_tả, mã_module}`. API đọc từ đây để cung cấp dropdown cho giao diện admin.

## Ghi chú

- @Valid đã được thêm cho 4 endpoint POST/PUT trong ApiRoleManagementController.
- Permission codes nên theo convention: `MODULE_ACTION` (VD: POST_CREATE, EVENT_DELETE, COMMENT_MODERATE, ROLE_MANAGE).
- Khi tạo endpoint admin MỚI: thêm `@RequirePermission("CODE")` trước mapping annotation. Nếu admin chưa tạo permission code đó → user (trừ SUPERADMIN) sẽ bị 403.
- Frontend dùng `PermissionManager.coQuyen("CODE")` hoặc `data-permission="CODE"` attribute trên element HTML.
- `PermissionRequest` có field `moduleId` (Integer FK), `PermissionResponse` trả kèm `moduleId`, `moduleCode`, `moduleName`, `riskLevel`.
- `RoleRequest` có field `permissions` (List<String>) — danh sách mã quyền gán cho role khi tạo/sửa.
- `MyPermissionResponse` trả `roles`, `permissions`, `roleLevel`, `superAdmin` cho frontend.
- `WebMvcConfig.addInterceptors()` đăng ký PermissionInterceptor cho: `/admin/**`, `/api/admin/**`, `/api/comments/**`, `/api/reports/**`, `/api/events/**`, `/api/posts/**`.
- Admin view hardening 2026-06-03: `AdminViewController` gắn `@RequirePermission` cho users (`USER_VIEW`), role-management (`ROLE_MANAGE`), posts (`POST_VIEW`), events (`EVENT_VIEW`), comments (`COMMENT_VIEW`). Dashboard chưa có annotation riêng nên vẫn dựa vào authenticated/admin route tổng.
