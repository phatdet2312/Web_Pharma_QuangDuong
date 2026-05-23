//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventAgendaResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * DTO: PHẢN HỒI LỊCH TRÌNH CHI TIẾT
 * =========================================================================
 * Bao gồm cả danh sách các Diễn giả (Speakers) được phân công trong khung giờ này.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAgendaResponse {
    private Long id;
    private Long ctEventId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionTitle;
    private String description;
    private Integer displayOrder;

    /** true nếu phần mô tả chi tiết đã được backend thay bằng teaser bảo mật */
    private boolean restricted;
    
    /** Danh sách thông tin chuyên gia phụ trách khung giờ này */
    private List<EventSpeakerResponse> speakers;
}
