//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/District.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ DISTRICTS (ÁNH XẠ BẢNG [DISTRICTS])
 * =========================================================================
 * Danh mục quận/huyện — tầng trung gian trong chuỗi địa chỉ hành chính.
 * Mỗi quận/huyện thuộc đúng một tỉnh/thành phố.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DISTRICTS")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PROVINCE_ID", nullable = false)
    private Integer provinceId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
