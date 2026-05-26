//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/entities/CtEvent.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * =========================================================================
 * THỰC THỂ CT_EVENTS (ÁNH XẠ BẢNG [CT_EVENTS])
 * =========================================================================
 * Một buổi cụ thể (Session) của chiến dịch sự kiện (EVENTS).
 * Một chiến dịch → nhiều buổi, mỗi buổi có địa điểm, giờ và số slot riêng.
 * Trạng thái buổi = bản ghi mới nhất trong CT_EVENT_STATUS_HISTORY.
 * DB có CHECK constraint: END_TIME > START_TIME.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CT_EVENTS")
public class CtEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Chiến dịch chứa buổi này (FK → EVENTS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    /** Địa điểm tổ chức (FK → LOCATIONS) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID", nullable = false)
    private Location location;

    /** Tên riêng của từng buổi (VD: Buổi 1: Carbapenem thế hệ mới) */
    @Column(name = "TITLE", length = 500)
    private String title;

    /** Nội dung mô tả chi tiết buổi (HTML) — admin nhập qua rich-text editor */
    @Column(name = "CONTENT", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    /** Tổng số chỗ đăng ký, >= 0. 0 = không giới hạn */
    @Builder.Default
    @Column(name = "TOTAL_SLOTS", nullable = false)
    private Integer totalSlots = 0;

    @Column(name = "SEO_TITLE", length = 200)
    private String seoTitle;

    @Column(name = "SEO_DESCRIPTION", length = 255)
    private String seoDescription;
}
