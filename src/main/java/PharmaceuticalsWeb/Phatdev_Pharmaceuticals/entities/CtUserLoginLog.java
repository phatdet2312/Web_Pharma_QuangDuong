//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtUserLoginLog.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_USER_LOGIN_LOG (ÁNH XẠ BẢNG [CT_USER_LOGIN_LOG])
 * =========================================================================
 * Nhật ký đăng nhập — ghi lại mọi sự kiện xác thực thành công/thất bại.
 * USER_ID nullable cho phép ghi log kể cả khi username không tồn tại trong CSDL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_USER_LOGIN_LOG")
public class CtUserLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** ID tài khoản đăng nhập thành công — null nếu username không tồn tại */
    @Column(name = "USER_ID")
    private Long userId;

    /** Khóa ngoại trỏ tới MODERATION_ACTIONS để phân loại sự kiện (LOGIN_SUCCESS, LOGIN_FAILED...) */
    @Column(name = "ACTION_ID", nullable = false)
    private Integer actionId;

    /** Tên đăng nhập được thử — lưu nguyên gốc ngay cả khi thất bại */
    @Column(name = "USERNAME_ATTEMPT", length = 100)
    private String usernameAttempt;

    @Column(name = "LOGIN_IP", length = 45)
    private String loginIp;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "MESSAGE", length = 255)
    private String message;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
