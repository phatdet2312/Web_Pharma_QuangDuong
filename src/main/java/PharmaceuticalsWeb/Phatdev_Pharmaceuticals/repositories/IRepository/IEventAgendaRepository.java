//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IEventAgendaRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEventAgendaRepository extends JpaRepository<EventAgenda, Long> {
    /** Trích xuất lịch trình của Phiên sự kiện, ưu tiên sắp xếp theo thời gian bắt đầu */
    List<EventAgenda> findByCtEventIdOrderByStartTimeAsc(Long ctEventId);

    /** Xóa toàn bộ lịch trình trực thuộc một phiên sau khi đã dọn bảng nối diễn giả. */
    @Modifying
    @Query("DELETE FROM EventAgenda agenda WHERE agenda.ctEvent.id = :ctEventId")
    void xoaLichTrinhTheoBuoi(@Param("ctEventId") Long ctEventId);
}
