//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IPostService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CategoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostDetailResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: GIAO DIỆN DỊCH VỤ BÀI VIẾT (PUBLIC FRONTEND)
 * =========================================================================
 * Cung cấp hợp đồng tiêu chuẩn cho mọi thao tác truy xuất và tương tác
 * với kho tàng kiến thức y khoa của người dùng (Public).
 */
public interface IPostService {

    /** Thống kê tổng quan cho Hero Stats trang public */
    PostStatsResponse layThongKeTrangBaiViet();

    /** Lấy danh sách danh mục kèm số bài viết cho sidebar */
    List<CategoryResponse> layDanhMucKemSoBaiViet();

    /** Lấy tag cloud kèm số lần dùng cho sidebar */
    List<TagResponse> layTagCloud();

    /**
     * Tìm kiếm + lọc bài viết đã xuất bản với phân trang.
     * keyword, categoryId, accessLevel: null = bỏ qua điều kiện đó.
     */
    Page<PostResponse> timKiemBaiViet(String keyword, Integer categoryId,
                                       String accessLevel, String sortBy,
                                       int page, int size);

    /** Lấy bài viết nổi bật (nhiều view nhất) cho Featured section */
    PostResponse layBaiVietNoiBat();

    /** Lấy chi tiết bài viết theo slug (ghi nhận lượt xem) */
    PostDetailResponse layChiTietBaiViet(String slug, String viewerIp, Long userId);

    /** Ghi nhận lượt xem (gọi sau khi load detail) */
    void ghiNhanLuotXem(Long postId, String viewerIp, Long userId);

    /**
     * Ghi nhận tương tác cảm xúc của độc giả lên Bài viết.
     * @param request Chứa ID bài viết và Mã cảm xúc
     * @param userId Người thực hiện tương tác
     */
    void thichBaiViet(LikeRequest request, Long userId);
}