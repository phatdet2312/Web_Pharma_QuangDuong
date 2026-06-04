//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtUserPermissionBlacklistRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserPermissionBlacklist;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserPermissionBlacklist.CtUserPermissionBlacklistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: MA TRẬN ĐÓNG BĂNG QUYỀN HẠT LỰU (BLACKLIST)
 * =========================================================================
 */
@Repository
public interface ICtUserPermissionBlacklistRepository extends JpaRepository<CtUserPermissionBlacklist, CtUserPermissionBlacklistId> {

    /**
     * Liệt kê toàn bộ các quyền hạt lựu đang bị đóng băng của một tài khoản.
     * @param userId Mã định danh Tài khoản
     * @return Danh sách các quyền bị cấm
     */
    List<CtUserPermissionBlacklist> findByUserId(Long userId);

    List<CtUserPermissionBlacklist> findByPermissionId(Integer permissionId);
}
