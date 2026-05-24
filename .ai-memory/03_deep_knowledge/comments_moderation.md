# Comments & Moderation
> Last updated: 2026-05-24
> Source files: `controller/api/ApiCommentController.java`, `controller/api/ApiAdminCommentController.java`, `controller/api/ApiReportController.java`, `controller/api/ApiAdminReportController.java`, `service/itf/ICommentService.java`, `service/impl/CommentServiceImpl.java`, `service/impl/PublicReportServiceImpl.java`, `dto/request/EditContentRequest.java`, comment/report/moderation entities
> Confidence: MEDIUM

## Mô tả chức năng

Module comment xử lý comment/reply cho post và event session, reaction, lịch sử chỉnh sửa/xóa, public report và admin moderation. Bootstrap đã đọc endpoint map và file/module chính, chưa phân tích toàn bộ `CommentServiceImpl` vì file lớn.

## Luồng xử lý chính

1. Client gọi `/api/comments/posts/{postId}` hoặc `/api/comments/events/{eventId}` để lấy comment.
2. User đăng nhập gửi comment/reply/update/delete qua `ApiCommentController`.
3. Reaction cho comment/reply gọi `/api/comments/like/cmt` hoặc `/api/comments/like/reply`.
4. Public report gọi `/api/reports/comments`.
5. Admin xử lý pending/moderate/delete/bulk qua `/api/admin/comments` và `/api/admin/reports`.
6. Service ghi log action/moderation/report để giữ audit trail.

## Business Rules quan trọng

- Public đọc comment được phép, nhưng write/update/delete/reaction cần user authenticated theo pattern controller.
- Input comment/reply/report dùng DTO có `@Valid` như `CommentRequest`, `ReplyRequest`, `LikeRequest`, `CommentReportRequest`.
- Có hai thực thể comment: `Cmt` là comment gốc, `PhCmt` là phản hồi từ cấp 2 trở xuống.
- `PH_CMT` lưu cây bằng `PARENT_PH_ID` và có thể lồng vô hạn tầng, nhưng UI public post chỉ hiển thị 3 tầng: CMT cấp 1, PH_CMT cấp 2, và mọi con cháu sâu hơn được gom về cấp 3 kèm tag người được trả lời.
- API danh sách comment post không nạp kèm toàn bộ `cmt.replies`; chỉ trả `replyCount`, frontend gọi endpoint lazy-load khi người dùng bấm "Xem X câu trả lời".
- Tag người được trả lời là chip có thể xóa trong form reply ở mọi cấp. Khi user giữ chip, frontend gửi `@Tên` như một phần của `content`; khi render chỉ highlight `@Tên` nếu nó thật sự nằm đầu `content`, không được tự ép thêm tag bằng metadata.
- Frontend reply post detail đang dựa vào DOM lồng cấp: `reply-thread-cmt-*` nằm trong output của CMT cấp 1, `reply-thread-phcmt-*` nằm trong output của PH_CMT cấp 2. Không được cộng thêm margin thủ công cho cấp 3 nếu mục tiêu là bắt đầu từ đầu output của cấp 2.
- Action row phải đồng bộ giữa CMT và PH_CMT: reaction/like ở trái, nút phản hồi ở phải bằng class riêng của button (`cia-reply`/`ria-reply`), không đảo thứ tự DOM.
- Khi lazy-load reply trả `totalElements <= 0`, frontend phải clear thread và navigation bằng helper dùng chung; không render lại nút "Xem 0 câu trả lời" hay "Ẩn câu trả lời".
- Có cặp entity/log/report riêng cho comment gốc và reply: `CtCmt*` và `CtPhCmt*`.
- Khi sửa logic moderation/report, cần đọc kỹ `CommentServiceImpl` và các repository liên quan trước khi patch.

## Lifecycle Delete Notes

- `PH_CMT` la cay tu tham chieu qua `PARENT_PH_ID`. Khi xoa vat ly mot reply, service phai xoa theo hau tu: tat ca con chau truoc, node cha sau. Xoa phang theo danh sach se co nguy co vi pham FK khi cha bi xoa truoc con.
- Cac thao tac dem/loc con chau cua `PH_CMT` phai dung ban do `parentId -> childIds` duoc dung lai trong cung request; khong dung map roi lai dung lai map trong tung vong lap.
- Xoa vat ly `PH_CMT` khong dung de quy theo do sau. Phai dung thu tu hau tu iterative: tao map `parentId -> childIds`, gom toan bo nhanh can xoa, xoa con truoc cha. Neu phat hien vong lap du lieu thi nem `AppException` de transaction rollback, khong duoc xoa dang do.
- Truoc khi xoa report cua `CMT`/`PH_CMT`, phai xoa `CT_CMT_REPORT_MOD_LOG`/`CT_PH_CMT_REPORT_MOD_LOG` lien quan. Report mod log tham chieu report, nen xoa report truoc log se gay loi rang buoc.
- Bulk delete comment phai di qua lifecycle `xoaCmtVatLy`, khong duoc xoa truc tiep link/action log/comment vi se bo sot reply, reaction, report, moderation log va cay `PH_CMT`.
- Sau khi tao reply thanh cong tren frontend post detail, phai dong form reply dang mo va refresh dung nhanh lazy-load thay vi reload toan bo comment root.

