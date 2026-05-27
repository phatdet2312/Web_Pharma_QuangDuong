//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/EventServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRegistrationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils.PagingUtil;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.EventStatusDisplayPolicy;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDung;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
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

    private static final String STATUS_LOCKED = "LOCKED";
    private static final String LOCKED_DISPLAY_STATUS = "Dành riêng cho nhóm chuyên môn";
    private static final String LOCKED_ACCESS_TITLE = "Phiên chuyên môn giới hạn";
    private static final String LOCKED_ACCESS_MESSAGE =
            "Một số thông tin chi tiết chỉ mở cho tài khoản có quyền phù hợp. "
            + "Vui lòng đăng nhập bằng tài khoản đủ quyền để xem lịch trình và tài nguyên chuyên môn.";
    private static final String LOCKED_REGISTRATION_MESSAGE =
            "Đăng ký cho phiên này chỉ mở sau khi tài khoản đủ quyền chuyên môn. "
            + "Vui lòng đăng nhập bằng tài khoản đã được xác minh hoặc liên hệ quản trị viên.";
    private static final String LOCKED_LOCATION_MESSAGE =
            "Chi tiết địa điểm/link tham gia sẽ mở sau khi hồ sơ đủ điều kiện.";


    private final IEventRepository eventRepository;
    private final ICtEventRepository ctEventRepository;
    private final IEventTypeRepository eventTypeRepository;
    private final ICtEventTagRepository ctEventTagRepository;
    private final ICtEventStatusHistoryRepository statusHistoryRepository;
    private final ICtEventRegistrationRepository registrationRepository;
    private final ICtPostEventRepository ctPostEventRepository;
    private final IUserRepository userRepository;
    private final ILocationRepository locationRepository;
    private final ICtEventSessionRoleRepository ctEventSessionRoleRepository;
    private final EventStatusDisplayPolicy eventStatusDisplayPolicy;

    /**
     * Báo cáo sơ bộ bức tranh toàn cảnh về sự kiện để phục vụ Hero Stats trên
     * Frontend.
     */
    /**
     * Báo cáo sơ bộ bức tranh toàn cảnh về sự kiện để phục vụ Hero Stats trên
     * Frontend.
     * Tích hợp Engine Phân giải Thời gian để đảm bảo số liệu đồng bộ 100% với danh
     * sách hiển thị.
     */
    @Override
    public EventStatsResponse layThongKeTrangSuKien(Integer type, String time) {
        KhoangThoiGianSuKien khoangThoiGian = phanGiaiKhoangThoiGianSuKien(time);
        LocalDateTime startDate = khoangThoiGian.startDate;
        LocalDateTime endDate = khoangThoiGian.endDate;

        // 3. TỔNG HỢP DỮ LIỆU
        EventStatsResponse stats = new EventStatsResponse();

        // Chỉ số 1: Số buổi sự kiện nằm trong Khung thời gian VÀ Loại sự kiện đã chọn
        long soBuoiCoLoc = ctEventRepository.demTongBuoiPublicCoLoc(type, startDate, endDate);
        stats.setEventsThisMonth(soBuoiCoLoc);

        // Chỉ số 2: Lượt đăng ký nằm trong Khung thời gian VÀ Loại sự kiện đã chọn
        long soDangKyCoLoc = registrationRepository.demTongDangKyPublicCoLoc(type, startDate, endDate);
        stats.setTotalRegistrations(soDangKyCoLoc);

        // Chỉ số 3: Tổng số buổi (Chỉ lọc theo Loại sự kiện, mở rộng thời gian về Vô
        // Cực để thấy quy mô tổng thể của nhánh đó)
        LocalDateTime thoiGianVoCucStart = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime thoiGianVoCucEnd = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        long tongSoBuoiTheoLoai = ctEventRepository.demTongBuoiPublicCoLoc(type, thoiGianVoCucStart, thoiGianVoCucEnd);
        stats.setTotalSessions(tongSoBuoiTheoLoai);

        // Chỉ số 4: Số Loại Sự Kiện (Luôn là con số Toàn cục để Sidebar giữ được định
        // dạng)
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
            resp.setAddress(null);
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
    public List<CtEventResponse> layBuoiTrongThang(int year, int month, NguCanhNguoiDung nguCanh) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime dauThang = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime cuoiThang = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        // Gọi hàm repository đã được ngài viết sẵn từ trước
        List<CtEvent> danhSach = ctEventRepository.layBuoiDaCongBoTrongThang(dauThang, cuoiThang);
        List<CtEventResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEvent ctEvent = (CtEvent) arr[i];
            result.add(xayDungCtEventResponse(ctEvent, nguCanh));
        }
        return result;
    }

    /**
     * Tìm kiếm và phân trang Chiến dịch Marketing hướng người dùng cuối.
     */
    @Override
    public Page<EventResponse> timKiemSuKien(String keyword, Integer type, String time, Integer locationId, Integer roleId, String sort,
            int page, int size, NguCanhNguoiDung nguCanh) {

        // 1. Cấu hình Sắp xếp (Sort)
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "created_at");
        if ("popular".equals(sort) == true) {
            // Sắp xếp theo phổ biến nếu cần
        }
        Pageable pageable = PageRequest.of(PagingUtil.chuanHoaPage(page), PagingUtil.chuanHoaSize(size), sortOrder);

        // 2. Phân giải tham số thời gian dùng chung với phần thống kê để tránh lệch số liệu.
        KhoangThoiGianSuKien khoangThoiGian = phanGiaiKhoangThoiGianSuKien(time);
        LocalDateTime startDate = khoangThoiGian.startDate;
        LocalDateTime endDate = khoangThoiGian.endDate;

        // Xử lý từ khóa an toàn
        String kw = null;
        if (keyword != null && keyword.trim().isEmpty() == false) {
            kw = keyword.trim();
        }

        Page<Event> eventsPage = eventRepository.timKiemChienDichPublic(kw, type, startDate, endDate, locationId,
                roleId, pageable);

        // 3. Đóng gói DTO (Sử dụng Object[] thay vì Stream API)
        List<Event> eventList = eventsPage.getContent();
        List<EventResponse> responseList = new ArrayList<>();

        Object[] arr = eventList.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            Event e = (Event) arr[i];
            responseList.add(xayDungEventResponse(e, nguCanh));
        }

        return new PageImpl<>(responseList, pageable, eventsPage.getTotalElements());
    }

    /**
     * Bóc tách toàn bộ thông tin chi tiết của một Chiến dịch dựa trên đường dẫn
     * tĩnh (Slug).
     */
    @Override
    public EventResponse layChiTietSuKien(String slug, NguCanhNguoiDung nguCanh) {
        Optional<Event> optEvent = eventRepository.findBySlug(slug);
        if (optEvent.isPresent() == false) {
            throw new AppException(404, "Chúng tôi không tìm thấy thông tin chiến dịch sự kiện này.");
        }
        return xayDungEventResponse(optEvent.get(), nguCanh);
    }

    /**
     * G1: Lấy chi tiết một Buổi sự kiện theo ID.
     * Tái sử dụng hàm xayDungCtEventResponse đã có sẵn.
     */
    @Override
    public CtEventResponse layChiTietBuoi(Long ctEventId, NguCanhNguoiDung nguCanh) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện được yêu cầu.");
        }
        return xayDungCtEventResponse(optCtEvent.get(), nguCanh);
    }

    /**
     * Trích xuất các Phiên sự kiện (Session) có lịch trình gần nhất trong vòng 3
     * tháng tới.
     */
    @Override
    public List<CtEventResponse> layBuoiSapToi(int limit, NguCanhNguoiDung nguCanh) {
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
            result.add(xayDungCtEventResponse(ctEvent, nguCanh));
            count = count + 1;
        }
        return result;
    }

    /**
     * G6: Lấy dữ liệu hồ sơ cá nhân để hiển thị vào khu vực "Vé sự kiện của tôi" CÓ
     * PHÂN TRANG.
     * Context-aware: populate campaignTitle vì user cần biết vé này thuộc chiến
     * dịch nào.
     */
    @Override
    @Transactional
    public Page<EventRegistrationResponse> layDangKyCuaToi(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(PagingUtil.chuanHoaPage(page), PagingUtil.chuanHoaSize(size));
        Page<CtEventRegistration> trangDuLieu = registrationRepository.findByUserIdOrderByRegisteredAtDesc(userId,
                pageable);

        List<EventRegistrationResponse> result = new ArrayList<>();
        Object[] arr = trangDuLieu.getContent().toArray();

        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventRegistration reg = (CtEventRegistration) arr[i];
            EventRegistrationResponse resp = chuyenDoiRegistrationResponse(reg);

            // Populate tên chiến dịch: người dùng cần biết vé thuộc sự kiện nào
            if (reg.getCtEvent() != null && reg.getCtEvent().getEvent() != null) {
                resp.setCampaignTitle(reg.getCtEvent().getEvent().getTitle());
            }
            result.add(resp);
        }

        return new PageImpl<>(result, pageable, trangDuLieu.getTotalElements());
    }

    /**
     * Lấy đích danh vé của một người dùng tại một phiên cụ thể (O(1) cho Detail).
     * Dùng để hiển thị thông tin vé ngay trên trang chi tiết buổi sự kiện, giúp
     * user dễ dàng kiểm tra xem họ đã sở hữu vé chưa.
     */

    @Override
    public EventRegistrationResponse layVeCuaToiTaiBuoiNay(Long ctEventId, Long userId) {
        // Lấy tấm vé thao tác gần đây nhất của user này
        Optional<CtEventRegistration> optReg = registrationRepository
                .findFirstByCtEventIdAndUserIdOrderByRegisteredAtDesc(ctEventId, userId);

        if (optReg.isPresent() == false) {
            return null; // Khách chưa từng có tương tác gì
        }

        CtEventRegistration reg = optReg.get();

        // Nếu tấm vé mới nhất là vé Hủy, báo cho Frontend biết là "Chưa có vé hợp lệ"
        // để hiện Form
        // Lịch sử hủy vẫn nằm an toàn trong DB
        if (reg.getStatus().equals("CANCELLED") == true) {
            return null;
        }

        return chuyenDoiRegistrationResponse(reg);
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
    public EventRegistrationResponse dangKyThamDu(EventRegistrationRequest request, NguCanhNguoiDung nguCanh) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(request.getCtEventId());
        if (optCtEvent.isPresent() == false) {
            throw new AppException(404, "Không tìm thấy Phiên sự kiện để đăng ký vé.");
        }

        CtEvent ctEvent = optCtEvent.get();

        EventAccessDecision access = xayDungQuyetDinhTruyCap(ctEvent, nguCanh);
        if (access.hasFullAccess == false) {
            throw new AppException(403, LOCKED_REGISTRATION_MESSAGE);
        }

        // 1. Phân tích Cổng chặn
        Optional<CtEventStatusHistory> optStatus = statusHistoryRepository.layTrangThaiHienTai(ctEvent.getId());
        if (optStatus.isPresent() == true) {
            String status = optStatus.get().getStatusCode();
            if (status.equals("OPEN") == false && status.equals("UPCOMING") == false) {
                throw new AppException(400,
                        "Xin lỗi, hiện tại sự kiện này không mở cổng đăng ký (Trạng thái: " + status + ").");
            }
        } else {
            throw new AppException(400, "Sự kiện này chưa được công bố hợp lệ.");
        }

        // 2. Thuật toán Đối soát Cung Cầu
        if (ctEvent.getTotalSlots() > 0) {
            long registered = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
            if (registered >= ctEvent.getTotalSlots()) {
                throw new AppException(400, "Rất tiếc, số lượng vé phát hành của hội thảo này đã đạt giới hạn tối đa.");
            }
        }

        // 3. ĐỐI SOÁT VÉ CÁ NHÂN (CHUẨN APPEND-ONLY)
        if (nguCanh.layUserId() != null) {
            // Chỉ kiểm tra vé mới nhất
            Optional<CtEventRegistration> optReg = registrationRepository
                    .findFirstByCtEventIdAndUserIdOrderByRegisteredAtDesc(ctEvent.getId(), nguCanh.layUserId());

            if (optReg.isPresent() == true) {
                CtEventRegistration latestReg = optReg.get();
                // Nếu vé mới nhất KHÔNG PHẢI là vé hủy -> Báo lỗi đang sở hữu vé
                if (latestReg.getStatus().equals("CANCELLED") == false) {
                    throw new AppException(400,
                            "Hệ thống ghi nhận tài khoản của bạn đang sở hữu một vé tham dự có hiệu lực tại phiên này.");
                }
                // NẾU LÀ VÉ HỦY -> Đi tiếp xuống dưới để đẻ ra 1 dòng vé MỚI.
                // Dòng Cancelled cũ bị đẩy lùi xuống thành lịch sử.
            }
        }

        // 4. KIẾN TẠO BẢN GHI MỚI TOANH (Bất chấp trước đó có bao nhiêu vé hủy)
        CtEventRegistration reg = new CtEventRegistration();
        reg.setCtEvent(ctEvent);
        reg.setGuestName(request.getGuestName());
        reg.setGuestEmail(request.getGuestEmail());
        reg.setGuestPhone(request.getGuestPhone());
        reg.setWorkplace(request.getWorkplace());
        reg.setStatus("PENDING");
        reg.setRegisteredAt(LocalDateTime.now());

        if (nguCanh.layUserId() != null) {
            Optional<User> optUser = userRepository.findById(nguCanh.layUserId());
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
    public List<EventStatusHistoryResponse> layLichSuTrangThaiPublic(Long ctEventId, NguCanhNguoiDung nguCanh) {
        if (coQuyenTruyCapBuoi(ctEventId, nguCanh) == false) {
            return new ArrayList<>();
        }

        List<CtEventStatusHistory> danhSach = statusHistoryRepository.findByCtEventIdOrderByChangedAtDesc(ctEventId);
        List<EventStatusHistoryResponse> result = new ArrayList<>();

        Object[] arr = danhSach.toArray();
        for (int i = 0; i < arr.length; i = i + 1) {
            CtEventStatusHistory h = (CtEventStatusHistory) arr[i];

            if (eventStatusDisplayPolicy.duocHienThiPublic(h.getStatusCode()) == false) {
                continue;
            }

            EventStatusHistoryResponse resp = new EventStatusHistoryResponse();
            resp.setId(h.getId());
            resp.setCtEventId(ctEventId);
            resp.setStatusCode(h.getStatusCode());
            resp.setDisplayStatus(eventStatusDisplayPolicy.layNhanPublic(h.getStatusCode()));
            resp.setChangedAt(h.getChangedAt());
            result.add(resp);
        }
        return result;
    }

    /**
     * Thuật toán trích xuất tóm tắt khách mời phục vụ Social Proof tại Frontend.
     * Áp dụng cơ chế che giấu dữ liệu (Masking) bằng vòng lặp For truyền thống.
     * Ý đồ nghiệp vụ: Luôn trả danh sách kể cả phiên bị khóa quyền,
     * nhằm phục vụ marketing — kích thích tò mò và thu hút đăng ký.
     */
    public EventAttendeePublicResponse layTomTatKhachMoiPublic(Long ctEventId, NguCanhNguoiDung nguCanh) {

        // 1. Đếm tổng số lượng (Tái sử dụng hàm đếm Slot để đồng bộ 100% với
        // thanh Tiến độ)
        long total = ctEventRepository.demSlotDaDangKy(ctEventId);

        // 2. Lấy danh sách 5 người đăng ký mới nhất (ĐÃ SỬA: Dùng hàm mới để loại bỏ vé
        // Cancelled)
        List<CtEventRegistration> latestRegs = registrationRepository.layDanhSachKhachMoiHopLe(ctEventId);
        List<EventAttendeePublicResponse.AttendeeMaskedInfo> maskedList = new ArrayList<>();

        Object[] regArray = latestRegs.toArray();
        int limit = 5;
        if (regArray.length < 5) {
            limit = regArray.length;
        }

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
            info.setRegisteredAt(reg.getRegisteredAt());
            if (reg.getWorkplace() != null) {
                info.setWorkplace("***");
            } else {
                info.setWorkplace(null);
            }

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



    /** Phân giải filter thời gian dùng chung cho list và stats để số liệu luôn khớp. */
    private KhoangThoiGianSuKien phanGiaiKhoangThoiGianSuKien(String time) {
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
            int thuTrongTuan = now.getDayOfWeek().getValue();
            startDate = now.minusDays(thuTrongTuan - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endDate = now.plusDays(7 - thuTrongTuan).withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else if ("NEXT_MONTH".equals(time) == true) {
            YearMonth thangSau = YearMonth.now().plusMonths(1);
            startDate = thangSau.atDay(1).atStartOfDay();
            endDate = thangSau.atEndOfMonth().atTime(23, 59, 59);
        } else if (time != null && time.contains("-") == true) {
            return phanGiaiKhoangThoiGianTuNgayThang(time);
        }

        return new KhoangThoiGianSuKien(startDate, endDate);
    }

    /** Parse YYYY-MM hoặc YYYY-MM-DD từ mini-calendar; lỗi format trả 400 có kiểm soát. */
    private KhoangThoiGianSuKien phanGiaiKhoangThoiGianTuNgayThang(String time) {
        String[] cacPhan = time.split("-");
        if (cacPhan.length < 2 || cacPhan.length > 3) {
            throw new AppException(400, "Bộ lọc thời gian sự kiện không hợp lệ.");
        }

        try {
            int nam = parseTimeFilterNumber(cacPhan[0]);
            int thang = parseTimeFilterNumber(cacPhan[1]);

            if (cacPhan.length == 3) {
                int ngay = parseTimeFilterNumber(cacPhan[2]);
                LocalDateTime startDate = LocalDateTime.of(nam, thang, ngay, 0, 0, 0);
                LocalDateTime endDate = LocalDateTime.of(nam, thang, ngay, 23, 59, 59);
                return new KhoangThoiGianSuKien(startDate, endDate);
            }

            YearMonth targetMonth = YearMonth.of(nam, thang);
            LocalDateTime startDate = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = targetMonth.atEndOfMonth().atTime(23, 59, 59);
            return new KhoangThoiGianSuKien(startDate, endDate);
        } catch (DateTimeException ex) {
            throw new AppException(400, "Bộ lọc thời gian sự kiện không hợp lệ.");
        }
    }

    /** Parse số trong bộ lọc thời gian và trả lỗi nghiệp vụ rõ thay vì NumberFormatException. */
    private int parseTimeFilterNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new AppException(400, "Bộ lọc thời gian sự kiện không hợp lệ.");
        }
    }

    /** Value object nội bộ cho khoảng thời gian đã chuẩn hóa của bộ lọc sự kiện. */
    private static class KhoangThoiGianSuKien {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        private KhoangThoiGianSuKien(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // =========================================================================
    // HÀM TIỆN ÍCH BÓC TÁCH DỮ LIỆU
    // =========================================================================

    @Override
    public boolean coQuyenTruyCapBuoi(Long ctEventId, NguCanhNguoiDung nguCanh) {
        Optional<CtEvent> optCtEvent = ctEventRepository.findById(ctEventId);
        if (optCtEvent.isPresent() == false) {
            return false;
        }

        EventAccessDecision access = xayDungQuyetDinhTruyCap(optCtEvent.get(), nguCanh);
        return access.hasFullAccess;
    }

    private EventAccessDecision xayDungQuyetDinhTruyCap(CtEvent ctEvent, NguCanhNguoiDung nguCanh) {
        EventAccessDecision decision = new EventAccessDecision();

        List<UserRole> requiredRoles = ctEventSessionRoleRepository.layDanhSachQuyenCuaBuoi(ctEvent.getId());
        if (requiredRoles == null || requiredRoles.isEmpty() == true) {
            decision.hasFullAccess = true;
            return decision;
        }

        Object[] roleArr = requiredRoles.toArray();
        for (int i = 0; i < roleArr.length; i = i + 1) {
            UserRole role = (UserRole) roleArr[i];

            CtEventResponse.RoleInfo roleInfo = new CtEventResponse.RoleInfo();
            roleInfo.setRoleId(role.getId());
            roleInfo.setRoleName(role.getRoleName());
            roleInfo.setDescription(role.getDescription());
            decision.requiredRolesInfo.add(roleInfo);
            decision.allowedRoleNames.add(role.getRoleName());

            if (role.getRoleLevel() != null && nguCanh.layCapBacCaoNhat() <= role.getRoleLevel()) {
                decision.hasFullAccess = true;
            }
        }

        return decision;
    }

    private String tinhTrangThaiHienThi(String status, int totalSlots, long availableSlots) {
        String chuoiHienThi = "Không xác định";

        if ("CANCELLED".equals(status) == true) {
            chuoiHienThi = "Đã hủy";
        } else if ("FINISHED".equals(status) == true || "ENDED".equals(status) == true) {
            chuoiHienThi = "Đã kết thúc";
        } else if ("ONGOING".equals(status) == true) {
            chuoiHienThi = "Đang diễn ra";
        } else if ("OPEN".equals(status) == true || "UPCOMING".equals(status) == true) {
            if (totalSlots > 0 && availableSlots <= 0) {
                chuoiHienThi = "Hết chỗ";
            } else {
                chuoiHienThi = "Sắp diễn ra";
            }
        } else if ("FULL".equals(status) == true) {
            chuoiHienThi = "Hết chỗ";
        }

        return chuoiHienThi;
    }

    /** Kiểm tra trạng thái đã kết thúc hoặc hủy — frontend được phép hiển thị công khai cho mọi user */
    private boolean laTrangThaiDongCongKhai(String status) {
        if ("CANCELLED".equals(status) == true) {
            return true;
        }
        if ("FINISHED".equals(status) == true || "ENDED".equals(status) == true) {
            return true;
        }
        return false;
    }

    /** Tạo bản tóm tắt marketing an toàn: loại bỏ HTML/script, ẩn URL, cắt 280 ký tự */
    private String taoMoTaMarketing(String rawDescription) {
        if (rawDescription == null) {
            return null;
        }

        String text = rawDescription
                .replaceAll("(?si)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?si)<style[^>]*>.*?</style>", " ")
                .replaceAll("<[^>]*>", " ")
                .replaceAll("(?i)https?://\\S+|www\\.\\S+", "[liên kết mở sau]")
                .replaceAll("\\s+", " ")
                .trim();

        if (text.length() > 280) {
            return text.substring(0, 280) + "...";
        }
        return text;
    }

    /**
     * Gom nhóm dữ liệu của một Chiến dịch, quét toàn bộ Phiên con.
     */
    private EventResponse xayDungEventResponse(Event event, NguCanhNguoiDung nguCanh) {
        EventResponse resp = new EventResponse();
        resp.setId(event.getId());
        resp.setTitle(event.getTitle());
        resp.setSlug(event.getSlug());
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
            sessionResponses.add(xayDungCtEventResponse(ce, nguCanh));

        }
        resp.setSessions(sessionResponses);

        boolean tatCaPhienBiKhoa = sessionResponses.isEmpty() == false;
        Object[] sessionArrForDescription = sessionResponses.toArray();
        for (int i = 0; i < sessionArrForDescription.length; i = i + 1) {
            CtEventResponse ss = (CtEventResponse) sessionArrForDescription[i];
            if (ss.isHasFullAccess() == true) {
                tatCaPhienBiKhoa = false;
                break;
            }
        }

        if (tatCaPhienBiKhoa == true) {
            resp.setDescription(taoMoTaMarketing(event.getDescription()));
        } else {
            resp.setDescription(event.getDescription());
        }

        List<String> gomQuyenChienDich = new ArrayList<>();
        Object[] sessionRespArr = sessionResponses.toArray();
        for (int i = 0; i < sessionRespArr.length; i++) {
            CtEventResponse ss = (CtEventResponse) sessionRespArr[i];
            if (ss.getAllowedRoleNames() != null) {
                Object[] rolesArr = ss.getAllowedRoleNames().toArray();
                for (int j = 0; j < rolesArr.length; j++) {
                    String rName = rolesArr[j].toString();
                    boolean daCo = false;
                    for (int k = 0; k < gomQuyenChienDich.size(); k++) {
                        if (gomQuyenChienDich.get(k).equals(rName)) {
                            daCo = true;
                            break;
                        }
                    }
                    if (!daCo) {
                        gomQuyenChienDich.add(rName);
                    }
                }
            }
        }
        resp.setAllowedRoleNames(gomQuyenChienDich);

        return resp;
    }

    /**
     * Bóc tách Phiên sự kiện, phân tích Slot trống và kết nối Dữ liệu Y khoa đính
     * kèm.
     */
    private CtEventResponse xayDungCtEventResponse(CtEvent ctEvent, NguCanhNguoiDung nguCanh) {
        CtEventResponse resp = new CtEventResponse();
        EventAccessDecision access = xayDungQuyetDinhTruyCap(ctEvent, nguCanh);

        // 1. Ánh xạ các thông tin định danh và thời gian cơ bản
        resp.setId(ctEvent.getId());
        resp.setTitle(ctEvent.getTitle());
        resp.setStartTime(ctEvent.getStartTime());
        resp.setEndTime(ctEvent.getEndTime());
        resp.setAllowedRoleNames(access.allowedRoleNames);

        if (access.hasFullAccess == true) {
            resp.setContent(ctEvent.getContent());
            resp.setSeoTitle(ctEvent.getSeoTitle());
            resp.setSeoDescription(ctEvent.getSeoDescription());
            resp.setHasFullAccess(true);
            resp.setRequiredRoles(null);
        } else {
            resp.setContent(null);
            resp.setSeoTitle(null);
            resp.setSeoDescription(null);
            resp.setHasFullAccess(false);
            resp.setRequiredRoles(access.requiredRolesInfo);
            resp.setAccessNoticeTitle(LOCKED_ACCESS_TITLE);
            resp.setAccessNoticeMessage(LOCKED_ACCESS_MESSAGE);
            resp.setRegistrationNotice(LOCKED_REGISTRATION_MESSAGE);
        }

        // 2. Phân giải thông tin Chiến dịch cha (EVENTS)
        if (ctEvent.getEvent() != null) {
            resp.setEventId(ctEvent.getEvent().getId());
            resp.setEventTitle(ctEvent.getEvent().getTitle());
            resp.setEventSlug(ctEvent.getEvent().getSlug());
        }

        // 3. Phân giải thông tin Địa điểm (LOCATIONS)
        if (ctEvent.getLocation() != null) {
            resp.setOnline(ctEvent.getLocation().isOnline());

            if (access.hasFullAccess == true) {
                resp.setLocationId(ctEvent.getLocation().getId());
                resp.setLocationName(ctEvent.getLocation().getName());
                resp.setLocationAddress(ctEvent.getLocation().getAddress());
            } else {
                resp.setLocationId(null);
                if (ctEvent.getLocation().isOnline() == true) {
                    resp.setLocationName("Phòng trực tuyến riêng");
                } else {
                    resp.setLocationName("Địa điểm dành riêng cho khách mời đủ điều kiện");
                }
                resp.setLocationAddress(LOCKED_LOCATION_MESSAGE);
            }
        }

        // 4. TRÍCH XUẤT TRẠNG THÁI GỐC TỪ NHẬT KÝ KIỂM TOÁN (Event Sourcing)
        String maTrangThaiGoc = "DRAFT";
        Optional<CtEventStatusHistory> optStatus = statusHistoryRepository.layTrangThaiHienTai(ctEvent.getId());
        if (optStatus.isPresent() == true) {
            maTrangThaiGoc = optStatus.get().getStatusCode();
        }

        // 5. THUẬT TOÁN ĐỐI SOÁT CUNG - CẦU VÀ TÍNH TOÁN "CHUẨN MÙ" CHO FRONTEND
        long soVeDaDangKy = ctEventRepository.demSlotDaDangKy(ctEvent.getId());
        long soVeConTrong = 0;

        if (ctEvent.getTotalSlots() > 0) {
            soVeConTrong = ctEvent.getTotalSlots() - soVeDaDangKy;
        }

        resp.setTotalSlots(ctEvent.getTotalSlots());
        resp.setRegisteredCount(soVeDaDangKy);
        resp.setAvailableSlots(soVeConTrong);

        if (access.hasFullAccess == true) {
            resp.setCurrentStatus(maTrangThaiGoc);
            resp.setDisplayStatus(tinhTrangThaiHienThi(maTrangThaiGoc, ctEvent.getTotalSlots(), soVeConTrong));
        } else {
            if (laTrangThaiDongCongKhai(maTrangThaiGoc) == true) {
                resp.setCurrentStatus(maTrangThaiGoc);
                resp.setDisplayStatus(tinhTrangThaiHienThi(maTrangThaiGoc, 0, 0));
            } else {
                resp.setCurrentStatus(STATUS_LOCKED);
                resp.setDisplayStatus(LOCKED_DISPLAY_STATUS);
            }
        }

        // --- BƯỚC B: THIẾT LẬP CỜ BÁO ĐỘNG ĐỎ (CRITICAL FLAG) ---
        // Thuật toán: Nếu số vé còn lại ít hơn hoặc bằng 20% tổng dung lượng -> Bật cờ
        // đỏ.
        boolean laToiHan = false;
        if (access.hasFullAccess == true && ctEvent.getTotalSlots() > 0) {
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
        // Ý đồ nghiệp vụ: Luôn hiển thị bài viết liên quan kể cả phiên bị khóa quyền,
        // nhằm kích thích tò mò — bài viết có cơ chế chặn xem riêng tại PostService.
        List<PostResponse> ketQuaPosts = new ArrayList<>();
        List<Post> danhSachBaiViet = ctPostEventRepository.layBaiVietLienQuan(ctEvent.getId());
        Object[] mangPost = danhSachBaiViet.toArray();
        for (int j = 0; j < mangPost.length; j = j + 1) {
            Post p = (Post) mangPost[j];
            PostResponse pr = new PostResponse();
            pr.setId(p.getId());
            pr.setTitle(p.getTitle());
            pr.setSlug(p.getSlug());
            pr.setSummary(p.getSummary());
            pr.setThumbnailUrl(p.getThumbnailUrl());
            // pr.setAccessLevel(p.getAccessLevel());

            if (p.getCategory() != null) {
                pr.setCategoryName(p.getCategory().getName());
            }
            if (p.getAuthor() != null) {
                pr.setAuthorName(p.getAuthor().getFullName());
            }
            ketQuaPosts.add(pr);
        }
        resp.setRelatedPosts(ketQuaPosts);

        return resp;
    }

    private static class EventAccessDecision {
        private boolean hasFullAccess = false;
        private final List<CtEventResponse.RoleInfo> requiredRolesInfo = new ArrayList<>();
        private final List<String> allowedRoleNames = new ArrayList<>();
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
        resp.setRegisteredAt(reg.getRegisteredAt());

        if (reg.getUser() != null) {
            resp.setUserId(reg.getUser().getId());
            resp.setUserName(reg.getUser().getFullName());
        }

        // [TÍCH HỢP CHUẨN MÙ] - Backend dịch mã trạng thái
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
                chuHienThi = maTrangThai; // Fallback
            }
        }

        resp.setStatus(maTrangThai);
        resp.setDisplayStatus(chuHienThi); // Đẩy chuỗi đã dịch ra Frontend

        return resp;
    }
}
