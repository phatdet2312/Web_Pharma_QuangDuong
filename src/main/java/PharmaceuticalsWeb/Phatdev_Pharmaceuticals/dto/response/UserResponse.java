//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/UserResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * =========================================================================
 * DTO: ĐÓNG GÓI DỮ LIỆU NGƯỜI DÙNG TRẢ VỀ FRONTEND
 * =========================================================================
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String provider;
    private boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Tách bạch rõ ràng 2 luồng dữ liệu phân quyền để Frontend dễ vẽ
    private Set<String> roles;
    private Set<String> permissions;

    /**
     * Hàm ánh xạ từ lõi Entity sang DTO an toàn.
     * TUÂN THỦ QUY TẮC: Xóa bỏ Stream API, dùng toArray() và vòng lặp for.
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }

        // 1. Trích xuất danh sách Nhóm Quyền (Roles)
        Set<String> danhSachRolesThucTe = new HashSet<>();
        List<String> listRoleTuDb = user.getDanhSachTenRole();
        
        if (listRoleTuDb != null) {
            Object[] mangRole = listRoleTuDb.toArray();
            for (int i = 0; i < mangRole.length; i = i + 1) {
                danhSachRolesThucTe.add(mangRole[i].toString());
            }
        }

        // 2. Trích xuất danh sách Quyền hạt lựu (Permissions)
        Set<String> danhSachPermissionsThucTe = new HashSet<>();
        List<String> listPermissionTuDb = user.getDanhSachTenPermission();
        
        if (listPermissionTuDb != null) {
            Object[] mangPermission = listPermissionTuDb.toArray();
            for (int j = 0; j < mangPermission.length; j = j + 1) {
                danhSachPermissionsThucTe.add(mangPermission[j].toString());
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .birthDate(user.getBirthDate())
                .provider(user.getProvider())
                .locked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(danhSachRolesThucTe)
                .permissions(danhSachPermissionsThucTe)
                .build();
    }
}