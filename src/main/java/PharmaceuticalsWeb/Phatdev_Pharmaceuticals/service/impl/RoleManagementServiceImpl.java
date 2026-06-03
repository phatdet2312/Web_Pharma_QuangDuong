//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/RoleManagementServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionModuleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionModuleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PermissionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.RoleResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtRolePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PermissionModule;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserPermissionBlacklist;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserPermissionBlacklist.CtUserPermissionBlacklistId;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtRolePermissionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserPermissionBlacklistRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPermissionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPermissionModuleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IRoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * THỰC THI: QUẢN LÝ CHỨC VỤ VÀ QUYỀN HẠT LỰU ĐỘNG
 * =========================================================================
 * Tuân thủ quy tắc: Không dùng Stream API. Dùng mảng toArray(). Lặp For.
 * Bảo vệ sự toàn vẹn của 6 bảng CSDL.
 */
@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements IRoleManagementService {

    private final IUserRoleRepository userRoleRepository;
    private final IPermissionRepository permissionRepository;
    private final ICtRolePermissionRepository ctRolePermissionRepository;
    private final ICtUserRoleRepository ctUserRoleRepository;
    private final ICtUserPermissionBlacklistRepository blacklistRepository;
    private final IUserRepository userRepository;
    private final IAuditService auditService;
    private final IPermissionModuleRepository permissionModuleRepository;

    /**
     * Lấy cấp bậc mạnh nhất của một tài khoản trực tiếp từ DB.
     * Số càng nhỏ quyền càng mạnh; không có role hợp lệ được xem là yếu nhất.
     */
    private int layCapBacManhNhatTuDb(User user) {
        int capBacManhNhat = 999;
        if (user == null || user.getId() == null) {
            return capBacManhNhat;
        }

        List<CtUserRole> roleMaps = ctUserRoleRepository.findByUserId(user.getId());
        if (roleMaps != null) {
            Object[] roleMapArray = roleMaps.toArray();
            for (int i = 0; i < roleMapArray.length; i = i + 1) {
                CtUserRole roleMap = (CtUserRole) roleMapArray[i];
                UserRole role = userRoleRepository.findById(roleMap.getRoleId()).orElse(null);
                if (role != null && role.getRoleLevel() != null && role.getRoleLevel() < capBacManhNhat) {
                    capBacManhNhat = role.getRoleLevel();
                }
            }
        }
        return capBacManhNhat;
    }

    /**
     * Kiểm tra tài khoản hiện tại có đang mang role đích hay không.
     * Dùng để chặn đường vòng tự sửa role đang cấp quyền cho chính mình.
     */
    private boolean actorDangMangRole(User actor, Integer roleId) {
        if (actor == null || actor.getId() == null || roleId == null) {
            return false;
        }

        List<CtUserRole> roleMaps = ctUserRoleRepository.findByUserId(actor.getId());
        if (roleMaps != null) {
            Object[] roleMapArray = roleMaps.toArray();
            for (int i = 0; i < roleMapArray.length; i = i + 1) {
                CtUserRole roleMap = (CtUserRole) roleMapArray[i];
                if (roleId.equals(roleMap.getRoleId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Chặn actor non-level-0 thao tác role mạnh hơn/ngang mình hoặc role đang gán cho chính actor.
     */
    private void damBaoDuocThaoTacRole(User actor, UserRole targetRole) {
        int actorLevel = layCapBacManhNhatTuDb(actor);
        if (actorLevel == 0) {
            return;
        }

        if (actorDangMangRole(actor, targetRole.getId()) == true) {
            throw new AppException(403, "Không được tự chỉnh sửa role đang cấp quyền cho chính tài khoản của bạn");
        }

        int targetLevel = 999;
        if (targetRole.getRoleLevel() != null) {
            targetLevel = targetRole.getRoleLevel();
        }

        if (targetLevel <= actorLevel) {
            throw new AppException(403, "Không được thao tác role mạnh hơn hoặc ngang cấp với bạn");
        }
    }

    /**
     * Chặn actor non-level-0 tạo hoặc lưu role có cấp bậc mạnh hơn/ngang mình.
     */
    private void damBaoDuocLuuRoleLevel(User actor, Integer roleLevel) {
        int levelCanLuu = 3;
        if (roleLevel != null) {
            levelCanLuu = roleLevel;
        }

        if (levelCanLuu < 0) {
            throw new AppException(400, "Cấp bậc role không được âm");
        }

        int actorLevel = layCapBacManhNhatTuDb(actor);
        if (actorLevel == 0) {
            return;
        }

        if (levelCanLuu <= actorLevel) {
            throw new AppException(403, "Không được tạo hoặc lưu role mạnh hơn hoặc ngang cấp với bạn");
        }
    }

    /**
     * Chặn actor non-level-0 quản lý tài khoản có cấp bậc mạnh hơn hoặc ngang mình.
     */
    private void damBaoDuocQuanLyUser(User actor, User targetUser) {
        int actorLevel = layCapBacManhNhatTuDb(actor);
        if (actorLevel == 0) {
            return;
        }

        int targetLevel = layCapBacManhNhatTuDb(targetUser);
        if (targetLevel <= actorLevel) {
            throw new AppException(403, "Không được thay đổi quyền của tài khoản mạnh hơn hoặc ngang cấp với bạn");
        }
    }

    /**
     * Lấy danh sách mã quyền hiệu lực của actor trực tiếp từ DB và loại bỏ blacklist cá nhân.
     */
    private List<String> layPermissionCodeHieuLucTuDb(User actor) {
        List<String> permissionCodes = new ArrayList<>();
        if (actor == null || actor.getId() == null) {
            return permissionCodes;
        }

        List<CtUserRole> roleMaps = ctUserRoleRepository.findByUserId(actor.getId());
        if (roleMaps != null) {
            Object[] roleMapArray = roleMaps.toArray();
            for (int i = 0; i < roleMapArray.length; i = i + 1) {
                CtUserRole roleMap = (CtUserRole) roleMapArray[i];
                List<CtRolePermission> rolePermissions = ctRolePermissionRepository.findByRoleId(roleMap.getRoleId());
                if (rolePermissions != null) {
                    Object[] rolePermArray = rolePermissions.toArray();
                    for (int j = 0; j < rolePermArray.length; j = j + 1) {
                        CtRolePermission rolePermission = (CtRolePermission) rolePermArray[j];
                        Permission permission = permissionRepository.findById(rolePermission.getPermissionId()).orElse(null);
                        if (permission != null && permission.getPermissionCode() != null) {
                            themNeuChuaCo(permissionCodes, permission.getPermissionCode());
                        }
                    }
                }
            }
        }

        List<CtUserPermissionBlacklist> blacklist = blacklistRepository.findByUserId(actor.getId());
        if (blacklist != null) {
            Object[] blacklistArray = blacklist.toArray();
            for (int i = 0; i < blacklistArray.length; i = i + 1) {
                CtUserPermissionBlacklist banGhi = (CtUserPermissionBlacklist) blacklistArray[i];
                Permission permission = permissionRepository.findById(banGhi.getPermissionId()).orElse(null);
                if (permission != null && permission.getPermissionCode() != null) {
                    permissionCodes.remove(permission.getPermissionCode());
                }
            }
        }

        return permissionCodes;
    }

    /**
     * Thêm giá trị vào danh sách nếu chưa có, giữ code dễ đọc và không dùng Set/Stream.
     */
    private void themNeuChuaCo(List<String> danhSach, String giaTri) {
        if (danhSach == null || giaTri == null) {
            return;
        }

        if (danhSachCoGiaTri(danhSach, giaTri) == false) {
            danhSach.add(giaTri);
        }
    }

    /**
     * Kiểm tra danh sách string có chứa giá trị hay không.
     */
    private boolean danhSachCoGiaTri(List<String> danhSach, String giaTri) {
        if (danhSach == null || giaTri == null) {
            return false;
        }

        Object[] mangGiaTri = danhSach.toArray();
        for (int i = 0; i < mangGiaTri.length; i = i + 1) {
            if (giaTri.equals(mangGiaTri[i].toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chặn actor non-level-0 gán permission mà chính actor không có hiệu lực.
     */
    private void damBaoActorCoQuyenDeGanPermission(User actor, Permission permission, List<String> actorPermissionCodes) {
        int actorLevel = layCapBacManhNhatTuDb(actor);
        if (actorLevel == 0) {
            return;
        }

        if (permission == null || permission.getPermissionCode() == null) {
            throw new AppException(400, "Quyền thao tác không hợp lệ");
        }

        if (danhSachCoGiaTri(actorPermissionCodes, permission.getPermissionCode()) == false) {
            throw new AppException(403, "Không được gán quyền mà chính tài khoản của bạn không có: " + permission.getPermissionCode());
        }
    }

    /**
     * Chặn clone role nếu role gốc chứa permission ngoài tập quyền hiệu lực của actor.
     */
    private void damBaoRoleChiChuaQuyenActorCo(User actor, Integer roleId) {
        int actorLevel = layCapBacManhNhatTuDb(actor);
        if (actorLevel == 0) {
            return;
        }

        List<String> actorPermissionCodes = layPermissionCodeHieuLucTuDb(actor);
        List<CtRolePermission> sourceMappings = ctRolePermissionRepository.findByRoleId(roleId);
        if (sourceMappings != null) {
            Object[] mapArray = sourceMappings.toArray();
            for (int i = 0; i < mapArray.length; i = i + 1) {
                CtRolePermission sourceMap = (CtRolePermission) mapArray[i];
                Permission permission = permissionRepository.findById(sourceMap.getPermissionId())
                        .orElseThrow(() -> new AppException(400, "Role gốc chứa quyền thao tác không tồn tại"));
                damBaoActorCoQuyenDeGanPermission(actor, permission, actorPermissionCodes);
            }
        }
    }

    /**
     * Tìm permission bắt buộc theo code, fail rõ ràng nếu client gửi code không tồn tại.
     */
    private Permission timPermissionBatBuoc(String permissionCode) {
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            throw new AppException(400, "Mã quyền trong danh sách gán role không được để trống");
        }

        String codeChuan = permissionCode.toUpperCase().trim().replaceAll("\\s+", "_");
        return permissionRepository.findByPermissionCode(codeChuan)
                .orElseThrow(() -> new AppException(400, "Mã quyền không tồn tại trong hệ thống: " + codeChuan));
    }

    /**
     * Kiểm tra moduleId nếu client có gửi lên khi tạo hoặc cập nhật permission.
     */
    private void damBaoModuleTonTaiNeuCo(Integer moduleId) {
        if (moduleId == null) {
            return;
        }
        if (permissionModuleRepository.findById(moduleId).isEmpty()) {
            throw new AppException(400, "Nhóm chức năng không tồn tại");
        }
    }

    // =====================================================================
    // 1. NGHIỆP VỤ LIÊN QUAN ĐẾN NHÓM CHỨC VỤ (ROLES)
    // =====================================================================
    
    @Override
    public List<RoleResponse> layTatCaChucVu() {
        List<UserRole> tatCaRoles = userRoleRepository.findAll();
        List<RoleResponse> responseList = new ArrayList<>();

        if (tatCaRoles != null) {
            Object[] rolesArray = tatCaRoles.toArray();
            for (int i = 0; i < rolesArray.length; i = i + 1) {
                UserRole role = (UserRole) rolesArray[i];
                
                // Lấy các quyền hạt lựu thuộc về Role này
                List<CtRolePermission> rolePerms = ctRolePermissionRepository.findByRoleId(role.getId());
                List<String> permissionCodes = new ArrayList<>();
                
                if (rolePerms != null) {
                    Object[] permsArray = rolePerms.toArray();
                    for (int j = 0; j < permsArray.length; j = j + 1) {
                        CtRolePermission map = (CtRolePermission) permsArray[j];
                        Permission p = permissionRepository.findById(map.getPermissionId()).orElse(null);
                        if (p != null) {
                            permissionCodes.add(p.getPermissionCode());
                        }
                    }
                }

                // Đếm số người dùng đang mang chức vụ này (dùng cho phân tích tác động xóa)
                List<CtUserRole> nguoiDungCuaRole = ctUserRoleRepository.findByRoleId(role.getId());
                int soNguoiDung = 0;
                if (nguoiDungCuaRole != null) {
                    soNguoiDung = nguoiDungCuaRole.size();
                }

                // Đóng gói thành DTO
                RoleResponse dto = new RoleResponse(
                        role.getId(),
                        role.getRoleName(),
                        role.getRoleLevel(),
                        role.getDescription(),
                        permissionCodes,
                        soNguoiDung
                );
                responseList.add(dto);
            }
        }
        return responseList;
    }

    @Override
    @Transactional
    public void taoChucVuMoi(RoleRequest request, User currentUser) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new AppException(400, "Tên chức vụ không được để trống");
        }

        String roleNameUpper = request.getRoleName().toUpperCase().trim();
        
        if (userRoleRepository.findByRoleName(roleNameUpper).isPresent()) {
            throw new AppException(400, "Chức vụ này đã tồn tại trong hệ thống");
        }

        Integer roleLevel = request.getRoleLevel() != null ? request.getRoleLevel() : 3;
        damBaoDuocLuuRoleLevel(currentUser, roleLevel);

        // Bước A: Tạo Nhóm Quyền mới vào DB
        UserRole newRole = new UserRole();
        newRole.setRoleName(roleNameUpper);
        // Nếu không truyền level, tự động gán cấp thấp nhất là 3
        newRole.setRoleLevel(roleLevel);
        newRole.setDescription(request.getDescription());
        
        newRole = userRoleRepository.save(newRole);

        // Bước B: Nối các quyền hạt lựu (Permissions) vào Nhóm Quyền này
        List<String> actorPermissionCodes = layPermissionCodeHieuLucTuDb(currentUser);
        if (request.getPermissions() != null) {
            Object[] requestedPerms = request.getPermissions().toArray();
            for (int i = 0; i < requestedPerms.length; i = i + 1) {
                String pCode = requestedPerms[i].toString();
                Permission p = timPermissionBatBuoc(pCode);
                damBaoActorCoQuyenDeGanPermission(currentUser, p, actorPermissionCodes);
                CtRolePermission map = new CtRolePermission(newRole.getId(), p.getId());
                ctRolePermissionRepository.save(map);
            }
        }
    }

    @Override
    @Transactional
    public void capNhatChucVu(Integer roleId, RoleRequest request, User currentUser) {
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ"));

        damBaoDuocThaoTacRole(currentUser, role);

        // Cập nhật thông tin cơ bản
        if (request.getRoleLevel() != null) {
            damBaoDuocLuuRoleLevel(currentUser, request.getRoleLevel());
            role.setRoleLevel(request.getRoleLevel());
        }
        role.setDescription(request.getDescription());
        userRoleRepository.save(role);

        // Bước C: Cập nhật lại danh sách Quyền Hạt Lựu
        // 1. Xóa toàn bộ liên kết cũ trong bảng trung gian CT_ROLE_PERMISSIONS
        List<CtRolePermission> oldMappings = ctRolePermissionRepository.findByRoleId(roleId);
        if (oldMappings != null) {
            Object[] oldMapsArray = oldMappings.toArray();
            for (int i = 0; i < oldMapsArray.length; i = i + 1) {
                CtRolePermission m = (CtRolePermission) oldMapsArray[i];
                ctRolePermissionRepository.delete(m);
            }
        }

        // 2. Lắp các liên kết quyền hạt lựu mới vào
        List<String> actorPermissionCodes = layPermissionCodeHieuLucTuDb(currentUser);
        if (request.getPermissions() != null) {
            Object[] newPermsArray = request.getPermissions().toArray();
            for (int i = 0; i < newPermsArray.length; i = i + 1) {
                String pCode = newPermsArray[i].toString();
                Permission p = timPermissionBatBuoc(pCode);
                damBaoActorCoQuyenDeGanPermission(currentUser, p, actorPermissionCodes);
                CtRolePermission map = new CtRolePermission(role.getId(), p.getId());
                ctRolePermissionRepository.save(map);
            }
        }
    }

    @Override
    @Transactional
    public void xoaChucVu(Integer roleId, User currentUser) {
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ"));

        damBaoDuocThaoTacRole(currentUser, role);

        // Kiểm tra xem có User nào đang mang chức vụ này không (Chống mồ côi dữ liệu)
        // Áp dụng Động cơ Phân tích Tác động (Impact Analysis Engine)
        List<CtUserRole> usersWithRole = ctUserRoleRepository.findByRoleId(roleId);
        if (usersWithRole != null && usersWithRole.isEmpty() == false) {
            int soLuongAnhHuong = usersWithRole.size();
            throw new AppException(400, "Từ chối xóa: Chức vụ này đang được gán cho " + soLuongAnhHuong + " đối tác/nhân viên. Xóa sẽ làm mất quyền truy cập của họ.");
        }

        // Xóa liên kết quyền hạt lựu trước (Dọn dẹp bảng trung gian)
        List<CtRolePermission> oldMappings = ctRolePermissionRepository.findByRoleId(roleId);
        if (oldMappings != null) {
            Object[] oldMapsArray = oldMappings.toArray();
            for (int i = 0; i < oldMapsArray.length; i = i + 1) {
                CtRolePermission m = (CtRolePermission) oldMapsArray[i];
                ctRolePermissionRepository.delete(m);
            }
        }

        // Cuối cùng mới xóa chức vụ
        userRoleRepository.delete(role);
    }

    @Override
    @Transactional
    public void nhanBanChucVu(Integer sourceRoleId, String tenChucVuBanSao, User currentUser) {
        if (tenChucVuBanSao == null || tenChucVuBanSao.trim().isEmpty()) {
            throw new AppException(400, "Tên chức vụ bản sao không được để trống");
        }

        UserRole sourceRole = userRoleRepository.findById(sourceRoleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ gốc để nhân bản"));

        damBaoDuocThaoTacRole(currentUser, sourceRole);
        damBaoDuocLuuRoleLevel(currentUser, sourceRole.getRoleLevel());
        damBaoRoleChiChuaQuyenActorCo(currentUser, sourceRoleId);

        String roleNameUpper = tenChucVuBanSao.toUpperCase().trim();
        
        if (userRoleRepository.findByRoleName(roleNameUpper).isPresent()) {
            throw new AppException(400, "Tên chức vụ bản sao đã tồn tại trong hệ thống");
        }

        // Tạo chức vụ bản sao
        UserRole cloneRole = new UserRole();
        cloneRole.setRoleName(roleNameUpper);
        cloneRole.setRoleLevel(sourceRole.getRoleLevel());
        cloneRole.setDescription("Bản sao của " + sourceRole.getRoleName());
        cloneRole = userRoleRepository.save(cloneRole);

        // Sao chép toàn bộ quyền hạt lựu
        List<CtRolePermission> sourceMappings = ctRolePermissionRepository.findByRoleId(sourceRoleId);
        if (sourceMappings != null) {
            Object[] mapArray = sourceMappings.toArray();
            for (int i = 0; i < mapArray.length; i = i + 1) {
                CtRolePermission sourceMap = (CtRolePermission) mapArray[i];
                CtRolePermission cloneMap = new CtRolePermission(cloneRole.getId(), sourceMap.getPermissionId());
                ctRolePermissionRepository.save(cloneMap);
            }
        }
    }

    // =====================================================================
    // 2. NGHIỆP VỤ LIÊN QUAN ĐẾN QUYỀN HẠT LỰU (PERMISSIONS)
    // =====================================================================
    
    @Override
    public List<PermissionResponse> layTatCaQuyenHatLuu() {
        List<Permission> tatCaQuyen = permissionRepository.findAll();
        List<PermissionResponse> responseList = new ArrayList<>();

        // Nạp toàn bộ module 1 lần duy nhất, xây Map tra cứu — tránh N+1 query
        List<PermissionModule> tatCaModule = permissionModuleRepository.findAll();
        java.util.Map<Integer, PermissionModule> mapModule = new java.util.HashMap<>();
        Object[] moduleArray = tatCaModule.toArray();
        for (int m = 0; m < moduleArray.length; m = m + 1) {
            PermissionModule mod = (PermissionModule) moduleArray[m];
            mapModule.put(mod.getId(), mod);
        }

        if (tatCaQuyen != null) {
            Object[] permsArray = tatCaQuyen.toArray();
            for (int i = 0; i < permsArray.length; i = i + 1) {
                Permission p = (Permission) permsArray[i];
                PermissionResponse dto = PermissionResponse.fromEntity(p);

                // Tra cứu module từ Map đã nạp sẵn (O(1) thay vì query DB)
                if (p.getModuleId() != null) {
                    PermissionModule mod = mapModule.get(p.getModuleId());
                    if (mod != null) {
                        dto.setModuleCode(mod.getModuleCode());
                        dto.setModuleName(mod.getModuleName());
                    }
                }

                // Thuật toán gắn cờ rủi ro (Risk Analysis Engine)
                String code = p.getPermissionCode().toUpperCase();
                boolean laQuyenRuiRo = false;

                if (code.contains("DELETE") || code.contains("REMOVE") || code.contains("DESTROY") || code.contains("LOCK") || code.contains("BLACKLIST") || code.contains("EXPORT")) {
                    laQuyenRuiRo = true;
                }

                if (laQuyenRuiRo == true) {
                    dto.setRiskLevel("DANGER");
                } else {
                    dto.setRiskLevel("SAFE");
                }

                responseList.add(dto);
            }
        }
        return responseList;
    }

    @Override
    @Transactional
    public void taoQuyenMoi(PermissionRequest request) {
        if (request.getPermissionCode() == null || request.getPermissionCode().trim().isEmpty()) {
            throw new AppException(400, "Mã quyền không được để trống");
        }
        damBaoModuleTonTaiNeuCo(request.getModuleId());

        // Chuẩn hóa mã quyền: Viết hoa, thay khoảng trắng bằng dấu gạch dưới
        String codeChuan = request.getPermissionCode().toUpperCase().trim().replaceAll("\\s+", "_");

        if (permissionRepository.findByPermissionCode(codeChuan).isPresent()) {
            throw new AppException(400, "Mã quyền này đã tồn tại trong hệ thống");
        }

        Permission p = new Permission();
        p.setPermissionCode(codeChuan);
        p.setDescription(request.getDescription());
        p.setModuleId(request.getModuleId());

        permissionRepository.save(p);
    }

    @Override
    @Transactional
    public void capNhatQuyen(Integer permissionId, PermissionRequest request) {
        Permission p = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy quyền hạt lựu này"));
        damBaoModuleTonTaiNeuCo(request.getModuleId());

        // Nếu client có gửi mã quyền mới lên, ta cần kiểm tra trùng lặp
        if (request.getPermissionCode() != null && request.getPermissionCode().trim().isEmpty() == false) {
            String codeMoi = request.getPermissionCode().toUpperCase().trim().replaceAll("\\s+", "_");
            
            // Nếu mã mới khác mã cũ, phải check xem có đụng hàng với quyền khác không
            if (codeMoi.equals(p.getPermissionCode()) == false) {
                if (permissionRepository.findByPermissionCode(codeMoi).isPresent()) {
                    throw new AppException(400, "Mã quyền mới bị trùng với một quyền đã có");
                }
                p.setPermissionCode(codeMoi);
            }
        }

        p.setDescription(request.getDescription());
        p.setModuleId(request.getModuleId());
        permissionRepository.save(p);
    }

    @Override
    @Transactional
    public void xoaQuyen(Integer permissionId) {
        Permission p = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy quyền hạt lựu này"));

        // Kiểm tra xem Quyền này có đang được gán cho Chức vụ nào không (Chống mồ côi)
        List<CtRolePermission> allMappings = ctRolePermissionRepository.findAll();
        boolean dangDuocSuDung = false;
        
        if (allMappings != null) {
            Object[] mapArray = allMappings.toArray();
            for (int i = 0; i < mapArray.length; i = i + 1) {
                CtRolePermission map = (CtRolePermission) mapArray[i];
                if (map.getPermissionId().equals(permissionId)) {
                    dangDuocSuDung = true;
                    break;
                }
            }
        }

        if (dangDuocSuDung == true) {
            throw new AppException(400, "Từ chối xóa: Quyền này đang được gán cho một số chức vụ. Hãy gỡ quyền khỏi chức vụ trước!");
        }

        permissionRepository.delete(p);
    }

    // =====================================================================
    // 3. KIỂM SOÁT QUYỀN HẠT LỰU CỤC BỘ (BLACKLIST)
    // =====================================================================
    
    @Override
    public List<Integer> layBlacklistPermissionCuaUser(Long userId) {
        List<CtUserPermissionBlacklist> blacklist = blacklistRepository.findByUserId(userId);
        List<Integer> danhSachPermissionIdBiCam = new ArrayList<>();

        if (blacklist != null) {
            Object[] mangBlacklist = blacklist.toArray();
            for (int i = 0; i < mangBlacklist.length; i = i + 1) {
                CtUserPermissionBlacklist banGhi = (CtUserPermissionBlacklist) mangBlacklist[i];
                danhSachPermissionIdBiCam.add(banGhi.getPermissionId());
            }
        }
        return danhSachPermissionIdBiCam;
    }

    @Override
    public List<Permission> layTatCaQuyenEntity() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional
    public void togglePermissionBlacklist(Long targetUserId, UserBlacklistRequest request, User currentUser) {
        
        if (targetUserId.equals(currentUser.getId())) {
            throw new AppException(403, "Bạn không thể tự tước quyền của chính mình.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(404, "Tài khoản không tồn tại."));
        damBaoDuocQuanLyUser(currentUser, targetUser);

        Permission p = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new AppException(404, "Quyền hạt lựu không tồn tại."));

        CtUserPermissionBlacklistId blacklistId = new CtUserPermissionBlacklistId(targetUserId, p.getId());

        if (request.isBanned() == true) {
            if (blacklistRepository.existsById(blacklistId) == false) {
                CtUserPermissionBlacklist banRecord = new CtUserPermissionBlacklist();
                banRecord.setUserId(targetUserId);
                banRecord.setPermissionId(p.getId());
                blacklistRepository.save(banRecord);
                
                auditService.logAction(targetUserId, "BLACKLIST_PERM", p.getId(), currentUser.getId(), request.getReason());
            }
        } else {
            if (blacklistRepository.existsById(blacklistId) == true) {
                blacklistRepository.deleteById(blacklistId);

                auditService.logAction(targetUserId, "UNBLACKLIST_PERM", p.getId(), currentUser.getId(), request.getReason());
            }
        }
    }

    // =====================================================================
    // 4. NGHIỆP VỤ QUẢN LÝ NHÓM CHỨC NĂNG (PERMISSION MODULES)
    // =====================================================================

    // Lấy toàn bộ nhóm chức năng, chuyển đổi sang DTO trả về cho Controller
    @Override
    public List<PermissionModuleResponse> layTatCaModule() {
        List<PermissionModule> modules = permissionModuleRepository.findAllByOrderByDisplayOrderAsc();
        List<PermissionModuleResponse> responseList = new ArrayList<>();

        Object[] modulesArray = modules.toArray();
        for (int i = 0; i < modulesArray.length; i = i + 1) {
            PermissionModule mod = (PermissionModule) modulesArray[i];
            responseList.add(PermissionModuleResponse.fromEntity(mod));
        }

        return responseList;
    }

    // Tạo nhóm chức năng mới sau khi kiểm tra trùng mã module
    @Override
    @Transactional
    public void taoModuleMoi(PermissionModuleRequest request) {
        String moduleCode = request.getModuleCode().toUpperCase().trim();

        if (permissionModuleRepository.findByModuleCode(moduleCode).isPresent()) {
            throw new AppException(400, "Mã module đã tồn tại");
        }

        PermissionModule mod = new PermissionModule();
        mod.setModuleCode(moduleCode);
        mod.setModuleName(request.getModuleName().trim());
        mod.setDescription(request.getDescription());
        mod.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        permissionModuleRepository.save(mod);
    }

    // Cập nhật thông tin nhóm chức năng theo ID
    @Override
    @Transactional
    public void capNhatModule(Integer moduleId, PermissionModuleRequest request) {
        PermissionModule mod = permissionModuleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy nhóm chức năng"));

        String moduleCode = request.getModuleCode().toUpperCase().trim();
        if (moduleCode.equals(mod.getModuleCode()) == false) {
            PermissionModule moduleTrung = permissionModuleRepository.findByModuleCode(moduleCode).orElse(null);
            if (moduleTrung != null && moduleTrung.getId().equals(moduleId) == false) {
                throw new AppException(400, "Mã module đã tồn tại");
            }
            mod.setModuleCode(moduleCode);
        }
        mod.setModuleName(request.getModuleName().trim());
        mod.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) {
            mod.setDisplayOrder(request.getDisplayOrder());
        }
        permissionModuleRepository.save(mod);
    }

    // Xóa nhóm chức năng (từ chối nếu còn quyền hạt lựu đang thuộc nhóm)
    @Override
    @Transactional
    public void xoaModule(Integer moduleId) {
        PermissionModule mod = permissionModuleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy nhóm chức năng"));

        List<Permission> tatCaQuyen = permissionRepository.findAll();
        Object[] quyenArray = tatCaQuyen.toArray();
        for (int i = 0; i < quyenArray.length; i = i + 1) {
            Permission p = (Permission) quyenArray[i];
            if (p.getModuleId() != null && p.getModuleId().equals(moduleId)) {
                throw new AppException(400, "Từ chối xóa: Nhóm này đang chứa quyền hạt lựu. Hãy chuyển quyền sang nhóm khác trước.");
            }
        }

        permissionModuleRepository.delete(mod);
    }
}
