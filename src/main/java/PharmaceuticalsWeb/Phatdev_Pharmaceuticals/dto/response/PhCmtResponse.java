//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PhCmtResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * THỰC THỂ TRUYỀN TẢI: PHẢN HỒI THỨ CẤP (REPLY COMMENT)
 * =========================================================================
 * Nhiệm vụ tổng quan:
 * Đóng gói dữ liệu phản hồi bình luận (reply) trả về client.
 * Duy trì liên kết logic vững chắc với Bình luận mỏ neo (RootCmt) 
 * và hỗ trợ thiết lập sơ đồ cây thông qua Phản hồi cha (ParentPh).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhCmtResponse {

    /** Mã định danh độc lập của Phản hồi thứ cấp */
    private Long id;

    /** Mã định danh của Bình luận gốc (Mỏ neo) mà phản hồi này trực thuộc */
    private Long rootCmtId;

    /** * Mã định danh của Phản hồi nhánh cha (Sử dụng cho cấu trúc lồng cấp).
     * Giá trị null tương đương với việc phản hồi trực tiếp vào comment gốc.
     */
    private Long parentPhId;

    /** Mã định danh của tài khoản tác giả */
    private Long userId;

    /** Họ và tên hiển thị của tài khoản tác giả */
    private String userFullName;

    /** Ký tự đại diện hoặc đường dẫn ảnh hồ sơ tác giả */
    private String userAvatar;

    /** Dấu vết mạng (IP) của thiết bị thực thi lệnh gửi phản hồi */
    private String ipAddress;

    /** Nội dung văn bản chi tiết của phản hồi */
    private String content;

    /** * Trạng thái kiểm duyệt: null | APPROVE | HIDE | WARN 
     * Hành động kiểm duyệt mới nhất có hiệu lực. null = chưa có log.
     */
    private String currentStatus;

    /** Tổng lưu lượng đơn báo cáo vi phạm đang chờ xử lý trên phản hồi này */
    private long reportCount;

    /** Danh sách thống kê reaction tổng hợp theo từng phân loại cảm xúc */
    private List<LoaiLikeResponse> reactions;

    /** Mã cảm xúc cá nhân hóa trên phản hồi thứ cấp của tài khoản đang truy cập */
    private String currentUserReaction;

    /** Cờ phán quyết từ Backend: Người đang xem có phải là tác giả của phản hồi này không? */
    private boolean isAuthor;

    /** Thời điểm khởi tạo phản hồi lần đầu tiên */
    private LocalDateTime createdAt;

    /** Thời điểm nội dung văn bản phản hồi được cập nhật lần cuối */
    private LocalDateTime updatedAt;
}