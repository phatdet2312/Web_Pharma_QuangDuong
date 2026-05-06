//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu bài viết cho danh sách (card view / list view).
 * Không bao gồm content đầy đủ — dùng PostDetailResponse cho trang chi tiết.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String thumbnailUrl;
    private List<String> allowedRoleNames;
    private boolean isPublished;

    /** Thông tin danh mục */
    private Integer categoryId;
    private String categoryName;
    private String categorySlug;

    /** Thông tin tác giả */
    private Long authorId;
    private String authorName;

    /** Danh sách tag gán cho bài viết */
    private List<TagResponse> tags;

    /** Số lượt xem (từ POST_VIEW_LOGS) */
    private long viewCount;

    /** Tổng lượt tải file (từ CT_FILE_DOWNLOADS) */
    private long downloadCount;

    /** Số bình luận (từ CT_POST_CMT) */
    private long commentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
