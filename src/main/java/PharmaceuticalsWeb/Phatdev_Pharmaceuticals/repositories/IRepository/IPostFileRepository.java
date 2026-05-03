//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPostFileRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository tài liệu đính kèm bài viết.
 */
@Repository
public interface IPostFileRepository extends JpaRepository<PostFile, Long> {

    /** Lấy tất cả file của một bài viết */
    List<PostFile> findByPostId(Long postId);

    /** Xóa tất cả file của bài viết (khi xóa bài viết) */
    @Modifying
    @Query("DELETE FROM PostFile pf WHERE pf.post.id = :postId")
    void xoaHetFileCuaBaiViet(@Param("postId") Long postId);
}
