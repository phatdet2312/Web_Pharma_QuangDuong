//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICmtRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Cmt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN DỮ LIỆU BÌNH LUẬN GỐC (CMT)
 * =========================================================================
 * Đảm nhiệm khai thác dữ liệu từ cấu trúc 5NF.
 * Phân tách rõ ràng các truy vấn đơn nhiệm để tối ưu hóa Execution Plan 
 * trên SQL Server, tuyệt đối tuân thủ nguyên lý Single Responsibility.
 */
@Repository
public interface ICmtRepository extends JpaRepository<Cmt, Long> {

    /** Trích xuất danh sách bình luận thuộc về một bài viết y khoa cụ thể. */
    @Query("SELECT c FROM Cmt c " +
           "WHERE EXISTS (SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id AND cpc.post.id = :postId) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtCuaBaiViet(@Param("postId") Long postId, Pageable pageable);

    /** Trích xuất danh sách bình luận thuộc về một Phiên sự kiện cụ thể. */
    @Query("SELECT c FROM Cmt c " +
           "WHERE EXISTS (SELECT 1 FROM CtEventCmt cec WHERE cec.cmt.id = c.id AND cec.ctEvent.id = :ctEventId) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtCuaBuoi(@Param("ctEventId") Long ctEventId, Pageable pageable);

    /** Tính toán tổng dung lượng bình luận của một bài viết. */
    @Query("SELECT COUNT(c) FROM Cmt c " +
           "WHERE EXISTS (SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id AND cpc.post.id = :postId)")
    long demCmtCuaBaiViet(@Param("postId") Long postId);

    /** Thống kê quy mô toàn bộ dữ liệu bình luận gốc trên hệ thống. */
    @Query("SELECT COUNT(c) FROM Cmt c")
    long demTongCmt();

    /** Đo lường chính xác lượng bình luận chưa được đưa vào quy trình kiểm duyệt. */
    @Query("SELECT COUNT(c) FROM Cmt c WHERE NOT EXISTS (SELECT 1 FROM CtCmtModerationLog l WHERE l.cmt.id = c.id)")
    long demCmtChuaDuyet();

    /** Khai thác danh sách các bình luận hoàn toàn trống vết kiểm toán. */
    @Query("SELECT c FROM Cmt c WHERE NOT EXISTS " +
           "(SELECT 1 FROM CtCmtModerationLog l WHERE l.cmt.id = c.id) " +
           "ORDER BY c.createdAt ASC")
    List<Cmt> layCmtChuaDuyet();

    /** Lấy danh sách toàn cảnh mọi bình luận gốc không phân biệt trạng thái. */
    @Query("SELECT c FROM Cmt c ORDER BY c.createdAt DESC")
    Page<Cmt> layTatCaCmt(Pageable pageable);

    /** Khai thác phân trang danh sách bình luận trống vết kiểm toán (Chờ duyệt). */
    @Query("SELECT c FROM Cmt c WHERE NOT EXISTS " +
           "(SELECT 1 FROM CtCmtModerationLog l WHERE l.cmt.id = c.id) " +
           "ORDER BY c.createdAt ASC")
    Page<Cmt> layCmtChuaDuyetPage(Pageable pageable);

    /** Phân tách bình luận dựa trên phán quyết kiểm duyệt có hiệu lực gần nhất. */
    @Query("SELECT c FROM Cmt c WHERE EXISTS " +
           "(SELECT 1 FROM CtCmtModerationLog l WHERE l.cmt.id = c.id " +
           "AND l.action.code = :actionCode " +
           "AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtCmtModerationLog l2 WHERE l2.cmt.id = c.id)) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtTheoTrangThai(@Param("actionCode") String actionCode, Pageable pageable);

    /** Gom nhóm tất cả bình luận được định tuyến từ hệ sinh thái Sự kiện. */
    @Query("SELECT c FROM Cmt c WHERE EXISTS " +
           "(SELECT 1 FROM CtEventCmt cec WHERE cec.cmt.id = c.id) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtSuKienPage(Pageable pageable);

    /** Gom nhóm tất cả bình luận được định tuyến từ hệ sinh thái Bài viết. */
    @Query("SELECT c FROM Cmt c WHERE EXISTS " +
           "(SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtBaiVietPage(Pageable pageable);

   /** Trích xuất Bình luận Bài viết sắp xếp theo Lưu lượng tương tác (Nhiều Like nhất). */
    @Query("SELECT c FROM Cmt c WHERE EXISTS (SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id AND cpc.post.id = :postId) " +
           "ORDER BY (SELECT COUNT(l) FROM CtLikeCmt l WHERE l.cmt.id = c.id) DESC, c.createdAt DESC")
    Page<Cmt> layCmtCuaBaiVietTheoLuotThich(@Param("postId") Long postId, Pageable pageable);

    /** Trích xuất Bình luận Sự kiện sắp xếp theo Lưu lượng tương tác (Nhiều Like nhất). */
    @Query("SELECT c FROM Cmt c WHERE EXISTS (SELECT 1 FROM CtEventCmt cec WHERE cec.cmt.id = c.id AND cec.ctEvent.id = :eventId) " +
           "ORDER BY (SELECT COUNT(l) FROM CtLikeCmt l WHERE l.cmt.id = c.id) DESC, c.createdAt DESC")
    Page<Cmt> layCmtCuaSuKienTheoLuotThich(@Param("eventId") Long eventId, Pageable pageable);
    
    /** Trích xuất Bình luận Bài viết sắp xếp tuần tự thời gian (Mới nhất). */
    @Query("SELECT c FROM Cmt c WHERE EXISTS (SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id AND cpc.post.id = :postId) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtCuaBaiVietTheoThoiGian(@Param("postId") Long postId, Pageable pageable);

    /** Trích xuất Bình luận Sự kiện sắp xếp tuần tự thời gian (Mới nhất). */
    @Query("SELECT c FROM Cmt c WHERE EXISTS (SELECT 1 FROM CtEventCmt cec WHERE cec.cmt.id = c.id AND cec.ctEvent.id = :eventId) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> layCmtCuaSuKienTheoThoiGian(@Param("eventId") Long eventId, Pageable pageable);

    /** Batch: đếm comment theo nhiều bài viết (tránh N+1, qua CT_POST_CMT) */
    @Query("SELECT cpc.post.id, COUNT(cpc) FROM CtPostCmt cpc WHERE cpc.post.id IN :postIds GROUP BY cpc.post.id")
    List<Object[]> demCmtTheoNhieuBaiViet(@Param("postIds") List<Long> postIds);

    /** Cỗ máy tìm kiếm Bình luận phân nhánh quản trị. */
    @Query("SELECT c FROM Cmt c WHERE " +
           "(:keyword IS NULL OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR EXISTS (SELECT 1 FROM CtCmtModerationLog l WHERE l.cmt.id = c.id AND l.action.code = :status AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtCmtModerationLog l2 WHERE l2.cmt.id = c.id))) " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR c.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR c.createdAt <= :endDate) " +
           "AND (:targetId IS NULL OR EXISTS (SELECT 1 FROM CtPostCmt cpc WHERE cpc.cmt.id = c.id AND cpc.post.id = :targetId) " +
           "                       OR EXISTS (SELECT 1 FROM CtEventCmt cec WHERE cec.cmt.id = c.id AND cec.ctEvent.id = :targetId)) " +
           "ORDER BY c.createdAt DESC")
    Page<Cmt> timKiemBinhLuanNangCao(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("targetId") Long targetId,
            Pageable pageable);
}