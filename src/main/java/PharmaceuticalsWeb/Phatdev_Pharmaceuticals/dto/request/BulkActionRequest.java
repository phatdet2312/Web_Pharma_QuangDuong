//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/BulkActionRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body cho các hành động bulk (xóa nhiều, duyệt nhiều...).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkActionRequest {

    @NotEmpty(message = "Danh sách ID không được rỗng")
    private List<Long> ids;

    /** Tùy chọn — dùng cho bulk moderate: APPROVE | HIDE | WARN */
    private String action;

    /** Tùy chọn — dùng cho bulk publish */
    private Boolean published;
}
