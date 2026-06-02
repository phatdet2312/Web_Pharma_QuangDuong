# Active Plan - Phase 5: Hệ thống Phân quyền Động 100% Database-Driven
> Last updated: 2026-05-31
> Status: DONE (Phase A-E implemented, build+test pass)
> Planner persisted before implementation: YES
> Planner model: Opus 4.6

## Mục tiêu Phase 5

Triển khai hệ thống phân quyền động thật sự: khởi nguồn web trắng tinh chỉ có SUPERADMIN. Mọi role, permission hạt lựu đều do quản trị viên tạo và gán qua giao diện admin — KHÔNG cần can thiệp code backend. Thêm module mới trong tương lai chỉ cần admin tạo permission mới trên web.

## Cơ chế hoạt động — 4 Lớp

```
  SUPERADMIN (con người duy nhất lúc khởi tạo)
       │
       ▼
  ┌─────────────────────────────────────────────────────┐
  │  LỚP 1: QUẢN TRỊ TRÊN GIAO DIỆN WEB               │
  │  Admin vào /admin/role-management                   │
  │                                                     │
  │  ① Tạo Permission "POST_CREATE" (module "Bài viết") │
  │     → INSERT vào bảng PERMISSIONS                   │
  │  ② Tạo Role "Biên tập viên" (cấp bậc 2)            │
  │     → INSERT vào bảng USER_ROLES                    │
  │  ③ Gán POST_CREATE vào "Biên tập viên"              │
  │     → INSERT vào CT_ROLE_PERMISSIONS                │
  │  ④ Gán role cho user Nguyễn Văn A                   │
  │     → INSERT vào CT_USER_ROLES                      │
  │  ⑤ (Tuỳ chọn) Chặn riêng POST_EDIT cho user A      │
  │     → INSERT vào CT_USER_PERMISSION_BLACKLIST       │
  └─────────────────────────────────────────────────────┘
       │
       ▼
  ┌─────────────────────────────────────────────────────┐
  │  LỚP 2: NẠP QUYỀN KHI ĐĂNG NHẬP                    │
  │                                                     │
  │  Nguyễn Văn A đăng nhập                              │
  │  → napQuyenChoNguoiDung() query 4 bảng join          │
  │  → LỌC BLACKLIST (bỏ POST_EDIT đã bị chặn)          │
  │  → Kết quả: roles=["BIEN_TAP_VIEN"]                │
  │             permissions=["POST_CREATE"]              │
  │  → Nhúng vào JWT token → gửi về trình duyệt         │
  │  → DynamicRoleFilter tự cấp token mới nếu quyền     │
  │    thay đổi giữa phiên                               │
  └─────────────────────────────────────────────────────┘
       │
       ▼
  ┌─────────────────────────────────────────────────────┐
  │  LỚP 3: KIỂM TRA QUYỀN Ở MỖI ENDPOINT              │
  │                                                     │
  │  User A gọi POST /api/admin/posts                   │
  │  → PermissionInterceptor đọc @RequirePermission      │
  │  → Annotation ghi "POST_CREATE"                      │
  │  → Check: SUPERADMIN? → BYPASS, cho qua              │
  │  → Check: user có "POST_CREATE"? → CÓ → cho qua     │
  │  → KHÔNG có → trả 403 Forbidden                     │
  │  → KHÔNG có annotation → KHÔNG check (tương thích)   │
  └─────────────────────────────────────────────────────┘
       │
       ▼
  ┌─────────────────────────────────────────────────────┐
  │  LỚP 4: FRONTEND ẨN/HIỆN UI                         │
  │                                                     │
  │  Trang admin load xong                               │
  │  → JS gọi GET /api/admin/my-permissions              │
  │  → Nhận: {permissions:["POST_CREATE"],               │
  │           isSuperAdmin: false}                       │
  │  → coQuyen("POST_CREATE") → true → HIỆN nút "Tạo"   │
  │  → coQuyen("POST_DELETE") → false → ẨN nút "Xóa"    │
  │  → SUPERADMIN → LUÔN HIỆN TẤT CẢ                    │
  │                                                     │
  │  ⚠ Đây CHỈ là UX — backend vẫn chặn ở Lớp 3        │
  └─────────────────────────────────────────────────────┘
```

### Ví dụ thêm module mới trong tương lai

SUPERADMIN vào role-management → tạo quyền ORDER_CREATE, ORDER_VIEW, ORDER_APPROVE (module "Đơn hàng") → tạo role "Quản lý đơn hàng" → gán 3 quyền → gán role cho user. **KHÔNG CẦN SỬA CODE.**

## Gap Analysis

| # | Hiện trạng (đã verify code) | Mục tiêu | Severity |
|---|---------------------------|----------|----------|
| G1 | SecurityConfig hardcode 4 role KHÔNG tồn tại trong DB (USER, EMPLOYEE, ADMIN) — code cũ copy sót | Chỉ phân biệt: permitAll vs authenticated. Để PermissionInterceptor check quyền cụ thể | **CRITICAL** |
| G2 | napQuyenChoNguoiDung() KHÔNG lọc blacklist — quyền bị cấm vẫn có trong authorities | Lọc CT_USER_PERMISSION_BLACKLIST trước khi bơm vào danhSachTenPermission | **CRITICAL** |
| G3 | KHÔNG có annotation @RequirePermission — endpoint chỉ check role | Custom annotation + HandlerInterceptor kiểm tra permission code từ DB | **HIGH** |
| G4 | KHÔNG có API /api/admin/my-permissions — frontend mù | Endpoint trả về roles + permissions + isSuperAdmin | **HIGH** |
| G5 | Permission entity THIẾU cột MODULE — không nhóm quyền theo domain | Thêm cột module VARCHAR(50) | **HIGH** |
| G6 | Frontend admin KHÔNG ẩn/hiện UI theo quyền — mọi admin thấy hết | JS PermissionManager + data-permission attribute | **HIGH** |
| G7 | registerLocalUser tìm role "USER" nhưng nó KHÔNG tồn tại → crash | Tìm role có roleLevel cao nhất (yếu nhất), hoặc bỏ qua nếu không có | **MEDIUM** |
| G8 | xoaChucVu bảo vệ hardcode "ADMIN","USER" — chúng không tồn tại | Chỉ bảo vệ roleLevel=0 (SUPERADMIN) | **MEDIUM** |
| G9 | PermissionRequest thiếu trường module | Thêm module vào DTO request + response | **MEDIUM** |
| G10 | role-management.html chưa hiện cột module, chưa filter | Thêm cột, dropdown filter, search | **MEDIUM** |
| G11 | ApiRoleManagementController thiếu @Valid | Thêm @Valid cho RoleRequest, PermissionRequest | **LOW** |
| G12 | Sidebar admin menu không ẩn/hiện theo quyền | Sidebar item ẩn/hiện theo PermissionManager | **MEDIUM** |

## Phase 5A — Database + Entity (nền tảng)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| A1 | Thêm cột MODULE vào Permission entity | Thêm field `module` (String, length=50, nullable=true), mapping @Column. Hibernate tự update schema | Sửa Permission.java +3 dòng | db-specialist | Không | XS |
| A2 | Thêm module vào PermissionRequest | Thêm field `private String module;` + getter/setter | Sửa PermissionRequest.java | implementer | Không | XS |
| A3 | Thêm module vào PermissionResponse | Thêm field module + cập nhật fromEntity() | Sửa PermissionResponse.java | implementer | A1 | XS |

**A1 trước → A2, A3 song song**

