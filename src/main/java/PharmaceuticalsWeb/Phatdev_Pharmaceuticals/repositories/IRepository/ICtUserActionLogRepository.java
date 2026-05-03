//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtUserActionLogRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: NHẬT KÝ HÀNH ĐỘNG NGƯỜI DÙNG (CT_USER_ACTION_LOG)
 * =========================================================================
 */
@Repository
public interface ICtUserActionLogRepository extends JpaRepository<CtUserActionLog, Long> {

    /** Lấy lịch sử hành động phân trang — phục vụ tab "Hành động của tôi" */
    org.springframework.data.domain.Page<CtUserActionLog> findByUserIdOrderByCreatedAtDesc(
        Long userId, org.springframework.data.domain.Pageable pageable);
}
