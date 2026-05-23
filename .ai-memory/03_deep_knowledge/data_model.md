# Data Model
> Last updated: 2026-05-21
> Source files: `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/*`, `repositories/IRepository/*`, `src/main/resources/application.properties`, `CSDL/FileKhoiTaoCSDL.sql`, `CSDL/DuLieuMau.sql`
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
| Events | `EVENTS`, `CT_EVENTS`, `EVENT_TYPES`, `LOCATIONS`, `CT_EVENT_TAGS`, `CT_EVENT_SESSION_ROLES`, `CT_EVENT_STATUS_HISTORY`, `CT_EVENT_REGISTRATIONS`, `CT_POST_EVENTS` | Campaign/session, status history, capacity, registration |
| Comments | `CMT`, `PH_CMT`, `CT_POST_CMT`, `CT_EVENT_CMT`, `CT_LIKECMT`, `CT_LIKEPHCMT`, `CT_LIKEPOST` | Comment/reply and reaction |
| Moderation/Reports | `CT_CMT_REPORTS`, `CT_PH_CMT_REPORTS`, `CT_CMT_MODERATION_LOG`, `CT_PH_CMT_MODERATION_LOG`, `CT_CMT_ACTION_LOG`, `CT_PH_CMT_ACTION_LOG`, `MODERATION_ACTIONS` | Report/action/moderation audit |
| Profile/Address | `PUBLIC_PROFILES`, `PARTNER_PROFILES`, `ADDRESSES`, `PROVINCES`, `DISTRICTS`, `WARDS` | User public/partner profile and location tree |

## Audit Notes 2026-05-21

- Sau fix truc tiep, `CSDL/DuLieuMau.sql` PASS validator noi bo: `GO=0`, transaction/idempotency co du, `EVENTS=100`, `CT_EVENTS=100`, status backend-compatible, timeline hop `TEST.MD`.
- Cac bang core do `DatabaseSeeder` quan ly duoc de dang guarded simulation theo correction cua user: neu du lieu seed tuong duong da ton tai thi khong insert trung va khong lam lech ket qua cuoi.
- Data chinh sau fix: `USERS=501`, `POSTS=300`, `EVENTS=100`, `CT_EVENTS=100`, `CT_EVENT_STATUS_HISTORY=200`, `CT_EVENT_REGISTRATIONS=19577`, `CMT=20088`, `PH_CMT=20000`.
- Duplicate/content validator sau fix: duplicate post title 0, post <1000 tu 0, repeated paragraph/heading exact 0, duplicate exact `CMT=0`, duplicate exact `PH_CMT=0`.
- Timeline/capacity sau fix: 20 future sessions, 0 session sau `2026-07-30`, 0 non-event timestamp sau `2026-05-20`, 0 over-capacity, 0 future `ATTENDED`, 0 registration sau start.
- Loi thieu source mapping da duoc sua: `CSDL/DULIEUMAU_POST_SOURCE_MATRIX.md` map 300/300 post vao Excel noi bo + nguon chinh thong duoc user cho phep tin dung; `DuLieuMau.sql` co comment tro toi file nay.
- Bao cao moi nhat nam tai `CSDL/BAO_CAO_KIEM_TRA_DULIEUMAU.md`; chua chay SQL vao SSMS/DB, user tu chay runtime.
- `CSDL/` va `*.sql` nam trong `.codexignore` cho quet rong; chi mo SQL cu the khi task yeu cau ro.

## Decision Log

| Quyet dinh | Phuong an (chon / bo) | Ly do | Ngay ghi | Het han |
|-----------|------------------------|-------|----------|---------|
| Data mau phai co audit validator truoc khi chap nhan | Chon: validate status, row relation, future event, capacity, paywall, moderation log, uniqueness/content repetition; Bo: chi dat row count roi xem la dat | Audit 2026-05-20 cho thay file lon van fail business rule va chat luong noi dung | 2026-05-20 | 2026-08-20 |
