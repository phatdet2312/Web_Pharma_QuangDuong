//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/PostFileResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu tài liệu đính kèm bài viết trả về client.
 * downloadCount: tổng lượt tải file này từ CT_FILE_DOWNLOADS.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFileResponse {

    private Long id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;

    /** Tổng lượt tải tài liệu này */
    private long downloadCount;
}
