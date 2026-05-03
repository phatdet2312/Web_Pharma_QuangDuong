//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/SpeakerAgendaServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventAgendaRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventSpeakerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtAgendaSpeaker;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventAgenda;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventSpeaker;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtAgendaSpeakerRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventAgendaRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventSpeakerRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * THỰC THI DỊCH VỤ: QUẢN LÝ LỊCH TRÌNH & DIỄN GIẢ
 * =========================================================================
 * Tuân thủ Quy tắc tối cao: Không Lambda, không Stream API.
 * Xử lý dữ liệu mảng thông qua vòng lặp For truyền thống.
 */
@Service
@RequiredArgsConstructor
public class SpeakerAgendaServiceImpl implements ISpeakerAgendaService {

    private final ICtEventRepository ctEventRepository;
    private final IEventSpeakerRepository speakerRepository;
    private final IEventAgendaRepository agendaRepository;
    private final ICtAgendaSpeakerRepository bridgeRepository;

    // =====================================================================
    // KHỐI NGHIỆP VỤ DIỄN GIẢ (SPEAKERS)
    // =====================================================================

    @Override
    public List<EventSpeakerResponse> layDSDienGiaCuaBuoi(Long ctEventId) {
        List<EventSpeaker> dsThucThe = speakerRepository.findByCtEventIdOrderByIdAsc(ctEventId);
        List<EventSpeakerResponse> dsKetQua = new ArrayList<>();

        Object[] mangThucThe = dsThucThe.toArray();
        for (int i = 0; i < mangThucThe.length; i++) {
            EventSpeaker sp = (EventSpeaker) mangThucThe[i];
            dsKetQua.add(dongGoiSpeaker(sp));
        }
        return dsKetQua;
    }

