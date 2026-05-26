//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật chiến dịch sự kiện (EVENTS).
 */
@Getter
@Setter
public class EventRequest {

    /** ID loại sự kiện (FK → EVENT_TYPES) */
    @NotNull(message = "Loại sự kiện không được để trống")
    private Integer eventTypeId;

    @NotBlank(message = "Tiêu đề chiến dịch không được để trống")
    @Size(max = 255, message = "Tiêu đề chiến dịch tối đa 255 ký tự")
    private String title;

    @Size(max = 255, message = "Slug chiến dịch tối đa 255 ký tự")
    private String slug;

    private String description;

    @Size(max = 255, message = "Đường dẫn ảnh đại diện tối đa 255 ký tự")
    private String thumbnailUrl;
}
