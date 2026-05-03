//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/service/impl/UserServiceImpl.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.impl;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.repositories.IRepository.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IAuditService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IEmailService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * =========================================================================
 * THỰC THI DỊCH VỤ NGƯỜI DÙNG - CẤU TRÚC 6 BẢNG ĐỘNG
 * =========================================================================
 * Xử lý xác thực, đăng ký, OTP và phân quyền người dùng.
 * Tuân thủ quy tắc tối cao: Không dùng Stream API, dùng mảng toArray() và vòng lặp for.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService, UserDetailsService {

    private final IUserRepository userRepository;
    private final IOtpCodeRepository otpRepository;
    
    // Tiêm Interface Email, mù hoàn toàn cách Email được gửi đi
    private final IEmailService emailService;
    
    // TIÊM 4 REPOSITORY CỦA MÔ HÌNH PHÂN QUYỀN MỚI
    private final IUserRoleRepository userRoleRepository;
    private final ICtUserRoleRepository ctUserRoleRepository;
    private final IPermissionRepository permissionRepository;
    private final ICtRolePermissionRepository ctRolePermissionRepository;

    // [THÊM MỚI] Dịch vụ ghi nhật ký kiểm toán — dùng trong updateUserRoles()
    private final IAuditService auditService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * =========================================================================
     * 1. HÀM CỐT LÕI: NẠP QUYỀN TỪ 6 BẢNG VÀO RAM (TRANSIENT)
     * =========================================================================
     * Quét xuyên qua các bảng trung gian để gom Nhóm Quyền và Quyền Hạt Lựu.
     * Đã mở public để RolesService có thể nhờ nạp quyền khi vẽ danh sách Admin.
     */
    @Override
    public void napQuyenChoNguoiDung(User user) {
        if (user == null) { 
            return; 
        }
        
        List<String> danhSachTenRole = new ArrayList<>();
        List<String> danhSachTenPermission = new ArrayList<>();
        
        // Bước A: Tìm các chức vụ của User trong bảng trung gian CT_USER_ROLES
        List<CtUserRole> userRolesMap = ctUserRoleRepository.findByUserId(user.getId());
        
        if (userRolesMap != null) {
            Object[] mapArray = userRolesMap.toArray();
            for (int i = 0; i < mapArray.length; i = i + 1) {
                CtUserRole map = (CtUserRole) mapArray[i];
                
                // Bước B: Lấy thông tin Nhóm Quyền từ bảng USER_ROLES
                UserRole role = userRoleRepository.findById(map.getRoleId()).orElse(null);
                
                if (role != null) {
                    danhSachTenRole.add(role.getRoleName());
                    
                    // Bước C: Tìm các quyền hạt lựu của Nhóm Quyền này trong bảng CT_ROLE_PERMISSIONS
                    List<CtRolePermission> rolePermsMap = ctRolePermissionRepository.findByRoleId(role.getId());
                    
                    if (rolePermsMap != null) {
                        Object[] permMapArray = rolePermsMap.toArray();
                        for (int j = 0; j < permMapArray.length; j = j + 1) {
                            CtRolePermission permMap = (CtRolePermission) permMapArray[j];
                            
                            // Bước D: Lấy mã quyền chi tiết từ bảng PERMISSIONS
                            Permission permission = permissionRepository.findById(permMap.getPermissionId()).orElse(null);
                            
                            if (permission != null) {
                                // Thuật toán chống trùng lặp quyền hạt lựu (Không dùng Set hay Stream)
                                boolean daTonTai = false;
                                Object[] currentPerms = danhSachTenPermission.toArray();
                                for(int k = 0; k < currentPerms.length; k = k + 1) {
                                    if (currentPerms[k].toString().equals(permission.getPermissionCode())) {
                                        daTonTai = true; 
                                        break;
                                    }
                                }
                                // Nếu chưa có trong RAM thì mới thêm vào
                                if (daTonTai == false) {
                                    danhSachTenPermission.add(permission.getPermissionCode());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bơm dữ liệu tìm được vào biến tạm (Transient) của Entity User
        user.setDanhSachTenRole(danhSachTenRole);
        user.setDanhSachTenPermission(danhSachTenPermission);
    }

    /**
     * =========================================================================
     * 2. HÀM NỘI BỘ: TÍNH TOÁN CẤP BẬC TỪ DATABASE (ROLE_LEVEL)
     * =========================================================================
     * Chống hiện tượng nhân viên lạm quyền khóa tài khoản của Sếp.
     */
    private int getRoleLevel(User user) {
        int capDoManhNhat = 999; // Khởi tạo cấp độ rất yếu (Số càng to càng yếu)
        
        List<String> danhSachRole = user.getDanhSachTenRole();
        
        if (danhSachRole != null) {
            Object[] rolesArray = danhSachRole.toArray();
            for (int i = 0; i < rolesArray.length; i = i + 1) {
                String tenRole = rolesArray[i].toString();
                
                // Truy vấn Database để lấy Level của từng Role
                UserRole roleTuDb = userRoleRepository.findByRoleName(tenRole).orElse(null);
                
                if (roleTuDb != null) {
                    int capDoHienTai = roleTuDb.getRoleLevel();
                    // Cập nhật nếu tìm thấy cấp độ mạnh hơn (Số càng nhỏ càng mạnh)
                    if (capDoHienTai < capDoManhNhat) {
                        capDoManhNhat = capDoHienTai;
                    }
                }
            }
        }
        return capDoManhNhat;
    }

    // =========================================================================
    // IMPLEMENT CỦA SPRING SECURITY VÀ TÌM KIẾM CƠ BẢN
    // =========================================================================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        napQuyenChoNguoiDung(user); // Bắt buộc nạp 6 bảng trước khi Security kiểm tra
        return user;
    }

    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        napQuyenChoNguoiDung(user);
        return user;
    }

    @Override
    public User findByUsernameOrEmail(String loginInput) {
        User user = userRepository.findByUsername(loginInput)
                .orElseGet(() -> userRepository.findByEmail(loginInput).orElse(null));
        napQuyenChoNguoiDung(user);
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng"));
        napQuyenChoNguoiDung(user);
        return user;
    }

    // =========================================================================
    // NGHIỆP VỤ ĐĂNG KÝ VÀ CẤP QUYỀN MẶC ĐỊNH
    // =========================================================================
    @Override
    @Transactional
    public void registerLocalUser(String fullName, String username, String email, String rawPassword, String phone, LocalDate birthDate, String address) {
        if (userRepository.existsByUsername(username)) { 
            throw new AppException(400, "Username đã tồn tại trong hệ thống"); 
        }
        if (userRepository.existsByEmail(email)) { 
            throw new AppException(400, "Email đã tồn tại trong hệ thống"); 
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setBirthDate(birthDate);
        user.setAddress(address);
        user.setProvider("LOCAL");
        
        user = userRepository.save(user); // Lưu để lấy ID
        
        // Tự động tìm quyền 'USER' trong DB để gán cho người mới
        UserRole defaultRole = userRoleRepository.findByRoleName("USER").orElse(null);
        if (defaultRole != null) {
            CtUserRole ctUserRole = new CtUserRole(user.getId(), defaultRole.getId());
            ctUserRoleRepository.save(ctUserRole);
        }
    }

    @Override
    @Transactional
    public void saveGoogleUser(String email, String name) {
        if (userRepository.findByEmail(email).isPresent()) { 
            return; 
        }

        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (baseUsername.length() < 3) { 
            baseUsername = "user" + baseUsername; 
        }
        if (baseUsername.length() > 30) { 
            baseUsername = baseUsername.substring(0, 30); 
        }

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
            if (counter > 100) { 
                throw new AppException(500, "Không thể tạo username unique cho email: " + email); 
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(passwordEncoder.encode("google_random_" + new Random().nextLong()));
        user.setProvider("GOOGLE");

        try {
            user = userRepository.save(user);
            
            // Tự động tìm quyền 'USER' trong DB để gán cho người mới
            UserRole defaultRole = userRoleRepository.findByRoleName("USER").orElse(null);
            if (defaultRole != null) {
                CtUserRole ctUserRole = new CtUserRole(user.getId(), defaultRole.getId());
                ctUserRoleRepository.save(ctUserRole);
            }
        } catch (Exception e) {
            throw new AppException(500, "Lỗi lưu dữ liệu Google User: " + e.getMessage());
        }
    }

    // =========================================================================
    // NGHIỆP VỤ OTP VÀ BẢO MẬT
    // =========================================================================
    @Override
    public void sendOtp(String email, String purpose) {
        LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);
        if (otpRepository.countByEmailAndCreatedAtAfter(email, fiveMinAgo) >= 5) {
            throw new AppException(429, "Bạn thao tác quá nhanh. Thử lại sau 5 phút.");
        }

        // Tắt hiệu lực các mã OTP cũ bằng vòng lặp For truyền thống
        List<OtpCode> danhSachOtpCu = otpRepository.findAll();
        Object[] otpArray = danhSachOtpCu.toArray();
        for (int i = 0; i < otpArray.length; i = i + 1) {
            OtpCode otp = (OtpCode) otpArray[i];
            if (otp.getEmail().equals(email) && otp.isUsed() == false) {
                otp.setUsed(true);
            }
        }
        otpRepository.saveAllAndFlush(danhSachOtpCu);

        String code = String.format("%06d", new Random().nextInt(1000000));
        
        OtpCode otp = new OtpCode();
        otp.setEmail(email);
        otp.setCode(code);
        otpRepository.save(otp);

        String subject = purpose.equals("register") ? "Mã xác nhận đăng ký" : "Mã xác nhận đặt lại mật khẩu";
        String content = "<p>Mã OTP của bạn là: <strong style='font-size: 24px; color: blue;'>" + code + "</strong></p><p>Hiệu lực 3 phút.</p>";
        
        emailService.sendEmail(email, subject, content);
    }

    @Override
    public boolean verifyOtp(String email, String code) {
        LocalDateTime now = LocalDateTime.now();
        OtpCode otp = otpRepository
                .findTopByEmailAndCodeAndUsedFalseAndExpiryAtAfterOrderByCreatedAtDesc(email, code, now)
                .orElse(null);

        if (otp == null) {
            // Tăng số lần nhập sai bằng vòng lặp For
            List<OtpCode> tatCaOtp = otpRepository.findAll();
            Object[] otpArray = tatCaOtp.toArray();
            for (int i = 0; i < otpArray.length; i = i + 1) {
                OtpCode o = (OtpCode) otpArray[i];
                if (o.getEmail().equals(email) && o.isUsed() == false) {
                    o.setAttempts(o.getAttempts() + 1);
                    if (o.getAttempts() >= 5) {
                        o.setUsed(true); // Khóa mã nếu nhập sai 5 lần
                    }
                    otpRepository.save(o);
                    break; 
                }
            }
            return false;
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng với email này"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // =========================================================================
    // NGHIỆP VỤ LÕI ADMIN: GÁN QUYỀN ĐỘNG VÀ KHÓA TÀI KHOẢN
    // =========================================================================
    @Override
    @Transactional
    public void updateUserRoles(Long targetUserId, List<String> roleNames, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng cần phân quyền"));

        napQuyenChoNguoiDung(targetUser); // Nạp danhSachTenRole vào @Transient để chụp role cũ và so sánh Level

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new AppException(403, "Bạn không thể tự thay đổi quyền của chính mình");
        }

        int currentLevel = getRoleLevel(currentUser);
        int targetLevel  = getRoleLevel(targetUser);

        // Quy tắc: Phải có Level NHỎ HƠN (Quyền to hơn) mới được sửa người ta.
        if (currentLevel >= targetLevel) {
            throw new AppException(403, "Bạn không đủ thẩm quyền sửa tài khoản này");
        }

        // [THÊM MỚI] Chụp danh sách chức vụ cũ (đã được napQuyenChoNguoiDung nạp sẵn)
        List<String> danhSachRoleCu = new ArrayList<>();
        List<String> tenRoleCuTuDb = targetUser.getDanhSachTenRole();
        if (tenRoleCuTuDb != null) {
            Object[] roleCuArr = tenRoleCuTuDb.toArray();
            for (int i = 0; i < roleCuArr.length; i = i + 1) {
                danhSachRoleCu.add(roleCuArr[i].toString());
            }
        }

        // Bước 1: Tước toàn bộ chức vụ cũ của người này trong DB
        ctUserRoleRepository.deleteByUserId(targetUserId);

        boolean coQuyenHopLe = false;
        // [THÊM MỚI] Theo dõi danh sách chức vụ thực sự được lưu thành công
        List<String> danhSachRoleMoi = new ArrayList<>();

        // Bước 2: Gán danh sách chức vụ mới
        if (roleNames != null) {
            Object[] newRolesArray = roleNames.toArray();
            for (int i = 0; i < newRolesArray.length; i = i + 1) {
                String roleName = newRolesArray[i].toString();

                UserRole roleTuDb = userRoleRepository.findByRoleName(roleName).orElse(null);
                if (roleTuDb != null) {
                    int newLevel = roleTuDb.getRoleLevel();

                    // Admin không được phép gán chức vụ to hơn hoặc bằng quyền của chính Admin
                    if (currentLevel >= newLevel) {
                        throw new AppException(403, "Bạn không thể cấp quyền " + roleName + " vì nó vượt quá hoặc ngang bằng thẩm quyền của bạn");
                    }

                    // Lưu liên kết mới
                    CtUserRole ctUserRole = new CtUserRole(targetUserId, roleTuDb.getId());
                    ctUserRoleRepository.save(ctUserRole);
                    danhSachRoleMoi.add(roleName); // [THÊM MỚI]
                    coQuyenHopLe = true;
                }
            }
        }

        // Chống lỗi: Nếu Admin không tick quyền nào hợp lệ, tự động trả về mức USER thấp nhất
        if (coQuyenHopLe == false) {
            UserRole defaultRole = userRoleRepository.findByRoleName("USER").orElse(null);
            if (defaultRole != null) {
                CtUserRole ctUserRole = new CtUserRole(targetUserId, defaultRole.getId());
                ctUserRoleRepository.save(ctUserRole);
                danhSachRoleMoi.add("USER"); // [THÊM MỚI]
            }
        }

        // =========================================================================
        // [THÊM MỚI] GHI NHẬT KÝ KIỂM TOÁN: DIFF TỪNG CHỨC VỤ THÊM / THU HỒI
        // Bọc try-catch: lỗi log KHÔNG rollback việc lưu phân quyền.
        // (Phòng trường hợp ASSIGN_ROLE chưa được seed khi server chưa restart)
        // =========================================================================
        try {
            Integer viewPermId = null;
            Permission viewPerm = permissionRepository.findByPermissionCode("VIEW").orElse(null);
            if (viewPerm != null) {
                viewPermId = viewPerm.getId();
            }

            // Xây dựng chuỗi lý do: [roleCu1, roleCu2] → [roleMoi1, roleMoi2]
            String roleCuText = "";
            Object[] roleCuArr2 = danhSachRoleCu.toArray();
            for (int i = 0; i < roleCuArr2.length; i = i + 1) {
                if (i > 0) { roleCuText = roleCuText + ", "; }
                roleCuText = roleCuText + roleCuArr2[i].toString();
            }
            String roleMoiText = "";
            Object[] roleMoiArr = danhSachRoleMoi.toArray();
            for (int i = 0; i < roleMoiArr.length; i = i + 1) {
                if (i > 0) { roleMoiText = roleMoiText + ", "; }
                roleMoiText = roleMoiText + roleMoiArr[i].toString();
            }
            String lyDo = "Cập nhật chức vụ: [" + roleCuText + "] → [" + roleMoiText + "]";

            // Log ASSIGN_ROLE cho từng chức vụ được thêm mới (chưa có trong danh sách cũ)
            for (int i = 0; i < roleMoiArr.length; i = i + 1) {
                String tenRoleMoi = roleMoiArr[i].toString();
                boolean daCoTruoc = false;
                for (int j = 0; j < roleCuArr2.length; j = j + 1) {
                    if (roleCuArr2[j].toString().equals(tenRoleMoi)) {
                        daCoTruoc = true;
                        break;
                    }
                }
                if (daCoTruoc == false) {
                    auditService.logAction(targetUserId, "ASSIGN_ROLE", viewPermId, currentUser.getId(), lyDo);
                }
            }

            // Log REVOKE_ROLE cho từng chức vụ bị thu hồi (không còn trong danh sách mới)
            for (int i = 0; i < roleCuArr2.length; i = i + 1) {
                String tenRoleCu = roleCuArr2[i].toString();
                boolean vanConSau = false;
                for (int j = 0; j < roleMoiArr.length; j = j + 1) {
                    if (roleMoiArr[j].toString().equals(tenRoleCu)) {
                        vanConSau = true;
                        break;
                    }
                }
                if (vanConSau == false) {
                    auditService.logAction(targetUserId, "REVOKE_ROLE", viewPermId, currentUser.getId(), lyDo);
                }
            }
        } catch (Exception e) {
            // Ghi log thất bại — bỏ qua, không rollback phân quyền
        }
    }

    @Override
    public void lockUnlockUser(Long targetUserId, boolean lock, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy người dùng"));
        
        napQuyenChoNguoiDung(targetUser);

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new AppException(403, "Không thể tự khóa chính mình");
        }

        int currentLevel = getRoleLevel(currentUser);
        int targetLevel = getRoleLevel(targetUser);

        if (currentLevel >= targetLevel) {
            throw new AppException(403, "Bạn không đủ thẩm quyền khóa/mở khóa người này");
        }

        targetUser.setLocked(lock);
        userRepository.save(targetUser);
    }

    // =========================================================================
    // NGHIỆP VỤ THỐNG KÊ (DÙNG VÒNG LẶP FOR)
    // =========================================================================
    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        Object[] usersArray = users.toArray();
        for (int i = 0; i < usersArray.length; i = i + 1) {
            User u = (User) usersArray[i];
            napQuyenChoNguoiDung(u);
        }
        return users;
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.isAuthenticated() == false) {
            throw new AppException(401, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User u = (User) principal;
            napQuyenChoNguoiDung(u);
            return u;
        }

        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            User u = userRepository.findByEmail(oidcUser.getEmail())
                    .orElseThrow(() -> new AppException(404, "Lỗi đồng bộ dữ liệu tài khoản Google"));
            napQuyenChoNguoiDung(u);
            return u;
        }

        if (principal instanceof String) {
            String username = (String) principal;
            User u = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(404, "Tài khoản không tồn tại trong hệ thống"));
            napQuyenChoNguoiDung(u);
            return u;
        }

        throw new AppException(500, "Không xác định được danh tính bảo mật");
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public int layCapBacQuyenLucCaoNhat(User user) {
        // Ủy quyền cho hàm nội bộ getRoleLevel đã có sẵn, expose ra Interface
        return getRoleLevel(user);
    }

    @Override
    public long countLockedUsers() {
        long count = 0;
        List<User> tatCaNguoiDung = userRepository.findAll();
        Object[] usersArray = tatCaNguoiDung.toArray();
        for (int i = 0; i < usersArray.length; i = i + 1) {
            User u = (User) usersArray[i];
            if (u.isLocked() == true) {
                count = count + 1;
            }
        }
        return count;
    }
}