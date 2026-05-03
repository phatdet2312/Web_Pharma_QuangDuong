//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/UserTrackingServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserActionLog;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserLoginLog;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.ModerationAction;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserActionLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserLoginLogRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IModerationActionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * =========================================================================
 * THỰC THI: DỊCH VỤ GHI DẤU VẾT HOẠT ĐỘNG
 * =========================================================================
 * Áp dụng pattern trichXuatThongTinMang() từ democach2:
 * tự động lấy IP thực (xuyên qua reverse-proxy) và User-Agent từ request context,
 * không buộc caller phải truyền — giảm coupling và loại bỏ trùng lặp code.
 */
@Service
@RequiredArgsConstructor
public class UserTrackingServiceImpl implements IUserTrackingService {

    private final ICtUserLoginLogRepository loginLogRepository;
    private final ICtUserActionLogRepository actionLogRepository;
    private final IModerationActionRepository actionRepository;

    // =========================================================================
    // API CÔNG KHAI
    // =========================================================================

    @Override
    public void ghiDangNhap(Long userId, String ipAddress, String userAgent,
                            boolean isSuccess, String usernameAttempt, String reason) {
        String actionCode = isSuccess ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        Optional<ModerationAction> optAction = actionRepository.findByCode(actionCode);
        if (optAction.isEmpty()) {
            return;
        }

        String[] thongTinMang = trichXuatThongTinMang(ipAddress, userAgent);

        CtUserLoginLog log = new CtUserLoginLog();
        log.setUserId(userId);
        log.setActionId(optAction.get().getId());
        log.setUsernameAttempt(usernameAttempt);
        log.setLoginIp(thongTinMang[0]);
        log.setUserAgent(thongTinMang[1]);
        log.setMessage(reason);

        loginLogRepository.save(log);
    }

    @Override
    public void ghiDangXuat(Long userId, String ipAddress, String userAgent) {
        Optional<ModerationAction> optAction = actionRepository.findByCode("LOGOUT");
        if (optAction.isEmpty()) {
            return;
        }

        String[] thongTinMang = trichXuatThongTinMang(ipAddress, userAgent);

        CtUserLoginLog log = new CtUserLoginLog();
        log.setUserId(userId);
        log.setActionId(optAction.get().getId());
        log.setLoginIp(thongTinMang[0]);
        log.setUserAgent(thongTinMang[1]);

        loginLogRepository.save(log);
    }

    @Override
    public void ghiHanhVi(Long userId, String actionCode, Long targetEntityId,
                          String oldPayload, String newPayload) {
        Optional<ModerationAction> optAction = actionRepository.findByCode(actionCode);
        if (optAction.isEmpty()) {
            return;
        }

        String[] thongTinMang = trichXuatThongTinMang(null, null);

        CtUserActionLog log = new CtUserActionLog();
        log.setUserId(userId);
        log.setActionId(optAction.get().getId());
        log.setTargetEntityId(targetEntityId);
        log.setOldPayload(oldPayload);
        log.setNewPayload(newPayload);
        log.setIpAddress(thongTinMang[0]);
        log.setUserAgent(thongTinMang[1]);

        actionLogRepository.save(log);
    }

    // =========================================================================
    // PHƯƠNG THỨC NỘI BỘ
    // =========================================================================

    /**
     * Trích xuất IP thực và User-Agent từ request context hiện tại.
     * Xử lý header X-Forwarded-For để lấy IP gốc qua reverse-proxy.
     * Nếu caller đã truyền giá trị sẵn (không null) thì dùng ngay, không trích xuất.
     *
     * @param ipOverride  IP đã biết sẵn (null → tự lấy từ request)
     * @param uaOverride  UA đã biết sẵn (null → tự lấy từ request)
     * @return String[2]: [0] = ipAddress, [1] = userAgent
     */
    private String[] trichXuatThongTinMang(String ipOverride, String uaOverride) {
        String ipAddress = (ipOverride != null) ? ipOverride : "Unknown";
        String userAgent = (uaOverride != null) ? uaOverride : "Unknown";

        if (ipOverride != null && uaOverride != null) {
            return new String[]{ipAddress, userAgent};
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return new String[]{ipAddress, userAgent};
        }

        HttpServletRequest request = attrs.getRequest();

        if (ipOverride == null) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && forwardedFor.isEmpty() == false) {
                ipAddress = forwardedFor.split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }
        }

        if (uaOverride == null) {
            String uaHeader = request.getHeader("User-Agent");
            if (uaHeader != null) {
                userAgent = uaHeader.length() > 490 ? uaHeader.substring(0, 490) : uaHeader;
            }
        }

        return new String[]{ipAddress, userAgent};
    }
}
