//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IDistrictRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: QUẬN/HUYỆN (DISTRICTS)
 * =========================================================================
 */
@Repository
public interface IDistrictRepository extends JpaRepository<District, Integer> {

    /** Lấy toàn bộ quận/huyện theo tỉnh — phục vụ dropdown địa chỉ phụ thuộc */
    List<District> findByProvinceIdOrderByNameAsc(Integer provinceId);
}
