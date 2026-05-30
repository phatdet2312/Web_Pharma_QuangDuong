//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostCommentPreviewResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Preview bình luận của bài viết dùng cho admin/posts chi tiết — tab Bình luận.
 * Content được cắt ngắn tối đa 200 ký tự để tối ưu tải trang.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentPreviewResponse {

    /** ID bình luận gốc (CMT.ID) */
    private Long cmtId;

    /** Tên tác giả bình luận (USER.FULL_NAME) */
    private String authorName;

    /** URL avatar tác giả (USER.AVATAR_URL) */
    private String authorAvatar;

    /** Nội dung bình luận, cắt ngắn tối đa 200 ký tự */
    private String content;

    /** Thời gian đăng bình luận */
    private LocalDateTime createdAt;
}
