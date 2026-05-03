//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/LocationResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu địa điểm sự kiện trả về client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private Integer id;
    private String name;
    private String address;

    /** true nếu là địa điểm tổ chức trực tuyến */
    private boolean isOnline;
}
