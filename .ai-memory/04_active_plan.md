# Active Plan - PageTransitionManager (Hệ thống chống nháy trang)
> Last updated: 2026-05-28
> Status: IN_PROGRESS
> Drift sync: 2026-05-28

## Mục tiêu

Xây dựng `PageTransitionManager` — 1 utility JS chung + 1 file CSS chung — giảm/xóa hiện tượng nháy trang khi load data từ API trên 5 template HTML.

## Phạm vi

| Trang | Hiện trạng | Mục tiêu |
|-------|-----------|---------|
| admin/events.html | Đã có fade-swap + skeleton. Đã fix coupling: 4 chỗ CRUD Type/Location bỏ loadCampaigns, 6 chỗ CRUD Campaign/Session/Status dùng silent refresh (anLang=true). 7/10 | Fade-swap khi load danh sách. Skeleton cho stats. GIỮ logic render hiện có |
| posts/list.html | Reveal animation (IntersectionObserver). 6/10 | THÊM skeleton cho lần load đầu. GIỮ reveal animation |
| posts/detail.html | Skeleton loading có sẵn (chỉ header). Surgical .textContent cho stats. 7/10 | MỞ RỘNG skeleton cho body/sidebar. KHÔNG đổi logic cập nhật số liệu |
| events/list.html | Reveal animation tương tự posts/list. 6/10 | THÊM skeleton cho lần load đầu. GIỮ reveal animation |
| events/detail.html | 61 chỗ innerHTML, không có cơ chế chuyên dụng. 4/10 | Skeleton cho hero/body. Fade-swap khi chuyển session |

## KHÔNG làm

- Partial DOM update cho admin/events.html (rủi ro quá cao, 3300 dòng code)
- Thay đổi logic surgical update (.textContent) ở posts/detail.html cho stats
- Thay đổi cấu trúc API hoặc backend
- Sửa logic nghiệp vụ của bất kỳ trang nào

## Danh sách Task

| # | Task | Agent | File | Trạng thái |
|---|------|-------|------|-----------|
| T1 | Tạo `page-transitions.css` — skeleton + fade CSS, prefix `ptm-` | implementer | TẠO: static/css/page-transitions.css | DONE |
| T2 | Tạo `page-transition-manager.js` — IIFE + prototype, 4 method chính | implementer | TẠO: static/js/page-transition-manager.js | DONE |
| T3 | Tích hợp vào `admin_layout.html` — link CSS + script JS | implementer | SỬA: templates/admin_layout.html | DONE |
| T4 | Tích hợp vào `user_layout.html` — link CSS + script JS | implementer | SỬA: templates/user_layout.html | DONE |
| T5 | admin/events.html — Stats fade-in lần load đầu (giữ surgical setText) | implementer | SỬA: templates/admin/events.html | DONE |
| T6 | admin/events.html — Campaigns skeleton + fade-swap thay nuke-rebuild | implementer | SỬA: templates/admin/events.html | DONE |
| T6b | admin/events.html — Fix nháy: xóa loadCampaigns khỏi Type/Location CRUD, thêm anLang silent refresh cho Campaign/Session/Status CRUD | implementer | SỬA: templates/admin/events.html | DONE |
| T7 | posts/list.html — Skeleton + fade-swap cho featured + article grid | implementer | SỬA: templates/posts/list.html | DONE |
| T8 | posts/detail.html — Skeleton body + swapContent, giữ surgical stats | implementer | SỬA: templates/posts/detail.html | DONE |
| T9 | events/list.html — Skeleton + fade-swap cho campaigns-container | implementer | SỬA: templates/events/list.html | DONE |
| T10 | events/detail.html — Body fade-in khi load chiến dịch | implementer | SỬA: templates/events/detail.html | DONE |
| T11 | Test toàn bộ 5 trang | tester | Tất cả | PENDING — cần chạy trên browser |
| T12 | Cập nhật memory | memory-keeper | .ai-memory/ | TODO |

## Thứ tự thực hiện

```
Phase 0 (NỀN TẢNG):
  T1 (CSS) --> T2 (JS) --> T3 + T4 (layouts, song song)

Phase 1 (ADMIN — tuần tự vì cùng file):
  T5 (stats skeleton) --> T6 (campaigns fade-swap)

Phase 2 (USER PAGES — song song vì khác file):
  T7 (posts/list) | T8 (posts/detail) | T9 (events/list) | T10 (events/detail)

Phase 3 (NGHIỆM THU):
  T11 (test) --> T12 (memory)
```