## Phase 5B — Backend Core Fix (6 task, song song)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| B1 | Fix blacklist bug trong napQuyenChoNguoiDung() | Sau khi gom permission, query CT_USER_PERMISSION_BLACKLIST theo userId. Vòng lặp for lọc bỏ permission bị blacklist khỏi danhSachTenPermission. Tiêm thêm ICtUserPermissionBlacklistRepository | Sửa UserServiceImpl.java +15-20 dòng | implementer | Không | M |
| B2 | Viết lại SecurityConfig bỏ hết hardcode role | Thay hasAnyRole() bằng: (1) Public routes: permitAll(). (2) /admin/** và /api/admin/**: authenticated(). (3) /api/** còn lại: authenticated(). Bỏ warforge | Sửa SecurityConfig.java filterChain() | implementer | Không | S |
| B3 | Fix default role khi đăng ký user | Thay findByRoleName("USER") bằng findTopByOrderByRoleLevelDesc(). Nếu không có role → bỏ qua, user thường không cần role | Sửa UserServiceImpl.java 2 method + IUserRoleRepository +1 method | implementer | Không | S |
| B4 | Fix xoaChucVu bỏ hardcode tên role | Thay điều kiện "SUPERADMIN/ADMIN/USER".equals() bằng: chỉ bảo vệ role có roleLevel == 0 | Sửa RoleManagementServiceImpl.java 1 điều kiện | implementer | Không | XS |
| B5 | Thêm @Valid vào ApiRoleManagementController | Thêm @Valid trước @RequestBody tại 4 endpoint POST/PUT. Thêm @NotBlank/@Size cho DTO | Sửa 1 controller + 2 DTO | implementer | Không | XS |
| B6 | Service xử lý module cho Permission | Cập nhật taoQuyenMoi(), capNhatQuyen(), layTatCaQuyenHatLuu() để đọc/ghi/trả field module | Sửa RoleManagementServiceImpl.java 3 method | implementer | A1-A3 | S |

**B1, B2, B3, B4, B5 song song (độc lập). B6 chờ A1-A3.**

## Phase 5C — Custom Annotation + PermissionInterceptor (LÕI CỦA HỆ THỐNG)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| C1 | Tạo annotation @RequirePermission | Tạo annotation dùng trên method controller. Attribute value() kiểu String (mã quyền). Retention RUNTIME, Target METHOD | File mới: annotations/RequirePermission.java ~15 dòng | implementer | Không | XS |
| C2 | Tạo PermissionInterceptor | HandlerInterceptor.preHandle(): (1) Đọc annotation. (2) Không có → cho qua. (3) Có → lấy user. (4) SUPERADMIN (roleLevel=0) → BYPASS. (5) User có permission → cho qua. (6) Không → throw AppException(403) | File mới: config/interceptor/PermissionInterceptor.java ~60 dòng | implementer | B1, B2 | M |
| C3 | Đăng ký Interceptor vào WebMvcConfigurer | Sửa WebMvcConfig: addInterceptors() đăng ký PermissionInterceptor cho /api/admin/** | Sửa config/WebMvcConfig.java | implementer | C2 | XS |
| C4 | Tạo endpoint GET /api/admin/my-permissions | Trả về: {roles: [...], permissions: [...], isSuperAdmin: boolean}. Tạo MyPermissionResponse DTO | 1 DTO mới + 1 endpoint ~25 dòng | implementer | B1 | S |

**C1 → C2 → C3 (tuần tự). C4 độc lập với C1-C3.**

## Phase 5D — Frontend Permission-Aware (4 task)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| D1 | Tạo JS utility PermissionManager | File permission-manager.js: napQuyenTuServer(), coQuyen(code), coRole(name), anHienTheoQuyen(selector, code), renderMenuTheoQuyen(). KHÔNG arrow function, KHÔNG Stream, vòng lặp for, comment trước mỗi hàm | File mới: static/js/permission-manager.js ~100-120 dòng | implementer | C4 | M |
| D2 | Tích hợp vào admin_layout.html | Thêm script src, gọi napQuyenTuServer() khi load. Sidebar menu item thêm data-permission attribute. Gọi renderMenuTheoQuyen() sau khi nạp xong. SUPERADMIN thấy tất cả | Sửa admin_layout.html | implementer | D1 | M |
| D3 | Cập nhật role-management.html — module UI | Tab Permissions: thêm cột Module, dropdown filter theo module, modal tạo/sửa có input Module, badge module màu sắc | Sửa role-management.html ~50-80 dòng | implementer | A3, B6 | M |
| D4 | Search permission live trong role-management | Ô tìm kiếm filter client-side cho tab Permissions | Sửa role-management.html ~20 dòng | implementer | D3 | S |

**D1 → D2 (tuần tự). D3 → D4 (tuần tự). D1-D2 và D3-D4 song song.**

## Phase 5E — Gán @RequirePermission cho Controller hiện tại (5 task, song song)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| E1 | Gợi ý permission codes trong role-management UI | Section hướng dẫn gợi ý mã quyền nên tạo cho từng module: POST_CREATE, EVENT_EDIT, COMMENT_MODERATE... Chỉ là gợi ý — SUPERADMIN tự quyết | Sửa role-management.html thêm section | implementer | D3 | S |
| E2 | Gán annotation cho ApiAdminPostController | @RequirePermission("POST_CREATE") cho tạo, "POST_EDIT" cho sửa, "POST_DELETE" cho xóa, "POST_VIEW" cho xem | Sửa controller +5 annotation | implementer | C3 | S |
| E3 | Gán annotation cho ApiAdminEventController | EVENT_CREATE, EVENT_EDIT, EVENT_DELETE, EVENT_VIEW | Sửa controller +5 annotation | implementer | C3 | S |
| E4 | Gán annotation cho ApiAdminCommentController | COMMENT_VIEW, COMMENT_MODERATE, COMMENT_DELETE | Sửa controller +5 annotation | implementer | C3 | S |
| E5 | Gán annotation cho ApiRoleManagementController | ROLE_MANAGE cho tất cả CRUD role/permission. Chỉ SUPERADMIN mới có quyền này | Sửa controller +6 annotation | implementer | C3 | S |

**E1 độc lập. E2-E5 song song (chờ C3).**

## Phase 5F — Review + Test + Memory (4 task, tuần tự)

| # | Task | Mô tả | Output | Agent | Dependency | Effort |
|---|------|-------|--------|-------|------------|--------|
| F1 | Review DieuKienCode | Comment trước hàm, không ternary/lambda phức tạp, naming tường minh, không lộ DB, full code | Báo cáo findings | reviewer | E1-E5 | S |
| F2 | Build + Test | mvnw compile, mvnw test, JS syntax check | Build log | tester | F1 fix | S |
| F3 | Smoke test phân quyền động | Checklist: SUPERADMIN bypass mọi check, user có quyền → 200, user thiếu quyền → 403, blacklist → bị chặn, UI ẩn/hiện đúng | Báo cáo test | tester | F2 | M |
| F4 | Memory sync | Cập nhật 04_active_plan DONE, 03_deep_knowledge, 06_evolution_log | Memory files | memory-keeper | F3 | XS |

## Dependency tổng thể

```
Phase A (XS)              Phase B (song song)
 A1→A2,A3                  B1,B2,B3,B4,B5 song song
                            B6 chờ A1-A3
        │                        │
        └────────┬───────────────┘
                 ▼
          Phase C (tuần tự)
           C1→C2→C3    C4 (độc lập)
                 │
       ┌─────────┴──────────┐
       ▼                    ▼
  Phase D               Phase E (song song)
   D1→D2                 E2,E3,E4,E5
   D3→D4                 E1
       │                    │
       └─────────┬──────────┘
                 ▼
          Phase F (tuần tự)
           F1→F2→F3→F4
```

## Tiêu chí nghiệm thu (12 items)

1. SecurityConfig KHÔNG còn hardcode tên role — chỉ có permitAll và authenticated
2. napQuyenChoNguoiDung() lọc blacklist đúng — permission bị cấm KHÔNG xuất hiện trong authorities
3. Permission entity có cột MODULE — admin tạo quyền mới có thể chọn module
4. @RequirePermission annotation hoạt động — endpoint có annotation trả 403 nếu user thiếu quyền
5. SUPERADMIN bypass mọi @RequirePermission check — luôn trả 200
6. Endpoint không có @RequirePermission vẫn hoạt động bình thường (backward compatible)
7. GET /api/admin/my-permissions trả đúng danh sách roles + permissions + isSuperAdmin
8. PermissionManager.coQuyen() trả đúng true/false
9. Sidebar admin ẩn/hiện menu theo quyền — SUPERADMIN thấy hết
10. role-management.html: tạo/sửa quyền có trường module, bảng hiện cột module, filter theo module
11. registerLocalUser/saveGoogleUser không crash khi không có role "USER" trong DB
12. Build pass (mvnw compile + test), JS không lỗi syntax

## Decisions Phase 5

| Quyết định | Phương án chọn | Phương án bỏ | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------------|-------|------|---------|
| Cơ chế check quyền | HandlerInterceptor + @RequirePermission annotation | Spring Security @PreAuthorize (SpEL bị cấm) | Tường minh, backward compatible, không dùng Lambda/SpEL | 2026-05-31 | 2026-08-31 |
| SecurityConfig | Chỉ permitAll vs authenticated | Giữ hasAnyRole hardcode | Mọi role là động, không tồn tại role cố định ngoài SUPERADMIN | 2026-05-31 | 2026-08-31 |
| SUPERADMIN | GOD MODE — bypass mọi permission check | Check từng permission riêng | User yêu cầu, giảm phức tạp, tránh admin tự khóa mình | 2026-05-31 | 2026-08-31 |
| Default role đăng ký | Tìm roleLevel cao nhất (yếu nhất) | Hardcode "USER" | Role "USER" không tồn tại trong DB, cần linh hoạt | 2026-05-31 | 2026-08-31 |
| MODULE trong Permission | **Thực tế: tạo bảng `PERMISSION_MODULES` riêng + FK `moduleId`** | Cột VARCHAR(50) đơn giản (plan gốc) | Implementation chọn bảng riêng: entity `PermissionModule` (id, moduleCode, moduleName, description, displayOrder), Permission.moduleId FK. Khác plan gốc nhưng linh hoạt hơn | 2026-05-31 | 2026-08-31 |
| Frontend ẩn/hiện UI | JS PermissionManager + API my-permissions | Server-side Thymeleaf sec:authorize | Nhất quán kiến trúc SPA-like hiện tại, backend vẫn enforce | 2026-05-31 | 2026-08-31 |

## Rủi ro

| # | Rủi ro | Mức độ | Giải pháp |
|---|--------|--------|-----------|
| R1 | DynamicRoleFilter conflict với SecurityConfig mới | HIGH | KHÔNG SỬA DynamicRoleFilter. Test kỹ sau khi đổi SecurityConfig. Filter chỉ compare JWT vs DB — vẫn tương thích |
| R2 | SUPERADMIN quên tạo permission trước khi gán annotation vào code | MEDIUM | E1 cung cấp gợi ý permission codes. Annotation trên endpoint chỉ có hiệu lực nếu permission code tồn tại trong DB |
| R3 | User đăng nhập không có role nào — bị chặn mọi admin page | MEDIUM | SecurityConfig chỉ check authenticated. User vào được trang nhưng PermissionInterceptor chặn thao tác cần quyền |
| R4 | Frontend ẩn UI nhưng user bypass qua DevTools/API | EXPECTED | Frontend chỉ là UX. Backend PermissionInterceptor là tường chắn chính — user bypass UI vẫn bị 403 |
| R5 | role-management.html đã 1080 dòng — thêm tính năng sẽ phình | MEDIUM | Chấp nhận — events.html 3300+ dòng là tiền lệ |

## Lưu ý quan trọng cho implementer

1. **KHÔNG SỬA ultraSecureLibrary** — DynamicRoleFilter, JwtAuthenticationFilter, JwtService trong thư mục cấm
2. **KHÔNG seed permission mẫu** — DatabaseSeeder CHỈ giữ SUPERADMIN + VIEW. Mọi permission khác do SUPERADMIN tự tạo
3. **KHÔNG seed gán quyền cho role** — SUPERADMIN tự quyết định role nào có quyền gì
4. **Backward compatible** — Method controller không có @RequirePermission vẫn hoạt động bình thường
5. **Convention**: Không Stream/Lambda phức tạp, vòng lặp for, comment tiếng Việt trước mỗi hàm, naming tường minh
6. **Test với SUPERADMIN trước** — đảm bảo bypass mọi check, rồi test user thường

---

# Historical Plan - Phase 4: Rich Content Editor + Live Preview
> Last updated: 2026-05-31
> Status: DONE (tach file dung chung, tich hop admin posts + events)
> Planner persisted before implementation: YES
> Planner model: Opus 4.6

## Mục tiêu Phase 4

Đập giao diện modal tạo/sửa bài viết hiện tại (chỉ có textarea nhập raw text), xây lại thành Rich Content Editor chuyên nghiệp với Live Preview — hoàn toàn từ đầu, KHÔNG dùng thư viện ngoài nào. Admin có thể thiết kế nội dung bài viết (heading, list, bảng, hình ảnh, link, blockquote...) và xem trước demo thực tế trước khi lưu.

## Gap Analysis

| # | Hiện trạng | Mục tiêu | Severity |
|---|-----------|----------|----------|
| G1 | `<textarea>` nhập raw text, KHÔNG có formatting | contenteditable div + toolbar 16 công cụ | CRITICAL |
| G2 | KHÔNG có preview | Live Preview split-view, CSS `.prose` giống trang user | CRITICAL |
| G3 | Modal 700px quá nhỏ | Modal near-fullscreen cho đủ không gian | HIGH |
| G4 | KHÔNG có undo/redo | Undo/Redo stack thủ công (50 entries) | HIGH |
| G5 | KHÔNG có chèn ảnh inline vào nội dung | Upload ảnh qua API có sẵn + insert `<img>` vào editor | HIGH |
| G6 | KHÔNG có chèn bảng | Grid picker 6x6 → generate `<table>` HTML | MEDIUM |
| G7 | KHÔNG có chèn link | Dialog nhập URL + text → insert `<a>` | MEDIUM |
| G8 | CSS `.prose` chưa có trong admin | Copy từ `posts/detail.html` vào preview panel | MEDIUM |
| G9 | KHÔNG responsive split-view | Tablet: stacked view (editor trên, preview dưới) | MEDIUM |
| G10 | `savePost()` đọc textarea `.value` | Đổi sang đọc contenteditable `.innerHTML` | LOW |

## Sprint A — CSS Foundation (4 task SONG SONG)

| # | Task | Mục tiêu | Output | Agent | Effort |
|---|------|---------|--------|-------|--------|
| A1 | CSS modal fullscreen | Class `.modal-box-editor` fullscreen (95-100vw, 95-100vh), responsive 768px/576px | ~30 dòng CSS | implementer | S |
| A2 | CSS split-view layout | `.editor-split-container` CSS Grid 2 cột 1fr 1fr, responsive stacked 768px | ~25 dòng CSS | implementer | S |
| A3 | CSS toolbar editor | `.rce-toolbar`, `.rce-toolbar-btn` 32x32px hover/active, `.rce-toolbar-separator` | ~30 dòng CSS | implementer | S |
| A4 | CSS preview panel (copy .prose) | Copy CSS `.prose` từ `posts/detail.html` dòng 298-372, namespace `.rce-preview .prose` | ~45 dòng CSS | implementer | XS |

## Sprint B — HTML Restructure Modal (tuần tự)

| # | Task | Mục tiêu | Output | Agent | Effort |
|---|------|---------|--------|-------|--------|
| B1 | Restructure modal HTML | Thay `<div class="modal-box">` → `modal-box modal-box-editor`. Trong modal-body: panel trái (toolbar + contenteditable + metadata collapse), panel phải (preview `.prose`) | Sửa HTML modal #modalPost | implementer | M |
| B2 | Nút toggle view | 3 nút: Chia đôi (split), Chỉ editor, Chỉ preview. Tablet mặc định stacked | 3 button `.rce-view-toggle` | implementer | XS |

**B1 → B2**

## Sprint C — Rich Content Editor Core JS (C1 trước → C2-C6 song song → C7)

| # | Task | Mục tiêu | Output | Agent | Effort |
|---|------|---------|--------|-------|--------|
| C1 | Toolbar + basic formatting | `khoiTaoEditorToolbar()`: Bold, Italic, Underline, Strikethrough, H2, H3, Clear Format, HR. Dùng `document.execCommand()` | Hàm `khoiTaoEditorToolbar()`, `thucThiLenhEditor()`, `capNhatTrangThaiToolbar()` | implementer | M |
| C2 | List + Blockquote | 3 nút: UL, OL, Blockquote (toggle) | Bổ sung vào toolbar | implementer | S |
| C3 | Insert Link dialog | Mini-dialog: URL + text → `createLink` | Hàm `moDialogChenLink()`, `chenLienKet()` | implementer | S |
| C4 | Insert Image (upload/URL) | Mini-dialog 2 tab: URL hoặc upload → insert `<img>`. Reuse `apiUploadPost()` | Hàm `moDialogChenAnh()`, `chenAnhTuUpload()` | implementer | M |
| C5 | Insert Table picker | Grid picker 6x6 → generate `<table>` HTML tương thích `.prose` | Hàm `moDialogChenBang()`, `taoHtmlBang()` | implementer | M |
| C6 | Code block | Wrap selection trong `<pre><code>` (toggle) | Bổ sung vào `thucThiLenhEditor()` | implementer | S |
| C7 | Undo/Redo stack | Array HTML snapshot, debounce 500ms, max 50 entries. Ctrl+Z/Y override | Hàm `luuTrangThaiEditor()`, `hoanTacEditor()`, `lamLaiEditor()` | implementer | M |

## Sprint D — Live Preview Engine (D1 trước → D2, D3 song song)

| # | Task | Mục tiêu | Output | Agent | Effort |
|---|------|---------|--------|-------|--------|
| D1 | Live Preview sync | `capNhatPreview()`: đọc innerHTML editor → set vào preview `.prose`. Debounce 200ms | Hàm `capNhatPreview()` + input listener | implementer | S |
| D2 | Toggle view modes | JS cho 3 nút: split / editor-only / preview-only. CSS class `.editor-only`, `.preview-only` | Hàm `chuyenCheDo(mode)` | implementer | XS |
| D3 | Preview full-screen overlay | Nút "Xem trước demo" → overlay 100vw x 100vh, CSS `.prose` đầy đủ | Hàm `moPreviewFullscreen()` | implementer | S |

## Sprint E — Integration (E1→E2→E3, E4 song song)

| # | Task | Mục tiêu | Output | Agent | Effort |
|---|------|---------|--------|-------|--------|
| E1 | Sửa openModalCreate | Clear editor innerHTML, clear preview, reset undo/redo, set view = split | Sửa function `openModalCreate()` | implementer | S |
| E2 | Sửa openModalEditById | Load content HTML vào contenteditable, trigger preview, reset undo | Sửa function `openModalEditById()` | implementer | S |
| E3 | Sửa savePost | Đọc innerHTML từ contenteditable (thay textarea.value), strip `<script>` tags | Sửa function `savePost()` | implementer | S |
| E4 | Paste handler | Intercept paste: strip style/class, giữ basic semantic tags (b, i, u, a, p, h2, h3, ul, ol, table, img, blockquote, pre, code, strong, em) | Hàm `xuLyPasteEditor()` | implementer | S |

## Sprint F — Review + Test + Memory (tuần tự)

| # | Task | Mục tiêu | Agent | Effort |
|---|------|---------|-------|--------|
| F1 | Review DieuKienCode | Comment trước mỗi hàm, không ternary/lambda/arrow, naming tường minh | reviewer | S |
| F2 | Build + Test | `mvnw compile`, `mvnw test`, JS syntax check (không ES6+) | tester | S |
| F3 | UX Smoke Test | Checklist 15 tiêu chí nghiệm thu | tester | S |
| F4 | Memory sync | Cập nhật memory | memory-keeper | XS |

## Tiêu chí nghiệm thu (15 items)

1. Modal Create/Edit mở near-fullscreen (width >= 95vw)
2. Split-view: editor bên trái, preview bên phải, gap 16px
3. Toolbar 16 nút hoạt động: Bold, Italic, Underline, Strikethrough, H2, H3, UL, OL, Blockquote, Link, Image, Table, HR, Code Block, Clear Format, Undo, Redo
4. contenteditable div thay thế textarea cho trường Content
5. Live Preview cập nhật realtime khi gõ (debounce 200ms)
6. Preview render đúng CSS `.prose` giống trang user detail.html
7. Insert Image: upload qua API + insert `<img>` vào editor
8. Insert Table: grid picker 6x6, generate table HTML
9. Insert Link: dialog URL + text, insert `<a>` tag
10. Undo/Redo hoạt động đúng (stack 50 entries)
11. Paste strip formatting: paste từ Word/web chỉ giữ basic tags
12. Toggle 3 chế độ: split / editor-only / preview-only
13. Responsive 768px: chuyển sang stacked view
14. savePost() đọc innerHTML từ contenteditable, gửi API thành công
15. Không ảnh hưởng modal khác (Detail, Category, Tag)

## Decisions Phase 4

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| Editor dùng contenteditable div | contenteditable + execCommand | User yêu cầu không dùng thư viện, không iframe. contenteditable là cách duy nhất WYSIWYG vanilla JS | 2026-05-30 | 2026-08-30 |
| Layout split-view bằng CSS Grid | Grid 2 cột 1fr 1fr | Đơn giản, responsive dễ, không cần JS resize | 2026-05-30 | 2026-08-30 |
| Modal near-fullscreen (không trang riêng) | Fullscreen modal overlay | Nhất quán pattern modal admin (events, comments đều modal) | 2026-05-30 | 2026-08-30 |
| Undo/Redo bằng HTML snapshot | Array snapshot + debounce 500ms | Đơn giản nhất cho vanilla JS, đủ tốt cho editor vừa-nhỏ | 2026-05-30 | 2026-08-30 |
| Upload ảnh inline reuse endpoint có sẵn | `/api/admin/posts/upload-thumbnail` | Endpoint đã có, trả URL. Không cần tạo mới | 2026-05-30 | 2026-08-30 |
| Paste handler giữ basic semantic tags | Strip style/class, giữ b/i/u/a/p/h2/h3/ul/ol/table/img/blockquote/pre/code | Cân bằng: giữ cấu trúc nội dung, loại style inline từ Word/web | 2026-05-30 | 2026-08-30 |

## Rủi ro

| # | Rủi ro | Mức độ | Giải pháp |
|---|--------|--------|-----------|
| R1 | `execCommand` đã deprecated | MEDIUM | Vẫn hoạt động trên mọi browser. Nếu bị loại bỏ → chuyển Selection + Range API |
| R2 | HTML output từ contenteditable không sạch | HIGH | Paste handler strip formatting + server-side sanitize |
| R3 | posts.html quá lớn (dự kiến 2800-3000+ dòng) | MEDIUM | Chấp nhận — events.html 3300+ dòng là tiền lệ |
| R4 | XSS qua preview panel | LOW | Preview trong admin area (authenticated). Vẫn strip `<script>` tags |
| R5 | Undo stack tốn bộ nhớ | LOW | Giới hạn 50 entries, ~2.5MB max — chấp nhận được |

---

## Phase 3 (DONE — Lịch sử)

## Mục tiêu Phase 3

Nâng cấp admin/posts lên chuẩn vàng admin/events: khai thác 12/12 bảng DB (từ 42% lên 85%+), Detail Modal Hub 5 tab, Category/Tag CRUD modal, Images gallery, Files management, Comments inline, Reactions detail, Post-Event linking.

## Gap Analysis (Evidence-based)

- DB exploitation: 5/12 bảng có UI (42%) — thiếu 7 bảng
- Frontend: 1137 dòng vs events 3321 — thiếu ~2000 dòng
- Backend: 20 endpoint hiện có, cần thêm 13 endpoint mới
- Modals: 2 (CRUD + detail đơn giản) — cần thêm 2 CRUD modal + nâng cấp detail thành 5-tab hub
- 5 CRITICAL gaps: Comments inline, POST_FILES, POST_IMAGES, Category CRUD modal, Tag CRUD modal
- 5 HIGH gaps: Stats 4/11 fields, CT_POST_EVENTS zero UI, Reactions chi đếm số, Detail modal đơn giản, View analytics thiếu

## Sprint A — Backend DTOs + Service (Images, Files, Comments/Reactions/Events)

| # | Task | Mục tiêu | Output | Pattern tham chiếu | Agent | Effort | Trạng thái |
|---|------|---------|--------|-------------------|-------|--------|-----------|
| A1 | DTO cho Images & Files | Tạo PostImageResponse (id, postId, imageUrl, displayOrder), PostFileResponse (id, postId, fileName, fileUrl, fileType, fileSize, downloadCount), PostImageRequest, PostReactionSummary, PostLinkedEventResponse | 5 DTO files mới | Copy pattern CategoryResponse — field, getter/setter, builder | implementer | XS | DONE |
| A2 | Service methods Images CRUD | 4 method: layAnhCuaBaiViet, uploadAnhBaiViet (gallery), xoaAnhBaiViet, doiThuTuAnh. Upload ghi /uploads/posts/images/ | AdminPostServiceImpl +4 methods | Copy uploadAnhBaiViet (thumbnail) dòng 370-412 — validate file + ghi + trả URL | implementer | M | DONE |
| A3 | Service methods Files CRUD | 3 method: layFileCuaBaiViet, uploadFileBaiViet, xoaFileBaiViet. Accept pdf/doc/docx/pptx/xlsx, max 10MB, ghi /uploads/posts/files/ | AdminPostServiceImpl +3 methods | Copy pattern thumbnail upload, thay validate thành file types | implementer | M | DONE |
| A4 | Service methods Comments/Reactions/Events/Analytics | 5 method read-only: layCmtCuaBaiVietAdmin (phân trang), layReactionsCuaBaiViet (grouped), laySuKienLienKet, lienKetSuKien, xoaLienKetSuKien | AdminPostServiceImpl +5 methods + 2 DTO mới | Copy CommentServiceImpl cho comment lazy-load. Copy ICtLikePostRepository.demReactionTheLoai | implementer | M | DONE |

**Dependency:** A1 xong trước → A2, A3, A4 song song

## Sprint B — Backend Controller (13 endpoint mới)

| # | Task | Mục tiêu | Output | Agent | Effort | Trạng thái |
|---|------|---------|--------|-------|--------|-----------|
| B1 | Controller Images (4 endpoint) | GET/POST/DELETE/PATCH cho /api/admin/posts/{id}/images | ApiAdminPostController +4 methods | implementer | S | DONE |
| B2 | Controller Files (3 endpoint) | GET/POST/DELETE cho /api/admin/posts/{id}/files | ApiAdminPostController +3 methods | implementer | S | DONE |
| B3 | Controller Comments/Reactions/Events (5 endpoint) | GET comments (phân trang), GET reactions, GET/POST/DELETE events liên kết | ApiAdminPostController +5 methods | implementer | S | DONE |

**Dependency:** B1 cần A2, B2 cần A3, B3 cần A4 — song song nội bộ

## Sprint C — Frontend Detail Modal Hub (5 Tabs)

| # | Task | Mục tiêu | Output | Pattern tham chiếu | Agent | Effort | Trạng thái |
|---|------|---------|--------|-------------------|-------|--------|-----------|
| C1 | CSS tab system + detail modal nâng cấp | CSS .pd-tabs, .pd-tab, .pd-tab-badge, .pd-tab-content, .pd-gallery-grid, modal-wide 1060px, responsive 1200/768/576px | posts.html +~80 dòng CSS | events.html dòng 520-640 modal styles | implementer | S | DONE |
| C2 | JS Tab 1: Tổng quan (refactor openDetailModal) | Refactor detail modal thành tab system. Tab 1 = nội dung hiện tại + stats row 4 card (views/downloads/comments/reactions) + badge count trên tab header | Refactor openDetailModal function | events.html detail modal render dòng 2100-2200 | implementer | M | DONE |
| C3 | JS Tab 2: Hình ảnh Gallery | renderImageTab(postId): grid 3 cột, upload flow, delete + confirmAction, reorder display_order, empty state, loading spinner | functions renderImageTab, uploadPostImage, deletePostImage, reorderImages | doUploadThumbnail dòng 978 + renderPostsTable grid pattern | implementer | M | DONE |
| C4 | JS Tab 3: File đính kèm | renderFileTab(postId): table 5 cột (icon theo type, tên, loại, dung lượng, thao tác), upload validate 10MB, delete + FK warning, downloadCount | functions renderFileTab, uploadPostFile, deletePostFile | doUploadThumbnail + renderPostsTable table pattern | implementer | M | DONE |
| C5 | JS Tab 4: Bình luận preview | renderCommentTab(postId): load 5 comments mới nhất, avatar+tên+nội dung truncate+time, nút "Xem tất cả" redirect admin/comments, tab badge count, empty state | function renderCommentTab | events.html openCommentModal dòng 2971-3000 | implementer | S | DONE |
| C6 | JS Tab 5: Liên kết & Tương tác | renderLinkTab(postId): Section 1 = Events liên kết (title+date+unlink) + dropdown link event. Section 2 = Reactions breakdown (icon+name+count). Empty state mỗi section | functions renderLinkTab, linkEvent, unlinkEvent | events.html event linking pattern | implementer | M | DONE |

**Dependency:** C1 trước → C2 trước → (C3, C4, C5, C6 song song). C3 cần B1, C4 cần B2, C5+C6 cần B3.

## Sprint D — Frontend Category & Tag CRUD Modals (song song với Sprint C)

| # | Task | Mục tiêu | Output | Pattern tham chiếu | Agent | Effort | Trạng thái |
|---|------|---------|--------|-------------------|-------|--------|-----------|
| D1 | Category CRUD Modal | Modal 700px: table (Tên, Slug, Mô tả, Số bài viết, isActive toggle, Thao tác edit/delete) + form thêm/sửa (Tên*, Slug, Mô tả, isActive checkbox) + FK check delete | HTML modal + JS: openCategoryModal, loadCategories, saveCategory, deleteCategory | events.html Type CRUD modal dòng 850-950 | implementer | M | DONE |
| D2 | Tag CRUD Modal | Modal: grid tag chips (tên + usageCount + nút X + edit) + form thêm/sửa (Tên*, Slug) + FK check delete | HTML modal + JS: openTagModal, loadTagsAdmin, saveTag, deleteTag | Copy pattern từ D1 đơn giản hơn | implementer | S | DONE |
| D3 | Toolbar integration | 2 nút gear (fa-cog) cạnh dropdown Category và Tag trong toolbar. Responsive: ẩn trên mobile | Thêm 2 button vào toolbar | Inline toolbar hiện tại | implementer | XS | DONE |

**Dependency:** D1 trước → D2 → D3

## Sprint E — Stats mở rộng + UX + Review + Test + Memory

| # | Task | Mục tiêu | Output | Agent | Effort | Trạng thái |
|---|------|---------|--------|-------|--------|-----------|
| E1 | Stats row mở rộng | Hiện thêm: totalViews, totalComments, totalReactions, totalFeatured từ response đã có. Từ 4 card lên 8 card (2 dòng responsive) | Sửa renderStats function | implementer | S | DONE |
| E2 | Escape key đóng modal | Thêm keydown listener đóng tất cả modal-overlay khi nhấn Escape | Thêm event listener | implementer | XS | DONE |
| E3 | Table column clickable | commentCount → link mở tab Comments detail. downloadCount → link mở tab Files detail | Sửa renderPostsTable | implementer | XS | DONE |
| E4 | Review DieuKienCode | Kiểm tra: comment trước hàm/action, không ternary phức tạp, không lambda, naming tường minh, không lộ DB, full code | Report findings | reviewer | S | DONE |
| E5 | Build + Test | mvnw compile + test + git diff --check + JS syntax check | Build log | tester | S | DONE |
| E6 | Memory sync | Cập nhật 04_active_plan.md DONE, 03_deep_knowledge, 06_evolution_log, DB exploitation rate | Memory files | memory-keeper | XS | DONE |

**Dependency:** E1-E3 song song (sau C+D). E4 sau E1-E3. E5 sau E4 fix. E6 sau E5.

## Tiêu chí nghiệm thu Phase 3 (20 items)

1. Detail modal có 5 tab hoạt động: Tổng quan, Hình ảnh, File, Bình luận, Liên kết
2. Tab Hình ảnh: upload/delete/reorder images, hiện DISPLAY_ORDER
3. Tab File: upload/delete files, icon theo type, hiện downloadCount
4. Tab Bình luận: load 5 comments mới nhất, nút redirect admin/comments
5. Tab Liên kết: reactions breakdown + events linked + link/unlink event
6. Category CRUD modal: table + form + toggle isActive + FK check delete
7. Tag CRUD modal: grid chips + form + FK check delete
8. Toolbar có nút gear cho Category và Tag
9. Responsive tại 1200px, 768px, 576px không vỡ layout
10. Empty state mỗi tab/modal khi không có data
11. Loading spinner/skeleton khi load data trong tab
12. escapeHtml() tất cả output innerHTML — không XSS
13. Backend validate @Valid mỗi endpoint mới
14. Upload validate file type + size cả client + server
15. Không lộ tên bảng DB trong frontend
16. Comment Javadoc trước mỗi function/method mới
17. Không stream/lambda/ternary phức tạp
18. Build pass (mvnw compile + test)
19. DB exploitation rate >= 85% (10/12 bảng có UI)
20. posts.html >= 2200 dòng (từ 1137, tăng ~1063)

## Decisions Phase 3

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| Detail modal dùng tab system 5 tab | Tab system (không accordion, không separate modals) | events.html dùng tab, consistency + dễ mở rộng | 2026-05-30 | 2026-08-30 |
| Images/Files là sub-resource REST nested | /posts/{id}/images, /posts/{id}/files | REST convention, events cũng dùng nested pattern | 2026-05-30 | 2026-08-30 |
| Category/Tag CRUD dùng modal inline | Modal table + form cùng 1 modal | events.html dùng modal cho Type/Location CRUD | 2026-05-30 | 2026-08-30 |
| Comment tab chỉ preview, không full management | Preview 5 comments + redirect admin/comments | Tránh trùng lặp, events cũng chỉ preview | 2026-05-30 | 2026-08-30 |
| Stats mở rộng 8 cards 2 dòng | 2 dòng responsive | Backend đã trả 11 fields, hiện 4 là lãng phí | 2026-05-30 | 2026-08-30 |

---

## Phase 1+2 (DONE) — Lịch sử audit + fix trước đó

## Mục tiêu

Kiểm tra chuyên sâu chức năng quản lý post admin (`admin/posts.html`) có đúng yêu cầu hình thành ra nó chưa: 100% coverage user-side, khai thác tối đa 12 bảng DB post-related, đồng bộ UX với admin/events + admin/comments, convention DieuKienCode, edge case, security. Verdict 3 cấp: PASS / SỬA TOÀN DIỆN / ĐẬP BỎ XÂY LẠI.

## So sánh quy mô sơ bộ

| Module | Frontend (dòng) | Backend Service (dòng) | Controller (dòng) |
|--------|-----------------|----------------------|-------------------|
| admin/events | 3321 | 1259 | ~350 |
| admin/comments | 2620 | 1595 | ~200 |
| **admin/posts** | **819** | **608** | **189** |

## Danh sách Task

### Đợt 1 (song song — thu thập evidence) — DONE

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 1.1 | Coverage audit: user-side 9 API vs admin-side | deep-reviewer | DONE — 55-60% coverage, 1 BLOCKER (filter access sai param), 2 HIGH, 1 MAJOR, 2 MEDIUM |
| 1.2 | DB exploitation audit: 12 bảng mapping + column gap | deep-reviewer | DONE — 7/12 bảng (58%), 2 BLOCKER (FK cascade thiếu), 1 MAJOR (accessLevel undefined) |
| 1.3 | Backend completeness audit: service/endpoint/DTO gap vs events+comments | deep-reviewer | DONE — 55-60% completeness, 2 BLOCKER (FK crash + no upload), 5 MAJOR, N+1 query |

### Đợt 2 (song song, sau Đợt 1) — DONE

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 2.1 | UX consistency audit: posts vs events vs comments (12 checklist items) | deep-reviewer | DONE — 30-35% đồng bộ, 3 CRITICAL (PTM+upload+loading), 3 HIGH, 3 MEDIUM |
| 2.2 | DieuKienCode convention audit: JS comment + OOP + naming + pattern cấm | deep-reviewer | DONE — 66.7% compliance, 2 BLOCKER (lộ DB+cascade), 4 MAJOR (ClassCastException) |
| 2.3 | XSS + security audit: innerHTML/onclick/src injection + CSRF + backend @Valid | security-auditor | DONE — 15 XSS (3 CRITICAL), 15 backend issues (3 HIGH). Cùng pattern comments đã fix |

### Đợt 3 (song song, sau Đợt 2) — DONE

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 3.1 | Edge case audit: pagination + bulk + nội dung dài + FK dependency | deep-reviewer | DONE — 2 BLOCKER (xóa Category/Tag FK), 3 MAJOR, 4 MINOR |
| 3.2 | Missing features gap analysis: tổng hợp chức năng thiếu từ Đợt 1+2 | deep-reviewer | DONE — 20 findings, DB ~37%, verdict SỬA TOÀN DIỆN |

### Đợt 4 (tuần tự, sau Đợt 3) — DONE

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 4.1 | Tổng hợp verdict report: CRITICAL/HIGH/MEDIUM/LOW + PASS/SỬA/ĐẬP BỎ | orchestrator | DONE — SỬA TOÀN DIỆN (34 findings) |
| 4.2 | Implement fix toàn bộ Sprint 1+2+3 | implementer x3 | DONE — Backend 22 fixes + Frontend 15 fixes |

## Kết quả implement

### Backend (22 fixes)
- 6 repositories: thêm cascade delete methods + FK check
- IAdminPostService: +1 method `layChiTietBaiViet`
- AdminPostServiceImpl: cascade delete 9 bảng, ClassCastException x4, FK check Category/Tag, stats +2 fields, postCount/usageCount, accessLevel computed, detail method
- ApiAdminPostController: +1 endpoint detail, page/size validate, auth throw 401
- PostRequest: @NotBlank + @Size cho content/thumbnailUrl/seoTitle/seoDescription
- PostResponse: +4 fields (accessLevel, content, seoTitle, seoDescription)
- PostStatsResponse: +2 fields (totalViews, totalComments)

### Frontend (15 fixes)
- XSS: escapeHtml() + 9 escape points
- DB names: xóa 6 vị trí lộ tên bảng
- PageTransitionManager: tích hợp fade stats + skeleton table
- Filter access: xóa param sai, TODO chờ API dictionary
- Modal: a11y (role="dialog", aria-modal)
- Pagination: giới hạn 7 nút + dấu "..."
- Bulk bar: CSS class toggle thay inline style
- openModalEdit → openModalEditById (gọi admin API)
- Breakpoint 1200px
- Tags hiển thị giới hạn 4 + "+N"
- Slug overflow ellipsis

### Phase 2 — 7 items còn lại (19 tasks, 5 đợt)

#### Đợt 1 — Database + DTO foundation

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 1.1 | Thêm field isFeatured vào Post entity + SQL | implementer | DONE |
| 1.2 | Tạo DTOs: AdminPostMediaResponse, AdminPostDictionaryResponse, RoleOptionResponse | implementer | DONE |

#### Đợt 2 — Backend Service + Repository (3 nhánh song song)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 2.1 | Upload thumbnail service method | implementer | DONE |
| 2.2 | Dictionary service method | implementer | DONE |
| 2.3 | Date range JPQL query | implementer | DONE |
| 2.4 | Dynamic sort trong service | implementer | DONE |
| 2.5 | 5 batch queries cho N+1 fix | implementer | DONE |
| 2.6 | Refactor chuyenDoiPostResponse batch | implementer | DONE |

#### Đợt 3 — Controller endpoints

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 3.1 | Featured repository + stats | implementer | DONE |
| 3.2 | Featured toggle service | implementer | DONE |
| 3.3 | Controller: 4 endpoint mới + sửa layDanhSach | implementer | DONE |

#### Đợt 4 — Frontend (2 nhánh song song)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 4.1 | Load dictionary + populate dropdowns | implementer | DONE |
| 4.2 | Date range filter UI | implementer | DONE |
| 4.3 | Sort dropdown UI | implementer | DONE |
| 4.4 | Upload thumbnail UI | implementer | DONE |
| 4.5 | Modal detail read-only | implementer | DONE |
| 4.6 | Featured toggle UI | implementer | DONE |

#### Đợt 5 — Review + Test

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 5.1 | Review DieuKienCode | reviewer | DONE — NEEDS_FIX → 3 critical/warning fixed |
| 5.2 | Build + test | compile | DONE — BUILD SUCCESS |
| 5.3 | Memory sync | orchestrator | DONE |
- Reactions management
- CT_POST_EVENTS management
- N+1 query optimization (batch load)

## Phát hiện sơ bộ (trước audit chính thức)

1. Header lộ tên bảng DB: `POSTS, CATEGORIES, TAGS, POST_VIEW_LOGS, POST_FILES`
2. Thumbnail input là text (vi phạm learning #14 — admin events đã chuyển file upload)
3. Quy mô quá nhỏ: 819 dòng vs 3321/2620
4. Access filter hardcode `ROLE_DOCTOR`, `ROLE_PARTNER`, `ROLE_ADMIN` thay vì API dictionary
5. Không có modal detail (events và comments đều có)
6. Không tích hợp PageTransitionManager

## Tiêu chí verdict

| Verdict | Điều kiện |
|---------|-----------|
| PASS | <= 5 findings, tất cả LOW/MEDIUM, không CRITICAL |
| SỬA TOÀN DIỆN | 6-15 findings, có HIGH nhưng khung có thể giữ, DB exploitation >= 50% |
| ĐẬP BỎ XÂY LẠI | > 15 findings, có CRITICAL, DB exploitation < 50%, UX bất đồng bộ nghiêm trọng |

## Decisions

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| Audit trước, implement sau | Audit 7 phương diện trước khi quyết định sửa/rebuild | Tránh lặp sai lầm admin/events (implement rồi mới audit) | 2026-05-29 | 2026-08-29 |
| Verdict 3 cấp | PASS / SỬA / ĐẬP BỎ thay vì PASS/FAIL | Có bản demo rollback (posts-demo.html) nên cần verdict rõ mức độ | 2026-05-29 | 2026-08-29 |

---

# Historical Plan - Kiểm tra chuyên sâu Admin Comments + Fix
> Last updated: 2026-05-29
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu

Kiểm tra chuyên sâu chức năng quản lý comment admin (`admin/comments.html`) có đúng yêu cầu hình thành ra nó chưa: 100% coverage user-side, khai thác tối đa 16 bảng DB, đồng bộ UX, convention DieuKienCode, edge case, security.

## Danh sách Task

### Đợt 1 (song song)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 1.1-1.3 | Coverage audit: user-side vs admin-side | deep-reviewer | DONE — PASS |
| 2.1-2.2 | DB exploitation: 16 bảng mapping + column gap | deep-reviewer | DONE — PASS |
| 5.1 | XSS prevention audit | security-auditor | DONE — FAIL→FIXED |

### Đợt 2 (song song, sau Đợt 1)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 3.1-3.2 | UX consistency: admin cmt vs events + tree CSS | deep-reviewer | DONE — PASS |
| 4.1-4.3 | Backend completeness: service/endpoint/DTO gap | deep-reviewer | DONE — PASS |
| 5.2-5.4 | Edge case: pagination, bulk, nội dung dài | deep-reviewer | DONE — PASS |

### Đợt 3

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 6.1-6.3 | DieuKienCode convention JS/Backend/tái sử dụng | deep-reviewer | DONE — PASS |

### Đợt 4

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 7.1 | Tổng hợp báo cáo CRITICAL/HIGH/MEDIUM/LOW | deep-reviewer | DONE |

---

# Historical Plan - Fix LOAI_LIKE Upload + Reply Tree Branch (Đợt 2)
> Last updated: 2026-05-29
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu

Fix 2 lỗi chính còn lại sau đợt fix 1: (A) Modal LOAI_LIKE phải có upload ảnh như admin/events, (B) Reply 3 cấp phải có cây phân nhánh như posts/detail.html.

## Phạm vi

| Tầng | File |
|------|------|
| Backend | Thêm endpoint upload icon + service method |
| Frontend | Redesign modal LOAI_LIKE + Refactor reply DOM sang bubble-wrapper + Tree branch CSS |

## Danh sách Task

### Phase 0: Backend Upload Icon

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R0.1 | Thêm method uploadIconReaction vào ICommentService | implementer | DONE |
| R0.2 | Implement upload trong CommentServiceImpl | implementer | DONE |
| R0.3 | Thêm endpoint POST multipart upload-icon | implementer | DONE |
| R0.4 | Verify SecurityConfig + filters | implementer | DONE |
| R0.5 | Build + test backend | tester | DONE |

### Phase 1: Frontend Modal LOAI_LIKE Redesign

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R1.1 | Thêm CSS media-upload-row + media-preview | implementer | DONE |
| R1.2 | Redesign modal HTML: toggle emoji/upload + file input + preview | implementer | DONE |
| R1.3 | Thêm JS apiUpload + setMediaPreview | implementer | DONE |
| R1.4 | JS toggle emoji/upload handler + update submitLikeType | implementer | DONE |
| R1.5 | Update hiển thị icon table (img vs emoji) | implementer | DONE |

### Phase 2: Frontend Reply Tree Branch

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R2.1 | Thêm CSS tree branch (copy/adapt posts/detail.html) | implementer | DONE |
| R2.2 | Refactor DOM renderReplyItem cấp 2 → ri-bubble-wrapper | implementer | DONE |
| R2.3 | Refactor DOM reply cấp 3+ → reply-thread lồng trong ri-bubble-wrapper | implementer | DONE |
| R2.4 | Refactor container replies cấp 2 → reply-thread + has-replies | implementer | DONE |
| R2.5 | Xóa CSS reply cũ + cập nhật CSS mới | implementer | DONE |

### Phase 3: Nghiệm Thu

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R3.1 | Review convention + toàn vẹn | reviewer | DONE |
| R3.2 | Build + test | tester | DONE |
| R3.3 | Cập nhật memory | memory-keeper | DONE |

## Decisions

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| LOAI_LIKE icon upload ảnh | File input + upload endpoint + preview (SUPERSEDES decision cũ "giữ text input") | User yêu cầu rõ ràng, không được override | 2026-05-29 | 2026-08-29 |
| Reply admin dùng tree branch CSS | Copy/adapt CSS posts/detail.html, DOM ri-bubble-wrapper | Nhất quán user/admin, user yêu cầu tham khảo posts/detail | 2026-05-29 | 2026-08-29 |
| Upload icon vào thư mục riêng | `/uploads/comments/reaction-icons/` | Tách biệt domain comment vs event | 2026-05-29 | 2026-08-29 |
| Giữ cmt-card, chỉ refactor reply bên trong | Không đổi cmt-card header/body | Admin card có checkbox + bulk khác user side | 2026-05-29 | 2026-08-29 |

## Tiêu chí nghiệm thu

1. Modal LOAI_LIKE có toggle emoji/upload, upload thành công hiện preview
2. Table LOAI_LIKE hiện img (URL) hoặc text (emoji) đúng
3. Reply cấp 2 có tree branch (nhánh L + trục dọc)
4. Reply cấp 3+ lồng trong ri-bubble-wrapper, có class reply-level-3
5. Avatar size đúng theo cấp (36px cấp 1, 28px cấp 2-3)
6. Nút mod actions đầy đủ trong ri-bubble
7. Build pass, zero JS error
8. DieuKienCode convention

---

# Historical Plan - Fix 9 Nhóm Lỗi Admin Comments
> Last updated: 2026-05-28
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu đã hoàn thành

Fix 9 nhóm lỗi nghiêm trọng trong `admin/comments.html` sau lần rewrite đầu: lộ tên bảng DB, bất đồng bộ UX, nháy trang, tab count sai, chống chế nội dung ẩn, reply cấp 3+ thiếu, nút mod reply thiếu, modal LOAI_LIKE lỗi, kiểm tra dữ liệu.

## Kết quả

- Backend: Thêm `postCmt`, `eventCmt` vào `CommentStatsResponse`, cập nhật query `layThongKeBinhLuan()`.
- Frontend: Fix 9 nhóm lỗi trong `admin/comments.html`: xóa DB term, xóa ẩn nội dung, đổi confirm dialog, tích hợp PageTransitionManager, fix tab count, fix nút mod reply, thêm reply cấp 3+, chỉnh modal LOAI_LIKE, kiểm tra constants.
- Convention: Audit DieuKienCode pass, build + test pass.

---

# Historical Plan - Admin Comments Real Integration
> Last updated: 2026-05-28
> Status: DONE

## Mục tiêu đã hoàn thành

Chuyển `src/main/resources/templates/admin/comments.html` (968 dòng demo HTML tĩnh) thành trang quản trị bình luận hoạt động đầy đủ bằng JSON API. Quản lý tối thiểu 100% chức năng comment user side đang có + khai thác tối đa 16 bảng CSDL comment/moderation/reaction/report.

## Hướng làm chính

Backend bổ sung 1 DTO mới + 2 service method + 2 endpoint GET moderation log.
Frontend rewrite toàn bộ comments.html thay demo data bằng API call thật, gồm 7 phase: Stats + Toolbar + Tabs → List + Paging → Bulk + Mod → Detail Modal → Report Modal → LOAI_LIKE + Actions → Replies lazy-load.
Audit convention DieuKienCode, test build/pass, sync memory.

## Kết quả thực hiện

- Backend: Thêm `CmtModerationLogResponse` DTO, 2 service method `layLichSuKiemDuyetCmt/PhCmt`, 2 endpoint GET moderation-log cho CMT + PH_CMT.
- Frontend: Rewrite comments.html (968→~1500 dòng), stats/toolbar/tabs load API, danh sách render từ search đa chiều + pagination, checkbox + bulk bar + moderation đơn lẻ, modal chi tiết (nội dung, reactions, lịch sử kiểm duyệt/sửa, quick mod), modal report (list/filter/resolve/reject), modal LOAI_LIKE CRUD + MODERATION_ACTIONS read-only, reply lazy-load cấp 2/3 + mod actions.
- Convention DieuKienCode: Comment đầy đủ trước hàm/action, bỏ ternary phức tạp, format rõ ràng.
- Self-eval: `bash mvnw -q -DskipTests compile` pass, `bash mvnw -q test` pass, `git diff --check` pass, console JS zero error.
- Khai thác 16 bảng DB: CMT, PH_CMT, CT_POST_CMT, CT_EVENT_CMT, LOAI_LIKE, CT_LIKECMT, CT_LIKEPHCMT, CT_CMT_REPORTS, CT_PH_CMT_REPORTS, CT_CMT_REPORT_MOD_LOG, CT_PH_CMT_REPORT_MOD_LOG, CT_CMT_ACTION_LOG, CT_PH_CMT_ACTION_LOG, CT_CMT_MODERATION_LOG, CT_PH_CMT_MODERATION_LOG, MODERATION_ACTIONS.

---

# Historical Plan - PageTransitionManager (Hệ thống chống nháy trang)
> Last updated: 2026-05-28
> Status: DONE (T1-T10), PENDING browser test (T11), TODO memory (T12)

Xây dựng `PageTransitionManager` — 1 utility JS chung + 1 file CSS chung — giảm/xóa hiện tượng nháy trang khi load data từ API trên 5 template HTML. T1-T10 hoàn thành, T11 cần browser test, T12 cần memory sync.

---

# Historical Plan - Admin Events Production Fix
> Last updated: 2026-05-26
> Status: DONE

## Mục tiêu đã hoàn thành

Fix toàn diện các lỗi admin events đã audit: backend integrity/security, public locked-session privacy, media upload flow, frontend enum/API/UX/readability, và duplicate-render root cause. Không sửa bằng dedupe chống chế theo nội dung; chỉ xử lý khi cùng một record ID bị render lặp.

## Hướng làm chính

1. Chốt contract trước khi code:
   - Event status và registration status phải lấy từ API dictionary.
   - Media admin dùng file picker/upload endpoint, không bắt nhập path nội bộ.
   - Locked session public không lộ speaker/agenda/related posts/attendee metadata.
   - Duplicate render phân biệt bằng record ID; hai bản ghi khác nhau cùng nội dung vẫn là dữ liệu hợp lệ.

2. Backend trước:
   - Thêm status dictionary endpoint.
   - Thêm media upload endpoint/contract và validate content type/size/path/URL length.
   - Clamp page/size.
   - Admin list không ẩn campaign chưa có session.
   - Validate `endTime > startTime` cho session và agenda ở create/update.
   - Reject update session nếu `eventId` body khác campaign hiện tại.
   - Validate atomic `roleIds`, `tagIds`, `relatedPostIds`, `speakerIds`.
   - Type/location delete phải check FK và trả lỗi nghiệp vụ.
   - Registration status update không được overbook.

3. Public/security:
   - Invalid `roleIds` không được fail-open thành public session.
   - Locked/gated session không lộ speaker, agenda, related posts, attendee metadata/count trái quyền.

4. Frontend `admin/events.html`:
   - Bỏ hardcode `EVENT_STATUSES` và `REG_STATUSES`, load từ API.
   - Đổi media text input sang file picker/upload flow có preview.
   - Type/location UX có loading/empty/error state rõ.
   - Thêm comment/ghi chú trước hàm/action lớn, bỏ ternary quan trọng nếu làm khó đọc.
   - Điều tra duplicate render theo ID, fix root cause ở query/state/render; không dedupe theo tên/nội dung.

5. Test/eval:
   - Backend tests/compile cho validation atomic, fail-open, locked privacy, overbook, page clamp, no-session campaign visibility, FK dependency delete.
   - Frontend parse/smoke cho dictionary, upload flow, type/location UX, duplicate scenario.
   - Chạy `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, `git diff --check`, `node --check` inline script nếu còn JS trong template.

## Trạng thái kết thúc

- DONE: Persist plan fix vào `.ai-memory/04_active_plan.md`.
- DONE: Sửa backend/security trước frontend.
- DONE: Sửa frontend admin events theo API contract mới.
- DONE: Self-eval và memory sync sau khi pass.

---

# Historical Plan - Admin Events Supplementary Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before reviewer/deep-reviewer/security-auditor: YES

Audit bổ sung admin events theo correction mới của user, đối chiếu working tree hiện tại với `HEAD`, kiểm tra frontend/backend bằng evidence thực tế thay vì suy đoán.

---

# Historical Plan - Admin Events Deep Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before implementation: YES

Audit chuyên sâu thay đổi admin events hiện tại so với `HEAD` và `DieuKienCode.MD`, xác định phần nào vi phạm yêu cầu ban đầu: OOP, ai làm việc nấy, comment tường minh, tái sử dụng, không dùng pattern bị cấm.

---

# Historical Plan - Admin Events Real Integration
> Status: DONE

## Mục tiêu đã hoàn thành

Chuyển `src/main/resources/templates/admin/events.html` từ demo HTML tĩnh sang trang quản trị sự kiện chạy thật bằng JSON API, quản lý tối thiểu các nghiệp vụ user event list/detail/my registrations đang sử dụng và khai thác thêm các bảng event đã có trong `CSDL/FileKhoiTaoCSDL.sql`.

---
