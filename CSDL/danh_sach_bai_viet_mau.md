# Danh sách 20 bài viết mẫu theo `CSDL\BaiMau.sql`

Tài liệu này phản ánh trạng thái hiện tại của file `CSDL\BaiMau.sql` sau khi đã cập nhật lại dữ liệu ảnh. Các đường dẫn bài viết public dùng domain:

`https://quangduong.olutech.net`

Route public đang dùng trong hệ thống là `/tin-tuc/{slug}`.

---

## Thông tin chung

- Nguồn dữ liệu chính: `CSDL\BaiMau.sql`
- Số bài viết: 20 bài, ID từ `9001` đến `9020`
- Tác giả seed: `AUTHOR_ID = 600`
- Trạng thái xuất bản: `IS_PUBLISHED = 1`
- Bảng ảnh gallery: `POST_IMAGES`, mỗi bài có 2 ảnh
- Ảnh hiện tại dùng URL trực tiếp dạng `https://upload.wikimedia.org/wikipedia/commons/...`
- File `BaiMau.sql` đã có khối dọn dữ liệu cũ của các bài `9001..9020` trước khi insert lại

---

## Danh mục dùng trong bài viết

| ID | Tên danh mục | Slug |
|---:|---|---|
| 100 | Nghiên cứu lâm sàng | `nghien-cuu-lam-sang` |
| 101 | Dược lý học | `duoc-ly-hoc` |
| 102 | Đào tạo y khoa | `dao-tao-y-khoa` |
| 103 | Chăm sóc sức khỏe | `cham-soc-suc-khoe` |
| 104 | Thiết bị y tế | `thiet-bi-y-te` |
| 106 | Hướng dẫn điều trị | `huong-dan-dieu-tri` |
| 107 | Trao đổi chuyên môn | `trao-doi-chuyen-mon` |
| 109 | Y học thường thức | `y-hoc-thuong-thuc` |

## Tags dùng trong bài viết

`#TimMach2026` (100), `#UngThuLamSang` (101), `#KhangSinhDo` (102), `#DuocLamSang` (104), `#NghienCuuMoi` (107), `#FDAOriented` (108), `#DinhDuongYHop` (109), `#TieuDuongTuyp2` (110), `#KiemNghiemThuoc` (114), `#DichTeHoc` (116), `#CongNgheGene` (118), `#DapUngMienDich` (124).

---

## Danh sách bài viết

