//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Location.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ LOCATIONS (ÁNH XẠ BẢNG [LOCATIONS])
 * =========================================================================
 * Địa điểm tổ chức sự kiện. Có thể là địa chỉ vật lý hoặc link online.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LOCATIONS")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    /** Địa chỉ đầy đủ hoặc URL nếu là sự kiện online */
    @Column(name = "ADDRESS", length = 500)
    private String address;

    /** Cờ phân biệt địa điểm tổ chức trực tuyến (1) hay trực tiếp (0) */
    @Column(name = "IS_ONLINE", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean isOnline;
}
