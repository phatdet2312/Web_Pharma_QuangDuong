# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-25

## Trang thai hien tai

- Code backend comment/event da on dinh sau OOP refactor + EditContentRequest fix (commit c101490, 2026-05-24).
- UI comment post detail co tree branch CSS kieu Facebook do user tu implement (commit ebf0b89, 2026-05-25): vach doc va nhanh L noi avatar cha voi reply con, 2 cap (L1->L2, L2->L3).
- User commit "DONG BO TRUOC KHI SUA CMT CUA EVENT" (52b6709) — cho thay user sap sua comment cua event (khac post).

## Thay doi quan trong gan day

### CSDL/ folder da don dep (2026-05-25)
- **DA XOA**: `DuLieuMau.sql` (367K dong), `BAO_CAO_KIEM_TRA_DULIEUMAU.md`, `DULIEUMAU_POST_SOURCE_MATRIX.md`, `TEST.MD`
- **CON GIU**: `FileKhoiTaoCSDL.sql` (schema), `THONG TIN HAN DUNG SAN PHAM_20260413.xlsx` (Excel san pham)
- Ly do: user quyet dinh xoa du lieu mau khoi repo

### OOP Refactor (2026-05-24, commit c101490)
- `NguCanhNguoiDung` + `NguCanhNguoiDungFactory` trong `service/support`
- 9 method `IEventService`/`EventServiceImpl` doi `Long userId` -> `NguCanhNguoiDung`
- Controller inject factory, tao context 1 lan/request
- `EditContentRequest` thay ReplyRequest/CommentRequest cho PUT comment endpoints

### UI Tree Branch CSS (2026-05-25, commit ebf0b89)
- CSS variables cho avatar/gap/trunk offset 2 cap
- Selector dung `:has()` va `:last-child` de ve/cat nhanh
- Class DOM can bao ton: `.has-replies`, `.ci-bubble-wrapper`, `.ri-bubble-wrapper`, `.reply-form-wrapper`

## Context cho phien sau

- User sap lam viec voi comment event (khac voi comment post da hoan thanh).
- Comment event dung cung `CommentServiceImpl` nhung endpoint la `/api/comments/events/{eventId}`.
- Event comment co them kiem tra quyen truy cap session qua `coQuyenTruyCapBuoi(nguCanh, ctEventId)`.
- `.codexignore` van ton tai; `CSDL/` chi doc file cu the khi task yeu cau.

## Debug Notes

```text
2026-05-24: Claude OOP refactor + EditContentRequest bug fix hoan thanh, commit c101490.
2026-05-25: User tu implement tree branch CSS cho comment post detail, commit ebf0b89.
2026-05-25: User xoa CSDL/DuLieuMau.sql + reports + TEST.MD khoi repo, commit 52b6709.
```
