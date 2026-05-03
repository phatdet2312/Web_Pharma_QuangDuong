//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/itf/IProfileService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ChangePasswordRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PublicProfileRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePartnerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePersonalRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * =========================================================================
 * GIAO DIỆN: DỊCH VỤ QUẢN LÝ HỒ SƠ CÁ NHÂN (CHUẨN MÙ)
 * =========================================================================
 * Toàn bộ nghiệp vụ Profile — từ thông tin cá nhân đến hoạt động bảo mật.
 */
public interface IProfileService {

    /**
     * Lấy toàn bộ thông tin cá nhân của tài khoản đang đăng nhập.
     * @return DTO thông tin cá nhân an toàn (không chứa password)
     */
    ProfileMeResponse layThongTinCaNhan();

    /**
     * Cập nhật thông tin cá nhân (fullName, phone, address, birthDate).
     * @param request Dữ liệu mới đã qua validate tầng Controller
     * @return DTO thông tin cá nhân sau khi cập nhật
     */
    ProfileMeResponse capNhatThongTinCaNhan(UpdatePersonalRequest request);

    /**
     * Lấy hồ sơ doanh nghiệp đối tác của tài khoản hiện tại.
     * Trả về daCoHoSo = false nếu chưa tạo hồ sơ.
     */
    PartnerProfileResponse layHoSoDoanhNghiep();

    /**
     * Tạo mới hoặc cập nhật hồ sơ doanh nghiệp đối tác.
     * Service tự phân biệt INSERT vs UPDATE dựa vào findByUserId().
     */
    PartnerProfileResponse capNhatHoSoDoanhNghiep(UpdatePartnerRequest request);

    /**
     * Upload ảnh đại diện doanh nghiệp.
     * Lưu file vào uploads/partners/avatars/ và cập nhật AVATAR_URL trong PARTNER_PROFILES.
     * @return URL truy cập ảnh vừa upload
     */
    String uploadAnhDaiDien(MultipartFile file);

    /**
     * Upload giấy phép kinh doanh.
     * Lưu file vào uploads/partners/licenses/ và cập nhật LICENSE_DOCUMENT_URL.
     * @return URL truy cập file vừa upload
     */
    String uploadGiayPhep(MultipartFile file);

    /**
     * Đổi mật khẩu tài khoản hiện tại.
     * Bắt buộc xác minh oldPassword bằng BCrypt trước khi cập nhật.
     */
    void doiMatKhau(ChangePasswordRequest request);

    /**
     * Lấy lịch sử OTP phân trang của email tài khoản hiện tại.
     * Mã OTP không được trả về trong response.
     * @param pageNo   Trang bắt đầu từ 0
     * @param pageSize Số bản ghi mỗi trang (mặc định 10)
     */
    OtpHistoryPageResponse layLichSuOtp(int pageNo, int pageSize);

    /**
     * Lấy lịch sử đăng nhập / đăng xuất phân trang.
     * Nguồn: CT_USER_LOGIN_LOG (LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT).
     */
    LoginHistoryPageResponse layLichSuDangNhap(int pageNo, int pageSize);

    /**
     * Lấy lịch sử tác động lên tài khoản phân trang.
     * Nguồn: CT_USER_MODERATION_LOG — các hành động do admin/system thực hiện.
     */
    AccountHistoryPageResponse layLichSuTaiKhoan(int pageNo, int pageSize);

    /**
     * Lấy chi tiết phân quyền: vai trò, danh sách permission, trạng thái blacklist,
     * lý do + người blacklist (JOIN CT_USER_MODERATION_LOG).
     */
    UserPermissionResponse layQuyenHanChiTiet();

    /**
     * Lấy các chỉ số thống kê tóm tắt trên thẻ Profile.
     * Các module chưa xây dựng trả về 0.
     */
    ProfileStatsResponse layThongKe();

    /**
     * =====================================================================
     * NHÓM QUẢN TRỊ QUYỀN RIÊNG TƯ VÀ HIỂN THỊ CÔNG KHAI
     * =====================================================================
     */
    
    /** * Khai thác cấu hình hiển thị công khai của tài khoản hiện tại.
     * Cung cấp dữ liệu để Frontend vẽ Biểu mẫu cài đặt Quyền riêng tư.
     */
    PublicProfileResponse layHoSoCongKhai();

    /** * Lưu trữ thiết lập Quyền riêng tư và thông tin định danh chuyên môn.
     * Đẩy hành vi cập nhật vào Sổ tay Kiểm toán thông qua UserTrackingService.
     */
    PublicProfileResponse capNhatHoSoCongKhai(PublicProfileRequest request);
}
