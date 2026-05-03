//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/PublicProfileRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * =========================================================================
 * ĐỐI TƯỢNG TRUYỀN TẢI: YÊU CẦU CẬP NHẬT HỒ SƠ CÔNG KHAI
 * =========================================================================
 * Mục đích: Tiếp nhận dữ liệu từ biểu mẫu cài đặt quyền riêng tư của người dùng.
 * Các trường dữ liệu đều cho phép để trống (null) nếu người dùng không muốn cập nhật.
 */
@Getter
@Setter
public class PublicProfileRequest {

    /** Chức danh chuyên môn người dùng tự khai báo */
    @Size(max = 100, message = "Chức danh chuyên môn không được vượt quá 100 ký tự")
    private String professionalTitle;

    /** Tên cơ quan công tác người dùng tự khai báo */
    @Size(max = 255, message = "Nơi công tác không được vượt quá 255 ký tự")
    private String workplace;

    /** Văn bản tự giới thiệu bản thân */
    private String bio;

    /** Đường dẫn ảnh đại diện (Thường được upload qua một API khác rồi gán URL vào đây) */
    @Size(max = 255, message = "Đường dẫn ảnh đại diện không hợp lệ")
    private String avatarUrl;

    /** Quyết định của người dùng về việc bật/tắt hiển thị công khai hồ sơ này */
    private boolean isVisible;
}