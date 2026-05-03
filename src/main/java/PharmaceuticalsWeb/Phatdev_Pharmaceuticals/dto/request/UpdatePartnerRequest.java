//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/UpdatePartnerRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO nhận dữ liệu cập nhật hồ sơ doanh nghiệp từ tab "Hồ sơ doanh nghiệp" trên Profile.
 * Dùng cho cả tạo mới (INSERT) và cập nhật (UPDATE) PARTNER_PROFILES.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartnerRequest {

    @Size(max = 200, message = "Tên doanh nghiệp không được vượt quá 200 ký tự")
    private String businessName;

    @Size(max = 20, message = "Số điện thoại doanh nghiệp không được vượt quá 20 ký tự")
    private String businessPhone;

    @Size(max = 20, message = "Mã số thuế không được vượt quá 20 ký tự")
    private String taxCode;

    @Size(max = 50, message = "Số giấy phép không được vượt quá 50 ký tự")
    private String licenseNumber;
}
