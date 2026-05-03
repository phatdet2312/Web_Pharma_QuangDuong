//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ReportModLogResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: LỊCH SỬ KIỂM TOÁN ĐƠN BÁO CÁO
 * =========================================================================
 * Mục đích: Xuất bản dữ liệu từ các Sổ tay ghi án (CT_CMT_REPORT_MOD_LOG, v.v.).
 * Cung cấp bằng chứng minh bạch về sự dịch chuyển trạng thái và trách nhiệm 
 * của Quản trị viên ra quyết định.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportModLogResponse {

    /** Mã định danh độc lập của bản ghi kiểm toán */
    private Long id;

    /** Mã định danh của Đơn báo cáo bị tác động kiểm duyệt */
    private Long reportId;

    /** Mã định danh kỹ thuật của hành vi kiểm duyệt (VD: RESOLVE_REPORT) */
    private String actionCode;

    /** Tên hiển thị của hành vi kiểm duyệt (VD: 'Xử lý báo cáo') */
    private String actionName;

    /** Trạng thái nguyên bản của đơn báo cáo trước khi bị can thiệp */
    private String oldStatus;

    /** Trạng thái mới của đơn báo cáo sau khi hoàn tất can thiệp */
    private String newStatus;

    /** Lý do giải trình cho quyết định thay đổi trạng thái */
    private String reason;

    /** Mã định danh của Quản trị viên thực thi phán quyết */
    private Long moderatorId;

    /** Họ và tên (hoặc Username) của Quản trị viên thực thi phán quyết */
    private String moderatorName;

    /** Dấu vết mạng (IP) của Quản trị viên tại thời điểm xử lý */
    private String ipAddress;

    /** Chữ ký trình duyệt/thiết bị của Quản trị viên tại thời điểm xử lý */
    private String userAgent;

    /** Mốc thời gian hệ thống chính thức ghi nhận lệnh thay đổi trạng thái */
    private LocalDateTime createdAt;
}