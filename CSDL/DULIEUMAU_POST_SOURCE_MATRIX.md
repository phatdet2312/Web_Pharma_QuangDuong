# Source Matrix for 300 posts in `DuLieuMau.sql`
> Created: 2026-05-21
> Scope: fixes the missing source-mapping issue only. This file does not certify that current SQL content is free of copy-template duplication.

## Source Policy

- User allowed Codex to trust discovered official/reasonable sources for this dataset task.
- Priority sources: manufacturer documentation, Roche/Accu-Chek pages, FDA/WHO, and the internal product Excel supplied by user.
- Every post is mapped to `S00` plus at least one external trusted source bundle.
- Status `SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED` means the source basis exists, but the current SQL content must still pass duplicate/context validators.

## Source Bundles

| ID | Source | URL/Path | Usage scope |
|---|---|---|---|
| S00 | Internal product Excel | CSDL/*.xlsx product expiry file | Material, Description, Shortest Expiry Date, Batch Number supplied by user. |
| S01 | Roche Life Science - MagNA Pure 96 System Fluid | https://lifescience.roche.com/global/en/products/others/magna-pure-96-system-fluid-external-382425.html | System fluid, wash buffer, reagent head/needle rinse, barcode/use-after-open. |
| S02 | Roche Diagnostics Vietnam - cobas b 123 POC system | https://diagnostics.roche.com/vn/vi/products/instruments/cobas-b-123-1-ins-730.html | Blood gas, electrolytes, metabolites, AutoQC, throughput/configuration, parameter menu. |
| S03 | FDA 510(k) K111188 - cobas b 123/AutoQC/Combitrol Plus B | https://www.accessdata.fda.gov/cdrh_docs/pdf11/K111188.pdf | Intended use, analyte list, AutoQC/AutoCVC/Combitrol levels, control material. |
| S04 | Accu-Chek Guide Test Strips support | https://www.accu-chek.com/products/strips/guide/support | Compatibility, handling, expired-strip warning, package insert/manual references. |
| S05 | Roche Diagnostics - cobas c pack/method-sheet product pages | https://diagnostics.roche.com/global/en/products/lab/co2-l-cps-000073.html | Example cobas c pack green product page and Method Sheet elements for clinical chemistry reagents. |
| S06 | WHO - Levey-Jennings control charts and Westgard rules | https://cdn.who.int/media/docs/default-source/immunization/vpd_surveillance/lab_networks/measles_rubella/manual/annex-12.3-levey-jennings-control-charts-and-westgard-rules.pdf | Mean/SD, Levey-Jennings, Westgard, low-normal-high QC controls. |
| S07 | WHO - Laboratory Quality Management System handbook | https://www.who.int/publications-detail-redirect/9789241548274 | Laboratory QMS, QC, and operational quality practice. |
| S08 | Roche eLabDoc/product documentation repository | https://elabdoc-prod.roche.com/ | IFU/COA/product documentation repository for product-specific verification. |

## Mapping Stats

| Bundle | Mapped posts |
|---|---:|
| S00 | 300 |
| S01 | 2 |
| S02 | 14 |
| S03 | 14 |
| S04 | 3 |
| S05 | 281 |
| S06 | 214 |
| S07 | 0 |
| S08 | 286 |

## 300-Post Mapping

| Post ID | Title | Source bundles | Status |
|---:|---|---|---|
| 100 | Tối Ưu Hóa Quy Trình Tách Chiết Axit Nucleic Tự Động Với Hệ Thống MagNA Pure 96 System Fluid (External) | S00, S01, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 101 | Hướng Dẫn Lâm Sàng và Đánh Giá Chuyên Môn Hệ Thống Đo Đường Huyết ACCU-CHEK GUIDE TEST STRIP 25CT APAC | S00, S04, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 102 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với MEASURING CELL WITH REF. ELECT. V7.0 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 103 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với COMBITROL PLUS B, LEVEL 1 (30 PCS) | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 104 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với COMBITROL PLUS B LEVEL 2 | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 105 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với COMBITROL PLUS B LEVEL 3 | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 106 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CLEANING SOLUTION 988-4 (125 ML) | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 107 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với cobas b 123 AUTOQC PACK, TRI-LEVEL | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 108 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với S3 FLUID PACK A (1 PC) | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 109 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với AssayTip | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 110 | Tối Ưu Hóa Quy Trình Tách Chiết Axit Nucleic Tự Động Với Hệ Thống MagNA Pure Filter Tips (1000 ul) | S00, S01, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 111 | Hướng Dẫn Lâm Sàng và Đánh Giá Chuyên Môn Hệ Thống Đo Đường Huyết Accu-Chek Active 25 Tests | S00, S04, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 112 | Hướng Dẫn Lâm Sàng và Đánh Giá Chuyên Môn Hệ Thống Đo Đường Huyết ACCU-CHEK INSTANT 50CT STRIP APAC | S00, S04, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 113 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MICRO ELECTRODE CL | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 114 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MICRO ELECTRODE CA++ | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 115 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MICRO ELECTRODE PH AVL | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 116 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với AVL NA+ ELECTRODE | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 117 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MICRO ELECTRODE NA+ | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 118 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với AVL CL- ELECTRODE | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 119 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MICRO ELECTRODE K+ | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 120 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với cobas b 123 SENSOR CART. BG/ISE/GLU/LAC | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 121 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với cobas b 123 SENSOR CART. BG/ISE | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 122 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với cobas b 123 FLUID PACK COOX 200 | S00, S02, S03, S06 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 123 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SNAPPAK, 9180 9181 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 124 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Tip CORE TIPS with Filter,1ml | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 125 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cfas PAC 3x1ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 126 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl ClinChem Multi 2, 20x5ml | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 127 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cfas 12x3ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 128 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Anti-HBs G2 Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 129 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PRECIMAT FRUC 3x1ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 130 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PRECIPATH FRUC | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 131 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Total PSA G2 CS Elecsys V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 132 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl Universal Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 133 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CELL CONDITIONING SOLUTION, CC1, 2L | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 134 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Folate G3 CS Elecsys V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 135 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Diluent MultiAssay Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 136 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Testosterone G2 CS G2 Elecsys V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 137 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HSV-2 IgG Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 138 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HSV-1 IgG Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 139 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CA 19-9 Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 140 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với AFP Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 141 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HCG+beta Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 142 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LH Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 143 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IL 6 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 144 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 19-9 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 145 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ISD Sample PT Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 146 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl TM Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 147 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ISE Diluent Gen.2, cobas c, Hitachi | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 148 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với FSH Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 149 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CK-MB STAT CS Elecsys V4 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 150 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Diluent Universal 2 2x36ml Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 151 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cfas Proteins | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 152 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Roche CARDIAC POC Trop T for new SW Ver. | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 153 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cfas CK.MB 3x1ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 154 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CRPHS 250Tests  cobas c 701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 155 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Vitamin B12 G2 CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 156 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với ACTH CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 157 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl HBA1c Path | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 158 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với T3 Elecsys cobas e 200 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 159 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với T3 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 160 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với proBNP G2 Elecsys cobas e 100 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 161 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Ferritin Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 162 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Prolactin G2 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 163 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với NaOH-D, cobas c | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 164 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Troponin T hs Elecsys E2G 300 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 165 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl a-HEV IgG Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 166 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PIVKAII Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 167 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ASTL,  500Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 168 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SCC CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 169 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-HCV PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 170 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CK-MB CS Elecsys V4 E2G | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 171 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với ISE Reference Electrolyte 300ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 172 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với proGRP Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 173 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Tacrolimus Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 174 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Tacrolimus CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 175 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SHBG CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 176 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HCG+beta CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 177 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HBsAg G2 PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 178 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Sample Cleaner 2, cobas 8000 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 179 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Roche CARDIAC Control proBNP (cobas) | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 180 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với C.f.a.s. PUC | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 181 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Precinorm PUC | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 182 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Precipath PUC | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 183 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với SAP Test Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 184 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CellCheck Gen2 Elecsys E2G | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 185 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Estradiol G3 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 186 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-CCP PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 187 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PAPP-A Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 188 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PAPP-A Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 189 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 15-3 G2 Elecsys E2G 100 V1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 190 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IL 6 Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 191 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ISE Int.Stand. Gen.2,  cobas c, Hitachi | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 192 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HDLC4, 350T, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 193 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl Cardiac G2 Elecsys V4 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 194 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FSH Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 195 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Free HCGbeta Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 196 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Progesterone G3 Elecsys E2G 100 V1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 197 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cfas Cystatin C | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 198 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Insulin Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 199 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Insulin Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 200 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Folate G3 Elecsys cobas e 100 V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 201 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HbA1c TQ Gen.3, 200Tests,cobas c111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 202 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với N-MID Osteocalcin Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 203 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Ferritin Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 204 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 15-3 G2 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 205 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FSH Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 206 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với UIBC, 100T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 207 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HIV PC G2 Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 208 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LDLC3, 600T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 209 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với NH3/ETH/CO2 Control A | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 210 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SAP Gen2 Elecsys E2G | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 211 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CMV IgM Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 212 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 72-4 Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 213 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CMV IgG Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 214 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IgE G2 Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 215 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CC 25mM cobas t 50ml | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 216 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-TSHR Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 217 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HBsAg G2 quant G2 Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 218 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ALB2, 300T, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 219 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-TG CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 220 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PCT Brahms-Roche Elecsys cobas e100 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 221 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-HAV G2 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 222 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Anti-HAV G2 Elecsys,cobas e100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 223 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Total PSA Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 224 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Syphilis Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 225 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Syphilis Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 226 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FT3 G3 CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 227 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl Varia Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 228 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SHBG Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 229 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với DHEA-S Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 230 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với DHEA-S Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 231 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với C-Peptide Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 232 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl Thyro AB Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 233 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với BIL-T Gen.3, 400Tests cobas c 111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 234 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PREA, 200T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 235 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với proBNP G2 CS Elecsys E2G V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 236 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với proBNP G2 CS Elecsys V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 237 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CEA Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 238 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 125 G2 CS G2 Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 239 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với C-Peptide Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 240 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-HAV IgM Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 241 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Anti-HAV IgM Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 242 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Troponin T hs Elecsys E2G 100 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 243 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CK-MB STAT Elecsys cobas e 100 V4 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 244 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CK-MB Elecsys cobas e 100 V4 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 245 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PLGF CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 246 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TSH Elecsys cobas e 200 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 247 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Prolactin G2 Elecsys cobas e 100 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 248 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TRIGL,  250Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 249 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Prolactin G2 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 250 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với TG G2 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 251 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với RF-II 500Tests cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 252 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với RFII, 100Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 253 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 19-9 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 254 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HDLC4, 700T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 255 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CREAJ2, 2500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 256 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CREAJ Gen.2, 1500Test, cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 257 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với T4 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 258 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cyfra 21-1 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 259 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với TSH Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 260 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TP Gen.2, 300Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 261 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HBsAg Auto Confirm PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 262 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Syphilis PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 263 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-CCP Elecsys E2G 100 V1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 264 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với T4 CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 265 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CMV IgM PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 266 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với C-Peptide CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 267 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HDL-C Gen.4, 500Tests cobas c701/702 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 268 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với hCG STAT Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 269 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ProCell Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 270 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IgE CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 271 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IgE G2 Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 272 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Testosterone G2 Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 273 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PreciControl ISD Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 274 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FT3 G3 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 275 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với AFP G2 CS Elecsys V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 276 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với AMH Plus PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 277 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HCV Duo PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 278 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ASLOT, 200T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 279 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với sFLT1 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 280 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với BIL-T Gen.3, 250Tests cobas c,Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 281 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CRP N Control 5x0,5ML | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 282 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với IRON2, 700T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 283 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Homocysteine Calibrator Kit 2x3ml | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 284 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Homocysteine Control Kit | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 285 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HSV PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 286 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với APOAT, 100Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 287 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với GLUC HK Gen.3, 2200Tests, cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 288 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Combur 10 UX, 100T, EN ES PT | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 289 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TG G2 Elecsys cobas e 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 290 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với sFLT1 Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 291 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với APOBT, 100Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 292 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với START, 1000T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 293 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với BILD2, 1000T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 294 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với BIL-D Gen.2, 100Tests, cobas c 111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 295 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với TG G2 CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 296 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Diluent MultiAssay E2G | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 297 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Cortisol G2 Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 298 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Owren B cobas t 50ml | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 299 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Vitamin D total G3 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 300 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với T3 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 301 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HBQ Sample Set Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 302 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với MTX Calibrator | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 303 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với NACl 9% Dil, cobas c 701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 304 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Troponin T hs Elecsys cobas e 200 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 305 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LDHI Gen.2 acc.IFCC, 750T, cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 306 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Calcium Gen.2, 2250Tests, cobas c 701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 307 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với IRON Gen.2, 750Tests, cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 308 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với INSTC, 130T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 309 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TPUC3, 150T, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 310 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với INSTC,  65 Tests, cobas c | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 311 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với C3c, 100Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 312 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với NACl 9% SI Gen.2, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 313 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ECO-D,  cobas c 311 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 314 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với FT4 G4 Elecsys cobas e 200 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 315 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Tacrolimus Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 316 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HbA1c TQ haemolyzing rgt, cobas c | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 317 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với GLUC HK Gen.3, 800Tests, cobas c, Int. | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 318 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với SCC Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 319 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PreClean M Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 320 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CRP4, 250T, cobas c  311/501/502,Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 321 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ALTL,  500Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 322 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HSV-2 IgG Elecsys E2G 100 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 323 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Cell Check Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 324 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Total P1NP CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 325 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-HAV IgM PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 326 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PHOS Gen.2, 250Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 327 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Rubella IgM Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 328 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ECO-D, cobas c 501/502 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 329 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Rubella IgM Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 330 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Haemolyse reagent 800T cobas c 111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 331 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TRSF2, 500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 332 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LIPC, 200Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 333 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Estradiol G3 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 334 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CA 125 G2 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 335 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FT4 G4 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 336 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với UA plus 400T cobas c111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 337 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Vitamin D total G3 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 338 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Diluent Universal E2G | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 339 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với BIL-D Gen.2, 350Tests cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 340 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Toxo IgM PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 341 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Toxo IgM Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 342 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với DHEA-S CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 343 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Estradiol G3 Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 344 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ALT 1100Tests cobas c 701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 345 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với HBeAg PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 346 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CleanCell Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 347 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Testosterone G2 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 348 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với TSH CS Elecsys V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 349 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với TSH CS for Instrument Check | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 350 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với SMS, cobas c | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 351 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ALP2, 200T, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 352 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CA, Gen.2, 300Tests, cobas c,Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 353 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với PRECISET RF | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 354 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Anti-CCP Elecsys cobas e 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 355 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Sys Wash Elecsys,cobas e | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 356 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Progesterone G3 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 357 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với UA Gen.2, 1000Tests, cobas c701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 358 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CRPHS, 600T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 359 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cell Wash Solution I/NaOH-D 2x1,8 L | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 360 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với PHOS2, 750T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 361 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với proBNP G2 Elecsys E2G 300 V2.1 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 362 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với LH G2 CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 363 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CHE Gen.2, 200Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 364 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CREP Gen.2, 250Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 365 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với cobas Integra Check Sample | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 366 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với IGF-1 Elecsys E2G 100 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 367 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CRP4, 200T, cobas c 111 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 368 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CRP4, 500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 369 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với HDLC4, 700T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 370 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LACT2, 100T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 371 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với IRON Gen.2, 200Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 372 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với A1CX3, 200T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 373 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với VENTANA HE 600 Wash | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 374 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với FT3 G3 Elecsys E2G 300 V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 375 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Ferritin CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 376 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Total PSA Elecsys cobas e 100 V3 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 377 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với GLUC3, 3300T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 378 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Sample Cleaner 1, cobas c | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 379 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cortisol G2 CS Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 380 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CMV IgM Elecsys E2G 300 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 381 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với GGT-2, 400T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 382 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với Anti-TG Elecsys cobas e 100 V5 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 383 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với AMYL2, 750T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 384 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với AMYL Gen.2, 300Tests, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 385 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CA2, 1500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 386 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CHOL HiCo Gen.2, 400Tests, cobas c, Int. | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 387 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với LACT2, 100T, cobas c, Integra | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 388 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Cystatin C Control Set Gen.2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 389 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với LCS | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 390 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với ALTP2, 800T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 391 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với Anti-HBe PC Elecsys | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 392 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CK, 500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 393 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với BLUING REAGENT | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 394 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với TP2, 1050T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 395 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với SI2, 14500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 396 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với NaOH-D, cobas c 701 | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 397 | Quy Trình Kiểm Chuẩn Chất Lượng Xét Nghiệm Tiêu Chuẩn Quốc Tế Với CEA CS Elecsys V2 | S00, S05, S06, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 398 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với CREAJ2, 2500T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |
| 399 | Ứng Dụng Thiết Bị Phân Tích Khí Máu và Điện Giải Cận Lâm Sàng Với GGT-2, 400T, cobas c pack green | S00, S05, S08 | SOURCE_MAPPED_TRUSTED_BUT_CONTENT_REWRITE_REQUIRED |

## Rebuild Usage

1. For each post, read `S00` for the product Material/Description/Expiry/Batch context.
2. Open the mapped external source bundles to fact-check technical claims.
3. Rewrite each post with its own outline and paragraphs; do not reuse paragraph/heading templates across posts.
4. Re-run validators: unique titles, zero repeated long paragraphs, context-matched comments/replies, and `TEST.MD` timeline compliance.
