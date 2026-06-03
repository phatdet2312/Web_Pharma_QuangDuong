//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/AdminViewController.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * =========================================================================
 * VIEW CONTROLLER: ĐIỀU HƯỚNG GIAO DIỆN QUẢN TRỊ (CHUẨN MÙ)
 * =========================================================================
 * Tuyệt đối không chứa logic gọi CSDL ở đây. Mọi dữ liệu do JS gọi API.
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    @RequirePermission("USER_VIEW")
    public String manageUsers(Model model) {
        model.addAttribute("title", "Quản lý Người dùng");
        return "admin/users"; 
    }

    @GetMapping("/users/phan-quyen/{id}")
    @RequirePermission("USER_VIEW")
    public String userDetail(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Hồ sơ Phân quyền");
        // Js sẽ tự đọc ID từ URL để gọi API lấy chi tiết User
        return "admin/user-details"; 
    }

    // =====================================================================
    // ĐƯỜNG DẪN MỚI CHO TÍNH NĂNG TẠO ROLE ĐỘNG
    // =====================================================================
    @GetMapping("/role-management")
    @RequirePermission("RBAC_ROLE_VIEW")
    public String roleManagement(Model model) {
        model.addAttribute("title", "Quản trị Chức vụ (Roles)");
        return "admin/role-management";
    }

    // =====================================================================
    // MODULE 3 — CONTENT MARKETING ENGINE (Điều 3: chỉ return tên HTML)
    // =====================================================================

    /** Trang quản trị bài viết y khoa */
    @GetMapping("/posts")
    @RequirePermission("POST_VIEW")
    public String quanTriBaiViet() {
        return "admin/posts";
    }

    /** Trang quản trị sự kiện & chiến dịch */
    @GetMapping("/events")
    @RequirePermission("EVENT_VIEW")
    public String quanTriSuKien() {
        return "admin/events";
    }

    /** Trang quản trị bình luận */
    @GetMapping("/comments")
    @RequirePermission("COMMENT_VIEW")
    public String quanTriBinhLuan() {
        return "admin/comments";
    }

    /* 
    @GetMapping("/test")
    public String test() {
        return "admin/test";
    }
    */
}
