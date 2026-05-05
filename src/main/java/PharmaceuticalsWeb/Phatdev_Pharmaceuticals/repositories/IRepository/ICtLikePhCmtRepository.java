//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtLikePhCmtRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtLikePhCmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository reaction trên phản hồi (reply).
 */
@Repository
public interface ICtLikePhCmtRepository extends JpaRepository<CtLikePhCmt, CtLikePhCmt.CtLikePhCmtId> {

    /** Lấy tất cả reaction của một reply */
    List<CtLikePhCmt> findById_PhCmtId(Long phCmtId);

    /** Lấy reaction của user cụ thể trên một reply */
    Optional<CtLikePhCmt> findById_UserIdAndId_PhCmtId(Long userId, Long phCmtId);

    /** Đếm reaction theo loại cho một reply */
    @Query("SELECT lk.loaiLike.code, COUNT(lk) FROM CtLikePhCmt lk " +
           "WHERE lk.phCmt.id = :phCmtId GROUP BY lk.loaiLike.code")
    List<Object[]> demReactionTheLoai(@Param("phCmtId") Long phCmtId);

    /** Xóa tất cả reaction của một reply khi reply đó bị xóa */
    @Modifying
    @Query("DELETE FROM CtLikePhCmt lk WHERE lk.phCmt.id = :phCmtId")
    void xoaLikeTheoPhCmtId(@Param("phCmtId") Long phCmtId);
}
