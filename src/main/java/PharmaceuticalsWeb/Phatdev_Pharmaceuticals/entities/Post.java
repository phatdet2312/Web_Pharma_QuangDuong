//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Post.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ POSTS (ÁNH XẠ BẢNG [POSTS])
 * =========================================================================
 * Bài viết nội dung y khoa. ACCESS_LEVEL kiểm soát tầng truy cập (Paywall):
 *   PUBLIC    — ai cũng đọc được (SEO landing page)
 *   PARTNER   — đối tác B2B đã xác thực
 *   DOCTOR    — bác sĩ/dược sĩ có tài khoản chuyên gia
 *   ADMIN     — nội bộ quản trị
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "POSTS")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 500)
    private String title;

    /** Slug URL-friendly, duy nhất để tạo đường dẫn bài viết */
    @Column(name = "SLUG", nullable = false, unique = true, length = 550)
    private String slug;

    @Column(name = "SUMMARY", length = 1000)
    private String summary;

    /** Nội dung HTML/Markdown đầy đủ của bài viết */
    @Column(name = "CONTENT", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "THUMBNAIL_URL", length = 500)
    private String thumbnailUrl;


    /** true = đã xuất bản, false = bản nháp */
    @Builder.Default
    @Column(name = "IS_PUBLISHED", nullable = false)
    private boolean isPublished = false;

    /** true = bài viết nổi bật hiển thị trang chủ, false = bình thường */
    @Builder.Default
    @Column(name = "IS_FEATURED", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "SEO_TITLE", length = 200)
    private String seoTitle;

    @Column(name = "SEO_DESCRIPTION", length = 500)
    private String seoDescription;

    /** Danh mục bài viết (FK → CATEGORIES) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    /** Tác giả bài viết (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHOR_ID", nullable = false)
    private User author;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
