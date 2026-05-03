//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtUserActionLog.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_USER_ACTION_LOG (ÁNH XẠ BẢNG [CT_USER_ACTION_LOG])
 * =========================================================================
 * Nhật ký hành động người dùng tự thực hiện (cập nhật hồ sơ, đổi mật khẩu...).
 * Lưu dữ liệu trước/sau dưới dạng JSON — phục vụ audit trail chi tiết.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_USER_ACTION_LOG")
public class CtUserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    /** Khóa ngoại trỏ tới MODERATION_ACTIONS để phân loại hành động */
    @Column(name = "ACTION_ID", nullable = false)
    private Integer actionId;

    /** ID của đối tượng bị tác động (VD: ID địa chỉ khi xóa địa chỉ) */
    @Column(name = "TARGET_ENTITY_ID")
    private Long targetEntityId;

    /** Snapshot JSON trạng thái trước khi thay đổi */
    @Column(name = "OLD_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String oldPayload;

    /** Snapshot JSON trạng thái sau khi thay đổi */
    @Column(name = "NEW_PAYLOAD", columnDefinition = "NVARCHAR(MAX)")
    private String newPayload;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
