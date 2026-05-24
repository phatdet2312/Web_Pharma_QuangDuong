//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiPublicSpeakerAgendaController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventAgendaResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.EventSpeakerResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEventService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.ISpeakerAgendaService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDung;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support.NguCanhNguoiDungFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    private final IEventService eventService;
    private final IUserService userService;
    private final NguCanhNguoiDungFactory nguCanhFactory;

    @GetMapping("/{ctEventId}/speakers")
    public ApiResponse<List<EventSpeakerResponse>> layDSDienGia(@PathVariable Long ctEventId) {
        return ApiResponse.thanhCong(service.layDSDienGiaCuaBuoi(ctEventId), "Tải danh sách diễn giả thành công");
    }

    @GetMapping("/{ctEventId}/agenda")
    public ApiResponse<List<EventAgendaResponse>> layDSLichTrinh(
            @PathVariable Long ctEventId,
            Authentication authentication) {
        Long userId = layUserIdTuAuthentication(authentication);
        NguCanhNguoiDung nguCanh = nguCanhFactory.taoNguCanh(userId);
        boolean coQuyenXemChiTiet = eventService.coQuyenTruyCapBuoi(ctEventId, nguCanh);
        return ApiResponse.thanhCong(service.layDSLichTrinhCuaBuoi(ctEventId, coQuyenXemChiTiet),
                "Tải lịch trình chi tiết thành công");
    }

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
