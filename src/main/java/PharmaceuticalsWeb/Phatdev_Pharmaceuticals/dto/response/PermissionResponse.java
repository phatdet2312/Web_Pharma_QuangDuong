//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PermissionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================================
 * DTO: ĐÓNG GÓI DANH SÁCH QUYỀN HẠT LỰU ĐỂ VẼ CHECKBOX
 * =========================================================================
 * Tuân thủ tuyệt đối quy tắc: Không trả trực tiếp Entity ra ngoài API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Integer id;
    private String permissionCode;
    private String description;
    private String riskLevel;
    private Integer moduleId;
    private String moduleCode;
    private String moduleName;

    // Hàm tiện ích chuyển đổi từ Entity sang DTO (không kèm thông tin module)
    public static PermissionResponse fromEntity(Permission entity) {
        if (entity == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(entity.getId())
                .permissionCode(entity.getPermissionCode())
                .description(entity.getDescription())
                .moduleId(entity.getModuleId())
                .riskLevel("SAFE")
                .build();
    }
}