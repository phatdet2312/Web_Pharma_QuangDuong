//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiPublicSpeakerAgendaController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{ctEventId}/speakers")
    public ApiResponse<List<EventSpeakerResponse>> layDSDienGia(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSDienGiaCuaBuoi(ctEventId), "Tải danh sách diễn giả thành công");
    }

    @GetMapping("/{ctEventId}/agenda")
    public ApiResponse<List<EventAgendaResponse>> layDSLichTrinh(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSLichTrinhCuaBuoi(ctEventId), "Tải lịch trình chi tiết thành công");
    }
}