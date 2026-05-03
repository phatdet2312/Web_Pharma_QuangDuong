//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventStatusRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request thay đổi trạng thái buổi sự kiện.
 * Ghi một bản ghi mới vào CT_EVENT_STATUS_HISTORY.
 */
@Getter
@Setter
public class EventStatusRequest {

    /** ID buổi sự kiện cần đổi trạng thái */
    private Long ctEventId;

    /** Trạng thái mới: DRAFT | OPEN | FULL | CANCELLED | COMPLETED */
    @NotBlank(message = "Mã trạng thái không được để trống")
    private String statusCode;

    /** Lý do thay đổi (tùy chọn nhưng khuyến khích điền) */
    private String note;
}
