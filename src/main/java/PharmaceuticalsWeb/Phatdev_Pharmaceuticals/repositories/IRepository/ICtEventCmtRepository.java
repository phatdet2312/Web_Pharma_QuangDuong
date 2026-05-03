//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtEventCmtRepository.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventCmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository bảng định tuyến Buổi sự kiện ↔ Comment gốc.
 */
@Repository
public interface ICtEventCmtRepository extends JpaRepository<CtEventCmt, CtEventCmt.CtEventCmtId> {

    /** Xóa liên kết comment khỏi buổi sự kiện */
    @Modifying
    @Query("DELETE FROM CtEventCmt cec WHERE cec.cmt.id = :cmtId")
    void xoaLienKetTheoCmt(@Param("cmtId") Long cmtId);

    /** Kiểm tra comment đã thuộc buổi này chưa */
    boolean existsById_CtEventIdAndId_CmtId(Long ctEventId, Long cmtId);

    /**
     * Truy vấn Batch: Ánh xạ danh sách cmtId sang thông tin Chiến dịch sự kiện nguồn gốc.
     * Trả về mảng Object[]{cmtId, ctEventId, eventTitle, eventSlug} cho mỗi liên kết.
     * Join qua CtEvent → Event để lấy title và slug cấp chiến dịch (hiển thị trên admin).
     */
    @Query("SELECT cec.cmt.id, ce.id, e.title, e.slug FROM CtEventCmt cec " +
           "JOIN cec.ctEvent ce JOIN ce.event e WHERE cec.cmt.id IN :cmtIds")
    List<Object[]> layCmtIdToCtEventMapping(@Param("cmtIds") List<Long> cmtIds);
}
