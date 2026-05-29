# Active Plan - Kiểm tra chuyên sâu Admin Posts Management
> Last updated: 2026-05-29
> Status: IN_PROGRESS
> Planner persisted before implementation: YES

## Mục tiêu

Kiểm tra chuyên sâu chức năng quản lý post admin (`admin/posts.html`) có đúng yêu cầu hình thành ra nó chưa: 100% coverage user-side, khai thác tối đa 12 bảng DB post-related, đồng bộ UX với admin/events + admin/comments, convention DieuKienCode, edge case, security. Verdict 3 cấp: PASS / SỬA TOÀN DIỆN / ĐẬP BỎ XÂY LẠI.

## So sánh quy mô sơ bộ

| Module | Frontend (dòng) | Backend Service (dòng) | Controller (dòng) |
|--------|-----------------|----------------------|-------------------|
| admin/events | 3321 | 1259 | ~350 |
| admin/comments | 2620 | 1595 | ~200 |
| **admin/posts** | **819** | **608** | **189** |

## Danh sách Task

### Đợt 1 (song song — thu thập evidence)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 1.1 | Coverage audit: user-side 9 API vs admin-side | deep-reviewer | TODO |
| 1.2 | DB exploitation audit: 12 bảng mapping + column gap | deep-reviewer | TODO |
| 1.3 | Backend completeness audit: service/endpoint/DTO gap vs events+comments | deep-reviewer | TODO |

### Đợt 2 (song song, sau Đợt 1)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 2.1 | UX consistency audit: posts vs events vs comments (12 checklist items) | deep-reviewer | TODO |
| 2.2 | DieuKienCode convention audit: JS comment + OOP + naming + pattern cấm | deep-reviewer | TODO |
| 2.3 | XSS + security audit: innerHTML/onclick/src injection + CSRF + backend @Valid | security-auditor | TODO |

### Đợt 3 (song song, sau Đợt 2)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 3.1 | Edge case audit: pagination + bulk + nội dung dài + FK dependency | deep-reviewer | TODO |
| 3.2 | Missing features gap analysis: tổng hợp chức năng thiếu từ Đợt 1+2 | deep-reviewer | TODO |

### Đợt 4 (tuần tự, sau Đợt 3)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 4.1 | Tổng hợp verdict report: CRITICAL/HIGH/MEDIUM/LOW + PASS/SỬA/ĐẬP BỎ | deep-reviewer | TODO |
| 4.2 | Lập roadmap sửa/rebuild (nếu cần) — sau khi có verdict | planner | TODO |

## Phát hiện sơ bộ (trước audit chính thức)

