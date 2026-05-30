//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostReactionSummary.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tổng hợp số lượng reaction theo từng loại cảm xúc của bài viết.
 * Dùng cho admin/posts chi tiết — hiển thị nhóm reaction có icon và tên đầy đủ.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReactionSummary {

    /** Mã phân loại cảm xúc (VD: LIKE, LOVE, HELPFUL) */
    private String code;

    /** Tên hiển thị của loại cảm xúc (VD: Yêu thích, Hữu ích) */
    private String name;

    /** Đường dẫn icon của loại cảm xúc */
    private String iconUrl;

    /** Tổng số lượt reaction thuộc loại này */
    private long count;
}
