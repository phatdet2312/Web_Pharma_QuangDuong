//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiRoleManagementController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.PermissionRegistry;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.MyPermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionModuleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PermissionModule;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPermissionModuleRepository;
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
 * API CONTROLLER: QUẢN LÝ CHỨC VỤ ĐỘNG VÀ QUYỀN HẠT LỰU
 * =========================================================================
 */
@RestController
@RequestMapping("/api/admin/role-management")
@RequiredArgsConstructor
public class ApiRoleManagementController {

    private final IRoleManagementService roleManagementService;
    private final IUserService userService;
    private final IPermissionModuleRepository permissionModuleRepository;

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

        // Kiểm tra SUPERADMIN: roleName hoặc roleLevel = 0
        boolean laSuperAdmin = false;
        int roleLevel = 999;

        List<String> danhSachRole = currentUser.getDanhSachTenRole();
        if (danhSachRole != null) {
            Object[] roleArray = danhSachRole.toArray();
            for (int i = 0; i < roleArray.length; i = i + 1) {
                if ("SUPERADMIN".equals(roleArray[i].toString())) {
                    laSuperAdmin = true;
                }
            }
        }

        // Lấy roleLevel cao nhất (số nhỏ = quyền lớn) từ IUserService
        roleLevel = userService.layCapBacQuyenLucCaoNhat(currentUser);
        if (roleLevel == 0) {
            laSuperAdmin = true;
        }

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

