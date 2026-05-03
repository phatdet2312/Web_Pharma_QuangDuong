//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPostTag.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_POST_TAGS (ÁNH XẠ BẢNG [CT_POST_TAGS])
 * =========================================================================
 * Bảng trung gian nối Bài viết ↔ Tag (nhiều-nhiều). PK kép (POST_ID, TAG_ID).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_POST_TAGS")
public class CtPostTag {

    @EmbeddedId
    private CtPostTagId id;

    /** Bài viết được gán tag (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "POST_ID")
    private Post post;

    /** Tag được gán (FK → TAGS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "TAG_ID")
    private Tag tag;

    /**
     * Khóa chính kép: (POST_ID, TAG_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtPostTagId implements Serializable {

        @Column(name = "POST_ID")
        private Long postId;

        @Column(name = "TAG_ID")
        private Long tagId;
    }
}
