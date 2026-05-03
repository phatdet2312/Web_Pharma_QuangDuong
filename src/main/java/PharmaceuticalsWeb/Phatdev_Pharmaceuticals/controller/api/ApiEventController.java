//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiEventController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventRegistrationRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CmtResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.CtEventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAttendeePublicResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventRegistrationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatsResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventStatusHistoryResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventTypeResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.LocationResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.CommentRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ICommentService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * =========================================================================
 * API SỰ KIỆN PHÍA PUBLIC
 * =========================================================================
 * Prefix /api/events. Trả về JSON qua ApiResponse<T>.
 * events/list.html và events/detail.html gọi các endpoint này.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ApiEventController {

    private final IEventService eventService;
    private final ICommentService commentService;
    private final IUserService userService;

    /** Thống kê Hero Stats trang sự kiện */
    @GetMapping("/stats")
    public ApiResponse<EventStatsResponse> layThongKe(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String time) {
        // Truyền type và time xuống Service
        return ApiResponse.thanhCong(eventService.layThongKeTrangSuKien(type, time), "Lấy thống kê thành công");
    }


    /** Danh sách địa điểm cho filter sidebar */
    @GetMapping("/locations")
    public ApiResponse<List<LocationResponse>> layDiaDiem() {
        return ApiResponse.thanhCong(eventService.layTatCaDiaDiemPublic(), "Lấy địa điểm thành công");
    }

    /** Lấy dữ liệu cho Lịch Mini (Đánh dấu ngày có sự kiện) */
    @GetMapping("/calendar")
    public ApiResponse<List<CtEventResponse>> layLichTrongThang(
            @RequestParam int year, 
            @RequestParam int month) {
        return ApiResponse.thanhCong(eventService.layBuoiTrongThang(year, month), "Lấy lịch sự kiện thành công");
    }

    /** Danh sách loại sự kiện cho filter sidebar */
    @GetMapping("/types")
    public ApiResponse<List<EventTypeResponse>> layLoaiSuKien() {
        return ApiResponse.thanhCong(eventService.layTatCaLoaiSuKien(), "Lấy loại sự kiện thành công");
    }

    /** Tìm kiếm chiến dịch sự kiện với phân trang */
    @GetMapping
    public ApiResponse<Page<EventResponse>> timKiemSuKien(
        @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Page<EventResponse> result = eventService.timKiemSuKien(keyword, type, time, locationId, sort, page, size);
        return ApiResponse.thanhCong(result, "Lấy danh sách sự kiện thành công");
    }

    /** Chi tiết chiến dịch theo slug */
    @GetMapping("/{slug}")
    public ApiResponse<EventResponse> layChiTiet(@PathVariable String slug) {
        return ApiResponse.thanhCong(eventService.layChiTietSuKien(slug), "Lấy chi tiết sự kiện thành công");
    }

    /** Buổi sự kiện sắp tới (sidebar "Upcoming events") */
    @GetMapping("/upcoming")
    public ApiResponse<List<CtEventResponse>> layBuoiSapToi(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.thanhCong(eventService.layBuoiSapToi(limit), "Lấy sự kiện sắp tới thành công");
    }

    /** Đăng ký của tôi (sidebar "My registrations") */
    @GetMapping("/my-registrations")
    public ApiResponse<List<EventRegistrationResponse>> layDangKyCuaToi(Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập để xem đăng ký");
        }
        return ApiResponse.thanhCong(eventService.layDangKyCuaToi(userId), "Lấy đăng ký thành công");
    }

    /** Đăng ký tham dự buổi sự kiện */
    @PostMapping("/register")
    public ApiResponse<EventRegistrationResponse> dangKy(
            @Valid @RequestBody EventRegistrationRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        EventRegistrationResponse result = eventService.dangKyThamDu(request, userId);
        return ApiResponse.thanhCong(result, "Đăng ký tham dự thành công");
    }

    /** Hủy đăng ký */
    @DeleteMapping("/registrations/{id}")
    public ApiResponse<Void> huyDangKy(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập");
        }
        eventService.huyDangKy(id, userId);
        return ApiResponse.thanhCong(null, "Hủy đăng ký thành công");
    }

    /** G1: Chi tiết một buổi sự kiện (session detail card trên events/detail.html) */
    @GetMapping("/sessions/{ctEventId}")
    public ApiResponse<CtEventResponse> layChiTietBuoi(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(eventService.layChiTietBuoi(ctEventId), "Lấy chi tiết buổi thành công");
    }

    /** Lấy bình luận của buổi sự kiện */
    @GetMapping("/sessions/{ctEventId}/comments")
    public ApiResponse<Page<CmtResponse>> layCmtBuoi(
            @PathVariable Long ctEventId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
             Authentication authentication) {

        // Giải mã định danh người dùng (Sẽ trả về null nếu là khách ẩn danh)
        Long userId = layUserIdTuAuthentication(authentication);
        Page<CmtResponse> result = commentService.layCmtCuaBuoi(ctEventId, sortBy, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy bình luận thành công");
    }

    /** Lịch sử trạng thái buổi sự kiện (cho timeline events/detail.html) */
    @GetMapping("/sessions/{ctEventId}/status-history")
    public ApiResponse<List<EventStatusHistoryResponse>> layLichSuTrangThai(
            @PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(
                eventService.layLichSuTrangThaiPublic(ctEventId),
                "Lấy lịch sử trạng thái thành công");
    }

    /** Gửi bình luận cho buổi sự kiện */
    @PostMapping("/sessions/{ctEventId}/comments")
    public ApiResponse<CmtResponse> guiCmtBuoi(
            @PathVariable Long ctEventId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập để bình luận");
        }
        request.setTargetId(ctEventId);
        CmtResponse result = commentService.guiCmtSuKien(request, userId);
        return ApiResponse.thanhCong(result, "Gửi bình luận thành công");
    }

    /** Tra cứu tóm tắt danh sách chuyên gia đã đăng ký */
    @GetMapping("/sessions/{ctEventId}/attendees-summary")
    public ApiResponse<EventAttendeePublicResponse> layTomTatKhachMoi(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(eventService.layTomTatKhachMoiPublic(ctEventId), "Tải tóm tắt đăng ký thành công");
    }

    /**
     * Thuật toán Định danh Chuyên sâu: 
     * Ủy thác hoàn toàn cho UserService để đọc Principal bất chấp Oauth2 hay JWT.
     */
    private Long layUserIdTuAuthentication(Authentication authentication) {
        if (authentication == null || authentication.isAuthenticated() == false || "anonymousUser".equals(authentication.getPrincipal())) {
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