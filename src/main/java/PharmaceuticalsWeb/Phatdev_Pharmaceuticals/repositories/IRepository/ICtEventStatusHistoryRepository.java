//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventStatusHistoryRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository lịch sử trạng thái buổi sự kiện.
 * Trạng thái hiện tại = bản ghi có CHANGED_AT mới nhất.
 */
@Repository
public interface ICtEventStatusHistoryRepository extends JpaRepository<CtEventStatusHistory, Long> {

    /** Lấy toàn bộ lịch sử trạng thái của một buổi, mới nhất trước */
    List<CtEventStatusHistory> findByCtEventIdOrderByChangedAtDesc(Long ctEventId);

    /**
     * Lấy trạng thái HIỆN TẠI của buổi sự kiện.
     * = bản ghi CHANGED_AT lớn nhất của buổi đó.
     */
    @Query("SELECT h FROM CtEventStatusHistory h WHERE h.ctEvent.id = :ctEventId " +
           "AND h.changedAt = (SELECT MAX(h2.changedAt) FROM CtEventStatusHistory h2 WHERE h2.ctEvent.id = :ctEventId)")
    Optional<CtEventStatusHistory> layTrangThaiHienTai(@Param("ctEventId") Long ctEventId);
}
