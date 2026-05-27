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
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils.PagingUtil;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDung;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDungFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
    private final NguCanhNguoiDungFactory nguCanhFactory;

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
            @RequestParam int month,
            Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(eventService.layBuoiTrongThang(year, month, nguCanh), "Lấy lịch sự kiện thành công");
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
            @RequestParam(required = false) Integer roleId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        Page<EventResponse> result = eventService.timKiemSuKien(keyword, type, time, locationId, roleId, sort, page,
                size, nguCanh);
        return ApiResponse.thanhCong(result, "Lấy danh sách sự kiện thành công");
    }

    /** Chi tiết chiến dịch theo slug */
    @GetMapping("/{slug}")
    public ApiResponse<EventResponse> layChiTiet(@PathVariable String slug, Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(eventService.layChiTietSuKien(slug, nguCanh), "Lấy chi tiết sự kiện thành công");
    }

    /** Buổi sự kiện sắp tới (sidebar "Upcoming events") */
    @GetMapping("/upcoming")
    public ApiResponse<List<CtEventResponse>> layBuoiSapToi(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(eventService.layBuoiSapToi(limit, nguCanh), "Lấy sự kiện sắp tới thành công");
    }

    /** Đăng ký của tôi (sidebar "My registrations") */
   @GetMapping("/my-registrations")
    public ApiResponse<Page<EventRegistrationResponse>> layDangKyCuaToi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            Authentication authentication) {
        
        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            return ApiResponse.loi(401, "Vui lòng đăng nhập để xem đăng ký");
        }
        Page<EventRegistrationResponse> result = eventService.layDangKyCuaToi(userId, page, size);
        return ApiResponse.thanhCong(result, "Lấy đăng ký thành công");
    }

    /**  Kiểm tra vé của tôi tại một buổi cụ thể*/
    @GetMapping("/sessions/{ctEventId}/my-ticket")
    public ApiResponse<EventRegistrationResponse> kiemTraVeCuaToi(
            @PathVariable Long ctEventId,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        if (userId == null) {
            // Không throw 401 để tránh rác Console của khách vãng lai
            return ApiResponse.thanhCong(null, "Khách vãng lai"); 
        }

        EventRegistrationResponse ticket = eventService.layVeCuaToiTaiBuoiNay(ctEventId, userId);
        return ApiResponse.thanhCong(ticket, "Kiểm tra vé thành công");
    }

    /** Đăng ký tham dự buổi sự kiện */
    @PostMapping("/register")
    public ApiResponse<EventRegistrationResponse> dangKy(
            @Valid @RequestBody EventRegistrationRequest request,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        EventRegistrationResponse result = eventService.dangKyThamDu(request, nguCanh);
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
    public ApiResponse<CtEventResponse> layChiTietBuoi(
            @PathVariable Long ctEventId,
            Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(eventService.layChiTietBuoi(ctEventId, nguCanh), "Lấy chi tiết buổi thành công");
    }

    /** Lấy bình luận của buổi sự kiện */
    @GetMapping("/sessions/{ctEventId}/comments")
    public ApiResponse<Page<CmtResponse>> layCmtBuoi(
            @PathVariable Long ctEventId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
             Authentication authentication) {

        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        if (eventService.coQuyenTruyCapBuoi(ctEventId, nguCanh) == false) {
            Page<CmtResponse> emptyPage = new PageImpl<>(
                    new ArrayList<>(), PageRequest.of(PagingUtil.chuanHoaPage(page), PagingUtil.chuanHoaSize(size)), 0);
            return ApiResponse.thanhCong(emptyPage, "Bình luận chỉ mở cho tài khoản đủ quyền tham dự.");
        }
        Page<CmtResponse> result = commentService.layCmtCuaBuoi(ctEventId, sortBy, page, size, userId);
        return ApiResponse.thanhCong(result, "Lấy bình luận thành công");
    }

    /** Lịch sử trạng thái buổi sự kiện (cho timeline events/detail.html) */
    @GetMapping("/sessions/{ctEventId}/status-history")
    public ApiResponse<List<EventStatusHistoryResponse>> layLichSuTrangThai(
            @PathVariable Long ctEventId,
            Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(
                eventService.layLichSuTrangThaiPublic(ctEventId, nguCanh),
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
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        if (eventService.coQuyenTruyCapBuoi(ctEventId, nguCanh) == false) {
            return ApiResponse.loi(403, "Bình luận chỉ mở cho tài khoản đủ quyền tham dự phiên này.");
        }
        request.setTargetId(ctEventId);
        CmtResponse result = commentService.guiCmtSuKien(request, userId);
        return ApiResponse.thanhCong(result, "Gửi bình luận thành công");
    }

    /** Tra cứu tóm tắt danh sách chuyên gia đã đăng ký */
    @GetMapping("/sessions/{ctEventId}/attendees-summary")
    public ApiResponse<EventAttendeePublicResponse> layTomTatKhachMoi(
            @PathVariable Long ctEventId,
            Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        return ApiResponse.thanhCong(eventService.layTomTatKhachMoiPublic(ctEventId, nguCanh),
                "Tải tóm tắt đăng ký thành công");
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
