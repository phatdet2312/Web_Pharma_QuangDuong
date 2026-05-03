//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtUserModerationLogRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: SỔ TAY KIỂM TOÁN HỆ THỐNG (AUDIT LOGS)
 * =========================================================================
 */
@Repository
public interface ICtUserModerationLogRepository extends JpaRepository<CtUserModerationLog, Long> {

    /**
     * Khai thác toàn bộ lịch sử bị tác động của một Tài khoản cụ thể.
     * Sắp xếp theo trình tự thời gian đảo ngược để phục vụ giao diện Timeline.
     * @param targetUserId Mã định danh Tài khoản bị tác động
     * @return Danh sách các bản ghi nhật ký
     */
    List<CtUserModerationLog> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    // [THÊM MỚI] Truy vấn phân trang có bộ lọc theo loại hành vi.
    // Dùng subquery thay vì JOIN vì actionId là plain FK (không có @ManyToOne).
    // actionCode = 'ALL' → không lọc loại; actionCode khác → lọc đúng code.
    @Query(value = "SELECT log FROM CtUserModerationLog log " +
                   "WHERE log.targetUserId = :targetUserId " +
                   "AND (:actionCode = 'ALL' OR log.actionId IN " +
                   "    (SELECT a.id FROM ModerationAction a WHERE a.code = :actionCode)) " +
                   "ORDER BY log.createdAt DESC",
           countQuery = "SELECT COUNT(log) FROM CtUserModerationLog log " +
                        "WHERE log.targetUserId = :targetUserId " +
                        "AND (:actionCode = 'ALL' OR log.actionId IN " +
                        "    (SELECT a.id FROM ModerationAction a WHERE a.code = :actionCode))")
    Page<CtUserModerationLog> layLichSuCoLocVaPhanTrang(
            @Param("targetUserId") Long targetUserId,
            @Param("actionCode") String actionCode,
            Pageable pageable);

    /**
     * Tìm bản ghi BLACKLIST_PERM mới nhất cho một permission cụ thể của user.
     * Dùng để lấy lý do + người blacklist + thời điểm — hiển thị trên tab Phân quyền.
     */
    @Query("SELECT log FROM CtUserModerationLog log " +
           "WHERE log.targetUserId = :userId " +
           "AND log.permissionId = :permissionId " +
           "AND log.actionId IN (SELECT a.id FROM ModerationAction a WHERE a.code = 'BLACKLIST_PERM') " +
           "ORDER BY log.createdAt DESC")
    java.util.Optional<CtUserModerationLog> timLyDoBlacklist(
            @Param("userId") Long userId,
            @Param("permissionId") Integer permissionId);

    // Đếm tổng số lần bị khóa (LOCK_USER) trong toàn bộ lịch sử.
    // Được gọi độc lập để Risk Indicator luôn chính xác dù đang ở trang nào.
    @Query("SELECT COUNT(log) FROM CtUserModerationLog log " +
           "WHERE log.targetUserId = :targetUserId " +
           "AND log.actionId IN (SELECT a.id FROM ModerationAction a WHERE a.code = 'LOCK_USER')")
    long demSoLanBiKhoa(@Param("targetUserId") Long targetUserId);
}