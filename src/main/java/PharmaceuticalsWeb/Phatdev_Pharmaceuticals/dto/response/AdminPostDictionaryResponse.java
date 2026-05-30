//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/AdminPostDictionaryResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO gom danh mục tham chiếu mà trang quản trị bài viết cần.
 * Frontend không tự hardcode mã quyền, chỉ hiển thị danh sách backend cho phép.
 */
@Getter
@Setter
public class AdminPostDictionaryResponse {

    /** Danh sách nhóm quyền (USER_ROLES) để populate dropdown Cấp độ truy cập */
    private List<RoleOptionResponse> roles;
}
