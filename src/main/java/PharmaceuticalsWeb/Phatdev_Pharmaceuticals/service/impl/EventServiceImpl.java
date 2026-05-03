//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/EventServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRegistrationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: THỰC THI NGHIỆP VỤ SỰ KIỆN (PUBLIC FRONTEND)
 * =========================================================================
 * Đảm nhiệm trọng trách vận hành "Cỗ máy Lead Capture" thu thập thông tin Khách
 * mời.
 * Thiết lập rào cản bảo mật chặt chẽ: Đối soát trạng thái thực tế, Tính toán
 * Capacity
 * và Phòng chống tấn công IDOR khi tương tác với dữ liệu cá nhân.
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements IEventService {

    private final IEventRepository eventRepository;
    private final ICtEventRepository ctEventRepository;
    private final IEventTypeRepository eventTypeRepository;
    private final ICtEventTagRepository ctEventTagRepository;
    private final ICtEventStatusHistoryRepository statusHistoryRepository;
    private final ICtEventRegistrationRepository registrationRepository;
    private final ICtPostEventRepository ctPostEventRepository;
    private final IUserRepository userRepository;
    private final ILocationRepository locationRepository;

    /**
     * Báo cáo sơ bộ bức tranh toàn cảnh về sự kiện để phục vụ Hero Stats trên
     * Frontend.
     */
    /**
     * Báo cáo sơ bộ bức tranh toàn cảnh về sự kiện để phục vụ Hero Stats trên Frontend.
     * Tích hợp Engine Phân giải Thời gian để đảm bảo số liệu đồng bộ 100% với danh sách hiển thị.
     */
    @Override
    public EventStatsResponse layThongKeTrangSuKien(Integer type, String time) {
        
        // 1. KHỞI TẠO MỐC THỜI GIAN AN TOÀN (Bao phủ toàn bộ Kỷ nguyên)
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        // 2. PHÂN GIẢI THAM SỐ THỜI GIAN (Đảm bảo logic y hệt Cỗ máy tìm kiếm)
        if ("THIS_MONTH".equals(time) == true) {
            LocalDateTime now = LocalDateTime.now();
            startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else if ("UPCOMING".equals(time) == true) {
            startDate = LocalDateTime.now();
        } else if ("THIS_WEEK".equals(time) == true) {
            LocalDateTime now = LocalDateTime.now();
            int thuTrongTuan = now.getDayOfWeek().getValue();
            startDate = now.minusDays(thuTrongTuan - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endDate = now.plusDays(7 - thuTrongTuan).withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else if ("NEXT_MONTH".equals(time) == true) {
            YearMonth thangSau = YearMonth.now().plusMonths(1);
            startDate = thangSau.atDay(1).atStartOfDay();
            endDate = thangSau.atEndOfMonth().atTime(23, 59, 59);
        } 
        // [QUAN TRỌNG] Nhánh này bắt chuỗi ngày YYYY-MM-DD từ sự kiện Click Lịch Mini của Frontend
        else if (time != null && time.contains("-") == true) {
            String[] cacPhan = time.split("-");
            int nam = Integer.parseInt(cacPhan[0]);
            int thang = Integer.parseInt(cacPhan[1]);
            
            if (cacPhan.length == 3) {
                // Xử lý Ngày cụ thể
                int ngay = Integer.parseInt(cacPhan[2]);
                startDate = LocalDateTime.of(nam, thang, ngay, 0, 0, 0);
                endDate = LocalDateTime.of(nam, thang, ngay, 23, 59, 59);
            } else if (cacPhan.length == 2) {
                // Xử lý nguyên Tháng
                YearMonth targetMonth = YearMonth.of(nam, thang);
                startDate = targetMonth.atDay(1).atStartOfDay();
                endDate = targetMonth.atEndOfMonth().atTime(23, 59, 59);
            }
        }

        // 3. TỔNG HỢP DỮ LIỆU
        EventStatsResponse stats = new EventStatsResponse();
        
        // Chỉ số 1: Số buổi sự kiện nằm trong Khung thời gian VÀ Loại sự kiện đã chọn
        long soBuoiCoLoc = ctEventRepository.demTongBuoiPublicCoLoc(type, startDate, endDate);
        stats.setEventsThisMonth(soBuoiCoLoc);

        // Chỉ số 2: Lượt đăng ký nằm trong Khung thời gian VÀ Loại sự kiện đã chọn
        long soDangKyCoLoc = registrationRepository.demTongDangKyPublicCoLoc(type, startDate, endDate);
        stats.setTotalRegistrations(soDangKyCoLoc);

        // Chỉ số 3: Tổng số buổi (Chỉ lọc theo Loại sự kiện, mở rộng thời gian về Vô Cực để thấy quy mô tổng thể của nhánh đó)
        LocalDateTime thoiGianVoCucStart = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime thoiGianVoCucEnd = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        long tongSoBuoiTheoLoai = ctEventRepository.demTongBuoiPublicCoLoc(type, thoiGianVoCucStart, thoiGianVoCucEnd);
        stats.setTotalSessions(tongSoBuoiTheoLoai);

        // Chỉ số 4: Số Loại Sự Kiện (Luôn là con số Toàn cục để Sidebar giữ được định dạng)
        stats.setTotalEventTypes(eventTypeRepository.count());

        return stats;
    }

    /**
     * Lấy danh mục các loại hình hội thảo/sự kiện đang được tổ chức.
     */
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
            // Đếm số chiến dịch thuộc loại này để hiển thị trên sidebar
            resp.setEventCount(eventRepository.demChienDichPublicTheoLoai(et.getId()));
            result.add(resp);
        }
        return result;
    }

    /**
     * Lấy danh sách địa điểm cho Public.
     */
    @Override
    public List<LocationResponse> layTatCaDiaDiemPublic() {
        // Dùng chung Repository của Location đã có sẵn
        List<Location> danhSach = locationRepository.findAllByOrderByNameAsc();
        List<LocationResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Location loc = (Location) arr[i];
            LocationResponse resp = new LocationResponse();
            resp.setId(loc.getId());
            resp.setName(loc.getName());
            resp.setAddress(loc.getAddress());
            resp.setOnline(loc.isOnline());
            result.add(resp);
        }
        return result;
    }

    /**
     * Lấy danh sách buổi sự kiện trong 1 tháng cụ thể (Mini Calendar).
     * Tham số: year, month.
     */
    @Override
    public List<CtEventResponse> layBuoiTrongThang(int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime dauThang = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime cuoiThang = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        // Gọi hàm repository đã được ngài viết sẵn từ trước
        List<CtEvent> danhSach = ctEventRepository.layBuoiDaCongBoTrongThang(dauThang, cuoiThang);
        List<CtEventResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEvent ctEvent = (CtEvent) arr[i];
            // Tái sử dụng cỗ máy mapping cực xịn của ngài
            result.add(xayDungCtEventResponse(ctEvent));
        }
        return result;
    }

    /**
     * Tìm kiếm và phân trang Chiến dịch Marketing hướng người dùng cuối.
     */
    @Override
    public Page<EventResponse> timKiemSuKien(String keyword, Integer type, String time, Integer locationId, String sort,
            int page, int size) {

        // 1. Cấu hình Sắp xếp (Sort)
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "created_at");
        if ("popular".equals(sort) == true) {
            // Sắp xếp theo phổ biến nếu cần
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 2. Phân giải tham số thời gian — luôn gán giá trị thực, KHÔNG truyền null
        // LocalDateTime
        // vào JPQL vì SQL Server JDBC (Hibernate 7) không resolve SQL type cho temporal
        // null.
        // Sentinel: 2000-01-01 đến 2099-12-31 → bao phủ toàn bộ khi không lọc ngày.
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        if ("THIS_MONTH".equals(time) == true) {
            LocalDateTime now = LocalDateTime.now();
            startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59)
                    .withNano(0);
        } else if ("UPCOMING".equals(time) == true) {
            startDate = LocalDateTime.now();
        } else if ("THIS_WEEK".equals(time) == true) {
            LocalDateTime now = LocalDateTime.now();
            int thuTrongTuan = now.getDayOfWeek().getValue(); // 1=T2, 7=CN
            startDate = now.minusDays(thuTrongTuan - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endDate = now.plusDays(7 - thuTrongTuan).withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else if ("NEXT_MONTH".equals(time) == true) {
            YearMonth thangSau = YearMonth.now().plusMonths(1);
            startDate = thangSau.atDay(1).atStartOfDay();
            endDate = thangSau.atEndOfMonth().atTime(23, 59, 59);
        }  else if (time != null && time.contains("-") == true) {
            String[] cacPhan = time.split("-");
            int nam = Integer.parseInt(cacPhan[0]);
            int thang = Integer.parseInt(cacPhan[1]);
            
            if (cacPhan.length == 3) {
                // Xử lý Ngày cụ thể
                int ngay = Integer.parseInt(cacPhan[2]);
                startDate = LocalDateTime.of(nam, thang, ngay, 0, 0, 0);
                endDate = LocalDateTime.of(nam, thang, ngay, 23, 59, 59);
            } else if (cacPhan.length == 2) {
                // Xử lý nguyên Tháng
                YearMonth targetMonth = YearMonth.of(nam, thang);
                startDate = targetMonth.atDay(1).atStartOfDay();
                endDate = targetMonth.atEndOfMonth().atTime(23, 59, 59);
            }
        }

        // Xử lý từ khóa an toàn
        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        Page<Event> eventsPage = eventRepository.timKiemChienDichPublic(kw, type, startDate, endDate, locationId, pageable);

        // 3. Đóng gói DTO (Sử dụng Object[] thay vì Stream API)
        List<Event> eventList = eventsPage.getContent();
        List<EventResponse> responseList = new ArrayList<>();

        Object[] arr = eventList.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Event e = (Event) arr[i];
            responseList.add(xayDungEventResponse(e)); // Giả định bạn đã có hàm helper này
        }

        return new PageImpl<>(responseList, pageable, eventsPage.getTotalElements());
    }

    /**
     * Bóc tách toàn bộ thông tin chi tiết của một Chiến dịch dựa trên đường dẫn
     * tĩnh (Slug).
     */
    @Override
    public EventResponse layChiTietSuKien(String slug) {
        Optional<Event> optEvent = eventRepository.findBySlug(slug);
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Chúng tôi không tìm thấy thông tin chiến dịch sự kiện này.");
        }
        return xayDungEventResponse(optEvent.get());
    }

    /**
     * G1: Lấy chi tiết một Buổi sự kiện theo ID.
     * Tái sử dụng hàm xayDungCtEventResponse đã có sẵn.
     */
    @Override
    public CtEventResponse layChiTietBuoi(Long ctEventId) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện được yêu cầu.");
        }
        return xayDungCtEventResponse(optCtEvent.get());
    }

    /**
     * Trích xuất các Phiên sự kiện (Session) có lịch trình gần nhất trong vòng 3
     * tháng tới.
     */
    @Override
    public List<CtEventResponse> layBuoiSapToi(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cuoiThang = now.plusMonths(3);
        List<CtEvent> danhSach = ctEventRepository.layBuoiDaCongBoTrongThang(now, cuoiThang);

        List<CtEventResponse> result = new ArrayList<>();
        int count = 0;

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            if (count >= limit) {
                break;
            }
            CtEvent ctEvent = (CtEvent) arr[i];
            result.add(xayDungCtEventResponse(ctEvent));
            count = count + 1;
        }
        return result;
    }

    /**
     * G6: Lấy dữ liệu hồ sơ cá nhân để hiển thị vào khu vực "Vé sự kiện của tôi".
     * Context-aware: populate campaignTitle vì user cần biết vé này thuộc chiến
     * dịch nào.
     */
    @Override
    @Transactional
    public List<EventRegistrationResponse> layDangKyCuaToi(Long userId) {
        List<CtEventRegistration> danhSach = registrationRepository.findByUserIdOrderByRegisteredAtDesc(userId);
        List<EventRegistrationResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventRegistration reg = (CtEventRegistration) arr[i];
            EventRegistrationResponse resp = chuyenDoiRegistrationResponse(reg);

            // Populate tên chiến dịch: người dùng cần biết vé thuộc sự kiện nào
            if (reg.getCtEvent() != null && reg.getCtEvent().getEvent() != null) {
                resp.setCampaignTitle(reg.getCtEvent().getEvent().getTitle());
            }
            result.add(resp);
        }
        return result;
    }

    /**
     * =========================================================================
     * THUẬT TOÁN ĐĂNG KÝ VÉ ĐIỆN TỬ (LEAD CAPTURE ENGINE)
     * =========================================================================
     * Hàm này được bọc @Transactional để chống hiện tượng bán quá số vé
     * (Overbooking)
     * khi có nhiều truy cập đồng thời.
     */
    @Override
    @Transactional
    public EventRegistrationResponse dangKyThamDu(EventRegistrationRequest request, Long userId) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(request.getCtEventId());
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để đăng ký vé.");
        }

        CtEvent ctEvent = optCtEvent.get();

        // 1. Phân tích Cổng chặn: Phiên sự kiện này có đang thực sự mở bán vé?
        Optional<CtEventStatusHistory> optStatus = statusHistoryRepository.layTrangThaiHienTai(ctEvent.getId());
        if (optStatus.isPresent() == true) {
            String status = optStatus.get().getStatusCode();
            if (status.equals("OPEN") == false) {
                throw new AppException(400,
                        "Xin lỗi, hiện tại sự kiện này không mở cổng đăng ký (Trạng thái: " + status + ").");
            }
        } else {
            throw new AppException(400, "Sự kiện này chưa được công bố hợp lệ.");
        }

        // 2. Thuật toán Đối soát Cung Cầu: Sức chứa hiện tại có cho phép nhận thêm?
        if (ctEvent.getTotalSlots() > 0) {
            long registered = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
            if (registered >= ctEvent.getTotalSlots()) {
                throw new AppException(400, "Rất tiếc, số lượng vé phát hành của hội thảo này đã đạt giới hạn tối đa.");
            }
        }

        // 3. Khởi tạo đối tượng định danh Khách mời
        CtEventRegistration reg = new CtEventRegistration();
        reg.setCtEvent(ctEvent);
        reg.setGuestName(request.getGuestName());
        reg.setGuestEmail(request.getGuestEmail());
        reg.setGuestPhone(request.getGuestPhone());
        reg.setWorkplace(request.getWorkplace());
        reg.setStatus("PENDING");
        reg.setRegisteredAt(LocalDateTime.now());

        // 4. Giải mã Đối tác Nội bộ (B2B User) - Nếu đã đăng nhập
        if (userId != null) {
            // Chặn đúp vé: Ngăn chặn một User spam form đăng ký cho cùng một phiên
            if (registrationRepository.existsByCtEventIdAndUserId(ctEvent.getId(), userId) == true) {
                throw new AppException(400, "Hệ thống ghi nhận tài khoản của bạn đã sở hữu vé tham dự phiên này rồi.");
            }

            Optional<User> optUser = userRepository.findById(userId);
            if (optUser.isPresent() == true) {
                reg.setUser(optUser.get());
            }
        }

        CtEventRegistration saved = registrationRepository.save(reg);
        return chuyenDoiRegistrationResponse(saved);
    }

    /**
     * =========================================================================
     * THUẬT TOÁN HỦY VÉ BẢO MẬT
     * =========================================================================
     * Thực thi lệnh hủy vé của khách hàng.
     * Cơ chế phòng thủ IDOR được triển khai: Xác minh người gửi lệnh Hủy
     * thực sự là chủ nhân của Hồ sơ đăng ký đó.
     */
    @Override
    @Transactional
    public void huyDangKy(Long registrationId, Long userId) {
        Optional<CtEventRegistration> opt = registrationRepository.findById(registrationId);
        if (opt.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy hồ sơ đăng ký.");
        }

        CtEventRegistration reg = opt.get();

        // Lá chắn IDOR (Insecure Direct Object Reference)
        // Kẻ tấn công không thể truyền ID ảo lên để xóa vé của người dùng khác.
        if (reg.getUser() != null && reg.getUser().getId().equals(userId) == false) {
            throw new AppException(403, "Bạn không có thẩm quyền thực thi việc hủy vé của đối tác này.");
        }

        reg.setStatus("CANCELLED");
        registrationRepository.save(reg);
    }

    /**
     * Tra cứu Timeline lịch sử hoạt động của một Phiên sự kiện.
     */
    @Override
    public List<EventStatusHistoryResponse> layLichSuTrangThaiPublic(Long ctEventId) {
        List<CtEventStatusHistory> danhSach = statusHistoryRepository.findByCtEventIdOrderByChangedAtDesc(ctEventId);
        List<EventStatusHistoryResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventStatusHistory h = (CtEventStatusHistory) arr[i];

            EventStatusHistoryResponse resp = new EventStatusHistoryResponse();
            resp.setId(h.getId());
            resp.setCtEventId(ctEventId);
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
     * Thuật toán trích xuất tóm tắt khách mời phục vụ Social Proof tại Frontend.
     * Áp dụng cơ chế che giấu dữ liệu (Masking) bằng vòng lặp For truyền thống.
     */
    @Override
    public EventAttendeePublicResponse layTomTatKhachMoiPublic(Long ctEventId) {
        // 1. Đếm tổng số lượng (Phép toán aggregate tại DB)
        long total = registrationRepository.countByCtEventIdAndStatus(ctEventId, "APPROVED");

        // 2. Lấy danh sách 5 người đăng ký mới nhất để hiển thị mẫu
        List<CtEventRegistration> latestRegs = registrationRepository.findByCtEventIdOrderByRegisteredAtDesc(ctEventId);
        List<EventAttendeePublicResponse.AttendeeMaskedInfo> maskedList = new ArrayList<>();

        Object[] regArray = latestRegs.toArray();
        int limit = regArray.length < 5 ? regArray.length : 5;

        for (int i = 0; i < limit; i = i + 1) {
            CtEventRegistration reg = (CtEventRegistration) regArray[i];

            // Xác định tên hiển thị gốc (Từ Guest hoặc từ User liên kết)
            String rawName = reg.getGuestName();
            if (rawName == null && reg.getUser() != null) {
                rawName = reg.getUser().getFullName();
            }
            if (rawName == null) {
                rawName = "Khách mời";
            }

            String rawPhone = reg.getGuestPhone();
            if (rawPhone == null && reg.getUser() != null) {
                rawPhone = reg.getUser().getPhone();
            }

            // Thực thi Masking Engine
            EventAttendeePublicResponse.AttendeeMaskedInfo info = new EventAttendeePublicResponse.AttendeeMaskedInfo();
            info.setInitial(rawName.substring(0, 1).toUpperCase());
            info.setMaskedName(maskName(rawName));
            info.setMaskedPhone(maskPhone(rawPhone));
            info.setWorkplace(reg.getWorkplace());
            info.setRegisteredAt(reg.getRegisteredAt());

            maskedList.add(info);
        }

        EventAttendeePublicResponse response = new EventAttendeePublicResponse();
        response.setTotalCount(total);
        response.setAttendees(maskedList);
        return response;
    }

    /** Thuật toán che tên: Giữ họ và tên đệm chữ cái đầu, thay lõi bằng * */
    private String maskName(String name) {
        if (name == null || name.length() < 2)
            return "***";
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i = i + 1) {
            String p = parts[i];
            if (p.length() > 0) {
                sb.append(p.charAt(0)).append("*** ");
            }
        }
        return sb.toString().trim();
    }

    /** Thuật toán che số điện thoại: Giữ 3 số đầu và 2 số cuối */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "*******";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }

    // =========================================================================
    // HÀM TIỆN ÍCH BÓC TÁCH DỮ LIỆU
    // =========================================================================

    /**
     * Gom nhóm dữ liệu của một Chiến dịch, quét toàn bộ Phiên con.
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

        List<CtEvent> sessions = ctEventRepository.layBuoiDaCongBoCuaChienDich(event.getId());
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
     * Bóc tách Phiên sự kiện, phân tích Slot trống và kết nối Dữ liệu Y khoa đính
     * kèm.
     */
    private CtEventResponse xayDungCtEventResponse(CtEvent ctEvent) {
        CtEventResponse resp = new CtEventResponse();
        
        // 1. Ánh xạ các thông tin định danh và thời gian cơ bản
        resp.setId(ctEvent.getId());
        resp.setTitle(ctEvent.getTitle());
        resp.setContent(ctEvent.getContent());
        resp.setStartTime(ctEvent.getStartTime());
        resp.setEndTime(ctEvent.getEndTime());
        resp.setTotalSlots(ctEvent.getTotalSlots());
        resp.setSeoTitle(ctEvent.getSeoTitle());
        resp.setSeoDescription(ctEvent.getSeoDescription());

        // 2. Phân giải thông tin Chiến dịch cha (EVENTS)
        if (ctEvent.getEvent() != null) {
            resp.setEventId(ctEvent.getEvent().getId());
            resp.setEventTitle(ctEvent.getEvent().getTitle());
            resp.setEventSlug(ctEvent.getEvent().getSlug());
        }

        // 3. Phân giải thông tin Địa điểm (LOCATIONS)
        if (ctEvent.getLocation() != null) {
            resp.setLocationId(ctEvent.getLocation().getId());
            resp.setLocationName(ctEvent.getLocation().getName());
            resp.setLocationAddress(ctEvent.getLocation().getAddress());
            resp.setOnline(ctEvent.getLocation().isOnline());
        }

        // 4. TRÍCH XUẤT TRẠNG THÁI GỐC TỪ NHẬT KÝ KIỂM TOÁN (Event Sourcing)
        String maTrangThaiGoc = "DRAFT";
        Optional<CtEventStatusHistory> optStatus = statusHistoryRepository.layTrangThaiHienTai(ctEvent.getId());
        if (optStatus.isPresent() == true) {
            maTrangThaiGoc = optStatus.get().getStatusCode();
        }
        resp.setCurrentStatus(maTrangThaiGoc);

        // 5. THUẬT TOÁN ĐỐI SOÁT CUNG - CẦU VÀ TÍNH TOÁN "CHUẨN MÙ" CHO FRONTEND
        long soVeDaDangKy = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
        long soVeConTrong = 0;
        
        if (ctEvent.getTotalSlots() > 0) {
            soVeConTrong = ctEvent.getTotalSlots() - soVeDaDangKy;
        }
        
        resp.setRegisteredCount(soVeDaDangKy);
        resp.setAvailableSlots(soVeConTrong);

        // --- BƯỚC A: TÍNH TOÁN CHUỖI HIỂN THỊ TRẠNG THÁI (DISPLAY STATUS) ---
        String chuoiHienThi = "Không xác định";
        
        if (maTrangThaiGoc.equals("CANCELLED") == true) {
            chuoiHienThi = "Đã hủy";
        } else if (maTrangThaiGoc.equals("FINISHED") == true || maTrangThaiGoc.equals("ENDED") == true) {
            chuoiHienThi = "Đã kết thúc";
        } else if (maTrangThaiGoc.equals("ONGOING") == true) {
            chuoiHienThi = "Đang diễn ra";
        } else if (maTrangThaiGoc.equals("OPEN") == true || maTrangThaiGoc.equals("UPCOMING") == true) {
            // Logic quan trọng: Nếu Backend thấy hết chỗ, ép trạng thái hiển thị thành "Hết chỗ"
            if (ctEvent.getTotalSlots() > 0 && soVeConTrong <= 0) {
                chuoiHienThi = "Hết chỗ";
            } else {
                chuoiHienThi = "Sắp diễn ra";
            }
        }
        resp.setDisplayStatus(chuoiHienThi);

        // --- BƯỚC B: THIẾT LẬP CỜ BÁO ĐỘNG ĐỎ (CRITICAL FLAG) ---
        // Thuật toán: Nếu số vé còn lại ít hơn hoặc bằng 20% tổng dung lượng -> Bật cờ đỏ.
        boolean laToiHan = false;
        if (ctEvent.getTotalSlots() > 0) {
            double nguongToiHan = ctEvent.getTotalSlots() * 0.2;
            if (soVeConTrong > 0 && soVeConTrong <= nguongToiHan) {
                laToiHan = true;
            } else if (soVeConTrong <= 0) {
                laToiHan = true; // Hết vé cũng coi là trạng thái tới hạn
            }
        }
        resp.setCritical(laToiHan);

        // 6. NẠP DANH SÁCH THẺ CHỦ ĐỀ (TAGS)
        List<Tag> danhSachTag = ctEventTagRepository.layTagCuaBuoi(ctEvent.getId());
        List<TagResponse> ketQuaTags = new ArrayList<>();
        if (danhSachTag != null) {
            Object[] mangTag = danhSachTag.toArray();
            for (int i = 0; i < mangTag.length; i = i + 1) {
                Tag t = (Tag) mangTag[i];
                TagResponse tr = new TagResponse();
                tr.setId(t.getId());
                tr.setName(t.getName());
                tr.setSlug(t.getSlug());
                ketQuaTags.add(tr);
            }
        }
        resp.setTags(ketQuaTags);

        // 7. NẠP DANH SÁCH BÀI VIẾT Y KHOA LIÊN QUAN (POSTS)
        List<Post> danhSachBaiViet = ctPostEventRepository.layBaiVietLienQuan(ctEvent.getId());
        List<PostResponse> ketQuaPosts = new ArrayList<>();
        if (danhSachBaiViet != null) {
            Object[] mangPost = danhSachBaiViet.toArray();
            for (int j = 0; j < mangPost.length; j = j + 1) {
                Post p = (Post) mangPost[j];
                PostResponse pr = new PostResponse();
                pr.setId(p.getId());
                pr.setTitle(p.getTitle());
                pr.setSlug(p.getSlug());
                pr.setSummary(p.getSummary());
                pr.setThumbnailUrl(p.getThumbnailUrl());
                pr.setAccessLevel(p.getAccessLevel());
                
                if (p.getCategory() != null) {
                    pr.setCategoryName(p.getCategory().getName());
                }
                if (p.getAuthor() != null) {
                    pr.setAuthorName(p.getAuthor().getFullName());
                }
                ketQuaPosts.add(pr);
            }
        }
        resp.setRelatedPosts(ketQuaPosts);

        return resp;
    }

    private EventRegistrationResponse chuyenDoiRegistrationResponse(CtEventRegistration reg) {
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

        return resp;
    }
}