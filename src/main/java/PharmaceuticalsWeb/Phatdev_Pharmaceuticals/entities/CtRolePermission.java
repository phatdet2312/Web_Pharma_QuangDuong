//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtRolePermission.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_ROLE_PERMISSIONS (ÁNH XẠ BẢNG [CT_ROLE_PERMISSIONS])
 * =========================================================================
 * Cầu nối N-N: Gắn Quyền hạt lựu vào Nhóm chức vụ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_ROLE_PERMISSIONS")
@IdClass(CtRolePermission.CtRolePermissionId.class) // Đăng ký Khóa kép
public class CtRolePermission {

    @Id
    @Column(name = "ROLE_ID")
    private Integer roleId;

    @Id
    @Column(name = "PERMISSION_ID")
    private Integer permissionId;

    // Class tĩnh quản lý Khóa kép chuẩn JPA
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CtRolePermissionId implements Serializable {
        private Integer roleId;
        private Integer permissionId;
    }
}