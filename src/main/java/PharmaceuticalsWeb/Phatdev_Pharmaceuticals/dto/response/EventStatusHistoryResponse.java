//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventStatusHistoryResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dữ liệu một bản ghi lịch sử trạng thái sự kiện.
 * Dùng để vẽ timeline trên modal admin/events.html.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusHistoryResponse {

    private Long id;
    private Long ctEventId;
    private String statusCode;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;
    private String note;
}
