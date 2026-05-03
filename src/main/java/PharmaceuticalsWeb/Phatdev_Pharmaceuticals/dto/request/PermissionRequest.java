//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/PermissionRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import lombok.Data;

/**
 * =========================================================================
 * DTO: HỨNG DỮ LIỆU TẠO/SỬA QUYỀN HẠT LỰU
 * =========================================================================
 * Khớp với form giao diện khi Admin muốn đẻ ra một nghiệp vụ mới.
 */
@Data
public class PermissionRequest {
    
    // Mã quyền thao tác (VD: XOA_BINH_LUAN, DUYET_DON_HANG)
    private String permissionCode;
    
    // Diễn giải chi tiết cho Admin dễ hiểu
    private String description;
}