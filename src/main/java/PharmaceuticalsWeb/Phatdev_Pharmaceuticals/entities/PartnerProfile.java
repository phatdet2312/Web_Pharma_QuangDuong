//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PartnerProfile.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ PARTNER_PROFILES (ÁNH XẠ BẢNG [PARTNER_PROFILES])
 * =========================================================================
 * Hồ sơ doanh nghiệp đối tác — quan hệ 1-1 với USERS.
 * Một tài khoản mới đăng ký có thể CHƯA có hồ sơ đối tác (null).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PARTNER_PROFILES")
public class PartnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Liên kết 1-1 với bảng USERS — một User chỉ có tối đa một hồ sơ đối tác */
    @Column(name = "USER_ID", nullable = false, unique = true)
    private Long userId;

    @Column(name = "BUSINESS_NAME", nullable = false, length = 150)
    private String businessName;

    @Column(name = "BUSINESS_PHONE", length = 20)
    private String businessPhone;

    @Column(name = "AVATAR_URL", length = 500)
    private String avatarUrl;

    @Column(name = "TAX_CODE", length = 20)
    private String taxCode;

    @Column(name = "LICENSE_NUMBER", length = 50)
    private String licenseNumber;

    /** Đường dẫn file giấy phép kinh doanh đã tải lên (1 file theo thiết kế CSDL) */
    @Column(name = "LICENSE_DOCUMENT_URL", length = 500)
    private String licenseDocumentUrl;

    /**
     * Trạng thái xác minh hồ sơ đối tác.
     * Giá trị: PENDING | VERIFIED | REJECTED
     * CSDL có DEFAULT 'PENDING' nên không cần set khi tạo mới.
     */
    @Column(name = "VERIFICATION_STATUS", length = 20)
    private String verificationStatus;
}
