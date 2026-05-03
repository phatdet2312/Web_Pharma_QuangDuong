//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ILoaiLikeRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.LoaiLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository loại reaction (like/love/insightful...).
 */
@Repository
public interface ILoaiLikeRepository extends JpaRepository<LoaiLike, Integer> {

    List<LoaiLike> findAllByOrderByIdAsc();

    Optional<LoaiLike> findByCode(String code);

    /** Kiểm tra mã loại phản ứng đã tồn tại chưa (dùng khi tạo mới để tránh trùng lặp). */
    boolean existsByCode(String code);
}
