//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiRoleManagementController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionModuleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.PermissionRegistry;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.MyPermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionModuleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IRoleManagementService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * =========================================================================
 * API CONTROLLER: QUẢN LÝ CHỨC VỤ ĐỘNG VÀ QUYỀN THAO TÁC
 * =========================================================================
 */
@RestController
@RequestMapping("/api/admin/role-management")
@RequiredArgsConstructor
public class ApiRoleManagementController {

    private final IRoleManagementService roleManagementService;
    private final IUserService userService;

    // =====================================================================
    // PHẦN 0: API LẤY QUYỀN CỦA USER HIỆN TẠI (PHÂN QUYỀN ĐỘNG FRONTEND)
    // =====================================================================

    /**
     * Trả về danh sách roles + permissions của user đang đăng nhập.
     * Frontend dùng để ẩn/hiện UI theo quyền (chỉ là UX — backend vẫn enforce).
     */
    @GetMapping("/my-permissions")
    public ApiResponse<MyPermissionResponse> layQuyenCuaToi() {
        User currentUser = userService.getCurrentAuthenticatedUser();

        // SUPERADMIN = roleLevel == 0 (nguồn sự thật duy nhất, không check tên role)
        int roleLevel = userService.layCapBacQuyenLucCaoNhat(currentUser);
        boolean laSuperAdmin = (roleLevel == 0);

        MyPermissionResponse response = MyPermissionResponse.builder()
                .roles(currentUser.getDanhSachTenRole())
                .permissions(currentUser.getDanhSachTenPermission())
                .roleLevel(roleLevel)
                .superAdmin(laSuperAdmin)
                .build();

        return ApiResponse.thanhCong(response, "Lấy quyền người dùng hiện tại thành công");
    }

    /**
     * Trả về danh sách mã quyền mà backend đang sử dụng (@RequirePermission).
     * Frontend dùng để hiện dropdown khi admin tạo quyền mới — admin CHỌN chứ không TỰ GÕ.
     * Mỗi phần tử gồm: code, description, moduleCode.
     */
    @RequirePermission("RBAC_PERMISSION_VIEW")
    @GetMapping("/system-permissions")
    public ApiResponse<List<Map<String, String>>> layDanhSachQuyenHeThong() {
        List<String[]> danhSachGoc = PermissionRegistry.layDanhSachQuyenHeThong();
        List<Map<String, String>> ketQua = new ArrayList<>();

        for (int i = 0; i < danhSachGoc.size(); i = i + 1) {
            String[] hangMuc = danhSachGoc.get(i);
            Map<String, String> item = new java.util.LinkedHashMap<>();
            item.put("code", hangMuc[0]);
            item.put("description", hangMuc[1]);
            item.put("moduleCode", hangMuc[2]);
            ketQua.add(item);
        }

        return ApiResponse.thanhCong(ketQua, "Lấy danh sách quyền hệ thống thành công");
    }

    // =====================================================================
    // PHẦN 1: API QUẢN LÝ CHỨC VỤ (ROLES)
    // =====================================================================

    @RequirePermission("RBAC_ROLE_VIEW")
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> layDanhSachChucVu() {
        List<RoleResponse> roles = roleManagementService.layTatCaChucVu();
        return ApiResponse.thanhCong(roles, "Lấy danh sách chức vụ thành công");
    }

    @RequirePermission("RBAC_ROLE_CREATE")
    @PostMapping("/roles")
    public ApiResponse<String> taoChucVuMoi(@Valid @RequestBody RoleRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.taoChucVuMoi(request, currentUser);
        return ApiResponse.thanhCong(null, "Đã tạo chức vụ thành công");
    }

    @RequirePermission("RBAC_ROLE_UPDATE")
    @PutMapping("/roles/{id}")
    public ApiResponse<String> capNhatChucVu(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.capNhatChucVu(id, request, currentUser);
        return ApiResponse.thanhCong(null, "Cập nhật chức vụ thành công");
    }

