//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AdminEventServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CtEventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventStatusRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventTypeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LocationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CtEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventRegistrationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatusHistoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventTypeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LocationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventRegistration;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventStatusHistory;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Event;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventType;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Location;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRegistrationRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventStatusHistoryRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventTagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventTypeRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ILocationRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ITagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: THỰC THI NGHIỆP VỤ QUẢN TRỊ SỰ KIỆN & CHIẾN DỊCH
 * =========================================================================
 * Quản lý toàn bộ vòng đời của cấu trúc phân tầng: Chiến dịch (Events) -> Phiên
 * (CtEvents).
 * Theo dõi hồ sơ đăng ký tham dự của đối tác B2B và kiểm soát Vết trạng thái
 * (Audit History).
 * Thiết kế tuân thủ nghiêm ngặt kỹ thuật lập trình nguyên thủy, không sử dụng
 * Stream API.
 */
@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements IAdminEventService {

    private final IEventRepository eventRepository;
    private final ICtEventRepository ctEventRepository;
    private final IEventTypeRepository eventTypeRepository;
    private final ILocationRepository locationRepository;
    private final ICtEventTagRepository ctEventTagRepository;
    private final ICtEventStatusHistoryRepository statusHistoryRepository;
    private final ICtEventRegistrationRepository registrationRepository;
    private final ICtPostEventRepository ctPostEventRepository;
    private final ITagRepository tagRepository;
    private final IUserRepository userRepository;

    /**
     * Đo lường sức khỏe toàn diện của Hệ sinh thái Sự kiện trong tháng hiện tại.
     * * @return EventStatsResponse DTO chứa các số liệu tổng hợp (Số lượng phiên,
     * lượt đăng ký, lượt tham dự).
     */
    @Override
    public EventStatsResponse layThongKeAdmin() {
        ZoneId businessZone = ZoneId.of("Asia/Ho_Chi_Minh");

        YearMonth thangNay = YearMonth.now();
        LocalDateTime dauThang = thangNay.atDay(1).atStartOfDay();
        LocalDateTime cuoiThang = thangNay.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime now = LocalDateTime.now(businessZone);
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        EventStatsResponse stats = new EventStatsResponse();

        // Các KPI gốc
        stats.setEventsThisMonth(ctEventRepository.demBuoiTrongThang(dauThang, cuoiThang));
        stats.setTotalRegistrations(registrationRepository.demTongDangKy());
        stats.setTotalAttended(registrationRepository.countByStatus("ATTENDED"));
        stats.setTotalSessions(ctEventRepository.demTongBuoi());
        stats.setTotalCampaigns(eventRepository.count());
        stats.setTotalLocations(locationRepository.count());
        stats.setTotalEventTypes(eventTypeRepository.count());

        // G8: 5 KPI admin bổ sung
        stats.setActiveCampaigns(eventRepository.demChienDichDangHoatDong(now));
        stats.setUpcomingSessions(ctEventRepository.demBuoiSapToi(now));
        stats.setTodayRegistrations(registrationRepository.demDangKyHomNay(startOfToday, endOfToday));
        stats.setOnlineLocations(locationRepository.countByIsOnlineTrue());

        // Tỉ lệ tham dự: tránh chia cho 0
        long totalReg = stats.getTotalRegistrations();
        long totalAtt = stats.getTotalAttended();
        if (totalReg > 0) {
            stats.setAttendanceRate(((double) totalAtt / (double) totalReg) * 100.0);
        } else {
            stats.setAttendanceRate(0.0);
        }

        return stats;
    }

    /**
     * Truy xuất danh sách Chiến dịch Marketing (Events) kết hợp bộ lọc và phân
     * trang.
     * Cấu trúc lồng nhau: Mỗi Chiến dịch sẽ tự động quét và đính kèm các Phiên con
     * (CtEvents).
     * * @param keyword Từ khóa tìm kiếm theo tên chiến dịch.
     * 
     * @param eventTypeId Phân loại chiến dịch (Ví dụ: Hội thảo, Đào tạo).
     * @param page        Trang hiện tại (0-indexed).
     * @param size        Số lượng bản ghi trên mỗi trang.
     * @return Page<EventResponse> Trang dữ liệu đã được ánh xạ hoàn chỉnh.
     */
    @Override
    public Page<EventResponse> layDanhSachChienDich(
            String keyword, Integer eventTypeId,
            LocalDateTime startDate, LocalDateTime endDate,
            Integer locationId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        Page<Event> eventsPage = eventRepository.timKiemChienDich(kw, eventTypeId, startDate, endDate, locationId,
                pageable);
        List<Event> eventList = eventsPage.getContent();
        List<EventResponse> responseList = new ArrayList<>();

        Object[] eventArray = eventList.toArray();
        for (int i = 0; i < eventArray.length; i = i + 1) {
            Event eventEntity = (Event) eventArray[i];
            responseList.add(xayDungEventResponse(eventEntity));
        }

        return new PageImpl<>(responseList, pageable, eventsPage.getTotalElements());
    }

    /**
     * Khởi tạo một Chiến dịch Marketing mới.
     * Xử lý chuẩn hóa tên chiến dịch thành đường dẫn tĩnh (Slug) phục vụ chiến lược
     * SEO.
     */
    @Override
    @Transactional
    public EventResponse taoChienDich(EventRequest request) {
        String slug = taoSlug(request.getTitle(), request.getSlug());
        if (eventRepository.existsBySlug(slug) == true) {
            throw new AppException(400, "Đường dẫn (Slug) cho chiến dịch này đã tồn tại: " + slug);
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setSlug(slug);
        event.setDescription(request.getDescription());
        event.setThumbnailUrl(request.getThumbnailUrl());
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        if (request.getEventTypeId() != null) {
            Optional<EventType> optType = eventTypeRepository.findById(request.getEventTypeId());
            if (optType.isPresent() == true) {
                event.setEventType(optType.get());
            }
        }

        Event saved = eventRepository.save(event);
        return xayDungEventResponse(saved);
    }

    /**
     * Cập nhật thông tin nền tảng của một Chiến dịch đang tồn tại.
     * Kiểm tra chặt chẽ xung đột định danh URL (Slug) trước khi thực thi lưu trữ.
     */
    @Override
    @Transactional
    public EventResponse capNhatChienDich(Long eventId, EventRequest request) {
        Optional<Event> optEvent = eventRepository.findById(eventId);
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Chiến dịch để cập nhật.");
        }

        Event event = optEvent.get();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setThumbnailUrl(request.getThumbnailUrl());
        event.setUpdatedAt(LocalDateTime.now());

        if (request.getEventTypeId() != null) {
            Optional<EventType> optType = eventTypeRepository.findById(request.getEventTypeId());
            if (optType.isPresent() == true) {
                event.setEventType(optType.get());
            }
        }

        // Logic bảo vệ tính toàn vẹn của Slug: Chỉ kiểm tra trùng lặp nếu có sự thay
        // đổi
        if (request.getSlug() != null && request.getSlug().equals(event.getSlug()) == false) {
            if (eventRepository.existsBySlug(request.getSlug()) == true) {
                throw new AppException(400, "Đường dẫn (Slug) cập nhật đã bị trùng với chiến dịch khác.");
            }
            event.setSlug(request.getSlug());
        }

        Event saved = eventRepository.save(event);
        return xayDungEventResponse(saved);
    }

    /**
     * Gỡ bỏ một Chiến dịch khỏi hệ thống.
     * (Cảnh báo: Tầng Controller cần xác nhận các ràng buộc dữ liệu con trước khi
     * gọi hàm này).
     */
    @Override
    @Transactional
    public void xoaChienDich(Long eventId) {
        if (eventRepository.existsById(eventId) == false) {
            throw new AppException(404, "Không tìm thấy Chiến dịch cần xóa.");
        }
        eventRepository.deleteById(eventId);
    }

    /**
     * Thiết lập một Phiên sự kiện (Session) trực thuộc một Chiến dịch.
     * Đồng thời, khởi tạo bản ghi Vết trạng thái (Event Sourcing) đầu tiên là
     * DRAFT.
     */
    @Override
    @Transactional
    public CtEventResponse taoBuoi(CtEventRequest request, Long moderatorId) {
        Optional<Event> optEvent = eventRepository.findById(request.getEventId());
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Chiến dịch cha để gán phiên này vào.");
        }

        // Ràng buộc tính hợp lý của không gian thời gian
        if (request.getEndTime().isBefore(request.getStartTime()) == true
                || request.getEndTime().isEqual(request.getStartTime()) == true) {
            throw new AppException(400, "Xung đột thời gian: Giờ bế mạc phải diễn ra sau giờ khai mạc.");
        }

        CtEvent ctEvent = new CtEvent();
        ctEvent.setEvent(optEvent.get());
        ctEvent.setStartTime(request.getStartTime());
        ctEvent.setEndTime(request.getEndTime());
        ctEvent.setTotalSlots(request.getTotalSlots());
        ctEvent.setTitle(request.getTitle());
        ctEvent.setContent(request.getContent());
        ctEvent.setSeoTitle(request.getSeoTitle());
        ctEvent.setSeoDescription(request.getSeoDescription());

        if (request.getLocationId() != null) {
            Optional<Location> optLoc = locationRepository.findById(request.getLocationId());
            if (optLoc.isPresent() == true) {
                ctEvent.setLocation(optLoc.get());
            }
        }

        CtEvent saved = ctEventRepository.save(ctEvent);

        // Khởi tạo Sổ tay trạng thái (Event Sourcing Pattern)
        // Việc lưu vết này đảm bảo mọi sự thay đổi vòng đời đều có thể truy vết được.
        Optional<User> optModerator = userRepository.findById(moderatorId);
        CtEventStatusHistory history = new CtEventStatusHistory();
        history.setCtEvent(saved);
        history.setStatusCode("DRAFT");
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Khởi tạo cấu trúc phiên sự kiện mới.");

        if (optModerator.isPresent() == true) {
            history.setChangedByUser(optModerator.get());
        }
        statusHistoryRepository.save(history);

        // Nối mảng thẻ chủ đề (Tags) vào bảng trung gian
        ganTagChoBuoi(saved, request.getTagIds());

        return xayDungCtEventResponse(saved);
    }

    /**
     * Cập nhật thời lượng, sức chứa và địa điểm của một Phiên sự kiện.
     * Cơ chế dọn dẹp và làm mới hoàn toàn cấu trúc Thẻ chủ đề (Tags).
     */
    @Override
    @Transactional
    public CtEventResponse capNhatBuoi(Long ctEventId, CtEventRequest request) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để cập nhật.");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) == true) {
            throw new AppException(400, "Xung đột thời gian: Giờ bế mạc không thể trước giờ khai mạc.");
        }

        CtEvent ctEvent = optCtEvent.get();
        ctEvent.setStartTime(request.getStartTime());
        ctEvent.setEndTime(request.getEndTime());
        ctEvent.setTotalSlots(request.getTotalSlots());
        ctEvent.setTitle(request.getTitle());
        ctEvent.setContent(request.getContent());
        ctEvent.setSeoTitle(request.getSeoTitle());
        ctEvent.setSeoDescription(request.getSeoDescription());

        if (request.getLocationId() != null) {
            Optional<Location> optLoc = locationRepository.findById(request.getLocationId());
            if (optLoc.isPresent() == true) {
                ctEvent.setLocation(optLoc.get());
            }
        }

        CtEvent saved = ctEventRepository.save(ctEvent);

        // Quy trình thay máu Tag: Hủy liên kết cũ, xây dựng liên kết mới.
        ctEventTagRepository.xoaHetTagCuaBuoi(saved.getId());
        ganTagChoBuoi(saved, request.getTagIds());

        return xayDungCtEventResponse(saved);
    }

    /**
     * Loại bỏ một Phiên sự kiện khỏi hệ thống, kèm theo việc dọn dẹp các liên kết
     * ngoại.
     */
    @Override
    @Transactional
    public void xoaBuoi(Long ctEventId) {
        if (ctEventRepository.existsById(ctEventId) == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để xóa.");
        }
        ctEventTagRepository.xoaHetTagCuaBuoi(ctEventId);
        ctEventRepository.deleteById(ctEventId);
    }

    /**
     * Khai thác dữ liệu từ Sổ tay trạng thái để dựng lại toàn bộ vòng đời của Phiên
     * sự kiện.
     */
    @Override
    public List<EventStatusHistoryResponse> layLichSuTrangThai(Long ctEventId) {
        List<CtEventStatusHistory> danhSach = statusHistoryRepository.findByCtEventIdOrderByChangedAtDesc(ctEventId);
        List<EventStatusHistoryResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventStatusHistory h = (CtEventStatusHistory) arr[i];
            EventStatusHistoryResponse resp = new EventStatusHistoryResponse();
            resp.setId(h.getId());
            resp.setCtEventId(h.getCtEvent().getId());
            resp.setStatusCode(h.getStatusCode());
            resp.setChangedAt(h.getChangedAt());
            resp.setNote(h.getNote());

            if (h.getChangedByUser() != null) {
                resp.setChangedByUserId(h.getChangedByUser().getId());
                resp.setChangedByUserName(h.getChangedByUser().getFullName());
            }
            result.add(resp);
        }
        return result;
    }

    /**
     * Bổ sung một bản ghi vào Sổ tay trạng thái (Event Sourcing Pattern).
     * Trạng thái của Phiên sự kiện KHÔNG bao giờ bị cập nhật đè (Update),
     * mà luôn được tính toán dựa trên bản ghi mới nhất được Insert vào đây.
     */
    @Override
    @Transactional
    public void doiTrangThaiBuoi(EventStatusRequest request, Long moderatorId) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(request.getCtEventId());
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để tác động.");
        }

        CtEventStatusHistory history = new CtEventStatusHistory();
        history.setCtEvent(optCtEvent.get());
        history.setStatusCode(request.getStatusCode());
        history.setChangedAt(LocalDateTime.now());
        history.setNote(request.getNote());

        Optional<User> optModerator = userRepository.findById(moderatorId);
        if (optModerator.isPresent() == true) {
            history.setChangedByUser(optModerator.get());
        }

        statusHistoryRepository.save(history);
    }

    /**
     * Vận hành Cỗ máy xử lý hàng loạt.
     * Quét qua danh sách các Chiến dịch, khai thác các Trạm con trực thuộc và
     * đóng dấu đồng loạt vào Sổ tay trạng thái.
     */
    @Override
    @Transactional
    public void doiTrangThaiNhieuChienDich(BulkActionRequest request, Long moderatorId) {
        Optional<User> optModerator = userRepository.findById(moderatorId);
        if (optModerator.isPresent() == false) {
            throw new AppException(401, "Danh tính Quản trị viên không hợp lệ.");
        }
        User moderator = optModerator.get();

        Object[] arr = request.getIds().toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Long eventId = (Long) arr[i];
            Optional<Event> optEvent = eventRepository.findById(eventId);

            if (optEvent.isPresent() == true) {
                Event event = optEvent.get();
                List<CtEvent> sessions = ctEventRepository.findByEventIdOrderByStartTimeAsc(event.getId());
                Object[] sessionArr = sessions.toArray();

                for (int j = 0; j < sessionArr.length; j = j + 1) {
                    CtEvent ctEvent = (CtEvent) sessionArr[j];
                    CtEventStatusHistory history = new CtEventStatusHistory();
                    history.setCtEvent(ctEvent);
                    history.setStatusCode(request.getAction());
                    history.setChangedByUser(moderator);
                    history.setChangedAt(LocalDateTime.now());
                    history.setNote("Thiết lập trạng thái hàng loạt từ Trung tâm Điều phối.");
                    statusHistoryRepository.save(history);
                }
            }
        }
    }

    /**
     * Truy xuất danh sách hồ sơ đăng ký tham dự của một Phiên sự kiện.
     */
    @Override
    public List<EventRegistrationResponse> layDanhSachDangKy(Long ctEventId) {
        List<CtEventRegistration> danhSach = registrationRepository.findByCtEventIdOrderByRegisteredAtDesc(ctEventId);
        List<EventRegistrationResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventRegistration reg = (CtEventRegistration) arr[i];
            EventRegistrationResponse resp = new EventRegistrationResponse();
            resp.setId(reg.getId());
            resp.setCtEventId(reg.getCtEvent().getId());
            resp.setSessionStartTime(reg.getCtEvent().getStartTime());
            resp.setGuestName(reg.getGuestName());
            resp.setGuestEmail(reg.getGuestEmail());
            resp.setGuestPhone(reg.getGuestPhone());
            resp.setWorkplace(reg.getWorkplace());
            resp.setStatus(reg.getStatus());
            resp.setRegisteredAt(reg.getRegisteredAt());

            if (reg.getUser() != null) {
                resp.setUserId(reg.getUser().getId());
                resp.setUserName(reg.getUser().getFullName());
            }

            String maTrangThai = reg.getStatus();
            String chuHienThi = "Không xác định";

            if (maTrangThai != null) {
                if (maTrangThai.equals("CONFIRMED") || maTrangThai.equals("APPROVED")) {
                    chuHienThi = "Đã duyệt";
                } else if (maTrangThai.equals("PENDING")) {
                    chuHienThi = "Chờ xác nhận";
                } else if (maTrangThai.equals("ATTENDED")) {
                    chuHienThi = "Đã tham dự";
                } else if (maTrangThai.equals("CANCELLED")) {
                    chuHienThi = "Đã hủy";
                } else {
                    chuHienThi = maTrangThai;
                }
            }

            resp.setStatus(maTrangThai);
            resp.setDisplayStatus(chuHienThi);

            result.add(resp);
        }
        return result;
    }

    /**
     * Sửa đổi trạng thái của một Hồ sơ đăng ký (Ví dụ: Từ PENDING sang CONFIRMED).
     */
    @Override
    @Transactional
    public void capNhatTrangThaiDangKy(Long registrationId, String newStatus) {
        Optional<CtEventRegistration> opt = registrationRepository.findById(registrationId);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không thể tìm thấy Hồ sơ vé điện tử này.");
        }
        CtEventRegistration reg = opt.get();
        reg.setStatus(newStatus);
        registrationRepository.save(reg);
    }

    // =========================================================================
    // QUẢN LÝ DANH MỤC LÕI (LOẠI SỰ KIỆN & ĐỊA ĐIỂM)
    // =========================================================================

    @Override
    public List<EventTypeResponse> layTatCaLoaiSuKien() {
        List<EventType> danhSach = eventTypeRepository.findAllByOrderByNameAsc();
        List<EventTypeResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            EventType et = (EventType) arr[i];
            EventTypeResponse resp = new EventTypeResponse();
            resp.setId(et.getId());
            resp.setName(et.getName());
            resp.setDescription(et.getDescription());
            result.add(resp);
        }
        return result;
    }

    @Override
    @Transactional
    public EventTypeResponse taoLoaiSuKien(EventTypeRequest request) {
        EventType et = new EventType();
        et.setName(request.getName());
        et.setDescription(request.getDescription());

        EventType saved = eventTypeRepository.save(et);

        EventTypeResponse resp = new EventTypeResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        return resp;
    }

    @Override
    @Transactional
    public EventTypeResponse capNhatLoaiSuKien(Integer typeId, EventTypeRequest request) {
        Optional<EventType> opt = eventTypeRepository.findById(typeId);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Loại sự kiện để sửa.");
        }
        EventType et = opt.get();
        et.setName(request.getName());
        et.setDescription(request.getDescription());

        EventType saved = eventTypeRepository.save(et);

        EventTypeResponse resp = new EventTypeResponse();
        resp.setId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        return resp;
    }

    @Override
    @Transactional
    public void xoaLoaiSuKien(Integer typeId) {
        if (eventTypeRepository.existsById(typeId) == false) {
            throw new AppException(404, "Không tìm thấy Loại sự kiện để xóa.");
        }
        eventTypeRepository.deleteById(typeId);
    }

    @Override
    public List<LocationResponse> layTatCaDiaDiem() {
        List<Location> danhSach = locationRepository.findAllByOrderByNameAsc();
        List<LocationResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Location loc = (Location) arr[i];
            result.add(chuyenDoiLocationResponse(loc));
        }
        return result;
    }

    @Override
    @Transactional
    public LocationResponse taoDiaDiem(LocationRequest request) {
        Location loc = new Location();
        loc.setName(request.getName());
        loc.setAddress(request.getAddress());
        loc.setOnline(request.isOnline());

        Location saved = locationRepository.save(loc);
        return chuyenDoiLocationResponse(saved);
    }

    @Override
    @Transactional
    public LocationResponse capNhatDiaDiem(Integer locationId, LocationRequest request) {
        Optional<Location> opt = locationRepository.findById(locationId);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Hội trường / Địa điểm.");
        }
        Location loc = opt.get();
        loc.setName(request.getName());
        loc.setAddress(request.getAddress());
        loc.setOnline(request.isOnline());

        Location saved = locationRepository.save(loc);
        return chuyenDoiLocationResponse(saved);
    }

    @Override
    @Transactional
    public void xoaDiaDiem(Integer locationId) {
        if (locationRepository.existsById(locationId) == false) {
            throw new AppException(404, "Không tìm thấy Địa điểm để xóa.");
        }
        locationRepository.deleteById(locationId);
    }

    // =========================================================================
    // HÀM BỔ TRỢ NỘI BỘ (INTERNAL HELPERS)
    // =========================================================================

    /**
     * Duyệt mảng cấu trúc để gắn liên kết Thẻ chủ đề (Tags) vào Phiên sự kiện.
     */
    private void ganTagChoBuoi(CtEvent ctEvent, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty() == true) {
            return;
        }

        Object[] arr = tagIds.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Long tagId = (Long) arr[i];
            Optional<Tag> optTag = tagRepository.findById(tagId);

            if (optTag.isPresent() == true) {
                CtEventTag.CtEventTagId pkId = new CtEventTag.CtEventTagId(ctEvent.getId(), tagId);
                CtEventTag bridge = new CtEventTag();
                bridge.setId(pkId);
                bridge.setCtEvent(ctEvent);
                bridge.setTag(optTag.get());

                ctEventTagRepository.save(bridge);
            }
        }
    }

    /**
     * Bóc tách và định hình DTO của một Chiến dịch Marketing tổng quát.
     * Quét qua Database để gom đủ các Phiên (CtEvents) lồng ghép vào bên trong.
     */
    private EventResponse xayDungEventResponse(Event event) {
        EventResponse resp = new EventResponse();
        resp.setId(event.getId());
        resp.setTitle(event.getTitle());
        resp.setSlug(event.getSlug());
        resp.setDescription(event.getDescription());
        resp.setThumbnailUrl(event.getThumbnailUrl());
        resp.setCreatedAt(event.getCreatedAt());
        resp.setUpdatedAt(event.getUpdatedAt());

        if (event.getEventType() != null) {
            resp.setEventTypeId(event.getEventType().getId());
            resp.setEventTypeName(event.getEventType().getName());
        }

        List<CtEvent> sessions = ctEventRepository.findByEventIdOrderByStartTimeAsc(event.getId());
        List<CtEventResponse> sessionResponses = new ArrayList<>();

        Object[] arr = sessions.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEvent ce = (CtEvent) arr[i];
            sessionResponses.add(xayDungCtEventResponse(ce));
        }
        resp.setSessions(sessionResponses);

        return resp;
    }

    /**
     * Khai thác toàn diện dữ liệu của một Phiên sự kiện.
     * Xử lý thuật toán Cung-Cầu (Sức chứa - Số vé đã xuất) để tính toán Không gian
     * trống.
     */
    private CtEventResponse xayDungCtEventResponse(CtEvent ctEvent) {
        CtEventResponse resp = new CtEventResponse();
        resp.setId(ctEvent.getId());
        resp.setTitle(ctEvent.getTitle());
        resp.setContent(ctEvent.getContent());
        resp.setStartTime(ctEvent.getStartTime());
        resp.setEndTime(ctEvent.getEndTime());
        resp.setTotalSlots(ctEvent.getTotalSlots());
        resp.setSeoTitle(ctEvent.getSeoTitle());
        resp.setSeoDescription(ctEvent.getSeoDescription());

        if (ctEvent.getEvent() != null) {
            resp.setEventId(ctEvent.getEvent().getId());
            resp.setEventTitle(ctEvent.getEvent().getTitle());
            resp.setEventSlug(ctEvent.getEvent().getSlug());
        }

        if (ctEvent.getLocation() != null) {
            resp.setLocationId(ctEvent.getLocation().getId());
            resp.setLocationName(ctEvent.getLocation().getName());
            resp.setLocationAddress(ctEvent.getLocation().getAddress());
            resp.setOnline(ctEvent.getLocation().isOnline());
        }

        // Kỹ thuật tính Toán sức chứa (Capacity Analysis)
        // Ghế trống khả dụng = Tổng Slot - (Đã đăng ký PENDING/CONFIRMED)
        long registered = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
        resp.setRegisteredCount(registered);

        if (ctEvent.getTotalSlots() > 0) {
            resp.setAvailableSlots(ctEvent.getTotalSlots() - registered);
        } else {
            resp.setAvailableSlots(0); // Trả về 0 đại diện cho Không giới hạn sức chứa
        }

        // Truy vấn Vết kiểm toán để trích xuất Trạng thái vận hành hiện tại
        Optional<CtEventStatusHistory> optStatus = statusHistoryRepository.layTrangThaiHienTai(ctEvent.getId());
        if (optStatus.isPresent() == true) {
            resp.setCurrentStatus(optStatus.get().getStatusCode());
        } else {
            resp.setCurrentStatus("DRAFT"); // Giao thức an toàn mặc định
        }

        // Kéo mảng Thẻ phân tích (CT_EVENT_TAGS)
        List<Tag> tags = ctEventTagRepository.layTagCuaBuoi(ctEvent.getId());
        List<TagResponse> tagResponses = new ArrayList<>();
        Object[] tagArr = tags.toArray();

        for (int i = 0; i < tagArr.length; i = i + 1) {
            Tag tag = (Tag) tagArr[i];
            TagResponse tagResp = new TagResponse();
            tagResp.setId(tag.getId());
            tagResp.setName(tag.getName());
            tagResp.setSlug(tag.getSlug());
            tagResponses.add(tagResp);
        }
        resp.setTags(tagResponses);

        // Khai thác hệ thống Tài nguyên số (CT_POST_EVENTS) liên quan
        List<Post> relatedPosts = ctPostEventRepository.layBaiVietLienQuan(ctEvent.getId());
        List<PostResponse> relatedPostResponses = new ArrayList<>();
        Object[] postArr = relatedPosts.toArray();

        for (int i = 0; i < postArr.length; i = i + 1) {
            Post post = (Post) postArr[i];
            PostResponse postResp = new PostResponse();
            postResp.setId(post.getId());
            postResp.setTitle(post.getTitle());
            postResp.setSlug(post.getSlug());
            postResp.setSummary(post.getSummary());
            postResp.setThumbnailUrl(post.getThumbnailUrl());
            postResp.setAccessLevel(post.getAccessLevel());

            if (post.getCategory() != null) {
                postResp.setCategoryName(post.getCategory().getName());
            }
            if (post.getAuthor() != null) {
                postResp.setAuthorName(post.getAuthor().getFullName());
            }
            relatedPostResponses.add(postResp);
        }
        resp.setRelatedPosts(relatedPostResponses);

        return resp;
    }

    /**
     * Đóng gói dữ liệu Địa điểm thành DTO — bao gồm cờ isOnline (G8).
     */
    private LocationResponse chuyenDoiLocationResponse(Location loc) {
        LocationResponse resp = new LocationResponse();
        resp.setId(loc.getId());
        resp.setName(loc.getName());
        resp.setAddress(loc.getAddress());
        resp.setOnline(loc.isOnline());
        return resp;
    }

    /**
     * Thuật toán tạo định danh tĩnh (Slug) phục vụ chiến lược SEO Marketing.
     */
    private String taoSlug(String text, String customSlug) {
        if (customSlug != null && customSlug.trim().isEmpty() == false) {
            return customSlug.trim().toLowerCase();
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("");
        return noAccent.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}