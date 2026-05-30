//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPostViewLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository lượt xem bài viết.
 * Không có cột VIEW_COUNT trên POSTS — đếm từ bảng này.
 */
@Repository
public interface IPostViewLogRepository extends JpaRepository<PostViewLog, Long> {

    /** Đếm tổng lượt xem của một bài viết */
    long countByPostId(Long postId);

    /**
     * Kiểm tra IP này đã xem bài viết này trong 24h chưa.
     * Tránh ghi log trùng lặp cho cùng 1 IP trong 1 ngày.
     */
    @Query("SELECT COUNT(v) > 0 FROM PostViewLog v WHERE v.post.id = :postId " +
           "AND v.viewerIp = :ip " +
           "AND v.viewedAt >= CURRENT_TIMESTAMP - 1 DAY")
    boolean daXemTrongNgayHom(
            @Param("postId") Long postId,
            @Param("ip") String ip);

    /**
     * Đếm tổng lượt xem toàn bộ hệ thống (cho Hero Stats trang admin).
     */
    @Query("SELECT COUNT(v) FROM PostViewLog v")
    long demTongLuotXem();

    /** Xóa tất cả lượt xem của một bài viết (cascade khi xóa bài) */
    @Modifying
    @Query("DELETE FROM PostViewLog v WHERE v.post.id = :postId")
    void xoaHetLuotXemCuaBaiViet(@Param("postId") Long postId);

    /** Batch: đếm lượt xem theo nhiều bài viết (tránh N+1) */
    @Query("SELECT v.post.id, COUNT(v) FROM PostViewLog v WHERE v.post.id IN :postIds GROUP BY v.post.id")
    List<Object[]> demLuotXemTheoNhieuBaiViet(@Param("postIds") List<Long> postIds);
}
