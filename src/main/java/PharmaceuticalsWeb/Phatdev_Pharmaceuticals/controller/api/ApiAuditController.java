//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAuditController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AuditLogPageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ModerationActionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================================
 * API CONTROLLER: CỔNG TRUY XUẤT KIỂM TOÁN BẢO MẬT
 * =========================================================================
 */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class ApiAuditController {

    private final IAuditService auditService;

    // [SỬA] Endpoint nay hỗ trợ lọc theo loại hành vi và phân trang.
    // actionCode mặc định = 'ALL' (không lọc); pageNo bắt đầu từ 0; pageSize mặc định = 10.
    @RequirePermission("AUDIT_VIEW")
    @GetMapping("/users/{userId}")
    public ApiResponse<AuditLogPageResponse> getUserAuditLogs(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String actionCode,
            @RequestParam(required = false, defaultValue = "0") int pageNo,
            @RequestParam(required = false, defaultValue = "5") int pageSize) {

        AuditLogPageResponse result = auditService.layLichSuKiemToan(userId, actionCode, pageNo, pageSize);
        return ApiResponse.thanhCong(result, "Truy xuất sổ tay kiểm toán thành công");
    }

    //Trả danh mục hành vi kiểm duyệt từ DB để Frontend populate dropdown bộ lọc.
    @RequirePermission("AUDIT_VIEW")
    @GetMapping("/moderation-actions")
    public ApiResponse<List<ModerationActionResponse>> getDanhSachHanhViKiemDuyet() {
        List<ModerationActionResponse> result = auditService.getDanhSachHanhViKiemDuyet();
        return ApiResponse.thanhCong(result, "Truy xuất danh mục hành vi kiểm duyệt thành công");
    }
}