## Decisions

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| Prefix `ptm-` cho CSS class mới | ptm- prefix | Tránh xung đột với `.skeleton` (posts/detail) và `.reveal` (4 trang) | 2026-05-28 | 2026-08-28 |
| IIFE + prototype cho JS | Không dùng ES6 class/arrow | Nhất quán với code hiện có, tuân thủ DieuKienCode | 2026-05-28 | 2026-08-28 |
| Chỉ fade-swap, KHÔNG partial DOM update cho admin/events | Fade-swap toàn container | 3300 dòng, 25+ hàm render lồng nhau, partial update rủi ro quá cao | 2026-05-28 | 2026-08-28 |
| loadCampaigns silent refresh bằng tham số anLang | Thêm param anLang vào hàm hiện có | DRY — 1 hàm duy nhất, không copy logic fetch. CRUD Type/Location bỏ hẳn loadCampaigns vì không liên quan | 2026-05-28 | 2026-08-28 |
| Load ở LAYOUT không ở từng trang | Layout load 1 lần | Giống pattern callApi/showToast, trang mới tự động có | 2026-05-28 | 2026-08-28 |
| CSS dùng `--transition` từ design-system | Không dùng --t-fast của user_layout | design-system.css load trong CẢ HAI layout | 2026-05-28 | 2026-08-28 |

## Risk

| Risk | Mức độ | Giảm thiểu |
|------|--------|-----------|
| admin/events.html 3300 dòng — sửa sai sẽ break nhiều chức năng | CAO | Chỉ sửa 2 hàm (loadStats, loadCampaigns), wrap BÊN NGOÀI render function |
| CSS class trùng tên | TRUNG BÌNH | Prefix ptm- cho tất cả class mới |
| Làm vỡ reveal animation khi thêm fade | TRUNG BÌNH | Fade-swap gọi kichHoatHieuUngXuatHien() SAU khi content visible |

## Tiêu chí nghiệm thu tổng thể

1. Zero flicker: 5 trang load/filter/navigate không nháy
2. Skeleton visible: mỗi trang hiện skeleton trước khi data API về
3. Fade smooth: transition 200-300ms mượt
4. Zero regression: CRUD, filter, pagination, comment, registration, reveal, surgical stats đều hoạt động
5. Zero JS error: console không có error mới
6. Convention: comment tiếng Việt, không ternary, không arrow function, không Stream
7. Responsive: skeleton + fade hoạt động trên mobile/tablet/desktop

---

# Historical Plan - Admin Events Production Fix
> Last updated: 2026-05-26
> Status: DONE

## Mục tiêu đã hoàn thành

Fix toàn diện các lỗi admin events đã audit: backend integrity/security, public locked-session privacy, media upload flow, frontend enum/API/UX/readability, và duplicate-render root cause. Không sửa bằng dedupe chống chế theo nội dung; chỉ xử lý khi cùng một record ID bị render lặp.

## Hướng làm chính

1. Chốt contract trước khi code:
   - Event status và registration status phải lấy từ API dictionary.
   - Media admin dùng file picker/upload endpoint, không bắt nhập path nội bộ.
   - Locked session public không lộ speaker/agenda/related posts/attendee metadata.
   - Duplicate render phân biệt bằng record ID; hai bản ghi khác nhau cùng nội dung vẫn là dữ liệu hợp lệ.

2. Backend trước:
   - Thêm status dictionary endpoint.
   - Thêm media upload endpoint/contract và validate content type/size/path/URL length.
   - Clamp page/size.
   - Admin list không ẩn campaign chưa có session.
   - Validate `endTime > startTime` cho session và agenda ở create/update.
   - Reject update session nếu `eventId` body khác campaign hiện tại.
   - Validate atomic `roleIds`, `tagIds`, `relatedPostIds`, `speakerIds`.
   - Type/location delete phải check FK và trả lỗi nghiệp vụ.
   - Registration status update không được overbook.

3. Public/security:
   - Invalid `roleIds` không được fail-open thành public session.
   - Locked/gated session không lộ speaker, agenda, related posts, attendee metadata/count trái quyền.

4. Frontend `admin/events.html`:
   - Bỏ hardcode `EVENT_STATUSES` và `REG_STATUSES`, load từ API.
   - Đổi media text input sang file picker/upload flow có preview.
   - Type/location UX có loading/empty/error state rõ.
   - Thêm comment/ghi chú trước hàm/action lớn, bỏ ternary quan trọng nếu làm khó đọc.
   - Điều tra duplicate render theo ID, fix root cause ở query/state/render; không dedupe theo tên/nội dung.

