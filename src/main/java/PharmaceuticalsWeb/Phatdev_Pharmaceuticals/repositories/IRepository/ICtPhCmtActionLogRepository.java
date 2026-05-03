//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPhCmtActionLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPhCmtActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: SỔ TAY KIỂM TOÁN HÀNH VI PHẢN HỒI THỨ CẤP
 * =========================================================================
 */
@Repository
public interface ICtPhCmtActionLogRepository extends JpaRepository<CtPhCmtActionLog, Long> {
    
    /** Giải phóng dung lượng Audit Log khi Phản hồi bị tiêu hủy */
    @Modifying
    @Query("DELETE FROM CtPhCmtActionLog l WHERE l.phCmtId = :phCmtId")
    void xoaNhatKyTheoPhCmtId(@Param("phCmtId") Long phCmtId);

    /** Trích xuất lịch sử tác động của một phản hồi thứ cấp, ưu tiên mới nhất */
    List<CtPhCmtActionLog> findByPhCmtIdOrderByCreatedAtDesc(Long phCmtId);
}