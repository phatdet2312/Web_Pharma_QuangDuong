//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminEventDictionaryResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO gom các danh mục trạng thái mà trang quản trị sự kiện cần để render select.
 * Frontend không tự hardcode mã nghiệp vụ, chỉ hiển thị danh sách backend cho phép.
 */
@Getter
@Setter
public class AdminEventDictionaryResponse {

    /** Danh sách trạng thái vòng đời phiên sự kiện. */
    private List<StatusOptionResponse> eventStatuses;

    /** Danh sách trạng thái hồ sơ đăng ký tham dự. */
    private List<StatusOptionResponse> registrationStatuses;
}
