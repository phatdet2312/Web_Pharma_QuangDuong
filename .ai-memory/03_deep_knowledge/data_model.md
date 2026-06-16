# Data Model
> Last updated: 2026-06-04
> Source files: `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/*`, `repositories/IRepository/*`, `src/main/resources/application.properties` (property names only), SQL schema files when explicitly needed
> Confidence: HIGH

## Summary

Data layer uses Spring Data JPA over SQL Server. Entities use JPA annotations and Lombok. Repositories extend `JpaRepository`. Hibernate does not own schema creation: current config has `spring.jpa.hibernate.ddl-auto=none`.

## Current Inventory

- Entities: 55 Java files.
- Repositories: 56 Java files.
- Request DTOs: 36 Java files.
- Response DTOs: 61 Java files.

## Rules

- Schema is managed by SQL/scripts, not Hibernate auto-DDL.
- Many relationships are lazy; avoid accessing lazy relations outside transaction when adding mappings.
- Join table entities commonly start with `Ct...`, for example `CtUserRole`, `CtPostRole`, `CtEventRegistration`.
- Many statuses are strings, not enums. When adding/changing status values, update service validation, repository queries and frontend mappings together.
- `DatabaseSeeder` initializes core admin/default/moderation/permission data; do not use it for large sample data that overwrites admin customization.
- Config files contain sensitive values; inspect property names only unless user explicitly asks and values are masked.

## Entity Groups

| Group | Main entities/tables | Notes |
|-------|----------------------|-------|
| User/Auth/RBAC | `User`, `UserRole`, `Permission`, `PermissionModule`, `CtUserRole`, `CtRolePermission`, `CtUserPermissionBlacklist` | Dynamic permission system and blacklist |
| Audit/User history | `CtUserLoginLog`, `CtUserActionLog`, `CtUserModerationLog`, `OtpCode` | Login/action/moderation/OTP history |
| Posts | `Post`, `Category`, `Tag`, `CtPostTag`, `CtPostRole`, `PostImage`, `PostFile`, `PostViewLog`, `CtFileDownload`, `CtLikePost`, `CtPostCmt`, `CtPostEvent` | Content, gates, assets, analytics, linked events |
| Events | `Event`, `CtEvent`, `EventType`, `Location`, `EventSpeaker`, `EventAgenda`, `CtAgendaSpeaker`, `CtEventTag`, `CtEventSessionRole`, `CtEventStatusHistory`, `CtEventRegistration`, `CtEventCmt` | Campaign/session, capacity, status, speakers, agenda |
| Comments/reactions | `Cmt`, `PhCmt`, `LoaiLike`, `CtLikeCmt`, `CtLikePhCmt` | Root comments, reply tree and reactions |
| Reports/moderation | `CtCmtReport`, `CtPhCmtReport`, `CtCmtReportModLog`, `CtPhCmtReportModLog`, `CtCmtModerationLog`, `CtPhCmtModerationLog`, `CtCmtActionLog`, `CtPhCmtActionLog`, `ModerationAction` | Report and moderation audit |
| Profile/address | `PublicProfile`, `PartnerProfile`, `Address`, `Province`, `District`, `Ward` | User public/partner profile and location tree |

## Config Properties Observed Without Values

Important property groups in `application.properties`: datasource, Hibernate dialect/ddl-auto, mail, OAuth2 Google, JWT, VNPay, multipart limits, Tomcat limits, trusted proxy, admin default user, custom security library toggles.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Schema source is SQL/script, not Hibernate | Keep `ddl-auto=none` | Prevent accidental DB drift from entity changes | 2026-06-03 | 2026-09-03 |
| Permission module FK uses `Permission.moduleId` | `PERMISSIONS.moduleId -> PERMISSION_MODULES` | Current code and repository/service logic use FK ID, not free-form module string | 2026-06-03 | 2026-09-03 |
