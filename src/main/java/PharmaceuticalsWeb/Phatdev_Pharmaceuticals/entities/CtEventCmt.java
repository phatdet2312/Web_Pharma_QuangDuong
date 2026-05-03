//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEventCmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENT_CMT (ÁNH XẠ BẢNG [CT_EVENT_CMT])
 * =========================================================================
 * Bảng định tuyến: xác định comment gốc nào thuộc buổi sự kiện nào (5NF).
 * CMT_ID là UNIQUE → mỗi comment gốc chỉ thuộc đúng 1 buổi sự kiện.
 * PK kép (CT_EVENT_ID, CMT_ID).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENT_CMT")
public class CtEventCmt {

    @EmbeddedId
    private CtEventCmtId id;

    /** Buổi sự kiện (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ctEventId")
    @JoinColumn(name = "CT_EVENT_ID")
    private CtEvent ctEvent;

    /** Comment gốc (FK → CMT, UNIQUE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cmtId")
    @JoinColumn(name = "CMT_ID")
    private Cmt cmt;

    /**
     * Khóa chính kép: (CT_EVENT_ID, CMT_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtEventCmtId implements Serializable {

        @Column(name = "CT_EVENT_ID")
        private Long ctEventId;

        @Column(name = "CMT_ID")
        private Long cmtId;
    }
}
