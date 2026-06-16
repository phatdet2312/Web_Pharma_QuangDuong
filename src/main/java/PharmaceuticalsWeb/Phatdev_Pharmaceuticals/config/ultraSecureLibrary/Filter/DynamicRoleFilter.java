//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/DynamicRoleFilter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityAuthoritySnapshot;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityClaimType;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenClaim;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenConstants;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Model.SecurityTokenVersion;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Provider.ISecurityUserProvider;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.CookieUtils;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.JwtService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.MaTranLuoiLocChongPhatLai;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.MaTranLuoiLocNghiaTrangQuyenHanCu;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.MaTranNhiPhanNguyenTu;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.TramPhatSongVoTuyenP2P;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class DynamicRoleFilter extends OncePerRequestFilter {

    private final ISecurityUserProvider userProvider;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final SecurityLibraryProperties props;
    private final String urlChuyenHuongKhiKhoa;// Lưu URL chuyển hướng thành Hằng số Runtime (Tính 1 lần duy nhất)
    private final MaTranLuoiLocChongPhatLai maTranLuoiLocChongPhatLai;
    private final long GIOI_HAN_REPLAY_MS;
    private final MaTranLuoiLocNghiaTrangQuyenHanCu maTranLuoiLocNghiaTrangQuyenHanCu;

    private final TramPhatSongVoTuyenP2P tramPhatSong;
    private final boolean enableInterClusterSync;
    private final String jwtSecretGoc;

    @Autowired
    public DynamicRoleFilter(
            ISecurityUserProvider userProvider,
            JwtService jwtService,
            CookieUtils cookieUtils,
            SecurityLibraryProperties props,
            MaTranLuoiLocChongPhatLai maTranLuoiLocChongPhatLai,
            MaTranLuoiLocNghiaTrangQuyenHanCu maTranLuoiLocNghiaTrangQuyenHanCu,
            TramPhatSongVoTuyenP2P tramPhatSong) {
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.cookieUtils = cookieUtils;
        this.props = props;
        this.maTranLuoiLocChongPhatLai = maTranLuoiLocChongPhatLai;
        this.maTranLuoiLocNghiaTrangQuyenHanCu = maTranLuoiLocNghiaTrangQuyenHanCu;
        this.tramPhatSong = tramPhatSong;

        // Tối ưu RAM O(1): Nối chuỗi đúng 1 lần khi khởi động Server
        this.urlChuyenHuongKhiKhoa = props.getLoginUrl() + "?locked";
        this.GIOI_HAN_REPLAY_MS = props.getReplayWindowMs();
        this.enableInterClusterSync = props.isEnableInterClusterSync();
        this.jwtSecretGoc = props.getJwtSecret();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // NHẬN LẠI TOKEN GỐC ĐỂ KIỂM TRA NGHĨA TRANG

        // Bỏ qua kiểm tra cho trang đăng nhập và tài nguyên tĩnh để tránh vòng lặp
        // chuyển hướng
        // Kiểm tra URL công khai ĐỌC THẺ BÀI O(1) RAM/CPU
        Boolean theBaiCongKhai = (Boolean) request.getAttribute("LA_URL_CONG_KHAI");
        boolean laUrlCongKhai = false;
        if (theBaiCongKhai != null) {
            if (theBaiCongKhai == true) {
                laUrlCongKhai = true;
            }
        }
        // Kiểm tra URL tĩnh bằng cấu hình động từ Thư viện
        Boolean theBaiTaiNguyenTinh = (Boolean) request.getAttribute("LA_URL_TINH_METHOD_GET");
        boolean laUrrlTinhMethodGet = false;
        if (theBaiTaiNguyenTinh != null) {
            if (theBaiTaiNguyenTinh == true) {
                laUrrlTinhMethodGet = true;
            }
        }

        if (laUrlCongKhai == true || laUrrlTinhMethodGet == true) {
            // Đánh dấu để Advice và Interceptor biết đây là API Public, không cần xét nét
            // BodyHash
            request.setAttribute("EXPECTED_BODY_HASH", "API_CONG_KHAI");
            request.setAttribute("FORMDATA_CHECKED", true);
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {

            // Lấy ID trực tiếp từ Principal (Hỗ trợ cả trường hợp web dùng Session)
            Long userId = null;
            Long tokenCreatedAt = 0L;
            String fingerprintTrongToken = null;
            boolean tokenThieuFingerprintBaoMat = false;
            String maDnaQuyenHanHienTai = null;

            // NHẬN LẠI DỮ LIỆU ĐÃ GIẢI MÃ TỪ FILTER 1 (Không tốn CPU tính toán lại)
            Claims claimsJwt = (Claims) request.getAttribute("JWT_CLAIMS");
            System.out.println("[DynamicRoleFilter] Nhận token từ JwtAuthenticationFilter: Bước[2/2]");

            if (claimsJwt != null) {
                try {

                    Object userIdObj = claimsJwt.get("userId");
                    if (userIdObj != null) {
                        userId = Long.valueOf(userIdObj.toString());
                        System.out.println("[DynamicRoleFilter] Lấy ID từ jwt token: " + userId);
                    }

                    Date issuedAt = claimsJwt.getIssuedAt();
                    if (issuedAt != null) {
                        tokenCreatedAt = issuedAt.getTime();
                        System.out.println("[DynamicRoleFilter] Lấy time từ jwt token: " + tokenCreatedAt);
                    }

                    Object fingerprintRaw = claimsJwt.get(SecurityTokenConstants.CLAIM_SECURITY_FINGERPRINT);
                    if (fingerprintRaw == null || fingerprintRaw.toString().trim().isEmpty()) {
                        tokenThieuFingerprintBaoMat = true;
                    } else {
                        fingerprintTrongToken = fingerprintRaw.toString().trim();
                    }

                    Integer v_adn = SecurityTokenVersion.docSoNguyen(claimsJwt.get("v_adn"), 0);
                    if (userId != null) {
                        if (tokenThieuFingerprintBaoMat == true) {
                            maDnaQuyenHanHienTai = taoDnaTuTokenDoiCu(userId, claimsJwt.get("roles"), v_adn);
                        } else {
                            maDnaQuyenHanHienTai = SecurityTokenVersion.taoDna(userId, fingerprintTrongToken, v_adn);
                        }
                    }
                    if (maDnaQuyenHanHienTai != null) {
                        // SOÁT XÉT NGHĨA TRANG QUYỀN HẠN BẰNG DNA (CHỐNG PHANTOM CLONE ATTACK)
                        if (maDnaQuyenHanHienTai != null
                                && maTranLuoiLocNghiaTrangQuyenHanCu.kiemTraDaChet(maDnaQuyenHanHienTai) == true) {
                            System.out.println(
                                    "[DynamicRoleFilter] CHẶN LẬP TỨC: Quyền hạn của Token này đã bị khai tử trên toàn cầu!");
                            tuChoiTruyCap(request, response);
                            return;
                        }
                    }

                    // Lấy Client Secret đã được giấu trong JWT
                    String clientSecret = claimsJwt.get("c_secret", String.class);

                    // =================================================================
                    // CƠ CHẾ AUTO-HEALING (CHỮA LÀNH ZOMBIE SESSION)
                    // Trả Secret về cho Frontend để đề phòng khách lỡ tắt trình duyệt
                    // =================================================================
                    if (clientSecret != null) {
                        response.setHeader("X-Client-Secret", clientSecret);
                        // Ép Trình duyệt cho phép Javascript đọc Header 'Date' (Dùng để Sync Time)
                        // Bắt buộc phải có chữ Date, nếu không hệ thống đồng bộ giờ sẽ bị Mù ở
                        // Cross-Origin!
                        response.setHeader("Access-Control-Expose-Headers", "X-Client-Secret, X-New-Token, Date");
                    }

                    // ========================================================================
                    // [TẤM KHIÊN BẢO MẬT] - CHỐNG CSRF & REPLAY ATTACK (KHÔNG TỐN RAM)
                    // ========================================================================
                    String method = request.getMethod();
                    if ("POST".equalsIgnoreCase(method)
                            || "PUT".equalsIgnoreCase(method)
                            || "PATCH".equalsIgnoreCase(method)
                            || "DELETE".equalsIgnoreCase(method)) {

                        // 1: lấy chữ ký gửi lên từ Header
                        String timestampStr = request.getHeader("X-Timestamp");
                        String clientSignature = request.getHeader("X-Signature");
                        String bodyHashHeader = request.getHeader("X-Body-Hash");
                        String targetDomainHeader = request.getHeader("X-Target-Domain");

                        // Xử lý chống nhân đôi Header do Proxy hoặc Client bug
                        // gây ra
                        if (timestampStr != null && timestampStr.contains(",")) {
                            timestampStr = timestampStr.split(",")[0].trim();
                        }
                        if (clientSignature != null && clientSignature.contains(",")) {
                            clientSignature = clientSignature.split(",")[0].trim();
                        }
                        if (bodyHashHeader != null && bodyHashHeader.contains(",")) {
                            bodyHashHeader = bodyHashHeader.split(",")[0].trim();
                        }
                        if (targetDomainHeader != null && targetDomainHeader.contains(",")) {
                            targetDomainHeader = targetDomainHeader.split(",")[0].trim();
                        }

                        // Nếu request thay đổi dữ liệu mà không có chữ ký -> Chém (Chống CSRF form ẩn)
                        if (clientSecret == null || timestampStr == null || clientSignature == null
                                || bodyHashHeader == null || bodyHashHeader.trim().isEmpty()
                                || targetDomainHeader == null || targetDomainHeader.trim().isEmpty()) {
                            System.out.println("[DynamicRoleFilter] Phát hiện truy cập thiếu chữ ký bảo mật!");
                            tuChoiTruyCap(request, response);
                            return;
                        }

                        // 2. FAIL-FAST: Kiểm tra xem Tên miền Client gửi lên có nằm trong Danh sách cho
                        // phép của Server này không?
                        // (Giao toàn quyền quyết định cho Developer cấu hình ở application.properties)
                        if (props.kiemTraDomainHopLe(targetDomainHeader) == false) {
                            System.out.println(
                                    "[DynamicRoleFilter] CHẶN: Gói tin đến từ Tên miền không được Server này cho phép ("
                                            + targetDomainHeader + ")");
                            tuChoiTruyCap(request, response);
                            return;
                        }

                        // 3A. Chống Replay Attack (Giới hạn gói tin chỉ sống 60 giây)
                        long clientTime = Long.parseLong(timestampStr);
                        if (Math.abs(System.currentTimeMillis() - clientTime) > GIOI_HAN_REPLAY_MS) {
                            System.out.println("[DynamicRoleFilter] Gói tin đã quá hạn (Replay Attack)!");
                            tuChoiTruyCap(request, response);
                            return;
                        }

                        // LẤY TRỌN VẸN URL BAO GỒM CẢ QUERY PARAMETERS (?bookId=1&qty=2)
                        String fullUri = request.getRequestURI();
                        if (request.getQueryString() != null) {
                            fullUri += "?" + request.getQueryString();
                        }
                        // 4. Tái tạo chữ ký tại Server: Hash(Method + URI + Timestamp) với chìa khóa là
                        // clientSecret
                        String rawData = method + "|" + targetDomainHeader + "|" + fullUri + "|" + timestampStr + "|"
                                + bodyHashHeader;
                        String serverSignature = generateHmacSHA256(rawData, clientSecret);

                        // 5. So sánh: Chỉ khi Hacker/Client biết được clientSecret mới tạo ra chữ ký
                        // khớp
                        // CHỐNG TIMING ATTACK BẰNG MessageDigest.isEqual
                        byte[] serverSigBytes = serverSignature.toLowerCase().getBytes(StandardCharsets.UTF_8);
                        byte[] clientSigBytes = clientSignature.toLowerCase().getBytes(StandardCharsets.UTF_8);
                        if (!MessageDigest.isEqual(serverSigBytes, clientSigBytes)) {
                            System.out.println("[DynamicRoleFilter] Chữ ký giả mạo!");
                            tuChoiTruyCap(request, response);
                            return;
                        }

                        // =================================================================
                        // 6. LƯỚI LỌC LƯỢNG TỬ
                        // Chỉ những gói tin HỢP LỆ 100% về mặt mật mã học mới được phép đi vào đây
                        // để kiểm tra xem nó đã từng được sử dụng chưa. Tránh bị Hacker bơm rác làm đầy
                        // bộ nhớ!
                        // =================================================================
                        if (maTranLuoiLocChongPhatLai.kiemTraVaGhiNhan(clientSignature, clientTime) == false) {
                            System.err.println("[DynamicRoleFilter] TỬ HÌNH GÓI TIN: Phát hiện nỗ lực Replay Attack!");
                            tuChoiTruyCap(request, response);
                            return;
                        }

                        // =========================================================================
                        // [ĐÓN ĐẦU NGỰA GỖ] - ĐỌC LỆNH ĐỒNG BỘ ẨN BẰNG PAYLOAD ĐỒNG NHẤT
                        // =========================================================================
                        String syncPayload = claimsJwt.get("sync_payload", String.class);

                        if (syncPayload != null && syncPayload.isEmpty() == false) {
                            String[] parts = syncPayload.split("\\|\\|\\|");

                            if (parts.length >= 3) {
                                String syncCommand = parts[0];
                                String syncData = parts[1];
                                long thoiDiemGoc = Long.parseLong(parts[2]);

                                // =======================================================
                                // [TRIỆT TIÊU BÃO MẠNG O(1)] - DÙNG CHÍNH PAYLOAD LÀM ĐỊNH DANH
                                // Đây là lưới lọc thứ 2 vì sao nó khác với lưới lọc chống phát lại ở trên?
                                // vì mỗi lần có lệnh chuyển tiếp sẽ sinh url mới -> rawData mới ->
                                // chuKyNguyTrang mới
                                // mà chuKyNguyTrang chính là X-Signature khi = clientSignature thứ duy nhất giữ
                                // nguyên
                                // dù chuyển tiếp bao cụm là syncPayload và thoiDiemGoc
                                // Gói tin đồng bộ quyền hạn phải có định dạng chuẩn:
                                // "LOAI_LENH|||DU_LIEU|||THOI_DIEM_GOC"
                                // =======================================================
                                if (maTranLuoiLocChongPhatLai.kiemTraVaGhiNhan(syncPayload, thoiDiemGoc) == false) {
                                    System.out.println(
                                            "[Trojan-Sync] Đã phát hiện gói tin hồi quy (Bão Mạng). Hủy diệt thành công!");
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    return; // KHÔNG THỰC THI, KHÔNG CHUYỂN TIẾP NỮA!
                                }

                                // Thực thi trực tiếp trên RAM O(1)
                                if ("ROLE_KILL".equals(syncCommand) == true) {
                                    maTranLuoiLocNghiaTrangQuyenHanCu.chonCat(syncData);
                                    tramPhatSong.phatLenhTruyNaDnaQuyenUDPVaToanCau(syncData, thoiDiemGoc);
                                    System.out.println("[Trojan-Sync] Đã chôn DNA Quyền: " + syncData);
                                } else if ("USER_BAN".equals(syncCommand) == true) {
                                    MaTranNhiPhanNguyenTu.danhDauViPham(Long.valueOf(syncData));
                                    tramPhatSong.phatLenhGanCoUserUDPVaToanCau(Long.valueOf(syncData), thoiDiemGoc);
                                    System.out.println("[Trojan-Sync] Đã cấm cờ User: " + syncData);
                                } else if ("USER_UNBAN".equals(syncCommand) == true) {
                                    MaTranNhiPhanNguyenTu.xoaDauVet(Long.valueOf(syncData));
                                    tramPhatSong.phatLenhXoaCoUserUDPVaToanCau(Long.valueOf(syncData), thoiDiemGoc);
                                    System.out.println("[Trojan-Sync] Đã mở cờ User: " + syncData);
                                }

                                // KHAI TỬ GÓI TIN TẠI ĐÂY!
                                response.setStatus(HttpServletResponse.SC_OK);
                                return;
                            }
                        }

                        request.setAttribute("EXPECTED_BODY_HASH", bodyHashHeader);
                        System.out.println("[DynamicRoleFilter] Xác thực chữ ký điện tử thành công!");
                    }
                } catch (Exception e) {
                    // Lỗi giải mã kệ cho JwtFilter xử lý
                    // CỰC KỲ QUAN TRỌNG: Nếu Token hỏng, XÓA NGAY!
                    System.out.println(
                            "[DynamicRoleFilter] Phát hiện Hacker cố tình gửi Header sai định dạng. Chặn truy cập.");
                    SecurityContextHolder.clearContext();
                    cookieUtils.clearJwtCookie(response);
                    tuChoiTruyCap(request, response);
                    return; // Chặn request đi tiếp!
                }

            }

            System.out.println("[DynamicRoleFilter] Lấy THOI_DIEM_KHOI_DONG server tu Ma Tran nhi phan: "
                    + MaTranNhiPhanNguyenTu.THOI_DIEM_KHOI_DONG);

            // Điều kiện 1: Token được tạo ra TRƯỚC khi Server khởi động (Token từ đời
            // trước, không đáng tin).
            // Điều kiện 2: ID của User này đang nằm trong danh sách đen của Ma Trận RAM Kỷ
            // nguyên này.
            boolean laTokenDoiCu = false;
            if (tokenCreatedAt < MaTranNhiPhanNguyenTu.THOI_DIEM_KHOI_DONG) {
                laTokenDoiCu = true;
            }
            if (tokenThieuFingerprintBaoMat == true) {
                laTokenDoiCu = true;
            }
            // ĐÚNG: kiểm tra null trước
            boolean biAdminDanhDau = false;
            if (userId != null) {
                biAdminDanhDau = MaTranNhiPhanNguyenTu.checkNghiVan(userId);
            }

            // LẤY HỘ CHIẾU LÃNH ĐỊA CỦA GÓI TIN
            String tokenClusterId = claimsJwt.get("clusterId", String.class);
            // KIỂM SOÁT HẢI QUAN (CHỐNG NÃO PHÂN LIỆT XUYÊN LỤC ĐỊA)
            boolean laKeNgoaiBang = false;
            if (tokenClusterId == null || tokenClusterId.equals(tramPhatSong.getClusterId()) == false) {
                System.out.println("[DynamicRoleFilter] Phát hiện Token ngoại bang vượt rào. Kích hoạt soi DB!");
                laKeNgoaiBang = true;
            }

            // 2. SOI MA TRẬN: Chỉ xử lý nếu ID này bị đánh dấu
            // Chân 1 (biAdminDanhDau + UDP): Trị bọn Hacker lách luật bằng Load Balancer
            // trong Cùng 1 Data Center.
            // Chân 2 (laKeNgoaiBang + ClusterID): Trị bọn Hacker xài VPN bay sang Lục địa
            // khác để trốn sóng UDP.
            // Chân 3 (laTokenDoiCu + Thời gian Boot): Trị sự kiện Đứt gãy phần cứng (Server
            // restart mất RAM).
            if (laTokenDoiCu || biAdminDanhDau || laKeNgoaiBang) {

                ISecurityUserAdapter userAdapter = userProvider.timNguoiDungTheoId(userId);

                // --- TRƯỜNG HỢP: BỊ KHÓA HOẶC XÓA TÀI KHOẢN ---
                if (userAdapter == null || userAdapter.kiemTraTaiKhoanBiKhoa() == true) {
                    // Dọn dẹp tàn dư để chống vòng lặp
                    SecurityContextHolder.clearContext();// xóa thông tin xác thực nếu bị khóa
                    cookieUtils.clearJwtCookie(response); // xóa Cookie để hủy JWT
                    MaTranNhiPhanNguyenTu.danhDauViPham(userId);// nội bộ
                    tramPhatSong.phatLenhGanCoUserUDPVaToanCau(userId);// đồng bô
                    if (maDnaQuyenHanHienTai != null) {
                        maTranLuoiLocNghiaTrangQuyenHanCu.chonCat(maDnaQuyenHanHienTai);// nội bộ
                        tramPhatSong.phatLenhTruyNaDnaQuyenUDPVaToanCau(maDnaQuyenHanHienTai);// đồng bô
                    }
                    tuChoiTruyCap(request, response);
                    return; // Chặn đứng Request tại đây
                }

                // TRƯỜNG HỢP: TÀI KHOẢN CÒN SỐNG -> LẤY SNAPSHOT OPAQUE TỪ APP RA ĐỂ ĐỐI SOÁT
                SecurityAuthoritySnapshot snapshotTrongDB = userAdapter.layAnhChupBaoMat();
                String fingerprintTrongDB = snapshotTrongDB.laySecurityFingerprint();
                // THUẬT TOÁN: CÓ THỰC SỰ CẦN CHỮA LÀNH KHÔNG?
                boolean quyenHanBiThayDoi = false;
                if (fingerprintTrongToken == null || fingerprintTrongToken.equals(fingerprintTrongDB) == false) {
                    quyenHanBiThayDoi = true;
                }

                // 1: là đời cũ phải cấp token mới
                // 2: quyền hạn thay đổi cấp token mới
                // 3: là kẻ từ cụm server khác cấp token mới
                // Nếu 1 trong 3 có thay đổi quyền thì chôn quyền cũ vào token
                if (laTokenDoiCu || quyenHanBiThayDoi == true || laKeNgoaiBang) {
                    System.out.println(
                            "[DynamicRoleFilter] Tiến hành Cấp Token Mới -> ");
                    // --- TRƯỜNG HỢP: CHỈ BỊ ĐỔI QUYỀN (KHÔNG KHÓA) ---
                    List<SimpleGrantedAuthority> newAuthorities = new ArrayList<>();
                    List<String> authoritiesTrongDB = snapshotTrongDB.layAuthoritiesChuanHoa();
                    Object[] authoritiesArray = authoritiesTrongDB.toArray();
                    for (int i = 0; i < authoritiesArray.length; i = i + 1) {
                        newAuthorities.add(new SimpleGrantedAuthority(authoritiesArray[i].toString()));
                    }

                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            userAdapter.layTenDangNhap(), auth.getCredentials(), newAuthorities);
                    newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    apDungRequestAttributes(request, snapshotTrongDB);

                    // TỰ CHỮA LÀNH: Cấp Token mới gửi ngầm về Frontend để nhận quyền
                    String oldClientSecret = claimsJwt.get("c_secret", String.class);
                    String newToken = jwtService.generateToken(userAdapter, oldClientSecret);
                    cookieUtils.createJwtCookie(response, newToken);
                    System.out.println("[DynamicRoleFilter] hoàn tất tự chữa lành");
                    // CHÔN CẤT CÁI QUYỀN CŨ NÀY XUỐNG MỘ (Khai tử toàn bộ Token có chung Quyền cũ
                    // này)

                    if (quyenHanBiThayDoi == true && maDnaQuyenHanHienTai != null) {
                        System.out.println(
                                " -> Chôn quyền cũ : " + maDnaQuyenHanHienTai);
                        maTranLuoiLocNghiaTrangQuyenHanCu.chonCat(maDnaQuyenHanHienTai);
                        tramPhatSong.phatLenhTruyNaDnaQuyenUDPVaToanCau(maDnaQuyenHanHienTai); // Bắn đạn Vô Tuyến!
                    }
                    System.out.println(
                            "  -> Xử lý xong . Giải phóng hệ thống!");
                } else {
                    System.out.println(
                            "[DynamicRoleFilter] Quyền hạn KHÔNG ĐỔI. Bỏ qua việc tạo Token mới. Giữ nguyên Token hiện tại.");
                }

                // DÙ LÀ 2A HAY 2B, TA ĐỀU ĐÃ KIỂM TRA XONG -> GIẢI PHÓNG HỆ THỐNG MƯỢT MÀ!
                // nơi khởi nguồn
                MaTranNhiPhanNguyenTu.xoaDauVet(userId);
                tramPhatSong.phatLenhXoaCoUserUDPVaToanCau(userId);

            } else {
                System.out.println("[DynamicRoleFilter] user không bị đánh dấu và không là token trước khi server sập");
            }

        } else {
            System.out.println("[DynamicRoleFilter] chưa đăng nhập");
        }

        // Đi qua trạm kiểm soát tiếp theo
        filterChain.doFilter(request, response);
    }

    // --- HÀM TIỆN ÍCH DÙNG CHUNG TRONG CLASS ---

    private void apDungRequestAttributes(HttpServletRequest request, SecurityAuthoritySnapshot snapshot) {
        List<SecurityTokenClaim> claimsBoSung = snapshot.layClaimsBoSung();
        Object[] mangClaim = claimsBoSung.toArray();
        for (int i = 0; i < mangClaim.length; i = i + 1) {
            SecurityTokenClaim claim = (SecurityTokenClaim) mangClaim[i];
            if (claim.isExposeAsRequestAttribute() == true) {
                request.setAttribute(claim.getClaimName(), chuyenClaimThanhRequestValue(claim));
            }
        }
    }

    // Hàm tạo chữ ký HMAC-SHA256 (Tương tự VNPay)
    private Object chuyenClaimThanhRequestValue(SecurityTokenClaim claim) {
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

    private String taoDnaTuTokenDoiCu(Long userId, Object rolesRaw, Integer vAdn) {
        List<String> roles = layDanhSachChuoiTuClaim(rolesRaw);
        if (roles.isEmpty() == true) {
            return null;
        }
        return userId + "|" + roles.toString() + "|v" + vAdn;
    }

    private List<String> layDanhSachChuoiTuClaim(Object rawValue) {
        List<String> ketQua = new ArrayList<>();
        if (rawValue instanceof List<?> danhSachRaw) {
            Object[] mangRaw = danhSachRaw.toArray();
            for (int i = 0; i < mangRaw.length; i = i + 1) {
                themChuoiNeuHopLe(ketQua, mangRaw[i]);
            }
            return ketQua;
        }
        themChuoiNeuHopLe(ketQua, rawValue);
        return ketQua;
    }

    private void themChuoiNeuHopLe(List<String> ketQua, Object rawValue) {
        if (rawValue == null) {
            return;
        }
        String value = rawValue.toString().trim();
        if (value.isEmpty() == false && ketQua.contains(value) == false) {
            ketQua.add(value);
        }
    }

    private String generateHmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký server", e);
        }
    }

    // Hàm từ chối truy cập (Chuẩn hóa trả về 401 thay vì đá về trang login)
    private void tuChoiTruyCap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Phân biệt request từ AJAX hay request load trang bình thường
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith) || request.getRequestURI().startsWith("/api/")) {
            // Nếu là AJAX: Trả về lỗi 401.
            // Đoạn JS (ajaxError) ở admin_layout.html sẽ tự bắt lỗi này và chuyển trang
            // hiển thị Toast.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"Phiên bản bảo mật không hợp lệ hoặc bị từ chối.\"}");
        } else {
            // Nếu tải trang trực tiếp: Chuyển hướng bình thường
            response.sendRedirect(this.urlChuyenHuongKhiKhoa);
        }
    }
}
