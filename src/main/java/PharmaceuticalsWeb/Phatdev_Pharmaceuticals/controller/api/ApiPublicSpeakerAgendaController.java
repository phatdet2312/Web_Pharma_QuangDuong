//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiPublicSpeakerAgendaController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * =========================================================================
 * API PUBLIC: TRA CỨU LỊCH TRÌNH & DIỄN GIẢ
 * =========================================================================
 */
@RestController
@RequestMapping("/api/events/sessions")
@RequiredArgsConstructor
public class ApiPublicSpeakerAgendaController {

    private final ISpeakerAgendaService service;

    /**
     * Lấy danh sách diễn giả của một phiên sự kiện (Public API).
     * Ý đồ nghiệp vụ: Luôn trả toàn bộ diễn giả kể cả phiên bị khóa quyền,
     * nhằm phục vụ marketing — kích thích tò mò và thu hút đăng ký.
     */
    @GetMapping("/{ctEventId}/speakers")
    public ApiResponse<List<EventSpeakerResponse>> layDSDienGia(
            @PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(
                service.layDSDienGiaCuaBuoi(ctEventId, true),
                "Tải danh sách diễn giả thành công");
    }

    /**
     * Lấy lịch trình chi tiết của một phiên sự kiện (Public API).
     * Ý đồ nghiệp vụ: Luôn trả toàn bộ lịch trình kể cả phiên bị khóa quyền,
     * nhằm phục vụ marketing — kích thích tò mò và thu hút đăng ký.
     */
    @GetMapping("/{ctEventId}/agenda")
    public ApiResponse<List<EventAgendaResponse>> layDSLichTrinh(
            @PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSLichTrinhCuaBuoi(ctEventId, true),
                "Tải lịch trình chi tiết thành công");
    }


}
