//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtLikePostRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtLikePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: TRUY VẤN DỮ LIỆU CẢM XÚC BÀI VIẾT (CT_LIKEPOST)
 * =========================================================================
 * Quản lý kho lưu trữ tương tác cảm xúc trực tiếp trên các Ấn phẩm Y khoa.
 * Thiết kế đối xứng hoàn toàn với hệ thống cảm xúc Bình luận để đảm bảo
 * tính nhất quán kiến trúc và khả năng mở rộng (Scalability) của hệ thống.
 */
@Repository
public interface ICtLikePostRepository extends JpaRepository<CtLikePost, CtLikePost.CtLikePostId> {

    /**
     * Khai thác toàn bộ danh sách người dùng đã thả cảm xúc vào một Bài viết.
     * Phục vụ các nghiệp vụ phân tích dữ liệu độc giả (Data Analytics) 
     * hoặc hiển thị danh sách người tương tác trên giao diện.
     */
    List<CtLikePost> findById_PostId(Long postId);

    /**
     * Truy xuất cảm xúc của một Độc giả cụ thể trên một Bài viết.
     * Cung cấp dữ liệu để hệ thống quyết định việc tạo mới (Insert) 
     * hay thay đổi (Update) loại cảm xúc, chống thao tác trùng lặp.
     */
    Optional<CtLikePost> findById_UserIdAndId_PostId(Long userId, Long postId);

    /**
     * Thống kê quy mô cảm xúc theo từng phân loại (Ví dụ: Hữu ích, Yêu thích).
     * Đẩy tải trọng đếm (COUNT) và gom nhóm (GROUP BY) trực tiếp xuống SQL Server
     * để tối ưu hóa I/O và ngăn chặn rủi ro tràn bộ nhớ (OOM).
     */
    @Query("SELECT lk.loaiLike.code, COUNT(lk) FROM CtLikePost lk " +
           "WHERE lk.post.id = :postId GROUP BY lk.loaiLike.code")
    List<Object[]> demReactionTheLoai(@Param("postId") Long postId);

    /**
     * Đo lường tổng số lượng tương tác cảm xúc trên toàn bộ Bài viết của hệ thống.
     * Cung cấp chỉ số hiệu suất (KPI) trực tiếp cho Bảng điều khiển Quản trị (Dashboard).
     */
    @Query("SELECT COUNT(lk) FROM CtLikePost lk")
    long demTongReaction();
}