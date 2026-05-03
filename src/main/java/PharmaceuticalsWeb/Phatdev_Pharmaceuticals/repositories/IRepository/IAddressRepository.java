//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IAddressRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: ĐỊA CHỈ DOANH NGHIỆP (ADDRESSES)
 * =========================================================================
 */
@Repository
public interface IAddressRepository extends JpaRepository<Address, Long> {

    /** Lấy toàn bộ địa chỉ của một đối tác */
    List<Address> findByPartnerIdOrderByIsDefaultDescIdAsc(Long partnerId);

    /** Đếm tổng số địa chỉ của một đối tác — phục vụ thống kê Profile */
    long countByPartnerId(Long partnerId);

    /**
     * Reset IS_DEFAULT = 0 cho tất cả địa chỉ của một đối tác.
     * Phải gọi trước khi set IS_DEFAULT = 1 cho địa chỉ mới mặc định
     * để đảm bảo tính nhất quán — mỗi đối tác chỉ có tối đa 1 địa chỉ mặc định.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.partnerId = :partnerId")
    void resetTatCaDiaDiChi(@Param("partnerId") Long partnerId);
}
