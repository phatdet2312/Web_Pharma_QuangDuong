//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ModerationActionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================================
 * DTO: PHẢN HỒI DANH MỤC HÀNH VI KIỂM DUYỆT
 * =========================================================================
 * Dùng để populate dropdown bộ lọc trên Frontend — không lộ Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationActionResponse {
    private String code;
    private String name;
}
