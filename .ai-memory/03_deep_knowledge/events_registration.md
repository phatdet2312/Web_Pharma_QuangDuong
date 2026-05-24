# Events & Registration
> Last updated: 2026-05-24
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

## Audit Notes 2026-05-21

- Sau fix truc tiep, event sample trong `DuLieuMau.sql` PASS validator noi bo.
- `EVENTS=100`, `CT_EVENTS=100`; moi session gan 1 campaign cha rieng va description khong trung exact.
- Future sessions: 20 session sau 2026-05-20, 0 session sau 2026-07-30, max `CT_EVENTS.START_TIME` trong gioi han.
- Latest status distribution: `ENDED=75`, `CANCELLED=5`, `OPEN=13`, `UPCOMING=7`; khong con `COMPLETED`.
- Capacity/registration: over-capacity 0, future `ATTENDED` 0, registration sau start 0, non-event registration timestamp sau 2026-05-20 = 0.

## Decision Log

| Quyet dinh | Phuong an (chon / bo) | Ly do | Ngay ghi | Het han |
|-----------|------------------------|-------|----------|---------|
| Event sample data phai validate nhu backend contract | Chon: tao future sessions, latest status hop le, capacity khong vuot, co `CT_EVENT_SESSION_ROLES` de test locked; Bo: session toan `ENDED` hoac chi public | Audit 2026-05-20 cho thay dataset lon van khong test duoc luong event dang mo/paywall | 2026-05-20 | 2026-08-20 |
