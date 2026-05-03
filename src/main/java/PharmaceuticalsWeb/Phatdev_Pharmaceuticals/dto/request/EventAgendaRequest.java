//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventAgendaRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * DTO: YÊU CẦU THÊM/SỬA MỐC LỊCH TRÌNH
 * =========================================================================
 * Bổ sung danh sách speakerIds để thiết lập quan hệ N-N với Diễn giả ngay khi tạo.
 */
@Getter
@Setter
public class EventAgendaRequest {

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    @NotBlank(message = "Tên chuyên đề không được để trống")
    private String sessionTitle;

    private String description;

    private Integer displayOrder;

    /** Danh sách ID của các diễn giả tham gia báo cáo trong mốc lịch trình này */
    private List<Long> speakerIds;
}