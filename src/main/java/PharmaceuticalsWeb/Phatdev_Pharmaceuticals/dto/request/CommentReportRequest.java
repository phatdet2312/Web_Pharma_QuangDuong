//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/CommentReportRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: YÊU CẦU BÁO CÁO VI PHẠM NỘI DUNG
 * =========================================================================
 * Mục đích: Hứng dữ liệu đầu vào từ phía Client khi phát sinh tín hiệu báo cáo.
 * Tuân thủ "Chuẩn mù": Không chứa thông tin danh tính hay IP, Backend sẽ tự 
 * động đối soát bảo mật.
 */
@Getter
@Setter
public class CommentReportRequest {

    /** Mã định danh của Bình luận gốc hoặc Phản hồi đang bị tố cáo */
    @NotNull(message = "Hệ thống cần mã định danh nội dung để xử lý báo cáo.")
    private Long targetId;

    /** * Phân loại vùng dữ liệu mục tiêu để Backend định tuyến ghi vào đúng bảng.
     * Quy ước hệ thống: "CMT" hoặc "PH_CMT".
     */
    @NotBlank(message = "Phân loại phân cấp nội dung không được để trống.")
    private String targetType;

    /** Chi tiết lý do vi phạm do người dùng khai báo */
    @NotBlank(message = "Vui lòng cung cấp lý do báo cáo để Quản trị viên xem xét.")
    private String reason;
}