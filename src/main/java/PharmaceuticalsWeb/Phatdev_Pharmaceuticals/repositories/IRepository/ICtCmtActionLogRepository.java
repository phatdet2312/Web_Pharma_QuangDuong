//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtCmtActionLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtCmtActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: SỔ TAY KIỂM TOÁN HÀNH VI BÌNH LUẬN GỐC
 * =========================================================================
 */
@Repository
public interface ICtCmtActionLogRepository extends JpaRepository<CtCmtActionLog, Long> {
    
    /** Lệnh xóa thác đổ (Cascade) thủ công để dọn dẹp bộ nhớ */
    @Modifying
    @Query("DELETE FROM CtCmtActionLog l WHERE l.cmtId = :cmtId")
    void xoaNhatKyTheoCmtId(@Param("cmtId") Long cmtId);

    /** Trích xuất lịch sử tác động của một bình luận gốc, ưu tiên mới nhất */
    List<CtCmtActionLog> findByCmtIdOrderByCreatedAtDesc(Long cmtId);
}