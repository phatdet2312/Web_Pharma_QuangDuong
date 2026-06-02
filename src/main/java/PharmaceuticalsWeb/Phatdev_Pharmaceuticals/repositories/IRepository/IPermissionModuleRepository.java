//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPermissionModuleRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PermissionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: DANH MỤC NHÓM CHỨC NĂNG (PERMISSION_MODULES)
 * =========================================================================
 */
@Repository
public interface IPermissionModuleRepository extends JpaRepository<PermissionModule, Integer> {

    // Tìm module theo mã hệ thống (VD: "POST", "EVENT")
    Optional<PermissionModule> findByModuleCode(String moduleCode);

    // Lấy danh sách sắp xếp theo thứ tự hiển thị
    List<PermissionModule> findAllByOrderByDisplayOrderAsc();
}
