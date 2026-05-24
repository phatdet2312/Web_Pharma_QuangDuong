# Project Map - Bản đồ dự án
> Last updated: 2026-05-18
> Status: BOOTSTRAPPED

## Cấu trúc thư mục chính

```text
project-root/
├── .agents/skills/                 # Skills workflow cho Codex
├── .ai-memory/                     # Memory bank của agent
├── .codex/                         # Agents, hooks, rules, launch scripts
├── docs/                           # Tài liệu dự án; chỉ dùng file có Status: ACTIVE khi có
├── CSDL/                           # SQL Server schema và dữ liệu mẫu chạy thủ công trong SSMS
├── src/
│   ├── main/
│   │   ├── java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/
│   │   │   ├── adapter/
│   │   │   ├── config/
│   │   │   ├── controller/api/
│   │   │   ├── controller/view/
│   │   │   ├── dto/request/
│   │   │   ├── dto/response/
│   │   │   ├── entities/
│   │   │   ├── exception/
│   │   │   ├── repositories/IRepository/
│   │   │   ├── service/itf/
│   │   │   ├── service/impl/
│   │   │   ├── utils/
│   │   │   └── validators/
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Module Map

| Module/Package | Đường dẫn | Vai trò | Files chính |
|----------------|-----------|---------|-------------|
| Main app | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals` | Spring Boot entry point | `PhatdevPharmaceuticalsWebApplication.java` |
| API controllers | `src/main/java/.../controller/api` | REST/JSON endpoints dưới `/api/**` | `ApiAuthController`, `ApiPostController`, `ApiEventController`, `ApiProfileController`, `ApiAdminPostController`, `ApiAdminEventController`, `ApiCommentController`, `ApiRoleManagementController` |
| View controllers | `src/main/java/.../controller/view` | Route trả Thymeleaf templates | `HomeViewController`, `AuthViewController`, `PostViewController`, `EventViewController`, `AdminViewController`, `PartnerViewController` |
| Services interfaces | `src/main/java/.../service/itf` | Hợp đồng business service | `IUserService`, `IPostService`, `IEventService`, `ICommentService`, `IRoleManagementService` |
| Services impl | `src/main/java/.../service/impl` | Business logic, transaction, DTO mapping | `UserServiceImpl`, `PostServiceImpl`, `EventServiceImpl`, `CommentServiceImpl`, `AdminPostServiceImpl`, `AdminEventServiceImpl`, `RoleManagementServiceImpl` |
| Service support | `src/main/java/.../service/support` | Policy/helper component dung chung cho service, khong truy cap DB truc tiep | `EventStatusDisplayPolicy`, `NguCanhNguoiDung`, `NguCanhNguoiDungFactory` |
| Repositories | `src/main/java/.../repositories/IRepository` | Spring Data JPA access layer | `IUserRepository`, `IPostRepository`, `IEventRepository`, `ICtEventRepository`, `ICmtRepository` |
| Entities | `src/main/java/.../entities` | JPA table mapping | `User`, `UserRole`, `Permission`, `Post`, `Event`, `CtEvent`, `Cmt`, `PhCmt`, `CtEventRegistration` |
| DTO request | `src/main/java/.../dto/request` | Client input objects + validation | `LoginRequest`, `RegisterRequest`, `PostRequest`, `EventRequest`, `CommentRequest`, `RoleRequest` |
| DTO response | `src/main/java/.../dto/response` | API output objects | `ApiResponse`, `PostResponse`, `PostDetailResponse`, `EventResponse`, `CtEventResponse`, `UserResponse` |
| Exception | `src/main/java/.../exception` | Global API error format | `AppException`, `GlobalExceptionHandler` |
| Security | `src/main/java/.../utils`, `src/main/java/.../config/ultraSecureLibrary`, `src/main/java/.../adapter` | Spring Security, JWT cookie, dynamic permission | `SecurityConfig`, `JwtService`, `CookieUtils`, `DynamicRoleFilter`, `JwtAuthenticationFilter`, `UserSecurityAdapter` |
| Payment | `src/main/java/.../config/vnpay` | VNPay integration | `VNPayService`, `VNPayLibrary`, `VNPayUtils`, `VNPayModels` |
| Seed/init | `src/main/java/.../config/init` | Initial database/user seed | `DatabaseSeeder` |
| SQL scripts | `CSDL/` | Schema SQL Server và seed data thủ công | `FileKhoiTaoCSDL.sql`, `DuLieuMau.sql` |
| Validators | `src/main/java/.../validators` | Custom Jakarta validation | `ValidUsername`, `ValidUsernameValidator` |
| Templates | `src/main/resources/templates` | Thymeleaf pages | `auth/*`, `posts/*`, `events/*`, `admin/*`, `partner/profile.html`, layouts |
| Static assets | `src/main/resources/static` | CSS/JS/images | `css/*`, `js/security-core.js`, `js/auth-sync.js`, Bootstrap/jQuery |
| Tests | `src/test/java/...` | Spring Boot tests | `AuctionSystemNhom6ApplicationTests.java` |

