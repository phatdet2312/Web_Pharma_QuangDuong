//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/CommentModerationRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: YÊU CẦU KIỂM DUYỆT BÌNH LUẬN VÀ CHẾ TÀI TÀI KHOẢN
 * =========================================================================
 * Ghi nhận một bản ghi kiểm toán vào CT_CMT_MODERATION_LOG hoặc CT_PH_CMT_MODERATION_LOG.
 * Cung cấp bộ tham số chỉ thị thực thi chế tài đóng băng tài khoản phát tán nội dung vi phạm.
 */
@Getter
@Setter
public class CommentModerationRequest {

    /** ID comment gốc hoặc reply cần kiểm duyệt */
    @NotNull(message = "ID đối tượng không được để trống")
    private Long targetId;

    /**
     * Loại đối tượng: CMT | PH_CMT
     * Xác định ghi vào bảng log nào.
     */
    @NotBlank(message = "Loại đối tượng không được để trống")
    private String targetType;

    /** ID hành động kiểm duyệt (FK → MODERATION_ACTIONS) */
    @NotNull(message = "ID hành động không được để trống")
    private Integer actionId;

    /** Lý do kiểm duyệt chi tiết phục vụ tra cứu kiểm toán */
    private String reason;

    /** Chỉ thị áp dụng hình thức đóng băng đối với tài khoản người viết bình luận */
    private boolean isLockUser = false;

    /** * Thời hạn đóng băng tài khoản (Tính bằng ngày). 
     * Giá trị null hoặc 0 đại diện cho hình thức đóng băng vĩnh viễn. 
     */
    private Integer lockDurationDays;
}