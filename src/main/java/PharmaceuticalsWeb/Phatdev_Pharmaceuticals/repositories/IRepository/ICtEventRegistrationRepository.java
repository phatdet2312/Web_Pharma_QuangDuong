//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventRegistrationRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: LƯU VẾT ĐĂNG KÝ SỰ KIỆN
 * =========================================================================
 */
@Repository
public interface ICtEventRegistrationRepository extends JpaRepository<CtEventRegistration, Long> {

       /** Trích xuất danh sách khách mời của một Phiên sự kiện cụ thể */
       List<CtEventRegistration> findByCtEventIdOrderByRegisteredAtDesc(Long ctEventId);

       /** Trích xuất lịch sử tham gia sự kiện của một Tài khoản */
       List<CtEventRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);

       /** Đo lường tổng lưu lượng vé đã phát hành trên toàn hệ thống */
       @Query("SELECT COUNT(r) FROM CtEventRegistration r")
       long demTongDangKy();

       /** Đo lường lưu lượng theo trạng thái cụ thể */
       long countByStatus(String status);

       /** Đối soát xem một tài khoản đã sở hữu vé tham gia phiên này chưa */
       boolean existsByCtEventIdAndUserId(Long ctEventId, Long userId);

       /** Đo lường lượng khách thực tế tham dự của một Phiên sự kiện */
       long countByCtEventIdAndStatus(Long ctEventId, String status);

       /**
        * Đo lường lượng vé phát hành trong một khung thời gian tuyến tính.
        * Tối ưu hóa Execution Plan trên SQL Server bằng cách loại bỏ hàm nội tại,
        * tận dụng hoàn toàn B-Tree Index của trường REGISTERED_AT.
        */
       @Query("SELECT COUNT(r) FROM CtEventRegistration r " +
                     "WHERE r.registeredAt >= :startOfDay AND r.registeredAt < :endOfDay")
       long demDangKyHomNay(
                     @Param("startOfDay") LocalDateTime startOfDay,
                     @Param("endOfDay") LocalDateTime endOfDay);

       /**
        * [DÀNH CHO PUBLIC] Đo lường tổng lưu lượng vé đã phát hành của các Phiên sự
        * kiện ĐÃ CÔNG BỐ.
        * [TỐI ƯU HIỆU NĂNG]: Áp dụng Window Function (ROW_NUMBER) qua Inline View.
        * Quét lịch sử 1 lần (O(N)), lấy ra trạng thái mới nhất (rn = 1) để đối soát.
        * Chặn việc đếm nhầm các vé thuộc về phiên bị hạ cấp về DRAFT.
        */
       @Query(value = "WITH LatestStatus AS ( " +
                   "    SELECT ct_event_id, status_code, " +
                   "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                   "    FROM ct_event_status_history " +
                   ") " +
                   "SELECT COUNT(r.id) FROM ct_event_registrations r " +
                   "INNER JOIN LatestStatus ls ON r.ct_event_id = ls.ct_event_id " +
                   "WHERE ls.rn = 1 AND ls.status_code != 'DRAFT'", 
           nativeQuery = true)
    long demTongDangKyPublic();
}