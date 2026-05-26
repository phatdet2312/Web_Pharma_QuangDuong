//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventStatusRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "ID buổi sự kiện không được để trống")
    private Long ctEventId;

    /** Trạng thái mới: DRAFT | OPEN | UPCOMING | ONGOING | FULL | CANCELLED | FINISHED | ENDED */
    @NotBlank(message = "Mã trạng thái không được để trống")
    @Size(max = 50, message = "Mã trạng thái tối đa 50 ký tự")
    private String statusCode;

    /** Lý do thay đổi (tùy chọn nhưng khuyến khích điền) */
    @Size(max = 255, message = "Ghi chú trạng thái tối đa 255 ký tự")
    private String note;
}
