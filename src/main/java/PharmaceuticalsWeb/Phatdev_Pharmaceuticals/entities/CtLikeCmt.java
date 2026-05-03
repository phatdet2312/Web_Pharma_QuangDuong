//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtLikeCmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_LIKECMT (ÁNH XẠ BẢNG [CT_LIKECMT])
 * =========================================================================
 * Reaction của người dùng lên comment gốc. PK kép (USER_ID, CMT_ID).
 * Mỗi user chỉ có 1 reaction / comment gốc (PK kép đảm bảo).
 * LOAILIKE_ID: loại reaction (Like, Love, Insightful...).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_LIKECMT")
public class CtLikeCmt {

    @EmbeddedId
    private CtLikeCmtId id;

    /** Người dùng react (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    /** Comment gốc được react (FK → CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cmtId")
    @JoinColumn(name = "CMT_ID")
    private Cmt cmt;

    /** Loại reaction (FK → LOAI_LIKE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAILIKE_ID", nullable = false)
    private LoaiLike loaiLike;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Khóa chính kép: (USER_ID, CMT_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtLikeCmtId implements Serializable {

        @Column(name = "USER_ID")
        private Long userId;

        @Column(name = "CMT_ID")
        private Long cmtId;
    }
}
