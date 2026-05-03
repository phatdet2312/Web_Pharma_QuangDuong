//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IWardRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: PHƯỜNG/XÃ/THỊ TRẤN (WARDS)
 * =========================================================================
 */
@Repository
public interface IWardRepository extends JpaRepository<Ward, Integer> {

    /** Lấy toàn bộ phường/xã theo quận/huyện — phục vụ dropdown địa chỉ phụ thuộc */
    List<Ward> findByDistrictIdOrderByNameAsc(Integer districtId);
}
