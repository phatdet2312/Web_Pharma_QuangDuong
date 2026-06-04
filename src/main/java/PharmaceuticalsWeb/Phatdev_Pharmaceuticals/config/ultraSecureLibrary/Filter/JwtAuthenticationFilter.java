//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/Filter/JwtAuthenticationFilter.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.CookieUtils;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.JwtService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.PhienBanPhanQuyenBaoMat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final SecurityLibraryProperties props;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String phuongThuc = request.getMethod();

    
        // =====================================================================
        // CHẶN TUYỆT ĐỐI TÀI NGUYÊN TĨNH
        // Trình duyệt luôn gửi kèm Cookie khi tải Ảnh/CSS/JS.
        // Nếu không chặn, CPU sẽ phải giải mã JWT hàng ngàn lần vô ích gây DoS.
        // Chỉ bỏ qua nếu là lệnh GET. Nếu Hacker dùng POST/PUT bơm file 
        // mạo danh thư mục tĩnh, không được phép bỏ qua!
        // KIỂM TRA URL DUY NHẤT 1 LẦN VÀ GẮN THẺ BÀI CHO CÁC TẦNG SAU
        // Tránh hao phí CPU do gọi hàm lặp lại ở DynamicRoleFilter
        // =====================================================================
        boolean laUrlTinh = props.kiemTraLaUrlTinh(requestUri);


        boolean laLenhGet = false;
        if ("GET".equalsIgnoreCase(phuongThuc) == true) {
            laLenhGet = true;
        }
        boolean choPhepBoQuaTaiNguyenTinh = false;
        if (laUrlTinh == true) {
           if (laLenhGet == true) {
                choPhepBoQuaTaiNguyenTinh = true;
            }
        }
        
        request.setAttribute("LA_URL_TINH_METHOD_GET", choPhepBoQuaTaiNguyenTinh);
        
        if (choPhepBoQuaTaiNguyenTinh == true) {
            filterChain.doFilter(request, response);
            return; // Thoát ngay, bảo vệ CPU
        }

        // =====================================================================
        // CHỐNG RÁC LOG CHO URL CÔNG KHAI
        // URL Công khai (như Trang chủ) vẫn CẦN giải mã JWT để hiển thị tên User trên Navbar.
        // Nhưng ta sẽ tắt lệnh in Log để Console không bị trôi đi mất kiểm soát.
        // =====================================================================
        boolean laUrlCongKhai = props.kiemTraLaUrlCongKhai(requestUri);

        request.setAttribute("LA_URL_CONG_KHAI", laUrlCongKhai);

        if (laUrlCongKhai == false) {
            System.out.println("[JwtAuthenticationFilter] Request bảo mật: " + requestUri);
        }

        // LẤY TOKEN TỪ COOKIE
        final String jwt = cookieUtils.getJwtFromRequest(request);

        if (jwt == null) {
            if (laUrlCongKhai == false) {
                System.out.println("[JwtAuthenticationFilter] Không có token → tiếp tục");
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // GIẢI MÃ TOKEN: Nếu Token bị sửa bậy hoặc hết hạn, nó sẽ văng Exception ngay
            // lập tức ở dòng này.
            // Do đó, nếu chạy trót lọt qua dòng này, Token chắc chắn an toàn 100%.
            // Kiểm tra xem luồng chính đã giải mã JWT trước đó chưa. 
            // Nếu có rồi thì lấy thẳng từ RAM (O(1)), cấm dùng CPU giải mã lại!
            Claims claims = (Claims) request.getAttribute("JWT_CLAIMS");
            if (claims == null) {
                // Giải mã lần đầu tiên (Tốn CPU)
                claims = jwtService.extractAllClaims(jwt);
                request.setAttribute("JWT_CLAIMS", claims); 
            } else {
                // Luồng Async quay về (Không tốn CPU)
                System.out.println("[JwtAuthenticationFilter] Luồng Async tái sử dụng RAM O(1). Không giải mã lại JWT.");
            }
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<?> rolesRaw = claims.get(PhienBanPhanQuyenBaoMat.CLAIM_ROLES, List.class);
                List<?> permissionsRaw = claims.get(PhienBanPhanQuyenBaoMat.CLAIM_PERMISSIONS, List.class);
                List<String> roles = PhienBanPhanQuyenBaoMat.chuanHoaDanhSach(rolesRaw);
                List<String> permissions = PhienBanPhanQuyenBaoMat.chuanHoaDanhSach(permissionsRaw);
                Integer roleLevel = PhienBanPhanQuyenBaoMat.docSoNguyen(
                        claims.get(PhienBanPhanQuyenBaoMat.CLAIM_ROLE_LEVEL), null);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                request.setAttribute(PhienBanPhanQuyenBaoMat.CLAIM_ROLES, roles);
                request.setAttribute(PhienBanPhanQuyenBaoMat.CLAIM_PERMISSIONS, permissions);
                if (roleLevel != null) {
                    request.setAttribute(PhienBanPhanQuyenBaoMat.ATTR_ROLE_LEVEL, roleLevel);
                }

                if (roles != null) {
                    // Chuyển List thành Array để dùng vòng lặp for i (Phong cách Code 1)
                    Object[] rolesArray = roles.toArray();

                    for (int i = 0; i < rolesArray.length; i++) {
                        // 1. Lấy giá trị từ mảng và chuyển sang String
                        String rawRole = rolesArray[i].toString();

                        // 2. Loại bỏ tiền tố "ROLE_" nếu nó đã tồn tại (để tránh bị trùng lặp)
                        String cleanRole = rawRole.replace("ROLE_", "");

                        // 3. Thêm tiền tố "ROLE_" chuẩn và đưa vào danh sách authorities
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRole));

                    }
                }

                Object[] permissionsArray = permissions.toArray();
                for (int i = 0; i < permissionsArray.length; i = i + 1) {
                    authorities.add(new SimpleGrantedAuthority(permissionsArray[i].toString()));
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                request.setAttribute("RAW_JWT_TOKEN", jwt);
                System.out.println(
                        "[JwtAuthenticationFilter] Cấp quyền tạm và  đồng bộ kiểu dữ liệu Principal toàn hệ thống thành công [Bươc 1/2]");

            }
        } catch (Exception e) {
            // Token hết hạn hoặc sai chữ ký thì bỏ qua, coi như chưa đăng nhập
            cookieUtils.clearJwtCookie(response);// xoá token khỏi cookie
            SecurityContextHolder.clearContext();//
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false; // BẮT BUỘC CHẠY LẠI ĐỂ ĐỌC TOKEN VÀ GẮN LẠI QUYỀN TRƯỚC KHI ĐÓNG GÓI TIN!
    }

    // =========================================================================
    //ÉP FILTER CHẠY LẠI KHI CÓ LỖI ĐỂ TRÌNH DUYỆT KHÔNG BỊ "ĐĂNG XUẤT ẢO"
    // =========================================================================
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false; // Trả về false để bắt Spring Security nạp lại JWT khi render trang /error
    }
}