    @RequirePermission("ROLE_MANAGE")
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> layDanhSachChucVu() {
        List<RoleResponse> roles = roleManagementService.layTatCaChucVu();
        return ApiResponse.thanhCong(roles, "Lấy danh sách chức vụ thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PostMapping("/roles")
    public ApiResponse<String> taoChucVuMoi(@Valid @RequestBody RoleRequest request) {
        roleManagementService.taoChucVuMoi(request);
        return ApiResponse.thanhCong(null, "Đã tạo chức vụ thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PutMapping("/roles/{id}")
    public ApiResponse<String> capNhatChucVu(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        roleManagementService.capNhatChucVu(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật chức vụ thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @DeleteMapping("/roles/{id}")
    public ApiResponse<String> xoaChucVu(@PathVariable Integer id) {
        roleManagementService.xoaChucVu(id);
        return ApiResponse.thanhCong(null, "Xóa chức vụ thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PostMapping("/roles/{id}/clone")
    public ApiResponse<String> nhanBanChucVu(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String tenChucVuBanSao = body.get("roleName");
        roleManagementService.nhanBanChucVu(id, tenChucVuBanSao);
        return ApiResponse.thanhCong(null, "Nhân bản chức vụ thành công");
    }

    // =====================================================================
    // PHẦN 2: API QUẢN LÝ QUYỀN HẠT LỰU VÀ BLACKLIST
    // =====================================================================

    @RequirePermission("ROLE_MANAGE")
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> layDanhSachQuyenHatLuu() {
        List<PermissionResponse> permissions = roleManagementService.layTatCaQuyenHatLuu();
        return ApiResponse.thanhCong(permissions, "Lấy danh sách quyền hạt lựu thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PostMapping("/permissions")
    public ApiResponse<String> taoQuyenMoi(@Valid @RequestBody PermissionRequest request) {
        roleManagementService.taoQuyenMoi(request);
        return ApiResponse.thanhCong(null, "Đã khởi tạo quyền thao tác thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PutMapping("/permissions/{id}")
    public ApiResponse<String> capNhatQuyen(@PathVariable Integer id, @Valid @RequestBody PermissionRequest request) {
        roleManagementService.capNhatQuyen(id, request);
        return ApiResponse.thanhCong(null, "Cập nhật quyền thao tác thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @DeleteMapping("/permissions/{id}")
    public ApiResponse<String> xoaQuyen(@PathVariable Integer id) {
        roleManagementService.xoaQuyen(id);
        return ApiResponse.thanhCong(null, "Đã xóa quyền thao tác khỏi hệ thống");
    }

    @RequirePermission("ROLE_MANAGE")
    @GetMapping("/blacklist/users/{userId}")
    public ApiResponse<List<Integer>> layBlacklistCuaUser(@PathVariable Long userId) {
        List<Integer> danhSachBiCam = roleManagementService.layBlacklistPermissionCuaUser(userId);
        return ApiResponse.thanhCong(danhSachBiCam, "Lấy danh sách quyền bị đóng băng thành công");
    }

    @RequirePermission("ROLE_MANAGE")
    @PostMapping("/blacklist/users/{userId}")
    public ApiResponse<String> toggleBlacklist(@PathVariable Long userId, @RequestBody UserBlacklistRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        roleManagementService.togglePermissionBlacklist(userId, request, currentUser);
        return ApiResponse.thanhCong(null, "Đã thực thi lệnh kiểm soát quyền hạt lựu");
    }

    // =====================================================================
    // PHẦN 3: API QUẢN LÝ NHÓM CHỨC NĂNG (PERMISSION MODULES)
    // =====================================================================

    /** Lấy danh sách tất cả nhóm chức năng (sắp xếp theo thứ tự hiển thị) */
    @RequirePermission("ROLE_MANAGE")
    @GetMapping("/modules")
    public ApiResponse<List<PermissionModuleResponse>> layDanhSachModule() {
        List<PermissionModule> modules = permissionModuleRepository.findAllByOrderByDisplayOrderAsc();
        List<PermissionModuleResponse> responseList = new ArrayList<>();

        Object[] modulesArray = modules.toArray();
        for (int i = 0; i < modulesArray.length; i = i + 1) {
            PermissionModule mod = (PermissionModule) modulesArray[i];
            responseList.add(PermissionModuleResponse.fromEntity(mod));
        }

        return ApiResponse.thanhCong(responseList, "Lấy danh sách nhóm chức năng thành công");
    }

    /** Tạo nhóm chức năng mới */
    @RequirePermission("ROLE_MANAGE")
    @PostMapping("/modules")
    public ApiResponse<String> taoModuleMoi(@RequestBody Map<String, Object> body) {
        String moduleCode = body.get("moduleCode") != null ? body.get("moduleCode").toString().toUpperCase().trim() : "";
        String moduleName = body.get("moduleName") != null ? body.get("moduleName").toString().trim() : "";

        if (moduleCode.isEmpty() || moduleName.isEmpty()) {
            throw new PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException(400, "Mã module và tên hiển thị không được để trống");
        }

        if (permissionModuleRepository.findByModuleCode(moduleCode).isPresent()) {
            throw new PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException(400, "Mã module đã tồn tại");
        }

        PermissionModule mod = new PermissionModule();
        mod.setModuleCode(moduleCode);
        mod.setModuleName(moduleName);
        mod.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        mod.setDisplayOrder(body.get("displayOrder") != null ? Integer.parseInt(body.get("displayOrder").toString()) : 0);
        permissionModuleRepository.save(mod);

        return ApiResponse.thanhCong(null, "Đã tạo nhóm chức năng thành công");
    }

    /** Cập nhật nhóm chức năng */
    @RequirePermission("ROLE_MANAGE")
    @PutMapping("/modules/{id}")
    public ApiResponse<String> capNhatModule(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        PermissionModule mod = permissionModuleRepository.findById(id)
                .orElseThrow(() -> new PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException(404, "Không tìm thấy nhóm chức năng"));

        if (body.get("moduleName") != null) {
            mod.setModuleName(body.get("moduleName").toString().trim());
        }
        if (body.get("description") != null) {
            mod.setDescription(body.get("description").toString());
        }
        if (body.get("displayOrder") != null) {
            mod.setDisplayOrder(Integer.parseInt(body.get("displayOrder").toString()));
        }
        permissionModuleRepository.save(mod);

        return ApiResponse.thanhCong(null, "Cập nhật nhóm chức năng thành công");
    }

    /** Xóa nhóm chức năng (chỉ khi không có permission nào đang dùng) */
    @RequirePermission("ROLE_MANAGE")
    @DeleteMapping("/modules/{id}")
    public ApiResponse<String> xoaModule(@PathVariable Integer id) {
        PermissionModule mod = permissionModuleRepository.findById(id)
                .orElseThrow(() -> new PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException(404, "Không tìm thấy nhóm chức năng"));

        // Kiểm tra xem có permission nào đang thuộc module này không
        List<PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission> tatCaQuyen = roleManagementService.layTatCaQuyenEntity();
        Object[] quyenArray = tatCaQuyen.toArray();
        for (int i = 0; i < quyenArray.length; i = i + 1) {
            PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission p = (PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission) quyenArray[i];
            if (p.getModuleId() != null && p.getModuleId().equals(id)) {
                throw new PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException(400, "Từ chối xóa: Nhóm này đang chứa quyền hạt lựu. Hãy chuyển quyền sang nhóm khác trước.");
            }
        }

        permissionModuleRepository.delete(mod);
        return ApiResponse.thanhCong(null, "Đã xóa nhóm chức năng");
    }
}