//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminReportController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReportResolutionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentReportResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ReportModLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminReportService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================================
 * CỔNG GIAO TIẾP ADMIN: HỆ SINH THÁI BÁO CÁO VÀ KIỂM TOÁN
 * =========================================================================
 * Cấm mọi quyền truy cập từ Public. Lớp khiên bảo vệ bởi Spring Security.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ApiAdminReportController {

    private final IAdminReportService adminReportService;
    private final IUserService userService;

    @RequirePermission("REPORT_VIEW")
    @GetMapping("/comments")
    public ApiResponse<List<CommentReportResponse>> layDanhSachBaoCao(
            @RequestParam String targetType,
            @RequestParam(required = false) String status) {
        
        List<CommentReportResponse> result = adminReportService.layDanhSachBaoCao(targetType, status);
        return ApiResponse.thanhCong(result, "Lấy danh sách đơn báo cáo thành công.");
    }

    @RequirePermission("REPORT_RESOLVE")
    @PatchMapping("/comments/resolve")
    public ApiResponse<Void> xuLyBaoCao(
            @Valid @RequestBody ReportResolutionRequest request,
            Authentication authentication) {

        Long moderatorId = layUserIdTuAuthentication(authentication);
        if (moderatorId == null) {
            return ApiResponse.loi(401, "Phiên đăng nhập quản trị không hợp lệ.");
        }

        adminReportService.xuLyBaoCao(request, moderatorId);
        return ApiResponse.thanhCong(null, "Đóng hồ sơ báo cáo thành công.");
    }

    @RequirePermission("REPORT_VIEW")
    @GetMapping("/comments/{reportId}/history")
    public ApiResponse<List<ReportModLogResponse>> layLichSuXuLyBaoCao(
            @PathVariable Long reportId,
            @RequestParam String targetType) {
        
        List<ReportModLogResponse> result = adminReportService.layLichSuXuLy(reportId, targetType);
        return ApiResponse.thanhCong(result, "Trích xuất lịch sử kiểm toán thành công.");
    }

    /**
     * Móc danh tính Admin một cách an toàn.
     */
    private Long layUserIdTuAuthentication(Authentication authentication) {
        if (authentication == null || authentication.isAuthenticated() == false || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            User user = userService.getCurrentAuthenticatedUser();
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
}