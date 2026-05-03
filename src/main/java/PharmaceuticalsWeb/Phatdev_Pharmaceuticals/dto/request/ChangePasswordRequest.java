//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/ChangePasswordRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO nhận yêu cầu đổi mật khẩu từ tab "Bảo mật" trên Profile.
 * Backend bắt buộc xác minh lại oldPassword bằng BCrypt trước khi cập nhật.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6 đến 100 ký tự")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*+=?_])[A-Za-z0-9!@#$%^&*+=?_]{6,}$",
        message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ số và 1 ký tự đặc biệt (!@#$%^&*+=?_)"
    )
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
