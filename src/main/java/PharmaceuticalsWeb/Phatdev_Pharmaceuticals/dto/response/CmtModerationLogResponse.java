//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CmtModerationLogResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: NHẬT KÝ KIỂM DUYỆT BÌNH LUẬN (MODERATION LOG)
 * =========================================================================
 * Mục đích: Xuất bản dữ liệu từ bảng CT_CMT_MODERATION_LOG và
 * CT_PH_CMT_MODERATION_LOG phục vụ giao diện Quản trị.
 * Đặc tả kiến trúc: Lưu vết mọi hành động kiểm duyệt (APPROVE, HIDE, WARN,
 * DELETE) do Quản trị viên thực hiện lên Bình luận gốc hoặc Phản hồi thứ cấp.
 * Field targetType phân biệt nguồn gốc bản ghi để frontend xử lý đúng context.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmtModerationLogResponse {

    /** Mã định danh độc lập của bản ghi nhật ký kiểm duyệt */
    private Long id;

    /** Mã định danh của đối tượng bị kiểm duyệt (CMT_ID hoặc PH_CMT_ID) */
    private Long targetId;

    /** Loại đối tượng bị kiểm duyệt: "CMT" = Bình luận gốc, "PH_CMT" = Phản hồi thứ cấp */
    private String targetType;

    /** Mã kỹ thuật của hành động kiểm duyệt (VD: APPROVE, HIDE, WARN, DELETE) */
    private String actionCode;

    /** Tên hiển thị có ý nghĩa của hành động kiểm duyệt (VD: "Duyệt", "Ẩn", "Cảnh cáo") */
    private String actionName;

    /** Mã định danh của Quản trị viên thực hiện kiểm duyệt */
    private Long moderatorId;

    /** Tên hiển thị của Quản trị viên tại thời điểm truy xuất */
    private String moderatorName;

    /** Lý do kiểm duyệt do Quản trị viên nhập, có thể null nếu không ghi chú */
    private String reason;

    /** Mốc thời gian hệ thống chính thức ghi nhận hành động kiểm duyệt */
    private LocalDateTime createdAt;
}
