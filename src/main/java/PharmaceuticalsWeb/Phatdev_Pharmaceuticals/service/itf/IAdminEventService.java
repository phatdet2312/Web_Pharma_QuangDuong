//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IAdminEventService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

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
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ: QUẢN TRỊ CHIẾN DỊCH SỰ KIỆN (ADMIN)
 * =========================================================================
 * Thiết lập hợp đồng kỹ thuật cho các thao tác điều phối Chiến dịch, 
 * Trạm sự kiện, Hồ sơ đăng ký và Lịch sử trạng thái.
 */
public interface IAdminEventService {

    /** Trích xuất Bảng chỉ số (KPIs) vận hành sự kiện */
    EventStatsResponse layThongKeAdmin();

    /** Trích xuất danh sách Chiến dịch phân trang thông qua Cỗ máy tìm kiếm đa chiều */
    Page<EventResponse> layDanhSachChienDich(
            String keyword, Integer eventTypeId, 
            LocalDateTime startDate, LocalDateTime endDate, 
            Integer locationId, Integer roleId, int page, int size);

    /** Khởi tạo một Chiến dịch Marketing y khoa */
    EventResponse taoChienDich(EventRequest request);
    
    /** Hiệu đính cấu trúc của một Chiến dịch */
    EventResponse capNhatChienDich(Long eventId, EventRequest request);
    
    /** Tiêu hủy vật lý một Chiến dịch */
    void xoaChienDich(Long eventId);

    /** Khởi tạo một Trạm sự kiện trực thuộc Chiến dịch */
    CtEventResponse taoBuoi(CtEventRequest request, Long moderatorId);
    
    /** Hiệu đính chi tiết Trạm sự kiện */
    CtEventResponse capNhatBuoi(Long ctEventId, CtEventRequest request);
    
    /** Tiêu hủy vật lý Trạm sự kiện */
    void xoaBuoi(Long ctEventId);

    /** Tra cứu toàn bộ Lịch sử dịch chuyển trạng thái của Trạm sự kiện */
    List<EventStatusHistoryResponse> layLichSuTrangThai(Long ctEventId);
    
    /** Chèn thêm một mốc Trạng thái mới vào Sổ tay Kiểm toán */
    void doiTrangThaiBuoi(EventStatusRequest request, Long moderatorId);

    /** Tra cứu danh sách khách mời đã nộp hồ sơ tham dự */
    List<EventRegistrationResponse> layDanhSachDangKy(Long ctEventId);

    /** Hiệu đính trạng thái xác nhận của một hồ sơ đăng ký */
    void capNhatTrangThaiDangKy(Long registrationId, String newStatus);

    /** Khai thác danh mục phân loại Sự kiện */
    List<EventTypeResponse> layTatCaLoaiSuKien();
    EventTypeResponse taoLoaiSuKien(EventTypeRequest request);
    EventTypeResponse capNhatLoaiSuKien(Integer typeId, EventTypeRequest request);
    void xoaLoaiSuKien(Integer typeId);

    /** Khai thác danh bạ Tọa độ địa lý / Phòng họp trực tuyến */
    List<LocationResponse> layTatCaDiaDiem();
    LocationResponse taoDiaDiem(LocationRequest request);
    LocationResponse capNhatDiaDiem(Integer locationId, LocationRequest request);
    void xoaDiaDiem(Integer locationId);

    /** Thực thi lệnh dịch chuyển trạng thái lên hàng loạt Chiến dịch sự kiện */
    void doiTrangThaiNhieuChienDich(BulkActionRequest request, Long moderatorId);
}