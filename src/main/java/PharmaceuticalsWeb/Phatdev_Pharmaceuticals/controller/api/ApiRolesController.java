//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiRolesController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.MaTranNhiPhanNguyenTu;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.TramPhatSongVoTuyenP2P;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkLockRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminMeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.UserResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IRolesService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =========================================================================
 * API CONTROLLER: QUẢN LÝ NGƯỜI DÙNG VÀ GÁN CHỨC VỤ
 * =========================================================================
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class ApiRolesController {

    private final IUserService userService;
    private final IRolesService rolesService;
    private final IAuditService auditService;
    private final TramPhatSongVoTuyenP2P tramPhatSong;

    /**
     * Trả về thông tin của Admin đang đăng nhập, bao gồm maxRoleLevel.
     * Frontend dùng để bôi xám các Checkbox chức vụ vượt cấp (Anti-privilege-escalation UI).
     */
    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> layThongTinAdminDangDangNhap() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        int maxRoleLevel = userService.layCapBacQuyenLucCaoNhat(currentUser);

        AdminMeResponse responseDto = AdminMeResponse.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .fullName(currentUser.getFullName())
                .maxRoleLevel(maxRoleLevel)
                .build();

        return ApiResponse.thanhCong(responseDto, "Lấy thông tin quản trị viên thành công");
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        List<User> users = rolesService.getAllUsersPaged(pageNo, pageSize, sortBy);
        List<UserResponse> userResponses = new ArrayList<>();

        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User userEntity = (User) usersArray[i];
                UserResponse responseDto = UserResponse.fromEntity(userEntity);
                userResponses.add(responseDto);
            }
        }

        return ApiResponse.thanhCong(userResponses, "Lấy danh sách người dùng thành công");
    }

    @GetMapping("/search-advanced")
    public ApiResponse<Map<String, Object>> searchAdvanced(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String roleName,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<User> users = rolesService.searchAdvanced(keyword, status, roleName, pageNo, pageSize);
        List<UserResponse> userResponses = new ArrayList<>();

        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User userEntity = (User) usersArray[i];
                userResponses.add(UserResponse.fromEntity(userEntity));
            }
        }

        long totalElements = rolesService.countSearchAdvanced(keyword, status, roleName);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("users", userResponses);
        responseData.put("totalPages", totalPages);
        responseData.put("totalElements", totalElements);

        return ApiResponse.thanhCong(responseData, "Truy xuất dữ liệu đa chiều thành công");
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getUserStats() {
        long total = userService.countTotalUsers();
        long locked = userService.countLockedUsers();
        long active = total - locked;

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("locked", locked);
        stats.put("online", 0L);

        return ApiResponse.thanhCong(stats, "Lấy thống kê thành công");
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserDetail(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponse responseDto = UserResponse.fromEntity(user);
        return ApiResponse.thanhCong(responseDto, "Lấy thông tin người dùng thành công");
    }

    @PostMapping("/{id}/roles")
    public ApiResponse<String> updateRoles(@PathVariable Long id, @RequestBody Map<String, List<String>> body) {
        List<String> roleStrings = body.get("roles");

        if (roleStrings == null || roleStrings.size() == 0) {
            throw new AppException(400, "Vui lòng chọn ít nhất 1 chức vụ cho tài khoản này");
        }

        User currentUser = userService.getCurrentAuthenticatedUser();
        userService.updateUserRoles(id, roleStrings, currentUser);

        MaTranNhiPhanNguyenTu.danhDauViPham(id);
        tramPhatSong.phatLenhGanCoUserUDPVaToanCau(id);

        return ApiResponse.thanhCong(null, "Cập nhật phân quyền thành công");
    }

    @PostMapping("/{id}/lock")
    public ApiResponse<String> lockUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean lock = Boolean.parseBoolean(body.get("lock").toString());
        String reason = "Khóa/Mở khóa thủ công";

        if (body.get("reason") != null && body.get("reason").toString().trim().isEmpty() == false) {
            reason = body.get("reason").toString();
        }

        User currentUser = userService.getCurrentAuthenticatedUser();

        // FIX BUG: Chuyển tác vụ xử lý 1 User vào chung hàm của Hàng Loạt (Đã bọc
        // @Transactional)
        // Đảm bảo nếu Ghi Log Lỗi -> Rollback không khóa User.
        List<Long> targetIds = new ArrayList<>();
        targetIds.add(id);
        rolesService.bulkLockUnlock(targetIds, lock, reason, currentUser);

        // Chỉ chạy các lệnh P2P khi Database đã Transaction an toàn thành công
        if (lock == true) {
            MaTranNhiPhanNguyenTu.danhDauViPham(id);
            tramPhatSong.phatLenhGanCoUserUDPVaToanCau(id);
        } else {
            MaTranNhiPhanNguyenTu.xoaDauVet(id);
            tramPhatSong.phatLenhXoaCoUserUDPVaToanCau(id);
        }

        String message = "";
        if (lock == true) {
            message = "Khóa tài khoản thành công";
        } else {
            message = "Mở khóa tài khoản thành công";
        }
        return ApiResponse.thanhCong(null, message);
    }

    @PostMapping("/bulk-lock")
    public ApiResponse<String> bulkLockUnlockUsers(@RequestBody BulkLockRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // Hàm này có @Transactional bên trong Service. Lỗi sẽ Rollback 100%.
        rolesService.bulkLockUnlock(request.getUserIds(), request.isLock(), request.getReason(), currentUser);

        // Lệnh phát tín hiệu chỉ kích hoạt khi DB thành công
        Object[] idArray = request.getUserIds().toArray();
        for (int i = 0; i < idArray.length; i = i + 1) {
            Long id = Long.valueOf(idArray[i].toString());
            if (request.isLock() == true) {
                MaTranNhiPhanNguyenTu.danhDauViPham(id);
                tramPhatSong.phatLenhGanCoUserUDPVaToanCau(id);
            } else {
                MaTranNhiPhanNguyenTu.xoaDauVet(id);
                tramPhatSong.phatLenhXoaCoUserUDPVaToanCau(id);
            }
        }

        return ApiResponse.thanhCong(null, "Thực thi kiểm duyệt hàng loạt thành công");
    }

    @GetMapping("/total-pages")
    public ApiResponse<Integer> getTotalPages(@RequestParam int pageSize) {
        int totalPages = rolesService.getTotalPages(pageSize);
        return ApiResponse.thanhCong(totalPages, "Lấy tổng số trang thành công");
    }

    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        List<User> users = rolesService.searchUsers(keyword);
        List<UserResponse> userResponses = new ArrayList<>();

        if (users != null) {
            Object[] usersArray = users.toArray();
            for (int i = 0; i < usersArray.length; i = i + 1) {
                User userEntity = (User) usersArray[i];
                userResponses.add(UserResponse.fromEntity(userEntity));
            }
        }

        return ApiResponse.thanhCong(userResponses, "Tìm kiếm thành công");
    }
}