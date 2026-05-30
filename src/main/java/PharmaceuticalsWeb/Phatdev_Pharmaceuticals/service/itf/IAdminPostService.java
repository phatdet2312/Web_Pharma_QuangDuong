//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAdminPostService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostDictionaryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminPostMediaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostCommentPreviewResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostFileResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostImageResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostLinkedEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostReactionSummary;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interface nghiệp vụ quản trị bài viết (admin/posts.html).
 */
public interface IAdminPostService {

    /** Thống kê tổng quan cho admin Hero Stats */
    PostStatsResponse layThongKeAdmin();

    /** Lấy danh sách tất cả bài viết với lọc/phân trang */
    Page<PostResponse> layDanhSachBaiViet(String keyword, Integer categoryId,
                                           Integer roleId, Boolean isPublished,
                                           int page, int size);

    /** Tạo bài viết mới */
    PostResponse taoBaiViet(PostRequest request, Long authorId);

    /** Cập nhật bài viết */
    PostResponse capNhatBaiViet(Long postId, PostRequest request);

    /** Xóa bài viết (xóa cascade: tags, images, files, view logs, comments) */
    void xoaBaiViet(Long postId);

    /** Bật/tắt trạng thái xuất bản */
    PostResponse doiTrangThaiXuatBan(Long postId, boolean isPublished);

    /** CRUD danh mục */
    List<CategoryResponse> layTatCaDanhMuc();
    CategoryResponse taoDanhMuc(CategoryRequest request);
    CategoryResponse capNhatDanhMuc(Integer categoryId, CategoryRequest request);
    void xoaDanhMuc(Integer categoryId);

    /** CRUD tag */
    List<TagResponse> layTatCaTag();
    TagResponse taoTag(TagRequest request);
    TagResponse capNhatTag(Long tagId, TagRequest request);
    void xoaTag(Long tagId);

    /** Đổi trạng thái xuất bản hàng loạt */
    void doiTrangThaiXuatBanNhieu(BulkActionRequest request);

    /** Xóa hàng loạt bài viết */
    void xoaNhieuBaiViet(BulkActionRequest request);

    /** Lấy chi tiết bài viết theo ID (admin, bất kể trạng thái published) */
    PostResponse layChiTietBaiViet(Long postId);

    /** Upload ảnh thumbnail bài viết */
    AdminPostMediaResponse uploadAnhBaiViet(MultipartFile file);

    /** Lấy danh mục từ điển cho frontend */
    AdminPostDictionaryResponse layDanhMucTuDien();

    /** Bật/tắt bài viết nổi bật */
    PostResponse doiTrangThaiFeatured(Long postId, boolean isFeatured);

    /** Lấy danh sách bài viết với date range + sort */
    Page<PostResponse> layDanhSachBaiViet(String keyword, Integer categoryId, Integer roleId,
                                           Boolean isPublished, int page, int size,
                                           String startDate, String endDate, String sort);

    // === QUẢN LÝ HÌNH ẢNH GALLERY ===
    /** Lấy danh sách ảnh gallery của bài viết */
    List<PostImageResponse> layAnhCuaBaiViet(Long postId);

    /** Upload ảnh vào gallery bài viết */
    PostImageResponse uploadAnhGallery(Long postId, MultipartFile file);

    /** Xóa ảnh khỏi gallery */
    void xoaAnhGallery(Long postId, Long imageId);

    /** Đổi thứ tự hiển thị ảnh */
    void doiThuTuAnh(Long postId, List<Map<String, Object>> reorderData);

    // === QUẢN LÝ FILE ĐÍNH KÈM ===
    /** Lấy danh sách file đính kèm của bài viết */
    List<PostFileResponse> layFileCuaBaiViet(Long postId);

    /** Upload file đính kèm */
    PostFileResponse uploadFileDinhKem(Long postId, MultipartFile file);

    /** Xóa file đính kèm */
    void xoaFileDinhKem(Long postId, Long fileId);

    // === XEM BÌNH LUẬN / REACTIONS / SỰ KIỆN LIÊN KẾT ===
    /** Lấy bình luận preview của bài viết (phân trang) */
    Page<PostCommentPreviewResponse> layCmtCuaBaiVietAdmin(Long postId, int page, int size);

    /** Lấy reactions chi tiết (grouped by type) */
    List<PostReactionSummary> layReactionsCuaBaiViet(Long postId);

    /** Lấy danh sách sự kiện liên kết */
    List<PostLinkedEventResponse> laySuKienLienKet(Long postId);

    /** Liên kết bài viết với buổi sự kiện */
    void lienKetSuKien(Long postId, Long ctEventId);

    /** Xóa liên kết bài viết - sự kiện */
    void xoaLienKetSuKien(Long postId, Long ctEventId);
}
