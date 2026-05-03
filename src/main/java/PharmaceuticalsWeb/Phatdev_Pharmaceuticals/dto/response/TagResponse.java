//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/TagResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu tag trả về client.
 * usageCount: số bài viết dùng tag này — cho Tag Cloud sidebar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String name;
    private String slug;

    /** Số bài viết đã xuất bản dùng tag này */
    private long usageCount;
}
