//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPostImageRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository ảnh đính kèm bài viết.
 */
@Repository
public interface IPostImageRepository extends JpaRepository<PostImage, Long> {

    /** Lấy tất cả ảnh của bài viết sắp xếp theo DISPLAY_ORDER */
    List<PostImage> findByPostIdOrderByDisplayOrderAsc(Long postId);

    /** Xóa tất cả ảnh của bài viết (khi xóa bài viết) */
    @Modifying
    @Query("DELETE FROM PostImage pi WHERE pi.post.id = :postId")
    void xoaHetAnhCuaBaiViet(@Param("postId") Long postId);

    /** Đếm tổng ảnh của bài viết */
    long countByPostId(Long postId);
}
