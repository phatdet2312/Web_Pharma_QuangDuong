# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-26

## Trang thai hien tai

- Comment event detail da duoc nang cap giong 100% post detail: CSS/HTML/JS ~40 ham, 3 cap comment, lazy-load, reaction, inline edit, report, tag mention.
- SecurityConfig da bo sung `/api/comments/events/**` vao `permitAll()` — fix 403 cho user chua co role.
- Compile pass, chua test tren browser.

## Thay doi quan trong gan day

### Event Comment System (2026-05-26)
- `events/detail.html`: CSS reaction picker/tree branch/author menu/inline edit/report modal, HTML comment section, JS comment system ~40 ham
- `SecurityConfig.java`: them `/api/comments/events/**` vao permitAll
- Fix 5 loi escape quote JS, 12 chuoi thieu dau tieng Viet
- Event va post chia se cung backend API (`ApiCommentController`), cung DB (`CMT`, `PH_CMT`)

### OOP Refactor (2026-05-24, commit c101490)
- `NguCanhNguoiDung` + `NguCanhNguoiDungFactory` trong `service/support`
- `EditContentRequest` thay ReplyRequest/CommentRequest cho PUT comment endpoints

## Context cho phien sau

- Can test event comment tren browser de xac nhan UI/UX hoat dong dung.
- Hai file `posts/detail.html` va `events/detail.html` gio co comment system tuong dong — khi sua 1 ben nen kiem tra ben kia.
- Comment event dung cung `CommentServiceImpl` nhung endpoint la `/api/comments/events/{eventId}`.
- Event comment co them kiem tra quyen truy cap session qua `coQuyenTruyCapBuoi(nguCanh, ctEventId)`.
- `.codexignore` van ton tai; `CSDL/` chi doc file cu the khi task yeu cau.

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
```
