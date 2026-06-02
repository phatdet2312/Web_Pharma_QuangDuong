//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PermissionModuleResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PermissionModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================================
 * DTO: ĐÓNG GÓI NHÓM CHỨC NĂNG ĐỂ VẼ DROPDOWN/TABLE TRÊN GIAO DIỆN
 * =========================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionModuleResponse {
    private Integer id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private Integer displayOrder;

    // Chuyển đổi từ Entity sang DTO
    public static PermissionModuleResponse fromEntity(PermissionModule entity) {
        if (entity == null) {
            return null;
        }
        return PermissionModuleResponse.builder()
                .id(entity.getId())
                .moduleCode(entity.getModuleCode())
                .moduleName(entity.getModuleName())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
