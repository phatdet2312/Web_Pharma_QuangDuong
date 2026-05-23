# Báo cáo kiểm tra `DuLieuMau.sql`
> Ngày cập nhật: 2026-05-22  
> Mốc hiện tại nghiệp vụ do user chốt: `2026-06-10`  
> Event/agenda schedule được phép tới tối đa: `2026-07-30`  
> Kết luận: **ĐẠT validator tĩnh nội bộ**. Chưa chạy lại trong SSMS.

## Kết luận nhanh

| Nhóm | Trạng thái | Bằng chứng |
|---|---|---|
| 300 bài post | Đạt | `POSTS=300`, title/summary/content duplicate exact = `0/0/0`, word min/avg/max = `1409/1567.1/1886` |
| Giữ phong cách HTML post | Đạt | Giữ cấu trúc HTML dài hiện có, bỏ marker `source-trace`/`Ghi chú kiểm chứng nội bộ`, thêm mục căn cứ kỹ thuật, `figure`, `img` cho đủ 300 bài |
| Tài liệu tham chiếu từng post | Đạt | 300/300 post dùng source matrix `S00` + nguồn ngoài như Roche, Accu-Chek, FDA, WHO, Roche eLabDoc |
| Ảnh/file remote | Đạt | URL remote `2043/2043`, local URL = `0`, URL quá `255` ký tự = `0`, 300 thumbnail post khác nhau |
| Event nhiều buổi | Đạt | `EVENTS=100`, `CT_EVENTS=288`, phân phối 1-5 buổi/event: `{1:16, 2:24, 3:28, 4:20, 5:12}` |
| Bảng con event | Đạt | Mỗi buổi có speaker, agenda, agenda-speaker, tag, post-link, status history, registration, event comment |
| Comment/reply | Đạt | `CMT=20088`, `PH_CMT=20000`, duplicate exact = `0/0`, không còn `Mã trao đổi`, `Mã phản hồi`, `CMT-`, `PHCMT-` |
| Timeline vật lý | Đạt | Timestamp hoạt động ngoài schedule sau `2026-06-10` = `0`; event schedule sau `2026-07-30` = `0` |
| Registration | Đạt | `registration_after_now=0`, `registration_after_start=0`, `future_attended=0`, `over_capacity=0` |
| Constraint schema SQL | Đạt | `parse_viol=0`, `null_viol=0`, `len_viol=0`, `missing_notnull_no_default=0` |
| FK tĩnh event | Đạt | Invalid `EVENT_ID`/`CT_EVENT_ID`/agenda-speaker FK = `0` |

## Row count chính

```text
USERS=501
POSTS=300
POST_IMAGES=300
POST_FILES=300
PUBLIC_PROFILES=250
PARTNER_PROFILES=251
EVENTS=100
CT_EVENTS=288
EVENT_SPEAKERS=288
EVENT_AGENDA=865
CT_AGENDA_SPEAKERS=865
CT_EVENT_TAGS=528
CT_POST_EVENTS=432
CT_EVENT_STATUS_HISTORY=835
CT_EVENT_REGISTRATIONS=13320
CMT=20088
PH_CMT=20000
CT_EVENT_CMT=7000
```

## Kiểm tra post

```text
POSTS=300
duplicate_title=0
duplicate_summary=0
duplicate_content_exact=0
word_count min/avg/max=1409/1567.1/1886
source_trace=0
internal_note_Ghi_chu_kiem_chung_noi_bo=0
technical_basis_section=300
figure=300
img_tag=300
replacement_char=0
triple_question=0
```

Ghi chú: bản này không rewrite phá phong cách HTML đang có. Các đoạn chuyên môn dài, bảng, checklist, heading hiện hữu vẫn được giữ; phần bị xử lý là marker nội bộ lặp và mục căn cứ kỹ thuật/tài liệu tham chiếu.

## Kiểm tra URL ảnh/file

```text
remote_url=2043
local_url=0
url_too_long_255=0
post_thumbnail_unique=300
post_image_unique=300
```

Các nhóm đã đổi sang URL remote:

