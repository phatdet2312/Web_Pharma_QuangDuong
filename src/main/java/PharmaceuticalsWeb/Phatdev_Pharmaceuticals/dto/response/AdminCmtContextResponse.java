//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminCmtContextResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =========================================================================
 * DTO BỌC BÌNH LUẬN CÓ NGỮ CẢNH NGUỒN GỐC — CHỈ DÙNG CHO ADMIN
 * =========================================================================
 * Bọc ngoài CmtResponse và bổ sung thông tin nguồn gốc (Bài viết / Buổi sự kiện).
 * Giải pháp tránh Payload Bloat: Không nhúng context trực tiếp vào CmtResponse dùng chung.
 * Giải pháp tránh N+1: Dữ liệu context được Batch-load một lần cho toàn trang,
 * sau đó tra cứu bằng Map tại Service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCmtContextResponse {

    /** Toàn bộ dữ liệu bình luận gốc (tái sử dụng DTO hiện có) */
    private CmtResponse cmtData;

    /** Loại nguồn gốc: "POST" | "EVENT" */
    private String targetType;

    /** ID của bài viết hoặc buổi sự kiện mà bình luận này thuộc về */
    private Long targetId;

    /** Tiêu đề bài viết / tên chiến dịch sự kiện (dùng để hiển thị link nguồn) */
    private String targetTitle;

    /** Đường dẫn tĩnh của bài viết / chiến dịch (dùng để build URL) */
    private String targetSlug;
}
