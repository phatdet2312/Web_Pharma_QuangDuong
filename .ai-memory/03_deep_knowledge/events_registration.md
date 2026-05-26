# Events & Registration
> Last updated: 2026-05-26
> Source files: `controller/view/EventViewController.java`, `controller/api/ApiEventController.java`, `controller/api/ApiCommentController.java`, `controller/api/ApiPublicSpeakerAgendaController.java`, `controller/api/ApiAdminEventController.java`, `service/itf/IEventService.java`, `service/impl/EventServiceImpl.java`, `service/impl/SpeakerAgendaServiceImpl.java`, `service/support/EventStatusDisplayPolicy.java`, `service/support/NguCanhNguoiDung.java`, `service/support/NguCanhNguoiDungFactory.java`, `entities/Event.java`, `entities/CtEvent.java`, `entities/CtEventRegistration.java`
> Confidence: HIGH

## Mo ta chuc nang

Module su kien quan ly campaign (`EVENTS`), session cu the (`CT_EVENTS`), dang ky tham du, lich su trang thai, location/type, speaker/agenda va social proof. Public pages goi API `/api/events`; admin CRUD nam duoi `/api/admin/events`.

## Business Rules quan trong

- Status session lay tu ban ghi moi nhat trong `CT_EVENT_STATUS_HISTORY`.
- Trang thai duoc phep dang ky: `OPEN` hoac `UPCOMING`.
- `totalSlots = 0` nghia la khong gioi han; neu > 0 thi check slot da dang ky de chong overbooking.
- Registration la append-friendly: neu ve moi nhat cua user la `CANCELLED` thi cho tao ban ghi moi thay vi reuse ban cu.
- Huy ve phai check `reg.user.id == userId`; khong cho user huy ve nguoi khac.
- Public attendee summary phai mask ten/so dien thoai truoc khi tra frontend.
- Session paywall phai xu ly o backend truoc khi tra DTO. Frontend chi render contract da loc.
- `CtEventResponse.currentStatus = LOCKED` va `displayStatus = Danh rieng cho nhom chuyen mon` la trang thai public cho session active nhung user khong du quyen. Cac trang thai da dong cong khai nhu `CANCELLED`, `FINISHED`, `ENDED` van hien thi that.
- `EventAccessDecision` trong `EventServiceImpl` la diem quyet dinh quyen truy cap event session; endpoint public calendar/list/upcoming/detail phai truyen `userId` vao mapper.
- `EventServiceImpl` toi uu quyen bang cach tinh `userLevel` mot lan tai method public roi truyen xuong mapper private; khong nen goi `userRepository.findById` lap lai cho tung session trong cung request.
- `EventStatusDisplayPolicy` trong `service/support` la policy dich ma trang thai sang nhan public/admin; frontend nen dung `displayStatus` tu API thay vi tu dich ma chuyen mon.
- `taoMoTaMarketing` phai strip `<script>...</script>`, `<style>...</style>`, HTML tag va URL truoc khi tao teaser cho event bi khoa.
- Comments/status-history/attendee-summary cua event session phai kiem tra `coQuyenTruyCapBuoi`; neu thieu quyen tra empty/403 phu hop.
- `POST /api/events/register` phai chan session thieu quyen truoc khi lo trang thai/capacity.

## OOP Refactor - NguCanhNguoiDung (2026-05-24)

Refactor 3 vi pham OOP trong event service:

1. **Tính mù (opacity)**: Service method trước nhận `Long userId` → giờ nhận `NguCanhNguoiDung` (chứa userId + capBacCaoNhat). Service tự tạo object này bằng factory, không còn "xin" data từ bên ngoài.
2. **Ai làm việc nấy**: Factory là @Component, inject `IUserRepository` + `IUserService`, có method `taoNguCanh(Long userId)` → trả object context chứa tất cả thông tin cần, không phải service tự query.
3. **Nâng cấp trần**: Class shared `NguCanhNguoiDung` trong `service/support`, getter methods `layCapBacCaoNhat()`, không phải inner class `private static` trong `EventServiceImpl`.

**Phương thức thay đổi (9 method dùng NguCanhNguoiDung):**
- `layBuoiTrongThang(NguCanhNguoiDung)`
- `timKiemSuKien(NguCanhNguoiDung, ...params)`
- `layChiTietSuKien(NguCanhNguoiDung, ...)`
- `layChiTietBuoi(NguCanhNguoiDung, ...)`
- `layBuoiSapToi(NguCanhNguoiDung, ...)`
- `dangKyThamDu(NguCanhNguoiDung, ...)`
- `layLichSuTrangThaiPublic(NguCanhNguoiDung, ...)`
- `layTomTatKhachMoiPublic(NguCanhNguoiDung, ...)`
- `coQuyenTruyCapBuoi(NguCanhNguoiDung, ctEventId)` — method kiểm tra quyền sử dụng trong controller event/speaker/comment endpoints

