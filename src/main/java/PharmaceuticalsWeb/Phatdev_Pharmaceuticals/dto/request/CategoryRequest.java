//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/CategoryRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật danh mục bài viết.
 */
@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100)
    private String name;

    @Size(max = 120)
    private String slug;

    @Size(max = 500)
    private String description;

    private boolean isActive = true;
}
