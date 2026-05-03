//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CommentReportResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: THÔNG TIN CHI TIẾT ĐƠN BÁO CÁO VI PHẠM
 * =========================================================================
 * Mục đích: Đóng gói dữ liệu đối soát toàn diện phục vụ giao diện Quản trị.
 * Đặc tả: Bản DTO đa hình, cung cấp đầy đủ thông tin nội dung bị tố cáo 
 * lẫn thông tin mạng của kẻ phát lệnh để ngăn chặn Botnet/Spam.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReportResponse {

    /** Mã định danh độc lập của đơn báo cáo trong cơ sở dữ liệu */
    private Long id;

    /** Mã định danh của bình luận gốc hoặc phản hồi bị báo cáo */
    private Long targetId;

    /** Phân cấp mục tiêu của nội dung bị báo cáo (Quy ước: CMT hoặc PH_CMT) */
    private String targetType;

    /** Trích xuất nguyên bản nội dung văn bản bị báo cáo để Quản trị viên thẩm định trực tiếp */
    private String targetContent;

    /** Tên hiển thị của tài khoản đã phát ngôn nội dung bị báo cáo */
    private String targetAuthorName;

    /** Mã định danh của tài khoản đã gửi đơn báo cáo */
    private Long reporterId;

    /** Tên hiển thị của tài khoản phát lệnh báo cáo vi phạm */
    private String reporterName;

    /** Địa chỉ thư điện tử của người báo cáo phục vụ công tác liên hệ xác minh */
    private String reporterEmail;

    /** Dấu vết mạng (IP) thực thi lệnh báo cáo để phân tích rủi ro hệ thống và khóa dải IP nếu cần */
    private String reporterIp;

    /** Nội dung diễn giải chi tiết lý do vi phạm do người báo cáo cung cấp */
    private String reason;

    /** * Tiến trình xử lý đơn báo cáo hiện tại.
     * Trạng thái quy ước: PENDING (Chờ xử lý) | RESOLVED (Đã giải quyết) | REJECTED (Bác bỏ) 
     */
    private String status;

    /** Thời điểm hệ thống ghi nhận đơn báo cáo vi phạm */
    private LocalDateTime createdAt;
}