//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtCmtReportRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtCmtReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: LƯU VẾT BÁO CÁO BÌNH LUẬN GỐC (CT_CMT_REPORTS)
 * =========================================================================
 * Quản lý kho dữ liệu chứa các cảnh báo vi phạm.
 * Cung cấp các hàm Aggregate trực tiếp tại tầng SQL Server để tránh 
 * quá tải bộ nhớ RAM khi phân tích dữ liệu rác.
 */
@Repository
public interface ICtCmtReportRepository extends JpaRepository<CtCmtReport, Long> {

    /** * Đo lường tổng số lượng đơn báo cáo đang chờ xử lý (PENDING) 
     * của một bình luận cụ thể để kích hoạt cơ chế ẩn tự động.
     */
    @Query("SELECT COUNT(r) FROM CtCmtReport r WHERE r.cmt.id = :cmtId AND r.status = 'PENDING'")
    long demSoBaoCaoChuaXuLy(@Param("cmtId") Long cmtId);

    /** * Trích xuất danh sách chi tiết các báo cáo thuộc về một bình luận 
     * để Quản trị viên đối soát lý do và truy vết IP kẻ gian.
     */
    List<CtCmtReport> findByCmtIdOrderByCreatedAtDesc(Long cmtId);

    /** * Lệnh dọn dẹp hệ sinh thái (Cascade Delete): Tiêu hủy toàn bộ
     * các báo cáo liên quan khi Bình luận gốc bị xóa vật lý.
     */
    @Modifying
    @Query("DELETE FROM CtCmtReport r WHERE r.cmt.id = :cmtId")
    void xoaBaoCaoTheoCmtId(@Param("cmtId") Long cmtId);

    /**
     * Đếm số bình luận gốc có ít nhất một báo cáo đang chờ xử lý (PENDING).
     * Phép đếm DISTINCT tránh đếm trùng khi một bình luận có nhiều báo cáo.
     * Dùng cho KPI "Báo cáo chờ xử lý" trên admin/comments.html.
     */
    @Query("SELECT COUNT(DISTINCT r.cmt.id) FROM CtCmtReport r WHERE r.status = 'PENDING'")
    long demCmtCoBaoCaoChoXuLy();
}