//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/UserRole.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ USER_ROLES (ÁNH XẠ BẢNG [USER_ROLES])
 * =========================================================================
 * Quản lý danh sách các Nhóm Quyền (Chức vụ). Admin có thể tạo thêm thoải mái.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USER_ROLES")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "ROLE_NAME", nullable = false, unique = true, length = 50)
    private String roleName;

    // QUAN TRỌNG: Cấp bậc quyền hạn. Số càng nhỏ quyền càng to (0 là SuperAdmin)
    // Dùng để chặn các nhân viên cấp thấp khóa tài khoản của sếp.
    @Column(name = "ROLE_LEVEL", nullable = false)
    private Integer roleLevel;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}