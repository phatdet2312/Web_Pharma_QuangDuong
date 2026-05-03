//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/init/DatabaseSeeder.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.init;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.CtUserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.ModerationAction;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.Permission;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.UserRole;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.ICtUserRoleRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IModerationActionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IPermissionRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRepository;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.IUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * DATABASE SEEDER — KIẾN TRÚC MÔ HÌNH 6 BẢNG (NÂNG CẤP HOÀN CHỈNH)
 * Chịu trách nhiệm khởi tạo dữ liệu nền tảng khi Server chạy.
 *
 * Cấu trúc mảng 4 phần tử: {CODE, NAME, DESCRIPTION, AFFECTED_TABLE}
 * AFFECTED_TABLE cho phép Activity Feed hiển thị ngữ cảnh "bảng nào bị tác động"
 * mà không cần hardcode trong business logic.
 *
 * CẬP NHẬT: Đã bổ sung bộ mã Hành vi Hệ thống liên quan đến việc Đăng tải 
 * và Báo cáo vi phạm nội dung Y khoa để cung cấp ID chuẩn mực cho Sổ tay Kiểm toán.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DatabaseSeeder.class.getName());

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final ICtUserRoleRepository ctUserRoleRepository;
    private final IModerationActionRepository moderationActionRepository;
    private final IPermissionRepository permissionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${pharma.admin.default.email}")
    private String adminEmail;

    @Value("${pharma.admin.default.username}")
    private String adminUsername;

    @Value("${pharma.admin.default.password}")
    private String adminPassword;

    @Value("${pharma.admin.default.fullname}")
    private String adminFullName;

    @Value("${pharma.admin.default.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) throws Exception {
        log.info("[DatabaseSeeder] Đang kiểm tra dữ liệu khởi tạo hệ thống...");

        // =====================================================================
        // PHẦN 1: KHỞI TẠO DANH MỤC HÀNH VI (MODERATION_ACTIONS)
        // BẮT BUỘC phải có trước khi UserTrackingService ghi bất kỳ log nào.
        // =====================================================================
        khoiTaoModerationActions();

        // =====================================================================
        // PHẦN 2: KHỞI TẠO QUYỀN NỀN TẢNG VIEW (PERMISSIONS)
        // VIEW là khóa ngoại ổn định cho ASSIGN_ROLE / REVOKE_ROLE trong
        // CT_USER_MODERATION_LOG — không phụ thuộc vào tên Role động.
        // =====================================================================
        khoiTaoViewPermission();

        // =====================================================================
        // PHẦN 3: KHỞI TẠO TÀI KHOẢN SUPER ADMIN MẶC ĐỊNH
        // =====================================================================
        if (userRepository.findByEmail(adminEmail).isEmpty() == true) {

            if (userRepository.existsByUsername(adminUsername) == true) {
                log.warning("[DatabaseSeeder] Username '" + adminUsername + "' đã tồn tại nhưng khác email. Bỏ qua.");
                return;
            }

            log.info("[DatabaseSeeder] Không tìm thấy tài khoản quản trị gốc. Đang tiến hành tạo mới...");

            User superAdmin = new User();
            superAdmin.setEmail(adminEmail);
            superAdmin.setUsername(adminUsername);
            superAdmin.setPassword(passwordEncoder.encode(adminPassword));
            superAdmin.setFullName(adminFullName);
            superAdmin.setPhone(adminPhone);
            superAdmin.setProvider("LOCAL");
            superAdmin.setLocked(false);

            superAdmin = userRepository.save(superAdmin);

            UserRole superRole = userRoleRepository.findByRoleName("SUPERADMIN").orElse(null);
            if (superRole == null) {
                superRole = new UserRole();
                superRole.setRoleName("SUPERADMIN");
                superRole.setRoleLevel(0);
                superRole.setDescription("Quản trị viên tối cao (Hệ thống tự khởi tạo)");
                superRole = userRoleRepository.save(superRole);
            }

            CtUserRole ctUserRole = new CtUserRole(superAdmin.getId(), superRole.getId());
            ctUserRoleRepository.save(ctUserRole);

            log.info("[DatabaseSeeder] ĐÃ TẠO THÀNH CÔNG tài khoản Super Admin: " + adminEmail);
        } else {
            log.info("[DatabaseSeeder] Tài khoản quản trị gốc đã tồn tại. Bỏ qua bước khởi tạo.");
        }
    }

    /**
     * Seed 18 hành vi lõi vào bảng MODERATION_ACTIONS.
     * Idempotent: chỉ INSERT nếu CODE chưa tồn tại — chạy lại nhiều lần không bị lỗi.
     *
     * Cấu trúc: {CODE, NAME, DESCRIPTION, AFFECTED_TABLE}
     * AFFECTED_TABLE giúp Activity Feed hiển thị đúng ngữ cảnh bảng bị tác động.
     */
    private void khoiTaoModerationActions() {
        String[][] danhSachHanhVi = {

            // ─── Nhóm 1: Hành vi Quản trị viên (CT_USER_MODERATION_LOG) ───
            {"LOCK_USER",          "Khóa tài khoản",            "Đình chỉ toàn bộ quyền truy cập của tài khoản",                   "USERS"},
            {"UNLOCK_USER",        "Mở khóa tài khoản",         "Khôi phục quyền truy cập sau khi xem xét",                        "USERS"},
            {"BLACKLIST_PERM",     "Tước quyền hạt lựu",        "Đóng băng một quyền thao tác cụ thể cấp cá nhân",                 "CT_USER_PERMISSION_BLACKLIST"},
            {"UNBLACKLIST_PERM",   "Khôi phục quyền hạt lựu",   "Gỡ lệnh đóng băng, trả lại quyền thao tác cụ thể",                "CT_USER_PERMISSION_BLACKLIST"},
            {"ASSIGN_ROLE",        "Gán chức vụ",               "Ghi nhận việc phân công chức vụ mới cho tài khoản",               "CT_USER_ROLES"},
            {"REVOKE_ROLE",        "Thu hồi chức vụ",           "Ghi nhận việc thu hồi chức vụ khỏi tài khoản",                    "CT_USER_ROLES"},

            // ─── Nhóm 2: Hành vi Đăng nhập / Xác thực (CT_USER_LOGIN_LOG) ───
            {"LOGIN_SUCCESS",      "Đăng nhập thành công",      "Ghi nhận phiên đăng nhập hợp lệ",                                 "CT_USER_LOGIN_LOG"},
            {"LOGIN_FAILED",       "Đăng nhập thất bại",        "Ghi nhận nỗ lực đăng nhập sai thông tin",                         "CT_USER_LOGIN_LOG"},
            {"LOGOUT",             "Đăng xuất",                 "Ghi nhận thao tác thoát khỏi hệ thống",                           "CT_USER_LOGIN_LOG"},

            // ─── Nhóm 3: Hành vi Tự phục vụ của Đối tác B2B (CT_USER_ACTION_LOG) ───
            {"UPDATE_PROFILE",     "Cập nhật thông tin cá nhân","Chỉnh sửa họ tên, số điện thoại, địa chỉ, ngày sinh",             "USERS"},
            {"UPDATE_BUSINESS",    "Cập nhật hồ sơ doanh nghiệp","Chỉnh sửa tên, SĐT, mã số thuế, số giấy phép",                   "PARTNER_PROFILES"},
            {"UPLOAD_AVATAR",      "Upload ảnh đại diện",       "Tải lên ảnh đại diện/logo doanh nghiệp mới",                      "PARTNER_PROFILES"},
            {"UPLOAD_LICENSE",     "Upload giấy phép",          "Tải lên giấy phép kinh doanh để xác minh pháp lý",                "PARTNER_PROFILES"},
            {"CHANGE_PASSWORD",    "Đổi mật khẩu",              "Cập nhật khóa bảo mật đăng nhập",                                 "USERS"},
            {"ADD_ADDRESS",        "Thêm địa chỉ mới",          "Đăng ký tọa độ nhận hàng/chi nhánh mới",                          "ADDRESSES"},
            {"UPDATE_ADDRESS",     "Cập nhật địa chỉ",          "Thay đổi thông tin địa chỉ đã lưu",                               "ADDRESSES"},
            {"DELETE_ADDRESS",     "Xóa địa chỉ",               "Gỡ bỏ tọa độ kho/chi nhánh khỏi danh bạ",                         "ADDRESSES"},
            {"SET_DEFAULT_ADDRESS","Đặt địa chỉ mặc định",      "Chỉ định địa chỉ nhận hàng ưu tiên",                              "ADDRESSES"},

            // ─── Nhóm 4: Quyền riêng tư Tác giả (CT_USER_ACTION_LOG) ───
            {"UPDATE_PUBLIC_PROFILE", "Cập nhật hồ sơ công khai", "User thay đổi chức danh, tiểu sử hiển thị",                      "PUBLIC_PROFILES"},

            // ─── Nhóm 5: Hành vi Tương tác và Báo cáo Nội dung Y khoa (CT_CMT_ACTION_LOG/CT_PH_CMT_ACTION_LOG) ───
            {"CREATE_CMT",         "Đăng bình luận",            "Khởi tạo bình luận gốc cấp 1 trên hệ thống",                       "CMT"},
            {"UPDATE_CMT",         "Sửa bình luận",             "Chỉnh sửa nội dung văn bản bình luận gốc",                         "CMT"},
            {"DELETE_CMT",         "Xóa bình luận",             "Gỡ bỏ bình luận gốc khỏi cơ sở dữ liệu",                           "CMT"},
            {"CREATE_PH_CMT",      "Gửi phản hồi",              "Khởi tạo phản hồi cấp 2 trở lên",                                  "PH_CMT"},
            {"UPDATE_PH_CMT",      "Sửa phản hồi",              "Chỉnh sửa nội dung văn bản phản hồi",                              "PH_CMT"},
            {"DELETE_PH_CMT",      "Xóa phản hồi",              "Gỡ bỏ phản hồi khỏi cơ sở dữ liệu",                                "PH_CMT"},
            
            // ─── Nhóm 6: Hệ sinh thái Phán quyết Báo cáo  (CT_CMT_REPORT_MOD_LOG/CT_CMT_REPORT_MOD_LOG) ───
            //{"REPORT_CMT",         "Báo cáo bình luận",         "Gửi cảnh báo vi phạm tiêu chuẩn đối với bình luận gốc",            "CT_CMT_REPORTS"}, //--KHÔNG THỂ GHI VÀO LOG
            //{"REPORT_PH_CMT",      "Báo cáo phản hồi",          "Gửi cảnh báo vi phạm tiêu chuẩn đối với phản hồi thứ cấp",         "CT_PH_CMT_REPORTS"}, //--KHÔNG THỂ GHI VÀO LOG
            {"RESOLVE_REPORT",     "Xử lý báo cáo",             "Xác nhận đơn báo cáo vi phạm đã được giải quyết",                 "CT_CMT_REPORTS"},
            {"REJECT_REPORT",      "Bác bỏ báo cáo",            "Xác định báo cáo là sai sự thật và gỡ bỏ trạng thái cảnh báo",    "CT_CMT_REPORTS"},

            // ─── Nhóm 7: Hành vi Kiểm duyệt Nội dung của Quản trị viên (CT_CMT_MODERATION_LOG/CT_PH_CMT_MODERATION_LOG) ───
            {"APPROVE",            "Duyệt hiển thị",            "Xác nhận nội dung an toàn và cho phép hiển thị công khai",        "CMT"},
            {"HIDE",               "Ẩn nội dung",               "Tạm thời gỡ nội dung khỏi giao diện người dùng để xem xét",       "CMT"},
            {"UNHIDE",             "Bỏ ẩn nội dung",            "Khôi phục trạng thái hiển thị của nội dung sau khi đối soát",     "CMT"},
            {"WARN",               "Cảnh báo",                  "Gửi thông điệp nhắc nhở đến tài khoản vi phạm",                   "USERS"},
            {"DELETE",             "Xóa vĩnh viễn",             "Tiêu hủy hoàn toàn nội dung vi phạm khỏi cơ sở dữ liệu",          "CMT"},
        };

        for (int i = 0; i < danhSachHanhVi.length; i = i + 1) {
            String[] hangMuc = danhSachHanhVi[i];
            String code          = hangMuc[0];
            String name          = hangMuc[1];
            String description   = hangMuc[2];
            String affectedTable = hangMuc[3];

            boolean daCoSan = moderationActionRepository.findByCode(code).isPresent();
            if (daCoSan == false) {
                ModerationAction action = new ModerationAction();
                action.setCode(code);
                action.setName(name);
                action.setDescription(description);
                action.setAffectedTable(affectedTable);
                moderationActionRepository.save(action);
                log.info("[DatabaseSeeder] Đã khởi tạo hành vi: " + code + " → " + affectedTable);
            }
        }
    }

    /**
     * Seed quyền VIEW nền tảng vào bảng PERMISSIONS.
     * VIEW là khóa ngoại ổn định được dùng khi ghi CT_USER_MODERATION_LOG cho
     * sự kiện ASSIGN_ROLE / REVOKE_ROLE — không phụ thuộc tên Role động.
     */
    private void khoiTaoViewPermission() {
        boolean daCoSan = permissionRepository.findByPermissionCode("VIEW").isPresent();
        if (daCoSan == false) {
            Permission viewPerm = new Permission();
            viewPerm.setPermissionCode("VIEW");
            viewPerm.setDescription("Quyền truy cập giao diện chức năng được phân công");
            permissionRepository.save(viewPerm);
            log.info("[DatabaseSeeder] Đã khởi tạo quyền nền tảng: VIEW");
        }
    }
}