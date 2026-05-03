
//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/controller/view/AuthViewController.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/register/verify-otp")
    public String verifyRegisterOtp() {
        return "auth/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/forgot-password/verify-otp")
    public String verifyForgotOtp() {
        return "auth/verify-otp";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "auth/reset-password";
    }
}