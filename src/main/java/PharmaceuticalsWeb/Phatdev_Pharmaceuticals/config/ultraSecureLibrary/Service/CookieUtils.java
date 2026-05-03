//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/CookieUtils.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service;

import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtils {

    private final String tenCookieJwt;
    private final boolean cookieSecure;
    private final String cookiePath;

    // Lấy cấu hình bảo mật Cookie từ Properties chung của Thư viện
   public CookieUtils(SecurityLibraryProperties thuVienProps) {
        this.tenCookieJwt = thuVienProps.getCookieName();
        this.cookieSecure = thuVienProps.isCookieSecure();
        this.cookiePath = thuVienProps.getCookiePath();
    }
    

    // 1. Hàm tạo HttpOnly Cookie chứa Token (Bảo mật, JS không đọc được)
    public void createJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(tenCookieJwt, token);
        cookie.setHttpOnly(true); // BẮT BUỘC: Ngăn chặn XSS đánh cắp Token
        cookie.setSecure(cookieSecure); // Sẽ đổi thành true nếu bạn chạy HTTPS (SSL)
        cookie.setPath(cookiePath); // Áp dụng cho toàn bộ Website
        cookie.setMaxAge(7 * 24 * 60 * 60); // Sống 7 ngày (trùng với hạn của JWT)
        response.addCookie(cookie);
        System.out.println("[cookieUtils] hoàn tất tạo cookie");
    }

    // 2. Hàm xóa Cookie (Dùng khi Đăng xuất hoặc bị Khóa)
    public void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(tenCookieJwt, null);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(0); // Cho chết ngay lập tức
        response.addCookie(cookie);
        System.out.println("[cookieUtils] Token bị xoá khỏi cookie");
    }

    // 3. Hàm lấy JWT từ Request
    public String getJwtFromRequest(HttpServletRequest request) {
        // Đọc từ Cookie trước
        String token = null;
        if (request.getCookies() != null) {
            Cookie[] danhSachCookie = request.getCookies();
            for (int i = 0; i < danhSachCookie.length; i = i + 1) {
                Cookie cookie = danhSachCookie[i];
                if (tenCookieJwt.equals(cookie.getName()) == true) {
                    token = cookie.getValue();
                    System.out.println("[cookieUtils] Token từ cookie: " + token);
                    return token;
                }
            }
        }
        // Dự phòng: Nếu API gọi từ thiết bị ngoài (Mobile App, Postman) truyền qua
        token = request.getHeader("Authorization");
        System.out.println("[cookieUtils] Token từ header: " + token);
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return null;
    }
}
