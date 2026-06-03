//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/RoleRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * =========================================================================
 * DTO: HỨNG DỮ LIỆU TẠO/SỬA CHỨC VỤ ĐỘNG
 * =========================================================================
 * Khớp với giao diện Admin khi họ điền Form tạo mới một Role.
 */
@Data
public class RoleRequest {

    @NotBlank(message = "Tên chức vụ không được để trống")
    @Size(max = 50, message = "Tên chức vụ tối đa 50 ký tự")
    private String roleName;          // Tên nhóm quyền (VD: ACCOUNTANT)

    @Min(value = 0, message = "Cấp bậc quyền lực không được âm")
    private Integer roleLevel;        // Cấp bậc (Số càng nhỏ quyền càng to)

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;       // Mô tả chức vụ

    private List<String> permissions; // Danh sách mã quyền thao tác (VD: ["READ_PRICE", "EXPORT_REPORT"])
}
