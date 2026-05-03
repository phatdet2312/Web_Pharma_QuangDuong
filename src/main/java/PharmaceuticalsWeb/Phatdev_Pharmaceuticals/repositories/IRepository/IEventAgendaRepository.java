//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/repositories/IRepository/IEventAgendaRepository.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEventAgendaRepository extends JpaRepository<EventAgenda, Long> {
    /** Trích xuất lịch trình của Phiên sự kiện, ưu tiên sắp xếp theo thời gian bắt đầu */
    List<EventAgenda> findByCtEventIdOrderByStartTimeAsc(Long ctEventId);
}