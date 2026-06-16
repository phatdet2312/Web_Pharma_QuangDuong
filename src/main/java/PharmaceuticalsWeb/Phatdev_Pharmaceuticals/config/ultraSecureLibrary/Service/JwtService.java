
//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/JwtService.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityClaimType;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityAuthoritySnapshot;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenClaim;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenConstants;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenVersion;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final SecurityLibraryProperties thuVienProps;

    //Khởi tạo SecretKey duy nhất 1 lần lúc boot server
    // Tránh việc gọi Keys.hmacShaKeyFor lặp đi lặp lại ở mỗi Request
    private final SecretKey khoaBaoMatToanCuc;
    //Hằng số Lãnh địa khởi tạo 1 lần duy nhất lúc Server bật!
    private final String dauAnLanhDia;
    private final MaTranLuoiLocNghiaTrangQuyenHanCu nghiaTrangQuyenHan;

    // Khởi tạo thông qua Constructor
    public JwtService(SecurityLibraryProperties thuVienProps, 
        MaTranLuoiLocNghiaTrangQuyenHanCu nghiaTrangQuyenHan) {
        this.thuVienProps = thuVienProps;
        this.nghiaTrangQuyenHan = nghiaTrangQuyenHan;
        String chuoiBaoMat = this.thuVienProps.getJwtSecret();
        this.khoaBaoMatToanCuc = Keys.hmacShaKeyFor(chuoiBaoMat.getBytes(StandardCharsets.UTF_8));
        this.dauAnLanhDia = thuVienProps.dauAnLanhDia();
        System.out.println("[JwtService] Đã khởi tạo Khóa HMAC-SHA256 toàn cục vào RAM.");
    }

    // Tạo JWT Token (giống hệt code C# của bạn)
    public String generateToken(ISecurityUserAdapter userAdapter, String clientSecret) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userAdapter.layEmail());
        String fullName = userAdapter.layTenDayDu();
        if (fullName == null) {
            fullName = userAdapter.layTenDangNhap();
        }
        claims.put("fullName", fullName);
        
        // App chu dong dong goi authorities, claims bo sung va fingerprint typed.
        SecurityAuthoritySnapshot snapshot = userAdapter.layAnhChupBaoMat();
        ghiSnapshotVaoClaims(claims, snapshot);
        
        // =========================================================================
        // [THUẬT TOÁN DÒ MÌN TỊNH TIẾN] - VƯỢT LỖ HỔNG PHANTOM CLONE VÀ LOGOUT/LOGIN
        // Ý tưởng của ngài: Dùng Nghĩa trang làm La Bàn Định Vị Phiên Bản.
        // =========================================================================
        Long userId = userAdapter.layIdNguoiDung();
        int v_adn = 0;
        int maxLoopSafety = 100; // Cầu chì chống treo CPU (Phòng khi Bloom Filter bị Full 100%)

        while (maxLoopSafety > 0) {
            maxLoopSafety--;
            // Ghép chuỗi thử nghiệm
            String adnThuNghiem = SecurityTokenVersion.taoDna(
                    userId, snapshot.laySecurityFingerprint(), v_adn);
            // Rọi xuống Nghĩa Trang xem cái DNA này có phải đồ cũ không?
            if (nghiaTrangQuyenHan.kiemTraDaChet(adnThuNghiem) == false) {
                // TÌM THẤY CHÂN LÝ! DNA này sạch sẽ, chưa từng bị chôn!
                break;
            }
            // Nếu đã chết, tịnh tiến lên 1 và thử lại
            v_adn++;
        }

        System.out.println("[JwtService] Đã cấp v_adn: " + v_adn);

        // Cầu chì an toàn: Nếu lặp quá 100 lần (Điều gần như không thể xảy ra vật lý trừ khi bị DoS RAM)
        if (maxLoopSafety == 0) {
            // Lấy thời gian thực làm version để ép thoát vòng lặp
            v_adn = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            System.err.println("[JwtService] Kích hoạt cầu chì an toàn. Sử dụng Timestamp làm version DNA.");
        }

        //Gắn thêm ID của User vào Token để lấy ra check Ma trận
        claims.put("userId", userId);
        claims.put("c_secret", clientSecret);
        claims.put("clusterId", this.dauAnLanhDia);
         //ĐÓNG ẤN CHÚ DNA VÀO TOKEN
        claims.put("v_adn", v_adn);
        long thoiGianHienTai = System.currentTimeMillis();
        long thoiGianHetHan = thoiGianHienTai + thuVienProps.getJwtExpirationMs();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userAdapter.layTenDangNhap())
                .setIssuedAt(new Date(thoiGianHienTai))
                .setExpiration(new Date(thoiGianHetHan))
                .setIssuer(thuVienProps.getJwtIssuer())
                .setAudience(thuVienProps.getJwtAudience())
                .signWith(khoaBaoMatToanCuc, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * TẠO NGỰA GỖ THÀNH TROY (TROJAN JWT) ĐỂ ĐỒNG BỘ XUYÊN LỤC ĐỊA
     * Nhét Lệnh Cấm ngầm vào Claims của JWT để qua mặt mọi thể loại Firewall.
     */
    public String generateTrojanSyncToken(String syncData) {
        Map<String, Object> claims = new HashMap<>();
        
        // Đóng ấn Ngựa Gỗ
        claims.put("sync_payload", syncData);
        
        // Giả lập như một Token thật để qua mặt Filter
        claims.put("userId", -999999L); 
        claims.put("c_secret", this.thuVienProps.getJwtSecret()); // Khóa mẹ, để Filter 2 xác thực HMAC
        claims.put("clusterId", this.dauAnLanhDia);
        
        List<String> authorities = new ArrayList<>();
        authorities.add("ROLE_SYNC_SYSTEM");
        SecurityAuthoritySnapshot snapshot = new SecurityAuthoritySnapshot(authorities, new ArrayList<>(),
                "sync=SYSTEM_SYNC_AGENT");
        ghiSnapshotVaoClaims(claims, snapshot);
        claims.put("v_adn", 0);

        long thoiGianHienTai = System.currentTimeMillis();
        long thoiGianHetHan = thoiGianHienTai + 60000; // Ngựa gỗ chỉ sống 60 giây!

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("SYSTEM_SYNC_AGENT")
                .setIssuedAt(new Date(thoiGianHienTai))
                .setExpiration(new Date(thoiGianHetHan))
                .setIssuer(thuVienProps.getJwtIssuer())
                .setAudience(thuVienProps.getJwtAudience())
                .signWith(khoaBaoMatToanCuc, SignatureAlgorithm.HS256)
                .compact();
    }



    //hàm lấy username từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //hàm kiểm tra token hợp lệ
    public boolean isTokenValid(String token, String usernameTuDB) {
        final String username = extractUsername(token);
        boolean usernameKhop = false;
        if (username != null && username.equals(usernameTuDB) == true) {
            usernameKhop = true;
        }
        if (usernameKhop == true && isTokenExpired(token) == false) {
            return true;
        }
        return false; 
    }

    //hàm kiểm tra hết hạn
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //hàm lấy thời gian hết hạn
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //hàm lấy claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //hàm giải mã token
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(khoaBaoMatToanCuc)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void ghiSnapshotVaoClaims(Map<String, Object> claims, SecurityAuthoritySnapshot snapshot) {
        List<String> authorities = snapshot.layAuthoritiesChuanHoa();
        List<SecurityTokenClaim> claimsBoSung = snapshot.layClaimsBoSung();
        List<String> exposedAttributes = new ArrayList<>();

        claims.put(SecurityTokenConstants.CLAIM_AUTHORITIES, authorities);
        claims.put(SecurityTokenConstants.CLAIM_SECURITY_FINGERPRINT, snapshot.laySecurityFingerprint());

        Object[] mangClaim = claimsBoSung.toArray();
        for (int i = 0; i < mangClaim.length; i = i + 1) {
            SecurityTokenClaim claim = (SecurityTokenClaim) mangClaim[i];
            claims.put(claim.getClaimName(), chuyenClaimThanhGiaTriJwt(claim));
            if (claim.isExposeAsRequestAttribute() == true) {
                exposedAttributes.add(claim.getClaimName());
            }
        }

        claims.put(SecurityTokenConstants.CLAIM_EXPOSED_ATTRIBUTES, exposedAttributes);
    }

    private Object chuyenClaimThanhGiaTriJwt(SecurityTokenClaim claim) {
        if (SecurityClaimType.STRING.equals(claim.getClaimType())) {
            return claim.layGiaTriChuoi();
        }
        if (SecurityClaimType.LONG.equals(claim.getClaimType())) {
            return claim.layGiaTriSo();
        }
        if (SecurityClaimType.BOOLEAN.equals(claim.getClaimType())) {
            return claim.layGiaTriBoolean();
        }
        return claim.layGiaTriDanhSachChuoi();
    }
}
