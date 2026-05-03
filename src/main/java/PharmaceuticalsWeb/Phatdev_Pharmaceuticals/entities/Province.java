//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Province.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ PROVINCES (ÁNH XẠ BẢNG [PROVINCES])
 * =========================================================================
 * Danh mục tỉnh/thành phố — tầng cao nhất trong chuỗi địa chỉ hành chính.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROVINCES")
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "CODE", nullable = false, unique = true, length = 20)
    private String code;
}
