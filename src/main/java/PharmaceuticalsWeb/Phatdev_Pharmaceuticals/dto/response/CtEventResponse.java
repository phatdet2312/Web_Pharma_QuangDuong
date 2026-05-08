//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CtEventResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu buổi sự kiện (session) trả về client.
 * currentStatus: trạng thái hiện tại lấy từ CT_EVENT_STATUS_HISTORY mới nhất.
 * availableSlots: TOTAL_SLOTS - số đăng ký đang có hiệu lực.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CtEventResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventSlug;

    /** Tên riêng của buổi (CT_EVENTS.TITLE) */
    private String title;

    /** Nội dung HTML mô tả chi tiết buổi (CT_EVENTS.CONTENT) */
    private String content;

    private Integer locationId;
    private String locationName;
    private String locationAddress;

    /** true nếu địa điểm là trực tuyến (LOCATIONS.IS_ONLINE) */
    private boolean isOnline;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSlots;

    /** Slot còn trống = totalSlots - registeredCount (0 nếu totalSlots=0 = không giới hạn) */
    private long registeredCount;
    private long availableSlots;

    /** 
     * Trạng thái hiển thị đã qua xử lý logic (VD: "Sắp diễn ra", "Hết chỗ", "Đã hủy")
     */
    private String displayStatus;

    /** 
     * Cờ báo động đỏ: true nếu số lượng slot còn lại đạt ngưỡng tối hạn (dưới 20%)
     */
    private boolean isCritical;

    /** Trạng thái hiện tại từ CT_EVENT_STATUS_HISTORY mới nhất */
    private String currentStatus;

    private String seoTitle;
    private String seoDescription;

    private List<TagResponse> tags;

    /** Bài viết liên quan của buổi sự kiện (CT_POST_EVENTS) */
    private List<PostResponse> relatedPosts;

    /** Danh sách Tên quyền (dành cho việc vẽ Badge nhỏ ở ngoài danh sách) */
    private List<String> allowedRoleNames;

    /** Cờ sinh tử: Quyết định mở khóa hay dựng Paywall */
    private boolean hasFullAccess;

    /** Danh sách Chi tiết quyền yêu cầu (dùng để vẽ khung Paywall bên trong detail) */
    private List<RoleInfo> requiredRoles;

    /** Lớp nội bộ mô tả thông tin quyền (Tái sử dụng cấu trúc của Post) */
    @Getter 
    @Setter 
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class RoleInfo {
        private String roleName;
        private String description;
    }
}
