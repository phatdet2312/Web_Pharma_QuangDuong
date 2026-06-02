//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiCommentController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EditContentRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReplyRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtActionLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LoaiLikeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PhCmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ICommentService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDung;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDungFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * API BÌNH LUẬN PHÍA PUBLIC
 * =========================================================================
 * Prefix /api/comments. Gửi comment cho bài viết, gửi reply, react.
 * posts/detail.html và events/detail.html gọi các endpoint này.
 * - Tích hợp tính năng Sắp xếp đa chiều (SortBy: popular/newest).
 * - Cấp quyền tự quản lý (Cập nhật, Xóa) cho chính tác giả bình luận.
 * - Truy xuất Nhật ký tự thân (Action Log) phục vụ minh bạch dữ liệu.
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class ApiCommentController {

    private final ICommentService commentService;
    private final IUserService userService;
    private final IEventService eventService;
    private final NguCanhNguoiDungFactory nguCanhFactory;
    
    /** Lấy tất cả loại reaction */
    @GetMapping("/reaction-types")
    public ApiResponse<List<LoaiLikeResponse>> layLoaiLike() {
        return ApiResponse.thanhCong(commentService.layTatCaLoaiLike(), "Lấy loại reaction thành công");
    }

    /** Lấy bình luận của bài viết */
    @GetMapping("/posts/{postId}")
    public ApiResponse<Page<CmtResponse>> layCmtBaiViet(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        Page<CmtResponse> result = commentService.layCmtCuaBaiViet(postId, sortBy, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy bình luận thành công");
    }

    /** * Khai thác toàn bộ cây bình luận của một Trạm Sự kiện. 
     * Hỗ trợ sắp xếp theo tương tác để ưu tiên hiển thị.
     */
    @GetMapping("/events/{eventId}")
    public ApiResponse<Page<CmtResponse>> layCmtCuaSuKien(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        if (eventService.coQuyenTruyCapBuoi(eventId, nguCanh) == false) {
            Page<CmtResponse> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
            return ApiResponse.thanhCong(emptyPage, "Bình luận chỉ mở cho tài khoản đủ quyền tham dự.");
        }
        Page<CmtResponse> result = commentService.layCmtCuaBuoi(eventId, sortBy, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy danh sách bình luận sự kiện thành công.");
    }

    /** Lấy phản hồi cấp 2 trực tiếp dưới một bình luận gốc */
    @GetMapping("/{cmtId}/replies")
    public ApiResponse<Page<PhCmtResponse>> layPhanHoiCapHai(
            @PathVariable Long cmtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        Page<PhCmtResponse> result = commentService.layPhanHoiCapHai(cmtId, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy danh sách câu trả lời thành công.");
    }

    /** Lấy phản hồi cấp 3, gom cả các tầng sâu hơn về cùng một luồng hiển thị */
    @GetMapping("/reply/{phCmtId}/replies")
    public ApiResponse<Page<PhCmtResponse>> layPhanHoiCapBa(
            @PathVariable Long phCmtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        Page<PhCmtResponse> result = commentService.layPhanHoiCapBa(phCmtId, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy danh sách câu trả lời lồng cấp thành công.");
    }

    /** Gửi bình luận cho bài viết */
    @RequirePermission("USER_COMMENT")
    @PostMapping("/posts/{postId}")
    public ApiResponse<CmtResponse> guiCmtBaiViet(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập để bình luận");
        }
        request.setTargetId(postId);
        CmtResponse result = commentService.guiCmtBaiViet(request, userId);
        return ApiResponse.thanhCong(result, "Gửi bình luận thành công");
    }

    /** Gửi bình luận cho sự kiện */
    @RequirePermission("USER_COMMENT")
    @PostMapping("/events/{eventId}")
    public ApiResponse<CmtResponse> guiCmtSuKien(
            @PathVariable Long eventId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập.");
        }
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        if (eventService.coQuyenTruyCapBuoi(eventId, nguCanh) == false) {
            return ApiResponse.loi(403, "Bình luận chỉ mở cho tài khoản đủ quyền tham dự phiên này.");
        }
        request.setTargetId(eventId);
        CmtResponse result = commentService.guiCmtSuKien(request, userId);
        return ApiResponse.thanhCong(result, "Bình luận sự kiện thành công.");
    }

    /** Gửi phản hồi (reply) */
    @RequirePermission("USER_COMMENT")
    @PostMapping("/reply")
    public ApiResponse<PhCmtResponse> guiPhCmt(
            @Valid @RequestBody ReplyRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập để phản hồi");
        }
        PhCmtResponse result = commentService.guiPhCmt(request, userId);
        return ApiResponse.thanhCong(result, "Gửi phản hồi thành công");
    }

    /** Hiệu đính nội dung văn bản của một Bình luận gốc bởi chính Tác giả. */
    @RequirePermission("USER_COMMENT")
    @PutMapping("/{cmtId}")
    public ApiResponse<CmtResponse> capNhatCmt(
            @PathVariable Long cmtId,
            @Valid @RequestBody EditContentRequest request,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập.");
        }
        CmtResponse result = commentService.capNhatCmt(cmtId, request, userId);
        return ApiResponse.thanhCong(result, "Cập nhật bình luận thành công.");
    }

    /** Gỡ bỏ hoàn toàn một Bình luận gốc bởi chính Tác giả. */
    @DeleteMapping("/{cmtId}")
    public ApiResponse<Void> xoaCmt(
            @PathVariable Long cmtId,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập.");
        }
        commentService.xoaCmt(cmtId, userId);
        return ApiResponse.thanhCong(null, "Xóa bình luận thành công.");
    }

    /** Hiệu đính nội dung văn bản của một Phản hồi lồng cấp bởi chính Tác giả. */
    @RequirePermission("USER_COMMENT")
    @PutMapping("/reply/{phCmtId}")
    public ApiResponse<PhCmtResponse> capNhatPhCmt(
            @PathVariable Long phCmtId,
            @Valid @RequestBody EditContentRequest request,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập.");
        }
        PhCmtResponse result = commentService.capNhatPhCmt(phCmtId, request, userId);
        return ApiResponse.thanhCong(result, "Cập nhật phản hồi thành công.");
    }

    /** Gỡ bỏ hoàn toàn một Phản hồi lồng cấp bởi chính Tác giả. */
    @DeleteMapping("/reply/{phCmtId}")
    public ApiResponse<Void> xoaPhCmt(
            @PathVariable Long phCmtId,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập.");
        }
        commentService.xoaPhCmt(phCmtId, userId);
        return ApiResponse.thanhCong(null, "Xóa phản hồi thành công.");
    }

    /** Truy xuất toàn bộ lịch sử chỉnh sửa văn bản của một Bình luận gốc. */
    @GetMapping("/{cmtId}/history")
    public ApiResponse<List<CmtActionLogResponse>> layLichSuTuThanCmt(@PathVariable Long cmtId) {
        List<CmtActionLogResponse> result = commentService.layLichSuTuThanCmt(cmtId);
        return ApiResponse.thanhCong(result, "Trích xuất lịch sử chỉnh sửa bình luận thành công.");
    }

    /** Truy xuất toàn bộ lịch sử chỉnh sửa văn bản của một Phản hồi lồng cấp. */
    @GetMapping("/reply/{phCmtId}/history")
    public ApiResponse<List<CmtActionLogResponse>> layLichSuTuThanPhCmt(@PathVariable Long phCmtId) {
        List<CmtActionLogResponse> result = commentService.layLichSuTuThanPhCmt(phCmtId);
        return ApiResponse.thanhCong(result, "Trích xuất lịch sử chỉnh sửa phản hồi thành công.");
    }

    /** Thêm / đổi reaction trên comment gốc */
    @RequirePermission("USER_REACT")
    @PostMapping("/like/cmt")
    public ApiResponse<Void> thichCmt(
            @Valid @RequestBody LikeRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập");
        }
        commentService.thichCmt(request, userId);
        return ApiResponse.thanhCong(null, "Đã react thành công");
    }

    /** Thêm / đổi reaction trên reply */
    @RequirePermission("USER_REACT")
    @PostMapping("/like/reply")
    public ApiResponse<Void> thichPhCmt(
            @Valid @RequestBody LikeRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập");
        }
        commentService.thichPhCmt(request, userId);
        return ApiResponse.thanhCong(null, "Đã react thành công");
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
