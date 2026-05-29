# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-30

## Trang thai hien tai

- **IDLE** — Audit chuyen sau 7 phase + fix XSS + fix bat dong bo trang thai/confirm DONE. Memory dong bo 2026-05-30.
- Admin comments module hoan chinh: XSS fixed (31 diem escape), renderLichSuSua hien old/new content, report table hien IP + label tieng Viet, status pill data-driven (lookup map + fallback API), confirm dialog dong bo SAVE/DELETE, DB terms don sach.
- Admin events module da on dinh tu 2026-05-26.

## Cac file dang thay doi chua commit

- `ApiAdminCommentController.java` — them endpoint upload-icon + moderation-log CMT/PH_CMT
- `CommentStatsResponse.java` — them field `postCmt`, `eventCmt`
- `ICtEventCmtRepository.java` — them `demTong()`
- `ICtPostCmtRepository.java` — them `demTong()`
- `CommentServiceImpl.java` — them `uploadIconReaction`, `layLichSuKiemDuyetCmt/PhCmt`, cap nhat `layThongKeBinhLuan`
- `ICommentService.java` — them 3 method interface tuong ung
- `admin/comments.html` — rewrite + fix 9 nhom loi + LOAI_LIKE upload + reply tree branch
- `CmtModerationLogResponse.java` — DTO moi (untracked)

## Context cho phien sau

- `mvnw.cmd` dang loi wrapper PowerShell; dung `bash mvnw` cho compile/test.
- Hai file `posts/detail.html` va `events/detail.html` co comment system tuong dong — khi sua 1 ben nen kiem tra ben kia.
- Admin comments reply dung prefix `adm-` cho CSS class (adm-ri-bubble-wrapper) de tranh xung dot voi user-side.
- LOAI_LIKE modal co 2 che do: emoji (text input) va upload anh (file picker + preview). Icon upload vao `/uploads/comments/reaction-icons/`.

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
2026-05-27: Codex rewrite admin event module (+49 files), commit 871f7dd.
2026-05-28: User fix codex tach ImagePathUtil + PagingUtil, commit 2d64c50.
2026-05-28: Admin comments rewrite API-driven + PageTransitionManager, commit ffcced1 + ecf601d.
2026-05-29: Fix LOAI_LIKE upload + reply tree branch, chua commit (bfaa0a0 la commit truoc do).
```
