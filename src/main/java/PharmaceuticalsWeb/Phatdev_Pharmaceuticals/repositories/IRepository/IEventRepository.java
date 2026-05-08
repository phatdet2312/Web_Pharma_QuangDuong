//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IEventRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: CHIẾN DỊCH SỰ KIỆN (EVENTS)
 * =========================================================================
 * Đảm nhiệm việc khai thác và quản lý dữ liệu nền tảng của chiến dịch.
 * Tích hợp cỗ máy tìm kiếm đa chiều để sàng lọc chiến dịch dựa trên
 * từ khóa, phân loại, khoảng thời gian và tọa độ địa lý.
 */
@Repository
public interface IEventRepository extends JpaRepository<Event, Long> {

        /** Dò tìm Chiến dịch sự kiện thông qua đường dẫn tĩnh (Slug). */
        Optional<Event> findBySlug(String slug);

        /** Kiểm chứng tính duy nhất của đường dẫn tĩnh trong CSDL. */
        boolean existsBySlug(String slug);

        /**
         * Đếm số Chiến dịch theo Loại sự kiện — phục vụ hiển thị count trên sidebar.
         */
        long countByEventTypeId(Integer eventTypeId);

        /**
         * Cỗ máy tìm kiếm Chiến dịch sự kiện đa chiều.
         * Thuật toán tối ưu: Sử dụng mệnh đề EXISTS để liên kết sang bảng cấu trúc
         * trạm sự kiện (CT_EVENTS) nhằm sàng lọc thời gian và địa điểm. Cơ chế này
         * triệt tiêu tình trạng nhân bản dòng (Duplicate Rows) và bảo vệ RAM khỏi
         * sự cố tràn bộ nhớ khi truy vấn lượng dữ liệu khổng lồ.
         */
        /**
         * Đếm số Chiến dịch đang có ít nhất một buổi khởi diễn trong tương lai.
         * Định nghĩa "đang hoạt động": Events có startTime > now trong ít nhất 1
         * CtEvent con.
         * Dùng cho KPI "Chiến dịch đang hoạt động" trên admin/events.html.
         */
        @Query("SELECT COUNT(DISTINCT e.id) FROM Event e WHERE EXISTS " +
                        "(SELECT 1 FROM CtEvent ce WHERE ce.event.id = e.id AND ce.startTime > :now)")
        long demChienDichDangHoatDong(@Param("now") LocalDateTime now);

        /**
         * Cỗ máy tìm kiếm phân trang — startDate/endDate LUÔN là LocalDateTime thực
         * (không null).
         * Gộp hai điều kiện thời gian vào một EXISTS duy nhất để tránh null temporal
         * binding
         * trên SQL Server JDBC (Hibernate 7 không resolve SQL type cho null
         * LocalDateTime).
         */
        @Query("SELECT e FROM Event e WHERE " +
                     "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                     "AND (:eventTypeId IS NULL OR e.eventType.id = :eventTypeId) " +
                     "AND EXISTS (SELECT 1 FROM CtEvent ce1 WHERE ce1.event.id = e.id AND ce1.startTime >= :startDate AND ce1.startTime <= :endDate) " +
                     "AND (:locationId IS NULL OR EXISTS (SELECT 1 FROM CtEvent ce3 WHERE ce3.event.id = e.id AND ce3.location.id = :locationId)) " +
                     "AND (:roleId IS NULL OR EXISTS (SELECT 1 FROM CtEvent ce4 JOIN CtEventSessionRole cesr ON ce4.id = cesr.ctEvent.id WHERE ce4.event.id = e.id AND cesr.role.id = :roleId)) ")
        // +
        // "ORDER BY e.createdAt DESC"
        Page<Event> timKiemChienDich(
                        @Param("keyword") String keyword,
                        @Param("eventTypeId") Integer eventTypeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("locationId") Integer locationId,
                        @Param("roleId") Integer roleId,
                        Pageable pageable);

