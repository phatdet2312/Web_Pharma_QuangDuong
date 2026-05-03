//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/CommentRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request gửi bình luận gốc mới cho bài viết hoặc sự kiện.
 * Hệ thống xác định loại ngữ cảnh (post/event) qua URL path.
 */
@Getter
@Setter
public class CommentRequest {

    /**
     * ID của đối tượng bình luận.
     * Là postId khi gửi đến /api/posts/{id}/comments.
     * Là ctEventId khi gửi đến /api/events/{id}/comments.
     */
    @NotNull(message = "ID đối tượng không được để trống")
    private Long targetId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}
