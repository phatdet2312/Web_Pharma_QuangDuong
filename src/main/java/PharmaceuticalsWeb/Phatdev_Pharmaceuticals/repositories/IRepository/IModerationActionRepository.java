//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IModerationActionRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: DANH MỤC HÀNH VI KIỂM DUYỆT (MODERATION ACTIONS)
 * =========================================================================
 * Truy xuất các mã hành vi chuẩn hóa (VD: LOCK_USER, BLACKLIST_PERM)
 * phục vụ cho việc ghi sổ tay kiểm toán.
 */
@Repository
public interface IModerationActionRepository extends JpaRepository<ModerationAction, Integer> {

    /**
     * Tìm kiếm hành vi kiểm duyệt dựa trên mã Code.
     * @param code Mã hành vi chuẩn mực
     * @return Optional chứa thực thể ModerationAction
     */
    Optional<ModerationAction> findByCode(String code);
}