//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository buổi sự kiện (CT_EVENTS).
 * Cung cấp truy vấn đếm slot còn trống, lọc theo tháng cho mini calendar.
 */
@Repository
public interface ICtEventRepository extends JpaRepository<CtEvent, Long> {

        /** Lấy tất cả buổi của một chiến dịch sắp xếp theo giờ bắt đầu */
        List<CtEvent> findByEventIdOrderByStartTimeAsc(Long eventId);

        /**
         * Đếm số slot đã đăng ký (trạng thái PENDING hoặc CONFIRMED).
         * Slot trống = TOTAL_SLOTS - số đăng ký này.
         */
        @Query("SELECT COUNT(r) FROM CtEventRegistration r WHERE r.ctEvent.id = :ctEventId " +
                        "AND r.status IN ('PENDING', 'CONFIRMED')")
        long demSlotDaDangKy(@Param("ctEventId") Long ctEventId);

        /**
         * Lấy tất cả buổi trong một tháng cụ thể (cho Mini Calendar).
         * Trả về ngày START_TIME để đánh dấu ngày có sự kiện.
         */
        @Query("SELECT ce FROM CtEvent ce WHERE ce.startTime >= :tuNgay AND ce.startTime < :denNgay " +
                        "ORDER BY ce.startTime ASC")
        List<CtEvent> layBuoiTrongThang(
                        @Param("tuNgay") LocalDateTime tuNgay,
                        @Param("denNgay") LocalDateTime denNgay);

        /** Đếm tổng số buổi sự kiện (cho admin stats) */
        @Query("SELECT COUNT(ce) FROM CtEvent ce")
        long demTongBuoi();

        /**
         * Đếm số buổi có START_TIME trong tháng này (cho Hero Stats).
         */
        @Query("SELECT COUNT(ce) FROM CtEvent ce WHERE ce.startTime >= :dauThang AND ce.startTime < :cuoiThang")
        long demBuoiTrongThang(
                        @Param("dauThang") LocalDateTime dauThang,
                        @Param("cuoiThang") LocalDateTime cuoiThang);

        /**
         * Đếm số buổi sự kiện có startTime lớn hơn thời điểm hiện tại (sắp diễn ra).
         * Dùng cho KPI "Buổi sắp tới" trên admin/events.html.
         */
        @Query("SELECT COUNT(ce) FROM CtEvent ce WHERE ce.startTime > :now")
        long demBuoiSapToi(@Param("now") LocalDateTime now);

        /**
         * [DÀNH CHO PUBLIC] Lấy tất cả buổi của một chiến dịch ĐÃ CÔNG BỐ.
         * Thuật toán: Trạng thái mới nhất trong History không được là DRAFT.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT ce.* FROM ct_events ce " +
                        "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                        "WHERE ce.event_id = :eventId AND ls.rn = 1 AND ls.status_code != 'DRAFT' " +
                        "ORDER BY ce.start_time ASC", nativeQuery = true)
        List<CtEvent> layBuoiDaCongBoCuaChienDich(@Param("eventId") Long eventId);

        /**
         * [DÀNH CHO PUBLIC] Lấy buổi trong tháng ĐÃ CÔNG BỐ (cho Mini Calendar).
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT ce.* FROM ct_events ce " +
                        "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                        "WHERE ce.start_time >= :tuNgay AND ce.start_time < :denNgay " +
                        "AND ls.rn = 1 AND ls.status_code != 'DRAFT' " +
                        "ORDER BY ce.start_time ASC", nativeQuery = true)
        List<CtEvent> layBuoiDaCongBoTrongThang(
                        @Param("tuNgay") LocalDateTime tuNgay,
                        @Param("denNgay") LocalDateTime denNgay);

        /**
         * [DÀNH CHO PUBLIC] Đếm tổng số lượng Phiên sự kiện ĐÃ CÔNG BỐ.
         * Thuật toán: Bỏ qua các bản ghi có trạng thái hiện tại là DRAFT.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT COUNT(ce.id) FROM ct_events ce " +
                        "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                        "WHERE ls.rn = 1 AND ls.status_code != 'DRAFT'", nativeQuery = true)
        long demTongBuoiDaCongBo();

        /**
         * [DÀNH CHO PUBLIC] Đếm lượng Phiên sự kiện khởi diễn trong tháng ĐÃ CÔNG BỐ.
         * Phục vụ chỉ số KPI trên giao diện người dùng.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT COUNT(ce.id) FROM ct_events ce " +
                        "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                        "WHERE ce.start_time >= :dauThang AND ce.start_time < :cuoiThang " +
                        "AND ls.rn = 1 AND ls.status_code != 'DRAFT'", nativeQuery = true)
        long demBuoiDaCongBoTrongThang(
                        @Param("dauThang") LocalDateTime dauThang,
                        @Param("cuoiThang") LocalDateTime cuoiThang);

        /**
         * [DÀNH CHO PUBLIC] Đếm tổng số lượng Phiên sự kiện ĐÃ CÔNG BỐ (Có tích hợp Bộ lọc Đa chiều).
         * Thuật toán: Dùng mệnh đề IS NULL để bỏ qua tham số nếu Frontend không truyền lên.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                       "    SELECT ct_event_id, status_code, " +
                       "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                       "    FROM ct_event_status_history " +
                       ") " +
                       "SELECT COUNT(ce.id) FROM ct_events ce " +
                       "INNER JOIN events e ON ce.event_id = e.id " +
                       "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                       "WHERE ls.rn = 1 AND ls.status_code != 'DRAFT' " +
                       "AND (:type IS NULL OR e.event_type_id = :type) " +
                       "AND ce.start_time >= :startDate AND ce.start_time <= :endDate", 
               nativeQuery = true)
        long demTongBuoiPublicCoLoc(
                        @Param("type") Integer type,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

}
