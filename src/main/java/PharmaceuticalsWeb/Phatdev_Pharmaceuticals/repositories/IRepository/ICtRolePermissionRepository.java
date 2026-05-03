//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtRolePermissionRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtRolePermission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtRolePermission.CtRolePermissionId;
import java.util.List;

public interface ICtRolePermissionRepository extends JpaRepository<CtRolePermission, CtRolePermissionId> {
    // Tìm toàn bộ quyền hạt lựu nằm trong 1 Nhóm chức vụ
    List<CtRolePermission> findByRoleId(Integer roleId);
}