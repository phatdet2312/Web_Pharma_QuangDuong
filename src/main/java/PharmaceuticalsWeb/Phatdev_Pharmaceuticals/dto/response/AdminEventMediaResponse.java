//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminEventMediaResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO kết quả upload ảnh trong phân hệ quản trị sự kiện.
 * Trả URL public tương đối để frontend lưu vào campaign/speaker payload.
 */
@Getter
@Setter
public class AdminEventMediaResponse {

    /** Đường dẫn public được WebMvcConfig phục vụ qua /uploads/**. */
    private String url;

    /** Tên file sau khi backend chuẩn hóa và sinh định danh an toàn. */
    private String fileName;
}
