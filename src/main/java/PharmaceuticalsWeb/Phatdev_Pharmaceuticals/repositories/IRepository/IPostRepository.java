//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPostRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: TRUY VẤN DỮ LIỆU BÀI VIẾT (POSTS)
 * =========================================================================
 * Quản lý các lệnh khai thác Content Marketing, lọc theo hệ thống truy cập (Access Level).
 */
@Repository
public interface IPostRepository extends JpaRepository<Post, Long> {

    /** Dò tìm Bài viết thông qua đường dẫn tĩnh (Slug). */
    Optional<Post> findBySlug(String slug);

    /** Kiểm chứng tính duy nhất của đường dẫn tĩnh trong CSDL. */
    boolean existsBySlug(String slug);

    /** Đếm tổng dung lượng bài viết đã được mở công khai trên hệ thống. */
    long countByIsPublishedTrue();

    /**
     * Đo lường sức ảnh hưởng cá nhân: Đếm tổng số bài viết khoa học 
     * đã được xuất bản (Public) của một Tác giả cụ thể.
     * Phục vụ việc hiển thị chỉ số chuyên môn trên Thẻ Hồ sơ (Profile Card).
     */
    long countByAuthorIdAndIsPublishedTrue(Long authorId);

    /**
     * Thống kê quy mô tài nguyên Y khoa Đặc quyền (Gated Content).
     * Bất cứ bài viết nào không gắn với quyền PUBLIC (Giả sử ROLE_PUBLIC có Level = 99) 
     * hoặc có Level < 999 thì được xem là Gated.
     */
    @Query("SELECT COUNT(DISTINCT p.id) FROM Post p WHERE p.isPublished = true " +
           "AND EXISTS (SELECT 1 FROM CtPostRole cpr JOIN cpr.role r WHERE cpr.post.id = p.id AND r.roleLevel < 999)")
    long demBaiVietGated();

    /**
     * Cỗ máy tìm kiếm đa chiều dành cho hệ sinh thái người dùng (Public).
     * Chỉ khai thác các ấn phẩm đã được thiết lập cờ IS_PUBLISHED = true.
     */
    @Query("SELECT p FROM Post p WHERE p.isPublished = true " +
           "AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:roleId IS NULL OR EXISTS (SELECT 1 FROM CtPostRole cpr WHERE cpr.post.id = p.id AND cpr.role.id = :roleId))")
    Page<Post> timKiemBaiVietDaXuatBan(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("roleId") Integer roleId,
            Pageable pageable);

    /**
     * Cỗ máy tìm kiếm toàn diện dành cho Quản trị viên.
     * Cho phép truy xuất toàn bộ dữ liệu, bao gồm cả các bản nháp (Draft).
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:roleId IS NULL OR EXISTS (SELECT 1 FROM CtPostRole cpr WHERE cpr.post.id = p.id AND cpr.role.id = :roleId)) " +
           "AND (:isPublished IS NULL OR p.isPublished = :isPublished)")
    Page<Post> timKiemBaiVietAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("roleId") Integer roleId,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable);

    /**
     * Sắp xếp và trích xuất danh sách bài viết dẫn đầu về lưu lượng truy cập (View).
     */
    @Query("SELECT p FROM Post p WHERE p.isPublished = true " +
           "ORDER BY (SELECT COUNT(v) FROM PostViewLog v WHERE v.post.id = p.id) DESC")
    List<Post> layBaiVietNoiBat(Pageable pageable);

    /**
     * Dò tìm các bài viết cùng hệ sinh thái Danh mục để hiển thị Đề xuất liên quan.
     * Cơ chế loại trừ chính bài viết đang đọc thông qua excludeId.
     */
    @Query("SELECT p FROM Post p WHERE p.isPublished = true " +
           "AND p.category.id = :categoryId AND p.id != :excludeId " +
           "ORDER BY p.createdAt DESC")
    List<Post> layBaiVietCungDanhMuc(
            @Param("categoryId") Integer categoryId,
            @Param("excludeId") Long excludeId,
            Pageable pageable);
}