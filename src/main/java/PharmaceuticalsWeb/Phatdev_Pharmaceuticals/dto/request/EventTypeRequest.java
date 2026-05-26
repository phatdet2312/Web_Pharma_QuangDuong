//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventTypeRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật loại sự kiện.
 */
@Getter
@Setter
public class EventTypeRequest {

    @NotBlank(message = "Tên loại sự kiện không được để trống")
    @Size(max = 100)
    private String name;

    @Size(max = 255, message = "Mô tả loại sự kiện tối đa 255 ký tự")
    private String description;
}
