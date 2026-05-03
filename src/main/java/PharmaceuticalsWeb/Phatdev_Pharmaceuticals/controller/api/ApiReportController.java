//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiReportController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentReportRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IPublicReportService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * =========================================================================
 * CỔNG GIAO TIẾP PUBLIC: HỆ SINH THÁI BÁO CÁO
 * =========================================================================
 * Nơi duy nhất hứng các gói tin báo cáo vi phạm từ người dùng cuối.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ApiReportController {

    private final IPublicReportService publicReportService;
    private final IUserService userService;

    @PostMapping("/comments")
    public ApiResponse<Void> guiBaoCaoBinhLuan(
            @Valid @RequestBody CommentReportRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập trước khi thực hiện báo cáo vi phạm.");
        }

        publicReportService.guiBaoCao(request, userId);
        return ApiResponse.thanhCong(null, "Báo cáo của bạn đã được ghi nhận. Cảm ơn bạn đã đóng góp làm sạch cộng đồng.");
    }

    /**
     * Định danh người dùng. Trả về null nếu chưa đăng nhập.
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