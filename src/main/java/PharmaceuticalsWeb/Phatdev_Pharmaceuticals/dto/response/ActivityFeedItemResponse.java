//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ActivityFeedItemResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về một mục trong dòng thời gian hoạt động (Activity Feed) trên Profile.
 * Tổng hợp từ CT_USER_MODERATION_LOG (do admin tác động) và CT_USER_ACTION_LOG (do user tự làm).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityFeedItemResponse {

    /**
     * Loại nguồn gốc của sự kiện.
     * Giá trị: "MODERATION" (admin tác động) hoặc "ACTION" (user tự thực hiện)
     */
    private String sourceType;

    private String actionCode;
    private String actionName;

    /** Lý do (cho MODERATION) hoặc mô tả ngắn (cho ACTION) */
    private String description;

    /** Tên người thực hiện — Admin name hoặc chính tài khoản */
    private String actorName;

    private LocalDateTime createdAt;
}
