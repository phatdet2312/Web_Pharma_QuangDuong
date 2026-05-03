//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IEventService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRegistrationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CtEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAttendeePublicResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventRegistrationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatusHistoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventTypeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LocationResponse;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface nghiệp vụ sự kiện phía public (trang events/list, events/detail).
 */
public interface IEventService {

    //  lấy danh sách địa điểm cho Public
    List<LocationResponse> layTatCaDiaDiemPublic();

    // lấy danh sách buổi sự kiện trong 1 tháng cụ thể (Mini Calendar)
    List<CtEventResponse> layBuoiTrongThang(int year, int month);


    /** Thống kê tổng quan cho Hero Stats trang public */
    EventStatsResponse layThongKeTrangSuKien(Integer type, String time);

    /** Lấy danh sách loại sự kiện cho filter sidebar */
    List<EventTypeResponse> layTatCaLoaiSuKien();

    /**
     * Tìm kiếm + lọc chiến dịch sự kiện kèm buổi.
     * Mỗi EventResponse chứa list sessions (CtEventResponse).
     */
    Page<EventResponse> timKiemSuKien(String keyword, Integer eventTypeId, String timeFilter,
                                       Integer locationId, String sortBy, int page, int size);

    /** Lấy chi tiết chiến dịch theo slug */
    EventResponse layChiTietSuKien(String slug);

    /** Lấy buổi sự kiện sắp tới (cho sidebar "Sự kiện sắp diễn ra") */
    List<CtEventResponse> layBuoiSapToi(int limit);

    /** Lấy các buổi user đã đăng ký (cho sidebar "Đăng ký của tôi") */
    List<EventRegistrationResponse> layDangKyCuaToi(Long userId);

    /** Đăng ký tham dự buổi sự kiện */
    EventRegistrationResponse dangKyThamDu(EventRegistrationRequest request, Long userId);

    /** Hủy đăng ký */
    void huyDangKy(Long registrationId, Long userId);

    /** Lịch sử trạng thái buổi sự kiện (public — dùng cho timeline events/detail.html) */
    List<EventStatusHistoryResponse> layLichSuTrangThaiPublic(Long ctEventId);

    /** Trích xuất tóm tắt danh sách chuyên gia đăng ký phục vụ Social Proof (Đã qua xử lý Masking) */
    EventAttendeePublicResponse layTomTatKhachMoiPublic(Long ctEventId);

    /**
     * Lấy chi tiết một Buổi sự kiện theo ID (G1).
     * Endpoint: GET /api/events/sessions/{ctEventId}
     * Dùng cho events/detail.html khi load thông tin nhanh facts của một buổi cụ thể.
     */
    CtEventResponse layChiTietBuoi(Long ctEventId);
}