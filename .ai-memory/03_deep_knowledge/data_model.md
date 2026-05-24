# Data Model
> Last updated: 2026-05-25
> Source files: `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/*`, `repositories/IRepository/*`, `src/main/resources/application.properties`, `CSDL/FileKhoiTaoCSDL.sql`
> Confidence: HIGH

## Mo ta chuc nang

Data layer dung Spring Data JPA anh xa SQL Server. Entity dung JPA annotation va Lombok; repository extend `JpaRepository`. Schema do SQL/script quan ly, khong de Hibernate tu sinh.

## Business Rules quan trong

- `spring.jpa.hibernate.ddl-auto=none`: schema/script la nguon quan ly DB.
- Lazy relationship pho bien; can tranh access ngoai transaction khi them mapping moi.
- Nhieu join table bat dau bang `Ct...`, vi du `CtUserRole`, `CtPostRole`, `CtEventRegistration`.
- Nhieu business rule dua vao status string thay vi enum; khi sua phai doi chieu repository/query/template/frontend.
- DatabaseSeeder quan ly role/permission/moderation actions; data mau khong duoc ghi de seed core.
- Neu data mau can test paywall/report flow thi phai co rows nghiep vu phu hop (`CT_POST_ROLES`, `CT_EVENT_SESSION_ROLES`, report moderation log) ma khong sua seed core.

## Entity Groups

| Nhom | Tables/Entities chinh | Ghi chu |
|------|------------------------|---------|
| User/Auth | `USERS`, `USER_ROLES`, `PERMISSIONS`, `CT_USER_ROLES`, `CT_ROLE_PERMISSIONS`, `CT_USER_PERMISSION_BLACKLIST` | Dynamic role/permission, blacklist theo user |
| Audit/User history | `CT_USER_LOGIN_LOG`, `CT_USER_ACTION_LOG`, `CT_USER_MODERATION_LOG`, `OTP_CODES` | Login/action/moderation/OTP history |
| Posts | `POSTS`, `CATEGORIES`, `TAGS`, `CT_POST_TAGS`, `CT_POST_ROLES`, `POST_IMAGES`, `POST_FILES`, `POST_VIEW_LOGS`, `CT_FILE_DOWNLOADS` | Content, category/tag, paywall, assets, analytics |
| Events | `EVENTS`, `CT_EVENTS`, `EVENT_TYPES`, `LOCATIONS`, `EVENT_SPEAKERS`, `EVENT_AGENDAS`, `CT_AGENDA_SPEAKERS`, `CT_EVENT_TAGS`, `CT_EVENT_SESSION_ROLES`, `CT_EVENT_STATUS_HISTORY`, `CT_EVENT_REGISTRATIONS`, `CT_POST_EVENTS` | Campaign/session, speaker/agenda, status history, capacity, registration |
| Comments | `CMT`, `PH_CMT`, `CT_POST_CMT`, `CT_EVENT_CMT`, `CT_LIKECMT`, `CT_LIKEPHCMT`, `CT_LIKEPOST`, `LOAI_LIKE` | Comment/reply, reaction types |
| Moderation/Reports | `CT_CMT_REPORTS`, `CT_PH_CMT_REPORTS`, `CT_CMT_REPORT_MOD_LOG`, `CT_PH_CMT_REPORT_MOD_LOG`, `CT_CMT_MODERATION_LOG`, `CT_PH_CMT_MODERATION_LOG`, `CT_CMT_ACTION_LOG`, `CT_PH_CMT_ACTION_LOG`, `MODERATION_ACTIONS` | Report/report-mod-log/action/moderation audit |
| Profile/Address | `PUBLIC_PROFILES`, `PARTNER_PROFILES`, `ADDRESSES`, `PROVINCES`, `DISTRICTS`, `WARDS` | User public/partner profile and location tree |

## Ghi chu ve CSDL/

- `DuLieuMau.sql` va cac file lien quan (`BAO_CAO_KIEM_TRA_DULIEUMAU.md`, `DULIEUMAU_POST_SOURCE_MATRIX.md`, `TEST.MD`) da bi user xoa khoi repo ngay 2026-05-25.
- Chi con `FileKhoiTaoCSDL.sql` (schema) va file Excel san pham.
- Schema DB do SQL script quan ly (`ddl-auto=none`), khong de Hibernate tu sinh.

## Decision Log

| Quyet dinh | Phuong an (chon / bo) | Ly do | Ngay ghi | Het han |
|-----------|------------------------|-------|----------|---------|
| Data mau phai co audit validator truoc khi chap nhan | Chon: validate status, row relation, future event, capacity, paywall, moderation log, uniqueness/content repetition; Bo: chi dat row count roi xem la dat | Audit 2026-05-20 cho thay file lon van fail business rule va chat luong noi dung | 2026-05-20 | 2026-08-20 |
