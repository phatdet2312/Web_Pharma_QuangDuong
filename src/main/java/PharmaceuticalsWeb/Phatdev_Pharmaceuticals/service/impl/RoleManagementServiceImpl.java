//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/RoleManagementServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PermissionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.RoleRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UserBlacklistRequest;
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
    public void taoChucVuMoi(RoleRequest request) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new AppException(400, "Tên chức vụ không được để trống");
        }

        String roleNameUpper = request.getRoleName().toUpperCase().trim();
        
        if (userRoleRepository.findByRoleName(roleNameUpper).isPresent()) {
            throw new AppException(400, "Chức vụ này đã tồn tại trong hệ thống");
        }

        // Bước A: Tạo Nhóm Quyền mới vào DB
        UserRole newRole = new UserRole();
        newRole.setRoleName(roleNameUpper);
        // Nếu không truyền level, tự động gán cấp thấp nhất là 3
        newRole.setRoleLevel(request.getRoleLevel() != null ? request.getRoleLevel() : 3);
        newRole.setDescription(request.getDescription());
        
        newRole = userRoleRepository.save(newRole);

        // Bước B: Nối các quyền hạt lựu (Permissions) vào Nhóm Quyền này
        if (request.getPermissions() != null) {
            Object[] requestedPerms = request.getPermissions().toArray();
            for (int i = 0; i < requestedPerms.length; i = i + 1) {
                String pCode = requestedPerms[i].toString();
                Permission p = permissionRepository.findByPermissionCode(pCode).orElse(null);
                
                if (p != null) {
                    CtRolePermission map = new CtRolePermission(newRole.getId(), p.getId());
                    ctRolePermissionRepository.save(map);
                }
            }
        }
    }

    @Override
    @Transactional
    public void capNhatChucVu(Integer roleId, RoleRequest request) {
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ"));

        if ("SUPERADMIN".equals(role.getRoleName())) {
            throw new AppException(403, "Không được phép chỉnh sửa chức vụ tối cao SUPERADMIN");
        }

        // Cập nhật thông tin cơ bản
        if (request.getRoleLevel() != null) {
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
        if (request.getPermissions() != null) {
            Object[] newPermsArray = request.getPermissions().toArray();
            for (int i = 0; i < newPermsArray.length; i = i + 1) {
                String pCode = newPermsArray[i].toString();
                Permission p = permissionRepository.findByPermissionCode(pCode).orElse(null);
                
                if (p != null) {
                    CtRolePermission map = new CtRolePermission(role.getId(), p.getId());
                    ctRolePermissionRepository.save(map);
                }
            }
        }
    }

    @Override
    @Transactional
    public void xoaChucVu(Integer roleId) {
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ"));

        // Bảo vệ chức vụ tối cao (roleLevel = 0) không bị xóa nhầm
        if (role.getRoleLevel() != null && role.getRoleLevel() == 0) {
            throw new AppException(403, "Không được xóa chức vụ tối cao của hệ thống");
        }

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
    public void nhanBanChucVu(Integer sourceRoleId, String tenChucVuBanSao) {
        if (tenChucVuBanSao == null || tenChucVuBanSao.trim().isEmpty()) {
            throw new AppException(400, "Tên chức vụ bản sao không được để trống");
        }

        UserRole sourceRole = userRoleRepository.findById(sourceRoleId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy chức vụ gốc để nhân bản"));

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

        if (tatCaQuyen != null) {
            Object[] permsArray = tatCaQuyen.toArray();
            for (int i = 0; i < permsArray.length; i = i + 1) {
                Permission p = (Permission) permsArray[i];
                PermissionResponse dto = PermissionResponse.fromEntity(p);

                // Nạp thông tin module nếu permission có moduleId
                if (p.getModuleId() != null) {
                    PermissionModule mod = permissionModuleRepository.findById(p.getModuleId()).orElse(null);
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
}