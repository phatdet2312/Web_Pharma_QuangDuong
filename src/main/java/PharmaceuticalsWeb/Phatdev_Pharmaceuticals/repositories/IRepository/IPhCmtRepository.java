//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPhCmtRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PhCmt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository phản hồi bình luận (reply).
 */
@Repository
public interface IPhCmtRepository extends JpaRepository<PhCmt, Long> {

    /**
     * Lấy tất cả reply của một comment gốc.
     * Sắp xếp theo CREATED_AT tăng dần (cũ nhất trước → đúng luồng hội thoại).
     */
    List<PhCmt> findByRootCmtIdOrderByCreatedAtAsc(Long rootCmtId);

    /**
     * Lấy phản hồi cấp 2 trực tiếp dưới một bình luận gốc.
     * Các tầng sâu hơn được tải bằng endpoint riêng để tránh đẩy cả cây reply lên frontend.
     */
    Page<PhCmt> findByRootCmtIdAndParentPhIsNullOrderByCreatedAtAsc(Long rootCmtId, Pageable pageable);

    /** Đếm phản hồi cấp 2 trực tiếp dưới một bình luận gốc */
    long countByRootCmtIdAndParentPhIsNull(Long rootCmtId);

    /** Đếm tổng tất cả phản hồi mọi cấp dưới một bình luận gốc (kiểu Facebook) */
    long countByRootCmtId(Long rootCmtId);

    /** Đếm tổng reply toàn hệ thống (cho admin stats) */
    @Query("SELECT COUNT(p) FROM PhCmt p")
    long demTongPhCmt();

    /**
     * Lấy reply chưa kiểm duyệt.
     */
    @Query("SELECT p FROM PhCmt p WHERE NOT EXISTS " +
           "(SELECT 1 FROM CtPhCmtModerationLog l WHERE l.phCmt.id = p.id) " +
           "ORDER BY p.createdAt ASC")
    List<PhCmt> layPhCmtChuaDuyet();
}
