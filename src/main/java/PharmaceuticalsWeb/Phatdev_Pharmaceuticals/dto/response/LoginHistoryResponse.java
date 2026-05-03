//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/LoginHistoryResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Đóng gói một bản ghi trong CT_USER_LOGIN_LOG.
 * Phục vụ tab "Lịch sử đăng nhập" trên Profile — hiển thị IP, thiết bị, kết quả.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {

    /** LOGIN_SUCCESS | LOGIN_FAILED | LOGOUT */
    private String actionCode;
    private String actionName;

    /** Chuỗi đăng nhập đã gõ — hữu ích khi phát hiện brute-force */
    private String usernameAttempt;

    private String loginIp;
    private String userAgent;

    /** Chi tiết lỗi nếu LOGIN_FAILED (VD: 'Sai mật khẩu') */
    private String message;

    private LocalDateTime createdAt;
}
