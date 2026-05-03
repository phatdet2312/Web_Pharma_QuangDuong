//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtUserRoleRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserRole.CtUserRoleId;
import java.util.List;

public interface ICtUserRoleRepository extends JpaRepository<CtUserRole, CtUserRoleId> {
    // Tìm xem 1 User đang mang những chức vụ gì
    List<CtUserRole> findByUserId(Long userId);
    
    // Tìm xem chức vụ này đang được giao cho những ai (Dùng để check trước khi xóa Role)
    List<CtUserRole> findByRoleId(Integer roleId);
    
    // Tước toàn bộ chức vụ của 1 User
    void deleteByUserId(Long userId);
}