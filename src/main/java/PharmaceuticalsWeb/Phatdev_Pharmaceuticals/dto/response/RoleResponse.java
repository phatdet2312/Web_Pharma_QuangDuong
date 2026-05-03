//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/RoleResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * =========================================================================
 * DTO: ĐÓNG GÓI CHI TIẾT CHỨC VỤ VÀ QUYỀN HẠT LỰU
 * =========================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Integer id;
    private String roleName;
    private Integer roleLevel;
    private String description;
    private List<String> permissions; // Chứa mảng mã quyền mà Chức vụ này đang sở hữu
    private Integer userCount;        // Số lượng tài khoản đang mang chức vụ này (Phân tích tác động)
}