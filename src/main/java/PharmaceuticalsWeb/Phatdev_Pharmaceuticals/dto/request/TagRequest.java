//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/TagRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật tag.
 */
@Getter
@Setter
public class TagRequest {

    @NotBlank(message = "Tên tag không được để trống")
    @Size(max = 100)
    private String name;

    @Size(max = 120)
    private String slug;
}
