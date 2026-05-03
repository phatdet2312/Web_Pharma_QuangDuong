//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/RoleRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

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
    private String roleName;          // Tên nhóm quyền (VD: ACCOUNTANT)
    private Integer roleLevel;        // Cấp bậc (Số càng nhỏ quyền càng to)
    private String description;       // Mô tả chức vụ
    private List<String> permissions; // Danh sách mã quyền hạt lựu (VD: ["READ_PRICE", "EXPORT_REPORT"])
}