//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Ward.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ WARDS (ÁNH XẠ BẢNG [WARDS])
 * =========================================================================
 * Danh mục phường/xã/thị trấn — tầng thấp nhất trong chuỗi địa chỉ hành chính.
 * Mỗi phường/xã thuộc đúng một quận/huyện.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "WARDS")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DISTRICT_ID", nullable = false)
    private Integer districtId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;
}
