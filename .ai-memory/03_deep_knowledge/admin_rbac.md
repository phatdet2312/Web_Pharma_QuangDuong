# Admin RBAC
> Last updated: 2026-05-18
> Source files: `controller/api/ApiRoleManagementController.java`, `controller/api/ApiRolesController.java`, `service/impl/RoleManagementServiceImpl.java`, `service/impl/RolesServiceImpl.java`, `entities/User.java`, `entities/UserRole.java`, `entities/Permission.java`, `entities/CtRolePermission.java`, `entities/CtUserPermissionBlacklist.java`
> Confidence: HIGH

## Mô tả chức năng

Module RBAC/admin quản lý role, permission, blacklist quyền ở cấp user, khóa tài khoản và quản trị user. Security sử dụng role Spring Security cho route-level auth, đồng thời có permission động trong `User.getAuthorities()`.

## Luồng xử lý chính

1. Admin gọi `/api/admin/role-management/roles` hoặc `/permissions` để CRUD role/permission.
2. `ApiRoleManagementController` gọi `IRoleManagementService`.
3. Service thao tác các bảng `USER_ROLES`, `PERMISSIONS`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST`.
4. Admin user-management gọi `/api/admin/users/**` để xem/search/lock/gán role/bulk lock.
5. Khi authenticate, user được nạp role/permission động và trả thành `GrantedAuthority`.

## Business Rules quan trọng

- Admin route `/api/admin/**` trong `SecurityConfig` yêu cầu `ADMIN` hoặc `SUPERADMIN`; trang `/admin/**` cho `EMPLOYEE`, `ADMIN`, `SUPERADMIN`.
- Role cần prefix `ROLE_` khi đưa vào Spring Security authority; permission không cần prefix.
- User-level blacklist có thể chặn permission dù role có quyền.
- Các endpoint nhận input từ admin vẫn là client input; khi thêm/sửa phải dùng DTO validation, không tin request body.
- Đây là critical path auth/authorization; sửa logic phân quyền phải review bảo mật.

## API Endpoints

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| GET | `/api/admin/role-management/roles` | Danh sách role | Admin |
| POST | `/api/admin/role-management/roles` | Tạo role | Admin |
| PUT | `/api/admin/role-management/roles/{id}` | Cập nhật role | Admin |
| DELETE | `/api/admin/role-management/roles/{id}` | Xóa role | Admin |
| POST | `/api/admin/role-management/roles/{id}/clone` | Clone role | Admin |
| GET | `/api/admin/role-management/permissions` | Danh sách permission | Admin |
| POST | `/api/admin/role-management/permissions` | Tạo permission | Admin |
| PUT | `/api/admin/role-management/permissions/{id}` | Cập nhật permission | Admin |
| DELETE | `/api/admin/role-management/permissions/{id}` | Xóa permission | Admin |
| GET | `/api/admin/role-management/blacklist/users/{userId}` | Permission blacklist của user | Admin |
| POST | `/api/admin/role-management/blacklist/users/{userId}` | Toggle permission blacklist | Admin |
| GET/POST | `/api/admin/users/**` | User list/search/detail/role/lock/bulk-lock | Admin |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |

## Ghi chú

- `ApiRoleManagementController` hiện có vài method nhận `@RequestBody` chưa thấy `@Valid`; khi chạm vào module này nên bổ sung validation nếu DTO hỗ trợ.
