//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    private Integer eventTypeId;

    @NotBlank(message = "Tiêu đề chiến dịch không được để trống")
    @Size(max = 500)
    private String title;

    @Size(max = 550)
    private String slug;

    private String description;

    private String thumbnailUrl;
}
