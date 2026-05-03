//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtUserRole.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_USER_ROLES (ÁNH XẠ BẢNG [CT_USER_ROLES])
 * =========================================================================
 * Cầu nối N-N: Cấp Nhóm chức vụ cho Người dùng (Hỗ trợ 1 người làm nhiều chức).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_USER_ROLES")
@IdClass(CtUserRole.CtUserRoleId.class)
public class CtUserRole {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "ROLE_ID")
    private Integer roleId;

    // Class tĩnh quản lý Khóa kép chuẩn JPA
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CtUserRoleId implements Serializable {
        private Long userId;
        private Integer roleId;
    }
}