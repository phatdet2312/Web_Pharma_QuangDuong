//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/PartnerViewController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * =========================================================================
 * VIEW CONTROLLER: CỔNG GIAO DIỆN ĐỐI TÁC
 * =========================================================================
 * Tuân thủ Điều 3 — Chỉ trả về tên file HTML.
 * Dữ liệu 100% lấy qua API (JS gọi /api/profile/**).
 */
@Controller
public class PartnerViewController {

    /** Ánh xạ cả /profile và /partner về cùng 1 trang — tránh 404 từ navbar */
    @GetMapping({"/profile", "/partner"})
    public String trangHoSo() {
        return "partner/profile";
    }
}
