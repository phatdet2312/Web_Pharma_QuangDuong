//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtFileDownloadRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtFileDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository lượt tải tài liệu.
 */
@Repository
public interface ICtFileDownloadRepository extends JpaRepository<CtFileDownload, Long> {

    /** Đếm tổng lượt tải của một file */
   long countByPostFileId(Long fileId);

    /** Đếm tổng lượt tải của tất cả file trong một bài viết */
    @Query("SELECT COUNT(d) FROM CtFileDownload d WHERE d.postFile.post.id = :postId")
    long demLuotTaiCuaBaiViet(@Param("postId") Long postId);

    /** Kiểm tra user đã tải file này chưa */
    boolean existsByPostFileIdAndUserId(Long fileId, Long userId);

    /** Đếm tổng lượt tải toàn hệ thống (cho Hero Stats) */
    @Query("SELECT COUNT(d) FROM CtFileDownload d")
    long demTongLuotTai();

    /** Xóa tất cả lượt tải của các file thuộc bài viết (cascade delete trước khi xóa post) */
    @Modifying
    @Query("DELETE FROM CtFileDownload d WHERE d.postFile.post.id = :postId")
    void xoaHetLuotTaiCuaBaiViet(@Param("postId") Long postId);

    /** Batch: đếm lượt tải theo nhiều bài viết (tránh N+1) */
    @Query("SELECT d.postFile.post.id, COUNT(d) FROM CtFileDownload d WHERE d.postFile.post.id IN :postIds GROUP BY d.postFile.post.id")
    List<Object[]> demLuotTaiTheoNhieuBaiViet(@Param("postIds") List<Long> postIds);

    /** Đếm lượt tải của từng file cụ thể trong một bài viết */
    @Query("SELECT d.postFile.id, COUNT(d) FROM CtFileDownload d WHERE d.postFile.post.id = :postId GROUP BY d.postFile.id")
    List<Object[]> demLuotTaiTheoTungFile(@Param("postId") Long postId);
}
