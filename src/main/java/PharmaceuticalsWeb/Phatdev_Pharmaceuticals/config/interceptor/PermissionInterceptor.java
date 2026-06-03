//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/interceptor/PermissionInterceptor.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.interceptor;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.validators.annotations.RequirePermission;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;

/**
 * =========================================================================
 * INTERCEPTOR PHÂN QUYỀN ĐỘNG: KIỂM TRA QUYỀN HẠT LỰU TRƯỚC MỖI ENDPOINT
 * =========================================================================
 * Đọc annotation @RequirePermission trên controller method.
 * Nếu có → kiểm tra user hiện tại có quyền tương ứng trong GrantedAuthority.
 * SUPERADMIN được nhận diện bằng roleLevel do backend tính từ DB.
 * Backend là nguồn sự thật cấp bậc; frontend và tên role không quyết định bypass.
 * Method không có annotation → cho qua (backward compatible).
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final IUserService userService;

    /**
     * Kiểm tra quyền trước khi controller method được thực thi.
     * Trả về true nếu cho phép, ném AppException(403) nếu từ chối.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Chỉ xử lý khi handler là method controller (bỏ qua resource handler)
        if ((handler instanceof HandlerMethod) == false) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Đọc annotation @RequirePermission trên method
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // Không có annotation → không kiểm tra quyền → cho qua (backward compatible)
        if (annotation == null) {
            return true;
        }

        // Lấy mã quyền cần kiểm tra từ annotation
        String permissionCanThiet = annotation.value();

        // Lấy thông tin xác thực của user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.isAuthenticated() == false || authentication instanceof AnonymousAuthenticationToken) {
            throw new AppException(401, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
        }

        User currentUser = userService.getCurrentAuthenticatedUser();

        // Lấy danh sách quyền đã được nạp vào GrantedAuthority (bởi napQuyenChoNguoiDung)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Kiểm tra SUPERADMIN — bypass mọi kiểm tra quyền (GOD MODE)
        // Nguồn sự thật là roleLevel từ backend, không phụ thuộc tên role hay frontend.
        int roleLevel = userService.layCapBacQuyenLucCaoNhat(currentUser);
        if (roleLevel == 0) {
            return true;
        }

        Object[] authorityArray = authorities.toArray();

        // Kiểm tra user có quyền thao tác được yêu cầu hay không
        boolean coQuyen = false;
        for (int i = 0; i < authorityArray.length; i = i + 1) {
            GrantedAuthority ga = (GrantedAuthority) authorityArray[i];
            if (permissionCanThiet.equals(ga.getAuthority())) {
                coQuyen = true;
                break;
            }
        }

        if (coQuyen == false) {
            throw new AppException(403, "Bạn không có quyền thực hiện thao tác này (" + permissionCanThiet + ")");
        }

        return true;
    }
}
