# Posts & Content
> Last updated: 2026-05-31
> Source files: `controller/view/PostViewController.java`, `controller/api/ApiPostController.java`, `controller/api/ApiAdminPostController.java`, `service/impl/PostServiceImpl.java`, `service/impl/AdminPostServiceImpl.java`, `service/itf/IAdminPostService.java`, `repositories/IRepository/IPostRepository.java`, `entities/Post.java`
> Confidence: HIGH

## Mô tả chức năng

Module bài viết gồm 2 phần: (1) Public — trang danh sách/chi tiết content y khoa, API public, analytics view/download, reaction và gated content theo role level; (2) Admin — trang quản trị `admin/posts.html` CRUD bài viết, danh mục, tag, gallery ảnh, file đính kèm, bình luận preview, reactions, sự kiện liên kết, Rich Content Editor. View controller chỉ trả template; dữ liệu thực tế được frontend gọi từ `/api/posts` (public) và `/api/admin/posts` (admin).

## Luồng xử lý chính

### Public
1. Client mở `/posts`, `/tin-tuc`, `/tin-tuc-y-khoa` hoặc slug detail.
2. `PostViewController` trả template `posts/list` hoặc `posts/detail`.
3. JavaScript gọi `ApiPostController` dưới `/api/posts`.
4. `PostServiceImpl` truy vấn `IPostRepository` và các repository phụ.
5. Service mapping entity sang DTO thủ công, tính stats, reaction counts, related posts, author profile.
6. Response trả bằng `ApiResponse<T>`.

### Admin
1. Admin mở `/admin/posts` → `AdminViewController` trả template `admin/posts`.
2. JavaScript gọi `ApiAdminPostController` dưới `/api/admin/posts`.
3. `AdminPostServiceImpl` (1287 dòng, 33 public methods) xử lý CRUD posts, categories, tags, gallery, files, comments preview, reactions, events linking, dictionary, bulk actions.
4. Upload ảnh/file ghi vào `/uploads/posts/` (thumbnail, gallery, files).
5. Rich Content Editor (file dùng chung `rich-content-editor.js` + `rich-content-editor.css`) cho soạn thảo nội dung WYSIWYG.

## Business Rules quan trọng

- Public list chỉ lấy post `isPublished = true`.
- Admin list lấy tất cả bất kể trạng thái, hỗ trợ filter keyword/category/roleId/published/dateRange/sort.
- Search dùng keyword/category/roleId/page/size; sort hiện fallback `createdAt` cho views.
- Detail theo slug ném `AppException(404)` nếu không tìm thấy.
- Gated content dùng `CtPostRole` + `UserRole.roleLevel`: level càng nhỏ quyền càng cao; nếu user level đủ thì trả content, nếu không thì `content = null` và trả `requiredRoles`.
- Bài viết không gắn quyền nào được xem là public.
- Author profile có privacy shield: ưu tiên `PublicProfile`, fallback `PartnerProfile`, fallback user mặc định; nếu profile ẩn thì trả tác giả ẩn danh.
- `ghiNhanLuotXem` lưu `PostViewLog`; guest vẫn có IP, user nullable.
- `ghiNhanTaiTaiLieu` chỉ log download nếu đã đăng nhập; guest bỏ qua để giữ lead data sạch.
- Xóa bài viết cascade 9 bảng: tags, images, files, view logs, downloads, comments, reactions, post-events, post-roles.
- Upload ảnh/file validate file type + size cả client + server.
- Entity `Post` có field `isFeatured` (boolean) để đánh dấu bài viết nổi bật.

## API Endpoints — Public

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| GET | `/api/posts/stats` | Thống kê trang bài viết | No |
| GET | `/api/posts/categories` | Danh mục kèm số bài | No |
| GET | `/api/posts/tags` | Tag cloud | No |
| GET | `/api/posts/featured` | Bài viết nổi bật | No |
| GET | `/api/posts` | Search/filter/page bài viết | No |
| GET | `/api/posts/{slug}` | Chi tiết bài viết theo slug | No, nhưng auth ảnh hưởng gated content |
| POST | `/api/posts/{postId}/view` | Ghi nhận lượt xem | No |
| POST | `/api/posts/like` | Reaction bài viết | Yes |
| POST | `/api/posts/files/{fileId}/download-track` | Log tải tài liệu | Optional, chỉ log khi có user |

## API Endpoints — Admin (`/api/admin/posts`)

