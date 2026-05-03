//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAdminSpeakerAgendaController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventAgendaRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.EventSpeakerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/events/sessions/{ctEventId}/speakers")
    public ApiResponse<EventSpeakerResponse> themDienGia(
            @PathVariable Long ctEventId, 
            @Valid @RequestBody EventSpeakerRequest request) {
        return ApiResponse.thanhCong(service.themDienGia(ctEventId, request), "Thêm Hồ sơ Diễn giả thành công");
    }

    @PutMapping("/events/speakers/{speakerId}")
    public ApiResponse<EventSpeakerResponse> capNhatDienGia(
            @PathVariable Long speakerId, 
            @Valid @RequestBody EventSpeakerRequest request) {
        return ApiResponse.thanhCong(service.capNhatDienGia(speakerId, request), "Cập nhật Diễn giả thành công");
    }

    @DeleteMapping("/events/speakers/{speakerId}")
    public ApiResponse<Void> xoaDienGia(@PathVariable Long speakerId) {
        service.xoaDienGia(speakerId);
        return ApiResponse.thanhCong(null, "Đã gỡ bỏ Diễn giả khỏi hệ thống");
    }

    // --- QUẢN LÝ LỊCH TRÌNH ---
    @PostMapping("/events/sessions/{ctEventId}/agenda")
    public ApiResponse<EventAgendaResponse> themLichTrinh(
            @PathVariable Long ctEventId, 
            @Valid @RequestBody EventAgendaRequest request) {
        return ApiResponse.thanhCong(service.themLichTrinh(ctEventId, request), "Lập mốc lịch trình thành công");
    }

    @PutMapping("/events/agenda/{agendaId}")
    public ApiResponse<EventAgendaResponse> capNhatLichTrinh(
            @PathVariable Long agendaId, 
            @Valid @RequestBody EventAgendaRequest request) {
        return ApiResponse.thanhCong(service.capNhatLichTrinh(agendaId, request), "Cập nhật lịch trình thành công");
    }

    @DeleteMapping("/events/agenda/{agendaId}")
    public ApiResponse<Void> xoaLichTrinh(@PathVariable Long agendaId) {
        service.xoaLichTrinh(agendaId);
        return ApiResponse.thanhCong(null, "Đã xóa mốc lịch trình");
    }
}