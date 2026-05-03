//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/ProfileServiceImpl.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.ChangePasswordRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.PublicProfileRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePartnerRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.UpdatePersonalRequest;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IProfileService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * =========================================================================
 * THỰC THI: DỊCH VỤ QUẢN LÝ HỒ SƠ CÁ NHÂN
 * =========================================================================
 * Tuân thủ tuyệt đối 6 Quy tắc: Không Stream/Lambda, vòng lặp For truyền thống.
 * Áp dụng IUserTrackingService để ghi audit trail tự động cho mọi thao tác write.
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final IUserService userService;
    private final IUserTrackingService trackingService;
    private final IUserRepository userRepository;
    private final IPartnerProfileRepository partnerProfileRepository;
    private final IPublicProfileRepository publicProfileRepository;
    private final IPostRepository postRepository;
    private final IOtpCodeRepository otpCodeRepository;
    private final ICtUserModerationLogRepository moderationLogRepository;
    private final IModerationActionRepository actionRepository;
    private final ICtUserPermissionBlacklistRepository blacklistRepository;
    private final IPermissionRepository permissionRepository;
    private final ICtUserRoleRepository ctUserRoleRepository;
    private final IUserRoleRepository userRoleRepository;
    private final ICtRolePermissionRepository ctRolePermissionRepository;
    private final ICtUserLoginLogRepository loginLogRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${pharma.upload.base-path:./uploads}")
    private String uploadBasePath;

    // =========================================================================
    // HỒ SƠ CÔNG KHAI (PUBLIC PROFILE) & QUYỀN RIÊNG TƯ
    // =========================================================================

    @Override
    public PublicProfileResponse layHoSoCongKhai() {
        User user = userService.getCurrentAuthenticatedUser();
        Optional<PublicProfile> optProfile = publicProfileRepository.findByUserId(user.getId());
        
        PublicProfileResponse dto = new PublicProfileResponse();
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        
        // Đếm tổng sản lượng nội dung Y khoa đã được duyệt
        long totalPosts = postRepository.countByAuthorIdAndIsPublishedTrue(user.getId());
        dto.setTotalPublishedPosts(totalPosts);

        if (optProfile.isPresent() == true) {
            PublicProfile p = optProfile.get();
            dto.setId(p.getId());
            dto.setProfessionalTitle(p.getProfessionalTitle());
            dto.setWorkplace(p.getWorkplace());
            dto.setBio(p.getBio());
            dto.setAvatarUrl(p.getAvatarUrl());
            dto.setVisible(p.isVisible());
        } else {
            dto.setProfessionalTitle("");
            dto.setWorkplace("");
            dto.setBio("");
            dto.setAvatarUrl("");
            dto.setVisible(true); // Mặc định mở hiển thị
        }

        return dto;
    }

    @Override
    @Transactional
    public PublicProfileResponse capNhatHoSoCongKhai(PublicProfileRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        Optional<PublicProfile> optProfile = publicProfileRepository.findByUserId(user.getId());
        
        PublicProfile profile;
        String oldPayload = null;

        if (optProfile.isPresent() == true) {
            profile = optProfile.get();
            oldPayload = taoJsonPublicProfile(profile);
        } else {
            profile = new PublicProfile();
            profile.setUserId(user.getId());
        }

        if (request.getProfessionalTitle() != null) profile.setProfessionalTitle(request.getProfessionalTitle().trim());
        if (request.getWorkplace() != null) profile.setWorkplace(request.getWorkplace().trim());
        if (request.getBio() != null) profile.setBio(request.getBio().trim());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl().trim());
        
        profile.setVisible(request.isVisible());

        PublicProfile saved = publicProfileRepository.save(profile);

        String newPayload = taoJsonPublicProfile(saved);
        trackingService.ghiHanhVi(user.getId(), "UPDATE_PUBLIC_PROFILE", saved.getId(), oldPayload, newPayload);

        PublicProfileResponse dto = new PublicProfileResponse();
        dto.setId(saved.getId());
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        dto.setProfessionalTitle(saved.getProfessionalTitle());
        dto.setWorkplace(saved.getWorkplace());
        dto.setBio(saved.getBio());
        dto.setAvatarUrl(saved.getAvatarUrl());
        dto.setVisible(saved.isVisible());
        
        long totalPosts = postRepository.countByAuthorIdAndIsPublishedTrue(user.getId());
        dto.setTotalPublishedPosts(totalPosts);

        return dto;
    }

    private String taoJsonPublicProfile(PublicProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"professionalTitle\":\"").append(chuoiAnToan(profile.getProfessionalTitle())).append("\",");
        sb.append("\"workplace\":\"").append(chuoiAnToan(profile.getWorkplace())).append("\",");
        sb.append("\"bio\":\"").append(chuoiAnToan(profile.getBio())).append("\",");
        sb.append("\"isVisible\":").append(profile.isVisible());
        sb.append("}");
        return sb.toString();
    }

    // =========================================================================
    // THÔNG TIN CÁ NHÂN
    // =========================================================================

    @Override
    public ProfileMeResponse layThongTinCaNhan() {
        User user = userService.getCurrentAuthenticatedUser();
        return dongGoiThongTinCaNhan(user);
    }

    @Override
    @Transactional
    public ProfileMeResponse capNhatThongTinCaNhan(UpdatePersonalRequest request) {
        User user = userService.getCurrentAuthenticatedUser();

        String oldPayload = taoJsonCaNhan(user);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        User saved = userRepository.save(user);

        String newPayload = taoJsonCaNhan(saved);
        trackingService.ghiHanhVi(saved.getId(), "UPDATE_PROFILE", saved.getId(), oldPayload, newPayload);

        return dongGoiThongTinCaNhan(saved);
    }

    /**
     * Đóng gói User entity thành ProfileMeResponse an toàn.
     * Truy vấn thêm danh sách Roles và Blacklist permissions để điền vào DTO.
     */
    private ProfileMeResponse dongGoiThongTinCaNhan(User user) {
        ProfileMeResponse dto = new ProfileMeResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setBirthDate(user.getBirthDate());
        dto.setProvider(user.getProvider());
        dto.setLocked(user.isLocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        List<String> danhSachRole = new ArrayList<>();
        List<CtUserRole> dsCtUserRole = ctUserRoleRepository.findByUserId(user.getId());
        if (dsCtUserRole != null) {
            for (int i = 0; i < dsCtUserRole.size(); i = i + 1) {
                CtUserRole ctUserRole = dsCtUserRole.get(i);
                UserRole role = userRoleRepository.findById(ctUserRole.getRoleId()).orElse(null);
                if (role != null) {
                    danhSachRole.add(role.getRoleName());
                }
            }
        }
        dto.setRoles(danhSachRole);

        List<String> danhSachBlacklist = new ArrayList<>();
        List<CtUserPermissionBlacklist> dsBlacklist = blacklistRepository.findByUserId(user.getId());
        if (dsBlacklist != null) {
            for (int i = 0; i < dsBlacklist.size(); i = i + 1) {
                CtUserPermissionBlacklist bl = dsBlacklist.get(i);
                Permission perm = permissionRepository.findById(bl.getPermissionId()).orElse(null);
                if (perm != null) {
                    danhSachBlacklist.add(perm.getPermissionCode());
                }
            }
        }
        dto.setBlacklistedPermissions(danhSachBlacklist);

        return dto;
    }

    // =========================================================================
    // HỒ SƠ DOANH NGHIỆP
    // =========================================================================

    @Override
    public PartnerProfileResponse layHoSoDoanhNghiep() {
        User user = userService.getCurrentAuthenticatedUser();
        PartnerProfile profile = partnerProfileRepository.findByUserId(user.getId()).orElse(null);

        PartnerProfileResponse dto = new PartnerProfileResponse();
        if (profile == null) {
            dto.setDaCoHoSo(false);
            return dto;
        }

        dto.setDaCoHoSo(true);
        dto.setProfileId(profile.getId());
        dto.setBusinessName(profile.getBusinessName());
        dto.setBusinessPhone(profile.getBusinessPhone());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setTaxCode(profile.getTaxCode());
        dto.setLicenseNumber(profile.getLicenseNumber());
        dto.setLicenseDocumentUrl(profile.getLicenseDocumentUrl());
        dto.setVerificationStatus(profile.getVerificationStatus());
        return dto;
    }

    @Override
    @Transactional
    public PartnerProfileResponse capNhatHoSoDoanhNghiep(UpdatePartnerRequest request) {
        User user = userService.getCurrentAuthenticatedUser();
        PartnerProfile profile = partnerProfileRepository.findByUserId(user.getId()).orElse(null);

        String oldPayload = null;

        if (profile == null) {
            profile = new PartnerProfile();
            profile.setUserId(user.getId());
            profile.setBusinessName("Chưa cập nhật");
            profile.setVerificationStatus("PENDING");
        } else {
            oldPayload = taoJsonDoanhNghiep(profile);
        }

        if (request.getBusinessName() != null) {
            profile.setBusinessName(request.getBusinessName().trim());
        }
        if (request.getBusinessPhone() != null) {
            profile.setBusinessPhone(request.getBusinessPhone().trim());
        }
        if (request.getTaxCode() != null) {
            profile.setTaxCode(request.getTaxCode().trim());
        }
        if (request.getLicenseNumber() != null) {
            profile.setLicenseNumber(request.getLicenseNumber().trim());
        }

        PartnerProfile saved = partnerProfileRepository.save(profile);

        String newPayload = taoJsonDoanhNghiep(saved);
        trackingService.ghiHanhVi(user.getId(), "UPDATE_BUSINESS", saved.getId(), oldPayload, newPayload);

        PartnerProfileResponse dto = new PartnerProfileResponse();
        dto.setDaCoHoSo(true);
        dto.setProfileId(saved.getId());
        dto.setBusinessName(saved.getBusinessName());
        dto.setBusinessPhone(saved.getBusinessPhone());
        dto.setAvatarUrl(saved.getAvatarUrl());
        dto.setTaxCode(saved.getTaxCode());
        dto.setLicenseNumber(saved.getLicenseNumber());
        dto.setLicenseDocumentUrl(saved.getLicenseDocumentUrl());
        dto.setVerificationStatus(saved.getVerificationStatus());
        return dto;
    }

    // =========================================================================
    // UPLOAD FILE
    // =========================================================================

    @Override
    @Transactional
    public String uploadAnhDaiDien(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(400, "File ảnh không được để trống");
        }

        String originalName = file.getOriginalFilename();
        String extension = layDuoiFile(originalName);
        String[] extsChoPhep = {"jpg", "jpeg", "png", "gif", "webp"};
        boolean hopLe = false;
        for (int i = 0; i < extsChoPhep.length; i = i + 1) {
            if (extsChoPhep[i].equalsIgnoreCase(extension)) {
                hopLe = true;
                break;
            }
        }
        if (hopLe == false) {
            throw new AppException(400, "Chỉ chấp nhận file ảnh: JPG, JPEG, PNG, GIF, WEBP");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(400, "Kích thước file ảnh không được vượt quá 5MB");
        }

        User user = userService.getCurrentAuthenticatedUser();
        PartnerProfile profile = layHoayTaoMoiProfile(user.getId());

        String tenFileMoi = UUID.randomUUID().toString() + "." + extension.toLowerCase();
        String duongDanThu = uploadBasePath + "/partners/avatars/";
        luuFile(file, duongDanThu, tenFileMoi);

        profile.setAvatarUrl("/images/partners/avatars/" + tenFileMoi);
        partnerProfileRepository.save(profile);

        trackingService.ghiHanhVi(user.getId(), "UPLOAD_AVATAR", profile.getId(),
                null, "{\"avatarUrl\":\"" + profile.getAvatarUrl() + "\"}");

        return profile.getAvatarUrl();
    }

    @Override
    @Transactional
    public String uploadGiayPhep(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(400, "File giấy phép không được để trống");
        }

        String originalName = file.getOriginalFilename();
        String extension = layDuoiFile(originalName);
        String[] extsChoPhep = {"pdf", "jpg", "jpeg", "png"};
        boolean hopLe = false;
        for (int i = 0; i < extsChoPhep.length; i = i + 1) {
            if (extsChoPhep[i].equalsIgnoreCase(extension)) {
                hopLe = true;
                break;
            }
        }
        if (hopLe == false) {
            throw new AppException(400, "Chỉ chấp nhận file: PDF, JPG, JPEG, PNG");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new AppException(400, "Kích thước file giấy phép không được vượt quá 20MB");
        }

        User user = userService.getCurrentAuthenticatedUser();
        PartnerProfile profile = layHoayTaoMoiProfile(user.getId());

        String tenFileMoi = UUID.randomUUID().toString() + "." + extension.toLowerCase();
        String duongDanThu = uploadBasePath + "/partners/licenses/";
        luuFile(file, duongDanThu, tenFileMoi);

        profile.setLicenseDocumentUrl("/images/partners/licenses/" + tenFileMoi);
        profile.setVerificationStatus("PENDING");
        partnerProfileRepository.save(profile);

        trackingService.ghiHanhVi(user.getId(), "UPLOAD_LICENSE", profile.getId(),
                null, "{\"licenseDocumentUrl\":\"" + profile.getLicenseDocumentUrl() + "\"}");

        return profile.getLicenseDocumentUrl();
    }

    /**
     * Lấy profile hiện có hoặc tạo mới nếu chưa có — dùng cho upload file.
     * BUSINESS_NAME NOT NULL: khởi tạo mặc định "Chưa cập nhật" thay vì null.
     */
    private PartnerProfile layHoayTaoMoiProfile(Long userId) {
        PartnerProfile profile = partnerProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = new PartnerProfile();
            profile.setUserId(userId);
            profile.setBusinessName("Chưa cập nhật");
            profile.setVerificationStatus("PENDING");
            profile = partnerProfileRepository.save(profile);
        }
        return profile;
    }

    /**
     * Lấy đuôi file từ tên file gốc (lowercase, không có dấu chấm).
     */
    private String layDuoiFile(String tenFile) {
        if (tenFile == null || tenFile.trim().isEmpty()) {
            return "";
        }
        int viTriDot = tenFile.lastIndexOf('.');
        if (viTriDot < 0 || viTriDot == tenFile.length() - 1) {
            return "";
        }
        return tenFile.substring(viTriDot + 1).toLowerCase();
    }

    /**
     * Lưu MultipartFile vào đường dẫn vật lý trên server.
     */
    private void luuFile(MultipartFile file, String duongDanThu, String tenFile) {
        try {
            File thu = new File(duongDanThu);
            if (thu.exists() == false) {
                thu.mkdirs();
            }
            File fileDich = new File(duongDanThu + tenFile);
            file.transferTo(fileDich.getAbsoluteFile());
        } catch (IOException e) {
            throw new AppException(500, "Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }

    // =========================================================================
    // BẢO MẬT & LỊCH SỬ KIỂM TOÁN
    // =========================================================================

    @Override
    @Transactional
    public void doiMatKhau(ChangePasswordRequest request) {
        if (request.getNewPassword().equals(request.getConfirmPassword()) == false) {
            throw new AppException(400, "Xác nhận mật khẩu mới không khớp");
        }

        User user = userService.getCurrentAuthenticatedUser();

        if ("GOOGLE".equalsIgnoreCase(user.getProvider())) {
            throw new AppException(400, "Tài khoản đăng nhập qua Google không thể đổi mật khẩu tại đây");
        }

        boolean matKhauCuDung = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (matKhauCuDung == false) {
            throw new AppException(400, "Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        trackingService.ghiHanhVi(user.getId(), "CHANGE_PASSWORD", user.getId(), null, null);
    }

    @Override
    public OtpHistoryPageResponse layLichSuOtp(int pageNo, int pageSize) {
        User user = userService.getCurrentAuthenticatedUser();
        Page<OtpCode> trang = otpCodeRepository.findByEmailOrderByCreatedAtDesc(
                user.getEmail(), PageRequest.of(pageNo, pageSize));

        List<OtpHistoryResponse> danhSach = new ArrayList<>();
        List<OtpCode> noiBat = trang.getContent();
        for (int i = 0; i < noiBat.size(); i = i + 1) {
            OtpCode otp = noiBat.get(i);
            OtpHistoryResponse dto = new OtpHistoryResponse();
            dto.setId(otp.getId());
            dto.setUsed(otp.isUsed());
            dto.setAttempts(otp.getAttempts());
            dto.setCreatedAt(otp.getCreatedAt());
            dto.setExpiryAt(otp.getExpiryAt());
            danhSach.add(dto);
        }

        OtpHistoryPageResponse response = new OtpHistoryPageResponse();
        response.setDanhSach(danhSach);
        response.setConTrangTiepTheo(trang.hasNext());
        return response;
    }

    @Override
    public LoginHistoryPageResponse layLichSuDangNhap(int pageNo, int pageSize) {
        User user = userService.getCurrentAuthenticatedUser();
        Page<CtUserLoginLog> trang = loginLogRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(pageNo, pageSize));

        List<LoginHistoryResponse> danhSach = new ArrayList<>();
        List<CtUserLoginLog> noiBat = trang.getContent();
        for (int i = 0; i < noiBat.size(); i = i + 1) {
            CtUserLoginLog log = noiBat.get(i);

            String actionCode = "UNKNOWN";
            String actionName = "Không xác định";
            ModerationAction action = actionRepository.findById(log.getActionId()).orElse(null);
            if (action != null) {
                actionCode = action.getCode();
                actionName = action.getName();
            }

            LoginHistoryResponse dto = new LoginHistoryResponse();
            dto.setActionCode(actionCode);
            dto.setActionName(actionName);
            dto.setUsernameAttempt(log.getUsernameAttempt());
            dto.setLoginIp(log.getLoginIp());
            dto.setUserAgent(log.getUserAgent());
            dto.setMessage(log.getMessage());
            dto.setCreatedAt(log.getCreatedAt());
            danhSach.add(dto);
        }

        LoginHistoryPageResponse response = new LoginHistoryPageResponse();
        response.setDanhSach(danhSach);
        response.setConTrangTiepTheo(trang.hasNext());
        return response;
    }

    @Override
    public AccountHistoryPageResponse layLichSuTaiKhoan(int pageNo, int pageSize) {
        User user = userService.getCurrentAuthenticatedUser();
        Page<CtUserModerationLog> trang = moderationLogRepository.layLichSuCoLocVaPhanTrang(
                user.getId(), "ALL", PageRequest.of(pageNo, pageSize));

        List<AccountHistoryResponse> danhSach = new ArrayList<>();
        List<CtUserModerationLog> noiBat = trang.getContent();
        for (int i = 0; i < noiBat.size(); i = i + 1) {
            CtUserModerationLog log = noiBat.get(i);

            String actionCode = "UNKNOWN";
            String actionName = "Không xác định";
            ModerationAction action = actionRepository.findById(log.getActionId()).orElse(null);
            if (action != null) {
                actionCode = action.getCode();
                actionName = action.getName();
            }

            String tenModerator = "Hệ thống";
            User mod = userRepository.findById(log.getModeratorId()).orElse(null);
            if (mod != null) {
                if (mod.getFullName() != null && mod.getFullName().trim().isEmpty() == false) {
                    tenModerator = mod.getFullName();
                } else {
                    tenModerator = mod.getUsername();
                }
            }

            String permissionCode = null;
            if (log.getPermissionId() != null) {
                Permission perm = permissionRepository.findById(log.getPermissionId()).orElse(null);
                if (perm != null) {
                    permissionCode = perm.getPermissionCode();
                }
            }

            AccountHistoryResponse dto = new AccountHistoryResponse();
            dto.setActionCode(actionCode);
            dto.setActionName(actionName);
            dto.setReason(log.getReason());
            dto.setTenModerator(tenModerator);
            dto.setPermissionCode(permissionCode);
            dto.setCreatedAt(log.getCreatedAt());
            danhSach.add(dto);
        }

        long tongSoLanBiKhoa = moderationLogRepository.demSoLanBiKhoa(user.getId());

        AccountHistoryPageResponse response = new AccountHistoryPageResponse();
        response.setDanhSach(danhSach);
        response.setConTrangTiepTheo(trang.hasNext());
        response.setTongSoLanBiKhoa(tongSoLanBiKhoa);
        return response;
    }

    // =========================================================================
    // PHÂN QUYỀN CHI TIẾT
    // =========================================================================

    @Override
    public UserPermissionResponse layQuyenHanChiTiet() {
        User user = userService.getCurrentAuthenticatedUser();

        // Tìm role chính: cấp bậc thấp nhất = quyền lực cao nhất
        List<CtUserRole> dsCtRole = ctUserRoleRepository.findByUserId(user.getId());
        UserRole roleChinh = null;
        for (int i = 0; i < dsCtRole.size(); i = i + 1) {
            CtUserRole ctRole = dsCtRole.get(i);
            UserRole role = userRoleRepository.findById(ctRole.getRoleId()).orElse(null);
            if (role == null) {
                continue;
            }
            if (roleChinh == null || role.getRoleLevel() < roleChinh.getRoleLevel()) {
                roleChinh = role;
            }
        }

        UserPermissionResponse response = new UserPermissionResponse();
        if (roleChinh == null) {
            response.setRoleName("Chưa phân quyền");
            response.setDanhSachQuyen(new ArrayList<>());
            return response;
        }

        response.setRoleName(roleChinh.getRoleName());
        response.setRoleLevel(roleChinh.getRoleLevel());
        response.setRoleDescription(roleChinh.getDescription());

        // Lấy danh sách permission của role chính và blacklist của user
        List<CtRolePermission> dsCtRolePerm = ctRolePermissionRepository.findByRoleId(roleChinh.getId());
        List<CtUserPermissionBlacklist> dsBlacklist = blacklistRepository.findByUserId(user.getId());

        List<UserPermissionResponse.PermissionItem> danhSachQuyen = new ArrayList<>();
        int soHoatDong = 0;
        int soBiKhoa = 0;

        for (int i = 0; i < dsCtRolePerm.size(); i = i + 1) {
            CtRolePermission ctRolePerm = dsCtRolePerm.get(i);
            Permission perm = permissionRepository.findById(ctRolePerm.getPermissionId()).orElse(null);
            if (perm == null) {
                continue;
            }

            boolean biBlacklist = false;
            for (int j = 0; j < dsBlacklist.size(); j = j + 1) {
                if (dsBlacklist.get(j).getPermissionId().equals(perm.getId())) {
                    biBlacklist = true;
                    break;
                }
            }

            UserPermissionResponse.PermissionItem item = new UserPermissionResponse.PermissionItem();
            item.setPermissionCode(perm.getPermissionCode());
            item.setDescription(perm.getDescription());
            item.setBiBlacklist(biBlacklist);

            if (biBlacklist == true) {
                soBiKhoa = soBiKhoa + 1;
                CtUserModerationLog logBlacklist = moderationLogRepository
                        .timLyDoBlacklist(user.getId(), perm.getId()).orElse(null);
                if (logBlacklist != null) {
                    item.setLyDoBlacklist(logBlacklist.getReason());
                    item.setBlacklistedAt(logBlacklist.getCreatedAt());
                    User mod = userRepository.findById(logBlacklist.getModeratorId()).orElse(null);
                    if (mod != null) {
                        boolean coTen = mod.getFullName() != null && mod.getFullName().trim().isEmpty() == false;
                        item.setTenModerator(coTen ? mod.getFullName() : mod.getUsername());
                    }
                }
            } else {
                soHoatDong = soHoatDong + 1;
            }

            danhSachQuyen.add(item);
        }

        response.setSoQuyenHoatDong(soHoatDong);
        response.setSoQuyenBiKhoa(soBiKhoa);
        response.setDanhSachQuyen(danhSachQuyen);
        return response;
    }

    // =========================================================================
    // THỐNG KÊ
    // =========================================================================

    @Override
    public ProfileStatsResponse layThongKe() {
        ProfileStatsResponse dto = new ProfileStatsResponse();
        dto.setAddressCount(0);
        dto.setQuoteSent(0);
        dto.setEventsAttended(0);
        dto.setPostsViewed(0);

        return dto;
    }

    // =========================================================================
    // PAYLOAD HELPERS — AUDIT TRAIL (DEMOCACH2 PATTERN)
    // =========================================================================

    /** Tạo JSON snapshot thông tin cá nhân user để ghi audit log. */
    private String taoJsonCaNhan(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"fullName\":\"").append(chuoiAnToan(user.getFullName())).append("\",");
        sb.append("\"phone\":\"").append(chuoiAnToan(user.getPhone())).append("\",");
        sb.append("\"address\":\"").append(chuoiAnToan(user.getAddress())).append("\",");
        sb.append("\"birthDate\":\"").append(user.getBirthDate() != null ? user.getBirthDate().toString() : "").append("\"");
        sb.append("}");
        return sb.toString();
    }

    /** Tạo JSON snapshot hồ sơ doanh nghiệp để ghi audit log. */
    private String taoJsonDoanhNghiep(PartnerProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"businessName\":\"").append(chuoiAnToan(profile.getBusinessName())).append("\",");
        sb.append("\"businessPhone\":\"").append(chuoiAnToan(profile.getBusinessPhone())).append("\",");
        sb.append("\"taxCode\":\"").append(chuoiAnToan(profile.getTaxCode())).append("\",");
        sb.append("\"licenseNumber\":\"").append(chuoiAnToan(profile.getLicenseNumber())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /** Escape ký tự đặc biệt trong JSON string value. */
    private String chuoiAnToan(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}