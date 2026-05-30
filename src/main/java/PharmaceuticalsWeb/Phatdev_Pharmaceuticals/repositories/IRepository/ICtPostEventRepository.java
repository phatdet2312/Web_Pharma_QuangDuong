//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPostEventRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository bảng trung gian Buổi sự kiện ↔ Bài viết liên quan.
 */
@Repository
public interface ICtPostEventRepository extends JpaRepository<CtPostEvent, CtPostEvent.CtPostEventId> {

    /** Lấy bài viết liên quan của một buổi sự kiện */
    @Query("SELECT cpe.post FROM CtPostEvent cpe WHERE cpe.ctEvent.id = :ctEventId")
    List<Post> layBaiVietLienQuan(@Param("ctEventId") Long ctEventId);

    /** Xóa tất cả liên kết bài viết của buổi sự kiện */
    @Modifying
    @Query("DELETE FROM CtPostEvent cpe WHERE cpe.ctEvent.id = :ctEventId")
    void xoaHetBaiVietCuaBuoi(@Param("ctEventId") Long ctEventId);

    /** Xóa tất cả liên kết sự kiện của bài viết (cascade delete trước khi xóa post) */
    @Modifying
    @Query("DELETE FROM CtPostEvent cpe WHERE cpe.post.id = :postId")
    void xoaHetSuKienCuaBaiViet(@Param("postId") Long postId);

    /** Lấy tất cả buổi sự kiện liên kết với bài viết */
    @Query("SELECT cpe.ctEvent FROM CtPostEvent cpe WHERE cpe.post.id = :postId")
    List<CtEvent> laySuKienCuaBaiViet(@Param("postId") Long postId);

    /** Kiểm tra liên kết post-event đã tồn tại */
    @Query("SELECT CASE WHEN COUNT(cpe) > 0 THEN true ELSE false END " +
           "FROM CtPostEvent cpe WHERE cpe.post.id = :postId AND cpe.ctEvent.id = :ctEventId")
    boolean kiemTraLienKetTonTai(@Param("postId") Long postId, @Param("ctEventId") Long ctEventId);
}
