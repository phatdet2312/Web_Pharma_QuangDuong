//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPermissionRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import java.util.List;
import java.util.Optional;

public interface IPermissionRepository extends JpaRepository<Permission, Integer> {
    
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * TÌM KIẾM ĐỘNG QUYỀN HẠT LỰU (LIVE SEARCH)
     * Quét trực tiếp dưới Database để giảm tải cho RAM khi danh sách quyền quá lớn.
     */
    @Query("SELECT p FROM Permission p WHERE " +
           ":keyword = '' OR " +
           "LOWER(p.permissionCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.id DESC")
    List<Permission> searchPermissions(@Param("keyword") String keyword);
}