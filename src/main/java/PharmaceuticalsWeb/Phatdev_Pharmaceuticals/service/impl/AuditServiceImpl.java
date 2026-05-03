//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AuditServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AuditLogPageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AuditLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ModerationActionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserModerationLog;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.ModerationAction;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserModerationLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IModerationActionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPermissionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * THỰC THI: DỊCH VỤ KIỂM TOÁN BẢO MẬT
 * =========================================================================
 * Xử lý logic ghi log và đóng gói dữ liệu an toàn.
 * Tuân thủ tuyệt đối Quy tắc: Không dùng Stream API, dùng vòng lặp For truyền thống.
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private final ICtUserModerationLogRepository logRepository;
    private final IModerationActionRepository actionRepository;
    private final IUserRepository userRepository;
    private final IPermissionRepository permissionRepository;

    @Override
    public void logAction(Long targetUserId, String actionCode, Integer permissionId, Long moderatorId, String reason) {
        
        // 1. Xác thực Mã hành vi
        ModerationAction action = actionRepository.findByCode(actionCode)
                .orElseThrow(() -> new AppException(500, "Mã hành vi kiểm duyệt không tồn tại trong hệ thống: " + actionCode));

        // 2. Khởi tạo và ghi bản ghi vào Sổ tay Kiểm toán
        CtUserModerationLog log = new CtUserModerationLog();
        log.setTargetUserId(targetUserId);
        log.setActionId(action.getId());
        log.setPermissionId(permissionId);
        log.setModeratorId(moderatorId);
        log.setReason(reason);

        logRepository.save(log);
    }

    @Override
    public List<AuditLogResponse> getUserAuditLogs(Long userId) {

        // 1. Kéo dữ liệu thô từ CSDL
        List<CtUserModerationLog> logs = logRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
        List<AuditLogResponse> responseList = new ArrayList<>();

        // 2. Xử lý đóng gói DTO an toàn bằng vòng lặp For nguyên thủy
        if (logs != null) {
            Object[] logsArray = logs.toArray();

            for (int i = 0; i < logsArray.length; i = i + 1) {
                CtUserModerationLog log = (CtUserModerationLog) logsArray[i];

                // Giải mã Hành vi Kiểm duyệt
                String actionName = "Không xác định";
                ModerationAction action = actionRepository.findById(log.getActionId()).orElse(null);
                if (action != null) {
                    actionName = action.getName();
                }

                // Giải mã Quyền Hạt Lựu (Nếu có)
                String permCode = "";
                if (log.getPermissionId() != null) {
                    Permission p = permissionRepository.findById(log.getPermissionId()).orElse(null);
                    if (p != null) {
                        permCode = p.getPermissionCode();
                    }
                }

                // Giải mã Danh tính Người thực thi
                String modName = "Hệ thống";
                User mod = userRepository.findById(log.getModeratorId()).orElse(null);
                if (mod != null) {
                    if (mod.getFullName() != null && mod.getFullName().trim().isEmpty() == false) {
                        modName = mod.getFullName();
                    } else {
                        modName = mod.getUsername();
                    }
                }

                // Lắp ráp DTO
                AuditLogResponse dto = new AuditLogResponse();
                dto.setLogId(log.getId());
                dto.setActionCode(action != null ? action.getCode() : "UNKNOWN");
                dto.setActionName(actionName);
                dto.setPermissionCode(permCode);
                dto.setModeratorName(modName);
                dto.setReason(log.getReason());
                dto.setCreatedAt(log.getCreatedAt());

                responseList.add(dto);
            }
        }

        return responseList;
    }

    // [THÊM MỚI] Truy xuất lịch sử có bộ lọc loại hành vi và phân trang.
    @Override
    public AuditLogPageResponse layLichSuKiemToan(Long userId, String actionCode, int pageNo, int pageSize) {

        // 1. Chuẩn hóa bộ lọc — null hoặc rỗng → ALL
        String boLoc = (actionCode != null && actionCode.trim().isEmpty() == false) ? actionCode.trim() : "ALL";

        // 2. Truy vấn phân trang từ CSDL
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<CtUserModerationLog> trang = logRepository.layLichSuCoLocVaPhanTrang(userId, boLoc, pageable);
        List<CtUserModerationLog> logs = trang.getContent();
        List<AuditLogResponse> responseList = new ArrayList<>();

        // 3. Đóng gói DTO an toàn bằng vòng lặp For — list.get(i) không vi phạm quy tắc
        for (int i = 0; i < logs.size(); i = i + 1) {
            CtUserModerationLog log = logs.get(i);

            String actionName = "Không xác định";
            String actionCodeTuDb = "UNKNOWN";
            ModerationAction action = actionRepository.findById(log.getActionId()).orElse(null);
            if (action != null) {
                actionName = action.getName();
                actionCodeTuDb = action.getCode();
            }

            String permCode = "";
            if (log.getPermissionId() != null) {
                Permission p = permissionRepository.findById(log.getPermissionId()).orElse(null);
                if (p != null) {
                    permCode = p.getPermissionCode();
                }
            }

            String modName = "Hệ thống";
            User mod = userRepository.findById(log.getModeratorId()).orElse(null);
            if (mod != null) {
                if (mod.getFullName() != null && mod.getFullName().trim().isEmpty() == false) {
                    modName = mod.getFullName();
                } else {
                    modName = mod.getUsername();
                }
            }

            AuditLogResponse dto = new AuditLogResponse();
            dto.setLogId(log.getId());
            dto.setActionCode(actionCodeTuDb);
            dto.setActionName(actionName);
            dto.setPermissionCode(permCode);
            dto.setModeratorName(modName);
            dto.setReason(log.getReason());
            dto.setCreatedAt(log.getCreatedAt());

            responseList.add(dto);
        }

        // 4. Đếm tổng số lần bị khóa từ TOÀN BỘ lịch sử — không chỉ trang hiện tại
        long soLanBiKhoa = logRepository.demSoLanBiKhoa(userId);

        // 5. Đóng gói kết quả phân trang
        AuditLogPageResponse result = new AuditLogPageResponse();
        result.setDanhSachLog(responseList);
        result.setConTrangTiepTheo(trang.hasNext());
        result.setSoLanBiKhoa(soLanBiKhoa);

        return result;
    }

    // [THÊM MỚI] Trả toàn bộ danh mục hành vi kiểm duyệt từ bảng MODERATION_ACTIONS.
    @Override
    public List<ModerationActionResponse> getDanhSachHanhViKiemDuyet() {
        List<ModerationAction> danhSachHanhVi = actionRepository.findAll();
        List<ModerationActionResponse> responseList = new ArrayList<>();

        for (int i = 0; i < danhSachHanhVi.size(); i = i + 1) {
            ModerationAction hanhVi = danhSachHanhVi.get(i);
            ModerationActionResponse dto = new ModerationActionResponse();
            dto.setCode(hanhVi.getCode());
            dto.setName(hanhVi.getName());
            responseList.add(dto);
        }

        return responseList;
    }
}