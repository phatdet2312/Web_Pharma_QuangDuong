//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtCmtModerationLog.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_CMT_MODERATION_LOG (ÁNH XẠ BẢNG [CT_CMT_MODERATION_LOG])
 * =========================================================================
 * Lịch sử kiểm duyệt comment gốc (Event Sourcing Pattern).
 * Trạng thái hiện tại của comment = ACTION_ID mới nhất của log đó.
 * ACTION_ID tham chiếu MODERATION_ACTIONS (APPROVE, HIDE, WARN, DELETE...).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_CMT_MODERATION_LOG")
public class CtCmtModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Comment gốc bị kiểm duyệt (FK → CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMT_ID", nullable = false)
    private Cmt cmt;

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
