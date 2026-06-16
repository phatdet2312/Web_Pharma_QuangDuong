# Project Map
> Last updated: 2026-06-04
> Status: BOOTSTRAPPED
> Sync basis: Current code snapshot under `src/` and `pom.xml`; git history intentionally not used.

## Top-Level Structure

```text
project-root/
├── .agents/skills/                 # Codex workflow skills
├── .ai-memory/                     # Agent memory bank
├── agent-config/                   # Codex config/hooks/rules area
├── docs/                           # Use only files marked Status: ACTIVE
├── CSDL/                           # SQL Server schema/data files; ignored for broad scans
├── uploads/                        # Runtime uploaded files; ignored for broad scans
├── src/
│   ├── main/
│   │   ├── java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/
│   │   │   ├── adapter/
│   │   │   ├── config/
│   │   │   │   ├── init/DatabaseSeeder.java
│   │   │   │   ├── interceptor/PermissionInterceptor.java
│   │   │   │   ├── ultraSecureLibrary/
│   │   │   │   ├── vnpay/
│   │   │   │   ├── PermissionRegistry.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   ├── controller/api/
│   │   │   ├── controller/view/
│   │   │   ├── dto/request/
│   │   │   ├── dto/response/
│   │   │   ├── entities/
│   │   │   ├── exception/
│   │   │   ├── repositories/IRepository/
│   │   │   ├── service/itf/
│   │   │   ├── service/impl/
│   │   │   ├── service/support/
│   │   │   ├── utils/
│   │   │   └── validators/
│   │   └── resources/
│   │       ├── static/css/
│   │       ├── static/js/
│   │       ├── templates/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Inventory From Current Code

| Area | Count / note |
|------|--------------|
| API controllers | 15 Java files |
| View controllers | 7 Java files |
| Entities | 55 Java files |
| Repositories | 56 Java files |
| DTO request | 36 Java files |
| DTO response | 61 Java files |
| `@RequirePermission` usage | 126 annotations under controllers/views |
| Permission registry | 44 permission codes |
| `ROLE_MANAGE` in source/templates/static JS | 0 matches |

## Module Map

| Module/Package | Path | Role | Key files |
|----------------|------|------|-----------|
| Main app | `src/main/java/.../` | Spring Boot entry point | `PhatdevPharmaceuticalsWebApplication.java` |
| API controllers | `controller/api` | REST/JSON endpoints under `/api/**` | `ApiAuthController`, `ApiPostController`, `ApiEventController`, `ApiCommentController`, `ApiProfileController`, `ApiReportController`, `ApiRolesController`, `ApiAuditController`, `ApiPublicSpeakerAgendaController`, `ApiAdminPostController`, `ApiAdminEventController`, `ApiAdminCommentController`, `ApiAdminReportController`, `ApiAdminSpeakerAgendaController`, `ApiRoleManagementController` |
| View controllers | `controller/view` | Thymeleaf route mapping | `HomeViewController`, `AuthViewController`, `PostViewController`, `EventViewController`, `AdminViewController`, `PartnerViewController`, `ErrorViewController` |
| Services interfaces | `service/itf` | Business service contracts | `IUserService`, `IPostService`, `IEventService`, `ICommentService`, `IProfileService`, `IAddressService`, `IAuditService`, `IAdminPostService`, `IAdminEventService`, `IAdminReportService`, `IPublicReportService`, `IRoleManagementService`, `IRolesService`, `ISpeakerAgendaService`, `IUserTrackingService` |
| Services impl | `service/impl` | Business logic, transactions, DTO mapping | `AdminPostServiceImpl` 1288L, `AdminEventServiceImpl` 1318L, `CommentServiceImpl` 1595L, `RoleManagementServiceImpl` 946L, `UserServiceImpl` 654L, plus public/profile/report services |
| Service support | `service/support` | Reusable policy/context classes | `EventStatusDisplayPolicy`, `NguCanhNguoiDung`, `NguCanhNguoiDungFactory` |
| Utils | `utils` | Shared static/config utility | `SecurityConfig`, `ImagePathUtil`, `PagingUtil` |
| Security infra | `config/ultraSecureLibrary`, `adapter` | JWT, filters, custom library integration | `JwtService`, `CookieUtils`, `SecurityAuthoritySnapshot`, `SecurityTokenClaim`, `SecurityTokenVersion`, `SecurityConfigurer`, `JwtAuthenticationFilter`, `DynamicRoleFilter`, `BodyIntegrityFilter`, `UserSecurityAdapter`, `SecurityUserProviderImpl` |
| RBAC config | `config`, `config/rbac`, `validators/annotations` | Permission registry, app RBAC snapshot and enforcement | `PermissionRegistry`, `RbacSecuritySnapshot`, `PermissionInterceptor`, `WebMvcConfig`, `RequirePermission` |
| Payment | `config/vnpay` | VNPay integration | `VNPayService`, `VNPayLibrary`, `VNPayUtils`, `VNPayModels` |
| Seed/init | `config/init` | Core data initialization | `DatabaseSeeder` |
| Exception | `exception` | API error wrapping | `AppException`, `GlobalExceptionHandler` |

## Key DTO Requests

`AddressRequest`, `BulkActionRequest`, `BulkLockRequest`, `CategoryRequest`, `ChangePasswordRequest`, `CommentModerationRequest`, `CommentReportRequest`, `CommentRequest`, `CtEventRequest`, `EditContentRequest`, `EventAgendaRequest`, `EventRegistrationRequest`, `EventRequest`, `EventSpeakerRequest`, `EventStatusRequest`, `EventTypeRequest`, `ForgotPasswordRequest`, `LikeRequest`, `LoaiLikeRequest`, `LocationRequest`, `LoginRequest`, `OtpVerificationRequest`, `PermissionModuleRequest`, `PermissionRequest`, `PostRequest`, `PublicProfileRequest`, `RegisterRequest`, `RegisterTemp`, `ReplyRequest`, `ReportResolutionRequest`, `ResetPasswordRequest`, `RoleRequest`, `TagRequest`, `UpdatePartnerRequest`, `UpdatePersonalRequest`, `UserBlacklistRequest`.

## Key DTO Responses

Includes `ApiResponse`, post/admin post DTOs (`AdminPostDictionaryResponse`, `AdminPostMediaResponse`, `PostCommentPreviewResponse`, `PostLinkedEventResponse`, `PostReactionSummary`, `RoleOptionResponse`), event/admin event DTOs (`AdminEventDictionaryResponse`, `AdminEventMediaResponse`, `StatusOptionResponse`), comment/moderation DTOs, profile/address DTOs, audit/history DTOs, and RBAC DTOs (`MyPermissionResponse`, `PermissionResponse`, `PermissionModuleResponse`, `RoleResponse`, `UserPermissionResponse`).

## Database Entity Groups

| Group | Main entities/tables |
|-------|----------------------|
| User/Auth/RBAC | `User`, `UserRole`, `Permission`, `PermissionModule`, `CtUserRole`, `CtRolePermission`, `CtUserPermissionBlacklist` |
| Audit/User history | `CtUserLoginLog`, `CtUserActionLog`, `CtUserModerationLog`, `OtpCode` |
| Posts | `Post`, `Category`, `Tag`, `CtPostTag`, `CtPostRole`, `PostImage`, `PostFile`, `PostViewLog`, `CtFileDownload`, `CtLikePost`, `CtPostCmt`, `CtPostEvent` |
| Events | `Event`, `CtEvent`, `EventType`, `Location`, `EventSpeaker`, `EventAgenda`, `CtAgendaSpeaker`, `CtEventTag`, `CtEventSessionRole`, `CtEventStatusHistory`, `CtEventRegistration`, `CtEventCmt` |
| Comments/reactions | `Cmt`, `PhCmt`, `LoaiLike`, `CtLikeCmt`, `CtLikePhCmt` |
| Reports/moderation | `CtCmtReport`, `CtPhCmtReport`, `CtCmtReportModLog`, `CtPhCmtReportModLog`, `CtCmtModerationLog`, `CtPhCmtModerationLog`, `CtCmtActionLog`, `CtPhCmtActionLog`, `ModerationAction` |
| Profile/address | `PublicProfile`, `PartnerProfile`, `Address`, `Province`, `District`, `Ward` |

## Template And Static Assets

| Area | Current files / notes |
|------|-----------------------|
| Admin templates | `admin/posts.html` 2565L, `admin/events.html` 3663L, `admin/comments.html` 2620L, `admin/role-management.html` 1636L, `admin/users.html` 1059L, `admin/user-details.html` 1792L, dashboard/test/demo pages |
| Public templates | `posts/detail.html` 3915L, `posts/list.html`, `events/detail.html` 3818L, `events/list.html`, `events/my_registrations.html`, `home/index.html`, `partner/profile.html`, auth/error/email layouts |
| Static JS | `security-core.js`, `auth-sync.js`, `page-transition-manager.js` 350L, `permission-manager.js` 183L, `rich-content-editor.js` 1017L, jQuery/Bootstrap |
| Static CSS | `design-system.css`, `admin.css`, `user.css`, `page-transitions.css`, `rich-content-editor.css` 196L, checkout/order/bootstrap CSS |

## Critical Entry Points

| Purpose | File |
|---------|------|
| Main application | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/PhatdevPharmaceuticalsWebApplication.java` |
| Security config | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/SecurityConfig.java` |
| Permission enforcement | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/interceptor/PermissionInterceptor.java` |
| Permission registry | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/PermissionRegistry.java` |
| MVC interceptor/static uploads | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/WebMvcConfig.java` |
| JWT internals | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Service/JwtService.java` |
| App RBAC snapshot | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/rbac/RbacSecuritySnapshot.java` |
| Global exception | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/exception/GlobalExceptionHandler.java` |
| App config | `src/main/resources/application.properties` |
| Test entry | `src/test/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/AuctionSystemNhom6ApplicationTests.java` |

## Deep Knowledge Router

Read `.ai-memory/03_deep_knowledge/INDEX.md` first, then open only the relevant file:

- Auth/Security: `auth_security.md`
- Posts/content: `posts_content.md`
- Events/registration: `events_registration.md`
- Comments/moderation: `comments_moderation.md`
- Admin RBAC/user permissions: `admin_rbac.md`
- Profile/address: `profile_address.md`
- Data model: `data_model.md`
