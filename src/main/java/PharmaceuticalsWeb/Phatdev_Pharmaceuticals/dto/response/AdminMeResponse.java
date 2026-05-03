//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminMeResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================================
 * DTO: THÔNG TIN QUẢN TRỊ VIÊN ĐANG ĐĂNG NHẬP
 * =========================================================================
 * Trả về đủ dữ liệu để Frontend vẽ giao diện chống lạm quyền (Anti-privilege-escalation UI):
 * so sánh maxRoleLevel của Admin với roleLevel của mỗi Chức vụ trong danh sách
 * để bôi xám các Checkbox vượt cấp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMeResponse {

    private Long id;

    private String username;

    private String fullName;

    /**
     * Cấp bậc quyền lực mạnh nhất (số NHỎ NHẤT) trong tất cả chức vụ đang mang.
     * Số càng nhỏ = quyền càng to (VD: SUPERADMIN = 0, ADMIN = 1, USER = 99).
     * Frontend dùng: if (role.roleLevel <= adminMaxRoleLevel) → disable checkbox.
     */
    private Integer maxRoleLevel;
}
