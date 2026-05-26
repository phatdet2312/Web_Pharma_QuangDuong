//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/StatusOptionResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO mô tả một mã trạng thái được phép sử dụng trong UI quản trị.
 * code là giá trị gửi về API, label là nhãn hiển thị cho người vận hành.
 */
@Getter
@Setter
public class StatusOptionResponse {

    /** Mã trạng thái ổn định dùng trong request/response JSON. */
    private String code;

    /** Nhãn tiếng Việt phục vụ giao diện quản trị. */
    private String label;
}
