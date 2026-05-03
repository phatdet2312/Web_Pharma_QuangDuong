//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IProvinceRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN TRUY VẤN: TỈNH/THÀNH PHỐ (PROVINCES)
 * =========================================================================
 */
@Repository
public interface IProvinceRepository extends JpaRepository<Province, Integer> {

    /** Lấy toàn bộ tỉnh/thành phố theo thứ tự tên — phục vụ dropdown địa chỉ */
    List<Province> findAllByOrderByNameAsc();
}
