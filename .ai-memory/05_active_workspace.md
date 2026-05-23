# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-23

## Trang thai hien tai

- 2026-05-23: Da trien khai lazy-load phan hoi binh luan cho post detail. `/api/comments/posts/{postId}` khong day toan bo `cmt.replies` cho post nua, chi tra `replyCount`; frontend mo reply bang 2 endpoint moi va hien thi toi da 3 tang theo kieu Facebook. Correction moi nhat: tag nguoi duoc tra loi la chip co the xoa trong form; khi `content` that su bat dau bang `@Ten`, UI highlight phan tag nay luc render; sua/xoa reply refresh dung nhanh dang mo thay vi reload comment root.
- 2026-05-23: Da fix regression sau lazy-load 3 tang: xoa `PH_CMT` theo cay hau tu de khong vi pham FK `PARENT_PH_ID`, don `CT_*_REPORT_MOD_LOG` truoc khi xoa report, bulk delete comment dung chung lifecycle `xoaCmtVatLy`, va dong form reply ngay sau khi API tao phan hoi thanh cong.
- 2026-05-24: Phat hien drift do Claude review/toi uu sau Codex. Code runtime hien tai giu event paywall va lazy-load comment, dong thoi toi uu `EventServiceImpl` tinh `userLevel` mot lan/request, them `EventStatusDisplayPolicy`, va toi uu dem/loc cay `PH_CMT` bang map parent->children. `mvnw.cmd -q test`, JS parse 3 template, va `git diff --check` deu pass.
- 2026-05-24: Fix gap sau review: `layPhanHoiCapHai` va `layPhanHoiCapBa` dung lai map `parentId -> childIds` trong cung request, xoa cay `PH_CMT` dung duyet iterative hau tu de xoa sach nhanh sau bat ky do sau hop le, vong lap du lieu se nem `AppException` rollback, va da bo cac ternary con sot trong `events/list.html`.
- 2026-05-24: Fix lai UI comment post detail theo root cause, khong them connector/vach trang tri: PH_CMT dung `.ria-reply` de day nut Tra loi sang phai nhu CMT, cap 3 bo margin thua vi DOM da long trong output cap 2, va reply navigation dung helper chung de clear khi `totalElements <= 0`.
- User da chay duoc `CSDL/DuLieuMau.sql` trong SSMS sau khi bo transaction toan cuc va sua success PRINT.
- Da trien khai toan bo correction ngay 2026-05-22 theo pham vi moi nhat: giu phong cach HTML post ban user ung, chi bo lap/template/marker noi bo, them muc technical-basis va figure remote, event multi-session 1-5 buoi, timestamp hanh dong user.
- Sau user correction ve loi dau `?`, post kem chat luong va comment/reply lap vo nghia, da khong rewrite pha post nua; da giu HTML rich hien co, sua CMT/PH_CMT duplicate exact ve 0 va bo toan bo marker noi bo.
- Ket qua moi nhat: **PASS validator tinh noi bo**. Bao cao chi tiet: `CSDL/BAO_CAO_KIEM_TRA_DULIEUMAU.md`.
- User chay SSMS va gap loi `Cannot insert the value NULL into column 'GUEST_NAME'` tai `CT_EVENT_REGISTRATIONS`; da fix bang cach dien du guest name/email/phone cho toan bo 13320 registrations. User chua chay lai ban da fix loi nay.

## Chi tiet validator moi nhat

- SQL wrapper: `GO=0`, explicit transaction toan cuc = 0, co `SET IMPLICIT_TRANSACTIONS OFF`, `SET XACT_ABORT ON`, `SET NOCOUNT ON`.
- Row count tong theo INSERT: 337406; FK static violations event = 0.
- Posts: 300, duplicate title/summary/content exact = 0, min/avg/max words = 1409/1567.1/1886, `source_trace=0`, `internal_note=0`, `technical_basis=300`, `figure=300`, `img_tag=300`.
- Comments/replies: `CMT=20088`, `PH_CMT=20000`, duplicate content exact=0/0; khong con marker `token`, `QD-`, `Ma trao doi`, `Ma phan hoi`, `Noi dung nay tra loi truc tiep`, `CMT-`, `PHCMT-`.
- Images/files: remote URL = 2043/2043; local URL issues = 0; URL >255 ky tu = 0; post thumbnail unique=300.
- Events: `EVENTS=100`, `CT_EVENTS=288`, per event distribution `{1:16, 2:24, 3:28, 4:20, 5:12}`.
- Event child coverage: speaker 1/CT, agenda 2-4/CT, agenda-speaker 1/agenda, tag 1-3/CT, post link 1-2/CT, status history 2-3/CT, registrations min/max 18/75, event comments min/max 14/35.
- Dates: event schedule after 2026-07-30 = 0; non-schedule/action after business-now 2026-06-10 = 0.
- Registration: `CT_EVENT_REGISTRATIONS=13320`, null guest fields=0, registration_after_now=0, reg_after_start=0, future_attended=0, over_capacity=0.
- Access gating sample: `CT_POST_ROLES=12` post SUPERADMIN-only; `CT_EVENT_SESSION_ROLES=32` buoi SUPERADMIN-only tren 24 events, moi event duoc chon van con it nhat 1 buoi public.
- Schema-wide validation theo `FileKhoiTaoCSDL.sql`: parse_viol=0, null_viol=0, len_viol=0, missing_notnull_no_default=0.

## Context quan trong cho phien sau

