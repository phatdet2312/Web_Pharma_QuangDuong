//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventTagRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository bảng trung gian Buổi sự kiện ↔ Tag.
 */
@Repository
public interface ICtEventTagRepository extends JpaRepository<CtEventTag, CtEventTag.CtEventTagId> {

    /** Lấy tag của một buổi sự kiện */
    @Query("SELECT cet.tag FROM CtEventTag cet WHERE cet.ctEvent.id = :ctEventId")
    List<Tag> layTagCuaBuoi(@Param("ctEventId") Long ctEventId);

    /** Xóa tất cả tag của buổi sự kiện */
    @Modifying
    @Query("DELETE FROM CtEventTag cet WHERE cet.ctEvent.id = :ctEventId")
    void xoaHetTagCuaBuoi(@Param("ctEventId") Long ctEventId);
}
