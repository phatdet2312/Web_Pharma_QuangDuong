//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IRoleManagementService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN: QUẢN LÝ CHỨC VỤ & QUYỀN HẠT LỰU ĐỘNG (CHUẨN MÙ)
 * =========================================================================
 * Cung cấp hợp đồng cho API Controller gọi đến, hoàn toàn giấu kín 
 * thuật toán lặp 6 bảng bên trong thư mục impl.
 */
public interface IRoleManagementService {
    
    // =====================================================================
    // PHẦN 1: QUẢN LÝ NHÓM CHỨC VỤ (ROLES)
    // =====================================================================
    
    // Lấy toàn bộ danh sách chức vụ kèm theo các quyền hạt lựu bên trong
    List<RoleResponse> layTatCaChucVu();
    
    // Lưu một chức vụ vào CSDL
    void taoChucVuMoi(RoleRequest request);
    
    // Cập nhật cấp bậc, mô tả hoặc thay đổi danh sách quyền hạt lựu của 1 chức vụ
    void capNhatChucVu(Integer roleId, RoleRequest request);
    
    // Xóa vĩnh viễn một chức vụ (Cần kiểm tra có ai đang giữ chức này không)
    void xoaChucVu(Integer roleId);

    /**
     * Nghiệp vụ nhân bản chức vụ.
     * Tạo ra một Chức vụ sao chép kế thừa toàn bộ quyền hạt lựu từ Chức vụ gốc.
     */
    void nhanBanChucVu(Integer sourceRoleId, String tenChucVuBanSao);
    
    // =====================================================================
    // PHẦN 2: QUẢN LÝ QUYỀN HẠT LỰU (PERMISSIONS)
    // =====================================================================
    
    // Lấy danh mục các quyền hạt lựu gốc (Dùng để vẽ danh sách Checkbox và Quản lý)
    List<PermissionResponse> layTatCaQuyenHatLuu();
    
    // Tạo ra một đặc quyền thao tác hoàn toàn vào CSDL
    void taoQuyenMoi(PermissionRequest request);
    
    // Cập nhật lại mô tả hoặc mã của Quyền
    void capNhatQuyen(Integer permissionId, PermissionRequest request);
    
    // Xóa Quyền (Chỉ cho phép xóa nếu quyền này chưa được gắn cho bất kỳ Chức vụ nào)
    void xoaQuyen(Integer permissionId);

    // =====================================================================
    // PHẦN 3: KIỂM SOÁT QUYỀN HẠT LỰU CỤC BỘ (BLACKLIST)
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
}