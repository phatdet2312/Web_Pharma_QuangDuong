# Active Plan - Admin Comments Real Integration
> Last updated: 2026-05-28
> Status: IN_PROGRESS
> Planner persisted before implementation: YES

## Mục tiêu

Chuyển `src/main/resources/templates/admin/comments.html` (968 dòng demo HTML tĩnh) thành trang quản trị bình luận hoạt động đầy đủ bằng JSON API. Quản lý tối thiểu 100% chức năng comment user side đang có + khai thác tối đa 16 bảng CSDL comment/moderation/reaction/report.

## Phạm vi

| Tầng | Phạm vi | File |
|------|---------|------|
| Backend bổ sung | 1 DTO mới + 2 service method + 2 endpoint GET moderation log | `CmtModerationLogResponse.java`, `ICommentService.java`, `CommentServiceImpl.java`, `ApiAdminCommentController.java` |
| Frontend rewrite | Toàn bộ `comments.html` — thay demo data bằng API call thật | `templates/admin/comments.html` |
| KHÔNG sửa | Public comment endpoints, CommentServiceImpl methods cũ, ultraSecureLibrary | — |

## Gap Analysis

### API đã có (sẵn sàng dùng)

| Chức năng | Endpoint | Trạng thái |
|-----------|----------|------------|
| Thống kê tổng quan | `GET /api/admin/comments/stats` | CÓ — CommentStatsResponse |
| Tìm kiếm đa chiều + phân trang | `GET /api/admin/comments` (keyword, status, startDate, endDate, targetId, targetType, page, size) | CÓ — Page AdminCmtContextResponse |
| Comment chờ duyệt | `GET /api/admin/comments/pending` | CÓ |
| Kiểm duyệt đơn lẻ | `POST /api/admin/comments/moderate` (targetId, targetType, actionId, reason) | CÓ |
| Kiểm duyệt hàng loạt | `POST /api/admin/comments/bulk/moderate` | CÓ — BulkActionRequest |
| Xóa vật lý CMT | `DELETE /api/admin/comments/cmt/{cmtId}` | CÓ — cascade reply+reaction |
| Xóa vật lý PH_CMT | `DELETE /api/admin/comments/reply/{phCmtId}` | CÓ |
| Xóa hàng loạt | `DELETE /api/admin/comments/bulk` | CÓ |
| LOAI_LIKE CRUD | `GET/POST/PUT/DELETE /api/admin/comments/reaction-types` | CÓ |
| Danh sách report | `GET /api/admin/reports/comments` (targetType, status) | CÓ |
| Xử lý report | `PATCH /api/admin/reports/comments/resolve` | CÓ |
| Lịch sử xử lý report | `GET /api/admin/reports/comments/{reportId}/history` | CÓ |
| Danh mục moderation actions | `GET /api/admin/audit/moderation-actions` | CÓ |
| Lazy-load replies cấp 2 | `GET /api/comments/{cmtId}/replies` | CÓ (public, reuse) |
| Lazy-load replies cấp 3 | `GET /api/comments/reply/{phCmtId}/replies` | CÓ (public, reuse) |
| Lịch sử sửa CMT | `GET /api/comments/{cmtId}/history` | CÓ (public, authenticated) |
| Lịch sử sửa PH_CMT | `GET /api/comments/reply/{phCmtId}/history` | CÓ (public, authenticated) |
| Reaction types | `GET /api/comments/reaction-types` | CÓ (public) |

### API cần bổ sung

| # | Gap | Mô tả | Repository đã có | Cần làm |
|---|-----|-------|-------------------|---------|
| G1 | Admin xem moderation log cho CMT | Lịch sử kiểm duyệt (CT_CMT_MODERATION_LOG) của 1 comment | `findByCmtIdOrderByCreatedAtDesc` đã có | Thêm endpoint + service method + DTO |
| G2 | Admin xem moderation log cho PH_CMT | Tương tự G1 cho reply | `findByPhCmtIdOrderByCreatedAtDesc` đã có | Thêm endpoint + service method |
| G3 | DTO CmtModerationLogResponse | Chưa có DTO trả moderation log về frontend | Entity có đủ field | Tạo DTO mới |

### KHÔNG cần bổ sung (đã đủ hoặc reuse)

| Chức năng | Lý do |
|-----------|-------|
| Admin xem action log (lịch sử sửa user) | Reuse public `/api/comments/{cmtId}/history` — trả CmtActionLogResponse đầy đủ |
| Admin xem replies | Reuse public lazy-load endpoint — READ-ONLY |
| Filter REPORTED | `AdminCmtContextResponse.cmtData.reportCount > 0` — filter client-side đủ nhanh (10-20 items/page) |
| MODERATION_ACTIONS view | Đã có `GET /api/admin/audit/moderation-actions` |

