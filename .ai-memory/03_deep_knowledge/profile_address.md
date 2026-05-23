# Profile & Address
> Last updated: 2026-05-18
> Source files: `controller/api/ApiProfileController.java`, `controller/view/PartnerViewController.java`, `service/impl/ProfileServiceImpl.java`, `service/impl/AddressServiceImpl.java`, `entities/PublicProfile.java`, `entities/PartnerProfile.java`, `entities/Address.java`, `entities/Province.java`, `entities/District.java`, `entities/Ward.java`
> Confidence: HIGH

## Mô tả chức năng

Module profile quản lý hồ sơ cá nhân, hồ sơ doanh nghiệp/partner, đổi mật khẩu, lịch sử OTP/login/account, permission summary, stats và địa chỉ. View `/profile` hoặc `/partner` trả template partner profile; dữ liệu dùng API `/api/profile/**`.

## Luồng xử lý chính

1. User authenticated gọi `GET /api/profile/me` để lấy thông tin profile.
2. Cập nhật cá nhân/partner/password/address qua `ApiProfileController`.
3. Controller dùng DTO request có validation như `UpdatePersonalRequest`, `UpdatePartnerRequest`, `ChangePasswordRequest`, `AddressRequest`.
4. Service kiểm tra user hiện tại, thao tác profile/address repositories, trả response DTO.
5. Address API hỗ trợ province/district/ward lookup và CRUD địa chỉ user.

## Business Rules quan trọng

- `/api/profile/**` yêu cầu role `USER`, `EMPLOYEE`, `ADMIN` hoặc `SUPERADMIN`.
- Mọi thao tác profile/address phải dùng current authenticated user, không tin userId từ client nếu không có rule admin riêng.
- Partner profile có file/avatar/license upload endpoints; cần kiểm soát path/upload theo `.codexignore` và rule bảo mật khi sửa.
- Address CRUD phải đảm bảo record thuộc user hiện tại.

## API Endpoints

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| GET | `/api/profile/me` | Hồ sơ cá nhân | Yes |
| PUT | `/api/profile/me` | Cập nhật cá nhân | Yes |
| GET | `/api/profile/partner` | Hồ sơ doanh nghiệp | Yes |
| PUT | `/api/profile/partner` | Cập nhật doanh nghiệp | Yes |
| POST | `/api/profile/partner/avatar` | Upload avatar partner | Yes |
| POST | `/api/profile/partner/license` | Upload license partner | Yes |
| PUT | `/api/profile/security/password` | Đổi mật khẩu | Yes |
| GET | `/api/profile/security/otp-history` | Lịch sử OTP | Yes |
| GET | `/api/profile/security/login-history` | Lịch sử đăng nhập | Yes |
| GET | `/api/profile/account-history` | Lịch sử tài khoản | Yes |
| GET | `/api/profile/permissions` | Quyền hiện tại | Yes |
| GET | `/api/profile/stats` | Profile stats | Yes |
| GET | `/api/profile/address/provinces` | Provinces | Yes |
| GET | `/api/profile/address/districts` | Districts | Yes |
| GET | `/api/profile/address/wards` | Wards | Yes |
| GET/POST/PUT/DELETE/PATCH | `/api/profile/address/**` | Address CRUD/default | Yes |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |

## Ghi chú

- Profile/address liên quan dữ liệu cá nhân; khi sửa response cần tránh leak dữ liệu user khác.
