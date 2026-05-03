//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminEventController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.BulkActionRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CtEventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventStatusRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventTypeRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.LocationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CtEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventRegistrationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatusHistoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventTypeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LocationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAdminEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =========================================================================
 * API QUẢN TRỊ SỰ KIỆN (ADMIN)
 * =========================================================================
 * Prefix /api/admin/events. Yêu cầu quyền ROLE_ADMIN.
 * admin/events.html gọi các endpoint này để CRUD chiến dịch, buổi, đăng ký.
 */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class ApiAdminEventController {

    private final IAdminEventService adminEventService;
    private final IUserService userService;

    /** Thống kê admin sự kiện */
    @GetMapping("/stats")
    public ApiResponse<EventStatsResponse> layThongKe() {
        return ApiResponse.thanhCong(adminEventService.layThongKeAdmin(), "Lấy thống kê thành công");
    }

    // === CRUD CHIẾN DỊCH (EVENTS) ===

    /**
     * Vận hành Cỗ máy tìm kiếm đa chiều.
     * Tiếp nhận mảng tham số và ủy thác cho tầng Service xử lý.
     */
    @GetMapping
    public ApiResponse<Page<EventResponse>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.thanhCong(
                adminEventService.layDanhSachChienDich(keyword, type, startDate, endDate, locationId, page, size),
                "Lấy danh sách thành công");
    }

    @PostMapping
    public ApiResponse<EventResponse> taoChienDich(@Valid @RequestBody EventRequest request) {
        return ApiResponse.thanhCong(adminEventService.taoChienDich(request), "Tạo chiến dịch thành công");
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> capNhatChienDich(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest request) {
        return ApiResponse.thanhCong(adminEventService.capNhatChienDich(eventId, request), "Cập nhật thành công");
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> xoaChienDich(@PathVariable Long eventId) {
        adminEventService.xoaChienDich(eventId);
        return ApiResponse.thanhCong(null, "Xóa chiến dịch thành công");
    }

    @PostMapping("/bulk/status")
    public ApiResponse<Void> doiTrangThaiNhieuChienDich(
            @Valid @RequestBody BulkActionRequest request,
            Authentication authentication) {

        Long moderatorId = layUserIdTuAuthentication(authentication);
        if (moderatorId == null) {
            return ApiResponse.loi(401, "Phiên đăng nhập không hợp lệ");
        }

        adminEventService.doiTrangThaiNhieuChienDich(request, moderatorId);
        return ApiResponse.thanhCong(null, "Thực thi thay đổi trạng thái hàng loạt thành công");
    }

    // === CRUD BUỔI (CT_EVENTS) ===

    @PostMapping("/sessions")
    public ApiResponse<CtEventResponse> taoBuoi(
            @Valid @RequestBody CtEventRequest request,
            Authentication authentication) {

        Long moderatorId = layUserIdTuAuthentication(authentication);
        return ApiResponse.thanhCong(adminEventService.taoBuoi(request, moderatorId), "Tạo buổi thành công");
    }

    @PutMapping("/sessions/{ctEventId}")
    public ApiResponse<CtEventResponse> capNhatBuoi(
            @PathVariable Long ctEventId,
            @Valid @RequestBody CtEventRequest request) {
        return ApiResponse.thanhCong(adminEventService.capNhatBuoi(ctEventId, request), "Cập nhật buổi thành công");
    }

    @DeleteMapping("/sessions/{ctEventId}")
    public ApiResponse<Void> xoaBuoi(@PathVariable Long ctEventId) {
        adminEventService.xoaBuoi(ctEventId);
        return ApiResponse.thanhCong(null, "Xóa buổi thành công");
    }

    // === TRẠNG THÁI BUỔI ===

    @GetMapping("/sessions/{ctEventId}/status-history")
    public ApiResponse<List<EventStatusHistoryResponse>> layLichSuTrangThai(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(adminEventService.layLichSuTrangThai(ctEventId), "Lấy lịch sử thành công");
    }

    @PostMapping("/sessions/status")
    public ApiResponse<Void> doiTrangThai(
            @Valid @RequestBody EventStatusRequest request,
            Authentication authentication) {

        Long moderatorId = layUserIdTuAuthentication(authentication);
        adminEventService.doiTrangThaiBuoi(request, moderatorId);
        return ApiResponse.thanhCong(null, "Thay đổi trạng thái thành công");
    }

    // === ĐĂNG KÝ ===

    @GetMapping("/sessions/{ctEventId}/registrations")
    public ApiResponse<List<EventRegistrationResponse>> layDanhSachDangKy(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(adminEventService.layDanhSachDangKy(ctEventId),
                "Lấy danh sách đăng ký thành công");
    }

    @PatchMapping("/registrations/{id}/status")
    public ApiResponse<Void> capNhatTrangThaiDangKy(
            @PathVariable Long id,
            @RequestParam String status) {
        adminEventService.capNhatTrangThaiDangKy(id, status);
        return ApiResponse.thanhCong(null, "Cập nhật trạng thái đăng ký thành công");
    }

    // === CRUD LOẠI SỰ KIỆN ===

    @GetMapping("/types")
    public ApiResponse<List<EventTypeResponse>> layLoaiSuKien() {
        return ApiResponse.thanhCong(adminEventService.layTatCaLoaiSuKien(), "Lấy loại sự kiện thành công");
    }

    @PostMapping("/types")
    public ApiResponse<EventTypeResponse> taoLoai(@Valid @RequestBody EventTypeRequest request) {
        return ApiResponse.thanhCong(adminEventService.taoLoaiSuKien(request), "Tạo loại sự kiện thành công");
    }

    @PutMapping("/types/{id}")
    public ApiResponse<EventTypeResponse> capNhatLoai(
            @PathVariable Integer id,
            @Valid @RequestBody EventTypeRequest request) {
        return ApiResponse.thanhCong(adminEventService.capNhatLoaiSuKien(id, request), "Cập nhật thành công");
    }

    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> xoaLoai(@PathVariable Integer id) {
        adminEventService.xoaLoaiSuKien(id);
        return ApiResponse.thanhCong(null, "Xóa loại sự kiện thành công");
    }

    // === CRUD ĐỊA ĐIỂM ===

    @GetMapping("/locations")
    public ApiResponse<List<LocationResponse>> layDiaDiem() {
        return ApiResponse.thanhCong(adminEventService.layTatCaDiaDiem(), "Lấy địa điểm thành công");
    }

    @PostMapping("/locations")
    public ApiResponse<LocationResponse> taoDiaDiem(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.thanhCong(adminEventService.taoDiaDiem(request), "Tạo địa điểm thành công");
    }

    @PutMapping("/locations/{id}")
    public ApiResponse<LocationResponse> capNhatDiaDiem(
            @PathVariable Integer id,
            @Valid @RequestBody LocationRequest request) {
        return ApiResponse.thanhCong(adminEventService.capNhatDiaDiem(id, request), "Cập nhật địa điểm thành công");
    }

    @DeleteMapping("/locations/{id}")
    public ApiResponse<Void> xoaDiaDiem(@PathVariable Integer id) {
        adminEventService.xoaDiaDiem(id);
        return ApiResponse.thanhCong(null, "Xóa địa điểm thành công");
    }

    /**
     * Thuật toán Định danh Chuyên sâu:
     * Ủy thác hoàn toàn cho UserService để đọc Principal bất chấp Oauth2 hay JWT.
     */
    private Long layUserIdTuAuthentication(Authentication authentication) {
        if (authentication == null || authentication.isAuthenticated() == false
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            User user = userService.getCurrentAuthenticatedUser();
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
