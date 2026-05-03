//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtLikeCmtRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtLikeCmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository reaction trên comment gốc.
 */
@Repository
public interface ICtLikeCmtRepository extends JpaRepository<CtLikeCmt, CtLikeCmt.CtLikeCmtId> {

    /** Lấy tất cả reaction của một comment gốc */
    List<CtLikeCmt> findById_CmtId(Long cmtId);

    /** Lấy reaction của một user cụ thể trên một comment */
    Optional<CtLikeCmt> findById_UserIdAndId_CmtId(Long userId, Long cmtId);

    /** Đếm tổng reaction theo loại cho một comment */
    @Query("SELECT lk.loaiLike.code, COUNT(lk) FROM CtLikeCmt lk " +
           "WHERE lk.cmt.id = :cmtId GROUP BY lk.loaiLike.code")
    List<Object[]> demReactionTheLoai(@Param("cmtId") Long cmtId);

    /** Đếm tổng reaction toàn hệ thống (cho admin stats) */
    @Query("SELECT COUNT(lk) FROM CtLikeCmt lk")
    long demTongReaction();
}
