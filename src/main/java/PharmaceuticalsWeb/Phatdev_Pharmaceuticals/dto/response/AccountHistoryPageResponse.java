//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AccountHistoryPageResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.*;
import java.util.List;

/**
 * Wrapper phân trang cho lịch sử hành động admin trên tài khoản (CT_USER_MODERATION_LOG).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryPageResponse {
    private List<AccountHistoryResponse> danhSach;
    private boolean conTrangTiepTheo;
    /** Tổng số lần bị khóa trong toàn bộ lịch sử — Risk Indicator */
    private long tongSoLanBiKhoa;
}
