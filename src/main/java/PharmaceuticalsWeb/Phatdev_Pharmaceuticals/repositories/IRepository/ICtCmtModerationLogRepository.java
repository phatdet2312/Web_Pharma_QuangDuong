//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtCmtModerationLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtCmtModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository lịch sử kiểm duyệt comment gốc.
 * Trạng thái hiện tại = hành động mới nhất trong log.
 */
@Repository
public interface ICtCmtModerationLogRepository extends JpaRepository<CtCmtModerationLog, Long> {

    /** Lấy toàn bộ lịch sử kiểm duyệt của một comment, mới nhất trước */
    List<CtCmtModerationLog> findByCmtIdOrderByCreatedAtDesc(Long cmtId);

    /**
     * Lấy hành động kiểm duyệt MỚI NHẤT của comment.
     * Trạng thái hiện tại của comment = action.code của bản ghi này.
     */
    @Query("SELECT l FROM CtCmtModerationLog l WHERE l.cmt.id = :cmtId " +
           "AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtCmtModerationLog l2 WHERE l2.cmt.id = :cmtId)")
    Optional<CtCmtModerationLog> layHanhDongMoiNhat(@Param("cmtId") Long cmtId);

    /**
     * Đếm comment đang bị ẩn (hành động mới nhất là HIDE).
     * Dùng cho admin stats.
     */
    @Query("SELECT COUNT(DISTINCT l.cmt.id) FROM CtCmtModerationLog l " +
           "WHERE l.action.code = 'HIDE' " +
           "AND l.createdAt = (SELECT MAX(l2.createdAt) FROM CtCmtModerationLog l2 WHERE l2.cmt.id = l.cmt.id)")
    long demCmtDangAnHien();

    /** Xóa toàn bộ log của một comment khi comment đó bị xóa */
    @Modifying
    @Query("DELETE FROM CtCmtModerationLog l WHERE l.cmt.id = :cmtId")
    void xoaLogTheoCmtId(@Param("cmtId") Long cmtId);
}
