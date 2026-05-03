//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PartnerProfileResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

/**
 * DTO trả về thông tin hồ sơ doanh nghiệp đối tác.
 * daCoHoSo = false nghĩa là tài khoản chưa tạo hồ sơ đối tác — Frontend hiện form rỗng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerProfileResponse {

    /** true = đã có hồ sơ; false = chưa tạo hồ sơ */
    private boolean daCoHoSo;

    private Long profileId;
    private String businessName;
    private String businessPhone;
    private String avatarUrl;
    private String taxCode;
    private String licenseNumber;
    private String licenseDocumentUrl;
    private String verificationStatus;
}
