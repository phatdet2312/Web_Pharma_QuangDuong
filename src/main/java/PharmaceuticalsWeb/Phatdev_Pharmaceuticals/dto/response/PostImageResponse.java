//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostImageResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu ảnh đính kèm bài viết trả về client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponse {

    private Long id;
    private String imageUrl;
    private Integer displayOrder;
}
