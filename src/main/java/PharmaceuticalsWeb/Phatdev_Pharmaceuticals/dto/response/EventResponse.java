//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu chiến dịch sự kiện (EVENTS) trả về client.
 * Kèm danh sách buổi (sessions) để hiển thị accordion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;

    private Integer eventTypeId;
    private String eventTypeName;

    /** Danh sách buổi của chiến dịch này */
    private List<CtEventResponse> sessions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
