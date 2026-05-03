//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IUserService.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;

import java.time.LocalDate;
import java.util.List;

/**
 * =========================================================================
 * GIAO DIỆN DỊCH VỤ NGƯỜI DÙNG CỐT LÕI (CHUẨN MÙ)
 * =========================================================================
 * Khai báo toàn bộ các chức năng liên quan đến vòng đời tài khoản.
 * Các Controller chỉ được phép gọi thông qua bản hợp đồng này.
 */
public interface IUserService {

    User findByEmail(String email);

    User findByUsernameOrEmail(String loginInput);

    void registerLocalUser(String fullName, String username, String email, String rawPassword, String phone, LocalDate birthDate, String address);

    void saveGoogleUser(String email, String name);

    void sendOtp(String email, String purpose);

    boolean verifyOtp(String email, String code);

    void updatePassword(String email, String newPassword);

    /**
     * Cập nhật danh sách chức vụ cho một tài khoản (Hỗ trợ Multi-Role).
     */
    void updateUserRoles(Long targetUserId, List<String> roleNames, User currentUser);

    /**
     * Khóa hoặc mở khóa tài khoản, có đối soát cấp bậc (Level) chống lạm quyền.
     */
    void lockUnlockUser(Long targetUserId, boolean lock, User currentUser);

    List<User> getAllUsers();

    User getCurrentAuthenticatedUser();

    User findById(Long id);
    
    long countTotalUsers();
    
    long countLockedUsers();

    /**
     * =====================================================================
     * HÀM MỞ KHÓA BẢO MẬT: NẠP QUYỀN ĐỘNG VÀO RAM
     * =====================================================================
     * Cho phép các Service khác (như RolesService) nhờ bơm quyền vào
     * thực thể User sau khi query từ Database, đảm bảo chuẩn "Ai làm việc nấy".
     */
    void napQuyenChoNguoiDung(User user);

    /**
     * Tính toán và trả về cấp bậc quyền lực mạnh nhất của người dùng.
     * Số càng nhỏ = quyền càng to (SUPERADMIN = 0, ADMIN = 1...).
     * Dùng để Frontend biết adminLevel hiện tại mà bôi xám các Checkbox vượt cấp.
     * @param user Tài khoản đã được napQuyenChoNguoiDung()
     * @return Số roleLevel nhỏ nhất (= cấp bậc mạnh nhất)
     */
    int layCapBacQuyenLucCaoNhat(User user);
}