//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPostTagRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository bảng trung gian Bài viết ↔ Tag.
 */
@Repository
public interface ICtPostTagRepository extends JpaRepository<CtPostTag, CtPostTag.CtPostTagId> {

    /** Lấy tất cả tag của một bài viết */
    @Query("SELECT cpt.tag FROM CtPostTag cpt WHERE cpt.post.id = :postId")
    List<Tag> layTagCuaBaiViet(@Param("postId") Long postId);

    /** Xóa tất cả tag của bài viết (dùng khi cập nhật danh sách tag) */
    @Modifying
    @Query("DELETE FROM CtPostTag cpt WHERE cpt.post.id = :postId")
    void xoaHetTagCuaBaiViet(@Param("postId") Long postId);

    /** Đếm tổng số tag đang được dùng (cho Hero Stats) */
    @Query("SELECT COUNT(DISTINCT cpt.tag.id) FROM CtPostTag cpt")
    long demTongSoTagDangDung();
}
