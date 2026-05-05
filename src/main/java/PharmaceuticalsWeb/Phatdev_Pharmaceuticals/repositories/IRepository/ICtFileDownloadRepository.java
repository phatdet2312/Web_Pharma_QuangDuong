//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtFileDownloadRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtFileDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
