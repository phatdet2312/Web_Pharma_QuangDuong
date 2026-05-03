//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/WardResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về phường/xã theo quận/huyện — phục vụ populate dropdown địa chỉ phụ thuộc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WardResponse {

    private Integer id;
    private Integer districtId;
    private String name;
}
