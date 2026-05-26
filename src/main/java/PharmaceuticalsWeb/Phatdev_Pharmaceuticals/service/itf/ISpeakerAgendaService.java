//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/ISpeakerAgendaService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventAgendaRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventSpeakerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;

import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ: QUẢN LÝ LỊCH TRÌNH VÀ DIỄN GIẢ SỰ KIỆN (CHUẨN MÙ)
 * =========================================================================
 */
public interface ISpeakerAgendaService {
    
    // --- NGHIỆP VỤ DIỄN GIẢ ---
    List<EventSpeakerResponse> layDSDienGiaCuaBuoi(Long ctEventId);
    List<EventSpeakerResponse> layDSDienGiaCuaBuoi(Long ctEventId, boolean coQuyenXemChiTiet);
    EventSpeakerResponse themDienGia(Long ctEventId, EventSpeakerRequest request);
    EventSpeakerResponse capNhatDienGia(Long speakerId, EventSpeakerRequest request);
    void xoaDienGia(Long speakerId);

    // --- NGHIỆP VỤ LỊCH TRÌNH ---
    List<EventAgendaResponse> layDSLichTrinhCuaBuoi(Long ctEventId);
    List<EventAgendaResponse> layDSLichTrinhCuaBuoi(Long ctEventId, boolean coQuyenXemChiTiet);
    EventAgendaResponse themLichTrinh(Long ctEventId, EventAgendaRequest request);
    EventAgendaResponse capNhatLichTrinh(Long agendaId, EventAgendaRequest request);
    void xoaLichTrinh(Long agendaId);
}
