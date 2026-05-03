//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ProvinceResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về tỉnh/thành phố — phục vụ populate dropdown địa chỉ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceResponse {

    private Integer id;
    private String name;
    private String code;
}
