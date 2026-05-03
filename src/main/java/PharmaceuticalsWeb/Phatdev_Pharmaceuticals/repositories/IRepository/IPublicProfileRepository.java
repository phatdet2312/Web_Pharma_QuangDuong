//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IPublicProfileRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.PublicProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: HỒ SƠ CÔNG KHAI (PUBLIC PROFILES)
 * =========================================================================
 * Mục đích: Giao tiếp trực tiếp với bảng PUBLIC_PROFILES.
 * Cung cấp công cụ để trích xuất dữ liệu hiển thị của các Tác giả, Chuyên gia.
 */
@Repository
public interface IPublicProfileRepository extends JpaRepository<PublicProfile, Long> {

    /**
     * Dò tìm hồ sơ công khai dựa trên mã định danh tài khoản cốt lõi.
     * @param userId Mã định danh của tài khoản (Bảng USERS)
     * @return Optional chứa thực thể Hồ sơ công khai nếu tồn tại
     */
    Optional<PublicProfile> findByUserId(Long userId);
}