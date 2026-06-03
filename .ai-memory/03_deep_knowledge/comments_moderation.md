# Comments & Moderation
> Last updated: 2026-06-03
> Source files: `controller/api/ApiCommentController.java`, `controller/api/ApiAdminCommentController.java`, `controller/api/ApiReportController.java`, `controller/api/ApiAdminReportController.java`, `service/itf/ICommentService.java`, `service/impl/CommentServiceImpl.java`, `service/impl/PublicReportServiceImpl.java`, comment/report/moderation entities and repositories, `templates/posts/detail.html`, `templates/events/detail.html`, `templates/admin/comments.html`
> Confidence: HIGH

## Summary

Comments support post and event-session conversations, replies, reactions, edit/delete history, public reports and admin moderation. Public post detail and event detail share very similar comment systems; changes to one should be checked against the other.

## Current Size / Inventory

- `admin/comments.html`: 2620 lines.
- `CommentServiceImpl`: 1595 lines.
- Admin reaction icon upload endpoint: `POST /api/admin/comments/reaction-types/upload-icon`.
- Reaction icon upload directory: `/uploads/comments/reaction-icons/`.

## Public Comment Rules

- Public read is allowed for post/event comments and reaction types.
- Write/update/delete/reaction/report require authentication and relevant permission where annotated.
- Root comments use `Cmt`; replies use `PhCmt`.
- `PH_CMT` supports arbitrary depth through `PARENT_PH_ID`; UI shows root, level 2, and deeper replies grouped visually at level 3.
- Root comment list returns `replyCount`; replies are lazy-loaded.
- Reply mention chips are user-editable content. Do not force-add a tag at render time if content does not contain it.
- For post/event comment UI, preserve DOM classes that tree-branch CSS depends on (`ci-bubble-wrapper`, `ri-bubble-wrapper`, `reply-thread`, `has-replies`, etc.).

## Public Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/comments/reaction-types` | Reaction types | No |
| GET | `/api/comments/posts/{postId}` | Post comments | No |
| GET | `/api/comments/events/{eventId}` | Event/session comments | No |
| GET | `/api/comments/{cmtId}/replies` | Replies under root comment | No |
| GET | `/api/comments/reply/{phCmtId}/replies` | Deeper replies anchored by reply | No |
| POST | `/api/comments/posts/{postId}` | Create post comment | `USER_COMMENT` |
| POST | `/api/comments/events/{eventId}` | Create event comment | `USER_COMMENT` |
| POST | `/api/comments/reply` | Create reply | `USER_COMMENT` |
| PUT | `/api/comments/{cmtId}` | Edit comment using `EditContentRequest` | `USER_COMMENT` |
| DELETE | `/api/comments/{cmtId}` | Delete comment | Authenticated |
| PUT | `/api/comments/reply/{phCmtId}` | Edit reply using `EditContentRequest` | `USER_COMMENT` |
| DELETE | `/api/comments/reply/{phCmtId}` | Delete reply | Authenticated |
| GET | `/api/comments/{cmtId}/history` | Comment edit/action history | Yes |
| GET | `/api/comments/reply/{phCmtId}/history` | Reply history | Yes |
| POST | `/api/comments/like/cmt` | React to comment | `USER_REACT` |
| POST | `/api/comments/like/reply` | React to reply | `USER_REACT` |
| POST | `/api/reports/comments` | Report comment/reply | `USER_REPORT` |

## Admin Endpoints

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/api/admin/comments/stats` | Admin comment stats | `COMMENT_VIEW` |
| GET/POST/PUT/DELETE | `/api/admin/comments/reaction-types/**` | Reaction type CRUD/upload | `COMMENT_MANAGE_REACTION` |
| GET | `/api/admin/comments/pending` | Pending moderation queue | `COMMENT_VIEW` |
| GET | `/api/admin/comments` | Search/filter/page admin comments | `COMMENT_VIEW` |
| POST | `/api/admin/comments/moderate` | Moderate one target | `COMMENT_MODERATE` |
| DELETE | `/api/admin/comments/cmt/{cmtId}` | Delete root comment | `COMMENT_DELETE` |
| DELETE | `/api/admin/comments/reply/{phCmtId}` | Delete reply | `COMMENT_DELETE` |
| POST | `/api/admin/comments/bulk/moderate` | Bulk moderate | `COMMENT_MODERATE` |
| DELETE | `/api/admin/comments/bulk` | Bulk delete | `COMMENT_DELETE` |
| GET | `/api/admin/comments/cmt/{cmtId}/moderation-log` | Root comment moderation log | `COMMENT_VIEW` |
| GET | `/api/admin/comments/reply/{phCmtId}/moderation-log` | Reply moderation log | `COMMENT_VIEW` |
| GET/PATCH | `/api/admin/reports/comments/**` | View/resolve reports | `REPORT_VIEW` / `REPORT_RESOLVE` |

## Delete Lifecycle

- Physical delete of `PH_CMT` must delete descendants before parent.
- Use an iterative post-order approach with `parentId -> childIds`; detect cycles and throw `AppException` to rollback.
- Delete report moderation logs before report rows.
- Bulk delete comment must call the central `xoaCmtVatLy` lifecycle, not delete direct rows ad hoc.
- Root comment delete must handle replies, reactions, reports, moderation logs and join rows.

## Admin Comments UI Rules

- Admin sees original content even when hidden; do not reuse public hiding behavior.
- All user-generated content in `innerHTML` must pass `lamSachChuoiHTML()`.
- Admin comments uses PageTransitionManager for skeleton/swap and supports silent reload after bulk actions.
- Reply admin DOM uses `adm-ri-bubble-wrapper` and tree branch CSS adapted from `posts/detail.html`.
- Status labels use lookup maps (`cauHinhTrangThaiCmt`, `cauHinhTrangThaiReport`) with fallback to moderation action data.
- `CommentStatsResponse` fills `postCmt` and `eventCmt` from repository counts (`ICtPostCmtRepository.demTong`, `ICtEventCmtRepository.demTong`).

## Decision Log

| Decision | Option | Reason | Date | Expiry |
|----------|--------|--------|------|--------|
| Lazy-load replies | Root API returns `replyCount`, reply APIs load branches | Smaller payload and simpler UI control | 2026-05-23 | 2026-08-23 |
| Delete reply tree post-order | Children before parent, central lifecycle | Prevent FK failures and partial deletes | 2026-05-23 | 2026-08-23 |
| Admin reaction icon supports upload | `/uploads/comments/reaction-icons/` | User explicitly required image upload, not text-only icon input | 2026-05-29 | 2026-08-29 |
