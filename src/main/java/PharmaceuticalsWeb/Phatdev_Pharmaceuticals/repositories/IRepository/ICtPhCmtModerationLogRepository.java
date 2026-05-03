//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPhCmtModerationLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPhCmtModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository lịch sử kiểm duyệt phản hồi (reply).
 * Cùng pattern với CtCmtModerationLog nhưng dành cho PH_CMT.
 */
@Repository
public interface ICtPhCmtModerationLogRepository extends JpaRepository<CtPhCmtModerationLog, Long> {

    /** Lấy toàn bộ lịch sử kiểm duyệt của một reply, mới nhất trước */
    List<CtPhCmtModerationLog> findByPhCmtIdOrderByCreatedAtDesc(Long phCmtId);

    /**
     * Lấy hành động kiểm duyệt MỚI NHẤT của reply.
     */
    @Query("SELECT l FROM CtPhCmtModerationLog l WHERE l.phCmt.id = :phCmtId " +
           "AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtPhCmtModerationLog l2 WHERE l2.phCmt.id = :phCmtId)")
    Optional<CtPhCmtModerationLog> layHanhDongMoiNhat(@Param("phCmtId") Long phCmtId);

    /** Đếm reply đang bị ẩn */
    @Query("SELECT COUNT(DISTINCT l.phCmt.id) FROM CtPhCmtModerationLog l " +
           "WHERE l.action.code = 'HIDE' " +
           "AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtPhCmtModerationLog l2 WHERE l2.phCmt.id = l.phCmt.id)")
    long demPhCmtDangAnHien();
}