5. Test/eval:
   - Backend tests/compile cho validation atomic, fail-open, locked privacy, overbook, page clamp, no-session campaign visibility, FK dependency delete.
   - Frontend parse/smoke cho dictionary, upload flow, type/location UX, duplicate scenario.
   - Chạy `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, `git diff --check`, `node --check` inline script nếu còn JS trong template.

## Contract đã chốt để implement

- Media upload dùng namespace server hiện có: `/uploads/events/campaigns/` và `/uploads/events/speakers/`.
- Endpoint public của session bị khóa trả `200` với empty/minimal DTO để frontend hiện thông báo nhẹ, không lộ metadata.
- Trạng thái chiếm slot giữ theo contract hiện tại: `PENDING`, `CONFIRMED`, `APPROVED`, `ATTENDED`.
- Update session body có `eventId` khác campaign hiện tại: reject `400`, không cho chuyển campaign âm thầm.

## Trạng thái task

- DONE: Persist plan fix vào `.ai-memory/04_active_plan.md`.
- DONE: Sửa backend/security trước frontend.
- DONE: Sửa frontend admin events theo API contract mới.
- DONE: Self-eval và memory sync sau khi pass.

## Kết quả thực hiện

- Admin events đã bỏ input nhập path ảnh; campaign/speaker dùng file picker upload vào `/uploads/events/campaigns/` và `/uploads/events/speakers/`.
- Status/session registration options load từ API `/api/admin/events/dictionaries/statuses`; bulk status action cũng render từ dictionary này.
- Query admin campaign không còn tạo duplicate từ join và không ẩn campaign chưa có session khi không lọc theo session; khi lọc ngày/location/role thì điều kiện phải khớp trên cùng một session.
- Type/location CRUD có empty state rõ; không dùng dedupe theo nội dung/tên, chỉ render từng record API trả về.
- Backend validate ID atomically, thời gian session/agenda, FK delete, page/size, overbooking khi đổi registration status, URL ảnh upload namespace, DTO/entity length theo `FileKhoiTaoCSDL.sql`.
- Locked public session không trả speaker/agenda/related posts/attendee metadata trái quyền.
- Self-eval pass: `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, inline script parse bằng Node, `git diff --check`.

## Risk / Blocker

- Không sửa song song cùng file `AdminEventServiceImpl` hoặc `src/main/resources/templates/admin/events.html`.
- Không dùng dedupe chống chế; duplicate render phải có evidence root cause.
- Không sửa `ultraSecureLibrary`.
- Tuân thủ `DieuKienCode.MD`: tường minh, comment, OOP/phân lớp, không ternary khó đọc, frontend chỉ render qua JSON API.
- `.codex/hooks*` và memory files cũng đang đổi nhưng ngoài scope audit admin events; chỉ ghi nhận để tránh nhiễu, không revert.

## Decision Note

| Quyết định | Phương án | Lý do | Ngày ghi | Hết hạn |
|-----------|-----------|-------|----------|---------|
| Duplicate render phải fix theo root cause và record ID | Chọn: điều tra query/state/render khi cùng một ID xuất hiện nhiều lần; bỏ: dedupe theo tên/nội dung | Hai bản ghi khác nhau có cùng nội dung là dữ liệu nghiệp vụ hợp lệ; dedupe chống chế có thể làm mất dữ liệu thật | 2026-05-26 | 2026-08-26 |
| Admin event media dùng upload/file picker contract | Chọn: endpoint upload trả URL/path hợp lệ; bỏ: admin nhập thủ công path/URL public | Tránh path nội bộ sai, URL nguy hiểm, lỗi length/schema và UX không thực tế | 2026-05-26 | 2026-08-26 |

---

# Historical Plan - Admin Events Supplementary Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before reviewer/deep-reviewer/security-auditor: YES

## Mục tiêu hiện tại

Audit bổ sung admin events theo correction mới của user, đối chiếu working tree hiện tại với `HEAD`, kiểm tra frontend/backend bằng evidence thực tế thay vì suy đoán.

## Kết quả audit bổ sung

