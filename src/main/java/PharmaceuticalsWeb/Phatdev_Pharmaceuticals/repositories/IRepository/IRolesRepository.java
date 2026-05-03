//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IRolesRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN CSDL: QUẢN LÝ ĐỊNH DANH & LỌC ĐA CHIỀU
 * =========================================================================
 */
@Repository
public interface IRolesRepository extends JpaRepository<User, Long> {

    default List<User> findAllWithPagination(int pageNo, int pageSize, String sortBy) {
        Page<User> page = findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy)));
        return page.getContent();
    }

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    default int getTotalPages(int pageSize) {
        long count = count();
        return (int) Math.ceil((double) count / pageSize);
    }

    /**
     * THUẬT TOÁN LỌC ĐA CHIỀU (ADVANCED FILTERING)
     * Đẩy tải trọng JOIN 3 bảng (USERS, CT_USER_ROLES, USER_ROLES) xuống CSDL.
     * Sử dụng LEFT JOIN để đảm bảo tài khoản chưa được phân quyền vẫn xuất hiện.
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN CtUserRole cur ON u.id = cur.userId " +
           "LEFT JOIN UserRole ur ON cur.roleId = ur.id " +
           "WHERE (:keyword = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status = 'ALL' OR (:status = 'ACTIVE' AND u.locked = false) OR (:status = 'LOCKED' AND u.locked = true)) " +
           "AND (:roleName = 'ALL' OR ur.roleName = :roleName) " +
           "ORDER BY u.id DESC")
    List<User> searchAdvanced(
            @Param("keyword") String keyword, 
            @Param("status") String status, 
            @Param("roleName") String roleName, 
            Pageable pageable);

    /**
     * Đo lường tổng dung lượng bản ghi thỏa mãn điều kiện của Bộ lọc Đa chiều,
     * phục vụ thuật toán phân trang (Pagination).
     */
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
           "LEFT JOIN CtUserRole cur ON u.id = cur.userId " +
           "LEFT JOIN UserRole ur ON cur.roleId = ur.id " +
           "WHERE (:keyword = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status = 'ALL' OR (:status = 'ACTIVE' AND u.locked = false) OR (:status = 'LOCKED' AND u.locked = true)) " +
           "AND (:roleName = 'ALL' OR ur.roleName = :roleName)")
    long countSearchAdvanced(
            @Param("keyword") String keyword, 
            @Param("status") String status, 
            @Param("roleName") String roleName);
}