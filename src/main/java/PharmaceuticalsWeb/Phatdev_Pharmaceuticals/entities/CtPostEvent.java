//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPostEvent.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_POST_EVENTS (ÁNH XẠ BẢNG [CT_POST_EVENTS])
 * =========================================================================
 * Nối Buổi sự kiện ↔ Bài viết liên quan (nhiều-nhiều). PK kép.
 * VD: Buổi hội thảo tim mạch gắn với 3 bài viết nghiên cứu liên quan.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_POST_EVENTS")
public class CtPostEvent {

    @EmbeddedId
    private CtPostEventId id;

    /** Buổi sự kiện (FK → CT_EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ctEventId")
    @JoinColumn(name = "CT_EVENT_ID")
    private CtEvent ctEvent;

    /** Bài viết liên quan (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "POST_ID")
    private Post post;

    /**
     * Khóa chính kép: (CT_EVENT_ID, POST_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtPostEventId implements Serializable {

        @Column(name = "CT_EVENT_ID")
        private Long ctEventId;

        @Column(name = "POST_ID")
        private Long postId;
    }
}
