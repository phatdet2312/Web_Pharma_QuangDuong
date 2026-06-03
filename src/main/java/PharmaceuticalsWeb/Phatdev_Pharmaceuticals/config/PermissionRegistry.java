//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/PermissionRegistry.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config;

import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================================
 * SỔ ĐĂNG KÝ QUYỀN HỆ THỐNG — NGUỒN SỰ THẬT DUY NHẤT
 * =========================================================================
 * Khai báo toàn bộ mã quyền mà backend đang sử dụng (@RequirePermission).
 * API đọc từ đây để cung cấp dropdown cho giao diện admin.
 *
 * Khi lập trình viên thêm @RequirePermission mới trên endpoint,
 * bổ sung vào danh sách bên dưới → frontend tự hiện thêm trong dropdown.
 */
public class PermissionRegistry {

    /**
     * Lấy danh sách tất cả quyền hệ thống đang sử dụng.
     * Mỗi phần tử gồm 3 giá trị: {MÃ_QUYỀN, MÔ_TẢ_NGHIỆP_VỤ, MÃ_MODULE}
     */
    public static List<String[]> layDanhSachQuyenHeThong() {
        List<String[]> danhSach = new ArrayList<>();

        // ─── Nhóm: Bài viết ───
        danhSach.add(new String[]{"POST_VIEW",              "Xem danh sách và chi tiết bài viết",                   "POST"});
        danhSach.add(new String[]{"POST_CREATE",            "Tạo bài viết mới",                                     "POST"});
        danhSach.add(new String[]{"POST_EDIT",              "Chỉnh sửa, xuất bản, nổi bật, upload ảnh bài viết",   "POST"});
        danhSach.add(new String[]{"POST_DELETE",            "Xóa bài viết",                                         "POST"});
        danhSach.add(new String[]{"POST_MANAGE_CATEGORY",   "Quản lý danh mục bài viết",                            "POST"});
        danhSach.add(new String[]{"POST_MANAGE_TAG",        "Quản lý thẻ từ khóa bài viết",                         "POST"});

        // ─── Nhóm: Sự kiện ───
        danhSach.add(new String[]{"EVENT_VIEW",             "Xem danh sách và chi tiết sự kiện",                    "EVENT"});
        danhSach.add(new String[]{"EVENT_CREATE",           "Tạo chiến dịch sự kiện mới",                           "EVENT"});
        danhSach.add(new String[]{"EVENT_EDIT",             "Chỉnh sửa chiến dịch, phiên, đăng ký, diễn giả",      "EVENT"});
        danhSach.add(new String[]{"EVENT_DELETE",           "Xóa chiến dịch sự kiện",                               "EVENT"});
        danhSach.add(new String[]{"EVENT_MANAGE_TYPE",      "Quản lý loại sự kiện",                                 "EVENT"});
        danhSach.add(new String[]{"EVENT_MANAGE_LOCATION",  "Quản lý địa điểm tổ chức",                             "EVENT"});

        // ─── Nhóm: Bình luận ───
        danhSach.add(new String[]{"COMMENT_VIEW",           "Xem danh sách và lịch sử bình luận",                   "COMMENT"});
        danhSach.add(new String[]{"COMMENT_MODERATE",       "Duyệt, ẩn, cảnh báo bình luận",                        "COMMENT"});
        danhSach.add(new String[]{"COMMENT_DELETE",         "Xóa bình luận và phản hồi",                            "COMMENT"});
        danhSach.add(new String[]{"COMMENT_MANAGE_REACTION","Quản lý loại biểu cảm (reaction)",                     "COMMENT"});

        // ─── Nhóm: Sự kiện (bổ sung) ───
        danhSach.add(new String[]{"EVENT_MANAGE_SPEAKER",   "Quản lý diễn giả sự kiện",                             "EVENT"});
        danhSach.add(new String[]{"EVENT_MANAGE_AGENDA",    "Quản lý chương trình sự kiện",                          "EVENT"});

        // ─── Nhóm: Người dùng ───
        danhSach.add(new String[]{"USER_VIEW",              "Xem danh sách và chi tiết người dùng",                  "USER"});
        danhSach.add(new String[]{"USER_ASSIGN_ROLE",       "Gán hoặc thu hồi chức vụ cho người dùng",              "USER"});
        danhSach.add(new String[]{"USER_LOCK",              "Khóa hoặc mở khóa tài khoản người dùng",               "USER"});

        // ─── Nhóm: Báo cáo vi phạm ───
        danhSach.add(new String[]{"REPORT_VIEW",            "Xem danh sách báo cáo vi phạm nội dung",               "REPORT"});
        danhSach.add(new String[]{"REPORT_RESOLVE",         "Xử lý hoặc bác bỏ báo cáo vi phạm",                   "REPORT"});

        // ─── Nhóm: Quyền người dùng (có thể bị tước khi vi phạm) ───
        danhSach.add(new String[]{"USER_COMMENT",           "Gửi và chỉnh sửa bình luận, phản hồi",                 "USER_ACTION"});
        danhSach.add(new String[]{"USER_REACT",             "Thả biểu cảm (like/reaction) trên bài viết và bình luận","USER_ACTION"});
        danhSach.add(new String[]{"USER_REPORT",            "Gửi báo cáo vi phạm nội dung",                          "USER_ACTION"});
        danhSach.add(new String[]{"USER_REGISTER",          "Đăng ký tham dự sự kiện",                                "USER_ACTION"});

        // ─── Nhóm: Hệ thống ───
        danhSach.add(new String[]{"VIEW",                   "Quyền truy cập giao diện chức năng được phân công",    "SYSTEM"});
        danhSach.add(new String[]{"RBAC_ROLE_VIEW",         "Xem danh sách chức vụ và quyền đang gán",               "SYSTEM"});
        danhSach.add(new String[]{"RBAC_ROLE_CREATE",       "Tạo chức vụ mới",                                       "SYSTEM"});
        danhSach.add(new String[]{"RBAC_ROLE_UPDATE",       "Cập nhật chức vụ và danh sách quyền của chức vụ",       "SYSTEM"});
        danhSach.add(new String[]{"RBAC_ROLE_DELETE",       "Xóa chức vụ khi không còn người dùng đang gán",         "SYSTEM"});
        danhSach.add(new String[]{"RBAC_ROLE_CLONE",        "Nhân bản chức vụ trong phạm vi quyền được cấp",         "SYSTEM"});

        danhSach.add(new String[]{"RBAC_PERMISSION_VIEW",   "Xem từ điển quyền thao tác và danh sách mã quyền hệ thống", "SYSTEM"});
        danhSach.add(new String[]{"RBAC_PERMISSION_CREATE", "Tạo quyền thao tác mới",                                "SYSTEM"});
        danhSach.add(new String[]{"RBAC_PERMISSION_UPDATE", "Cập nhật mô tả hoặc nhóm chức năng của quyền thao tác",  "SYSTEM"});
        danhSach.add(new String[]{"RBAC_PERMISSION_DELETE", "Xóa quyền thao tác khi chưa được gán vào chức vụ",       "SYSTEM"});

        danhSach.add(new String[]{"RBAC_MODULE_VIEW",       "Xem danh sách nhóm chức năng phân quyền",               "SYSTEM"});
        danhSach.add(new String[]{"RBAC_MODULE_CREATE",     "Tạo nhóm chức năng phân quyền",                         "SYSTEM"});
        danhSach.add(new String[]{"RBAC_MODULE_UPDATE",     "Cập nhật nhóm chức năng phân quyền",                    "SYSTEM"});
        danhSach.add(new String[]{"RBAC_MODULE_DELETE",     "Xóa nhóm chức năng khi chưa có quyền thuộc nhóm",        "SYSTEM"});

        danhSach.add(new String[]{"RBAC_BLACKLIST_VIEW",    "Xem danh sách quyền bị khóa riêng theo người dùng",      "SYSTEM"});
        danhSach.add(new String[]{"RBAC_BLACKLIST_TOGGLE",  "Khóa hoặc mở khóa quyền thao tác riêng theo người dùng", "SYSTEM"});
        danhSach.add(new String[]{"AUDIT_VIEW",             "Xem nhật ký kiểm toán hệ thống",                        "SYSTEM"});

        return danhSach;
    }
}
