# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-31

## Trang thai hien tai

- **IDLE** — Admin Posts Phase 2+3+4 HOAN CHINH. Commit `89977f7` (2026-05-31) gom tat ca thay doi.
- Admin posts module: 33 public methods (AdminPostServiceImpl 1287 dong), 32 controller endpoints, 6 DTO moi, tab system 5 tab, gallery/files CRUD, comments preview, reactions/events linking, category/tag CRUD modals, 8 stats cards, Rich Content Editor, cascade delete 9 bang, XSS escapeHtml, PageTransitionManager, isFeatured toggle, bulk actions. Frontend 2564 dong.
- Admin events module: Rich Content Editor tich hop cho campaign description va session content. Frontend 3663 dong.
- Admin comments module hoan chinh tu 2026-05-29.
- Rich Content Editor tach thanh file dung chung (`rich-content-editor.js` 1017 dong + `rich-content-editor.css` 196 dong), include tu `admin_layout.html`.

## Cac file untracked chua commit

- `AdminPostDictionaryResponse.java` — DTO moi (roles dropdown)
- `AdminPostMediaResponse.java` — DTO moi (upload result)
- `PostCommentPreviewResponse.java` — DTO moi (comment preview)
- `PostLinkedEventResponse.java` — DTO moi (event link)
- `PostReactionSummary.java` — DTO moi (reaction grouped)
- `RoleOptionResponse.java` — DTO moi (role option)
- `rich-content-editor.css` — CSS editor dung chung (196 dong)
- `rich-content-editor.js` — JS editor dung chung (1017 dong)
- `admin/test.html` — file test (co the xoa)
- `uploads/comments/` — thu muc upload comments runtime
- `uploads/posts/` — thu muc upload posts runtime
- `uploads/test/` — thu muc test (co the xoa)

## Context cho phien sau

- `mvnw.cmd` dang loi wrapper PowerShell; dung `bash mvnw` cho compile/test.
- Hai file `posts/detail.html` va `events/detail.html` co comment system tuong dong — khi sua 1 ben nen kiem tra ben kia.
- Admin comments reply dung prefix `adm-` cho CSS class (adm-ri-bubble-wrapper) de tranh xung dot voi user-side.
- LOAI_LIKE modal co 2 che do: emoji (text input) va upload anh (file picker + preview). Icon upload vao `/uploads/comments/reaction-icons/`.
- Rich Content Editor dung bien `rceHienTaiEditorId`/`rceHienTaiPreviewId` de ho tro nhieu editor tren 1 trang. Ham `khoiTaoRichEditor()` khoi tao editor.
- 6 DTO moi chua commit — can `git add` va commit.

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
2026-05-27: Codex rewrite admin event module (+49 files), commit 871f7dd.
2026-05-28: User fix codex tach ImagePathUtil + PagingUtil, commit 2d64c50.
2026-05-28: Admin comments rewrite API-driven + PageTransitionManager, commit ffcced1 + ecf601d.
2026-05-29: Fix LOAI_LIKE upload + reply tree branch, commit 1bb9565.
2026-05-31: Nang cap admin post va event, commit 89977f7.
```
