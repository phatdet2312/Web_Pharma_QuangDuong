//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtUserLoginLogRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: NHẬT KÝ ĐĂNG NHẬP (CT_USER_LOGIN_LOG)
 * =========================================================================
 */
@Repository
public interface ICtUserLoginLogRepository extends JpaRepository<CtUserLoginLog, Long> {

    /** Lấy lịch sử đăng nhập phân trang — phục vụ tab "Lịch sử đăng nhập" */
    org.springframework.data.domain.Page<CtUserLoginLog> findByUserIdOrderByCreatedAtDesc(
        Long userId, org.springframework.data.domain.Pageable pageable);
}
