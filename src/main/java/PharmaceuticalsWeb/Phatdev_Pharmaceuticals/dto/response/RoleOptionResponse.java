//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/RoleOptionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Một lựa chọn nhóm quyền cho dropdown select.
 */
@Getter
@Setter
public class RoleOptionResponse {

    private Integer id;
    private String roleName;
    private Integer roleLevel;
}
