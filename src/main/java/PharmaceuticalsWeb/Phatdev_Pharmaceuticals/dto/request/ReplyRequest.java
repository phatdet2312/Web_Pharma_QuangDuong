//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/ReplyRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request gửi phản hồi (reply) cho một bình luận gốc hoặc reply khác.
 */
@Getter
@Setter
public class ReplyRequest {

    /** Comment gốc mà reply này thuộc về (FK → CMT) */
    @NotNull(message = "ID comment gốc không được để trống")
    private Long rootCmtId;

    /**
     * ID của reply cha (nếu là reply lồng nhau).
     * null = phản hồi trực tiếp comment gốc.
     */
    private Long parentPhId;

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String content;
}
