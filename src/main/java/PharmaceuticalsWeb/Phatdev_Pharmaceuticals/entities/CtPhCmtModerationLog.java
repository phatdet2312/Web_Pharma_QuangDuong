//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPhCmtModerationLog.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_PH_CMT_MODERATION_LOG (ÁNH XẠ BẢNG [CT_PH_CMT_MODERATION_LOG])
 * =========================================================================
 * Lịch sử kiểm duyệt phản hồi (reply). Tương tự CT_CMT_MODERATION_LOG
 * nhưng dành cho PH_CMT thay vì CMT.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_PH_CMT_MODERATION_LOG")
public class CtPhCmtModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Phản hồi bị kiểm duyệt (FK → PH_CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PH_CMT_ID", nullable = false)
    private PhCmt phCmt;

    /** Hành động kiểm duyệt (FK → MODERATION_ACTIONS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID", nullable = false)
    private ModerationAction action;

    /** Người kiểm duyệt (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODERATOR_ID", nullable = false)
    private User moderator;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
