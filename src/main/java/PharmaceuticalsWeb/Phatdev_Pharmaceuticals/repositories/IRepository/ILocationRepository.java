//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ILocationRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository địa điểm tổ chức sự kiện.
 */
@Repository
public interface ILocationRepository extends JpaRepository<Location, Integer> {

    List<Location> findAllByOrderByNameAsc();

    /** Đếm số địa điểm tổ chức trực tuyến (IS_ONLINE = 1) cho admin stats. */
    long countByIsOnlineTrue();
}
