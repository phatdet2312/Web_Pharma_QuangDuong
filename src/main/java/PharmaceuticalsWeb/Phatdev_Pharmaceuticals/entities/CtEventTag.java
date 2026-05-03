//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEventTag.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENT_TAGS (ÁNH XẠ BẢNG [CT_EVENT_TAGS])
 * =========================================================================
 * Bảng trung gian nối Buổi sự kiện ↔ Tag (nhiều-nhiều). PK kép.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENT_TAGS")
public class CtEventTag {

    @EmbeddedId
    private CtEventTagId id;

    /** Buổi sự kiện được gán tag (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ctEventId")
    @JoinColumn(name = "CT_EVENT_ID")
    private CtEvent ctEvent;

    /** Tag được gán (FK → TAGS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "TAG_ID")
    private Tag tag;

    /**
     * Khóa chính kép: (CT_EVENT_ID, TAG_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtEventTagId implements Serializable {

        @Column(name = "CT_EVENT_ID")
        private Long ctEventId;

        @Column(name = "TAG_ID")
        private Long tagId;
    }
}
