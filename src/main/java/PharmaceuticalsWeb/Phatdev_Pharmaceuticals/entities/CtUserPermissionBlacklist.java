//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtUserPermissionBlacklist.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * =========================================================================
 * THỰC THỂ CT_USER_PERMISSION_BLACKLIST (ÁNH XẠ BẢNG [CT_USER_PERMISSION_BLACKLIST])
 * =========================================================================
 * Danh sách đen đóng băng quyền hạt lựu cấp độ cá nhân.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_USER_PERMISSION_BLACKLIST")
@IdClass(CtUserPermissionBlacklist.CtUserPermissionBlacklistId.class)
public class CtUserPermissionBlacklist {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "PERMISSION_ID")
    private Integer permissionId;

    /**
     * Lớp tĩnh quản lý Khóa kép (Composite Key) chuẩn JPA.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CtUserPermissionBlacklistId implements Serializable {
        private Long userId;
        private Integer permissionId;
    }
}