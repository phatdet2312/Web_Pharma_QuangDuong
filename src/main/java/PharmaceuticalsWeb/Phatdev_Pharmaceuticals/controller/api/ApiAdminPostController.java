//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminPostController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminPostService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * =========================================================================
 * API QUẢN TRỊ BÀI VIẾT (ADMIN)
 * =========================================================================
 * Prefix /api/admin/posts. Yêu cầu quyền ROLE_ADMIN.
 * admin/posts.html gọi các endpoint này để CRUD bài viết, danh mục, tag.
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class ApiAdminPostController {

    private final IAdminPostService adminPostService;
    private final IUserService userService;

    /** Thống kê admin (tổng, nháp, gated, downloads...) */
    @GetMapping("/stats")
    public ApiResponse<PostStatsResponse> layThongKeAdmin() {
        return ApiResponse.thanhCong(adminPostService.layThongKeAdmin(), "Lấy thống kê thành công");
    }

    /** Danh sách bài viết với lọc/phân trang */
    @GetMapping
    public ApiResponse<Page<PostResponse>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostResponse> result = adminPostService.layDanhSachBaiViet(
                keyword, category, roleId, published, page, size);
        return ApiResponse.thanhCong(result, "Lấy danh sách thành công");
    }

    /** Tạo bài viết mới */
    @PostMapping
    public ApiResponse<PostResponse> taoBaiViet(
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {

        Long authorId = layUserIdTuAuthentication(authentication);
        PostResponse post = adminPostService.taoBaiViet(request, authorId);
        return ApiResponse.thanhCong(post, "Tạo bài viết thành công");
    }

    /** Cập nhật bài viết */
    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> capNhatBaiViet(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {

        PostResponse post = adminPostService.capNhatBaiViet(postId, request);
        return ApiResponse.thanhCong(post, "Cập nhật bài viết thành công");
    }

    /** Xóa bài viết */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> xoaBaiViet(@PathVariable Long postId) {
        adminPostService.xoaBaiViet(postId);
        return ApiResponse.thanhCong(null, "Xóa bài viết thành công");
    }

    /** Bật/tắt xuất bản (toggle) */
    @PatchMapping("/{postId}/publish")
    public ApiResponse<PostResponse> doiTrangThaiXuatBan(
            @PathVariable Long postId,
            @RequestParam boolean value) {

        PostResponse post = adminPostService.doiTrangThaiXuatBan(postId, value);
        return ApiResponse.thanhCong(post, "Cập nhật trạng thái xuất bản thành công");
    }

    // === BULK ACTIONS ===

    /** Đổi trạng thái xuất bản hàng loạt */
    @PatchMapping("/bulk/publish")
    public ApiResponse<Void> doiTrangThaiNhieu(@Valid @RequestBody BulkActionRequest request) {
        adminPostService.doiTrangThaiXuatBanNhieu(request);
        return ApiResponse.thanhCong(null, "Cập nhật trạng thái xuất bản hàng loạt thành công");
    }

    /** Xóa hàng loạt bài viết */
    @DeleteMapping("/bulk")
    public ApiResponse<Void> xoaNhieu(@Valid @RequestBody BulkActionRequest request) {
        adminPostService.xoaNhieuBaiViet(request);
        return ApiResponse.thanhCong(null, "Xóa hàng loạt thành công");
    }

    // === CRUD DANH MỤC ===

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> layDanhMuc() {
        return ApiResponse.thanhCong(adminPostService.layTatCaDanhMuc(), "Lấy danh mục thành công");
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> taoDanhMuc(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.thanhCong(adminPostService.taoDanhMuc(request), "Tạo danh mục thành công");
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> capNhatDanhMuc(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.thanhCong(adminPostService.capNhatDanhMuc(id, request), "Cập nhật danh mục thành công");
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> xoaDanhMuc(@PathVariable Integer id) {
        adminPostService.xoaDanhMuc(id);
        return ApiResponse.thanhCong(null, "Xóa danh mục thành công");
    }

    // === CRUD TAG ===

    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> layTag() {
        return ApiResponse.thanhCong(adminPostService.layTatCaTag(), "Lấy tags thành công");
    }

    @PostMapping("/tags")
    public ApiResponse<TagResponse> taoTag(@Valid @RequestBody TagRequest request) {
        return ApiResponse.thanhCong(adminPostService.taoTag(request), "Tạo tag thành công");
    }

    @PutMapping("/tags/{id}")
    public ApiResponse<TagResponse> capNhatTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        return ApiResponse.thanhCong(adminPostService.capNhatTag(id, request), "Cập nhật tag thành công");
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> xoaTag(@PathVariable Long id) {
        adminPostService.xoaTag(id);
        return ApiResponse.thanhCong(null, "Xóa tag thành công");
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
