//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/RegisterRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    @ValidUsername(message = "Username đã tồn tại")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    private String phone;
    private String address;
    private LocalDate birthDate;
}