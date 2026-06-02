//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/MyPermissionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * =========================================================================
 * DTO: ĐÓNG GÓI DANH SÁCH QUYỀN CỦA NGƯỜI DÙNG HIỆN TẠI
 * =========================================================================
 * Frontend dùng để ẩn/hiện UI theo quyền (chỉ là UX — backend vẫn enforce).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPermissionResponse {

    // Danh sách tên chức vụ (VD: ["BIEN_TAP_VIEN", "KIEM_DUYET_VIEN"])
    private List<String> roles;

    // Danh sách mã quyền hạt lựu (VD: ["POST_CREATE", "POST_EDIT", "COMMENT_VIEW"])
    private List<String> permissions;

    // Cấp bậc quyền lực cao nhất (số càng nhỏ càng mạnh, 0 = SUPERADMIN)
    private int roleLevel;

    // Cờ đánh dấu: true nếu là SUPERADMIN (GOD MODE — bypass mọi kiểm tra)
    private boolean superAdmin;
}
