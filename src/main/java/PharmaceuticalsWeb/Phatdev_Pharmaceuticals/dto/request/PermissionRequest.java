//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/PermissionRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * =========================================================================
 * DTO: HỨNG DỮ LIỆU TẠO/SỬA QUYỀN HẠT LỰU
 * =========================================================================
 * Khớp với form giao diện khi Admin muốn đẻ ra một nghiệp vụ mới.
 */
@Data
public class PermissionRequest {

    // Mã quyền thao tác (VD: XOA_BINH_LUAN, DUYET_DON_HANG)
    @NotBlank(message = "Mã quyền không được để trống")
    @Size(max = 50, message = "Mã quyền tối đa 50 ký tự")
    private String permissionCode;

    // Diễn giải chi tiết cho Admin dễ hiểu
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    // ID nhóm chức năng (FK tới bảng PERMISSION_MODULES)
    private Integer moduleId;
}