//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiProfileController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.AddressRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ChangePasswordRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePartnerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePersonalRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAddressService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * =========================================================================
 * API CONTROLLER: CỔNG QUẢN LÝ HỒ SƠ CÁ NHÂN & DOANH NGHIỆP
 * =========================================================================
 * Prefix: /api/profile/**
 * Toàn bộ endpoint yêu cầu xác thực — SecurityConfig đã bảo vệ /api/** bởi JWT.
 * Controller chỉ điều phối — không chứa bất kỳ logic nghiệp vụ nào.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ApiProfileController {

    private final IProfileService profileService;
    private final IAddressService addressService;

    // =========================================================================
    // NHÓM 1: THÔNG TIN CÁ NHÂN
    // =========================================================================

    /** Lấy thông tin cá nhân của tài khoản đang đăng nhập */
    @GetMapping("/me")
    public ApiResponse<ProfileMeResponse> layThongTinCaNhan() {
        ProfileMeResponse result = profileService.layThongTinCaNhan();
        return ApiResponse.thanhCong(result, "Truy xuất thông tin cá nhân thành công");
    }

    /** Cập nhật thông tin cá nhân (fullName, phone, address, birthDate) */
    @PutMapping("/me")
    public ApiResponse<ProfileMeResponse> capNhatThongTinCaNhan(@Valid @RequestBody UpdatePersonalRequest request) {
        ProfileMeResponse result = profileService.capNhatThongTinCaNhan(request);
        return ApiResponse.thanhCong(result, "Cập nhật thông tin cá nhân thành công");
    }

    // =========================================================================
    // NHÓM 2: HỒ SƠ DOANH NGHIỆP
    // =========================================================================

    /** Lấy hồ sơ doanh nghiệp đối tác — daCoHoSo = false nếu chưa tạo */
    @GetMapping("/partner")
    public ApiResponse<PartnerProfileResponse> layHoSoDoanhNghiep() {
        PartnerProfileResponse result = profileService.layHoSoDoanhNghiep();
        return ApiResponse.thanhCong(result, "Truy xuất hồ sơ doanh nghiệp thành công");
    }

    /** Tạo mới hoặc cập nhật hồ sơ doanh nghiệp — Service tự phân biệt INSERT vs UPDATE */
    @PutMapping("/partner")
    public ApiResponse<PartnerProfileResponse> capNhatHoSoDoanhNghiep(@Valid @RequestBody UpdatePartnerRequest request) {
        PartnerProfileResponse result = profileService.capNhatHoSoDoanhNghiep(request);
        return ApiResponse.thanhCong(result, "Lưu hồ sơ doanh nghiệp thành công");
    }

    // =========================================================================
    // NHÓM 3: UPLOAD FILE
    // =========================================================================

    /** Upload ảnh đại diện doanh nghiệp — tối đa 5MB, định dạng JPG/PNG/GIF/WEBP */
    @PostMapping("/partner/avatar")
    public ApiResponse<String> uploadAnhDaiDien(@RequestParam("file") MultipartFile file) {
        String url = profileService.uploadAnhDaiDien(file);
        return ApiResponse.thanhCong(url, "Upload ảnh đại diện thành công");
    }

    /** Upload giấy phép kinh doanh — tối đa 20MB, định dạng PDF/JPG/PNG */
    @PostMapping("/partner/license")
    public ApiResponse<String> uploadGiayPhep(@RequestParam("file") MultipartFile file) {
        String url = profileService.uploadGiayPhep(file);
        return ApiResponse.thanhCong(url, "Upload giấy phép kinh doanh thành công");
    }

    // =========================================================================
    // NHÓM 4: BẢO MẬT & LỊCH SỬ
    // =========================================================================

    /** Đổi mật khẩu — bắt buộc xác minh oldPassword trước khi cập nhật */
    @PutMapping("/security/password")
    public ApiResponse<Void> doiMatKhau(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.doiMatKhau(request);
        return ApiResponse.thanhCong(null, "Đổi mật khẩu thành công");
    }

    /**
     * Lấy lịch sử OTP phân trang — không trả về mã OTP thực.
     * @param pageNo   Trang bắt đầu từ 0 (mặc định 0)
     * @param pageSize Số bản ghi mỗi trang (mặc định 10)
     */
    @GetMapping("/security/otp-history")
    public ApiResponse<OtpHistoryPageResponse> layLichSuOtp(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        OtpHistoryPageResponse result = profileService.layLichSuOtp(pageNo, pageSize);
        return ApiResponse.thanhCong(result, "Truy xuất lịch sử OTP thành công");
    }

    /**
     * Lấy lịch sử đăng nhập / đăng xuất phân trang.
     * Nguồn: CT_USER_LOGIN_LOG — hiển thị IP, thiết bị, kết quả từng phiên.
     */
    @GetMapping("/security/login-history")
    public ApiResponse<LoginHistoryPageResponse> layLichSuDangNhap(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        LoginHistoryPageResponse result = profileService.layLichSuDangNhap(pageNo, pageSize);
        return ApiResponse.thanhCong(result, "Truy xuất lịch sử đăng nhập thành công");
    }

    /**
     * Lấy lịch sử hành động admin lên tài khoản phân trang.
     * Nguồn: CT_USER_MODERATION_LOG — bao gồm Risk Indicator (tongSoLanBiKhoa).
     */
    @GetMapping("/account-history")
    public ApiResponse<AccountHistoryPageResponse> layLichSuTaiKhoan(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        AccountHistoryPageResponse result = profileService.layLichSuTaiKhoan(pageNo, pageSize);
        return ApiResponse.thanhCong(result, "Truy xuất lịch sử tài khoản thành công");
    }

    /**
     * Lấy bản đồ phân quyền chi tiết — vai trò, danh sách permission, trạng thái blacklist,
     * lý do + người blacklist từ CT_USER_MODERATION_LOG.
     */
    @GetMapping("/permissions")
    public ApiResponse<UserPermissionResponse> layQuyenHanChiTiet() {
        UserPermissionResponse result = profileService.layQuyenHanChiTiet();
        return ApiResponse.thanhCong(result, "Truy xuất quyền hạn thành công");
    }

    /** Lấy chỉ số thống kê hồ sơ — số địa chỉ thật, module khác trả về 0 */
    @GetMapping("/stats")
    public ApiResponse<ProfileStatsResponse> layThongKe() {
        ProfileStatsResponse result = profileService.layThongKe();
        List<AddressDetailResponse> dsDiaChi = addressService.layDanhSachDiaChi();
        result.setAddressCount(dsDiaChi.size());
        return ApiResponse.thanhCong(result, "Truy xuất thống kê hồ sơ thành công");
    }

    // =========================================================================
    // NHÓM 5: QUẢN LÝ ĐỊA CHỈ
    // =========================================================================

    /** Lấy danh sách tất cả tỉnh/thành phố — phục vụ dropdown */
    @GetMapping("/address/provinces")
    public ApiResponse<List<ProvinceResponse>> layDanhSachTinh() {
        List<ProvinceResponse> result = addressService.layDanhSachTinh();
        return ApiResponse.thanhCong(result, "Truy xuất danh sách tỉnh/thành phố thành công");
    }

    /** Lấy danh sách quận/huyện theo tỉnh — phục vụ dropdown phụ thuộc */
    @GetMapping("/address/districts")
    public ApiResponse<List<DistrictResponse>> layDanhSachQuan(@RequestParam Integer provinceId) {
        List<DistrictResponse> result = addressService.layDanhSachQuan(provinceId);
        return ApiResponse.thanhCong(result, "Truy xuất danh sách quận/huyện thành công");
    }

    /** Lấy danh sách phường/xã theo quận — phục vụ dropdown phụ thuộc */
    @GetMapping("/address/wards")
    public ApiResponse<List<WardResponse>> layDanhSachPhuong(@RequestParam Integer districtId) {
        List<WardResponse> result = addressService.layDanhSachPhuong(districtId);
        return ApiResponse.thanhCong(result, "Truy xuất danh sách phường/xã thành công");
    }

    /** Lấy toàn bộ địa chỉ của tài khoản hiện tại — địa chỉ mặc định đứng đầu */
    @GetMapping("/address")
    public ApiResponse<List<AddressDetailResponse>> layDanhSachDiaChi() {
        List<AddressDetailResponse> result = addressService.layDanhSachDiaChi();
        return ApiResponse.thanhCong(result, "Truy xuất danh sách địa chỉ thành công");
    }

    /** Thêm địa chỉ mới */
    @PostMapping("/address")
    public ApiResponse<AddressDetailResponse> themDiaChi(@Valid @RequestBody AddressRequest request) {
        AddressDetailResponse result = addressService.themDiaChi(request);
        return ApiResponse.thanhCong(result, "Thêm địa chỉ thành công");
    }

    /** Cập nhật địa chỉ theo ID — có kiểm tra IDOR */
    @PutMapping("/address/{id}")
    public ApiResponse<AddressDetailResponse> suaDiaChi(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressDetailResponse result = addressService.suaDiaChi(id, request);
        return ApiResponse.thanhCong(result, "Cập nhật địa chỉ thành công");
    }

    /** Xóa địa chỉ theo ID — có kiểm tra IDOR */
    @DeleteMapping("/address/{id}")
    public ApiResponse<Void> xoaDiaChi(@PathVariable Long id) {
        addressService.xoaDiaChi(id);
        return ApiResponse.thanhCong(null, "Xóa địa chỉ thành công");
    }

    /** Đặt địa chỉ làm mặc định — reset các địa chỉ khác — có kiểm tra IDOR */
    @PatchMapping("/address/{id}/default")
    public ApiResponse<Void> datDiaChiMacDinh(@PathVariable Long id) {
        addressService.datDiaChiMacDinh(id);
        return ApiResponse.thanhCong(null, "Đặt địa chỉ mặc định thành công");
    }
}
