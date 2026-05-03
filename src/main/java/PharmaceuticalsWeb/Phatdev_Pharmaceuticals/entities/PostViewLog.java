//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/PostViewLog.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ POST_VIEW_LOGS (ÁNH XẠ BẢNG [POST_VIEW_LOGS])
 * =========================================================================
 * Ghi nhận mỗi lượt xem bài viết. Không có cột VIEW_COUNT trên POSTS (3NF).
 * Số lượt xem = COUNT(*) GROUP BY POST_ID từ bảng này.
 * USER_ID nullable — cho phép ghi nhận lượt xem ẩn danh qua IP.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "POST_VIEW_LOGS")
public class PostViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Bài viết được xem (FK → POSTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    /** IP người xem (ghi nhận cả ẩn danh) */
    @Column(name = "VIEWER_IP", length = 45)
    private String viewerIp;

    /** Người dùng đăng nhập (nullable — khách ẩn danh không có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder.Default
    @Column(name = "VIEWED_AT", nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();
}