| ID | Tiêu đề | Đường dẫn trên web | Chuyên mục | Tags | Thumbnail trong `POSTS` | Ảnh gallery trong `POST_IMAGES` | Tóm tắt |
|---:|---|---|---|---|---|---|---|
| 9001 | Tách chiết Acid Nucleic tự động công suất cao với MagNA Pure 96 System | https://quangduong.olutech.net/tin-tuc/tach-chiet-acid-nucleic-tu-dong-cong-suat-cao-voi-magna-pure-96-system | Thiết bị y tế (104) | `#NghienCuuMoi` (107), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/7/7b/Automated_pipetting_system_using_manual_pipettes.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/0/07/Researcher_uses_pipettes.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/7/76/Micropipettes.jpg | Giới thiệu giải pháp tách chiết tự động MagNA Pure 96 từ Roche Diagnostics, giúp tối ưu hóa quy trình sinh học phân tử lâm sàng, bảo đảm hiệu suất cao và ngăn ngừa nhiễm chéo. |
| 9002 | Tầm quan trọng của giám sát đường huyết liên tục và vai trò của que thử Accu-Chek Guide | https://quangduong.olutech.net/tin-tuc/tam-quan-trong-cua-giam-sat-duong-huyet-lien-tuc-va-vai-tro-cua-que-thu-accu-chek-guide | Chăm sóc sức khỏe (103) | `#TieuDuongTuyp2` (110), `#DinhDuongYHop` (109) | https://upload.wikimedia.org/wikipedia/commons/7/7a/Blood_Glucose_Measurement_Meter.svg | 1. https://upload.wikimedia.org/wikipedia/commons/e/e2/Blausen_0299_Diabetes_BloodGlucoseMeter.png<br>2. https://upload.wikimedia.org/wikipedia/commons/7/7a/Blood_Glucose_Measurement_Meter.svg | Hướng dẫn kỹ thuật đo đường huyết chuẩn y khoa tại nhà và ưu thế của hệ thống Accu-Chek Guide trong tự theo dõi đường huyết. |
| 9003 | Phân tích khí máu động mạch tại giường bệnh bằng hệ thống cobas b 123 | https://quangduong.olutech.net/tin-tuc/phan-tich-khi-mau-dong-mach-tai-giuong-benh-bang-he-thong-cobas-b-123 | Thiết bị y tế (104) | `#NghienCuuMoi` (107), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/c/c2/Cobas221A.png | 1. https://upload.wikimedia.org/wikipedia/commons/c/c2/Cobas221A.png<br>2. https://upload.wikimedia.org/wikipedia/commons/1/15/Clinical_lab_equipment.JPG | Đánh giá giải pháp phân tích khí máu tại điểm chăm sóc bằng hệ thống cobas b 123 và quy trình kiểm chuẩn xét nghiệm cấp cứu. |
| 9004 | Chẩn đoán sớm suy tim cấp bằng xét nghiệm miễn dịch định lượng NT-proBNP Elecsys | https://quangduong.olutech.net/tin-tuc/chan-doan-som-suy-tim-cap-bang-xet-nghiem-mien-dich-dinh-luong-nt-probnp-elecsys | Hướng dẫn điều trị (106) | `#TimMach2026` (100), `#DuocLamSang` (104) | https://upload.wikimedia.org/wikipedia/commons/b/b9/Blausen_0451_Heart_Anterior.png | 1. https://upload.wikimedia.org/wikipedia/commons/b/b9/Blausen_0451_Heart_Anterior.png<br>2. https://upload.wikimedia.org/wikipedia/commons/1/18/Coronary_arteries.svg | Phân tích giá trị NT-proBNP trong chẩn đoán và loại trừ suy tim cấp ở người bệnh khó thở. |
| 9005 | Xét nghiệm Troponin T độ nhạy cao (hs-cTnT) trong chẩn đoán nhồi máu cơ tim cấp | https://quangduong.olutech.net/tin-tuc/xet-nghiem-troponin-t-do-nhay-cao-hs-ctnt-trong-chan-doan-nhoi-mau-co-tim-cap | Hướng dẫn điều trị (106) | `#TimMach2026` (100), `#NghienCuuMoi` (107) | https://upload.wikimedia.org/wikipedia/commons/1/18/Coronary_arteries.svg | 1. https://upload.wikimedia.org/wikipedia/commons/1/18/Coronary_arteries.svg<br>2. https://upload.wikimedia.org/wikipedia/commons/b/b2/Histopathology_of_myofiber_waviness_in_myocardial_infarction.jpg | Phân tích phác đồ chẩn đoán nhanh nhồi máu cơ tim cấp 0/1 giờ bằng xét nghiệm Troponin T độ nhạy cao. |
| 9006 | Sàng lọc trước sinh không xâm lấn và vai trò của xét nghiệm PAPP-A, Free HCG beta | https://quangduong.olutech.net/tin-tuc/sang-loc-truoc-sinh-khong-xam-lan-va-vai-tro-cua-xet-nghiem-papp-a-free-hcg-beta | Đào tạo y khoa (102) | `#FDAOriented` (108), `#CongNgheGene` (118) | https://upload.wikimedia.org/wikipedia/commons/e/ee/Fetal_Ultrasound.png | 1. https://upload.wikimedia.org/wikipedia/commons/e/ee/Fetal_Ultrasound.png<br>2. https://upload.wikimedia.org/wikipedia/commons/4/4f/Medical_examination%2C_pregnant_women.jpg | Ý nghĩa Double Test quý 1 thai kỳ và ứng dụng xét nghiệm PAPP-A, Free beta-hCG trong sàng lọc nguy cơ dị tật bẩm sinh. |
| 9007 | Chẩn đoán và theo dõi ung thư gan nguyên bào với bộ đôi AFP và PIVKA-II | https://quangduong.olutech.net/tin-tuc/chan-doan-va-theo-doi-ung-thu-gan-nguyen-bao-voi-bo-doi-afp-va-pivka-ii | Nghiên cứu lâm sàng (100) | `#UngThuLamSang` (101), `#NghienCuuMoi` (107) | https://upload.wikimedia.org/wikipedia/commons/8/8a/Hepatocellular_carcinoma_histopathology_(2)_at_higher_magnification.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/8/8a/Hepatocellular_carcinoma_histopathology_(2)_at_higher_magnification.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/e/e7/Liver_vascular_anatomy.svg | Phân tích vai trò phối hợp AFP và PIVKA-II trong tầm soát, chẩn đoán và theo dõi ung thư biểu mô tế bào gan. |
| 9008 | Ý nghĩa lâm sàng của xét nghiệm kháng thể viêm gan B định lượng (Anti-HBs G2 Elecsys) | https://quangduong.olutech.net/tin-tuc/y-nghia-lam-sang-cua-xet-nghiem-khang-the-viem-gan-b-dinh-luong-anti-hbs-g2-elecsys | Y học thường thức (109) | `#DapUngMienDich` (124), `#DuocLamSang` (104) | https://upload.wikimedia.org/wikipedia/commons/8/8c/HBV_replication.png | 1. https://upload.wikimedia.org/wikipedia/commons/8/8c/HBV_replication.png<br>2. https://upload.wikimedia.org/wikipedia/commons/f/ff/Hepatitis_b.jpg | Hướng dẫn đọc hiểu xét nghiệm Anti-HBs định lượng để đánh giá miễn dịch bảo vệ với virus viêm gan B. |
| 9009 | Xét nghiệm Procalcitonin (PCT Brahms-Roche Elecsys) trong quản lý nhiễm trùng huyết | https://quangduong.olutech.net/tin-tuc/xet-nghiem-procalcitonin-pct-brahms-roche-elecsys-trong-quan-ly-nhiem-trung-huyet | Hướng dẫn điều trị (106) | `#KhangSinhDo` (102), `#DuocLamSang` (104) | https://upload.wikimedia.org/wikipedia/commons/f/fc/Human_Blood_Test.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/7/7a/Antibiotic_disk_diffusion.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/f/fc/Human_Blood_Test.jpg | Giá trị định lượng Procalcitonin trong chẩn đoán nhiễm khuẩn, nhiễm trùng huyết và tối ưu sử dụng kháng sinh. |
| 9010 | Ứng dụng công nghệ điện cực chọn lọc (ISE) trong phân tích điện giải đồ lâm sàng | https://quangduong.olutech.net/tin-tuc/ung-dung-cong-nghe-dien-cuc-chon-loc-ise-trong-phan-tich-dien-giai-do-lam-sang | Thiết bị y tế (104) | `#NghienCuuMoi` (107), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/c/c2/Cobas221A.png | 1. https://upload.wikimedia.org/wikipedia/commons/c/c2/Cobas221A.png<br>2. https://upload.wikimedia.org/wikipedia/commons/3/38/Laboratory-313861.jpg | Giải thích nguyên lý điện cực chọn lọc ion trong phân tích Na+, K+, Cl- và quản lý chất lượng xét nghiệm điện giải đồ. |
| 9011 | Quản lý đái tháo đường thai kỳ: Hướng dẫn và tiêu chuẩn chẩn đoán mới nhất | https://quangduong.olutech.net/tin-tuc/quan-ly-dai-thao-duong-thai-ky-huong-dan-va-tieu-chuan-chan-doan-moi-nhat | Hướng dẫn điều trị (106) | `#TieuDuongTuyp2` (110), `#DinhDuongYHop` (109) | https://upload.wikimedia.org/wikipedia/commons/7/7a/Blood_Glucose_Measurement_Meter.svg | 1. https://upload.wikimedia.org/wikipedia/commons/e/e2/Blausen_0299_Diabetes_BloodGlucoseMeter.png<br>2. https://upload.wikimedia.org/wikipedia/commons/7/7a/Blood_Glucose_Measurement_Meter.svg | Hướng dẫn sản phụ thực hiện OGTT 75g và tự kiểm soát đường huyết trong đái tháo đường thai kỳ. |
| 9012 | Tầm soát và theo dõi ung thư tuyến tiền liệt bằng xét nghiệm Total PSA và Free PSA | https://quangduong.olutech.net/tin-tuc/tam-soat-va-theo-doi-ung-thu-tuyen-tien-liet-bang-xet-nghiem-total-psa-va-free-psa | Nghiên cứu lâm sàng (100) | `#UngThuLamSang` (101), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/d/df/Prostatic_carcinoma_-_Gleason_pattern_4_--_intermed_mag.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/d/df/Prostatic_carcinoma_-_Gleason_pattern_4_--_intermed_mag.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/5/57/Prostate_adenocarcinoma_whole_slide.jpg | Phân tích ý nghĩa lâm sàng của Total PSA, Free PSA và tỷ lệ %fPSA trong tầm soát ung thư tuyến tiền liệt. |
| 9013 | Đánh giá dự trữ buồng trứng ở phụ nữ: Ý nghĩa lâm sàng của xét nghiệm AMH | https://quangduong.olutech.net/tin-tuc/danh-gia-du-tru-buong-trung-o-phu-nu-y-nghia-lam-sang-cua-xet-nghiem-amh | Đào tạo y khoa (102) | `#NghienCuuMoi` (107), `#DapUngMienDich` (124) | https://upload.wikimedia.org/wikipedia/commons/d/df/Oocyte_with_Zona_pellucida_(27771482282).jpg | 1. https://upload.wikimedia.org/wikipedia/commons/d/df/Oocyte_with_Zona_pellucida_(27771482282).jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/f/fc/Human_egg_cell.svg | Vai trò của AMH trong đánh giá dự trữ buồng trứng, dự báo đáp ứng kích thích buồng trứng và hỗ trợ sinh sản. |
| 9014 | Vai trò của Interleukin-6 (IL-6 Elecsys) trong đánh giá phản ứng viêm hệ thống | https://quangduong.olutech.net/tin-tuc/vai-tro-cua-interleukin-6-il-6-elecsys-trong-danh-gia-phan-ung-viem-he-thong | Nghiên cứu lâm sàng (100) | `#DapUngMienDich` (124), `#NghienCuuMoi` (107) | https://upload.wikimedia.org/wikipedia/commons/6/6d/Macrophage.svg | 1. https://upload.wikimedia.org/wikipedia/commons/6/6d/Macrophage.svg<br>2. https://upload.wikimedia.org/wikipedia/commons/2/26/Immune_response2.svg | Vai trò của IL-6 trong đánh giá phản ứng viêm hệ thống, nhiễm trùng nặng và nguy cơ bão cytokine. |
| 9015 | Chẩn đoán nhiễm virus Herpes Simplex bằng xét nghiệm HSV-1 và HSV-2 IgG Elecsys | https://quangduong.olutech.net/tin-tuc/chan-doan-nhiem-virus-herpes-simplex-bang-xet-nghiem-hsv-1-va-hsv-2-igg-elecsys | Trao đổi chuyên môn (107) | `#DichTeHoc` (116), `#DapUngMienDich` (124) | https://upload.wikimedia.org/wikipedia/commons/4/45/Herpes_simplex_virus_TEM_B82-0474_lores.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/4/45/Herpes_simplex_virus_TEM_B82-0474_lores.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/4/45/Herpes_simplex_virus_TEM_B82-0474_lores.jpg | Phân biệt nhiễm HSV-1 và HSV-2 bằng xét nghiệm IgG đặc hiệu, ứng dụng trong tư vấn bệnh truyền nhiễm và thai kỳ. |
| 9016 | Xét nghiệm sàng lọc Giang mai tự động: Giải pháp tối ưu với Syphilis Elecsys | https://quangduong.olutech.net/tin-tuc/xet-nghiem-sang-loc-giang-mai-tu-dong-giai-phap-toi-uu-voi-syphilis-elecsys | Đào tạo y khoa (102) | `#DichTeHoc` (116), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/5/58/Treponema_pallidum_Bacteria_(Syphilis).jpg | 1. https://upload.wikimedia.org/wikipedia/commons/5/58/Treponema_pallidum_Bacteria_(Syphilis).jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/0/0a/Treponema_pallidum_cropped.png | Ứng dụng xét nghiệm miễn dịch tự động trong sàng lọc Treponema pallidum và quản lý nguy cơ lây truyền giang mai. |
| 9017 | Quy trình kiểm chuẩn chất lượng (QC) hóa sinh lâm sàng với PreciControl ClinChem Multi | https://quangduong.olutech.net/tin-tuc/quy-trinh-kiem-chuan-chat-luong-qc-hoa-sinh-lam-sang-voi-precicontrol-clinchem-multi | Trao đổi chuyên môn (107) | `#KiemNghiemThuoc` (114), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/e/e0/Rule_3_-_Control_Charts_for_Nelson_Rules.svg | 1. https://upload.wikimedia.org/wikipedia/commons/e/e0/Rule_3_-_Control_Charts_for_Nelson_Rules.svg<br>2. https://upload.wikimedia.org/wikipedia/commons/1/15/Clinical_lab_equipment.JPG | Hướng dẫn thiết lập biểu đồ kiểm chuẩn, áp dụng quy tắc Westgard/Nelson và kiểm soát chất lượng hóa sinh lâm sàng. |
| 9018 | Ứng dụng xét nghiệm CA 19-9 và CEA trong theo dõi điều trị ung thư đường tiêu hóa | https://quangduong.olutech.net/tin-tuc/ung-dung-xet-nghiem-ca-19-9-va-cea-trong-theo-doi-dieu-tri-ung-thu-duong-tieu-hoa | Nghiên cứu lâm sàng (100) | `#UngThuLamSang` (101), `#NghienCuuMoi` (107) | https://upload.wikimedia.org/wikipedia/commons/9/9c/Colorectal_carcinoma_lymph_node_metastasis_--_high_mag.jpg | 1. https://upload.wikimedia.org/wikipedia/commons/9/9c/Colorectal_carcinoma_lymph_node_metastasis_--_high_mag.jpg<br>2. https://upload.wikimedia.org/wikipedia/commons/d/db/Colon_illustration_lg.jpg | Ứng dụng CA 19-9 và CEA trong theo dõi đáp ứng điều trị, phát hiện tái phát và quản lý ung thư đường tiêu hóa. |
| 9019 | Tối ưu hóa phác đồ điều trị thải ghép với xét nghiệm nồng độ thuốc Tacrolimus Elecsys | https://quangduong.olutech.net/tin-tuc/toi-uu-hoa-phac-do-dieu-tri-thai-ghep-voi-xet-nghiem-nong-do-thuoc-tacrolimus-elecsys | Dược lý học (101) | `#DuocLamSang` (104), `#FDAOriented` (108) | https://upload.wikimedia.org/wikipedia/commons/4/48/201405_kidney.png | 1. https://upload.wikimedia.org/wikipedia/commons/4/48/201405_kidney.png<br>2. https://upload.wikimedia.org/wikipedia/commons/5/52/Nephron_illustration.svg | Giám sát nồng độ Tacrolimus trong máu toàn phần để cân bằng hiệu quả chống thải ghép và nguy cơ độc tính thận. |
| 9020 | Công nghệ sinh học phân tử trong chẩn đoán y khoa hiện đại: Tương lai và Thách thức | https://quangduong.olutech.net/tin-tuc/cong-nghe-sinh-hoc-phan-tu-trong-chuan-doan-y-khoa-hien-dai-tuong-lai-va-thach-thuc | Trao đổi chuyên môn (107) | `#CongNgheGene` (118), `#NghienCuuMoi` (107) | https://upload.wikimedia.org/wikipedia/commons/9/96/Polymerase_chain_reaction.svg | 1. https://upload.wikimedia.org/wikipedia/commons/9/96/Polymerase_chain_reaction.svg<br>2. https://upload.wikimedia.org/wikipedia/commons/6/60/Gel_electrophoresis_2.jpg | Tổng quan vai trò PCR, điện di DNA và công nghệ sinh học phân tử trong chẩn đoán y khoa hiện đại. |