**3 method giữ nguyên Long userId:**
- `layDangKyCuaToi(Long userId)` — endpoint `/my-registrations`
- `layVeCuaToiTaiBuoiNay(Long userId)` — check ticket hiện tại
- `huyDangKy(Long userId, Long regId)` — validation user ownership

**Controller refactor:**
- `ApiEventController`: inject `NguCanhNguoiDungFactory`, mỗi endpoint public tạo `NguCanhNguoiDung` một lần rồi truyền xuống service.
- `ApiPublicSpeakerAgendaController`: inject factory, gọi `coQuyenTruyCapBuoi(nguCanh)`.
- `ApiCommentController`: inject factory, sửa 2 lời gọi `coQuyenTruyCapBuoi(nguCanh)` cho comment event session.

## API Endpoints

| Method | Path | Mo ta | Auth |
|--------|------|-------|------|
| GET | `/api/events/stats` | Thong ke event theo filter | No |
| GET | `/api/events/locations` | Danh sach dia diem | No |
| GET | `/api/events/calendar` | Session theo thang | No, auth anh huong paywall |
| GET | `/api/events/types` | Loai su kien | No |
| GET | `/api/events` | Search/filter/page campaign | No, auth anh huong paywall |
| GET | `/api/events/{slug}` | Detail campaign | No, auth anh huong paywall |
| GET | `/api/events/upcoming` | Session sap toi | No, auth anh huong paywall |
| GET | `/api/events/my-registrations` | Ve cua toi | Yes |
| GET | `/api/events/sessions/{ctEventId}` | Detail session | No, auth anh huong paywall |
| GET | `/api/events/sessions/{ctEventId}/my-ticket` | Ve hien tai cua user cho session | Optional |
| POST | `/api/events/register` | Dang ky tham du | Optional user; blocked if session role-gated and user lacks access |
| DELETE | `/api/events/registrations/{id}` | Huy dang ky | Yes |
| GET | `/api/events/sessions/{ctEventId}/comments` | Comment session | No, empty if locked |
| POST | `/api/events/sessions/{ctEventId}/comments` | Gui comment session | Yes, 403 if locked |
| GET | `/api/events/sessions/{ctEventId}/status-history` | Timeline status | No, empty if locked |
| GET | `/api/events/sessions/{ctEventId}/attendees-summary` | Social proof masked attendees | No, empty if locked |

## Admin Events Integration (2026-05-26)

Trang `admin/events.html` khong con la demo tinh; trang nay goi `callApi` toi JSON API admin va chi render DTO, khong doc/cung ma hoa truc tiep cau truc bang SQL.

### Admin endpoint contract dang duoc UI dung

| Method | Path | Mo ta |
|--------|------|-------|
| GET | `/api/admin/events/stats` | Thong ke campaign/session/registration/location |
| GET | `/api/admin/events` | List campaign co filter keyword/type/location/role/start/end va pagination |
| POST/PUT/DELETE | `/api/admin/events` + `/{eventId}` | CRUD campaign `EVENTS` |
| POST/PUT/DELETE | `/api/admin/events/sessions` + `/{ctEventId}` | CRUD session `CT_EVENTS` |
| POST | `/api/admin/events/bulk/status` | Doi status tat ca session thuoc nhieu campaign |
| GET/POST | `/api/admin/events/sessions/{ctEventId}/status-history`, `/api/admin/events/sessions/status` | Xem/ghi `CT_EVENT_STATUS_HISTORY` |
| GET/PATCH | `/api/admin/events/sessions/{ctEventId}/registrations`, `/api/admin/events/registrations/{id}/status` | Xem va cap nhat `CT_EVENT_REGISTRATIONS` |
| GET/POST/PUT/DELETE | `/api/admin/events/sessions/{ctEventId}/speakers`, `/api/admin/events/speakers/{speakerId}` | Quan ly `EVENT_SPEAKERS` |
| GET/POST/PUT/DELETE | `/api/admin/events/sessions/{ctEventId}/agenda`, `/api/admin/events/agenda/{agendaId}` | Quan ly `EVENT_AGENDA` va `CT_AGENDA_SPEAKERS` |
| GET/POST/PUT/DELETE | `/api/admin/events/types`, `/api/admin/events/locations` | Quan ly `EVENT_TYPES`, `LOCATIONS` |
| GET | `/api/admin/posts/tags`, `/api/admin/posts`, `/api/admin/role-management/roles` | Nap tag, related posts, role gates cho session |
| GET | `/api/comments/events/{ctEventId}` | Xem nhanh comment session, dieu huong sang moderation khi can |

