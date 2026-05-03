//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PostFile.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * =========================================================================
 * THỰC THỂ POST_FILES (ÁNH XẠ BẢNG [POST_FILES])
 * =========================================================================
 * Tài liệu đính kèm bài viết: PDF nghiên cứu, slide hội thảo...
 * FILE_SIZE >= 0 được enforce ở DB. FILE_TYPE: pdf, docx, pptx...
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "POST_FILES")
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Bài viết chứa file này (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;

    @Column(name = "FILE_URL", nullable = false, length = 500)
    private String fileUrl;

    /** Loại file: pdf, docx, pptx, xlsx... */
    @Column(name = "FILE_TYPE", length = 20)
    private String fileType;

    /** Kích thước file tính bằng byte, >= 0 */
    @Column(name = "FILE_SIZE")
    private Long fileSize;
}
