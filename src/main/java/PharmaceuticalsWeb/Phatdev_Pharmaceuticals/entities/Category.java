//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Category.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ CATEGORIES (ÁNH XẠ BẢNG [CATEGORIES])
 * =========================================================================
 * Danh mục bài viết y khoa. Dùng SLUG để tạo URL thân thiện SEO.
 * IS_ACTIVE kiểm soát hiển thị public, không xóa vật lý.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CATEGORIES")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /** Tên hiển thị của danh mục */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Slug URL-friendly, duy nhất toàn hệ thống */
    @Column(name = "SLUG", nullable = false, unique = true, length = 120)
    private String slug;

    /** Mô tả ngắn cho SEO và sidebar */
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    /** Trạng thái hiển thị: 1=đang dùng, 0=ẩn */
    @Builder.Default
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive = true;
}
