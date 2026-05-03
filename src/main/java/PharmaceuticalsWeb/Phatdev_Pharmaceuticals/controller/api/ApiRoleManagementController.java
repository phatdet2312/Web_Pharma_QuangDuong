//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiRoleManagementController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IRoleManagementService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * =========================================================================
 * API CONTROLLER: QUẢN LÝ CHỨC VỤ ĐỘNG VÀ QUYỀN HẠT LỰU
 * =========================================================================
 */
@RestController
@RequestMapping("/api/admin/role-management")
@RequiredArgsConstructor
public class ApiRoleManagementController {

    private final IRoleManagementService roleManagementService;
    private final IUserService userService;

    // =====================================================================
    // PHẦN 1: API QUẢN LÝ CHỨC VỤ (ROLES)
    // =====================================================================

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> layDanhSachChucVu() {
        List<RoleResponse> roles = roleManagementService.layTatCaChucVu();
        return ApiResponse.thanhCong(roles, "Lấy danh sách chức vụ thành công");
    }

    @PostMapping("/roles")
    public ApiResponse<String> taoChucVuMoi(@RequestBody RoleRequest request) {
        roleManagementService.taoChucVuMoi(request);
        return ApiResponse.thanhCong(null, "Đã tạo chức vụ thành công");
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<String> capNhatChucVu(@PathVariable Integer id, @RequestBody RoleRequest request) {
        roleManagementService.capNhatChucVu(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật chức vụ thành công");
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<String> xoaChucVu(@PathVariable Integer id) {
        roleManagementService.xoaChucVu(id);
        return ApiResponse.thanhCong(null, "Xóa chức vụ thành công");
    }

    @PostMapping("/roles/{id}/clone")
    public ApiResponse<String> nhanBanChucVu(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String tenChucVuBanSao = body.get("roleName");
        roleManagementService.nhanBanChucVu(id, tenChucVuBanSao);
        return ApiResponse.thanhCong(null, "Nhân bản chức vụ thành công");
    }

    // =====================================================================
    // PHẦN 2: API QUẢN LÝ QUYỀN HẠT LỰU VÀ BLACKLIST
    // =====================================================================

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> layDanhSachQuyenHatLuu() {
        List<PermissionResponse> permissions = roleManagementService.layTatCaQuyenHatLuu();
        return ApiResponse.thanhCong(permissions, "Lấy danh sách quyền hạt lựu thành công");
    }

    @PostMapping("/permissions")
    public ApiResponse<String> taoQuyenMoi(@RequestBody PermissionRequest request) {
        roleManagementService.taoQuyenMoi(request);
        return ApiResponse.thanhCong(null, "Đã khởi tạo quyền thao tác thành công");
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<String> capNhatQuyen(@PathVariable Integer id, @RequestBody PermissionRequest request) {
        roleManagementService.capNhatQuyen(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật quyền thao tác thành công");
    }

    @DeleteMapping("/permissions/{id}")
    public ApiResponse<String> xoaQuyen(@PathVariable Integer id) {
        roleManagementService.xoaQuyen(id);
        return ApiResponse.thanhCong(null, "Đã xóa quyền thao tác khỏi hệ thống");
    }

    @GetMapping("/blacklist/users/{userId}")
    public ApiResponse<List<Integer>> layBlacklistCuaUser(@PathVariable Long userId) {
        List<Integer> danhSachBiCam = roleManagementService.layBlacklistPermissionCuaUser(userId);
        return ApiResponse.thanhCong(danhSachBiCam, "Lấy danh sách quyền bị đóng băng thành công");
    }

    @PostMapping("/blacklist/users/{userId}")
    public ApiResponse<String> toggleBlacklist(@PathVariable Long userId, @RequestBody UserBlacklistRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.togglePermissionBlacklist(userId, request, currentUser);
        return ApiResponse.thanhCong(null, "Đã thực thi lệnh kiểm soát quyền hạt lựu");
    }
}