        /**
         * [DÀNH CHO PUBLIC] Đếm số Chiến dịch ĐÃ CÔNG BỐ theo Loại sự kiện.
         * Phục vụ hiển thị con số chính xác trên Sidebar Lọc sự kiện.
         * [TỐI ƯU HIỆU NĂNG]: Dùng Native SQL loại bỏ vòng lặp MAX() đệ quy.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT COUNT(DISTINCT e.id) FROM events e " +
                        "INNER JOIN ct_events ce ON e.id = ce.event_id " +
                        "INNER JOIN LatestStatus ls ON ce.id = ls.ct_event_id " +
                        "WHERE e.event_type_id = :eventTypeId AND ls.rn = 1 AND ls.status_code != 'DRAFT'", nativeQuery = true)
        long demChienDichPublicTheoLoai(@Param("eventTypeId") Integer eventTypeId);

        /**
         * [DÀNH CHO PUBLIC] Cỗ máy tìm kiếm Chiến dịch (Lọc bỏ DRAFT).
         * Chỉ trả về các Chiến dịch có ít nhất 1 Phiên sự kiện đã được công bố.
         */
        @Query(value = "WITH LatestStatus AS ( " +
                        "    SELECT ct_event_id, status_code, " +
                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn " +
                        "    FROM ct_event_status_history " +
                        ") " +
                        "SELECT e.* FROM events e " +
                        "WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:eventTypeId IS NULL OR e.event_type_id = :eventTypeId) " +
                        "AND EXISTS ( " +
                        "    SELECT 1 FROM ct_events ce1 " +
                        "    INNER JOIN LatestStatus ls ON ce1.id = ls.ct_event_id " +
                        "    WHERE ce1.event_id = e.id AND ce1.start_time >= :startDate AND ce1.start_time <= :endDate "
                        +
                        "    AND ls.rn = 1 AND ls.status_code != 'DRAFT' " +
                        ") " +
                        "AND (:locationId IS NULL OR EXISTS ( " +
                        "    SELECT 1 FROM ct_events ce3 " +
                        "    WHERE ce3.event_id = e.id AND ce3.location_id = :locationId " +
                        "))" +
                        "AND (:roleId IS NULL OR EXISTS ( " +
                        "    SELECT 1 FROM ct_events ce4 " +
                        "    INNER JOIN ct_event_session_roles cesr ON ce4.id = cesr.ct_event_id " +
                        "    WHERE ce4.event_id = e.id AND cesr.role_id = :roleId " +
                        "))", countQuery = "WITH LatestStatus AS ( " +
                                        "    SELECT ct_event_id, status_code, " +
                                        "    ROW_NUMBER() OVER(PARTITION BY ct_event_id ORDER BY changed_at DESC) as rn "
                                        +
                                        "    FROM ct_event_status_history " +
                                        ") " +
                                        "SELECT COUNT(e.id) FROM events e " +
                                        "WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
                                        +
                                        "AND (:eventTypeId IS NULL OR e.event_type_id = :eventTypeId) " +
                                        "AND EXISTS ( " +
                                        "    SELECT 1 FROM ct_events ce1 " +
                                        "    INNER JOIN LatestStatus ls ON ce1.id = ls.ct_event_id " +
                                        "    WHERE ce1.event_id = e.id AND ce1.start_time >= :startDate AND ce1.start_time <= :endDate "
                                        +
                                        "    AND ls.rn = 1 AND ls.status_code != 'DRAFT' " +
                                        ") " +
                                        "AND (:locationId IS NULL OR EXISTS ( " +
                                        "    SELECT 1 FROM ct_events ce3 " +
                                        "    WHERE ce3.event_id = e.id AND ce3.location_id = :locationId " +
                                        "))" +
                                        "AND (:roleId IS NULL OR EXISTS ( " +
                                        "    SELECT 1 FROM ct_events ce4 " +
                                        "    INNER JOIN ct_event_session_roles cesr ON ce4.id = cesr.ct_event_id " +
                                        "    WHERE ce4.event_id = e.id AND cesr.role_id = :roleId " +
                                        "))", nativeQuery = true)
        Page<Event> timKiemChienDichPublic(
                        @Param("keyword") String keyword,
                        @Param("eventTypeId") Integer eventTypeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("locationId") Integer locationId,
                        @Param("roleId") Integer roleId,
                        Pageable pageable);
}