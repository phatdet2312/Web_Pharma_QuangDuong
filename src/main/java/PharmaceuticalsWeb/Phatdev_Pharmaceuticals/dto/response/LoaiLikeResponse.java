//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/LoaiLikeResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu loại reaction trả về client.
 * count: số lần reaction loại này được dùng trên một đối tượng cụ thể.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoaiLikeResponse {

    private Integer id;
    private String code;
    private String name;
    private String iconUrl;

    /** Số lần reaction này được dùng (populated theo ngữ cảnh) */
    private long count;
}
