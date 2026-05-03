//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/AutoConfig/SecurityConfigurer.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.AutoConfig;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.BodyIntegrityFilter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.DynamicRoleFilter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.JwtAuthenticationFilter;

@Component
public class SecurityConfigurer extends AbstractHttpConfigurer<SecurityConfigurer, HttpSecurity> {

    private final JwtAuthenticationFilter jwtFilter;
    private final DynamicRoleFilter dynamicRoleFilter;
    private final BodyIntegrityFilter bodyIntegrityFilter;

    public SecurityConfigurer(
            JwtAuthenticationFilter jwtFilter,
            DynamicRoleFilter dynamicRoleFilter,
            BodyIntegrityFilter bodyIntegrityFilter) {
        this.jwtFilter = jwtFilter;
        this.dynamicRoleFilter = dynamicRoleFilter;
        this.bodyIntegrityFilter = bodyIntegrityFilter;
    }

    
    // =====================================================================
    // KHÓA CHẾT THỨ TỰ FILTER TẠI ĐÂY (DEV BÊN NGOÀI KHÔNG THỂ CAN THIỆP)
    // 1. JWT -> 2. Role/Sign -> 3. Body Integrity
    // =====================================================================
    @Override
    public void configure(HttpSecurity http) {
        
        // 1. Giải mã JWT và lấy Secret Key
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 2. Xác thực chữ ký HMAC và cấp quyền (Chạy sau JWT)
        http.addFilterAfter(dynamicRoleFilter, JwtAuthenticationFilter.class);
        
        // 3. Ống nước băm Body (BẮT BUỘC CHẠY SAU CÙNG vì cần EXPECTED_BODY_HASH từ bước 2)
       http.addFilterAfter(bodyIntegrityFilter, DynamicRoleFilter.class);

        System.out.println("[UltraSecureLibrary] Đã niêm phong thứ tự chuỗi phòng ngự SecurityFilterChain.");
    }
}