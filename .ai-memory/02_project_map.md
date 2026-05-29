# Project Map - Bản đồ dự án
> Last updated: 2026-05-28
> Status: BOOTSTRAPPED

## Cấu trúc thư mục chính

```text
project-root/
├── .agents/skills/                 # Skills workflow cho Codex agent
├── .ai-memory/                     # Memory bank của agent
├── .claude/                        # Claude Code settings, hooks, rules
├── .codex/                         # Codex agent config
├── docs/                           # Tài liệu dự án; chỉ dùng file có Status: ACTIVE
├── CSDL/                           # SQL Server schema (seed data đã xóa 2026-05-25)
├── uploads/                        # File upload runtime (books/, files/, partners/)
├── src/
│   ├── main/
│   │   ├── java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/
│   │   │   ├── adapter/
│   │   │   ├── config/
│   │   │   │   ├── init/
│   │   │   │   ├── ultraSecureLibrary/
│   │   │   │   ├── vnpay/
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
│   │       ├── static/
│   │       │   ├── css/page-transitions.css    # Skeleton + fade CSS (prefix ptm-)
│   │       │   └── js/page-transition-manager.js # Utility chống nháy trang (IIFE)
│   │       ├── templates/
│   │       │   ├── admin/
│   │       │   ├── auth/
│   │       │   ├── email/
│   │       │   ├── errors/
│   │       │   ├── events/
│   │       │   ├── home/
│   │       │   ├── partner/
│   │       │   ├── posts/
│   │       │   ├── admin_layout.html
│   │       │   ├── auth_layout.html
│   │       │   └── user_layout.html
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
| Main app | `src/main/java/.../` | Spring Boot entry point | `PhatdevPharmaceuticalsWebApplication.java` |
| API controllers | `src/main/java/.../controller/api` | REST/JSON endpoints `/api/**` | `ApiAuthController`, `ApiPostController`, `ApiEventController`, `ApiCommentController`, `ApiProfileController`, `ApiReportController`, `ApiRolesController`, `ApiAuditController`, `ApiPublicSpeakerAgendaController`, `ApiAdminPostController`, `ApiAdminEventController`, `ApiAdminCommentController`, `ApiAdminReportController`, `ApiAdminSpeakerAgendaController`, `ApiRoleManagementController` |
| View controllers | `src/main/java/.../controller/view` | Route trả Thymeleaf templates | `HomeViewController`, `AuthViewController`, `PostViewController`, `EventViewController`, `AdminViewController`, `PartnerViewController`, `ErrorViewController` |
| Services interfaces | `src/main/java/.../service/itf` | Hợp đồng business service | `IUserService`, `IPostService`, `IEventService`, `ICommentService`, `IProfileService`, `IAddressService`, `IEmailService`, `IAuditService`, `IAdminPostService`, `IAdminEventService` (thêm dictionary/upload/bulkStatus), `IAdminReportService`, `IPublicReportService`, `IRoleManagementService`, `IRolesService`, `ISpeakerAgendaService`, `IUserTrackingService` |
| Services impl | `src/main/java/.../service/impl` | Business logic, transaction, DTO mapping | `UserServiceImpl`, `PostServiceImpl`, `EventServiceImpl`, `CommentServiceImpl`, `ProfileServiceImpl`, `AddressServiceImpl`, `EmailServiceImpl`, `AuditServiceImpl`, `AdminPostServiceImpl`, `AdminEventServiceImpl`, `AdminReportServiceImpl`, `PublicReportServiceImpl`, `RoleManagementServiceImpl`, `RolesServiceImpl`, `SpeakerAgendaServiceImpl`, `UserTrackingServiceImpl` |
| Service support | `src/main/java/.../service/support` | Policy/helper dùng chung, không truy cập DB trực tiếp | `EventStatusDisplayPolicy`, `NguCanhNguoiDung`, `NguCanhNguoiDungFactory` |
| Utils | `src/main/java/.../utils` | Lớp tiện ích tĩnh dùng chung | `SecurityConfig`, `ImagePathUtil` (validate/chuẩn hóa URL ảnh upload event), `PagingUtil` (chuẩn hóa page/size, default 12, max 100) |
| Repositories | `src/main/java/.../repositories/IRepository` | Spring Data JPA (53 files) | `IUserRepository`, `IPostRepository`, `IEventRepository`, `ICtEventRepository`, `ICmtRepository`, `IPhCmtRepository`, `ICategoryRepository`, `ITagRepository`, `IUserRoleRepository`, `IRolesRepository`, `IPermissionRepository`, `ICtUserRoleRepository`, `ICtRolePermissionRepository`, `ICtUserPermissionBlacklistRepository`, `ICtPostTagRepository`, `ICtPostRoleRepository`, `ICtPostEventRepository`, `ICtPostCmtRepository`, `IPostImageRepository`, `IPostFileRepository`, `IPostViewLogRepository`, `ICtFileDownloadRepository`, `ICtLikePostRepository`, `ICtLikeCmtRepository`, `ICtLikePhCmtRepository`, `ILoaiLikeRepository`, `ICtEventRegistrationRepository`, `ICtEventStatusHistoryRepository`, `ICtEventSessionRoleRepository`, `ICtEventTagRepository`, `ICtEventCmtRepository`, `IEventTypeRepository`, `ILocationRepository`, `IEventSpeakerRepository`, `IEventAgendaRepository`, `ICtAgendaSpeakerRepository`, `ICtCmtReportRepository`, `ICtPhCmtReportRepository`, `ICtCmtReportModLogRepository`, `ICtPhCmtReportModLogRepository`, `ICtCmtActionLogRepository`, `ICtPhCmtActionLogRepository`, `ICtCmtModerationLogRepository`, `ICtPhCmtModerationLogRepository`, `IModerationActionRepository`, `IOtpCodeRepository`, `IPublicProfileRepository`, `IPartnerProfileRepository`, `IAddressRepository`, `IProvinceRepository`, `IDistrictRepository`, `IWardRepository`, `ICtUserActionLogRepository`, `ICtUserLoginLogRepository`, `ICtUserModerationLogRepository` |
| Entities | `src/main/java/.../entities` | JPA table mapping (54 files) | `User`, `UserRole`, `Permission`, `Post`, `Category`, `Tag`, `Event`, `CtEvent`, `EventType`, `EventAgenda`, `EventSpeaker`, `Location`, `Cmt`, `PhCmt`, `CtEventCmt`, `CtPostCmt`, `CtEventRegistration`, `CtEventStatusHistory`, `CtEventSessionRole`, `CtEventTag`, `CtPostEvent`, `CtPostTag`, `CtPostRole`, `PostImage`, `PostFile`, `PostViewLog`, `CtFileDownload`, `CtLikePost`, `CtLikeCmt`, `CtLikePhCmt`, `LoaiLike`, `CtAgendaSpeaker`, `CtCmtReport`, `CtPhCmtReport`, `CtCmtReportModLog`, `CtPhCmtReportModLog`, `CtCmtActionLog`, `CtPhCmtActionLog`, `CtCmtModerationLog`, `CtPhCmtModerationLog`, `ModerationAction`, `CtRolePermission`, `CtUserRole`, `CtUserPermissionBlacklist`, `CtUserActionLog`, `CtUserLoginLog`, `CtUserModerationLog`, `PublicProfile`, `PartnerProfile`, `Address`, `Province`, `District`, `Ward`, `OtpCode` |
| DTO request | `src/main/java/.../dto/request` | Client input + validation (35 files) | `LoginRequest`, `RegisterRequest`, `RegisterTemp`, `PostRequest`, `EventRequest`, `CtEventRequest`, `CommentRequest`, `ReplyRequest`, `EditContentRequest`, `LikeRequest`, `LoaiLikeRequest`, `CommentReportRequest`, `CommentModerationRequest`, `ReportResolutionRequest`, `EventRegistrationRequest`, `EventStatusRequest`, `EventTypeRequest`, `EventSpeakerRequest`, `EventAgendaRequest`, `LocationRequest`, `CategoryRequest`, `TagRequest`, `RoleRequest`, `PermissionRequest`, `AddressRequest`, `ChangePasswordRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`, `OtpVerificationRequest`, `PublicProfileRequest`, `UpdatePartnerRequest`, `UpdatePersonalRequest`, `UserBlacklistRequest`, `BulkActionRequest`, `BulkLockRequest` |
| DTO response | `src/main/java/.../dto/response` | API output (53 files) | `ApiResponse`, `PostResponse`, `PostDetailResponse`, `PostStatsResponse`, `PostFileResponse`, `PostImageResponse`, `EventResponse`, `CtEventResponse`, `EventStatsResponse`, `EventStatusHistoryResponse`, `EventRegistrationResponse`, `EventAttendeePublicResponse`, `EventTypeResponse`, `EventSpeakerResponse`, `EventAgendaResponse`, `LocationResponse`, `AdminEventDictionaryResponse` (gom event+registration statuses), `AdminEventMediaResponse` (URL+fileName upload), `StatusOptionResponse` (code+label cho select), `CmtResponse`, `PhCmtResponse`, `CmtActionLogResponse`, `CmtModerationLogResponse` (moderation log comment/reply), `CommentReportResponse`, `CommentStatsResponse`, `AdminCmtContextResponse`, `ReportModLogResponse`, `ModerationActionResponse`, `CategoryResponse`, `TagResponse`, `LoaiLikeResponse`, `UserResponse`, `AdminMeResponse`, `ProfileMeResponse`, `ProfileStatsResponse`, `PublicProfileResponse`, `PartnerProfileResponse`, `UserPermissionResponse`, `RoleResponse`, `PermissionResponse`, `AddressDetailResponse`, `ProvinceResponse`, `DistrictResponse`, `WardResponse`, `AuditLogResponse`, `AuditLogPageResponse`, `LoginHistoryResponse`, `LoginHistoryPageResponse`, `OtpHistoryResponse`, `OtpHistoryPageResponse`, `AccountHistoryResponse`, `AccountHistoryPageResponse`, `ActivityFeedItemResponse` |
| Exception | `src/main/java/.../exception` | Global API error format | `AppException`, `GlobalExceptionHandler` |
| Security | `src/main/java/.../utils`, `.../config/ultraSecureLibrary`, `.../adapter` | Spring Security, JWT, OAuth2, custom filters | `SecurityConfig` (utils), `UserSecurityAdapter`, `SecurityUserProviderImpl` (adapter), `JwtService`, `CookieUtils`, `InterClusterSyncCamouflage`, `MaTranLuoiLocChongPhatLai`, `MaTranLuoiLocNghiaTrangQuyenHanCu`, `MaTranNhiPhanNguyenTu`, `TramPhatSongVoTuyenP2P`, `SecurityLibraryProperties` (ultraSecureLibrary/Service), `JwtAuthenticationFilter`, `DynamicRoleFilter`, `BodyIntegrityFilter`, `FormDataHashCheckerInterceptor`, `PublicApiSizeFilter`, `TomcatServerConfig` (ultraSecureLibrary/Filter), `SecurityConfigurer`, `LibraryFilterRegistryConfig`, `LibraryWebMvcAutoConfig` (ultraSecureLibrary/AutoConfig), `ISecurityUserAdapter` (ultraSecureLibrary/Adapter), `ISecurityUserProvider` (ultraSecureLibrary/Provider) |
| Payment | `src/main/java/.../config/vnpay` | VNPay integration | `VNPayService`, `VNPayLibrary`, `VNPayUtils`, `VNPayModels` |
| Seed/init | `src/main/java/.../config/init` | Initial database/user seed | `DatabaseSeeder` |
| Config | `src/main/java/.../config` | Web MVC config | `WebMvcConfig.java` |
| SQL scripts | `CSDL/` | Schema SQL Server (seed data đã xóa) | `FileKhoiTaoCSDL.sql` |
| Validators | `src/main/java/.../validators` | Custom Jakarta validation | `annotations/ValidUsername.java`, `ValidUsernameValidator.java` |
| Templates | `src/main/resources/templates` | Thymeleaf pages | `admin/` (dashboard, posts, events, comments, users, user-details, role-management), `auth/`, `email/`, `errors/`, `events/` (list, detail, my_registrations), `home/` (index), `partner/`, `posts/` (list, detail), layouts (`admin_layout`, `auth_layout`, `user_layout`) |
| Static assets | `src/main/resources/static` | CSS/JS/images | JS: `security-core.js`, `auth-sync.js`, `bootstrap.min.js`, `jquery-3.7.0.min.js`; CSS: `design-system.css`, `user.css`, `admin.css`, `checkout.css`, `checkout-result.css`, `order-history.css`, `bootstrap.min.css` |
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
| Test Entry | `src/test/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/AuctionSystemNhom6ApplicationTests.java` |

## Database Entities

| Entity | Table | Quan hệ chính |
|--------|-------|---------------|
| `User` | `USERS` | Implements `UserDetails`; quyền động qua `CtUserRole`, `CtRolePermission`, blacklist permission |
| `UserRole` | `USER_ROLES` | Role level dùng cho dynamic access/paywall |
| `Permission` | `PERMISSIONS` | Quyền thao tác hạt nhỏ, gắn qua `CtRolePermission` |
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
| `CtLikePost` | `CT_LIKEPOST` | Reaction/like bài viết |
| `CtPostCmt` | `CT_POST_CMT` | Join post-comment |
| `Event` | `EVENTS` | Campaign, many-to-one `EventType`, có nhiều `CtEvent` |
| `CtEvent` | `CT_EVENTS` | Session, many-to-one `Event` và `Location` |
| `EventType` | `EVENT_TYPES` | Loại sự kiện |
| `EventSpeaker` | `EVENT_SPEAKERS` | Diễn giả sự kiện |
| `EventAgenda` | `EVENT_AGENDAS` | Chương trình từng session |
| `CtAgendaSpeaker` | `CT_AGENDA_SPEAKERS` | Join agenda-speaker |
| `Location` | `LOCATIONS` | Địa điểm/online location |
| `CtEventRegistration` | `CT_EVENT_REGISTRATIONS` | Đăng ký session, nullable `User` cho guest |
| `CtEventStatusHistory` | `CT_EVENT_STATUS_HISTORY` | Current status lấy từ bản ghi mới nhất |
| `CtEventSessionRole` | `CT_EVENT_SESSION_ROLES` | Paywall role cho session |
| `CtEventTag` | `CT_EVENT_TAGS` | Join event/session-tag |
| `CtEventCmt` | `CT_EVENT_CMT` | Join event-comment |
| `CtPostEvent` | `CT_POST_EVENTS` | Liên kết bài viết liên quan tới event/session |
| `Cmt` | `CMT` | Bình luận gốc, liên kết user/target |
| `PhCmt` | `PH_CMT` | Reply/comment con, cây qua `PARENT_PH_ID` + `ROOT_CMT_ID` |
| `CtLikeCmt` | `CT_LIKECMT` | Reaction comment gốc |
| `CtLikePhCmt` | `CT_LIKEPHCMT` | Reaction reply |
| `LoaiLike` | `LOAI_LIKE` | Loại reaction (thích, yêu, haha...) |
| `CtCmtReport` | `CT_CMT_REPORTS` | Báo cáo comment gốc |
| `CtPhCmtReport` | `CT_PH_CMT_REPORTS` | Báo cáo reply |
| `CtCmtReportModLog` | `CT_CMT_REPORT_MOD_LOG` | Log xử lý report comment |
| `CtPhCmtReportModLog` | `CT_PH_CMT_REPORT_MOD_LOG` | Log xử lý report reply |
| `CtCmtActionLog` | `CT_CMT_ACTION_LOG` | Lịch sử hành động comment (sửa/xóa) |
| `CtPhCmtActionLog` | `CT_PH_CMT_ACTION_LOG` | Lịch sử hành động reply |
| `CtCmtModerationLog` | `CT_CMT_MODERATION_LOG` | Audit moderation comment |
| `CtPhCmtModerationLog` | `CT_PH_CMT_MODERATION_LOG` | Audit moderation reply |
| `ModerationAction` | `MODERATION_ACTIONS` | Danh sách hành động kiểm duyệt |
| `PublicProfile` | `PUBLIC_PROFILES` | Hồ sơ public/chuyên gia |
| `PartnerProfile` | `PARTNER_PROFILES` | Hồ sơ doanh nghiệp |
| `Address` | `ADDRESSES` | Địa chỉ người dùng |
| `Province` | `PROVINCES` | Tỉnh/thành |
| `District` | `DISTRICTS` | Quận/huyện |
| `Ward` | `WARDS` | Phường/xã |
| `OtpCode` | `OTP_CODES` | OTP register/forgot password |
| `CtUserLoginLog` | `CT_USER_LOGIN_LOG` | Nhật ký đăng nhập |
| `CtUserActionLog` | `CT_USER_ACTION_LOG` | Nhật ký hành động |
| `CtUserModerationLog` | `CT_USER_MODERATION_LOG` | Kiểm duyệt user |

## Deep Knowledge Router

Đọc `.ai-memory/03_deep_knowledge/INDEX.md` trước, rồi mở đúng file module cần sửa:

- Auth/Security: `auth_security.md`
- Bài viết/content: `posts_content.md`
- Sự kiện/đăng ký: `events_registration.md`
- Comment/moderation: `comments_moderation.md`
- Admin roles/permissions: `admin_rbac.md`
- Profile/address: `profile_address.md`
- Data model tổng quan: `data_model.md`
