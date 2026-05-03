//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtPostCmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_POST_CMT (ÁNH XẠ BẢNG [CT_POST_CMT])
 * =========================================================================
 * Bảng định tuyến: xác định comment gốc nào thuộc bài viết nào (5NF).
 * CMT_ID là UNIQUE → mỗi comment gốc chỉ thuộc đúng 1 bài viết.
 * PK kép (POST_ID, CMT_ID).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_POST_CMT")
public class CtPostCmt {

    @EmbeddedId
    private CtPostCmtId id;

    /** Bài viết (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "POST_ID")
    private Post post;

    /** Comment gốc (FK → CMT, UNIQUE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cmtId")
    @JoinColumn(name = "CMT_ID")
    private Cmt cmt;

    /**
     * Khóa chính kép: (POST_ID, CMT_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtPostCmtId implements Serializable {

        @Column(name = "POST_ID")
        private Long postId;

        @Column(name = "CMT_ID")
        private Long cmtId;
    }
}
