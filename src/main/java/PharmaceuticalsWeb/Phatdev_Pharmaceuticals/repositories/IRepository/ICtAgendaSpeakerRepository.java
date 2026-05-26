//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/ICtAgendaSpeakerRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtAgendaSpeaker;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventSpeaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICtAgendaSpeakerRepository extends JpaRepository<CtAgendaSpeaker, CtAgendaSpeaker.CtAgendaSpeakerId> {
    
    /** Kéo danh sách diễn giả được phân công trong một mốc lịch trình cụ thể */
    @Query("SELECT cas.speaker FROM CtAgendaSpeaker cas WHERE cas.agenda.id = :agendaId")
    List<EventSpeaker> layDSDienGiaTheoLichTrinh(@Param("agendaId") Long agendaId);

    /** Dọn dẹp liên kết khi XÓA hoặc CẬP NHẬT Lịch trình */
    @Modifying
    @Query("DELETE FROM CtAgendaSpeaker cas WHERE cas.agenda.id = :agendaId")
    void xoaLienKetTheoLichTrinh(@Param("agendaId") Long agendaId);

    /** Dọn toàn bộ liên kết diễn giả-lịch trình của một phiên trước khi xóa phiên. */
    @Modifying
    @Query("DELETE FROM CtAgendaSpeaker cas WHERE cas.agenda.ctEvent.id = :ctEventId")
    void xoaLienKetTheoBuoi(@Param("ctEventId") Long ctEventId);

    /** Dọn dẹp liên kết khi XÓA Diễn giả khỏi hệ thống */
    @Modifying
    @Query("DELETE FROM CtAgendaSpeaker cas WHERE cas.speaker.id = :speakerId")
    void xoaLienKetTheoDienGia(@Param("speakerId") Long speakerId);
}
