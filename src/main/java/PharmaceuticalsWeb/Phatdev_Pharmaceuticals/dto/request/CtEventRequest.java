//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/CtEventRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request tạo mới / cập nhật buổi sự kiện (CT_EVENTS).
 * eventId là chiến dịch cha chứa buổi này.
 */
@Getter
@Setter
public class CtEventRequest {

    /** Chiến dịch cha (FK → EVENTS) */
    @NotNull(message = "ID chiến dịch không được để trống")
    private Long eventId;

    /** Tên riêng của buổi (VD: Buổi 1: Carbapenem thế hệ mới) */
    @Size(max = 500, message = "Tên buổi tối đa 500 ký tự")
    private String title;

    /** Nội dung mô tả chi tiết buổi (HTML) */
    private String content;

    /** Địa điểm tổ chức (FK → LOCATIONS) */
    @NotNull(message = "Địa điểm tổ chức không được để trống")
    private Integer locationId;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalDateTime endTime;

    @Min(value = 0, message = "Số slot phải >= 0 (0 = không giới hạn)")
    private int totalSlots = 0;

    @Size(max = 200, message = "SEO title tối đa 200 ký tự")
    private String seoTitle;

    @Size(max = 255, message = "SEO description tối đa 255 ký tự")
    private String seoDescription;

    /** Danh sách ID các Nhóm quyền (USER_ROLES) được phép đăng ký/xem buổi này */
    private List<Integer> roleIds;

    /** Danh sách ID tag gán cho buổi */
    private List<Long> tagIds;

    /** Danh sách ID bài viết/tài liệu liên quan gắn qua CT_POST_EVENTS */
    private List<Long> relatedPostIds;
}
