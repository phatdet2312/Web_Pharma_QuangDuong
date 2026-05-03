//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Cmt.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CMT (ÁNH XẠ BẢNG [CMT])
 * =========================================================================
 * Bình luận gốc (Root Comment) — THUẦN KHIẾT theo chuẩn 5NF.
 * KHÔNG có cột POST_ID, EVENT_ID trực tiếp.
 * Bình luận này thuộc về bài viết nào / sự kiện nào?
 *   → Tra qua CT_POST_CMT hoặc CT_EVENT_CMT (bảng định tuyến).
 * Thiết kế này cho phép comment có thể xuất hiện ở nhiều ngữ cảnh.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CMT")
public class Cmt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Tác giả bình luận (FK → USERS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    /** Nội dung văn bản bình luận */
    @Column(name = "CONTENT", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
