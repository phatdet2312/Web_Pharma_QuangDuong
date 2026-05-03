//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/EventSpeakerRequest.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * =========================================================================
 * DTO: YÊU CẦU THÊM/SỬA HỒ SƠ DIỄN GIẢ
 * =========================================================================
 */
@Getter
@Setter
public class EventSpeakerRequest {

    @NotBlank(message = "Tên diễn giả không được để trống")
    @Size(max = 100, message = "Tên diễn giả tối đa 100 ký tự")
    private String fullName;

    @Size(max = 100, message = "Học hàm/Học vị tối đa 100 ký tự")
    private String academicTitle;

    @Size(max = 255, message = "Cơ quan công tác tối đa 255 ký tự")
    private String organization;

    private String avatarUrl;

    private String bio;
}