---

## Ghi chú thực thi SQL

File `CSDL\BaiMau.sql` hiện có khối dọn dữ liệu đầu file trước khi insert dữ liệu mới. Khối này không chỉ xóa 4 bảng cơ bản như bản tài liệu cũ, mà còn dọn các dữ liệu phụ có thể phát sinh quanh bài viết mẫu như:

- `POST_IMAGES`, `POST_FILES`, `CT_FILE_DOWNLOADS`
- `POST_VIEW_LOGS`, `CT_LIKEPOST`, `CT_POST_EVENTS`
- `CT_POST_TAGS`, `CT_POST_ROLES`
- `CT_POST_CMT`, `CMT`, `PH_CMT` và các bảng like/report/moderation/action log liên quan đến bình luận

Sau khi dọn dữ liệu cũ, script bật `SET IDENTITY_INSERT [POSTS] ON`, insert lại 20 bài `9001..9020`, rồi insert phân quyền, tag và gallery ảnh.

Kiểm tra nhanh sau khi chạy script:

```sql
SELECT ID, TITLE, SLUG, CATEGORY_ID, AUTHOR_ID, THUMBNAIL_URL
FROM [POSTS]
WHERE ID BETWEEN 9001 AND 9020
ORDER BY ID;

SELECT POST_ID, ROLE_ID
FROM [CT_POST_ROLES]
WHERE POST_ID BETWEEN 9001 AND 9020
ORDER BY POST_ID, ROLE_ID;

SELECT POST_ID, TAG_ID
FROM [CT_POST_TAGS]
WHERE POST_ID BETWEEN 9001 AND 9020
ORDER BY POST_ID, TAG_ID;

SELECT POST_ID, IMAGE_URL, DISPLAY_ORDER
FROM [POST_IMAGES]
WHERE POST_ID BETWEEN 9001 AND 9020
ORDER BY POST_ID, DISPLAY_ORDER;
```
