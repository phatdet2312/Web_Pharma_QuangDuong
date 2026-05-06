//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAdminPostService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CategoryRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PostRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.TagRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import org.springframework.data.domain.Page;

import java.util.List;

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
}
