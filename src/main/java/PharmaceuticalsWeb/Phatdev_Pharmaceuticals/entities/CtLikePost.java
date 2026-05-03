//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtLikePost.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_LIKEPOST (ÁNH XẠ BẢNG [CT_LIKEPOST])
 * =========================================================================
 * Lưu vết phản ứng cảm xúc của độc giả đối với một ấn phẩm y khoa.
 * Áp dụng Khóa chính kép (USER_ID, POST_ID) để đảm bảo tính duy nhất.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CT_LIKEPOST")
public class CtLikePost {

    @EmbeddedId
    private CtLikePostId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAILIKE_ID", nullable = false)
    private LoaiLike loaiLike;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Khóa chính kép: (USER_ID, POST_ID)
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CtLikePostId implements Serializable {
        @Column(name = "USER_ID")
        private Long userId;

        @Column(name = "POST_ID")
        private Long postId;
    }
}