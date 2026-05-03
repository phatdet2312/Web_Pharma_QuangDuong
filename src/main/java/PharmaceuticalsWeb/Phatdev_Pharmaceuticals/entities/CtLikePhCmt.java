//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtLikePhCmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_LIKEPHCMT (ÁNH XẠ BẢNG [CT_LIKEPHCMT])
 * =========================================================================
 * Reaction của người dùng lên phản hồi (reply). PK kép (USER_ID, PH_CMT_ID).
 * Tương tự CT_LIKECMT nhưng dành cho PH_CMT thay vì CMT.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_LIKEPHCMT")
public class CtLikePhCmt {

    @EmbeddedId
    private CtLikePhCmtId id;

    /** Người dùng react (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    /** Phản hồi được react (FK → PH_CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("phCmtId")
    @JoinColumn(name = "PH_CMT_ID")
    private PhCmt phCmt;

    /** Loại reaction (FK → LOAI_LIKE) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAILIKE_ID", nullable = false)
    private LoaiLike loaiLike;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Khóa chính kép: (USER_ID, PH_CMT_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtLikePhCmtId implements Serializable {

        @Column(name = "USER_ID")
        private Long userId;

        @Column(name = "PH_CMT_ID")
        private Long phCmtId;
    }
}