- `POSTS.THUMBNAIL_URL`
- `POST_IMAGES.IMAGE_URL`
- `POST_FILES.FILE_URL`
- `EVENTS.THUMBNAIL_URL`
- `EVENT_SPEAKERS.AVATAR_URL`
- `PUBLIC_PROFILES.AVATAR_URL`
- `PARTNER_PROFILES.AVATAR_URL`
- `PARTNER_PROFILES.LICENSE_DOCUMENT_URL`
- `LOAI_LIKE.ICON_URL`

Ảnh post/event lấy từ Wikimedia Commons với license mở hoặc public-domain/CC, URL được lọc để không vượt giới hạn `VARCHAR(255)`.

## Kiểm tra event nhiều buổi

```text
EVENTS=100
CT_EVENTS=288
distribution_per_event={1:16, 2:24, 3:28, 4:20, 5:12}
event_without_ct=0
bad_distribution_outside_1_5=0
```

Coverage bảng con theo từng `CT_EVENTS`:

```text
EVENT_SPEAKERS: min=1, max=1, zero=0
EVENT_AGENDA: min=2, max=4, zero=0
CT_AGENDA_SPEAKERS: min=1, max=1, zero=0
CT_EVENT_TAGS: min=1, max=3, zero=0
CT_POST_EVENTS: min=1, max=2, zero=0
CT_EVENT_STATUS_HISTORY: min=2, max=3, zero=0
CT_EVENT_REGISTRATIONS: min=18, max=75, zero=0
CT_EVENT_CMT: min=14, max=35, zero=0
```

## Kiểm tra registration runtime

```text
CT_EVENT_REGISTRATIONS=13320
null_guest_name=0
null_guest_email=0
null_guest_phone=0
registration_after_now=0
registration_after_start=0
future_attended_registration=0
over_capacity=0
```

Lưu ý: schema thật yêu cầu `GUEST_NAME`, `GUEST_EMAIL`, `GUEST_PHONE` là `NOT NULL`, kể cả khi `USER_ID` có giá trị. Bản hiện tại đã điền đủ 3 trường này cho toàn bộ vé.

## Kiểm tra comment/reply

```text
CMT=20088
PH_CMT=20000
cmt_duplicate_content_exact=0
ph_cmt_duplicate_content_exact=0
bad_marker_Ma_trao_doi=0
bad_marker_Ma_phan_hoi=0
bad_marker_Noi_dung_nay_tra_loi_truc_tiep=0
bad_marker_CMT_dash=0
bad_marker_PHCMT_dash=0
token_marker=0
replacement_char=0
```

Comment vẫn giữ hướng hỏi/đáp vận hành, nhưng các dòng bị trùng exact được phân biệt bằng ngữ cảnh checklist/ca trực thay vì mã nội bộ.

## Kiểm tra thời gian

```text
business_now=2026-06-10 23:59:59
event_schedule_cap=2026-07-30 23:59:59
before_2023_excluding_birth_date=0
event_schedule_after_2026_07_30=0
non_schedule_action_timestamp_after_2026_06_10=0
registration_after_now=0
registration_after_start=0
future_attended_registration=0
over_capacity=0
```

`USERS.BIRTH_DATE` không tính vào rule timestamp hoạt động vì đó là ngày sinh hồ sơ cá nhân, không phải thời điểm phát sinh hành động trong hệ thống.

## Kiểm tra schema SQL thật

Validator đã đọc `CREATE TABLE` trong `CSDL/FileKhoiTaoCSDL.sql` và đối chiếu toàn bộ `INSERT` trong `CSDL/DuLieuMau.sql`.

```text
insert_tables=51
total_insert_rows=337406
parse_viol=0
null_viol=0
len_viol=0
missing_notnull_no_default=0
```

## Còn cần SSMS xác nhận

Validator hiện tại là validator tĩnh trên script. Cần user chạy lại `CSDL/DuLieuMau.sql` trong SSMS để xác nhận runtime SQL Server không phát sinh lỗi ngoài phân tích tĩnh.
