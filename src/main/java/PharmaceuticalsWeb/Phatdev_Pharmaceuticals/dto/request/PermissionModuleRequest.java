//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/PermissionModuleRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * =========================================================================
 * DTO: HỨNG DỮ LIỆU TẠO/SỬA NHÓM CHỨC NĂNG (PERMISSION MODULE)
 * =========================================================================
 * Admin dùng khi tạo hoặc cập nhật nhóm chức năng để phân loại quyền hạt lựu.
 */
@Data
public class PermissionModuleRequest {

    // Mã module hệ thống (VD: POST, EVENT, COMMENT)
    @NotBlank(message = "Mã module không được để trống")
    @Size(max = 50, message = "Mã module tối đa 50 ký tự")
    private String moduleCode;

    // Tên hiển thị cho admin (VD: "Bài viết", "Sự kiện")
    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(max = 100, message = "Tên hiển thị tối đa 100 ký tự")
    private String moduleName;

    // Mô tả chi tiết module (không bắt buộc)
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    // Thứ tự hiển thị trên giao diện (số nhỏ hiện trước, mặc định 0)
    private Integer displayOrder;
}
