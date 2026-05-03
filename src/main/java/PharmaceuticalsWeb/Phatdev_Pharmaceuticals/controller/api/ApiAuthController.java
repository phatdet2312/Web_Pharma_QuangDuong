//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/api/ApiAuthController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.adapter.UserSecurityAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Adapter.ISecurityUserAdapter;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.CookieUtils;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.config.ultraSecureLibrary.Service.JwtService;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request.*;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response.ApiResponse;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities.User;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.exception.AppException;
import PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.itf.IUserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    req.getUsernameOrEmail(), req.getPassword());

            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = (User) auth.getPrincipal();
            ISecurityUserAdapter userAdapter = new UserSecurityAdapter(user);

            String clientSecret = java.util.UUID.randomUUID().toString().replace("-", "");
            String jwtToken = jwtService.generateToken(userAdapter, clientSecret);
            cookieUtils.createJwtCookie(response, jwtToken);

            Map<String, Object> data = Map.of(
                    "clientSecret", clientSecret,
                    "token", jwtToken,
                    "redirectUrl", "/"
            );

            return ApiResponse.thanhCong(data, "Đăng nhập thành công! Đang chuyển hướng...");

        } catch (BadCredentialsException e) {
            throw new AppException(401, "Sai tên đăng nhập hoặc mật khẩu");
        } catch (LockedException e) {
            throw new AppException(403, "Tài khoản của bạn đã bị khóa!");
        } catch (DisabledException e) {
            throw new AppException(403, "Tài khoản bị vô hiệu hóa!");
        }
    }

    @PostMapping("/register/send-otp")
    public ApiResponse<String> sendRegisterOtp(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new AppException(400, "Mật khẩu xác nhận không khớp");
        }

        RegisterTemp temp = new RegisterTemp(
                req.getFullName(), req.getUsername(), req.getEmail(),
                req.getPassword(), req.getPhone(), req.getAddress(), req.getBirthDate()
        );
        session.setAttribute("registerTemp", temp);

        userService.sendOtp(req.getEmail(), "register");
        return ApiResponse.thanhCong(null, "Mã OTP đã gửi thành công!");
    }

    @PostMapping("/register/verify-otp")
    public ApiResponse<String> verifyRegisterOtp(@Valid @RequestBody OtpVerificationRequest req, HttpSession session) {
        RegisterTemp temp = (RegisterTemp) session.getAttribute("registerTemp");
        if (temp == null) {
            throw new AppException(400, "Phiên đăng ký hết hạn, vui lòng đăng ký lại");
        }

        if (!userService.verifyOtp(temp.getEmail(), req.getCode())) {
            throw new AppException(400, "Mã OTP sai hoặc hết hạn");
        }

        userService.registerLocalUser(
                temp.getFullName(), temp.getUsername(), temp.getEmail(),
                temp.getPassword(), temp.getPhone(), temp.getBirthDate(), temp.getAddress()
        );
        session.removeAttribute("registerTemp");
        
        return ApiResponse.thanhCong(null, "Đăng ký thành công!");
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<String> sendForgotOtp(@Valid @RequestBody ForgotPasswordRequest req, HttpSession session) {
        if (userService.findByEmail(req.getEmail()) == null) {
            throw new AppException(404, "Email không tồn tại trong hệ thống");
        }

        session.setAttribute("resetEmail", req.getEmail());
        userService.sendOtp(req.getEmail(), "forgot");
        return ApiResponse.thanhCong(null, "Mã OTP đã gửi thành công!");
    }

    @PostMapping("/forgot-password/verify-otp")
    public ApiResponse<String> verifyForgotOtp(@Valid @RequestBody OtpVerificationRequest req, HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            throw new AppException(400, "Session hết hạn, vui lòng thử lại");
        }

        if (!userService.verifyOtp(email, req.getCode())) {
            throw new AppException(400, "Mã OTP sai hoặc hết hạn");
        }

        session.setAttribute("resetToken", "valid");
        return ApiResponse.thanhCong(null, "Xác thực thành công!");
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest req, HttpSession session) {
        if (session.getAttribute("resetToken") == null) {
            throw new AppException(403, "Yêu cầu không hợp lệ");
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new AppException(400, "Mật khẩu xác nhận không khớp");
        }

        String email = (String) session.getAttribute("resetEmail");
        userService.updatePassword(email, req.getPassword());
        session.invalidate();
        
        return ApiResponse.thanhCong(null, "Đổi mật khẩu thành công!");
    }
}