//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AuditLogPageResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * =========================================================================
 * DTO: PHẢN HỒI LỊCH SỬ KIỂM TOÁN CÓ BỘ LỌC VÀ PHÂN TRANG
 * =========================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPageResponse {

    // Danh sách bản ghi của trang hiện tại
    private List<AuditLogResponse> danhSachLog;

    // Còn trang tiếp theo để tải không?
    private boolean conTrangTiepTheo;

    // Tổng số lần bị khóa (LOCK_USER) trong toàn bộ lịch sử — dùng cho Risk Indicator
    private long soLanBiKhoa;
}
