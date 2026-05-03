//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/ProfileMeResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về toàn bộ thông tin cá nhân của tài khoản hiện tại.
 * Không chứa password — an toàn để gửi ra Frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileMeResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String provider;
    private boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Danh sách tên các Nhóm Quyền (Roles) đang giữ */
    private List<String> roles;

    /** Danh sách mã các Quyền hạt lựu đang bị Blacklist */
    private List<String> blacklistedPermissions;
}
