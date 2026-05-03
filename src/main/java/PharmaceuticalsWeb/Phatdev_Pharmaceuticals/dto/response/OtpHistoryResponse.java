//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/OtpHistoryResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về một bản ghi lịch sử OTP — phục vụ tab Bảo mật trên Profile.
 * Trường code bị ẩn hoàn toàn — chỉ trả về trạng thái và thời gian.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpHistoryResponse {

    private Long id;
    private boolean used;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime expiryAt;
}
