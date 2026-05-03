//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/LoginHistoryPageResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.util.List;

/**
 * Wrapper phân trang cho lịch sử đăng nhập.
 * Frontend dùng conTrangTiepTheo để biết có nên hiển thị nút "Xem thêm" không.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryPageResponse {
    private List<LoginHistoryResponse> danhSach;
    private boolean conTrangTiepTheo;
}
