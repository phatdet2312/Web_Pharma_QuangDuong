//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IUserTrackingService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

/**
 * =========================================================================
 * GIAO DIỆN: DỊCH VỤ GHI DẤU VẾT HOẠT ĐỘNG (CHUẨN MÙ)
 * =========================================================================
 * Học từ democach2: tách riêng việc GHI log thành service độc lập.
 * Tất cả nghiệp vụ write (Profile, Address) gọi service này để ghi audit trail.
 * Auto-extract IP và User-Agent từ request context — caller không cần truyền.
 */
public interface IUserTrackingService {

    /**
     * Ghi nhận một phiên đăng nhập vào hệ thống.
     * @param userId           ID user (null nếu username không tồn tại)
     * @param ipAddress        IP thực (null → tự extract từ request)
     * @param userAgent        UA string (null → tự extract từ request)
     * @param isSuccess        true = LOGIN_SUCCESS, false = LOGIN_FAILED
     * @param usernameAttempt  Chuỗi đã gõ vào ô đăng nhập
     * @param reason           Lý do thất bại (null nếu thành công)
     */
    void ghiDangNhap(Long userId, String ipAddress, String userAgent,
                     boolean isSuccess, String usernameAttempt, String reason);

    /**
     * Ghi nhận thao tác đăng xuất.
     * @param userId    ID user đang đăng xuất
     * @param ipAddress IP thực (null → tự extract)
     * @param userAgent UA string (null → tự extract)
     */
    void ghiDangXuat(Long userId, String ipAddress, String userAgent);

    /**
     * Ghi nhận các thay đổi tự phục vụ vào CT_USER_ACTION_LOG.
     * IP và User-Agent được tự động lấy từ request context hiện tại.
     *
     * @param userId         Ai thực hiện
     * @param actionCode     Mã hành vi (VD: "UPDATE_PROFILE", "ADD_ADDRESS")
     * @param targetEntityId ID dòng dữ liệu bị tác động
     * @param oldPayload     JSON trạng thái CŨ (null nếu là INSERT)
     * @param newPayload     JSON trạng thái MỚI (null nếu là DELETE)
     */
    void ghiHanhVi(Long userId, String actionCode, Long targetEntityId,
                   String oldPayload, String newPayload);
}
