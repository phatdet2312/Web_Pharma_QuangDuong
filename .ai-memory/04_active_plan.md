# Active Plan
> Last updated: 2026-06-04
> Status: DONE_WITH_VERIFY_BLOCKED_BY_NETWORK
> Source: Synced from current working tree after implementation.

## Completed Task

Nang cap `ultraSecureLibrary` RBAC JWT contract tu mot danh sach `roles` tong hop sang snapshot canonical 4 manh:

- `roles`
- `permissions`
- `roleLevel`
- `blacklist`

## Non-Negotiable Constraints

| Constraint | Status | Evidence |
|------------|--------|----------|
| Giu `JwtService` token mechanism | DONE | `JwtService.generateToken()` van cap JWT cookie claims, them snapshot 4 manh |
| Giu `DynamicRoleFilter` DNA / refresh / kick | DONE | Filter doc claims, doi soat DNA, reissue token khi token cu/quyen doi/khac cluster |
| Giu `MaTranLuoiLocNghiaTrangQuyenHanCu` graveyard | DONE | DNA quyen cu van bi `chonCat()` va broadcast `ROLE_KILL` |
| Giu `MaTranNhiPhanNguyenTu` user flag | DONE | RBAC mutation danh dau user bang `danhDauViPham()` |
| Giu P2P `ROLE_KILL` / `USER_BAN` / `USER_UNBAN` | DONE | DynamicRoleFilter van xu ly sync payload va broadcast P2P |
| No DB each request | DONE | `PermissionInterceptor` doc `roleLevel` tu request attr/JWT, chi fallback DB khi attr thieu |
| No new filter | DONE | Cap nhat filter hien co: `JwtAuthenticationFilter`, `DynamicRoleFilter` |
| No body / replay / Tomcat changes | DONE | Khong thay doi contract body hash/replay/Tomcat; DynamicRoleFilter giu luong signature/replay hien co |

## Plan Phases

| Phase | Status | Result |
|-------|--------|--------|
| 1. Baseline audit | DONE | Da xac dinh cac diem lien quan JWT/RBAC: adapter, JwtService, 2 filter, interceptor, User/UserService, RBAC controller/service |
| 2. Design canonical snapshot helper | DONE | Them `PhienBanPhanQuyenBaoMat` de chuan hoa list, roleLevel, DNA va so sanh snapshot |
| 3. Extend `ISecurityUserAdapter` with 4 parts while preserving `layDanhSachQuyen` | DONE | Interface co `layDanhSachChucVu`, `layDanhSachQuyenThaoTac`, `layCapBacQuyenLuc`, `layDanhSachQuyenBiChan`; `layDanhSachQuyen` van giu contract cu |
| 4. Update `JwtService` claims / DNA | DONE | JWT ghi `roles`, `permissions`, `roleLevel`, `blacklist`; DNA tao tu ca 4 manh + `v_adn` |
| 5. Update `DynamicRoleFilter` read / compare / reissue | DONE | Filter doc 4 claims, danh dau token thieu snapshot la token cu, doi soat DB khi can va cap token moi |
| 6. Update `JwtAuthenticationFilter` authorities | DONE | Role authority co prefix `ROLE_`; permission authority giu raw; request attr luu roles/permissions/roleLevel |
| 7. Update app adapter / provider / `UserService` | DONE | `User` co transient roles/permissions/blacklist/roleLevel; `UserServiceImpl.napQuyenChoNguoiDung()` nap va expose 4 manh; `UserSecurityAdapter` map 4 manh |
| 8. Update `PermissionInterceptor` | DONE | Interceptor doc `roleLevel` tu request attr/JWT, SUPERADMIN bypass theo `roleLevel == 0`, fallback DB khi attr thieu |
| 9. Mutation invalidation for affected users | DONE | `ApiRoleManagementController` danh dau user bi anh huong khi update role/permission/delete permission/toggle blacklist |
| 10. Tests | NOT_RECORDED | Khong thay file test moi trong working tree hien tai |
| 11. Security / regression review | NEEDS_REVIEW | Chua co ket qua review rieng trong memory hien tai |
| 12. Final sync memory | DONE | Cap nhat active plan, deep knowledge auth/RBAC va evolution log |

## Verification

| Command | Status | Result |
|---------|--------|--------|
| `bash mvnw -q -DskipTests compile` | BLOCKED_BY_NETWORK | Maven khong tai duoc parent POM tu Maven Central: `Permission denied: getsockopt` |

## Handover

- Source code khong duoc sua trong buoc memory sync nay.
- Compile chua duoc xac nhan pass; trang thai verify la `BLOCKED_BY_NETWORK`.