## KHÔNG làm

- Settings modal backend (DB không có bảng COMMENT_SETTINGS — ngoài scope)
- Sửa CommentServiceImpl methods cũ
- Sửa public comment endpoints
- Sửa ultraSecureLibrary
- Tạo admin endpoint riêng cho replies/history (reuse public)

## Danh sách Task

### Phase 0: Backend Bổ Sung (tuần tự — cùng service file)

| # | Task | Agent | File | Trạng thái |
|---|------|-------|------|-----------|
| T1 | Tạo DTO `CmtModerationLogResponse` (id, targetId, actionCode, actionName, moderatorName, reason, createdAt) | implementer | TẠO: `dto/response/CmtModerationLogResponse.java` | TODO |
| T2 | Thêm 2 service method `layLichSuKiemDuyetCmt(Long cmtId)` + `layLichSuKiemDuyetPhCmt(Long phCmtId)` | implementer | SỬA: `service/itf/ICommentService.java`, `service/impl/CommentServiceImpl.java` | TODO |
| T3 | Thêm 2 endpoint: `GET /api/admin/comments/cmt/{cmtId}/moderation-log` + `GET /api/admin/comments/reply/{phCmtId}/moderation-log` | implementer | SỬA: `controller/api/ApiAdminCommentController.java` | TODO |
| T4 | Build + test backend | tester | — | TODO |

### Phase 1: Frontend Rewrite (tuần tự — cùng file comments.html)

| # | Task | Agent | Scope | Trạng thái |
|---|------|-------|-------|-----------|
| T5 | Stats row load từ API + Toolbar search/filter hoạt động + Tab bar switch params | implementer | Stats + Toolbar + Tabs | TODO |
| T6 | Danh sách comment render từ API tìm kiếm đa chiều + Pagination thật | implementer | List + Paging | TODO |
| T7 | Checkbox chọn + Bulk bar + Moderation đơn lẻ (approve/hide/unhide/warn/delete) gọi API | implementer | Bulk + Mod actions | TODO |
| T8 | Modal chi tiết: nội dung, author, target link, reactions, tab lịch sử kiểm duyệt + lịch sử sửa, quick mod | implementer | Detail Modal | TODO |
| T9 | Modal Report: list reports, filter, resolve/reject, history | implementer | Report Modal | TODO |
| T10 | Modal LOAI_LIKE CRUD + MODERATION_ACTIONS read-only list | implementer | LOAI_LIKE + Actions Modal | TODO |
| T11 | Reply toggle + lazy-load replies cấp 2/3 trong comment card + mod actions trên reply | implementer | Replies lazy-load | TODO |

### Phase 2: Nghiệm Thu

| # | Task | Agent | File | Trạng thái |
|---|------|-------|------|-----------|
| T12 | Audit convention DieuKienCode (comment, no ternary, no lambda, full code, tính mù) | reviewer | Tất cả files mới/sửa | TODO |
| T13 | Build + test toàn bộ (`mvnw compile`, `mvnw test`, `git diff --check`, console JS) | tester | — | TODO |
| T14 | Cập nhật memory (plan DONE, deep knowledge, evolution log, project map) | memory-keeper | `.ai-memory/` | TODO |

## Thứ tự thực hiện

```
Phase 0 (BACKEND — tuần tự vì cùng service file):
  T1 (DTO) --> T2 (Service) --> T3 (Controller) --> T4 (Build+Test)

Phase 1 (FRONTEND — tuần tự vì cùng file comments.html):
  T5 (Stats+Toolbar+Tabs) --> T6 (List+Paging) --> T7 (Bulk+Mod)
  --> T8 (Detail Modal) --> T9 (Report Modal) --> T10 (LOAI_LIKE+Actions)
  --> T11 (Replies lazy-load)

Phase 2 (NGHIỆM THU — tuần tự):
  T12 (Convention audit) --> T13 (Full test) --> T14 (Memory)
```

**KHÔNG song song:** Phase 1 tuần tự vì cùng sửa 1 file `comments.html`.

## Decisions

