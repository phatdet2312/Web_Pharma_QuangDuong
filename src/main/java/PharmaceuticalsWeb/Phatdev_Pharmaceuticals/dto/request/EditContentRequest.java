package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditContentRequest {

    @NotBlank(message = "Nội dung chỉnh sửa không được để trống")
    private String content;
}
