//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/AutoConfig/LibraryWebMvcAutoConfig.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.AutoConfig;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.FormDataHashCheckerInterceptor;

/**
 * LỚP TỰ ĐỘNG CẤU HÌNH INTERCEPTOR (NẰM BÊN TRONG THƯ VIỆN)
 * Dev sử dụng thư viện KHÔNG CẦN phải tự addInterceptor thủ công nữa.
 */
@Configuration
public class LibraryWebMvcAutoConfig implements WebMvcConfigurer {

    private final FormDataHashCheckerInterceptor formDataHashCheckerInterceptor;
    private final SecurityLibraryProperties props;

    public LibraryWebMvcAutoConfig(FormDataHashCheckerInterceptor formDataHashCheckerInterceptor, SecurityLibraryProperties props) {
        this.formDataHashCheckerInterceptor = formDataHashCheckerInterceptor;
        this.props = props;
    }


    // =========================================================================
    // [CẤP KIM BÀI MIỄN TỬ CHO LUỒNG STREAMING]
    // Spring Boot mặc định chém luồng Async sau 30 giây. Ta phải nới lỏng ra!
    // =========================================================================
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Nâng thời gian sống tối đa của Streaming lên 10 phút (600,000 mili-giây)
        // Cho phép AI tha hồ suy nghĩ mà không bị đứt gãy mạng!
        configurer.setDefaultTimeout(600000L);
    }
    

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> paths = props.getProtectedUrls();
        
        // Vòng lặp for truyền thống để đăng ký interceptor
        for (int i = 0; i < paths.size(); i = i + 1) {
            String path = paths.get(i);
            registry.addInterceptor(formDataHashCheckerInterceptor).addPathPatterns(path);
        }
                
        System.out.println("[Thư Viện Bảo Mật] Đã tự động cài đặt FormDataHashCheckerInterceptor thành công.");
    }
}