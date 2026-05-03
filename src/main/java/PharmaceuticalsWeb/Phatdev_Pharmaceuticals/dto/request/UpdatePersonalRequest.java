//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/UpdatePersonalRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu cập nhật thông tin cá nhân từ tab "Thông tin cá nhân" trên Profile.
 * Backend validate lại toàn bộ — không tin dữ liệu từ client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalRequest {

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Số điện thoại chỉ chứa chữ số và ký tự +, -, khoảng trắng")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private LocalDate birthDate;
}
