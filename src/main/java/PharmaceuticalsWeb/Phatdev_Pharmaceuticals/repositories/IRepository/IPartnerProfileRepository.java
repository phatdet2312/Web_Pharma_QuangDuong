//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPartnerProfileRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PartnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: HỒ SƠ ĐỐI TÁC (PARTNER_PROFILES)
 * =========================================================================
 */
@Repository
public interface IPartnerProfileRepository extends JpaRepository<PartnerProfile, Long> {

    /**
     * Tìm hồ sơ đối tác theo USER_ID.
     * Trả về Optional rỗng nếu user chưa tạo hồ sơ (tài khoản mới).
     */
    Optional<PartnerProfile> findByUserId(Long userId);
}
