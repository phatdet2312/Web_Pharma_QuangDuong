//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IUserRoleRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import java.util.List;
import java.util.Optional;

public interface IUserRoleRepository extends JpaRepository<UserRole, Integer> {
    
    Optional<UserRole> findByRoleName(String roleName);

    /**
     * TÌM CHỨC VỤ YẾU NHẤT (roleLevel cao nhất) ĐỂ GÁN MẶC ĐỊNH KHI ĐĂNG KÝ.
     * Nếu không có chức vụ nào ngoài SUPERADMIN → trả về Optional.empty().
     */
    Optional<UserRole> findTopByRoleLevelGreaterThanOrderByRoleLevelDesc(int minLevel);

    /**
     * TÌM KIẾM ĐỘNG CHỨC VỤ (LIVE SEARCH)
     * Tối ưu hóa cho thanh tìm kiếm Real-time tại giao diện RBAC.
     */
    @Query("SELECT r FROM UserRole r WHERE " +
           ":keyword = '' OR " +
           "LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY r.roleLevel ASC")
    List<UserRole> searchRoles(@Param("keyword") String keyword);
}