//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IPublicReportService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentReportRequest;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ: HỆ SINH THÁI BÁO CÁO (PUBLIC)
 * =========================================================================
 * Thiết kế theo nguyên lý ISP (Interface Segregation Principle).
 * Chỉ cung cấp duy nhất đặc quyền ghi nhận báo cáo. 
 * Tuyệt đối không chứa các hàm lấy danh sách hay xử lý báo cáo của Admin.
 */
public interface IPublicReportService {

    /**
     * Ghi nhận đơn báo cáo từ người dùng.
     * Mở toang CSDL để hứng dữ liệu, tuyệt đối không chặn trùng lặp.
     * @param request DTO chứa ID nội dung, Loại nội dung và Lý do
     * @param userId Người gửi báo cáo (Bắt buộc)
     */
    void guiBaoCao(CommentReportRequest request, Long userId);
}