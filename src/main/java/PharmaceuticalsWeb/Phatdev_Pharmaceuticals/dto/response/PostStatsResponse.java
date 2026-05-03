//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostStatsResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: DTO THỐNG KÊ BÀI VIẾT (POST STATS)
 * =========================================================================
 * Đóng gói các chỉ số tổng quan của thư viện y khoa.
 * Bổ sung chỉ số đo lường Cảm xúc (Reactions) để đánh giá mức độ tương tác.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatsResponse {

    /** Tổng bài viết đã xuất bản (public) */
    private long totalPublished;

    /** Tổng số danh mục đang hoạt động */
    private long totalCategories;

    /** Tổng số tag đang được sử dụng */
    private long totalTags;

    /** Tổng lượt tải tài liệu toàn hệ thống */
    private long totalDownloads;

    /** (Admin) Tổng tất cả bài viết kể cả bản nháp */
    private long totalAll;

    /** (Admin) Số bài nháp */
    private long totalDraft;

    /** (Admin) Số bài có tầng DOCTOR/PARTNER/ADMIN */
    private long totalGated;

    /** Tổng lượng tương tác cảm xúc trực tiếp trên bài viết */
    private long totalReactions;
}