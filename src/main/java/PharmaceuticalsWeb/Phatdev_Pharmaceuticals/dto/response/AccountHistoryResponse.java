//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AccountHistoryResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Đóng gói một bản ghi trong CT_USER_MODERATION_LOG nhìn từ góc độ USER.
 * "Lịch sử tài khoản" — user thấy admin đã làm gì với tài khoản của mình,
 * bao gồm lý do khóa, gán/thu hồi role, tước/khôi phục quyền.
 * Đây là dữ liệu giá trị nhất từ CT_USER_MODERATION_LOG chưa từng được khai thác trước đây.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryResponse {

    private String actionCode;
    private String actionName;

    /** Lý do admin ghi — KHÔNG được để trống theo thiết kế CSDL (NOT NULL) */
    private String reason;

    /** Tên Admin/Moderator đã thực hiện hành động */
    private String tenModerator;

    /** Quyền hạt lựu bị tác động — chỉ có khi actionCode = BLACKLIST_PERM / UNBLACKLIST_PERM */
    private String permissionCode;

    private LocalDateTime createdAt;
}
