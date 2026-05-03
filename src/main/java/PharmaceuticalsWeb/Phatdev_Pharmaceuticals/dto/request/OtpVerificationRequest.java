//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/OtpVerificationRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email; 
    
    @NotBlank(message = "Mã OTP không được để trống")
    private String code;
}