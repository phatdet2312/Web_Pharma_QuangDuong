//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/LikeRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request thêm / đổi reaction trên comment gốc hoặc reply.
 * Loại đối tượng (CMT / PH_CMT) phân biệt qua URL path.
 */
@Getter
@Setter
public class LikeRequest {

    /** ID comment gốc hoặc reply */
    @NotNull(message = "ID đối tượng không được để trống")
    private Long targetId;

    /**
     * Mã loại reaction: LIKE | LOVE | HAHA | INSIGHTFUL...
     * Phải trùng với LoaiLike.CODE trong DB.
     */
    @NotBlank(message = "Loại reaction không được để trống")
    private String loaiLikeCode;
}
