//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/CategoryResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu danh mục trả về client.
 * postCount: số bài viết đã xuất bản trong danh mục này (dùng cho sidebar).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;
    private boolean isActive;

    /** Số bài viết đã xuất bản trong danh mục (populated khi cần) */
    private long postCount;
}
