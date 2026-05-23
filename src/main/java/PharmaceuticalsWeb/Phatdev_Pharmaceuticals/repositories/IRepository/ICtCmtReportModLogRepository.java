//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtCmtReportModLogRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtCmtReportModLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: LƯU VẾT XỬ LÝ BÁO CÁO BÌNH LUẬN GỐC
 * =========================================================================
 * Mục đích: Giao tiếp với bảng CT_CMT_REPORT_MOD_LOG. 
 * Cung cấp công cụ để trích xuất lịch sử xử lý của Quản trị viên đối với 
 * một đơn báo cáo cụ thể.
 */
@Repository
public interface ICtCmtReportModLogRepository extends JpaRepository<CtCmtReportModLog, Long> {

    /**
     * Trích xuất toàn bộ tiến trình xử lý của một Đơn báo cáo.
     * Sắp xếp theo thời gian giảm dần (Mới nhất nằm trên cùng).
     * @param reportId Mã định danh đơn báo cáo
     * @return Danh sách các bản ghi nhật ký kiểm toán
     */
    List<CtCmtReportModLog> findByReportIdOrderByCreatedAtDesc(Long reportId);

    /**
     * Dọn nhật ký xử lý báo cáo trước khi xóa báo cáo của một bình luận gốc.
     */
    @Modifying
    @Query("DELETE FROM CtCmtReportModLog l WHERE l.report.id IN " +
           "(SELECT r.id FROM CtCmtReport r WHERE r.cmt.id = :cmtId)")
    void xoaLogTheoCmtId(@Param("cmtId") Long cmtId);
}
