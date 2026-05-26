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
        return layDSDienGiaCuaBuoi(ctEventId, true);
    }

    @Override
    public List<EventSpeakerResponse> layDSDienGiaCuaBuoi(Long ctEventId, boolean coQuyenXemChiTiet) {
        if (coQuyenXemChiTiet == false) {
            return new ArrayList<>();
        }
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
        speaker.setAvatarUrl(chuanHoaDuongDanAnh(request.getAvatarUrl()));
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
        speaker.setAvatarUrl(chuanHoaDuongDanAnh(request.getAvatarUrl()));
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
        return layDSLichTrinhCuaBuoi(ctEventId, true);
    }

    @Override
    public List<EventAgendaResponse> layDSLichTrinhCuaBuoi(Long ctEventId, boolean coQuyenXemChiTiet) {
        List<EventAgenda> dsLichTrinh = agendaRepository.findByCtEventIdOrderByStartTimeAsc(ctEventId);
        List<EventAgendaResponse> dsKetQua = new ArrayList<>();

        Object[] mangLichTrinh = dsLichTrinh.toArray();
        for (int i = 0; i < mangLichTrinh.length; i++) {
            EventAgenda agenda = (EventAgenda) mangLichTrinh[i];
            dsKetQua.add(dongGoiAgenda(agenda, coQuyenXemChiTiet));
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

        kiemTraThoiGianLichTrinhHopLe(request);
        kiemTraDanhSachDienGiaHopLe(optCtEvent.get(), request.getSpeakerIds());

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

        EventAgenda agenda = optAgenda.get();
        kiemTraThoiGianLichTrinhHopLe(request);
        kiemTraDanhSachDienGiaHopLe(agenda.getCtEvent(), request.getSpeakerIds());
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

    /** Chặn lịch trình có thời điểm kết thúc không sau thời điểm bắt đầu. */
    private void kiemTraThoiGianLichTrinhHopLe(EventAgendaRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new AppException(400, "Thời gian bắt đầu và kết thúc lịch trình không được để trống.");
        }
        if (request.getEndTime().isAfter(request.getStartTime()) == false) {
            throw new AppException(400, "Thời gian kết thúc lịch trình phải sau thời gian bắt đầu.");
        }
    }

    /** Validate speakerIds trước khi thay cầu nối để không ignore ID sai âm thầm. */
    private void kiemTraDanhSachDienGiaHopLe(CtEvent ctEvent, List<Long> speakerIds) {
        if (speakerIds == null || speakerIds.isEmpty() == true) {
            return;
        }
        kiemTraIdLongKhongLap(speakerIds, "diễn giả của lịch trình");
        Object[] mangId = speakerIds.toArray();
        for (int i = 0; i < mangId.length; i = i + 1) {
            Long speakerId = (Long) mangId[i];
            Optional<EventSpeaker> optSpeaker = speakerRepository.findById(speakerId);
            if (optSpeaker.isPresent() == false) {
                throw new AppException(400, "Không tìm thấy diễn giả có ID: " + speakerId);
            }
            if (optSpeaker.get().getCtEvent().getId().equals(ctEvent.getId()) == false) {
                throw new AppException(400, "Diễn giả không thuộc phiên sự kiện của lịch trình này.");
            }
        }
    }

    /** Chặn cùng một speakerId xuất hiện nhiều lần trong payload. */
    private void kiemTraIdLongKhongLap(List<Long> ids, String tenNghiepVu) {
        Object[] arr = ids.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Long idHienTai = (Long) arr[i];
            if (idHienTai == null) {
                throw new AppException(400, "Danh sách " + tenNghiepVu + " chứa ID rỗng.");
            }
            for (int j = i + 1; j < arr.length; j = j + 1) {
                Long idSoSanh = (Long) arr[j];
                if (idHienTai.equals(idSoSanh) == true) {
                    throw new AppException(400, "Danh sách " + tenNghiepVu + " chứa ID lặp: " + idHienTai);
                }
            }
        }
    }

    /** Validate avatar speaker gửi qua API; chỉ nhận URL do upload server sự kiện cấp. */
    private String chuanHoaDuongDanAnh(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty() == true) {
            return null;
        }
        String url = rawUrl.trim();
        if (url.length() > 255) {
            throw new AppException(400, "Đường dẫn ảnh đại diện diễn giả vượt quá 255 ký tự.");
        }
        if (url.startsWith("/uploads/events/speakers/") == true) {
            return url;
        }
        throw new AppException(400, "Ảnh diễn giả phải được tải lên qua hệ thống quản trị.");
    }

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
        return dongGoiAgenda(agenda, true);
    }

    /** Đóng gói lịch trình public; giữ teaser marketing nhưng không lộ mô tả chuyên sâu khi bị khóa */
    private EventAgendaResponse dongGoiAgenda(EventAgenda agenda, boolean coQuyenXemChiTiet) {
        EventAgendaResponse resp = new EventAgendaResponse();
        resp.setId(agenda.getId());
        resp.setCtEventId(agenda.getCtEvent().getId());
        resp.setStartTime(agenda.getStartTime());
        resp.setEndTime(agenda.getEndTime());
        resp.setSessionTitle(agenda.getSessionTitle());
        resp.setDisplayOrder(agenda.getDisplayOrder());

        if (coQuyenXemChiTiet == true) {
            resp.setDescription(agenda.getDescription());
            resp.setRestricted(false);
        } else {
            resp.setDescription("Nội dung chuyên sâu của khung này sẽ mở sau khi tài khoản đủ quyền tham dự.");
            resp.setRestricted(true);
        }

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
