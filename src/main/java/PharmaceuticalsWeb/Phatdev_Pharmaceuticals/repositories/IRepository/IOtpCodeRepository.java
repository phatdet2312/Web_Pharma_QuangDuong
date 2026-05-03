//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IOtpCodeRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.OtpCode;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IOtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByEmailAndCodeAndUsedFalseAndExpiryAtAfterOrderByCreatedAtDesc(
        String email, String code, LocalDateTime now);

    int countByEmailAndCreatedAtAfter(String email, LocalDateTime fiveMinutesAgo);

    /** Lấy danh sách OTP phân trang — democach2 pattern, thay thế Top5 cứng */
    Page<OtpCode> findByEmailOrderByCreatedAtDesc(
        String email, Pageable pageable);
}