- FAIL frontend convention: `admin/events.html` vẫn thiếu comment trước nhiều hàm/action/render lớn, còn nhiều toán tử 3 ngôi, và template gom CSS + HTML + state + API + render + dispatcher trong một file quá lớn.
- PASS một phần API render: list/stats/dictionaries/campaign/session/history/registration/speaker/agenda/comment preview đều gọi API thật; không thấy fake dataset hoặc success toast độc lập với API.
- FAIL yêu cầu "100% từ API": `EVENT_STATUSES` và `REG_STATUSES` vẫn hardcode ở JS.
- Correction nghiệp vụ duplicate: không được coi hai bản ghi khác nhau nhưng cùng tên là lỗi code; chỉ cùng một record bị render nhiều lần mới là lỗi. Khi thấy lặp phải truy root cause ở API query/state/render, không vá bằng lớp dedupe chống chế.
- FAIL UX type/location: có nút mở modal và có POST/PUT/DELETE API thật, nhưng discoverability/empty/error/loading state kém; danh sách rỗng thành bảng trống.
- FAIL media: campaign thumbnail và speaker avatar vẫn là text input path/URL, chưa có file picker/upload endpoint/domain media contract như pattern upload profile.
- FAIL backend business/integrity: admin list có nguy cơ ẩn campaign chưa có session, DTO length lệch schema, update session/agenda cho lọt equal time, update session ignore `eventId`, delete type/location không check FK dependency.
- FAIL invalid ID atomicity: `roleIds`, `tagIds`, `relatedPostIds`, `speakerIds` sai bị ignore âm thầm, có thể báo success nhưng mất gate/tag/post/speaker link.
- FAIL security/access: invalid `roleIds` có thể làm session fail-open thành public; public speaker/agenda/related posts/attendees summary có nguy cơ lộ metadata session bị khóa quyền.

---

# Historical Plan - Admin Events Deep Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before implementation: YES

## Mục tiêu hiện tại

Audit chuyên sâu thay đổi admin events hiện tại so với `HEAD` và `DieuKienCode.MD`, xác định phần nào vi phạm yêu cầu ban đầu: OOP, ai làm việc nấy, comment tường minh, tái sử dụng, không dùng pattern bị cấm như toán tử 3 ngôi, frontend chỉ dùng JSON API, backend validate input và không phá vỡ luồng public event.

## Phạm vi audit

- `src/main/resources/templates/admin/events.html`
- Backend admin/event files đang đổi trong `controller/api`, `service/impl`, `dto/request`, `dto/response`, `repositories/IRepository`
- Tham chiếu bắt buộc: `ApiAdminEventController`, `IAdminEventService`, `ISpeakerAgendaService`, `SpeakerAgendaServiceImpl`, `.ai-memory/03_deep_knowledge/events_registration.md`, `DieuKienCode.MD`

## Checklist audit

- Đối chiếu `git diff HEAD` từng file event/admin.
- Kiểm tra frontend không hardcode DB/business core, không báo success giả, không còn demo data.
- Kiểm tra no ternary/clever syntax/rườm rà; ưu tiên code tường minh.
- Kiểm tra comment bắt buộc và readability theo `DieuKienCode.MD`.
- Kiểm tra phân lớp Controller -> Service -> Repository -> DTO đúng trách nhiệm.
- Kiểm tra DTO/service validate input client.
- Kiểm tra service giữ OOP, tái sử dụng helper/policy, không lấn module.
- Kiểm tra public event flow không regression: list/detail/session/paywall/registration/comment/speaker/agenda.

## Thứ tự kiểm tra

1. Chốt baseline diff và danh sách file đổi.
2. Lập checklist chi tiết từ `DieuKienCode.MD`.
3. Audit `admin/events.html`.
4. Audit controller/API/DTO/repository.
5. Audit service/domain/OOP.
6. Đối chiếu regression public/admin event contract.
7. Tổng hợp verdict PASS/FAIL với evidence file/dòng và đề xuất sửa.

## Trạng thái audit hiện tại

- DONE: Gọi `planner` phân rã task audit.
- DONE: Persist plan audit vào `.ai-memory/04_active_plan.md` trước khi review sâu.
- DONE: Thu thập diff, luật code và bằng chứng vi phạm convention.
- DONE: Gọi reviewer/deep-reviewer/security-auditor theo phạm vi read-only.
- DONE: Tổng hợp verdict audit: FAIL, chưa nên merge.

## Kết quả audit

- FAIL frontend convention: `admin/events.html` vẫn dùng nhiều toán tử 3 ngôi, thiếu comment trước gần như toàn bộ hàm/action, template ôm CSS + HTML + state + API orchestration + renderer trong một file lớn.
- FAIL backend zero-trust/data integrity: update session/agenda còn để lọt `endTime == startTime`, `roleIds`/`relatedPostIds` sai có thể bị bỏ qua âm thầm, update session ignore `eventId`, page/size chưa bound.
- FAIL security/access-control: `roleIds` sai có thể biến phiên gated thành public, public API còn có nguy cơ lộ related posts/speakers cho session bị khóa quyền, URL ảnh chưa validate scheme/domain.
- PASS cục bộ: trang không còn demo data, có render dữ liệu thật; `node --check`, `git diff --check`, `bash mvnw -q -DskipTests compile` pass.

