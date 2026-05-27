//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/AdminEventServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CtEventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventStatusRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventTypeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LocationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminEventDictionaryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.AdminEventMediaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CtEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventRegistrationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatusHistoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventTypeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LocationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.PostResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.StatusOptionResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.TagResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventRegistration;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventSessionRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventStatusHistory;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtEventTag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtPostEvent;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Event;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.EventType;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Location;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Post;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Tag;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtAgendaSpeakerRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventCmtRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRegistrationRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventSessionRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventStatusHistoryRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtEventTagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtPostEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventAgendaRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventSpeakerRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IEventTypeRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ILocationRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPostRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ITagRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.EventStatusDisplayPolicy;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils.ImagePathUtil;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils.PagingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    private static final long MAX_EVENT_IMAGE_BYTES = 5L * 1024L * 1024L;

    private static final String[] EVENT_STATUS_CODES = {
            "DRAFT", "OPEN", "UPCOMING", "ONGOING", "FULL", "CANCELLED", "FINISHED", "ENDED"
    };

    private static final String[] REGISTRATION_STATUS_CODES = {
            "PENDING", "CONFIRMED", "APPROVED", "ATTENDED", "CANCELLED"
    };

    private final IEventRepository eventRepository;
    private final ICtEventRepository ctEventRepository;
    private final IEventTypeRepository eventTypeRepository;
    private final ILocationRepository locationRepository;
    private final ICtEventTagRepository ctEventTagRepository;
    private final ICtEventStatusHistoryRepository statusHistoryRepository;
    private final ICtEventRegistrationRepository registrationRepository;
    private final ICtPostEventRepository ctPostEventRepository;
    private final ICtEventCmtRepository ctEventCmtRepository;
    private final ICtAgendaSpeakerRepository ctAgendaSpeakerRepository;
    private final IEventAgendaRepository eventAgendaRepository;
    private final IEventSpeakerRepository eventSpeakerRepository;
    private final IPostRepository postRepository;
    private final ITagRepository tagRepository;
    private final IUserRepository userRepository;
    private final ICtEventSessionRoleRepository ctEventSessionRoleRepository;
    private final IUserRoleRepository userRoleRepository;
    private final EventStatusDisplayPolicy eventStatusDisplayPolicy;

    @Value("${pharma.upload.base-path:./uploads}")
    private String uploadBasePath;

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
     * Trả toàn bộ mã trạng thái mà admin được phép chọn.
     * Frontend chỉ render danh sách này, không tự duy trì enum riêng.
     */
    @Override
    public AdminEventDictionaryResponse layDanhMucTrangThai() {
        AdminEventDictionaryResponse response = new AdminEventDictionaryResponse();
        response.setEventStatuses(taoDanhSachTrangThaiBuoi());
        response.setRegistrationStatuses(taoDanhSachTrangThaiDangKy());
        return response;
    }

    /**
     * Lưu ảnh đại diện chiến dịch vào namespace upload riêng của phân hệ sự kiện.
     */
    @Override
    public AdminEventMediaResponse uploadAnhChienDich(MultipartFile file) {
        return luuAnhSuKien(file, "campaigns");
    }

    /**
     * Lưu ảnh đại diện diễn giả vào namespace upload riêng của phân hệ sự kiện.
     */
    @Override
    public AdminEventMediaResponse uploadAnhDienGia(MultipartFile file) {
        return luuAnhSuKien(file, "speakers");
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
            Integer locationId, Integer roleId, int page, int size) {

        int pageDaKiemSoat = PagingUtil.chuanHoaPage(page);
        int sizeDaKiemSoat = PagingUtil.chuanHoaSize(size);
        Pageable pageable = PageRequest.of(pageDaKiemSoat, sizeDaKiemSoat, Sort.by(Sort.Direction.DESC, "createdAt"));

        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        boolean canLocTheoBuoi = startDate != null || endDate != null || locationId != null || roleId != null;

        LocalDateTime ngayBatDauLoc = startDate;
        if (ngayBatDauLoc == null) {
            ngayBatDauLoc = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }

        LocalDateTime ngayKetThucLoc = endDate;
        if (ngayKetThucLoc == null) {
            ngayKetThucLoc = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        }

        Page<Event> eventsPage = eventRepository.timKiemChienDich(
                kw, eventTypeId, canLocTheoBuoi, ngayBatDauLoc, ngayKetThucLoc, locationId, roleId, pageable);
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
        event.setThumbnailUrl(ImagePathUtil.chuanHoaDuongDanAnh(
                request.getThumbnailUrl(), 255, "/uploads/events/campaigns/"));
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        event.setEventType(layLoaiSuKienBatBuoc(request.getEventTypeId()));

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
        event.setThumbnailUrl(ImagePathUtil.chuanHoaDuongDanAnh(
                request.getThumbnailUrl(), 255, "/uploads/events/campaigns/"));
        event.setUpdatedAt(LocalDateTime.now());

        event.setEventType(layLoaiSuKienBatBuoc(request.getEventTypeId()));

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
        List<CtEvent> sessions = ctEventRepository.findByEventIdOrderByStartTimeAsc(eventId);
        Object[] sessionArr = sessions.toArray();
        for (int i = 0; i < sessionArr.length; i = i + 1) {
            CtEvent ctEvent = (CtEvent) sessionArr[i];
            xoaBuoi(ctEvent.getId());
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
        Event eventCha = layChienDichBatBuoc(request.getEventId());
        Location diaDiem = layDiaDiemBatBuoc(request.getLocationId());
        kiemTraThoiGianBuoiHopLe(request.getStartTime(), request.getEndTime());
        kiemTraDanhSachRoleHopLe(request.getRoleIds());
        kiemTraDanhSachTagHopLe(request.getTagIds());
        kiemTraDanhSachBaiVietHopLe(request.getRelatedPostIds());

        CtEvent ctEvent = new CtEvent();
        ctEvent.setEvent(eventCha);
        ctEvent.setStartTime(request.getStartTime());
        ctEvent.setEndTime(request.getEndTime());
        ctEvent.setTotalSlots(request.getTotalSlots());
        ctEvent.setTitle(request.getTitle());
        ctEvent.setContent(request.getContent());
        ctEvent.setSeoTitle(request.getSeoTitle());
        ctEvent.setSeoDescription(request.getSeoDescription());

        ctEvent.setLocation(diaDiem);

        CtEvent saved = ctEventRepository.save(ctEvent);

        // Khởi tạo Sổ tay trạng thái (Event Sourcing Pattern)
        // Việc lưu vết này đảm bảo mọi sự thay đổi vòng đời đều có thể truy vết được.
        User moderator = layModeratorBatBuoc(moderatorId);
        CtEventStatusHistory history = new CtEventStatusHistory();
        history.setCtEvent(saved);
        history.setStatusCode("DRAFT");
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Khởi tạo cấu trúc phiên sự kiện mới.");
        history.setChangedByUser(moderator);
        statusHistoryRepository.save(history);

        ganQuyenChoBuoi(saved, request.getRoleIds());
        // Nối mảng thẻ chủ đề (Tags) vào bảng trung gian
        ganTagChoBuoi(saved, request.getTagIds());
        ganBaiVietChoBuoi(saved, request.getRelatedPostIds());

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

        CtEvent ctEvent = optCtEvent.get();
        kiemTraEventIdKhongDoi(ctEvent, request.getEventId());
        Location diaDiem = layDiaDiemBatBuoc(request.getLocationId());
        kiemTraThoiGianBuoiHopLe(request.getStartTime(), request.getEndTime());
        kiemTraDanhSachRoleHopLe(request.getRoleIds());
        kiemTraDanhSachTagHopLe(request.getTagIds());
        kiemTraDanhSachBaiVietHopLe(request.getRelatedPostIds());

        ctEvent.setStartTime(request.getStartTime());
        ctEvent.setEndTime(request.getEndTime());
        ctEvent.setTotalSlots(request.getTotalSlots());
        ctEvent.setTitle(request.getTitle());
        ctEvent.setContent(request.getContent());
        ctEvent.setSeoTitle(request.getSeoTitle());
        ctEvent.setSeoDescription(request.getSeoDescription());

        ctEvent.setLocation(diaDiem);

        CtEvent saved = ctEventRepository.save(ctEvent);

        ctEventSessionRoleRepository.xoaHetQuyenCuaBuoi(saved.getId());
        ganQuyenChoBuoi(saved, request.getRoleIds());
        // Quy trình thay máu Tag: Hủy liên kết cũ, xây dựng liên kết mới.
        ctEventTagRepository.xoaHetTagCuaBuoi(saved.getId());
        ganTagChoBuoi(saved, request.getTagIds());
        ctPostEventRepository.xoaHetBaiVietCuaBuoi(saved.getId());
        ganBaiVietChoBuoi(saved, request.getRelatedPostIds());

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
        ctEventCmtRepository.xoaLienKetTheoBuoi(ctEventId);
        ctAgendaSpeakerRepository.xoaLienKetTheoBuoi(ctEventId);
        eventAgendaRepository.xoaLichTrinhTheoBuoi(ctEventId);
        eventSpeakerRepository.xoaDienGiaTheoBuoi(ctEventId);
        registrationRepository.xoaDangKyTheoBuoi(ctEventId);
        statusHistoryRepository.xoaLichSuTheoBuoi(ctEventId);
        ctPostEventRepository.xoaHetBaiVietCuaBuoi(ctEventId);
        ctEventTagRepository.xoaHetTagCuaBuoi(ctEventId);
        ctEventSessionRoleRepository.xoaHetQuyenCuaBuoi(ctEventId);
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
            resp.setDisplayStatus(eventStatusDisplayPolicy.layNhanAdmin(h.getStatusCode()));
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
        kiemTraTrangThaiBuoiHopLe(request.getStatusCode());
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(request.getCtEventId());
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để tác động.");
        }
        User moderator = layModeratorBatBuoc(moderatorId);

        CtEventStatusHistory history = new CtEventStatusHistory();
        history.setCtEvent(optCtEvent.get());
        history.setStatusCode(request.getStatusCode().trim().toUpperCase());
        history.setChangedAt(LocalDateTime.now());
        history.setNote(request.getNote());
        history.setChangedByUser(moderator);

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
        kiemTraTrangThaiBuoiHopLe(request.getAction());
        User moderator = layModeratorBatBuoc(moderatorId);

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
                    history.setStatusCode(request.getAction().trim().toUpperCase());
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
        kiemTraTrangThaiDangKyHopLe(newStatus);
        Optional<CtEventRegistration> opt = registrationRepository.findById(registrationId);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không thể tìm thấy Hồ sơ vé điện tử này.");
        }
        CtEventRegistration reg = opt.get();
        String trangThaiMoi = newStatus.trim().toUpperCase();
        kiemTraSucChuaKhiDoiTrangThaiDangKy(reg, trangThaiMoi);
        reg.setStatus(trangThaiMoi);
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
        long soChienDichDangDung = eventRepository.countByEventTypeId(typeId);
        if (soChienDichDangDung > 0) {
            throw new AppException(409, "Không thể xóa loại sự kiện đang được chiến dịch sử dụng.");
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
        long soPhienDangDung = ctEventRepository.countByLocationId(locationId);
        if (soPhienDangDung > 0) {
            throw new AppException(409, "Không thể xóa địa điểm đang được phiên sự kiện sử dụng.");
        }
        locationRepository.deleteById(locationId);
    }

    // =========================================================================
    // HÀM BỔ TRỢ NỘI BỘ (INTERNAL HELPERS)
    // =========================================================================

    /** Lấy loại sự kiện bắt buộc để tránh lỗi ràng buộc NOT NULL từ database. */
    private EventType layLoaiSuKienBatBuoc(Integer eventTypeId) {
        if (eventTypeId == null) {
            throw new AppException(400, "Loại sự kiện không được để trống.");
        }
        Optional<EventType> optType = eventTypeRepository.findById(eventTypeId);
        if (optType.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy loại sự kiện đã chọn.");
        }
        return optType.get();
    }

    /** Lấy địa điểm bắt buộc để bảo vệ đúng ràng buộc LOCATION_ID của CT_EVENTS. */
    private Location layDiaDiemBatBuoc(Integer locationId) {
        if (locationId == null) {
            throw new AppException(400, "Địa điểm tổ chức không được để trống.");
        }
        Optional<Location> optLocation = locationRepository.findById(locationId);
        if (optLocation.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy địa điểm đã chọn.");
        }
        return optLocation.get();
    }

    /** Lấy chiến dịch cha bắt buộc trước khi tạo hoặc kiểm tra phiên. */
    private Event layChienDichBatBuoc(Long eventId) {
        if (eventId == null) {
            throw new AppException(400, "ID chiến dịch không được để trống.");
        }
        Optional<Event> optEvent = eventRepository.findById(eventId);
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Chiến dịch cha để gán phiên này vào.");
        }
        return optEvent.get();
    }

    /** Lấy tài khoản admin đang thao tác để ghi audit status history. */
    private User layModeratorBatBuoc(Long moderatorId) {
        if (moderatorId == null) {
            throw new AppException(401, "Phiên quản trị không hợp lệ.");
        }
        Optional<User> optModerator = userRepository.findById(moderatorId);
        if (optModerator.isPresent() == false) {
            throw new AppException(401, "Danh tính Quản trị viên không hợp lệ.");
        }
        return optModerator.get();
    }

    /** Chặn khoảng thời gian không hợp lệ trước khi chạm DB CHECK constraint. */
    private void kiemTraThoiGianBuoiHopLe(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new AppException(400, "Giờ bắt đầu và giờ kết thúc không được để trống.");
        }
        if (endTime.isAfter(startTime) == false) {
            throw new AppException(400, "Xung đột thời gian: Giờ bế mạc phải diễn ra sau giờ khai mạc.");
        }
    }

    /** Update phiên không được âm thầm chuyển sang chiến dịch khác. */
    private void kiemTraEventIdKhongDoi(CtEvent ctEvent, Long requestEventId) {
        if (requestEventId == null) {
            throw new AppException(400, "ID chiến dịch không được để trống.");
        }
        if (ctEvent.getEvent() == null || ctEvent.getEvent().getId().equals(requestEventId) == false) {
            throw new AppException(400, "Không hỗ trợ chuyển phiên sang chiến dịch khác trong thao tác cập nhật.");
        }
    }

    /** Chặn mọi mã trạng thái buổi ngoài vòng đời được hệ thống hỗ trợ. */
    private void kiemTraTrangThaiBuoiHopLe(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty() == true) {
            throw new AppException(400, "Mã trạng thái buổi không được để trống.");
        }
        String status = statusCode.trim().toUpperCase();
        if (status.equals("DRAFT") || status.equals("OPEN") || status.equals("UPCOMING")
                || status.equals("ONGOING") || status.equals("FULL") || status.equals("CANCELLED")
                || status.equals("FINISHED") || status.equals("ENDED")) {
            return;
        }
        throw new AppException(400, "Mã trạng thái buổi không hợp lệ: " + statusCode);
    }

    /** Chặn trạng thái vé không nằm trong tập nghiệp vụ đăng ký sự kiện. */
    private void kiemTraTrangThaiDangKyHopLe(String status) {
        if (status == null || status.trim().isEmpty() == true) {
            throw new AppException(400, "Trạng thái đăng ký không được để trống.");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (normalizedStatus.equals("PENDING") || normalizedStatus.equals("CONFIRMED")
                || normalizedStatus.equals("APPROVED") || normalizedStatus.equals("ATTENDED")
                || normalizedStatus.equals("CANCELLED")) {
            return;
        }
        throw new AppException(400, "Trạng thái đăng ký không hợp lệ: " + status);
    }

    /** Chỉ cho phép cập nhật trạng thái vé nếu không vượt sức chứa phiên. */
    private void kiemTraSucChuaKhiDoiTrangThaiDangKy(CtEventRegistration registration, String statusMoi) {
        CtEvent ctEvent = registration.getCtEvent();
        if (ctEvent == null || ctEvent.getTotalSlots() == null || ctEvent.getTotalSlots() <= 0) {
            return;
        }
        if (trangThaiDangKyChiemSlot(statusMoi) == false) {
            return;
        }
        if (trangThaiDangKyChiemSlot(registration.getStatus()) == true) {
            return;
        }
        long soSlotDangChiem = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
        if (soSlotDangChiem >= ctEvent.getTotalSlots()) {
            throw new AppException(409, "Phiên sự kiện đã hết chỗ, không thể chuyển vé sang trạng thái chiếm slot.");
        }
    }

    /** Giữ cùng định nghĩa slot với repository demSlotDaDangKy. */
    private boolean trangThaiDangKyChiemSlot(String status) {
        if (status == null) {
            return false;
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (normalizedStatus.equals("PENDING") || normalizedStatus.equals("CONFIRMED")
                || normalizedStatus.equals("APPROVED") || normalizedStatus.equals("ATTENDED")) {
            return true;
        }
        return false;
    }

    /** Validate toàn bộ roleIds trước khi xóa/gắn bảng quyền để tránh fail-open. */
    private void kiemTraDanhSachRoleHopLe(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty() == true) {
            if (userRoleRepository.findByRoleName("PUBLIC").isPresent() == false) {
                throw new AppException(500, "Hệ thống chưa cấu hình quyền PUBLIC cho sự kiện.");
            }
            return;
        }
        kiemTraIdIntegerKhongLap(roleIds, "quyền truy cập phiên");
        Object[] roleIdArr = roleIds.toArray();
        for (int i = 0; i < roleIdArr.length; i = i + 1) {
            Integer roleId = (Integer) roleIdArr[i];
            if (userRoleRepository.existsById(roleId) == false) {
                throw new AppException(400, "Không tìm thấy quyền truy cập phiên có ID: " + roleId);
            }
        }
    }

    /** Validate toàn bộ tagIds trước khi thay liên kết tag. */
    private void kiemTraDanhSachTagHopLe(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty() == true) {
            return;
        }
        kiemTraIdLongKhongLap(tagIds, "tag của phiên");
        Object[] tagIdArr = tagIds.toArray();
        for (int i = 0; i < tagIdArr.length; i = i + 1) {
            Long tagId = (Long) tagIdArr[i];
            if (tagRepository.existsById(tagId) == false) {
                throw new AppException(400, "Không tìm thấy tag sự kiện có ID: " + tagId);
            }
        }
    }

    /** Validate toàn bộ relatedPostIds trước khi thay liên kết bài viết. */
    private void kiemTraDanhSachBaiVietHopLe(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty() == true) {
            return;
        }
        kiemTraIdLongKhongLap(postIds, "bài viết liên quan");
        Object[] postIdArr = postIds.toArray();
        for (int i = 0; i < postIdArr.length; i = i + 1) {
            Long postId = (Long) postIdArr[i];
            if (postRepository.existsById(postId) == false) {
                throw new AppException(400, "Không tìm thấy bài viết liên quan có ID: " + postId);
            }
        }
    }

    /** Chặn payload lặp cùng một role ID trong cùng request. */
    private void kiemTraIdIntegerKhongLap(List<Integer> ids, String tenNghiepVu) {
        Object[] arr = ids.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Integer idHienTai = (Integer) arr[i];
            if (idHienTai == null) {
                throw new AppException(400, "Danh sách " + tenNghiepVu + " chứa ID rỗng.");
            }
            for (int j = i + 1; j < arr.length; j = j + 1) {
                Integer idSoSanh = (Integer) arr[j];
                if (idHienTai.equals(idSoSanh) == true) {
                    throw new AppException(400, "Danh sách " + tenNghiepVu + " chứa ID lặp: " + idHienTai);
                }
            }
        }
    }

    /** Chặn payload lặp cùng một ID Long trong cùng request. */
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

    private void ganQuyenChoBuoi(CtEvent ctEvent, List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty() == true) {
            UserRole publicRole = userRoleRepository.findByRoleName("PUBLIC").orElse(null);
            if (publicRole != null) {
                CtEventSessionRole.CtEventSessionRoleId pkId =
                        new CtEventSessionRole.CtEventSessionRoleId(ctEvent.getId(), publicRole.getId());
                CtEventSessionRole bridge = new CtEventSessionRole();
                bridge.setId(pkId);
                bridge.setCtEvent(ctEvent);
                bridge.setRole(publicRole);
                ctEventSessionRoleRepository.save(bridge);
            }
            return;
        }

        Object[] roleIdArr = roleIds.toArray();
        for (int i = 0; i < roleIdArr.length; i = i + 1) {
            Integer rId = (Integer) roleIdArr[i];
            Optional<UserRole> optRole = userRoleRepository.findById(rId);
            if (optRole.isPresent() == true) {
                CtEventSessionRole.CtEventSessionRoleId pkId =
                        new CtEventSessionRole.CtEventSessionRoleId(ctEvent.getId(), rId);
                CtEventSessionRole bridge = new CtEventSessionRole();
                bridge.setId(pkId);
                bridge.setCtEvent(ctEvent);
                bridge.setRole(optRole.get());
                ctEventSessionRoleRepository.save(bridge);
            }
        }
    }


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
     * Gắn các bài viết chuyên môn vào phiên sự kiện thông qua CT_POST_EVENTS.
     * Frontend chỉ truyền ID bài viết; service chịu trách nhiệm kiểm tra thực thể tồn tại.
     */
    private void ganBaiVietChoBuoi(CtEvent ctEvent, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty() == true) {
            return;
        }

        Object[] postIdArr = postIds.toArray();
        for (int i = 0; i < postIdArr.length; i = i + 1) {
            Long postId = (Long) postIdArr[i];
            Optional<Post> optPost = postRepository.findById(postId);

            if (optPost.isPresent() == true) {
                CtPostEvent.CtPostEventId pkId = new CtPostEvent.CtPostEventId(ctEvent.getId(), postId);
                CtPostEvent bridge = new CtPostEvent();
                bridge.setId(pkId);
                bridge.setCtEvent(ctEvent);
                bridge.setPost(optPost.get());
                ctPostEventRepository.save(bridge);
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
        // Ghế trống khả dụng = Tổng Slot - các trạng thái còn chiếm chỗ.
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

        List<UserRole> roles = ctEventSessionRoleRepository.layDanhSachQuyenCuaBuoi(ctEvent.getId());
        List<String> roleNames = new ArrayList<>();
        List<CtEventResponse.RoleInfo> roleInfos = new ArrayList<>();
        Object[] roleArr = roles.toArray();

        for (int i = 0; i < roleArr.length; i = i + 1) {
            UserRole role = (UserRole) roleArr[i];
            roleNames.add(role.getRoleName());

            CtEventResponse.RoleInfo roleInfo = new CtEventResponse.RoleInfo();
            roleInfo.setRoleId(role.getId());
            roleInfo.setRoleName(role.getRoleName());
            roleInfo.setDescription(role.getDescription());
            roleInfos.add(roleInfo);
        }
        resp.setAllowedRoleNames(roleNames);
        resp.setRequiredRoles(roleInfos);

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
            //postResp.setAccessLevel(post.getAccessLevel());

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

    /** Tạo danh mục trạng thái phiên sự kiện theo thứ tự vận hành admin. */
    private List<StatusOptionResponse> taoDanhSachTrangThaiBuoi() {
        List<StatusOptionResponse> result = new ArrayList<>();
        for (int i = 0; i < EVENT_STATUS_CODES.length; i = i + 1) {
            String code = EVENT_STATUS_CODES[i];
            result.add(taoStatusOption(code, eventStatusDisplayPolicy.layNhanAdmin(code)));
        }
        return result;
    }

    /** Tạo danh mục trạng thái đăng ký tham dự theo contract backend đang validate. */
    private List<StatusOptionResponse> taoDanhSachTrangThaiDangKy() {
        List<StatusOptionResponse> result = new ArrayList<>();
        for (int i = 0; i < REGISTRATION_STATUS_CODES.length; i = i + 1) {
            String code = REGISTRATION_STATUS_CODES[i];
            result.add(taoStatusOption(code, layNhanTrangThaiDangKy(code)));
        }
        return result;
    }

    /** Đóng gói một option trạng thái dùng chung cho dictionary API. */
    private StatusOptionResponse taoStatusOption(String code, String label) {
        StatusOptionResponse response = new StatusOptionResponse();
        response.setCode(code);
        response.setLabel(label);
        return response;
    }

    /** Dịch mã trạng thái đăng ký sang nhãn quản trị. */
    private String layNhanTrangThaiDangKy(String status) {
        if ("PENDING".equals(status) == true) {
            return "Chờ xác nhận";
        }
        if ("CONFIRMED".equals(status) == true || "APPROVED".equals(status) == true) {
            return "Đã duyệt";
        }
        if ("ATTENDED".equals(status) == true) {
            return "Đã tham dự";
        }
        if ("CANCELLED".equals(status) == true) {
            return "Đã hủy";
        }
        return status;
    }



    /** Lưu ảnh sự kiện do admin upload vào thư mục con đã kiểm soát. */
    private AdminEventMediaResponse luuAnhSuKien(MultipartFile file, String thuMucCon) {
        String phanMoRong = kiemTraFileAnhUpload(file);
        String tenFile = UUID.randomUUID().toString() + phanMoRong;
        Path thuMucLuu = Paths.get(uploadBasePath, "events", thuMucCon).normalize();
        Path duongDanLuu = thuMucLuu.resolve(tenFile).normalize();

        if (duongDanLuu.startsWith(thuMucLuu) == false) {
            throw new AppException(400, "Đường dẫn upload ảnh sự kiện không hợp lệ.");
        }
        try {
            Files.createDirectories(thuMucLuu);
            file.transferTo(duongDanLuu);
        } catch (IOException ex) {
            throw new AppException(500, "Không thể lưu file ảnh sự kiện trên server.");
        }

        AdminEventMediaResponse response = new AdminEventMediaResponse();
        response.setFileName(tenFile);
        response.setUrl("/uploads/events/" + thuMucCon + "/" + tenFile);
        return response;
    }

    /** Kiểm tra định dạng và dung lượng ảnh trước khi ghi xuống ổ đĩa server. */
    private String kiemTraFileAnhUpload(MultipartFile file) {
        if (file == null || file.isEmpty() == true) {
            throw new AppException(400, "Vui lòng chọn file ảnh cần upload.");
        }
        if (file.getSize() > MAX_EVENT_IMAGE_BYTES) {
            throw new AppException(400, "Ảnh sự kiện không được vượt quá 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || laContentTypeAnhHopLe(contentType) == false) {
            throw new AppException(400, "Chỉ hỗ trợ ảnh JPG, PNG, GIF hoặc WEBP.");
        }
        return layPhanMoRongAnh(file.getOriginalFilename());
    }

    /** Chỉ cho phép các MIME type ảnh phổ biến mà trình duyệt và server đang phục vụ. */
    private boolean laContentTypeAnhHopLe(String contentType) {
        if ("image/jpeg".equals(contentType) == true || "image/png".equals(contentType) == true) {
            return true;
        }
        if ("image/gif".equals(contentType) == true || "image/webp".equals(contentType) == true) {
            return true;
        }
        return false;
    }

    /** Rút phần mở rộng an toàn từ tên file gốc; tên file lưu thực tế do server sinh. */
    private String layPhanMoRongAnh(String originalFileName) {
        if (originalFileName == null) {
            throw new AppException(400, "Tên file ảnh không hợp lệ.");
        }
        String tenFileThuong = originalFileName.toLowerCase();
        if (tenFileThuong.endsWith(".jpg") == true || tenFileThuong.endsWith(".jpeg") == true) {
            return ".jpg";
        }
        if (tenFileThuong.endsWith(".png") == true) {
            return ".png";
        }
        if (tenFileThuong.endsWith(".gif") == true) {
            return ".gif";
        }
        if (tenFileThuong.endsWith(".webp") == true) {
            return ".webp";
        }
        throw new AppException(400, "Định dạng file ảnh không hợp lệ.");
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
