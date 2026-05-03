//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/AddressRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO nhận dữ liệu thêm/sửa địa chỉ từ tab "Địa chỉ" trên Profile.
 * wardId xác định đầy đủ tỉnh/quận/phường trong chuỗi hành chính.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotNull(message = "Phường/xã không được để trống")
    private Integer wardId;

    @NotBlank(message = "Địa chỉ đường/số nhà không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String streetAddress;

    private boolean isDefault = false;

    @Size(max = 20, message = "Loại địa chỉ không được vượt quá 20 ký tự")
    private String addressType;
}
