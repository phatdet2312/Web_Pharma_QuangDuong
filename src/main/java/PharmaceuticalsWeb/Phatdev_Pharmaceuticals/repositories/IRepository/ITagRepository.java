//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ITagRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository tag bài viết / sự kiện.
 */
@Repository
public interface ITagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Tag> findAllByOrderByNameAsc();

    /**
     * Lấy tag kèm số lần dùng trong bài viết đã xuất bản.
     * Dùng để vẽ Tag Cloud trên sidebar (tag nhiều lượt dùng = chữ to hơn).
     * Trả về mảng [tagId, tagName, tagSlug, usageCount].
     */
    @Query("SELECT t.id, t.name, t.slug, COUNT(cpt.post.id) FROM Tag t " +
           "LEFT JOIN CtPostTag cpt ON cpt.tag.id = t.id " +
           "LEFT JOIN Post p ON p.id = cpt.post.id AND p.isPublished = true " +
           "GROUP BY t.id, t.name, t.slug " +
           "ORDER BY COUNT(cpt.post.id) DESC")
    List<Object[]> layTagKemSoLanDung();
}
