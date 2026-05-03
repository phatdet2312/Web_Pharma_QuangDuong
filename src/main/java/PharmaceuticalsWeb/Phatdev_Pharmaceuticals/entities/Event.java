//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/Event.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ EVENTS (ÁNH XẠ BẢNG [EVENTS])
 * =========================================================================
 * Chiến dịch sự kiện (Campaign). Một EVENTS có thể có nhiều buổi (CT_EVENTS).
 * SLUG dùng cho URL chiến dịch. THUMBNAIL_URL là ảnh đại diện chiến dịch.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EVENTS")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Loại sự kiện (FK → EVENT_TYPES) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_TYPE_ID")
    private EventType eventType;

    @Column(name = "TITLE", nullable = false, length = 500)
    private String title;

    /** Slug URL-friendly cho trang chiến dịch */
    @Column(name = "SLUG", nullable = false, unique = true, length = 550)
    private String slug;

    /** Mô tả tổng quan chiến dịch */
    @Column(name = "DESCRIPTION", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "THUMBNAIL_URL", length = 500)
    private String thumbnailUrl;

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
