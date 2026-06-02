//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/utils/SecurityConfig.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.utils;

import jakarta.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.adapter.UserSecurityAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.SecurityLibraryProperties;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.AutoConfig.SecurityConfigurer;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.CookieUtils;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.JwtService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.CustomOidcUserService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;


@Configuration
public class SecurityConfig {

        private final IUserService userService;
        private final CustomOidcUserService customOidcUserService;
        private final SecurityConfigurer securityConfigurer;
        private final JwtService jwtService;
        private final CookieUtils cookieUtils;
        private final SecurityLibraryProperties thuVienProps;

        private final String tenCookieAuth;

        // Constructor
        @Autowired
        public SecurityConfig(
                        IUserService userService,
                        CustomOidcUserService customOidcUserService,
                        SecurityConfigurer securityConfigurer,
                        JwtService jwtService,
                        CookieUtils cookieUtils,
                        SecurityLibraryProperties thuVienProps) {

                this.userService = userService;
                this.customOidcUserService = customOidcUserService;
                this.securityConfigurer = securityConfigurer;
                this.jwtService = jwtService;
                this.cookieUtils = cookieUtils;
                this.thuVienProps = thuVienProps;

                this.tenCookieAuth = thuVienProps.getCookieName();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Tích hợp Lõi nguyên tử bảo mật của thư viện
                                .with(securityConfigurer, customizer -> {
                                })

                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // CÁC TRANG PUBLIC KHÔNG CẦN LOGIN
                                                .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**",
                                                                "/login", "/login?**", "/register/**",
                                                                "/forgot-password/**", "/reset-password",
                                                                "/oauth2/**", "/api/auth/**", "/search", "/error")
                                                .permitAll()

                                                // Trang bài viết và sự kiện public (ai cũng xem được)
                                                .requestMatchers("/posts", "/posts/**", "/events", "/events/**")
                                                .permitAll()

                                                // API public cho bài viết, sự kiện, bình luận
                                                .requestMatchers("/api/posts", "/api/posts/**",
                                                                "/api/events", "/api/events/**",
                                                                "/api/comments/posts/**",
                                                                "/api/comments/events/**",
                                                                "/api/comments/reaction-types")
                                                .permitAll()

                                                // PHÂN QUYỀN ĐỘNG: Mọi route admin và API đều chỉ cần xác thực.
                                                // Quyền cụ thể do PermissionInterceptor kiểm tra từ DB.
                                                .requestMatchers("/admin/**").authenticated()
                                                .requestMatchers("/api/admin/**").authenticated()
                                                .requestMatchers("/api/profile/**").authenticated()
                                                .requestMatchers("/api/**").authenticated()

                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .permitAll())

                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                // SỬ DỤNG SERVICE MỚI TẠI ĐÂY
                                                                .oidcUserService(customOidcUserService))
                                                .successHandler((request, response, authentication) -> {
                                                        // Lấy email từ principal Google
                                                        String email = ((OidcUser) authentication
                                                                        .getPrincipal()).getEmail();

                                                        // Lấy User và tạo Token
                                                        User dbUser = userService.findByEmail(email);
                                                        if (dbUser.isLocked()) {
                                                                SecurityContextHolder.clearContext();

                                                                // Nếu DB báo khóa, không cấp Token, đá văng ra login
                                                                // kèm cảnh báo
                                                                response.sendRedirect("/login?locked");
                                                                return; // Dừng ngay lập tức
                                                        }

                                                        // Tạo token jwt
                                                        String clientSecret = java.util.UUID.randomUUID().toString()
                                                                        .replace("-", "");

                                                        ISecurityUserAdapter userAdapter = new UserSecurityAdapter(
                                                                        dbUser);

                                                        String token = jwtService.generateToken(userAdapter,
                                                                        clientSecret);

                                                        // nhet token vao cookie
                                                        cookieUtils.createJwtCookie(response, token);

                                                        // KÝ GỬI CHÌA KHÓA QUA COOKIE TẠM (JS CÓ THỂ ĐỌC)
                                                        Cookie secretCookie = new Cookie("temp_secret", clientSecret);
                                                        secretCookie.setPath("/");
                                                        secretCookie.setMaxAge(60); // Chỉ sống 60 giây
                                                        response.addCookie(secretCookie);

                                                        // Redirect bình thường (Clean Redirect)
                                                        response.sendRedirect("/");
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        System.err.println("[ERROR OAuth2] Login Google thất bại: "
                                                                        + exception.getMessage());
                                                        response.sendRedirect("/login?error");
                                                }))
                                .logout(logout -> logout

                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)// nếu có session tạm tạo ngầm từ google thì
                                                                            // xoá
                                                .clearAuthentication(true)// xoá thông tin xác thực nếu bị google tạo
                                                                          // ngầm
                                                .deleteCookies("JSESSIONID", this.tenCookieAuth)// xoá cookie

                                                .permitAll())
                                .rememberMe(remember -> remember
                                                .key("uniqueKey")
                                                .tokenValiditySeconds(86400));

                return http.build();
        }

}