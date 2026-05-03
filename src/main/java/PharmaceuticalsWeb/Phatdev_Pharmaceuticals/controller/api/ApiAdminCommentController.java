//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminCommentController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentModerationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LoaiLikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminCmtContextResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LoaiLikeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ICommentService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * API QUẢN TRỊ BÌNH LUẬN (ADMIN)
 * =========================================================================
 * Prefix /api/admin/comments. Yêu cầu quyền ROLE_ADMIN.
 * admin/comments.html gọi các endpoint này để kiểm duyệt, xóa, xem thống kê.

 * - Nâng cấp cỗ máy tìm kiếm đa chiều (Lọc theo vùng thời gian, trạng thái, từ khóa).
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class ApiAdminCommentController {

    private final ICommentService commentService;
    private final IUserService userService;

    /** Thống kê bình luận cho Hero Stats */
    @GetMapping("/stats")
    public ApiResponse<CommentStatsResponse> layThongKe() {
        return ApiResponse.thanhCong(commentService.layThongKeBinhLuan(), "Lấy thống kê thành công");
    }

    /** Tất cả loại reaction (dùng cho modal quản lý LOAI_LIKE) */
    @GetMapping("/reaction-types")
    public ApiResponse<List<LoaiLikeResponse>> layLoaiLike() {
        return ApiResponse.thanhCong(commentService.layTatCaLoaiLike(), "Lấy loại reaction thành công");
    }

    /** G2: Tạo mới loại reaction */
    @PostMapping("/reaction-types")
    public ApiResponse<LoaiLikeResponse> taoLoaiLike(@Valid @RequestBody LoaiLikeRequest request) {
        return ApiResponse.thanhCong(commentService.taoLoaiLike(request), "Tạo loại reaction thành công");
    }

    /** G2: Cập nhật loại reaction */
    @PutMapping("/reaction-types/{id}")
    public ApiResponse<LoaiLikeResponse> capNhatLoaiLike(
            @PathVariable Integer id,
            @Valid @RequestBody LoaiLikeRequest request) {
        return ApiResponse.thanhCong(commentService.capNhatLoaiLike(id, request), "Cập nhật loại reaction thành công");
    }

    /** G2: Xóa loại reaction */
    @DeleteMapping("/reaction-types/{id}")
    public ApiResponse<Void> xoaLoaiLike(@PathVariable Integer id) {
        commentService.xoaLoaiLike(id);
        return ApiResponse.thanhCong(null, "Xóa loại reaction thành công");
    }

    /** Comment gốc chờ duyệt */
    @GetMapping("/pending")
    public ApiResponse<List<CmtResponse>> layCmtChuaDuyet() {
        return ApiResponse.thanhCong(commentService.layCmtChuaDuyet(), "Lấy comment chờ duyệt thành công");
    }

    /**
     * Danh sách comment phân trang (cho tất cả tab của admin/comments.html).
     * status: null=all, PENDING, HIDE, WARN
     * targetType: null=all, POST, EVENT
     * * Đã nâng cấp thành Cỗ máy tìm kiếm đa chiều: Bổ sung lọc từ khóa, thời gian và mục tiêu.
     */
    @GetMapping
    public ApiResponse<Page<AdminCmtContextResponse>> timKiemBinhLuanAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (targetType != null && keyword == null && startDate == null && targetId == null) {
            Page<AdminCmtContextResponse> resultLegacy = commentService.layDanhSachBinhLuan(status, targetType, page, size);
            return ApiResponse.thanhCong(resultLegacy, "Lấy danh sách bình luận thành công.");
        }

        Page<AdminCmtContextResponse> result = commentService.timKiemBinhLuanAdmin(keyword, status, startDate, endDate, targetId, page, size);
        return ApiResponse.thanhCong(result, "Trích xuất danh sách bình luận hệ thống thành công.");
    }

    /**
     * Kiểm duyệt bình luận / reply.
     * Body: targetId, targetType (CMT|PH_CMT), actionId, reason.
     */
    @PostMapping("/moderate")
    public ApiResponse<Void> kiemDuyet(
            @Valid @RequestBody CommentModerationRequest request,
            Authentication authentication) {

        Long moderatorId = layUserIdTuAuthentication(authentication);
        if (moderatorId == null) {
            return ApiResponse.loi(401, "Phiên đăng nhập không hợp lệ");
        }
        commentService.kiemDuyetBinhLuan(request, moderatorId);
        return ApiResponse.thanhCong(null, "Kiểm duyệt thành công");
    }

    /** Xóa vật lý comment gốc (cascade tất cả reply + reaction) */
    @DeleteMapping("/cmt/{cmtId}")
    public ApiResponse<Void> xoaCmt(@PathVariable Long cmtId) {
        commentService.xoaCmtVatLy(cmtId);
        return ApiResponse.thanhCong(null, "Xóa bình luận thành công");
    }

    /** Xóa vật lý reply */
    @DeleteMapping("/reply/{phCmtId}")
    public ApiResponse<Void> xoaPhCmt(@PathVariable Long phCmtId) {
        commentService.xoaPhCmtVatLy(phCmtId);
        return ApiResponse.thanhCong(null, "Xóa phản hồi thành công");
    }

    /** Kiểm duyệt hàng loạt (APPROVE / HIDE / WARN) */
    @PostMapping("/bulk/moderate")
    public ApiResponse<Void> kiemDuyetNhieu(
            @Valid @RequestBody BulkActionRequest request,
            Authentication authentication) {
        Long moderatorId = layUserIdTuAuthentication(authentication);
        if (moderatorId == null) {
            return ApiResponse.loi(401, "Phiên đăng nhập không hợp lệ");
        }
        commentService.kiemDuyetNhieu(request, moderatorId);
        return ApiResponse.thanhCong(null, "Kiểm duyệt hàng loạt thành công");
    }

    /** Xóa hàng loạt comment gốc */
    @DeleteMapping("/bulk")
    public ApiResponse<Void> xoaNhieu(@Valid @RequestBody BulkActionRequest request) {
        commentService.xoaNhieuCmt(request);
        return ApiResponse.thanhCong(null, "Xóa hàng loạt thành công");
    }

    /**
     * Thuật toán Định danh Chuyên sâu: 
     * Ủy thác hoàn toàn cho UserService để đọc Principal bất chấp Oauth2 hay JWT.
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