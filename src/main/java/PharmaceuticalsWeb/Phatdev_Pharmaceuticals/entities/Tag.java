//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Tag.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ TAGS (ÁNH XẠ BẢNG [TAGS])
 * =========================================================================
 * Tag dùng chung cho bài viết và sự kiện (nhiều-nhiều qua bảng trung gian).
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TAGS")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Slug URL-friendly, duy nhất */
    @Column(name = "SLUG", nullable = false, unique = true, length = 120)
    private String slug;
}
