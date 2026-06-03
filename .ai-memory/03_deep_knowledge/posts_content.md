# Posts & Content
> Last updated: 2026-06-03
> Source files: `controller/view/PostViewController.java`, `controller/api/ApiPostController.java`, `controller/api/ApiAdminPostController.java`, `service/impl/PostServiceImpl.java`, `service/impl/AdminPostServiceImpl.java`, `service/itf/IAdminPostService.java`, `repositories/IRepository/IPostRepository.java`, `entities/Post.java`, `templates/posts/list.html`, `templates/posts/detail.html`, `templates/admin/posts.html`
> Confidence: HIGH

## Summary

Posts are split into public content APIs/pages and admin post management. Public pages render `/posts` and slug details; admin page `admin/posts.html` manages posts, categories, tags, media, files, comments preview, reactions, linked events, publish/featured state, bulk actions and Rich Content Editor.

## Current Size / Inventory

- `admin/posts.html`: 2565 lines.
- `AdminPostServiceImpl`: 1288 lines.
- `ApiAdminPostController`: 33 mapped admin endpoints under `/api/admin/posts`.

## Public Flow

1. `PostViewController` returns `posts/list` or `posts/detail`.
2. Template JS calls `ApiPostController` under `/api/posts`.
3. `PostServiceImpl` reads posts/categories/tags/reactions/view/download data and maps to DTOs.
4. Gated content is resolved in backend using `CtPostRole` and `UserRole.roleLevel`.

## Public Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/posts/stats` | Public post stats | No |
| GET | `/api/posts/categories` | Categories with counts | No |
| GET | `/api/posts/tags` | Tag cloud | No |
| GET | `/api/posts/featured` | Featured posts | No |
| GET | `/api/posts` | Search/filter/page posts | No |
| GET | `/api/posts/{slug}` | Post detail by slug; auth affects gated content | Optional |
| POST | `/api/posts/{postId}/view` | Track view | No |
| POST | `/api/posts/like` | React to post | `USER_REACT` |
| POST | `/api/posts/files/{fileId}/download-track` | Track file download when logged in | Optional |

## Admin Endpoints (`/api/admin/posts`)

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/stats` | Admin stats | `POST_VIEW` |
| GET | `/` | List with keyword/category/role/published/date range/sort/page/size | `POST_VIEW` |
| POST | `/` | Create post | `POST_CREATE` |
| PUT | `/{postId}` | Update post | `POST_EDIT` |
| DELETE | `/{postId}` | Delete post with cascade cleanup | `POST_DELETE` |
| GET | `/{postId}` | Admin detail including drafts | `POST_VIEW` |
| POST | `/upload-thumbnail` | Upload thumbnail to `/uploads/posts/thumbnails/` | `POST_EDIT` |
| PATCH | `/{postId}/publish` | Toggle published | `POST_EDIT` |
| PATCH | `/{postId}/featured` | Toggle featured | `POST_EDIT` |
| GET | `/dictionaries` | Roles/options for UI | `POST_VIEW` |
| PATCH | `/bulk/publish` | Bulk publish/unpublish | `POST_EDIT` |
| DELETE | `/bulk` | Bulk delete | `POST_DELETE` |
| GET/POST/PUT/DELETE | `/categories/**` | Category CRUD and FK checks | `POST_MANAGE_CATEGORY` |
| GET/POST/PUT/DELETE | `/tags/**` | Tag CRUD and FK checks | `POST_MANAGE_TAG` |
| GET/POST/DELETE/PATCH | `/{postId}/images/**` | Gallery list/upload/delete/reorder | `POST_EDIT` |
| GET/POST/DELETE | `/{postId}/files/**` | Attachment list/upload/delete | `POST_EDIT` |
| GET | `/{postId}/comments` | Comment preview | `POST_VIEW` |
| GET | `/{postId}/reactions` | Reactions grouped by type | `POST_VIEW` |
| GET/POST/DELETE | `/{postId}/events/**` | Linked event sessions | `POST_EDIT` |

## Business Rules

- Public list only returns published posts.
- Admin list returns all posts and supports keyword/category/role/published/date range/sort.
- `Post.isFeatured` controls featured state.
- If a post has no `CtPostRole`, it is public.
- If a post has role gates and user lacks sufficient level, backend returns limited data/content according to DTO contract.
- Author profile resolves through public profile, partner profile, then user fallback, with privacy handling.
- View logs record guest IP/user nullable.
- Download tracking logs only authenticated users to keep lead data clean.
- Delete post cleans dependent post tables: tags, roles, images, files, view logs, downloads, comments, reactions and post-event links.
- File/image upload validates type and size server-side.
- Admin frontend escapes user-generated strings and uses PageTransitionManager for lists/categories/tags.
- Rich Content Editor shared files are `rich-content-editor.js` and `rich-content-editor.css`.

## DTOs

- `AdminPostDictionaryResponse`: options for admin dropdowns.
- `AdminPostMediaResponse`: upload result (`url`, `fileName`).
- `PostCommentPreviewResponse`: truncated comment preview.
- `PostLinkedEventResponse`: linked event session DTO.
- `PostReactionSummary`: grouped reactions.
- `RoleOptionResponse`: role option for gates.

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| RCE extracted to shared static files | `rich-content-editor.js/css` used by posts/events | Avoid duplicated editor logic in large admin templates | 2026-05-30 | 2026-08-30 |
| Admin post media uses server upload namespaces | `/uploads/posts/thumbnails`, `/images`, `/files` | Avoid asking admin users to type internal paths | 2026-05-31 | 2026-08-31 |
