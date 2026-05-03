//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtPhCmtReportModLogRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPhCmtReportModLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: LƯU VẾT XỬ LÝ BÁO CÁO PHẢN HỒI THỨ CẤP
 * =========================================================================
 * Mục đích: Giao tiếp với bảng CT_PH_CMT_REPORT_MOD_LOG.
 * Thiết kế đối xứng hoàn toàn với giao diện kiểm toán Bình luận gốc.
 */
@Repository
public interface ICtPhCmtReportModLogRepository extends JpaRepository<CtPhCmtReportModLog, Long> {

    /**
     * Trích xuất toàn bộ tiến trình xử lý của một Đơn báo cáo phản hồi.
     */
    List<CtPhCmtReportModLog> findByReportIdOrderByCreatedAtDesc(Long reportId);
}