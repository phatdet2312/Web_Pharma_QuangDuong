//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/EventType.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ EVENT_TYPES (ÁNH XẠ BẢNG [EVENT_TYPES])
 * =========================================================================
 * Loại sự kiện: Hội thảo, Webinar, CME, Triển lãm...
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EVENT_TYPES")
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}
