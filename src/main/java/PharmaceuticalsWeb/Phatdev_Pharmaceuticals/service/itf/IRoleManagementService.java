//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IRoleManagementService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionModuleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionModuleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN: QUẢN LÝ CHỨC VỤ & QUYỀN THAO TÁC ĐỘNG (CHUẨN MÙ)
 * =========================================================================
 * Cung cấp hợp đồng cho API Controller gọi đến, hoàn toàn giấu kín 
 * thuật toán lặp 6 bảng bên trong thư mục impl.
 */
public interface IRoleManagementService {
    
    // =====================================================================
    // PHẦN 1: QUẢN LÝ NHÓM CHỨC VỤ (ROLES)
    // =====================================================================
    
    // Lấy toàn bộ danh sách chức vụ kèm theo các quyền thao tác bên trong
    List<RoleResponse> layTatCaChucVu();
    
    // Lưu một chức vụ vào CSDL
    void taoChucVuMoi(RoleRequest request, User currentUser);
    
    // Cập nhật cấp bậc, mô tả hoặc thay đổi danh sách quyền thao tác của 1 chức vụ
    void capNhatChucVu(Integer roleId, RoleRequest request, User currentUser);
    
    // Xóa vĩnh viễn một chức vụ (Cần kiểm tra có ai đang giữ chức này không)
    void xoaChucVu(Integer roleId, User currentUser);

    /**
     * Nghiệp vụ nhân bản chức vụ.
     * Tạo ra một Chức vụ sao chép kế thừa toàn bộ quyền thao tác từ Chức vụ gốc.
     */
    void nhanBanChucVu(Integer sourceRoleId, String tenChucVuBanSao, User currentUser);
    
    // =====================================================================
    // PHẦN 2: QUẢN LÝ QUYỀN THAO TÁC (PERMISSIONS)
    // =====================================================================
    
    // Lấy danh mục các quyền thao tác gốc (Dùng để vẽ danh sách Checkbox và Quản lý)
    List<PermissionResponse> layTatCaQuyenHatLuu();
    
    // Tạo ra một đặc quyền thao tác hoàn toàn vào CSDL
    void taoQuyenMoi(PermissionRequest request);
    
    // Cập nhật lại mô tả hoặc mã của Quyền (kiểm tra cấp bậc actor trước khi sửa)
    void capNhatQuyen(Integer permissionId, PermissionRequest request, User currentUser);

    // Xóa Quyền (kiểm tra cấp bậc actor + chỉ cho phép xóa nếu quyền chưa được gắn chức vụ)
    void xoaQuyen(Integer permissionId, User currentUser);

    // =====================================================================
    // PHẦN 3: KIỂM SOÁT QUYỀN THAO TÁC CỤC BỘ (BLACKLIST)
    // =====================================================================
    
    /**
     * Đóng băng hoặc cấp lại quyền thao tác cụ thể cho cá nhân.
     */
    void togglePermissionBlacklist(Long targetUserId, UserBlacklistRequest request, User currentUser);

    /**
     * Truy xuất danh sách ID các quyền đang bị đóng băng của một tài khoản.
     * Frontend dùng để vẽ trạng thái toggle blacklist.
     */
    List<Integer> layBlacklistPermissionCuaUser(Long userId);

    /**
     * Lấy danh sách Entity Permission gốc (dùng khi cần kiểm tra FK trước xóa module).
     */
    List<Permission> layTatCaQuyenEntity();

    // =====================================================================
    // PHẦN 4: QUẢN LÝ NHÓM CHỨC NĂNG (PERMISSION MODULES)
    // =====================================================================

    // Lấy toàn bộ nhóm chức năng, sắp xếp theo thứ tự hiển thị
    List<PermissionModuleResponse> layTatCaModule();

    // Tạo nhóm chức năng mới (kiểm tra trùng mã module)
    void taoModuleMoi(PermissionModuleRequest request);

    // Cập nhật tên, mô tả, thứ tự hiển thị của nhóm chức năng (kiểm tra cấp bậc actor)
    void capNhatModule(Integer moduleId, PermissionModuleRequest request, User currentUser);

    // Xóa nhóm chức năng (kiểm tra cấp bậc actor + chỉ khi không còn permission thuộc nhóm)
    void xoaModule(Integer moduleId, User currentUser);
}
