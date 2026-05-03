//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventRegistrationResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dữ liệu đăng ký sự kiện trả về client.
 * Dùng cho admin/events.html modal "Danh sách đăng ký".
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationResponse {

    private Long id;

    private Long ctEventId;
    private LocalDateTime sessionStartTime;

    /** null nếu khách ẩn danh */
    private Long userId;
    private String userName;

    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String workplace;

    private String status;
    private LocalDateTime registeredAt;

    /**
     * Tên chiến dịch sự kiện mà buổi này thuộc về.
     * Chỉ được populate trong ngữ cảnh "Đăng ký của tôi" (sidebar user).
     * Giá trị null khi gọi từ ngữ cảnh admin xem danh sách đăng ký của một buổi cụ thể.
     */
    private String campaignTitle;
}
