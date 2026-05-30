//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminPostController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostDictionaryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostMediaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
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

import org.springframework.web.multipart.MultipartFile;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostCommentPreviewResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostFileResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostImageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostLinkedEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostReactionSummary;

import java.util.List;
import java.util.Map;

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

    /** Danh sách bài viết với lọc/phân trang + date range + sort */
    @GetMapping
    public ApiResponse<Page<PostResponse>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort) {

        if (page < 0) { page = 0; }
        if (size < 1 || size > 100) { size = 10; }
        Page<PostResponse> result = adminPostService.layDanhSachBaiViet(
                keyword, category, roleId, published, page, size, startDate, endDate, sort);
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

    /** Lấy chi tiết bài viết theo ID (admin, bao gồm cả nháp) */
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> layChiTiet(@PathVariable Long postId) {
        PostResponse post = adminPostService.layChiTietBaiViet(postId);
        return ApiResponse.thanhCong(post, "Lấy chi tiết bài viết thành công");
    }

    /** Upload ảnh thumbnail bài viết */
    @PostMapping("/upload-thumbnail")
    public ApiResponse<AdminPostMediaResponse> uploadThumbnail(@RequestParam("file") MultipartFile file) {
        AdminPostMediaResponse result = adminPostService.uploadAnhBaiViet(file);
        return ApiResponse.thanhCong(result, "Upload ảnh thành công");
    }

    /** Bật/tắt xuất bản (toggle) */
    @PatchMapping("/{postId}/publish")
    public ApiResponse<PostResponse> doiTrangThaiXuatBan(
            @PathVariable Long postId,
            @RequestParam boolean value) {

        PostResponse post = adminPostService.doiTrangThaiXuatBan(postId, value);
        return ApiResponse.thanhCong(post, "Cập nhật trạng thái xuất bản thành công");
    }

    /** Bật/tắt bài viết nổi bật */
    @PatchMapping("/{postId}/featured")
    public ApiResponse<PostResponse> doiTrangThaiFeatured(
            @PathVariable Long postId,
            @RequestParam boolean value) {
        PostResponse post = adminPostService.doiTrangThaiFeatured(postId, value);
        return ApiResponse.thanhCong(post, "Cập nhật trạng thái nổi bật thành công");
    }

    /** Lấy danh mục từ điển cho frontend (roles, ...) */
    @GetMapping("/dictionaries")
    public ApiResponse<AdminPostDictionaryResponse> layTuDien() {
        return ApiResponse.thanhCong(adminPostService.layDanhMucTuDien(), "Lấy từ điển thành công");
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

    // === QUẢN LÝ HÌNH ẢNH GALLERY ===

    /** Lấy danh sách ảnh gallery của bài viết */
    @GetMapping("/{postId}/images")
    public ApiResponse<List<PostImageResponse>> layAnhGallery(@PathVariable Long postId) {
        return ApiResponse.thanhCong(adminPostService.layAnhCuaBaiViet(postId), "Lấy ảnh gallery thành công");
    }

    /** Upload ảnh vào gallery bài viết */
    @PostMapping("/{postId}/images")
    public ApiResponse<PostImageResponse> uploadAnhGallery(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file) {
        PostImageResponse result = adminPostService.uploadAnhGallery(postId, file);
        return ApiResponse.thanhCong(result, "Upload ảnh gallery thành công");
    }

    /** Xóa ảnh khỏi gallery bài viết */
    @DeleteMapping("/{postId}/images/{imageId}")
    public ApiResponse<Void> xoaAnhGallery(
            @PathVariable Long postId,
            @PathVariable Long imageId) {
        adminPostService.xoaAnhGallery(postId, imageId);
        return ApiResponse.thanhCong(null, "Xóa ảnh gallery thành công");
    }

    /** Đổi thứ tự hiển thị ảnh trong gallery */
    @PatchMapping("/{postId}/images/reorder")
    public ApiResponse<Void> doiThuTuAnh(
            @PathVariable Long postId,
            @RequestBody List<Map<String, Object>> reorderData) {
        adminPostService.doiThuTuAnh(postId, reorderData);
        return ApiResponse.thanhCong(null, "Đổi thứ tự ảnh thành công");
    }

    // === QUẢN LÝ FILE ĐÍNH KÈM ===

    /** Lấy danh sách file đính kèm của bài viết */
    @GetMapping("/{postId}/files")
    public ApiResponse<List<PostFileResponse>> layFileDinhKem(@PathVariable Long postId) {
        return ApiResponse.thanhCong(adminPostService.layFileCuaBaiViet(postId), "Lấy file đính kèm thành công");
    }

    /** Upload file đính kèm cho bài viết */
    @PostMapping("/{postId}/files")
    public ApiResponse<PostFileResponse> uploadFileDinhKem(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file) {
        PostFileResponse result = adminPostService.uploadFileDinhKem(postId, file);
        return ApiResponse.thanhCong(result, "Upload file đính kèm thành công");
    }

    /** Xóa file đính kèm của bài viết */
    @DeleteMapping("/{postId}/files/{fileId}")
    public ApiResponse<Void> xoaFileDinhKem(
            @PathVariable Long postId,
            @PathVariable Long fileId) {
        adminPostService.xoaFileDinhKem(postId, fileId);
        return ApiResponse.thanhCong(null, "Xóa file đính kèm thành công");
    }

    // === BÌNH LUẬN / REACTIONS / SỰ KIỆN LIÊN KẾT ===

    /** Lấy bình luận preview của bài viết (phân trang) */
    @GetMapping("/{postId}/comments")
    public ApiResponse<Page<PostCommentPreviewResponse>> layCmtPreview(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (page < 0) { page = 0; }
        if (size < 1 || size > 50) { size = 5; }
        return ApiResponse.thanhCong(adminPostService.layCmtCuaBaiVietAdmin(postId, page, size),
                "Lấy bình luận thành công");
    }

    /** Lấy reactions chi tiết (grouped by type) */
    @GetMapping("/{postId}/reactions")
    public ApiResponse<List<PostReactionSummary>> layReactions(@PathVariable Long postId) {
        return ApiResponse.thanhCong(adminPostService.layReactionsCuaBaiViet(postId),
                "Lấy reactions thành công");
    }

    /** Lấy danh sách sự kiện liên kết với bài viết */
    @GetMapping("/{postId}/events")
    public ApiResponse<List<PostLinkedEventResponse>> laySuKienLienKet(@PathVariable Long postId) {
        return ApiResponse.thanhCong(adminPostService.laySuKienLienKet(postId),
                "Lấy sự kiện liên kết thành công");
    }

    /** Liên kết bài viết với buổi sự kiện */
    @PostMapping("/{postId}/events")
    public ApiResponse<Void> lienKetSuKien(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> body) {
        Long ctEventId = body.get("ctEventId");
        if (ctEventId == null) {
            throw new AppException(400, "Thiếu ctEventId trong body request.");
        }
        adminPostService.lienKetSuKien(postId, ctEventId);
        return ApiResponse.thanhCong(null, "Liên kết sự kiện thành công");
    }

    /** Xóa liên kết bài viết - sự kiện */
    @DeleteMapping("/{postId}/events/{ctEventId}")
    public ApiResponse<Void> xoaLienKetSuKien(
            @PathVariable Long postId,
            @PathVariable Long ctEventId) {
        adminPostService.xoaLienKetSuKien(postId, ctEventId);
        return ApiResponse.thanhCong(null, "Xóa liên kết sự kiện thành công");
    }

    /**
     * Thuật toán Định danh Chuyên sâu:
     * Ủy thác hoàn toàn cho UserService để đọc Principal bất chấp Oauth2 hay JWT.
     * Throw AppException 401 thay vì nuốt lỗi trả null.
     */
    private Long layUserIdTuAuthentication(Authentication authentication) {
        if (authentication == null || authentication.isAuthenticated() == false
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(401, "Không thể xác định tác giả. Vui lòng đăng nhập lại.");
        }
        User user = userService.getCurrentAuthenticatedUser();
        return user.getId();
    }
}
