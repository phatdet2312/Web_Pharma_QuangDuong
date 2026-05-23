//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEventStatusHistory.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENT_STATUS_HISTORY (ÁNH XẠ BẢNG [CT_EVENT_STATUS_HISTORY])
 * =========================================================================
 * Lịch sử thay đổi trạng thái buổi sự kiện (Event Sourcing Pattern).
 * Trạng thái hiện tại = bản ghi có CHANGED_AT mới nhất của buổi đó.
 * Các STATUS_CODE thường dùng: DRAFT, OPEN, UPCOMING, ONGOING, FULL, CANCELLED, FINISHED, ENDED.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENT_STATUS_HISTORY")
public class CtEventStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Buổi sự kiện liên quan (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CT_EVENT_ID", nullable = false)
    private CtEvent ctEvent;

    /** Mã trạng thái mới: DRAFT|OPEN|UPCOMING|ONGOING|FULL|CANCELLED|FINISHED|ENDED */
    @Column(name = "STATUS_CODE", nullable = false, length = 30)
    private String statusCode;

    /** Người thực hiện thay đổi (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANGED_BY_USER_ID")
    private User changedByUser;

    @Builder.Default
    @Column(name = "CHANGED_AT", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    /** Ghi chú lý do thay đổi trạng thái */
    @Column(name = "NOTE", length = 500)
    private String note;
}