## Entry Points quan trọng

| Mục đích | File path |
|----------|-----------|
| Main Application | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/PhatdevPharmaceuticalsWebApplication.java` |
| Security Config | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/SecurityConfig.java` |
| JWT/Auth internals | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Service/JwtService.java` |
| Global Exception | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/exception/GlobalExceptionHandler.java` |
| API Response Wrapper | `src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ApiResponse.java` |
| App Config | `src/main/resources/application.properties` |
| SQL Schema | `CSDL/FileKhoiTaoCSDL.sql` |
| Manual Seed Data | `CSDL/DuLieuMau.sql` |
| Test Entry | `src/test/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/AuctionSystemNhom6ApplicationTests.java` |

## Database Entities

| Entity | Table | Quan hệ chính |
|--------|-------|---------------|
| `User` | `USERS` | Implements `UserDetails`; quyền động qua `CtUserRole`, `CtRolePermission`, blacklist permission |
| `UserRole` | `USER_ROLES` | Role level dùng cho dynamic access/paywall |
| `Permission` | `PERMISSIONS` | Quyền thao tác hạt lựu, gắn qua `CtRolePermission` |
| `CtUserRole` | `CT_USER_ROLES` | Join user-role |
| `CtRolePermission` | `CT_ROLE_PERMISSIONS` | Join role-permission |
| `CtUserPermissionBlacklist` | `CT_USER_PERMISSION_BLACKLIST` | Chặn permission ở cấp user |
| `Post` | `POSTS` | Many-to-one `Category`, `User` author; liên kết tag/role/file/image/view/comment |
| `Category` | `CATEGORIES` | Danh mục bài viết |
| `Tag` | `TAGS` | Dùng cho bài viết và sự kiện qua join table |
| `CtPostTag` | `CT_POST_TAGS` | Join post-tag |
| `CtPostRole` | `CT_POST_ROLES` | Paywall role cho bài viết |
| `PostImage` | `POST_IMAGES` | Ảnh bài viết |
| `PostFile` | `POST_FILES` | Tài liệu đính kèm bài viết |
| `PostViewLog` | `POST_VIEW_LOGS` | Log lượt xem |
| `CtFileDownload` | `CT_FILE_DOWNLOADS` | Log tải tài liệu |
| `Event` | `EVENTS` | Campaign, many-to-one `EventType`, có nhiều `CtEvent` |
| `CtEvent` | `CT_EVENTS` | Session, many-to-one `Event` và `Location` |
| `EventType` | `EVENT_TYPES` | Loại sự kiện |
| `Location` | `LOCATIONS` | Địa điểm/online location |
| `CtEventRegistration` | `CT_EVENT_REGISTRATIONS` | Đăng ký session, nullable `User` cho guest |
| `CtEventStatusHistory` | `CT_EVENT_STATUS_HISTORY` | Current status lấy từ bản ghi mới nhất |
| `CtEventSessionRole` | `CT_EVENT_SESSION_ROLES` | Paywall role cho session |
| `CtEventTag` | `CT_EVENT_TAGS` | Join event/session-tag |
| `CtPostEvent` | `CT_POST_EVENTS` | Liên kết bài viết liên quan tới event/session |
| `Cmt` | `CMT` | Bình luận gốc, liên kết user/target |
| `PhCmt` | `PH_CMT` | Reply/comment con |
| `CtLikePost`, `CtLikeCmt`, `CtLikePhCmt` | `CT_LIKEPOST`, `CT_LIKECMT`, `CT_LIKEPHCMT` | Reaction/like theo target |
| `CtCmtReport`, `CtPhCmtReport` | `CT_CMT_REPORTS`, `CT_PH_CMT_REPORTS` | Báo cáo comment/reply |
| `CtCmtModerationLog`, `CtPhCmtModerationLog` | `CT_CMT_MODERATION_LOG`, `CT_PH_CMT_MODERATION_LOG` | Audit moderation |
| `PublicProfile`, `PartnerProfile` | `PUBLIC_PROFILES`, `PARTNER_PROFILES` | Hồ sơ public/chuyên gia và doanh nghiệp |
| `Address`, `Province`, `District`, `Ward` | `ADDRESSES`, `PROVINCES`, `DISTRICTS`, `WARDS` | Địa chỉ người dùng |
| `OtpCode` | `OTP_CODES` | OTP register/forgot password |
| `CtUserLoginLog`, `CtUserActionLog`, `CtUserModerationLog` | audit tables | Nhật ký đăng nhập/hành động/kiểm duyệt user |

## Deep Knowledge Router

Đọc `.ai-memory/03_deep_knowledge/INDEX.md` trước, rồi mở đúng file module cần sửa:

- Auth/Security: `auth_security.md`
- Bài viết/content: `posts_content.md`
- Sự kiện/đăng ký: `events_registration.md`
- Comment/moderation: `comments_moderation.md`
- Admin roles/permissions: `admin_rbac.md`
- Profile/address: `profile_address.md`
- Data model tổng quan: `data_model.md`
