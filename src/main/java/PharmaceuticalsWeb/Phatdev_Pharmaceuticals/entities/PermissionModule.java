//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PermissionModule.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ PERMISSION_MODULES (ÁNH XẠ BẢNG [PERMISSION_MODULES])
 * =========================================================================
 * Danh mục nhóm chức năng để phân loại quyền hạt lựu.
 * Admin quản lý tập trung: thêm/sửa/xóa module, frontend load dropdown từ API.
 * VD: "Bài viết", "Sự kiện", "Bình luận", "Hệ thống".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PERMISSION_MODULES")
public class PermissionModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    // Mã module hệ thống (VD: POST, EVENT, COMMENT, SYSTEM)
    @Column(name = "MODULE_CODE", nullable = false, unique = true, length = 50)
    private String moduleCode;

    // Tên hiển thị cho admin (VD: "Bài viết", "Sự kiện")
    @Column(name = "MODULE_NAME", nullable = false, length = 100)
    private String moduleName;

    // Mô tả chi tiết module
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    // Thứ tự hiển thị trên giao diện (số nhỏ hiện trước)
    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;
}
