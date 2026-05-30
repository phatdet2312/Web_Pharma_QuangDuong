//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostLinkedEventResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Thông tin buổi sự kiện liên kết với bài viết.
 * Dùng cho admin/posts chi tiết — tab Sự kiện liên kết.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLinkedEventResponse {

    /** ID buổi sự kiện (CT_EVENTS.ID) */
    private Long ctEventId;

    /** Tiêu đề buổi sự kiện (CT_EVENTS.TITLE) */
    private String eventTitle;

    /** Thời gian bắt đầu buổi */
    private LocalDateTime startTime;

    /** Thời gian kết thúc buổi */
    private LocalDateTime endTime;
}