## API Endpoints

| Method | Path | Mô tả | Auth |
|--------|------|-------|------|
| GET | `/api/comments/reaction-types` | Loại reaction | No |
| GET | `/api/comments/posts/{postId}` | Comment bài viết | No |
| GET | `/api/comments/events/{eventId}` | Comment sự kiện/session | No |
| GET | `/api/comments/{cmtId}/replies` | Phản hồi cấp 2 của comment gốc | No |
| GET | `/api/comments/reply/{phCmtId}/replies` | Phản hồi cấp 3, gom tầng sâu hơn theo neo cấp 2 | No |
| POST | `/api/comments/posts/{postId}` | Gửi comment post | Yes |
| POST | `/api/comments/events/{eventId}` | Gửi comment event | Yes |
| POST | `/api/comments/reply` | Gửi reply | Yes |
| PUT | `/api/comments/{cmtId}` | Sửa comment | Yes |
| DELETE | `/api/comments/{cmtId}` | Xóa comment | Yes |
| PUT | `/api/comments/reply/{phCmtId}` | Sửa reply | Yes |
| DELETE | `/api/comments/reply/{phCmtId}` | Xóa reply | Yes |
| GET | `/api/comments/{cmtId}/history` | Lịch sử comment | Yes |
| GET | `/api/comments/reply/{phCmtId}/history` | Lịch sử reply | Yes |
| POST | `/api/comments/like/cmt` | Reaction comment | Yes |
| POST | `/api/comments/like/reply` | Reaction reply | Yes |
| POST | `/api/reports/comments` | Report comment/reply | Yes |
| GET/POST/PUT/DELETE | `/api/admin/comments/**` | Admin moderation/comment tooling | Admin |
| GET/PATCH | `/api/admin/reports/comments/**` | Admin xử lý report | Admin |

## Decision Log

| Quyết định | Phương án (chọn / bỏ) | Lý do | Ngày ghi | Hết hạn | Dead End |
|-----------|------------------------|-------|----------|---------|----------|
| Chưa có | N/A | Bootstrap chỉ ghi nhận code hiện tại | 2026-05-18 | N/A | N/A |
| Lazy-load reply public post | Chọn: root API chỉ trả `replyCount`, reply cấp 2/cấp 3 có endpoint riêng; Bỏ: trả cả cây `PH_CMT` trong `/api/comments/posts/{postId}` | Giảm payload, tránh frontend biết toàn bộ cây reply, giữ UI tối đa 3 tầng nhưng DB vẫn hỗ trợ vô hạn tầng để mở rộng sau này | 2026-05-23 | 2026-08-23 | Trả full `cmt.replies` làm UI rối và tốn payload |
| Delete lifecycle cho cay reply | Chon: xoa `PH_CMT` theo hau tu va don log/report truoc node chinh; Bo: xoa phang reply theo danh sach hoac bulk delete comment truc tiep | Dam bao khong vi pham FK khi reply co nhieu tang, giu delete flow tap trung va de bao tri khi tang so cap hien thi sau nay | 2026-05-23 | 2026-08-23 | Xoa cha truoc con gay loi he thong ngoai y muon khi `PARENT_PH_ID` con tro ve node cha |

## Bug Fix - EditContentRequest (2026-05-24)

**Root cause:** PUT `/api/comments/reply/{id}` dùng `ReplyRequest` có `@NotNull rootCmtId`, nhưng frontend chỉ gửi `{content}` khi sửa reply → validate fail.

**Fix:**
- Tạo DTO `EditContentRequest` chỉ có `@NotBlank content`.
- Sửa `capNhatCmt` và `capNhatPhCmt` trong `ICommentService` + `CommentServiceImpl`: signature đổi từ `CommentRequest`/`ReplyRequest` → `EditContentRequest`.
- `ApiCommentController`: PUT endpoints (cmt cấp 1 và reply cấp 2+) dùng `EditContentRequest`.
- Controller không thay đổi logic validate, chỉ DTO bỏ field không cần khi chỉnh sửa.

**Phương án bỏ:** Sửa `ReplyRequest` thêm field `rootCmtId` optional → sai design; request creation và update không nên dùng DTO chung khi contract khác nhau.

## Ghi chú

- Module này có blast radius cao do liên quan content public, audit log và moderation. Khi sửa nên chạy targeted test/compile và review endpoint auth.
