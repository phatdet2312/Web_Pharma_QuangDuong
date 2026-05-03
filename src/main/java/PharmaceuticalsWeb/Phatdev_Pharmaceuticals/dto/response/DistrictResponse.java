//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/DistrictResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về quận/huyện theo tỉnh — phục vụ populate dropdown địa chỉ phụ thuộc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponse {

    private Integer id;
    private Integer provinceId;
    private String name;
}
