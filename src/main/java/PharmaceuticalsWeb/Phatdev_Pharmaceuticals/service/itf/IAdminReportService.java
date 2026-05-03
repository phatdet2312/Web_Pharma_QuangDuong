//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAdminReportService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReportResolutionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentReportResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ReportModLogResponse;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ: HỆ SINH THÁI BÁO CÁO (ADMIN)
 * =========================================================================
 * Chỉ dành riêng cho việc truy xuất thông tin nhạy cảm (IP) 
 * và thay đổi trạng thái kiểm duyệt (Ghi Audit Log).
 */
public interface IAdminReportService {

    /**
     * Trích xuất danh sách đơn báo cáo để quản trị.
     * @param targetType CMT hoặc PH_CMT
     * @param status PENDING, RESOLVED, REJECTED (Tùy chọn)
     */
    List<CommentReportResponse> layDanhSachBaoCao(String targetType, String status);

    /**
     * Chốt hồ sơ: Đổi trạng thái báo cáo và ghi vào Sổ tay Kiểm toán.
     * @param request DTO chứa ID, Quyết định và Lý do.
     * @param moderatorId Người thực thi lệnh.
     */
    void xuLyBaoCao(ReportResolutionRequest request, Long moderatorId);

    /**
     * Tra cứu lịch sử thay đổi trạng thái của 1 Đơn báo cáo cụ thể.
     */
    List<ReportModLogResponse> layLichSuXuLy(Long reportId, String targetType);
}