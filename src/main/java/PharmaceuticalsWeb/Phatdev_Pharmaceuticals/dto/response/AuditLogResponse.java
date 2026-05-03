//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AuditLogResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * DTO: PHẢN HỒI LỊCH SỬ KIỂM TOÁN BẢO MẬT
 * =========================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long logId;
    private String actionCode;   // Mã máy (VD: LOCK_USER, BLACKLIST_PERM) — Frontend dùng để render icon
    private String actionName;   // Tên hiển thị (VD: Khóa tài khoản)
    private String permissionCode;
    private String moderatorName;
    private String reason;
    private LocalDateTime createdAt;
}