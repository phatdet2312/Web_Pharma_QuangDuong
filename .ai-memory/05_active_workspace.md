# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-28

## Trang thai hien tai

- **IDLE** — khong co task dang do. Memory da dong bo 2026-05-28 sau 5 commit ngoai Claude/Codex.
- Admin events module da on dinh: dictionary/upload/bulk status endpoints, utility classes `ImagePathUtil` + `PagingUtil`, entity column length sync voi schema.
- 5 file dang sua chua commit: `admin_layout.html` (reformat CSS nhieu dong), `admin/events.html` (reformat), `admin/posts.html`, `admin/user-details.html`, `.claude/agents/15-planner.md`.

## Thay doi quan trong gan day

### Codex + User Changes (2026-05-27 ~ 2026-05-28)
- Codex commit `871f7dd`: mo rong lon admin event — them `AdminEventDictionaryResponse`, `AdminEventMediaResponse`, `StatusOptionResponse`, them validation DTO, sync entity length, upload endpoint, them repositories inject.
- User commit `2d64c50`: fix loi codex bang cach tach `ImagePathUtil` (validate URL anh) va `PagingUtil` (chuan hoa page/size) tu duplicate logic trong services/controllers.
- Codex config `6b6cfe0` + `34335b6`: cap nhat toan bo `.codex/` agents/hooks/scripts.
- User uncommitted: reformat `admin_layout.html` CSS tu single-line sang multi-line, cac template admin khac dang chinh.

### Admin Event Management (2026-05-26 — da on dinh)
- `admin/events.html`: trang quan tri dong goi API that, status dictionary, file upload, bulk actions.
- `AdminEventServiceImpl`: validate atomic IDs, FK delete, overbooking, locked privacy.
- `IAdminEventService`: them `layDanhMucTrangThai()`, `uploadAnhChienDich()`, `uploadAnhDienGia()`, `doiTrangThaiNhieuChienDich()`.

## Context cho phien sau

- `mvnw.cmd` dang loi wrapper PowerShell; dung `bash mvnw` cho compile/test.
- Hai file `posts/detail.html` va `events/detail.html` co comment system tuong dong — khi sua 1 ben nen kiem tra ben kia.
- `.codexignore` van ton tai; `CSDL/` chi doc file cu the khi task yeu cau.
- `admin_layout.html` dang co thay doi chua commit — can verify truoc khi lam task lien quan admin template.

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
2026-05-27: Codex rewrite admin event module (+49 files), commit 871f7dd.
2026-05-28: User fix codex tach ImagePathUtil + PagingUtil, commit 2d64c50.
```