    @RequirePermission("RBAC_ROLE_DELETE")
    @DeleteMapping("/roles/{id}")
    public ApiResponse<String> xoaChucVu(@PathVariable Integer id) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.xoaChucVu(id, currentUser);
        return ApiResponse.thanhCong(null, "Xóa chức vụ thành công");
    }

    @RequirePermission("RBAC_ROLE_CLONE")
    @PostMapping("/roles/{id}/clone")
    public ApiResponse<String> nhanBanChucVu(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String tenChucVuBanSao = body.get("roleName");
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.nhanBanChucVu(id, tenChucVuBanSao, currentUser);
        return ApiResponse.thanhCong(null, "Nhân bản chức vụ thành công");
    }

    // =====================================================================
    // PHẦN 2: API QUẢN LÝ QUYỀN THAO TÁC VÀ BLACKLIST
    // =====================================================================

    @RequirePermission("RBAC_PERMISSION_VIEW")
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> layDanhSachQuyenHatLuu() {
        List<PermissionResponse> permissions = roleManagementService.layTatCaQuyenHatLuu();
        return ApiResponse.thanhCong(permissions, "Lấy danh sách quyền thao tác thành công");
    }

    @RequirePermission("RBAC_PERMISSION_CREATE")
    @PostMapping("/permissions")
    public ApiResponse<String> taoQuyenMoi(@Valid @RequestBody PermissionRequest request) {
        roleManagementService.taoQuyenMoi(request);
        return ApiResponse.thanhCong(null, "Đã khởi tạo quyền thao tác thành công");
    }

    @RequirePermission("RBAC_PERMISSION_UPDATE")
    @PutMapping("/permissions/{id}")
    public ApiResponse<String> capNhatQuyen(@PathVariable Integer id, @Valid @RequestBody PermissionRequest request) {
        roleManagementService.capNhatQuyen(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật quyền thao tác thành công");
    }

    @RequirePermission("RBAC_PERMISSION_DELETE")
    @DeleteMapping("/permissions/{id}")
    public ApiResponse<String> xoaQuyen(@PathVariable Integer id) {
        roleManagementService.xoaQuyen(id);
        return ApiResponse.thanhCong(null, "Đã xóa quyền thao tác khỏi hệ thống");
    }

    @RequirePermission("RBAC_BLACKLIST_VIEW")
    @GetMapping("/blacklist/users/{userId}")
    public ApiResponse<List<Integer>> layBlacklistCuaUser(@PathVariable Long userId) {
        List<Integer> danhSachBiCam = roleManagementService.layBlacklistPermissionCuaUser(userId);
        return ApiResponse.thanhCong(danhSachBiCam, "Lấy danh sách quyền bị đóng băng thành công");
    }

    @RequirePermission("RBAC_BLACKLIST_TOGGLE")
    @PostMapping("/blacklist/users/{userId}")
    public ApiResponse<String> toggleBlacklist(@PathVariable Long userId, @Valid @RequestBody UserBlacklistRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.togglePermissionBlacklist(userId, request, currentUser);
        return ApiResponse.thanhCong(null, "Đã thực thi lệnh kiểm soát quyền thao tác");
    }

    // =====================================================================
    // PHẦN 3: API QUẢN LÝ NHÓM CHỨC NĂNG (PERMISSION MODULES)
    // =====================================================================

    /** Lấy danh sách tất cả nhóm chức năng (sắp xếp theo thứ tự hiển thị) */
    @RequirePermission("RBAC_MODULE_VIEW")
    @GetMapping("/modules")
    public ApiResponse<List<PermissionModuleResponse>> layDanhSachModule() {
        List<PermissionModuleResponse> modules = roleManagementService.layTatCaModule();
        return ApiResponse.thanhCong(modules, "Lấy danh sách nhóm chức năng thành công");
    }

    /** Tạo nhóm chức năng mới */
    @RequirePermission("RBAC_MODULE_CREATE")
    @PostMapping("/modules")
    public ApiResponse<String> taoModuleMoi(@Valid @RequestBody PermissionModuleRequest request) {
        roleManagementService.taoModuleMoi(request);
        return ApiResponse.thanhCong(null, "Đã tạo nhóm chức năng thành công");
    }

    /** Cập nhật nhóm chức năng */
    @RequirePermission("RBAC_MODULE_UPDATE")
    @PutMapping("/modules/{id}")
    public ApiResponse<String> capNhatModule(@PathVariable Integer id, @Valid @RequestBody PermissionModuleRequest request) {
        roleManagementService.capNhatModule(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật nhóm chức năng thành công");
    }

    /** Xóa nhóm chức năng (chỉ khi không có permission nào đang dùng) */
    @RequirePermission("RBAC_MODULE_DELETE")
    @DeleteMapping("/modules/{id}")
    public ApiResponse<String> xoaModule(@PathVariable Integer id) {
        roleManagementService.xoaModule(id);
        return ApiResponse.thanhCong(null, "Đã xóa nhóm chức năng");
    }
}
