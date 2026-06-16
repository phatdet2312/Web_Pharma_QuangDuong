# Profile & Address
> Last updated: 2026-06-04
> Source files: `controller/api/ApiProfileController.java`, `controller/view/PartnerViewController.java`, `service/impl/ProfileServiceImpl.java`, `service/impl/AddressServiceImpl.java`, `entities/PublicProfile.java`, `entities/PartnerProfile.java`, `entities/Address.java`, `entities/Province.java`, `entities/District.java`, `entities/Ward.java`, `templates/partner/profile.html`
> Confidence: HIGH

## Summary

Profile module manages personal profile, partner/business profile, password change, OTP/login/account history, permission summary, profile stats and address CRUD. View routes `/profile` and `/partner` return the partner profile template; data comes from `/api/profile/**`.

## Main Flow

1. Authenticated user calls `GET /api/profile/me`.
2. Updates go through `ApiProfileController` using validated request DTOs such as `UpdatePersonalRequest`, `UpdatePartnerRequest`, `ChangePasswordRequest`, `AddressRequest`.
3. Services use current authenticated user; do not trust arbitrary client user IDs.
4. Address service enforces ownership for CRUD/default operations.

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/profile/me` | Personal profile | Yes |
| PUT | `/api/profile/me` | Update personal profile | Yes |
| GET | `/api/profile/partner` | Partner/business profile | Yes |
| PUT | `/api/profile/partner` | Update partner profile | Yes |
| POST | `/api/profile/partner/avatar` | Upload partner avatar | Yes |
| POST | `/api/profile/partner/license` | Upload partner license | Yes |
| PUT | `/api/profile/security/password` | Change password | Yes |
| GET | `/api/profile/security/otp-history` | OTP history | Yes |
| GET | `/api/profile/security/login-history` | Login history | Yes |
| GET | `/api/profile/account-history` | Account history | Yes |
| GET | `/api/profile/permissions` | Current permissions | Yes |
| GET | `/api/profile/stats` | Profile stats | Yes |
| GET | `/api/profile/address/provinces` | Provinces | Yes |
| GET | `/api/profile/address/districts` | Districts | Yes |
| GET | `/api/profile/address/wards` | Wards | Yes |
| GET/POST/PUT/DELETE/PATCH | `/api/profile/address/**` | Address CRUD/default | Yes |

## Business Rules

- `/api/profile/**` requires authentication in `SecurityConfig`.
- Profile/address operations must use the authenticated user.
- Partner avatar/license upload touches runtime upload paths; apply path/size/type checks if modifying.
- Do not leak other users' profile/address/history data in response DTOs.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| No module-specific decision currently active | N/A | Current file reflects observed code | 2026-06-03 | N/A |
