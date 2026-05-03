//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtUserModerationLog.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_USER_MODERATION_LOG (ÁNH XẠ BẢNG [CT_USER_MODERATION_LOG])
 * =========================================================================
 * Sổ tay kiểm toán các thao tác nhạy cảm trên tài khoản.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_USER_MODERATION_LOG")
public class CtUserModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TARGET_USER_ID", nullable = false)
    private Long targetUserId;

    @Column(name = "ACTION_ID", nullable = false)
    private Integer actionId;

    @Column(name = "PERMISSION_ID")
    private Integer permissionId;

    @Column(name = "MODERATOR_ID", nullable = false)
    private Long moderatorId;

    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}