//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminPostMediaResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO kết quả upload ảnh/file trong phân hệ quản trị bài viết.
 * Trả URL public tương đối để frontend lưu vào PostRequest.thumbnailUrl.
 */
@Getter
@Setter
public class AdminPostMediaResponse {

    /** Đường dẫn public qua /uploads/** */
    private String url;

    /** Tên file sau khi chuẩn hóa */
    private String fileName;
}
