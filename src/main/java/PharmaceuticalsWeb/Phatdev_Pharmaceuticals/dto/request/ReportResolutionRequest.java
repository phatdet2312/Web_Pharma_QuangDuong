//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/ReportResolutionRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: YÊU CẦU PHÁN QUYẾT KIỂM DUYỆT BÁO CÁO
 * =========================================================================
 * Mục đích: Hứng quyết định thực thi của Quản trị viên lên một Đơn báo cáo.
 * Thiết kế tái sử dụng: Có thể tích hợp cho nhiều phân hệ báo cáo khác nhau 
 * (Bình luận, Bài viết, Tài khoản) thông qua biến targetType.
 */
@Getter
@Setter
public class ReportResolutionRequest {

    /** Mã định danh của Đơn báo cáo cần đóng hồ sơ */
    @NotNull(message = "Mã đơn báo cáo không được để trống.")
    private Long reportId;

    /** Xác định phân hệ dữ liệu của báo cáo (VD: CMT, PH_CMT, POST, USER) */
    @NotBlank(message = "Loại phân cấp mục tiêu không được để trống.")
    private String targetType;

    /** * Mã hành vi kiểm duyệt chốt hạ hồ sơ. 
     * Khớp với danh mục MODERATION_ACTIONS (VD: RESOLVE_REPORT, REJECT_REPORT) 
     */
    @NotBlank(message = "Mã hành động kiểm duyệt không được để trống.")
    private String actionCode;

    /** Diễn giải pháp lý nội bộ cho quyết định phê duyệt hoặc bác bỏ báo cáo */
    @NotBlank(message = "Bắt buộc cung cấp lý do thực thi phán quyết.")
    private String reason;
}