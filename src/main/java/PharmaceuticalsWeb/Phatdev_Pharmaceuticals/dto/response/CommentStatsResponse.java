//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CommentStatsResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thống kê tổng quan trang quản lý bình luận.
 * Dùng cho Hero Stats trên admin/comments.html.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentStatsResponse {

    /** Tổng comment gốc */
    private long totalCmt;

    /** Tổng phản hồi */
    private long totalPhCmt;

    /** Comment gốc chưa có log kiểm duyệt nào */
    private long pendingCmt;

    /** Comment gốc đang bị ẩn (hành động mới nhất = HIDE) */
    private long hiddenCmt;

    /** Phản hồi đang bị ẩn */
    private long hiddenPhCmt;

    /** Tổng reaction toàn hệ thống */
    private long totalReactions;

    /** Số bình luận gốc đang có ít nhất một báo cáo chờ xử lý (PENDING) */
    private long reportedCmt;
}
