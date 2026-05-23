//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/ICommentService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentModerationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LoaiLikeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ReplyRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminCmtContextResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtActionLogResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CommentStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LoaiLikeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PhCmtResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ BÌNH LUẬN (ĐIỀU PHỐI PUBLIC VÀ ADMIN)
 * =========================================================================
 */
public interface ICommentService {

    /** Lấy danh sách comment + reply của bài viết (phân trang theo comment gốc) */
    Page<CmtResponse> layCmtCuaBaiViet(Long postId, String sortBy, int page, int size, Long userId);

    /** Lấy danh sách comment + reply của buổi sự kiện */
    Page<CmtResponse> layCmtCuaBuoi(Long ctEventId, String sortBy, int page, int size, Long userId);

    /** Gửi comment gốc mới cho bài viết */
    CmtResponse guiCmtBaiViet(CommentRequest request, Long userId);

    /** Gửi comment gốc mới cho buổi sự kiện */
    CmtResponse guiCmtSuKien(CommentRequest request, Long userId);

    /** Gửi reply cho comment gốc hoặc reply khác */
    PhCmtResponse guiPhCmt(ReplyRequest request, Long userId);

    /** Lấy phản hồi cấp 2 trực tiếp của một bình luận gốc */
    Page<PhCmtResponse> layPhanHoiCapHai(Long rootCmtId, int page, int size, Long userId);

    /** Lấy phản hồi cấp 3 hiển thị dưới một phản hồi cấp 2 */
    Page<PhCmtResponse> layPhanHoiCapBa(Long phCmtId, int page, int size, Long userId);

    /** Chỉnh sửa bình luận gốc — Kích hoạt ghi log Audit Tự thân */
    CmtResponse capNhatCmt(Long cmtId, CommentRequest request, Long userId);

    /** Xóa bình luận gốc — Kích hoạt ghi log Audit Tự thân */
    void xoaCmt(Long cmtId, Long userId);

    /** Chỉnh sửa phản hồi — Kích hoạt ghi log Audit Tự thân */
    PhCmtResponse capNhatPhCmt(Long phCmtId, ReplyRequest request, Long userId);

    /** Xóa phản hồi — Kích hoạt ghi log Audit Tự thân */
    void xoaPhCmt(Long phCmtId, Long userId);

    /** Thêm / đổi reaction trên comment gốc */
    void thichCmt(LikeRequest request, Long userId);

    /** Thêm / đổi reaction trên reply */
    void thichPhCmt(LikeRequest request, Long userId);

    /** Lấy tất cả loại reaction để hiển thị picker */
    List<LoaiLikeResponse> layTatCaLoaiLike();

    /** Trích xuất Sổ tay hành động tự thân của Bình luận gốc */
    List<CmtActionLogResponse> layLichSuTuThanCmt(Long cmtId);

    /** Trích xuất Sổ tay hành động tự thân của Phản hồi thứ cấp */
    List<CmtActionLogResponse> layLichSuTuThanPhCmt(Long phCmtId);

    /** ADMIN: Thống kê bình luận */
    CommentStatsResponse layThongKeBinhLuan();

    /** ADMIN: Lấy comment chờ duyệt */
    List<CmtResponse> layCmtChuaDuyet();

    /**
     * ADMIN: Lấy danh sách comment phân trang theo tab — trả về context nguồn gốc.
     * Mỗi phần tử bọc CmtResponse + targetType/targetId/targetTitle/targetSlug.
     */
    Page<AdminCmtContextResponse> layDanhSachBinhLuan(String status, String targetType, int page, int size);

    /**
     * ADMIN: Tìm kiếm bộ lọc đa chiều — trả về context nguồn gốc.
     * Batch-load mapping sau khi query để tránh N+1.
     */
    Page<AdminCmtContextResponse> timKiemBinhLuanAdmin(String keyword, String status, LocalDateTime startDate, LocalDateTime endDate, Long targetId, int page, int size);

    /** ADMIN G2: Tạo mới Loại Phản ứng */
    LoaiLikeResponse taoLoaiLike(LoaiLikeRequest request);

    /** ADMIN G2: Cập nhật Loại Phản ứng theo ID */
    LoaiLikeResponse capNhatLoaiLike(Integer id, LoaiLikeRequest request);

    /** ADMIN G2: Xóa Loại Phản ứng theo ID */
    void xoaLoaiLike(Integer id);

    /** ADMIN: Kiểm duyệt comment / reply */
    void kiemDuyetBinhLuan(CommentModerationRequest request, Long moderatorId);

    /** ADMIN: Kiểm duyệt hàng loạt (APPROVE/HIDE/WARN) */
    void kiemDuyetNhieu(BulkActionRequest request, Long moderatorId);

    /** ADMIN: Xóa vật lý comment gốc (cascade reply + reaction + bridge) */
    void xoaCmtVatLy(Long cmtId);

    /** ADMIN: Xóa vật lý reply */
    void xoaPhCmtVatLy(Long phCmtId);

    /** ADMIN: Xóa hàng loạt comment gốc */
    void xoaNhieuCmt(BulkActionRequest request);
}
