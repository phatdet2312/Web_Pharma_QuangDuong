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
    @NotBlank(message = "Nội dung bài viết không được để trống")
    @Size(max = 100000, message = "Nội dung tối đa 100.000 ký tự")
    private String content;

    @Size(max = 2000, message = "URL ảnh tối đa 2000 ký tự")
    private String thumbnailUrl;

    /** Danh sách ID các Nhóm quyền (USER_ROLES) được phép đọc bài này */
    private List<Integer> roleIds;

    /** true = xuất bản ngay, false = lưu nháp */
    private boolean isPublished = false;

    @Size(max = 200, message = "SEO Title tối đa 200 ký tự")
    private String seoTitle;

    @Size(max = 500, message = "SEO Description tối đa 500 ký tự")
    private String seoDescription;

    /** ID danh mục (FK → CATEGORIES) */
    private Integer categoryId;

    /** Danh sách ID tag gán cho bài viết */
    private List<Long> tagIds;
}
