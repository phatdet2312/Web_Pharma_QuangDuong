//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminSpeakerAgendaController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventAgendaRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventSpeakerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =========================================================================
 * API QUẢN TRỊ LỊCH TRÌNH VÀ DIỄN GIẢ
 * =========================================================================
 * Quyền: ROLE_ADMIN. Giao tiếp thao tác CRUD tại admin/events.html.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ApiAdminSpeakerAgendaController {

    private final ISpeakerAgendaService service;

    // --- QUẢN LÝ DIỄN GIẢ ---
    /** Lấy toàn bộ diễn giả của một phiên cho màn hình quản trị. */
    @RequirePermission("EVENT_MANAGE_SPEAKER")
    @GetMapping("/events/sessions/{ctEventId}/speakers")
    public ApiResponse<List<EventSpeakerResponse>> layDanhSachDienGia(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSDienGiaCuaBuoi(ctEventId), "Tải danh sách Diễn giả thành công");
    }

    @RequirePermission("EVENT_MANAGE_SPEAKER")
    @PostMapping("/events/sessions/{ctEventId}/speakers")
    public ApiResponse<EventSpeakerResponse> themDienGia(
            @PathVariable Long ctEventId, 
            @Valid @RequestBody EventSpeakerRequest request) {
        return ApiResponse.thanhCong(service.themDienGia(ctEventId, request), "Thêm Hồ sơ Diễn giả thành công");
    }

    @RequirePermission("EVENT_MANAGE_SPEAKER")
    @PutMapping("/events/speakers/{speakerId}")
    public ApiResponse<EventSpeakerResponse> capNhatDienGia(
            @PathVariable Long speakerId, 
            @Valid @RequestBody EventSpeakerRequest request) {
        return ApiResponse.thanhCong(service.capNhatDienGia(speakerId, request), "Cập nhật Diễn giả thành công");
    }

    @RequirePermission("EVENT_MANAGE_SPEAKER")
    @DeleteMapping("/events/speakers/{speakerId}")
    public ApiResponse<Void> xoaDienGia(@PathVariable Long speakerId) {
        service.xoaDienGia(speakerId);
        return ApiResponse.thanhCong(null, "Đã gỡ bỏ Diễn giả khỏi hệ thống");
    }

    // --- QUẢN LÝ LỊCH TRÌNH ---
    /** Lấy lịch trình đầy đủ cho admin, bỏ qua paywall public vì đã qua route quản trị. */
    @RequirePermission("EVENT_MANAGE_AGENDA")
    @GetMapping("/events/sessions/{ctEventId}/agenda")
    public ApiResponse<List<EventAgendaResponse>> layDanhSachLichTrinh(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSLichTrinhCuaBuoi(ctEventId), "Tải lịch trình thành công");
    }

    @RequirePermission("EVENT_MANAGE_AGENDA")
    @PostMapping("/events/sessions/{ctEventId}/agenda")
    public ApiResponse<EventAgendaResponse> themLichTrinh(
            @PathVariable Long ctEventId, 
            @Valid @RequestBody EventAgendaRequest request) {
        return ApiResponse.thanhCong(service.themLichTrinh(ctEventId, request), "Lập mốc lịch trình thành công");
    }

    @RequirePermission("EVENT_MANAGE_AGENDA")
    @PutMapping("/events/agenda/{agendaId}")
    public ApiResponse<EventAgendaResponse> capNhatLichTrinh(
            @PathVariable Long agendaId, 
            @Valid @RequestBody EventAgendaRequest request) {
        return ApiResponse.thanhCong(service.capNhatLichTrinh(agendaId, request), "Cập nhật lịch trình thành công");
    }

    @RequirePermission("EVENT_MANAGE_AGENDA")
    @DeleteMapping("/events/agenda/{agendaId}")
    public ApiResponse<Void> xoaLichTrinh(@PathVariable Long agendaId) {
        service.xoaLichTrinh(agendaId);
        return ApiResponse.thanhCong(null, "Đã xóa mốc lịch trình");
    }
}
