//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/EventViewController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: ĐIỀU HƯỚNG GIAO DIỆN SỰ KIỆN (VIEW CONTROLLER)
 * =========================================================================
 * Tuân thủ Điều 3: chỉ trả về tên file HTML. Mọi dữ liệu thực tế do JS đảm nhiệm.
 */
@Controller
public class EventViewController {

    /**
     * Định tuyến danh sách sự kiện, hỗ trợ đa URL phục vụ SEO.
     */
    @GetMapping({"/events", "/su-kien", "/hoi-thao", "/dao-tao-san-pham", "/hoi-nghi-khoa-hoc"})
    public String trangDanhSachSuKien() {
        return "events/list";
    }

    /**
     * Định tuyến chi tiết chiến dịch sự kiện.
     */
    @GetMapping({"/events/{slug}", "/su-kien/{slug}", "/hoi-thao/{slug}"})
    public String trangChiTietSuKien(@PathVariable String slug) {
        return "events/detail";
    }
}