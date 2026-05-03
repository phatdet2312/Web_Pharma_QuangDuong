//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PostImage.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ POST_IMAGES (ÁNH XẠ BẢNG [POST_IMAGES])
 * =========================================================================
 * Thư viện ảnh đính kèm bài viết. DISPLAY_ORDER kiểm soát thứ tự hiển thị.
 * Ràng buộc DISPLAY_ORDER > 0 được enforce ở DB, tầng service validate trước.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "POST_IMAGES")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Bài viết chứa ảnh này (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;

    /** Thứ tự hiển thị, phải > 0 */
    @Column(name = "DISPLAY_ORDER", nullable = false)
    private Integer displayOrder;
}
