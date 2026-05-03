//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/OtpHistoryPageResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.util.List;

/**
 * Wrapper phân trang cho lịch sử OTP (democach2 pattern — thay thế Top5 cứng).
 * Cho phép xem nhiều trang lịch sử OTP thay vì bị giới hạn 5 bản ghi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpHistoryPageResponse {
    private List<OtpHistoryResponse> danhSach;
    private boolean conTrangTiepTheo;
}
