//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/LoaiLike.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ LOAI_LIKE (ÁNH XẠ BẢNG [LOAI_LIKE])
 * =========================================================================
 * Hệ thống reaction đa loại: Like, Love, Insightful, Haha...
 * CODE là định danh kỹ thuật (LIKE, LOVE...), ICON_URL chứa đường dẫn emoji/icon.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LOAI_LIKE")
public class LoaiLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    /** Mã định danh kỹ thuật, duy nhất (VD: LIKE, LOVE, HAHA) */
    @Column(name = "CODE", nullable = false, unique = true, length = 30)
    private String code;

    /** Tên hiển thị người dùng */
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    /** Đường dẫn icon/emoji hiển thị trên giao diện */
    @Column(name = "ICON_URL", length = 255)
    private String iconUrl;
}