| Quyết định | Phương án chọn | Phương án bỏ | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------------|-------|------|---------|
| Reuse public endpoint cho admin xem replies | Gọi `/api/comments/{cmtId}/replies` (public READ-ONLY) | Tạo endpoint admin riêng | Public endpoint đã trả đủ data, tạo riêng là duplicate code | 2026-05-28 | 2026-08-28 |
| Reuse public endpoint cho admin xem edit history | Gọi `/api/comments/{cmtId}/history` (authenticated) | Tạo endpoint admin riêng | CmtActionLogResponse đã có old/new payload, IP, user-agent | 2026-05-28 | 2026-08-28 |
| Filter REPORTED ở frontend | `cmtData.reportCount > 0` client-side | Thêm status=REPORTED vào backend JPQL | reportCount đã có trong response, 10-20 items/page đủ nhanh | 2026-05-28 | 2026-08-28 |
| 1 DTO moderation log dùng chung CMT + PH_CMT | `CmtModerationLogResponse` có targetId | Tạo 2 DTO riêng | Hai entity cùng structure, khác FK — gom vào 1 field targetId | 2026-05-28 | 2026-08-28 |
| Settings modal disabled (chưa có backend) | Giữ layout, disabled/ẩn, ghi chú "Chưa có API" | Implement backend cho settings | DB không có bảng COMMENT_SETTINGS — ngoài scope | 2026-05-28 | 2026-08-28 |
| Giữ CSS hiện có | Giữ CSS demo (prefix cm-/cmt-/mod-), chỉ thay data + JS | Rewrite CSS từ đầu | CSS đã đẹp và nhất quán, rewrite là công vô ích | 2026-05-28 | 2026-08-28 |
| Frontend chia 7 phần tuần tự | Mỗi task có tiêu chí riêng, compile check | Rewrite 1 lần toàn bộ | 968 dòng, chia nhỏ dễ kiểm soát và review | 2026-05-28 | 2026-08-28 |

## Risk

| Risk | Mức độ | File | Blast radius | Giảm thiểu |
|------|--------|------|-------------|------------|
| comments.html rewrite lớn (968→~1500-2000 dòng) | CAO | `templates/admin/comments.html` | Chỉ trang admin comments | Chia 7 phần, mỗi phần compile check. Giữ CSS cũ |
| Sửa CommentServiceImpl (1400+ dòng) | THẤP | `service/impl/CommentServiceImpl.java` | Admin + Public comment | Chỉ THÊM 2 method readonly, KHÔNG sửa method cũ |
| Sửa ApiAdminCommentController | THẤP | `controller/api/ApiAdminCommentController.java` | Admin comment API | Chỉ THÊM 2 endpoint GET, không sửa endpoint cũ |
| CSS xung đột | THẤP | `templates/admin/comments.html` | Trang admin comments | CSS scoped trong style tag, prefix cm-/cmt-/mod- |

## 16 bảng DB khai thác

CMT, PH_CMT, CT_POST_CMT, CT_EVENT_CMT, LOAI_LIKE, CT_LIKECMT, CT_LIKEPHCMT, CT_CMT_REPORTS, CT_PH_CMT_REPORTS, CT_CMT_REPORT_MOD_LOG, CT_PH_CMT_REPORT_MOD_LOG, CT_CMT_ACTION_LOG, CT_PH_CMT_ACTION_LOG, CT_CMT_MODERATION_LOG, CT_PH_CMT_MODERATION_LOG, MODERATION_ACTIONS

## Tiêu chí nghiệm thu tổng thể

1. **Zero hardcoded data**: Không còn số liệu, tên, nội dung demo nào trong HTML
2. **100% user feature coverage**: Đọc/tạo/sửa/xóa/reaction/report/lịch sử — admin đều quản lý được
3. **16 bảng DB khai thác**: Tất cả hiển thị hoặc sử dụng qua API
4. **Convention DieuKienCode**: Comment đầy đủ, không ternary phức tạp, không lambda, format rõ ràng
5. **Tính mù**: Frontend chỉ gọi API + render JSON, không biết DB structure
6. **Zero trust**: Mọi moderation action gửi backend validate, frontend không tự quyết
7. **Build pass**: `bash mvnw -q -DskipTests compile` + `bash mvnw -q test` + `git diff --check`
8. **Zero JS error**: Console không có error mới
9. **Toast trung thực**: Success chỉ khi API thành công, error khi API lỗi
10. **Responsive**: Giữ nguyên responsive CSS hiện có

---

# Historical Plan - PageTransitionManager (Hệ thống chống nháy trang)
> Last updated: 2026-05-28
> Status: DONE (T1-T10), PENDING browser test (T11), TODO memory (T12)

Xây dựng `PageTransitionManager` — 1 utility JS chung + 1 file CSS chung — giảm/xóa hiện tượng nháy trang khi load data từ API trên 5 template HTML. T1-T10 hoàn thành, T11 cần browser test, T12 cần memory sync.

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
