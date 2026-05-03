//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/PostViewController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * =========================================================================
 * TỔNG QUAN NHIỆM VỤ FILE: ĐIỀU HƯỚNG GIAO DIỆN BÀI VIẾT (VIEW CONTROLLER)
 * =========================================================================
 * Chỉ đảm nhiệm duy nhất một việc: Nhận URL từ người dùng và trả về đúng tệp HTML.
 * KHÔNG chứa logic nghiệp vụ, KHÔNG chọc xuống CSDL.
 * Thiết kế chuẩn SEO: Hỗ trợ nhiều định dạng URL tiếng Việt không dấu thân thiện.
 */
@Controller
public class PostViewController {

    /**
     * Định tuyến danh sách bài viết.
     * Hỗ trợ đồng thời nhiều đường dẫn để tối ưu trải nghiệm và SEO.
     */
    @GetMapping({"/posts", "/tin-tuc", "/tin-tuc-y-khoa", "/nghien-cuu", "/phac-do-dieu-tri"})
    public String trangDanhSachBaiViet() {
        return "posts/list";
    }

    /**
     * Định tuyến chi tiết bài viết dựa trên đường dẫn tĩnh (Slug).
     * Bắt mọi yêu cầu có dạng /tin-tuc/ten-bai-viet hoặc /tin-tuc-y-khoa/ten-bai-viet.
     */
    @GetMapping({"/posts/{slug}", "/tin-tuc/{slug}", "/tin-tuc-y-khoa/{slug}"})
    public String trangChiTietBaiViet(@PathVariable String slug) {
        // Tầng View Controller mù mờ: Nó không cần biết slug này có tồn tại trong CSDL hay không.
        // Việc kiểm tra tính hợp lệ của slug sẽ do Javascript gọi API ở client-side quyết định.
        return "posts/detail";
    }
}