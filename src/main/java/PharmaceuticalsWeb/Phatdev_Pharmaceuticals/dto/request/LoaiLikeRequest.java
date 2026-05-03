//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/LoaiLikeRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật Loại Phản ứng (Reaction Type).
 * Dùng cho admin/comments.html — modal quản lý icon cảm xúc.
 */
@Getter
@Setter
public class LoaiLikeRequest {

    /** Mã định danh duy nhất (VD: LIKE, LOVE, INSIGHTFUL) */
    @NotBlank(message = "Mã loại phản ứng không được để trống")
    @Size(max = 50, message = "Mã tối đa 50 ký tự")
    private String code;

    /** Tên hiển thị thân thiện (VD: Thích, Yêu thích, Hữu ích) */
    @NotBlank(message = "Tên loại phản ứng không được để trống")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    /** Đường dẫn icon/emoji đại diện (có thể null nếu dùng emoji text) */
    @Size(max = 500)
    private String iconUrl;
}
