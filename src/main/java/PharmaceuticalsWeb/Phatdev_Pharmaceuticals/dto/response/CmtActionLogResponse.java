//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CmtActionLogResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: NHẬT KÝ HÀNH VI TỰ THÂN CỦA BÌNH LUẬN (ACTION LOG)
 * =========================================================================
 * Mục đích: Xuất bản dữ liệu từ bảng CT_CMT_ACTION_LOG phục vụ giao diện Quản trị.
 * Đặc tả kiến trúc: Lưu vết mọi sự thay đổi nội dung (Create, Update) do chính 
 * người dùng thực hiện. Cung cấp bằng chứng (Old Payload vs New Payload) để 
 * đối soát khi người dùng cố tình sửa bình luận vi phạm thành bình luận sạch.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmtActionLogResponse {

    /** Mã định danh độc lập của bản ghi nhật ký hành vi */
    private Long id;

    /** Mã định danh của Bình luận gốc chịu sự tác động */
    private Long targetId;

    /** Mã định danh của Người dùng thực hiện hành vi (Tác giả bình luận) */
    private Long userId;

    /** Tên hiển thị của Người dùng tại thời điểm truy xuất */
    private String authorName;

    /** Mã kỹ thuật của hành vi (VD: 'CREATE_CMT', 'UPDATE_CMT', 'DELETE_CMT') */
    private String actionCode;

    /** Tên hiển thị có ý nghĩa của hành vi (VD: 'Tạo mới bình luận', 'Chỉnh sửa bình luận') */
    private String actionName;

    /** * Khối dữ liệu nguyên bản (Snapshot dạng JSON) trước khi bị tác động.
     * Trả về null nếu hành vi là Tạo mới (CREATE). 
     */
    private String oldPayload;

    /** * Khối dữ liệu mới (Snapshot dạng JSON) sau khi hoàn tất tác động.
     * Trả về null nếu hành vi là Xóa (DELETE).
     */
    private String newPayload;

    /** Dấu vết mạng (IP Address) của người dùng tại thời điểm thực thi lệnh, dùng để chặn Spam/Botnet */
    private String ipAddress;

    /** Chữ ký trình duyệt và thiết bị (User-Agent) của người dùng */
    private String userAgent;

    /** Mốc thời gian hệ thống chính thức ghi nhận hành vi thay đổi dữ liệu */
    private LocalDateTime createdAt;
}