### Backend rules moi

- `EventRequest.eventTypeId`, `CtEventRequest.locationId`, `EventStatusRequest.ctEventId`, `LocationRequest.address` duoc validate bat buoc de loi tra ve ro thay vi de DB constraint phat no.
- Admin list phai default `startDate/endDate` thanh range rong `2000-01-01` den `2099-12-31` khi request khong co filter ngay. Khong duoc truyen null vao `timKiemChienDich` vi query co dieu kien `ce.startTime >= :startDate AND <= :endDate`, se lam trang admin render rong khi vua vao.
- `CtEventRequest.relatedPostIds` gan session voi bai viet qua `CT_POST_EVENTS`.
- `CtEventResponse.requiredRoles` co them `roleId` de admin UI edit role gates chinh xac, khong phai map bang ten.
- Xoa campaign di qua tung session va don cac bang phu thuoc truoc khi xoa: comment link, agenda-speaker bridge, agenda, speaker, registrations, status history, post links, tags, roles.
- Admin GET speakers/agenda dung route admin rieng de xem du lich trinh/dien gia, khong dung public route co paywall.
- Status session duoc validate trong tap `DRAFT`, `OPEN`, `UPCOMING`, `ONGOING`, `FULL`, `CANCELLED`, `FINISHED`, `ENDED`; registration status trong tap `PENDING`, `CONFIRMED`, `APPROVED`, `ATTENDED`, `CANCELLED`.

### Admin Events Production Fix (2026-05-26)

- `admin/events.html` da bo hardcode `EVENT_STATUSES`/`REG_STATUSES`; status select va bulk status action lay tu `/api/admin/events/dictionaries/statuses`.
- Media event/speaker dung file picker + upload endpoint admin; backend chi chap nhan URL trong namespace server `/uploads/events/campaigns/` va `/uploads/events/speakers/`.
- Type/location co CRUD that qua `/api/admin/events/types` va `/api/admin/events/locations`, co empty state ro. Khong duoc sua duplicate bang cach dedupe ten/noi dung; hai record khac nhau nhung cung ten la du lieu nghiep vu, cung mot record render nhieu lan moi la bug query/state/render.
- Admin campaign query khong join truc tiep tao duplicate; khi khong loc theo session thi campaign chua co session van hien. Khi loc ngay/location/role thi tat ca dieu kien phai khop tren cung mot `CT_EVENTS` record.
- DTO/entity length da doi theo `FileKhoiTaoCSDL.sql` cho title/slug/thumbnail/type description/location/session SEO/agenda/speaker/status note.
- `AdminEventServiceImpl` validate atomic `roleIds`, `tagIds`, `relatedPostIds`, session time, unchanged `eventId`, FK delete type/location, overbooking khi doi registration status.
- `SpeakerAgendaServiceImpl` validate agenda `endTime > startTime`, speakerIds ton tai va speaker thuoc dung session.
- Public locked session khong tra speaker/agenda/related posts/attendee summary trai quyen; public stats/list dung chung helper parse time va tra 400 voi filter ngay thang sai.
- Verify cuoi: `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, Node parse inline script `admin/events.html`, `git diff --check`.

## Ghi chu

- File `DuLieuMau.sql` (chua sample data event) da bi user xoa khoi repo ngay 2026-05-25. Chi con schema `FileKhoiTaoCSDL.sql`.
- Khi can test event flows, phai tao du lieu truc tiep trong DB hoac dung DatabaseSeeder.

## Decision Log

| Quyet dinh | Phuong an (chon / bo) | Ly do | Ngay ghi | Het han |
|-----------|------------------------|-------|----------|---------|
| Event sample data phai validate nhu backend contract | Chon: tao future sessions, latest status hop le, capacity khong vuot, co `CT_EVENT_SESSION_ROLES` de test locked; Bo: session toan `ENDED` hoac chi public | Audit 2026-05-20 cho thay dataset lon van khong test duoc luong event dang mo/paywall | 2026-05-20 | 2026-08-20 |
