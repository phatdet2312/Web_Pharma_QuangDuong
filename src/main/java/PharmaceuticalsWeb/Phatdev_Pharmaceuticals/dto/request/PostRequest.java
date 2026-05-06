//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/request/PostRequest.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request tạo mới hoặc cập nhật bài viết y khoa.
 * Dùng cho cả tạo mới lẫn sửa (admin phân biệt bằng POST/PUT).
 */
@Getter
@Setter
public class PostRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề tối đa 500 ký tự")
    private String title;

    /** Slug do admin nhập hoặc hệ thống tự sinh từ title */
    @Size(max = 550, message = "Slug tối đa 550 ký tự")
    private String slug;

    @Size(max = 1000, message = "Tóm tắt tối đa 1000 ký tự")
    private String summary;

    /** Nội dung HTML/Markdown đầy đủ */
    private String content;

    private String thumbnailUrl;

   /** Danh sách ID các Nhóm quyền (USER_ROLES) được phép đọc bài này */
    private List<Integer> roleIds;

    /** true = xuất bản ngay, false = lưu nháp */
    private boolean isPublished = false;

    private String seoTitle;

    private String seoDescription;

    /** ID danh mục (FK → CATEGORIES) */
    private Integer categoryId;

    /** Danh sách ID tag gán cho bài viết */
    private List<Long> tagIds;
}