| Method | Path | Mô tả |
|--------|------|-------|
| GET | `/stats` | Thống kê admin (tổng, nháp, gated, downloads, views, comments, reactions, featured) |
| GET | `/` | Danh sách + filter keyword/category/roleId/published/dateRange/sort + phân trang |
| GET | `/{postId}` | Chi tiết bài viết (admin, bao gồm nháp) |
| POST | `/` | Tạo bài viết mới (cần Authentication) |
| PUT | `/{postId}` | Cập nhật bài viết |
| DELETE | `/{postId}` | Xóa bài viết (cascade 9 bảng) |
| PATCH | `/{postId}/publish` | Bật/tắt xuất bản |
| PATCH | `/{postId}/featured` | Bật/tắt nổi bật |
| GET | `/dictionaries` | Danh mục từ điển (roles cho dropdown) |
| POST | `/upload-thumbnail` | Upload ảnh thumbnail (multipart) |
| PATCH | `/bulk/publish` | Đổi trạng thái xuất bản hàng loạt |
| DELETE | `/bulk` | Xóa hàng loạt bài viết |
| GET | `/categories` | Lấy tất cả danh mục |
| POST | `/categories` | Tạo danh mục |
| PUT | `/categories/{id}` | Cập nhật danh mục |
| DELETE | `/categories/{id}` | Xóa danh mục (FK check) |
| GET | `/tags` | Lấy tất cả tag |
| POST | `/tags` | Tạo tag |
| PUT | `/tags/{id}` | Cập nhật tag |
| DELETE | `/tags/{id}` | Xóa tag (FK check) |
| GET | `/{postId}/images` | Lấy ảnh gallery |
| POST | `/{postId}/images` | Upload ảnh gallery (multipart) |
| DELETE | `/{postId}/images/{imageId}` | Xóa ảnh gallery |
| PATCH | `/{postId}/images/reorder` | Đổi thứ tự ảnh |
| GET | `/{postId}/files` | Lấy file đính kèm |
| POST | `/{postId}/files` | Upload file đính kèm (multipart) |
| DELETE | `/{postId}/files/{fileId}` | Xóa file đính kèm |
| GET | `/{postId}/comments` | Bình luận preview (phân trang, truncate 200 ký tự) |
| GET | `/{postId}/reactions` | Reactions grouped by type |
| GET | `/{postId}/events` | Sự kiện liên kết |
| POST | `/{postId}/events` | Liên kết bài viết với buổi sự kiện |
| DELETE | `/{postId}/events/{ctEventId}` | Xóa liên kết |

## DTO mới cho Admin Posts

| DTO | Mô tả |
|-----|-------|
| `AdminPostDictionaryResponse` | Gom roles cho dropdown cấp độ truy cập |
| `AdminPostMediaResponse` | Kết quả upload (url + fileName) |
| `PostCommentPreviewResponse` | Preview bình luận (cmtId, authorName, authorAvatar, content truncate, createdAt) |
| `PostLinkedEventResponse` | Buổi sự kiện liên kết (ctEventId, eventTitle, startTime, endTime) |
| `PostReactionSummary` | Reaction grouped (code, name, iconUrl, count) |
| `RoleOptionResponse` | Option dropdown role (id, roleName, roleLevel) |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Detail modal dùng tab system 5 tab | Tab system (không accordion) | events.html dùng tab, consistency | 2026-05-30 | 2026-08-30 | N/A |
| Images/Files là sub-resource REST nested | /posts/{id}/images, /posts/{id}/files | REST convention, events cũng dùng | 2026-05-30 | 2026-08-30 | N/A |
| Category/Tag CRUD dùng modal inline | Modal table + form cùng 1 modal | events dùng modal cho Type/Location | 2026-05-30 | 2026-08-30 | N/A |
| Editor dùng contenteditable div | contenteditable + execCommand | User yêu cầu không dùng thư viện, vanilla JS | 2026-05-30 | 2026-08-30 | N/A |
| RCE tách file dùng chung | CSS + JS standalone, include từ admin_layout | Dùng cho cả posts và events editor | 2026-05-30 | 2026-08-30 | N/A |

## Ghi chú

- `PostServiceImpl` rất lớn, chứa nhiều mapping helper; `AdminPostServiceImpl` (700+ dòng) tách riêng cho admin.
- `admin/posts.html` 2564 dòng: tab system 5 tab, gallery CRUD, files CRUD, comments preview, reactions/events linking, category/tag CRUD modals, 8 stats cards, Rich Content Editor, escapeHtml XSS protection.
- Rich Content Editor (`rich-content-editor.js` + `rich-content-editor.css`) là file dùng chung, include từ `admin_layout.html`, dùng cho cả admin posts và admin events.
- Upload ảnh/file ghi vào `/uploads/posts/` (thumbnail, gallery, files).
- Table liên quan chính: `POSTS`, `CATEGORIES`, `TAGS`, `CT_POST_TAGS`, `CT_POST_ROLES`, `POST_IMAGES`, `POST_FILES`, `POST_VIEW_LOGS`, `CT_FILE_DOWNLOADS`, `CT_LIKEPOST`, `CT_POST_CMT`, `CT_POST_EVENTS`.
