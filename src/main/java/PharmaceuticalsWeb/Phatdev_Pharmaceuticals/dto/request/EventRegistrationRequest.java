//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventRegistrationRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request đăng ký tham dự buổi sự kiện.
 * Hỗ trợ cả user đăng nhập (điền tối thiểu) lẫn khách ẩn danh (phải điền GUEST_*).
 */
@Getter
@Setter
public class EventRegistrationRequest {

    /** ID buổi sự kiện muốn đăng ký */
    @NotNull(message = "ID buổi sự kiện không được để trống")
    private Long ctEventId;

    /** Thông tin khách ẩn danh — bỏ qua nếu user đã đăng nhập */
    @Size(max = 100)
    private String guestName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100)
    private String guestEmail;

    @Size(max = 20)
    private String guestPhone;

    @Size(max = 200)
    private String workplace;
}
