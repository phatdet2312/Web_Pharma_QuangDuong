//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/ModerationAction.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ MODERATION_ACTIONS (ÁNH XẠ BẢNG [MODERATION_ACTIONS])
 * =========================================================================
 * Quản lý danh mục các hành vi kiểm duyệt của hệ thống.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MODERATION_ACTIONS")
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /** Tên bảng bị ảnh hưởng trực tiếp bởi hành vi này (nullable — dùng cho audit log chi tiết) */
    @Column(name = "AFFECTED_TABLE", length = 50)
    private String affectedTable;
}