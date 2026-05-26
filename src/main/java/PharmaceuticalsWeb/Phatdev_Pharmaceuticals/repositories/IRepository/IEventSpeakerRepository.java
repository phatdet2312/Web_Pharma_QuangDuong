//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IEventSpeakerRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventSpeaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEventSpeakerRepository extends JpaRepository<EventSpeaker, Long> {
    /** Trích xuất toàn bộ diễn giả trực thuộc một Phiên sự kiện (CT_EVENT) */
    List<EventSpeaker> findByCtEventIdOrderByIdAsc(Long ctEventId);

    /** Xóa diễn giả thuộc một phiên sau khi đã dọn bảng nối lịch trình. */
    @Modifying
    @Query("DELETE FROM EventSpeaker speaker WHERE speaker.ctEvent.id = :ctEventId")
    void xoaDienGiaTheoBuoi(@Param("ctEventId") Long ctEventId);
}
