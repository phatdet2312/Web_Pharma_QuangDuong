//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IRolesService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ QUẢN LÝ QUYỀN VÀ TÌM KIẾM NGƯỜI DÙNG
 * =========================================================================
 */
public interface IRolesService {

    List<User> getAllUsersPaged(int pageNo, int pageSize, String sortBy);

    int getTotalPages(int pageSize);

    List<User> searchUsers(String keyword);

    /**
     * Thuật toán lọc đa chiều đẩy tải trọng xuống Cơ sở dữ liệu.
     */
    List<User> searchAdvanced(String keyword, String status, String roleName, int pageNo, int pageSize);

    /**
     * Đo lường tổng dung lượng để tính toán phân trang cho bộ lọc đa chiều.
     */
    long countSearchAdvanced(String keyword, String status, String roleName);

    /**
     * Nghiệp vụ kiểm duyệt hàng loạt (Bulk Action).
     */
    void bulkLockUnlock(List<Long> userIds, boolean lock, String reason, User currentUser);
}