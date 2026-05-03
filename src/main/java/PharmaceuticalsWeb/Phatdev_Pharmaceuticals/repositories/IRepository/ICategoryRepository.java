//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICategoryRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository danh mục bài viết.
 * Cung cấp truy vấn đếm bài viết theo từng danh mục cho sidebar.
 */
@Repository
public interface ICategoryRepository extends JpaRepository<Category, Integer> {

    /** Lấy tất cả danh mục đang hoạt động, sắp xếp theo tên */
    List<Category> findByIsActiveTrueOrderByNameAsc();

    /** Đếm tổng danh mục đang hoạt động (cho Hero Stats) */
    long countByIsActiveTrue();

    /** Tìm theo slug để resolve URL */
    Optional<Category> findBySlug(String slug);

    /** Kiểm tra slug đã tồn tại chưa (dùng khi tạo/sửa) */
    boolean existsBySlug(String slug);

    /**
     * Đếm số bài viết đã xuất bản theo từng danh mục.
     * Trả về mảng [categoryId, categoryName, count].
     */
    @Query("SELECT c.id, c.name, COUNT(p.id) FROM Category c " +
           "LEFT JOIN Post p ON p.category.id = c.id AND p.isPublished = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id, c.name " +
           "ORDER BY c.name ASC")
    List<Object[]> demBaiVietTheoTungDanhMuc();
}
