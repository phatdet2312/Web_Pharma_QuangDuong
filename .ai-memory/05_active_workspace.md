# Active Workspace - Ban lam viec hien tai
> Last updated: 2026-05-26

## Trang thai hien tai

- Admin events production fix da hoan thanh sau correction moi nhat: backend/security/frontend da duoc sua, self-eval pass. Subagent deep/security bi loi usage limit nen review cuoi duoc thuc hien thu cong theo checklist, khong bo qua.
- `admin/events.html` khong con hardcode `EVENT_STATUSES`/`REG_STATUSES`; status select va bulk status lay tu `/api/admin/events/dictionaries/statuses`.
- Media campaign/speaker dung file picker + multipart upload endpoint, hidden field chi luu URL server tra ve trong `/uploads/events/campaigns/` hoac `/uploads/events/speakers/`; backend reject URL/path ngoai namespace nay.
- Admin campaign query da fix root cause filter/render: khong join tao duplicate, khong an campaign chua co session khi khong loc session; neu loc ngay/location/role thi dieu kien phai khop tren cung mot session.
- Type/location modal co CRUD API that va empty state ro. Khong dedupe theo ten/noi dung; neu cung mot record bi render lap thi phai truy root cause query/state/render.
- Public locked session da chan speaker/agenda/related posts/attendee summary trai quyen; public stats/list dung chung helper parse time de loi format tra 400.
- Verify pass: `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, Node parse inline script `admin/events.html`, `git diff --check`.

- Historical audit FAIL truoc lan fix nay da duoc giu trong `.ai-memory/04_active_plan.md`; cac loi chinh trong audit do da duoc xu ly o trang thai hien tai ben tren.
- Admin events da duoc sua sau correction cua user: template khong con cac khoi HTML/JS dai mot dong; script dung helper + event delegation, line length `events.html` <= 180 ky tu.
- Root cause render rong khi vua vao trang: admin service truyen `startDate/endDate = null` vao query bat buoc so sanh thoi gian. Da fix default range `2000-01-01` den `2099-12-31` nhu public event service.
- Admin events da chuyen tu demo HTML sang UI goi API that: stats/list/filter/pagination, CRUD campaign/session/type/location, status/history, registrations, speakers, agenda, tag/role/related posts, comment preview.
- Backend admin event da bo sung endpoint GET speakers/agenda cho admin, validate DTO bat buoc, `roleId` trong requiredRoles, `relatedPostIds` cho session, cleanup dependency khi xoa campaign/session, validate status.
- `node --check` inline script, `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, `git diff --check` pass.
- Local dev server code moi dang nghe `http://localhost:8081`; `/admin/events` redirect HTTP 302 khi chua dang nhap admin, public `/api/events?page=0&size=1` tra HTTP 200 va `totalElements=100`.

## Thay doi quan trong gan day

### Admin Event Management (2026-05-26)
- `admin/events.html`: rewrite thanh trang quan tri dong, khong con du lieu demo; frontend chi goi JSON API qua `callApi`.
- Correction sau review user: rewrite lai template theo style doc duoc, bo inline handler dai, them event delegation va empty-state phan biet chua co data / khong khop filter.
- `ApiAdminSpeakerAgendaController`: them GET admin cho speakers/agenda de admin xem day du, khong bi paywall public route.
- `AdminEventServiceImpl`: gan event type/location bat buoc, default date range cho admin list, related posts qua `CT_POST_EVENTS`, required roles co `roleId`, cleanup cac bang lien quan truoc khi xoa session/campaign.
- DTO/repository: them validation `eventTypeId`, `locationId`, `ctEventId`, `address`; them repository delete helpers cho agenda/speaker/status/registration/comment link.

### Event Comment System (2026-05-26)
- `events/detail.html`: CSS reaction picker/tree branch/author menu/inline edit/report modal, HTML comment section, JS comment system ~40 ham
- `SecurityConfig.java`: them `/api/comments/events/**` vao permitAll
- Fix 5 loi escape quote JS, 12 chuoi thieu dau tieng Viet
- Event va post chia se cung backend API (`ApiCommentController`), cung DB (`CMT`, `PH_CMT`)

### OOP Refactor (2026-05-24, commit c101490)
- `NguCanhNguoiDung` + `NguCanhNguoiDungFactory` trong `service/support`
- `EditContentRequest` thay ReplyRequest/CommentRequest cho PUT comment endpoints

## Context cho phien sau

- Can dang nhap admin tren browser de smoke test thao tac CRUD thuc te o `/admin/events`.
- De test ban code moi hien tai, dung `http://localhost:8081/admin/events` vi process cu tren 8080 khong dung duoc bang Stop-Process trong sandbox.
- `mvnw.cmd` dang loi wrapper PowerShell; dung `bash mvnw` cho compile/test.
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
