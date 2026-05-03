//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Address.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ ADDRESSES (ÁNH XẠ BẢNG [ADDRESSES])
 * =========================================================================
 * Địa chỉ doanh nghiệp của đối tác — liên kết với PARTNER_PROFILES.
 * Một đối tác có thể có nhiều địa chỉ, mỗi loại chỉ có 1 mặc định (IS_DEFAULT=1).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADDRESSES")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PARTNER_ID", nullable = false)
    private Long partnerId;

    @Column(name = "WARD_ID", nullable = false)
    private Integer wardId;

    @Column(name = "STREET_ADDRESS", nullable = false, length = 255)
    private String streetAddress;

    /**
     * Cờ địa chỉ mặc định. Logic nghiệp vụ: khi set IS_DEFAULT=1 cho một địa chỉ,
     * Service phải reset IS_DEFAULT=0 cho tất cả địa chỉ khác của cùng đối tác trước.
     */
    @Column(name = "IS_DEFAULT")
    private boolean isDefault = false;

    /**
     * Loại địa chỉ — CSDL có DEFAULT 'OFFICE'.
     * Giá trị gợi ý: OFFICE | WAREHOUSE | BRANCH
     */
    @Column(name = "ADDRESS_TYPE", length = 20)
    private String addressType;
}
