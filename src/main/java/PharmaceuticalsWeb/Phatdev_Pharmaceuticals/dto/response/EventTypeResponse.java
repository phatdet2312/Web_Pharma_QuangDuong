//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventTypeResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu loại sự kiện trả về client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTypeResponse {

    private Integer id;
    private String name;
    private String description;

    /** Số chiến dịch thuộc loại này */
    private long eventCount;
}