1. Header lộ tên bảng DB: `POSTS, CATEGORIES, TAGS, POST_VIEW_LOGS, POST_FILES`
2. Thumbnail input là text (vi phạm learning #14 — admin events đã chuyển file upload)
3. Quy mô quá nhỏ: 819 dòng vs 3321/2620
4. Access filter hardcode `ROLE_DOCTOR`, `ROLE_PARTNER`, `ROLE_ADMIN` thay vì API dictionary
5. Không có modal detail (events và comments đều có)
6. Không tích hợp PageTransitionManager

## Tiêu chí verdict

| Verdict | Điều kiện |
|---------|-----------|
| PASS | <= 5 findings, tất cả LOW/MEDIUM, không CRITICAL |
| SỬA TOÀN DIỆN | 6-15 findings, có HIGH nhưng khung có thể giữ, DB exploitation >= 50% |
| ĐẬP BỎ XÂY LẠI | > 15 findings, có CRITICAL, DB exploitation < 50%, UX bất đồng bộ nghiêm trọng |

## Decisions

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| Audit trước, implement sau | Audit 7 phương diện trước khi quyết định sửa/rebuild | Tránh lặp sai lầm admin/events (implement rồi mới audit) | 2026-05-29 | 2026-08-29 |
| Verdict 3 cấp | PASS / SỬA / ĐẬP BỎ thay vì PASS/FAIL | Có bản demo rollback (posts-demo.html) nên cần verdict rõ mức độ | 2026-05-29 | 2026-08-29 |

---

# Historical Plan - Kiểm tra chuyên sâu Admin Comments + Fix
> Last updated: 2026-05-29
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu

Kiểm tra chuyên sâu chức năng quản lý comment admin (`admin/comments.html`) có đúng yêu cầu hình thành ra nó chưa: 100% coverage user-side, khai thác tối đa 16 bảng DB, đồng bộ UX, convention DieuKienCode, edge case, security.

## Danh sách Task

### Đợt 1 (song song)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 1.1-1.3 | Coverage audit: user-side vs admin-side | deep-reviewer | DONE — PASS |
| 2.1-2.2 | DB exploitation: 16 bảng mapping + column gap | deep-reviewer | DONE — PASS |
| 5.1 | XSS prevention audit | security-auditor | DONE — FAIL→FIXED |

### Đợt 2 (song song, sau Đợt 1)

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 3.1-3.2 | UX consistency: admin cmt vs events + tree CSS | deep-reviewer | DONE — PASS |
| 4.1-4.3 | Backend completeness: service/endpoint/DTO gap | deep-reviewer | DONE — PASS |
| 5.2-5.4 | Edge case: pagination, bulk, nội dung dài | deep-reviewer | DONE — PASS |

### Đợt 3

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 6.1-6.3 | DieuKienCode convention JS/Backend/tái sử dụng | deep-reviewer | DONE — PASS |

### Đợt 4

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| 7.1 | Tổng hợp báo cáo CRITICAL/HIGH/MEDIUM/LOW | deep-reviewer | DONE |

---

# Historical Plan - Fix LOAI_LIKE Upload + Reply Tree Branch (Đợt 2)
> Last updated: 2026-05-29
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu

Fix 2 lỗi chính còn lại sau đợt fix 1: (A) Modal LOAI_LIKE phải có upload ảnh như admin/events, (B) Reply 3 cấp phải có cây phân nhánh như posts/detail.html.

## Phạm vi

| Tầng | File |
|------|------|
| Backend | Thêm endpoint upload icon + service method |
| Frontend | Redesign modal LOAI_LIKE + Refactor reply DOM sang bubble-wrapper + Tree branch CSS |

## Danh sách Task

### Phase 0: Backend Upload Icon

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R0.1 | Thêm method uploadIconReaction vào ICommentService | implementer | DONE |
| R0.2 | Implement upload trong CommentServiceImpl | implementer | DONE |
| R0.3 | Thêm endpoint POST multipart upload-icon | implementer | DONE |
| R0.4 | Verify SecurityConfig + filters | implementer | DONE |
| R0.5 | Build + test backend | tester | DONE |

### Phase 1: Frontend Modal LOAI_LIKE Redesign

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R1.1 | Thêm CSS media-upload-row + media-preview | implementer | DONE |
| R1.2 | Redesign modal HTML: toggle emoji/upload + file input + preview | implementer | DONE |
| R1.3 | Thêm JS apiUpload + setMediaPreview | implementer | DONE |
| R1.4 | JS toggle emoji/upload handler + update submitLikeType | implementer | DONE |
| R1.5 | Update hiển thị icon table (img vs emoji) | implementer | DONE |

### Phase 2: Frontend Reply Tree Branch

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R2.1 | Thêm CSS tree branch (copy/adapt posts/detail.html) | implementer | DONE |
| R2.2 | Refactor DOM renderReplyItem cấp 2 → ri-bubble-wrapper | implementer | DONE |
| R2.3 | Refactor DOM reply cấp 3+ → reply-thread lồng trong ri-bubble-wrapper | implementer | DONE |
| R2.4 | Refactor container replies cấp 2 → reply-thread + has-replies | implementer | DONE |
| R2.5 | Xóa CSS reply cũ + cập nhật CSS mới | implementer | DONE |

### Phase 3: Nghiệm Thu

| # | Task | Agent | Trạng thái |
|---|------|-------|-----------|
| R3.1 | Review convention + toàn vẹn | reviewer | DONE |
| R3.2 | Build + test | tester | DONE |
| R3.3 | Cập nhật memory | memory-keeper | DONE |

## Decisions

| Quyết định | Phương án chọn | Lý do | Ngày | Hết hạn |
|-----------|---------------|-------|------|---------|
| LOAI_LIKE icon upload ảnh | File input + upload endpoint + preview (SUPERSEDES decision cũ "giữ text input") | User yêu cầu rõ ràng, không được override | 2026-05-29 | 2026-08-29 |
| Reply admin dùng tree branch CSS | Copy/adapt CSS posts/detail.html, DOM ri-bubble-wrapper | Nhất quán user/admin, user yêu cầu tham khảo posts/detail | 2026-05-29 | 2026-08-29 |
| Upload icon vào thư mục riêng | `/uploads/comments/reaction-icons/` | Tách biệt domain comment vs event | 2026-05-29 | 2026-08-29 |
| Giữ cmt-card, chỉ refactor reply bên trong | Không đổi cmt-card header/body | Admin card có checkbox + bulk khác user side | 2026-05-29 | 2026-08-29 |

## Tiêu chí nghiệm thu

1. Modal LOAI_LIKE có toggle emoji/upload, upload thành công hiện preview
2. Table LOAI_LIKE hiện img (URL) hoặc text (emoji) đúng
3. Reply cấp 2 có tree branch (nhánh L + trục dọc)
4. Reply cấp 3+ lồng trong ri-bubble-wrapper, có class reply-level-3
5. Avatar size đúng theo cấp (36px cấp 1, 28px cấp 2-3)
6. Nút mod actions đầy đủ trong ri-bubble
7. Build pass, zero JS error
8. DieuKienCode convention

---

# Historical Plan - Fix 9 Nhóm Lỗi Admin Comments
> Last updated: 2026-05-28
> Status: DONE
> Planner persisted before implementation: YES

## Mục tiêu đã hoàn thành

Fix 9 nhóm lỗi nghiêm trọng trong `admin/comments.html` sau lần rewrite đầu: lộ tên bảng DB, bất đồng bộ UX, nháy trang, tab count sai, chống chế nội dung ẩn, reply cấp 3+ thiếu, nút mod reply thiếu, modal LOAI_LIKE lỗi, kiểm tra dữ liệu.

## Kết quả

- Backend: Thêm `postCmt`, `eventCmt` vào `CommentStatsResponse`, cập nhật query `layThongKeBinhLuan()`.
- Frontend: Fix 9 nhóm lỗi trong `admin/comments.html`: xóa DB term, xóa ẩn nội dung, đổi confirm dialog, tích hợp PageTransitionManager, fix tab count, fix nút mod reply, thêm reply cấp 3+, chỉnh modal LOAI_LIKE, kiểm tra constants.
- Convention: Audit DieuKienCode pass, build + test pass.

---

# Historical Plan - Admin Comments Real Integration
> Last updated: 2026-05-28
> Status: DONE

## Mục tiêu đã hoàn thành

Chuyển `src/main/resources/templates/admin/comments.html` (968 dòng demo HTML tĩnh) thành trang quản trị bình luận hoạt động đầy đủ bằng JSON API. Quản lý tối thiểu 100% chức năng comment user side đang có + khai thác tối đa 16 bảng CSDL comment/moderation/reaction/report.

## Hướng làm chính

Backend bổ sung 1 DTO mới + 2 service method + 2 endpoint GET moderation log.
Frontend rewrite toàn bộ comments.html thay demo data bằng API call thật, gồm 7 phase: Stats + Toolbar + Tabs → List + Paging → Bulk + Mod → Detail Modal → Report Modal → LOAI_LIKE + Actions → Replies lazy-load.
Audit convention DieuKienCode, test build/pass, sync memory.

## Kết quả thực hiện

- Backend: Thêm `CmtModerationLogResponse` DTO, 2 service method `layLichSuKiemDuyetCmt/PhCmt`, 2 endpoint GET moderation-log cho CMT + PH_CMT.
- Frontend: Rewrite comments.html (968→~1500 dòng), stats/toolbar/tabs load API, danh sách render từ search đa chiều + pagination, checkbox + bulk bar + moderation đơn lẻ, modal chi tiết (nội dung, reactions, lịch sử kiểm duyệt/sửa, quick mod), modal report (list/filter/resolve/reject), modal LOAI_LIKE CRUD + MODERATION_ACTIONS read-only, reply lazy-load cấp 2/3 + mod actions.
- Convention DieuKienCode: Comment đầy đủ trước hàm/action, bỏ ternary phức tạp, format rõ ràng.
- Self-eval: `bash mvnw -q -DskipTests compile` pass, `bash mvnw -q test` pass, `git diff --check` pass, console JS zero error.
- Khai thác 16 bảng DB: CMT, PH_CMT, CT_POST_CMT, CT_EVENT_CMT, LOAI_LIKE, CT_LIKECMT, CT_LIKEPHCMT, CT_CMT_REPORTS, CT_PH_CMT_REPORTS, CT_CMT_REPORT_MOD_LOG, CT_PH_CMT_REPORT_MOD_LOG, CT_CMT_ACTION_LOG, CT_PH_CMT_ACTION_LOG, CT_CMT_MODERATION_LOG, CT_PH_CMT_MODERATION_LOG, MODERATION_ACTIONS.

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

## Trạng thái kết thúc

- DONE: Persist plan fix vào `.ai-memory/04_active_plan.md`.
- DONE: Sửa backend/security trước frontend.
- DONE: Sửa frontend admin events theo API contract mới.
- DONE: Self-eval và memory sync sau khi pass.

---

# Historical Plan - Admin Events Supplementary Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before reviewer/deep-reviewer/security-auditor: YES

Audit bổ sung admin events theo correction mới của user, đối chiếu working tree hiện tại với `HEAD`, kiểm tra frontend/backend bằng evidence thực tế thay vì suy đoán.

---

# Historical Plan - Admin Events Deep Audit
> Last updated: 2026-05-26
> Status: DONE - AUDIT_FAIL
> Planner persisted before implementation: YES

Audit chuyên sâu thay đổi admin events hiện tại so với `HEAD` và `DieuKienCode.MD`, xác định phần nào vi phạm yêu cầu ban đầu: OOP, ai làm việc nấy, comment tường minh, tái sử dụng, không dùng pattern bị cấm.

---

# Historical Plan - Admin Events Real Integration
> Status: DONE

## Mục tiêu đã hoàn thành

Chuyển `src/main/resources/templates/admin/events.html` từ demo HTML tĩnh sang trang quản trị sự kiện chạy thật bằng JSON API, quản lý tối thiểu các nghiệp vụ user event list/detail/my registrations đang sử dụng và khai thác thêm các bảng event đã có trong `CSDL/FileKhoiTaoCSDL.sql`.

---
