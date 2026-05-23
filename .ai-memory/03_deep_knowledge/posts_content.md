# Posts & Content
> Last updated: 2026-05-18
> Source files: `controller/view/PostViewController.java`, `controller/api/ApiPostController.java`, `service/impl/PostServiceImpl.java`, `repositories/IRepository/IPostRepository.java`, `entities/Post.java`
> Confidence: HIGH

## Mô tả chức năng

Module bài viết cung cấp trang danh sách/chi tiết content y khoa, API public, analytics view/download, reaction và gated content theo role level. View controller chỉ trả template; dữ liệu thực tế được frontend gọi từ `/api/posts`.

## Luồng xử lý chính

1. Client mở `/posts`, `/tin-tuc`, `/tin-tuc-y-khoa` hoặc slug detail.
2. `PostViewController` trả template `posts/list` hoặc `posts/detail`.
3. JavaScript gọi `ApiPostController` dưới `/api/posts`.
4. `PostServiceImpl` truy vấn `IPostRepository` và các repository phụ như category/tag/file/image/like/view.
5. Service mapping entity sang DTO thủ công, tính stats, reaction counts, related posts, author profile.
6. Response trả bằng `ApiResponse<T>`.

## Business Rules quan trọng

- Public list chỉ lấy post `isPublished = true`.
- Search dùng keyword/category/roleId/page/size; sort hiện fallback `createdAt` cho views.
- Detail theo slug ném `AppException(404)` nếu không tìm thấy.
- Gated content dùng `CtPostRole` + `UserRole.roleLevel`: level càng nhỏ quyền càng cao; nếu user level đủ thì trả content, nếu không thì `content = null` và trả `requiredRoles`.
- Bài viết không gắn quyền nào được xem là public.
- Author profile có privacy shield: ưu tiên `PublicProfile`, fallback `PartnerProfile`, fallback user mặc định; nếu profile ẩn thì trả tác giả ẩn danh.
- `ghiNhanLuotXem` lưu `PostViewLog`; guest vẫn có IP, user nullable.
- `ghiNhanTaiTaiLieu` chỉ log download nếu đã đăng nhập; guest bỏ qua để giữ lead data sạch.

## API Endpoints

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

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |

## Ghi chú

- `PostServiceImpl` đang rất lớn và chứa nhiều mapping helper; khi sửa nên giữ thay đổi hẹp hoặc tách có kế hoạch.
- Table liên quan chính: `POSTS`, `CATEGORIES`, `TAGS`, `CT_POST_TAGS`, `CT_POST_ROLES`, `POST_IMAGES`, `POST_FILES`, `POST_VIEW_LOGS`, `CT_FILE_DOWNLOADS`, `CT_LIKEPOST`.
