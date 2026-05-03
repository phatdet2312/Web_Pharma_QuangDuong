//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/config/ultraSecureLibrary/AutoConfig/LibraryFilterRegistryConfig.java   
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.AutoConfig;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.BodyIntegrityFilter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.DynamicRoleFilter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Filter.JwtAuthenticationFilter;

/**
 * LỚP NGĂN CHẶN XUNG ĐỘT VÒNG ĐỜI (DOUBLE EXECUTION)
 * Tắt tính năng tự động đăng ký Filter của Spring Boot Container,
 * Ép các Filter này CHỈ ĐƯỢC PHÉP CHẠY bên trong Spring Security Chain.
 */
@Configuration
public class LibraryFilterRegistryConfig {

    
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableJwtFilter(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Tắt chạy tự động
        registration.setAsyncSupported(true);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<DynamicRoleFilter> disableDynamicRoleFilter(DynamicRoleFilter filter) {
        FilterRegistrationBean<DynamicRoleFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Tắt chạy tự động
        registration.setAsyncSupported(true);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<BodyIntegrityFilter> disableBodyIntegrityFilter(BodyIntegrityFilter filter) {
        FilterRegistrationBean<BodyIntegrityFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Tắt chạy tự động
        registration.setAsyncSupported(true);
        return registration;
    }
        
}