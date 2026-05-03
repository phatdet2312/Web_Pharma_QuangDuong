//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PhCmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ PH_CMT (ÁNH XẠ BẢNG [PH_CMT])
 * =========================================================================
 * Phản hồi bình luận (Reply Comment) — THUẦN KHIẾT theo chuẩn 5NF.
 * ROOT_CMT_ID → CMT: phản hồi này luôn thuộc về một comment gốc.
 * PARENT_PH_ID → PH_CMT (self-ref nullable): reply của reply (nested replies).
 * Khi PARENT_PH_ID = null → phản hồi trực tiếp vào comment gốc.
 * Khi PARENT_PH_ID != null → phản hồi vào một reply khác (thread).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PH_CMT")
public class PhCmt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Comment gốc mà reply này thuộc về (FK → CMT) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_CMT_ID", nullable = false)
    private Cmt rootCmt;

    /**
     * Reply cha nếu đây là reply lồng nhau (self-reference nullable).
     * null → phản hồi trực tiếp comment gốc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PH_ID")
    private PhCmt parentPh;

    /** Tác giả phản hồi (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
