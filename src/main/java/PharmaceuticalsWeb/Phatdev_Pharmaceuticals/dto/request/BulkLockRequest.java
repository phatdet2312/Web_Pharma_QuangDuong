//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/BulkLockRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * =========================================================================
 * DTO: YÊU CẦU THAO TÁC KHÓA/MỞ KHÓA HÀNG LOẠT
 * =========================================================================
 */
@Data
public class BulkLockRequest {

    @NotEmpty(message = "Danh sách tài khoản không được để trống")
    private List<Long> userIds;

    private boolean lock;

    @NotBlank(message = "Bắt buộc phải nhập lý do kiểm duyệt")
    private String reason;
}