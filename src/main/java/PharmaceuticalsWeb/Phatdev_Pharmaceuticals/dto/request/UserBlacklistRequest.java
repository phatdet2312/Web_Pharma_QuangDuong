//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/UserBlacklistRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * =========================================================================
 * DTO: YÊU CẦU ĐÓNG BĂNG QUYỀN HẠT LỰU CỤC BỘ
 * =========================================================================
 */
@Data
public class UserBlacklistRequest {

    @NotNull(message = "Mã quyền không được để trống")
    private Integer permissionId;

    private boolean isBanned;

    @NotBlank(message = "Bắt buộc phải nhập lý do thay đổi quyền")
    private String reason;
}