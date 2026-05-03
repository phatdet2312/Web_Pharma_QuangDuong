//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAuditService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AuditLogPageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AuditLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ModerationActionResponse;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN: DỊCH VỤ KIỂM TOÁN BẢO MẬT HỆ THỐNG ĐỊNH DANH (CHUẨN MÙ)
 * =========================================================================
 * Cung cấp hợp đồng tiêu chuẩn để ghi lại lịch sử vi phạm và truy xuất
 * dữ liệu phục vụ giao diện Timeline.
 */
public interface IAuditService {

    /**
     * Ghi nhận một hành động kiểm duyệt vào Sổ tay Kiểm toán.
     * @param targetUserId Mã định danh tài khoản bị tác động
     * @param actionCode Mã hành vi (VD: 'LOCK_USER', 'BLACKLIST_PERM')
     * @param permissionId Mã quyền hạt lựu bị ảnh hưởng (Có thể để null)
     * @param moderatorId Quản trị viên ra lệnh
     * @param reason Lý do thực thi lệnh (Bắt buộc)
     */
    void logAction(Long targetUserId, String actionCode, Integer permissionId, Long moderatorId, String reason);

    /**
     * Truy xuất toàn bộ lịch sử kiểm duyệt của một tài khoản (không phân trang).
     * @param userId Mã định danh tài khoản cần tra cứu
     * @return Danh sách lịch sử đã được làm sạch (Không chứa Entity nhạy cảm)
     */
    List<AuditLogResponse> getUserAuditLogs(Long userId);

    // [THÊM MỚI] Truy xuất lịch sử kiểm duyệt có bộ lọc loại hành vi và phân trang.
    // actionCode = 'ALL' → không lọc loại; pageNo bắt đầu từ 0.
    AuditLogPageResponse layLichSuKiemToan(Long userId, String actionCode, int pageNo, int pageSize);

    // [THÊM MỚI] Trả toàn bộ danh mục hành vi kiểm duyệt từ bảng MODERATION_ACTIONS.
    // Frontend dùng để populate dropdown bộ lọc — không gán cứng.
    List<ModerationActionResponse> getDanhSachHanhViKiemDuyet();
}