//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CmtResponse.java

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
 * THỰC THỂ TRUYỀN TẢI: BÌNH LUẬN GỐC (ROOT COMMENT)
 * =========================================================================
 * Nhiệm vụ tổng quan:
 * Đóng gói dữ liệu bình luận gốc trả về client, bao gồm danh sách phản hồi 
 * lồng cấp (replies) và thống kê cảm xúc. Cung cấp đầy đủ thông tin để 
 * Client-side vẽ giao diện mà không cần tự xử lý logic phân quyền hay kiểm duyệt.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmtResponse {

    /** Mã định danh độc lập của Bình luận gốc */
    private Long id;

    /** Mã định danh của tài khoản tác giả */
    private Long userId;

    /** Họ và tên hiển thị của tài khoản tác giả */
    private String userFullName;

    /** Ký tự đại diện hoặc đường dẫn ảnh hồ sơ tác giả */
    private String userAvatar;

    /** Danh xưng hoặc chức vụ chuyên môn hiển thị kèm theo tên tác giả */
    private String userRoleName;

    /** Dấu vết mạng (IP) của thiết bị thực thi lệnh đăng bình luận */
    private String ipAddress;

    /** Nội dung văn bản chi tiết của bình luận */
    private String content;

    /** * Trạng thái kiểm duyệt: null | APPROVE | HIDE | WARN 
     * Phán quyết kiểm duyệt có hiệu lực gần nhất từ Quản trị viên. 
     * Giá trị null đồng nghĩa với việc bình luận chưa có log kiểm duyệt nào.
     */
    private String currentStatus;

    /** Tổng lưu lượng đơn báo cáo vi phạm đang chờ xử lý liên quan đến bình luận này */
    private long reportCount;

    /** Mã cảm xúc cá nhân hóa trên bình luận gốc của tài khoản đang truy cập (VD: LIKE, LOVE) */
    private String currentUserReaction;
    
    /** Danh sách thống kê reaction tổng hợp theo từng phân loại cảm xúc */
    private List<LoaiLikeResponse> reactions;

    /** Danh sách phản hồi thứ cấp trực thuộc bình luận gốc, sắp xếp theo CREATED_AT tăng dần */
    private List<PhCmtResponse> replies;

    /** Cờ phán quyết từ Backend: Người đang xem có phải là tác giả của bình luận này không? */
    private boolean isAuthor;

    /** Thời điểm khởi tạo bình luận lần đầu tiên */
    private LocalDateTime createdAt;

    /** Thời điểm nội dung văn bản được cập nhật lần cuối */
    private LocalDateTime updatedAt;
}