## Risk / Blocker

- `events.html` đổi rất lớn nên phải review theo section, không chỉ dựa vào render/compile.
- Nếu audit phát hiện vi phạm lớn, hướng sửa có thể là sửa trực tiếp trên working tree hiện tại hoặc rollback một phần rồi làm lại theo thiết kế tách file/helper.

---

# Historical Plan - Admin Events Real Integration
> Status: DONE

## Mục tiêu đã hoàn thành

Chuyển `src/main/resources/templates/admin/events.html` từ demo HTML tĩnh sang trang quản trị sự kiện chạy thật bằng JSON API, quản lý tối thiểu các nghiệp vụ user event list/detail/my registrations đang sử dụng và khai thác thêm các bảng event đã có trong `CSDL/FileKhoiTaoCSDL.sql`.

## Phạm vi đã xử lý

- Trang chính: `src/main/resources/templates/admin/events.html`.
- API/admin: `ApiAdminEventController`, `ApiAdminSpeakerAgendaController`, `IAdminEventService`, `AdminEventServiceImpl`, `ISpeakerAgendaService`.
- API/user không regression: `/api/events/**`, `/api/comments/events/**`, `/api/events/sessions/*/speakers`, `/api/events/sessions/*/agenda`.
- Schema được khai thác đúng qua service/API, không để view biết trực tiếp bảng SQL: `EVENTS`, `CT_EVENTS`, `EVENT_TYPES`, `LOCATIONS`, `EVENT_SPEAKERS`, `EVENT_AGENDA`, `CT_AGENDA_SPEAKERS`, `CT_EVENT_SESSION_ROLES`, `CT_EVENT_TAGS`, `CT_POST_EVENTS`, `CT_EVENT_STATUS_HISTORY`, `CT_EVENT_REGISTRATIONS`, `CT_EVENT_CMT`.

## Bước chính

- DONE: Gọi `planner` phân rã task và persist kế hoạch trước khi implement.
- DONE: Ghi learning cho lỗi quy trình từng bỏ qua persist plan.
- DONE: Audit user/admin event contract, DTO, service và schema event liên quan.
- DONE: Bổ sung backend tối thiểu: admin GET speakers/agenda, validate DTO bắt buộc, roleId trong requiredRoles, relatedPostIds cho session, cleanup dependency khi xóa campaign/session, status validation.
- DONE: Rewrite `admin/events.html` để gọi API thật, render loading/empty/error, form/modal CRUD, filter/pagination, quản lý type/location/session/status/registration/speaker/agenda/tag/role/related posts/comments.
- DONE: Self-eval: `bash mvnw -q -DskipTests compile`, `bash mvnw -q test`, `git diff --check`.
- DONE: Smoke local: dev server nghe `http://localhost:8080`; `/login` trả `HTTP 200`, `/admin/events` redirect `HTTP 302` khi chưa đăng nhập admin.
- DONE: Correction sau phản hồi user: rewrite lại `admin/events.html` để không còn block HTML/JS dài một dòng; fix root cause admin list rỗng do `startDate/endDate` null; verify lại trên port `8081`.
- DONE: Sync memory.

## Kết quả nghiệm thu

- `/admin/events` không còn dữ liệu demo; stats/list/filter/pagination lấy từ `/api/admin/events/**`.
- Admin CRUD được campaign, session, type, location, status, registration status, speaker, agenda; có quản lý tag/role/related posts dựa trên API/DB hiện có.
- Admin nhìn/quản trị được các dữ liệu user đang dùng: list/detail/session/status/registration/agenda/speaker/location/tag/related posts/comment preview.
- Form validate client-side tối thiểu và backend validate DTO; không hiển thị success giả khi API lỗi.
- Không sửa `ultraSecureLibrary`, không hardcode secret, không để frontend phụ thuộc cấu trúc bảng SQL.

## Ghi chú

- Drift ngoài phạm vi đã thấy đầu phiên ở nhóm hook config. Không revert, không trộn vào task event.
- `mvnw.cmd` lỗi wrapper PowerShell (`Cannot index into a null array`); dùng `bash mvnw` để compile/test.
