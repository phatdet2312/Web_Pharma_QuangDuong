//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Permission.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ PERMISSIONS (ÁNH XẠ BẢNG [PERMISSIONS])
 * =========================================================================
 * Danh sách quyền thao tác hạt lựu (Granular). VD: HIDE_COMMENT, APPROVE_EVENT.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PERMISSIONS")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PERMISSION_CODE", nullable = false, unique = true, length = 50)
    private String permissionCode;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}