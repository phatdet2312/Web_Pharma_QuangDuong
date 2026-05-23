# Active Plan - Ke hoach hien tai
> Last updated: 2026-05-22

## Muc tieu hien tai

Trien khai dung correction moi nhat cho `CSDL/DuLieuMau.sql`: giu phong cach HTML post ban user ung, chi go lap/template, chuyen anh/file sang URL remote, mo rong `CT_EVENTS` 1-5 buoi/event, sua comment/reply lap va timestamp vat ly. Sau loi SSMS `GUEST_NAME NOT NULL`, da fix `CT_EVENT_REGISTRATIONS` va validator schema-wide + nghiep vu deu **PASS**.

## Task Breakdown

| # | Task | Status | Ghi chu |
|---|------|--------|---------|
| 1 | Kiem tra guard `.codexignore`, bootstrap memory va drift | DONE | Bootstrap OK; `CSDL/`/`*.sql` chi doc file cu the vi user yeu cau ro |
| 2 | Doc `TEST.MD`, schema, deep knowledge va file `DuLieuMau.sql` | DONE | Da doi chieu DatabaseSeeder, backend event/comment contract |
| 3 | Validator dinh luong chat luong du lieu | DONE | Phat hien loi transaction, event count, duplicate post/comment/reply, timeline |
| 4 | Sua truc tiep `DuLieuMau.sql` | DONE | Transaction/idempotency, guarded seed simulation, EVENTS=100, status/timeline/capacity, post/comment/reply duplicate |
| 5 | Fix thieu source mapping cho 300 post | DONE | Da tao `CSDL/DULIEUMAU_POST_SOURCE_MATRIX.md` va them comment tro nguon trong `DuLieuMau.sql` |
| 6 | Recheck sau fix noi dung | DONE | Validator noi bo pass |
| 7 | Maven self-eval | DONE | `bash ./mvnw test` pass 1/1 |
| 8 | Audit lai sau user chay SSMS | DONE | Phat hien post/anh/event/thoi gian chua dat |
| 9 | Trien khai toan bo fix dataset | DONE | Giu post HTML hien tai, bo source marker lap, them technical-basis/figure, remote URL, event multi-session, timestamp; validator tinh PASS |
| 10 | Fix regression encode/readability sau user correction | DONE | Khong rewrite pha post; CMT/PH_CMT duplicate exact=0 va khong con ma noi bo, URL remote 2043/2043, cap nhat report |
| 11 | Fix runtime SSMS `CT_EVENT_REGISTRATIONS.GUEST_NAME NOT NULL` | DONE | Dien du `GUEST_NAME/GUEST_EMAIL/GUEST_PHONE` cho 13320 registrations; schema validator `null_viol=0`, `len_viol=0`, `missing_notnull=0` |
| 12 | Them du lieu test an thong tin post/event bang SUPERADMIN | DONE | `CT_POST_ROLES=12`, `CT_EVENT_SESSION_ROLES=32`; event duoc chon van con it nhat 1 buoi public |

## Task moi 2026-05-23 - Lazy-load phan hoi binh luan post

| # | Task | Status | Ghi chu |
|---|------|--------|---------|
| 1 | Doc CSDL/entity `PH_CMT` va flow comment post | DONE | `PH_CMT` co `ROOT_CMT_ID` + `PARENT_PH_ID` ho tro vo han tang |
| 2 | Thiet ke contract backend 3 tang hien thi | DONE | Root comment chi tra `replyCount`; cap 2/cap 3 co endpoint rieng |
| 3 | Trien khai API lazy-load reply | DONE | Them `/api/comments/{cmtId}/replies` va `/api/comments/reply/{phCmtId}/replies` |
| 4 | Cap nhat UI post detail | DONE | Nut `Xem X cau tra loi`, tai them/an reply; tag nguoi duoc tra loi la chip co the xoa; `@Ten` trong content duoc highlight khi render |
| 5 | Self-eval | DONE | `node` parse inline JS OK; `mvnw.cmd -q test` pass |

## Ket luan audit moi nhat

- Runtime wrapper: `GO=0`, khong con explicit transaction toan cuc; co `SET IMPLICIT_TRANSACTIONS OFF`, `SET XACT_ABORT ON`, `SET NOCOUNT ON`.
- Row count tong theo INSERT: 337406; FK static violations event: 0.
- Posts: 300 bai, min/avg/max words 1409/1567.1/1886; duplicate title/summary/content=0; `source_trace=0`, `internal_note=0`, `technical_basis=300`, `figure=300`, `img=300`.
- Comments/replies: `CMT=20088`, `PH_CMT=20000`, duplicate content exact=0/0; khong con `Ma trao doi`, `Ma phan hoi`, `CMT-`, `PHCMT-`, quote cat cut.
- Images/files: remote URL = 2043/2043; local URL=0; URL >255 ky tu=0; post thumbnail unique=300.
- Events: `EVENTS=100`, `CT_EVENTS=288`, distribution `{1:16, 2:24, 3:28, 4:20, 5:12}`.
- Event children coverage: moi CT_EVENT co 1 speaker, 2-4 agenda, 1 agenda-speaker/agenda, 1-3 tag, 1-2 post link, 2-3 status history, 18-75 registrations va 14-35 event comments.
- Dates: event schedule after 2026-07-30 = 0; non-schedule/action timestamp after business-now 2026-06-10 = 0.
- Registration: `CT_EVENT_REGISTRATIONS=13320`, null guest fields=0, registration_after_now=0, reg_after_start=0, future_attended=0, over_capacity=0.
- Access gating sample: `CT_POST_ROLES=12` gan role SUPERADMIN cho post; `CT_EVENT_SESSION_ROLES=32` gan role SUPERADMIN cho mot phan buoi cua 24 events, khong event nao bi khoa toan bo.
- Schema-wide validation theo `FileKhoiTaoCSDL.sql`: parse_viol=0, null_viol=0, len_viol=0, missing_notnull_no_default=0.
- Bao cao moi: `CSDL/BAO_CAO_KIEM_TRA_DULIEUMAU.md` ket luan **DAT validator tinh noi bo**.

## Hanh dong tiep theo dung

1. User chay lai `CSDL/DuLieuMau.sql` trong SSMS de verify runtime ban hien tai.
2. Neu SSMS bao loi, dung exact error de fix tiep.

## Cau hoi cho User

- Khong co.
