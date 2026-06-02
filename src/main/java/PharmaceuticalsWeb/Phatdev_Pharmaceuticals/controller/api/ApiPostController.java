//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiPostController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostDetailResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IPostService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * =========================================================================
 * API BÀI VIẾT PHÍA PUBLIC
 * =========================================================================
 * Tuân thủ Điều 3: prefix /api/posts, trả về 100% JSON qua ApiResponse<T>.
 * Trang posts/list.html và posts/detail.html gọi các endpoint này.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ApiPostController {

    private final IPostService postService;
    private final IUserService userService;

    /** Thống kê tổng quan cho Hero Stats */
    @GetMapping("/stats")
    public ApiResponse<PostStatsResponse> layThongKe() {
        PostStatsResponse stats = postService.layThongKeTrangBaiViet();
        return ApiResponse.thanhCong(stats, "Lấy thống kê thành công");
    }

    /** Danh mục kèm số bài viết cho sidebar */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> layDanhMuc() {
        List<CategoryResponse> danhMuc = postService.layDanhMucKemSoBaiViet();
        return ApiResponse.thanhCong(danhMuc, "Lấy danh mục thành công");
    }

    /** Tag cloud kèm số lần dùng cho sidebar */
    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> layTagCloud() {
        List<TagResponse> tags = postService.layTagCloud();
        return ApiResponse.thanhCong(tags, "Lấy tags thành công");
    }

    /** Bài viết nổi bật cho Featured section */
    @GetMapping("/featured")
    public ApiResponse<PostResponse> layBaiVietNoiBat() {
        PostResponse post = postService.layBaiVietNoiBat();
        return ApiResponse.thanhCong(post, "Lấy bài viết nổi bật thành công");
    }

    /**
     * Tìm kiếm + lọc + phân trang bài viết đã xuất bản.
     * Tham số: keyword, category (ID), access, sort, page, size.
     */
    @GetMapping
    public ApiResponse<Page<PostResponse>> timKiemBaiViet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Page<PostResponse> result = postService.timKiemBaiViet(keyword, category, roleId, sort, page, size);
        return ApiResponse.thanhCong(result, "Lấy danh sách bài viết thành công");
    }

    /**
     * Chi tiết bài viết theo slug.
     * Ghi nhận lượt xem đồng thời (IP + userId nếu đăng nhập).
     */
    @GetMapping("/{slug}")
    public ApiResponse<PostDetailResponse> layChiTiet(
            @PathVariable String slug,
            HttpServletRequest request,
            Authentication authentication) {

        String viewerIp = layIpNguoiDung(request);
        Long userId = layUserIdTuAuthentication(authentication);

        PostDetailResponse detail = postService.layChiTietBaiViet(slug, viewerIp, userId);
        return ApiResponse.thanhCong(detail, "Lấy chi tiết bài viết thành công");
    }

    /**
     * Ghi nhận lượt xem riêng biệt (gọi sau khi render xong).
     * Tách ra endpoint riêng để frontend gọi async không block render.
     */
    @PostMapping("/{postId}/view")
    public ApiResponse<Void> ghiNhanLuotXem(
            @PathVariable Long postId,
            HttpServletRequest request,
            Authentication authentication) {

        String viewerIp = layIpNguoiDung(request);
        Long userId = layUserIdTuAuthentication(authentication);
        postService.ghiNhanLuotXem(postId, viewerIp, userId);
        return ApiResponse.thanhCong(null, "Ghi nhận lượt xem thành công");
    }

    /** Lấy IP thật của người dùng qua header proxy */
    private String layIpNguoiDung(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


    /**
     * Bổ sung API đón Request Thả cảm xúc trực tiếp lên Bài viết.
     */
    @RequirePermission("USER_REACT")
    @PostMapping("/like")
    public ApiResponse<Void> thichBaiViet(
            @Valid @RequestBody LikeRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng định danh (đăng nhập) trước khi tương tác.");
        }
        postService.thichBaiViet(request, userId);
        return ApiResponse.thanhCong(null, "Ghi nhận phản ứng cảm xúc thành công");
    }

    /**
     * Thu thập dữ liệu: Ghi nhận tải tài liệu
     */
    @PostMapping("/files/{fileId}/download-track")
    public ApiResponse<Void> ghiNhanTaiTaiLieu(
            @PathVariable Long fileId,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        // Backend âm thầm thu thập, Frontend không cần biết logic bên trong
        postService.ghiNhanTaiTaiLieu(fileId, userId);
        
        return ApiResponse.thanhCong(null, "Đã ghi nhận tín hiệu hệ thống.");
    }


    /**
     * Lấy định danh từ JWT Authentication.
     * Trả về null thay vì Exception để hỗ trợ khách vãng lai (Guest).
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