- `.codexignore` da duoc kiem tra; `CSDL/` va `*.sql` chi doc file cu the khi task yeu cau ro, khong quet rong.
- `CSDL/THÔNG TIN HẠN DÙNG SẢN PHẨM_20260413.xlsx` ton tai, 1394 rows/6 columns; day la nguon san pham noi bo dung cho rebuild.
- Source matrix da co tai `CSDL/DULIEUMAU_POST_SOURCE_MATRIX.md`: 300/300 post duoc map toi Excel noi bo + Roche/Accu-Chek/FDA/WHO/eLabDoc.
- Learning ve du lieu mau copy-template da len Lan=3; nen de xuat user chay `$promote-learning` de dua thanh rule enforced.

## Debug Notes

```text
2026-05-18: Tao `CSDL/DuLieuMau.sql` tu `TEST.MD`/Excel; user reject vi du lieu con cong thuc/thieu tu nhien.
2026-05-19: User reject ban rework; da xoa sach noi dung sai trong `CSDL/DuLieuMau.sql`, khong chay DB.
2026-05-20: Audit dataset lon; phat hien KHONG DAT va tao bao cao chi tiet.
2026-05-21: Memory truoc do ghi dataset da pass, nhung audit lai file thuc te cho thay KHONG DAT nghiem trong; da sua memory/report, chua patch noi dung SQL de tranh tao du lieu gia moi.
2026-05-21: User cho phep tu dong tin nguon chinh thong tim thay; da tao source matrix 300/300 post va them comment trong `DuLieuMau.sql`.
2026-05-21: User correction ve DatabaseSeeder: mo phong neu ket qua cuoi khong lech thi xem nhu khong dong vao. Da sua SQL truc tiep va validator pass.
2026-05-22: User yeu cau trien khai toan bo. Da rewrite dataset: 300 post unique theo validator, remote URL, 280 CT_EVENTS, fix timestamp; validator tinh PASS, chua chay SSMS.
2026-05-22: User correction nang: ban rewrite truoc sinh dau `?`, post mat chat luong HTML va comment/reply lap vo nghia. Da ghi learning, sau do user khoi phuc ban post ung y; lan nay giu HTML rich hien co, bo source marker lap, them technical-basis/figure remote, remote URL 2043/2043, CT_EVENTS=288 phan bo 1-5 buoi, CMT/PH_CMT duplicate exact=0, timestamp action sau 2026-06-10=0.
2026-05-22: User phat hien trong post con cau meta kem chuyen nghiep: `Bai viet nay giu cau truc...`, `Co so du lieu internet dung cho bai viet`, `nguon internet`. Da sua 300/300 section thanh `Can cu ky thuat va pham vi ap dung`, class `technical-basis`, noi dung tac nghiep ve ho so lo/nhan/IFU/COA/SOP; grep bad phrases = 0.
2026-05-22: User chay SSMS gap loi `CT_EVENT_REGISTRATIONS.GUEST_NAME NOT NULL`. Root cause: validator cu chua check `NOT NULL` schema. Da fix guest fields cho 13320 registrations va them schema-wide validation parse/null/length/missing-notnull deu 0.
2026-05-22: User yeu cau chi them du lieu test an thong tin post/event bang SUPERADMIN. Da them 12 rows `CT_POST_ROLES` va 32 rows `CT_EVENT_SESSION_ROLES`; validate FK/schema pass, khong sua noi dung khac.
2026-05-23: Implement lazy-load PH_CMT cho post: root comment page chi co `replyCount`, cap 2 endpoint phan trang, cap 3 gom tat ca con chau sau cap 2. Sau correction, tag la chip co the xoa trong form; neu user giu tag, noi dung gui len co tien to `@Ten` va khi render duoc highlight. Sau regression, edit/delete reply refresh dung cap 2/cap 3 dang mo. JS syntax OK va Maven test pass.
2026-05-23: Fix regression xoa comment sau khi PH_CMT co nhieu tang. Root cause: flow xoa cu xoa PH_CMT phang theo danh sach nen co the xoa cha truoc con; voi PARENT_PH_ID tu tham chieu se gay FK error va frontend chi hien canh bao 500. Da doi sang xoa con chau truoc cha, don log xu ly report truoc report, va dong form reply sau khi tao thanh cong.
2026-05-24: Claude toi uu code sau review: event access khong con query user level lap lai theo tung session, comment tree co map parent->children, tao teaser event bi khoa strip script/style/HTML/URL. Con ton tai mot so ternary cu trong `events/list.html` khong thuoc phan Claude vua sua.
2026-05-24: Sau yeu cau fix gap, da doi xoa cay PH_CMT sang duyet iterative hau tu khong phu thuoc gioi han 1000 tang; neu du lieu co vong lap thi nem `AppException` va rollback. Dong thoi dung chung map con theo cha cho dem/loc reply trong request va xoa ternary con sot tai filter/pagination event list.
2026-05-24: Rollback toan bo cac patch UI comment post detail bat dau tu yeu cau dong bo action row/vi tri reply/nut 0 vi lam hong giao dien. Code quay ve trang thai truoc chuoi patch UI nay; lazy-load/backend comment truoc do van giu.
2026-05-24: Re-fix UI comment theo dung root cause: thieu class can phai cho reply action, margin cap 3 bi cong hai lan, va cap nhat/thu gon navigation khong xu ly totalElements=0 tap trung. Da sua bang CSS/class/helper chung, khong them vach/connector.
```
