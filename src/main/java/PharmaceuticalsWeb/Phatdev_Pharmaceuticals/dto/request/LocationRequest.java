//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/LocationRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request tạo mới / cập nhật địa điểm sự kiện.
 */
@Getter
@Setter
public class LocationRequest {

    @NotBlank(message = "Tên địa điểm không được để trống")
    @Size(max = 150, message = "Tên địa điểm tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ hoặc đường dẫn phòng họp không được để trống")
    @Size(max = 500)
    private String address;

    /** true nếu địa điểm tổ chức trực tuyến */
    private boolean isOnline;
}