    @Override
    @Transactional
    public EventSpeakerResponse themDienGia(Long ctEventId, EventSpeakerRequest request) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để thêm Diễn giả.");
        }

        EventSpeaker speaker = new EventSpeaker();
        speaker.setCtEvent(optCtEvent.get());
        speaker.setFullName(request.getFullName().trim());
        speaker.setAcademicTitle(request.getAcademicTitle());
        speaker.setOrganization(request.getOrganization());
        speaker.setAvatarUrl(request.getAvatarUrl());
        speaker.setBio(request.getBio());

        EventSpeaker saved = speakerRepository.save(speaker);
        return dongGoiSpeaker(saved);
    }

    @Override
    @Transactional
    public EventSpeakerResponse capNhatDienGia(Long speakerId, EventSpeakerRequest request) {
        Optional<EventSpeaker> optSpeaker = speakerRepository.findById(speakerId);
        if (optSpeaker.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Hồ sơ Diễn giả.");
        }

        EventSpeaker speaker = optSpeaker.get();
        speaker.setFullName(request.getFullName().trim());
        speaker.setAcademicTitle(request.getAcademicTitle());
        speaker.setOrganization(request.getOrganization());
        speaker.setAvatarUrl(request.getAvatarUrl());
        speaker.setBio(request.getBio());

        EventSpeaker saved = speakerRepository.save(speaker);
        return dongGoiSpeaker(saved);
    }

    @Override
    @Transactional
    public void xoaDienGia(Long speakerId) {
        if (speakerRepository.existsById(speakerId) == false) {
            throw new AppException(404, "Không tìm thấy Hồ sơ Diễn giả để xóa.");
        }
        // Gỡ bỏ mọi liên kết tại các Khung giờ lịch trình trước khi xóa vật lý Diễn giả
        bridgeRepository.xoaLienKetTheoDienGia(speakerId);
        speakerRepository.deleteById(speakerId);
    }

    // =====================================================================
    // KHỐI NGHIỆP VỤ LỊCH TRÌNH (AGENDA)
    // =====================================================================

    @Override
    public List<EventAgendaResponse> layDSLichTrinhCuaBuoi(Long ctEventId) {
        List<EventAgenda> dsLichTrinh = agendaRepository.findByCtEventIdOrderByStartTimeAsc(ctEventId);
        List<EventAgendaResponse> dsKetQua = new ArrayList<>();

        Object[] mangLichTrinh = dsLichTrinh.toArray();
        for (int i = 0; i < mangLichTrinh.length; i++) {
            EventAgenda agenda = (EventAgenda) mangLichTrinh[i];
            dsKetQua.add(dongGoiAgenda(agenda));
        }
        return dsKetQua;
    }

    @Override
    @Transactional
    public EventAgendaResponse themLichTrinh(Long ctEventId, EventAgendaRequest request) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để lập Lịch trình.");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AppException(400, "Thời gian kết thúc không thể diễn ra trước thời gian bắt đầu.");
        }

        EventAgenda agenda = new EventAgenda();
        agenda.setCtEvent(optCtEvent.get());
        agenda.setStartTime(request.getStartTime());
        agenda.setEndTime(request.getEndTime());
        agenda.setSessionTitle(request.getSessionTitle().trim());
        agenda.setDescription(request.getDescription());
        
        if (request.getDisplayOrder() != null) {
            agenda.setDisplayOrder(request.getDisplayOrder());
        }

        EventAgenda savedAgenda = agendaRepository.save(agenda);

        // Thiết lập Cầu nối Định tuyến: Gắn Diễn giả vào Lịch trình này
        ganDienGiaVaoLichTrinh(savedAgenda, request.getSpeakerIds());

        return dongGoiAgenda(savedAgenda);
    }

    @Override
    @Transactional
    public EventAgendaResponse capNhatLichTrinh(Long agendaId, EventAgendaRequest request) {
        Optional<EventAgenda> optAgenda = agendaRepository.findById(agendaId);
        if (optAgenda.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy mốc Lịch trình để cập nhật.");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AppException(400, "Xung đột thời gian: Kết thúc trước khi Bắt đầu.");
        }

        EventAgenda agenda = optAgenda.get();
        agenda.setStartTime(request.getStartTime());
        agenda.setEndTime(request.getEndTime());
        agenda.setSessionTitle(request.getSessionTitle().trim());
        agenda.setDescription(request.getDescription());

        if (request.getDisplayOrder() != null) {
            agenda.setDisplayOrder(request.getDisplayOrder());
        }

        EventAgenda savedAgenda = agendaRepository.save(agenda);

        // Quá trình thay máu (Replace All): Dọn sạch cầu nối cũ, gắn cầu nối mới
        bridgeRepository.xoaLienKetTheoLichTrinh(savedAgenda.getId());
        ganDienGiaVaoLichTrinh(savedAgenda, request.getSpeakerIds());

        return dongGoiAgenda(savedAgenda);
    }

    @Override
    @Transactional
    public void xoaLichTrinh(Long agendaId) {
        if (agendaRepository.existsById(agendaId) == false) {
            throw new AppException(404, "Không tìm thấy Lịch trình để xóa.");
        }
        // Thác đổ (Cascade) thủ công: Xóa cầu nối định tuyến trước
        bridgeRepository.xoaLienKetTheoLichTrinh(agendaId);
        agendaRepository.deleteById(agendaId);
    }

    // =====================================================================
    // CÁC HÀM TIỆN ÍCH NỘI BỘ (INTERNAL HELPERS)
    // =====================================================================

    /** Gắn danh sách ID chuyên gia vào một mốc Lịch trình */
    private void ganDienGiaVaoLichTrinh(EventAgenda agenda, List<Long> speakerIds) {
        if (speakerIds == null || speakerIds.isEmpty() == true) {
            return;
        }

        Object[] mangId = speakerIds.toArray();
        for (int i = 0; i < mangId.length; i++) {
            Long sId = (Long) mangId[i];
            Optional<EventSpeaker> optSpeaker = speakerRepository.findById(sId);
            
            if (optSpeaker.isPresent() == true) {
                CtAgendaSpeaker.CtAgendaSpeakerId pkId = new CtAgendaSpeaker.CtAgendaSpeakerId(agenda.getId(), sId);
                CtAgendaSpeaker bridge = new CtAgendaSpeaker();
                bridge.setId(pkId);
                bridge.setAgenda(agenda);
                bridge.setSpeaker(optSpeaker.get());
                
                bridgeRepository.save(bridge);
            }
        }
    }

    /** Đóng gói dữ liệu Diễn giả thuần túy */
    private EventSpeakerResponse dongGoiSpeaker(EventSpeaker speaker) {
        EventSpeakerResponse resp = new EventSpeakerResponse();
        resp.setId(speaker.getId());
        resp.setCtEventId(speaker.getCtEvent().getId());
        resp.setFullName(speaker.getFullName());
        resp.setAcademicTitle(speaker.getAcademicTitle());
        resp.setOrganization(speaker.getOrganization());
        resp.setAvatarUrl(speaker.getAvatarUrl());
        resp.setBio(speaker.getBio());
        return resp;
    }

    /** Đóng gói dữ liệu Lịch trình, bao gồm việc quét mảng Diễn giả liên quan */
    private EventAgendaResponse dongGoiAgenda(EventAgenda agenda) {
        EventAgendaResponse resp = new EventAgendaResponse();
        resp.setId(agenda.getId());
        resp.setCtEventId(agenda.getCtEvent().getId());
        resp.setStartTime(agenda.getStartTime());
        resp.setEndTime(agenda.getEndTime());
        resp.setSessionTitle(agenda.getSessionTitle());
        resp.setDescription(agenda.getDescription());
        resp.setDisplayOrder(agenda.getDisplayOrder());

        // Kéo danh sách Diễn giả thực sự báo cáo trong mốc lịch trình này
        List<EventSpeaker> dsDienGia = bridgeRepository.layDSDienGiaTheoLichTrinh(agenda.getId());
        List<EventSpeakerResponse> dsDienGiaResp = new ArrayList<>();
        
        Object[] mangDienGia = dsDienGia.toArray();
        for (int i = 0; i < mangDienGia.length; i++) {
            EventSpeaker sp = (EventSpeaker) mangDienGia[i];
            dsDienGiaResp.add(dongGoiSpeaker(sp));
        }
        
        resp.setSpeakers(dsDienGiaResp);
        return resp;
    }
}