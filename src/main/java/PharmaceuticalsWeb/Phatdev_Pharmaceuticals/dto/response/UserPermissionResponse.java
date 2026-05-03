//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/UserPermissionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bản đồ quyền hạn đầy đủ của tài khoản — phục vụ tab "Phân quyền" trên Profile.
 * Khai thác toàn bộ 3 bảng: CT_ROLE_PERMISSIONS, PERMISSIONS, CT_USER_PERMISSION_BLACKLIST
 * + JOIN với CT_USER_MODERATION_LOG để lộ LÝ DO và NGƯỜI blacklist.
 * Đây là phiên bản nâng cấp so với thiết kế cũ (chỉ có cờ true/false).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {

    private String roleName;
    private Integer roleLevel;
    private String roleDescription;
    private int soQuyenHoatDong;
    private int soQuyenBiKhoa;
    private List<PermissionItem> danhSachQuyen;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionItem {
        private String permissionCode;
        private String description;
        private boolean biBlacklist;

        /** Lý do bị tước quyền — từ CT_USER_MODERATION_LOG.REASON */
        private String lyDoBlacklist;

        /** Ai đã tước — từ USERS.FULL_NAME của MODERATOR_ID */
        private String tenModerator;

        /** Thời điểm bị tước — từ CT_USER_MODERATION_LOG.CREATED_AT */
        private LocalDateTime blacklistedAt;
    }
}
