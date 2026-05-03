//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPhCmtReportRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPhCmtReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: LƯU VẾT BÁO CÁO PHẢN HỒI (CT_PH_CMT_REPORTS)
 * =========================================================================
 */
@Repository
public interface ICtPhCmtReportRepository extends JpaRepository<CtPhCmtReport, Long> {

    /** Đếm số lượng báo cáo chờ duyệt để kích hoạt cơ chế phòng thủ tự động */
    @Query("SELECT COUNT(r) FROM CtPhCmtReport r WHERE r.phCmt.id = :phCmtId AND r.status = 'PENDING'")
    long demSoBaoCaoChuaXuLy(@Param("phCmtId") Long phCmtId);

    /** Trích xuất chi tiết đơn báo cáo phục vụ giao diện Admin */
    List<CtPhCmtReport> findByPhCmtIdOrderByCreatedAtDesc(Long phCmtId);

    /** Dọn dẹp rác hệ thống khi nhánh Phản hồi bị xóa vật lý */
    @Modifying
    @Query("DELETE FROM CtPhCmtReport r WHERE r.phCmt.id = :phCmtId")
    void xoaBaoCaoTheoPhCmtId(@Param("phCmtId") Long phCmtId);
}